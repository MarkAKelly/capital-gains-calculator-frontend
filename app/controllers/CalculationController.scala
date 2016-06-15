/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import java.text.SimpleDateFormat

import predicates._

import connectors.CalculatorConnector
import common.{CustomerTypeKeys, Dates, KeystoreKeys}
import constructors.CalculationElectionConstructor
import forms.OtherPropertiesForm._
import forms.AcquisitionValueForm._
import forms.CustomerTypeForm._
import forms.DisabledTrusteeForm._
import forms.AnnualExemptAmountForm._
import forms.DisposalDateForm._
import forms.DisposalValueForm._
import forms.OtherReliefsForm._
import forms.AllowableLossesForm._
import forms.DisposalCostsForm._
import forms.ImprovementsForm._
import forms.PersonalAllowanceForm._
import forms.AcquisitionCostsForm._
import forms.CurrentIncomeForm._
import forms.CalculationElectionForm._
import forms.AcquisitionDateForm._
import forms.RebasedValueForm._
import forms.RebasedCostsForm._
import forms.PrivateResidenceReliefForm._
import models._
import java.util.{Calendar, Date, UUID}

import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.{Await, Future}
import views.html._
import common.DefaultRoutes._

import scala.concurrent.duration.Duration

object CalculationController extends CalculationController {
  val calcConnector = CalculatorConnector
  val calcElectionConstructor = CalculationElectionConstructor
}

trait CalculationController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  val calcElectionConstructor: CalculationElectionConstructor

  //################### Shared/Common methods #######################
  def getAcquisitionDate(implicit hc: HeaderCarrier): Future[Option[Date]] =
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).map {
      case Some(AcquisitionDateModel("Yes", Some(day), Some(month), Some(year))) => Some(Dates.constructDate(day, month, year))
      case _ => None
    }

  //################### Customer Type methods #######################
  val customerType = Action.async { implicit request =>
    if (request.session.get(SessionKeys.sessionId).isEmpty) {
      val sessionId = UUID.randomUUID.toString
      Future.successful(Ok(calculation.customerType(customerTypeForm)).withSession(request.session + (SessionKeys.sessionId -> s"session-$sessionId")))
    }
    else {
      calcConnector.fetchAndGetFormData[CustomerTypeModel](KeystoreKeys.customerType).map {
        case Some(data) => Ok(calculation.customerType(customerTypeForm.fill(data)))
        case None => Ok(calculation.customerType(customerTypeForm))
      }
    }
  }

  val submitCustomerType = ValidateSession.async { implicit request =>
    customerTypeForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(calculation.customerType(errors))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.customerType, success)
        success.customerType match {
          case CustomerTypeKeys.individual => Future.successful(Redirect(routes.CalculationController.currentIncome()))
          case CustomerTypeKeys.trustee => Future.successful(Redirect(routes.CalculationController.disabledTrustee()))
          case CustomerTypeKeys.personalRep => Future.successful(Redirect(routes.CalculationController.otherProperties()))
        }
      }
    )
  }

  //################### Disabled Trustee methods #######################
  val disabledTrustee = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisabledTrusteeModel](KeystoreKeys.disabledTrustee).map {
      case Some(data) => Ok(calculation.disabledTrustee(disabledTrusteeForm.fill(data)))
      case None => Ok(calculation.disabledTrustee(disabledTrusteeForm))
    }
  }

  val submitDisabledTrustee = ValidateSession.async { implicit request =>
    disabledTrusteeForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(calculation.disabledTrustee(errors))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.disabledTrustee,success)
        Future.successful(Redirect(routes.CalculationController.otherProperties()))
      }
    )
  }

  //################### Current Income methods #######################

  val currentIncome = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[CurrentIncomeModel](KeystoreKeys.currentIncome).map {
      case Some(data) => Ok(calculation.currentIncome(currentIncomeForm.fill(data)))
      case None => Ok(calculation.currentIncome(currentIncomeForm))
    }
  }

  val submitCurrentIncome = ValidateSession.async { implicit request =>
   currentIncomeForm.bindFromRequest.fold(
     errors => Future.successful(BadRequest(calculation.currentIncome(errors))),
     success => {
       calcConnector.saveFormData(KeystoreKeys.currentIncome, success)
       if (success.currentIncome > 0) Future.successful(Redirect(routes.CalculationController.personalAllowance()))
       else Future.successful(Redirect(routes.CalculationController.otherProperties()))
     }
   )
  }

  //################### Personal Allowance methods #######################
  val personalAllowance = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[PersonalAllowanceModel](KeystoreKeys.personalAllowance).map {
      case Some(data) => Ok(calculation.personalAllowance(personalAllowanceForm().fill(data)))
      case None => Ok(calculation.personalAllowance(personalAllowanceForm()))
    }
  }

  val submitPersonalAllowance = ValidateSession.async { implicit request =>
    calcConnector.getPA("2017").flatMap { pa =>
      personalAllowanceForm(pa.get).bindFromRequest.fold(
        errors => Future.successful(BadRequest(calculation.personalAllowance(errors))),
        success => {
          calcConnector.saveFormData(KeystoreKeys.personalAllowance, success)
          Future.successful(Redirect(routes.CalculationController.otherProperties()))
        }
      )
    }
  }

  //################### Other Properties methods #######################
  def otherPropertiesBackUrl(implicit hc: HeaderCarrier): Future[String] =
    calcConnector.fetchAndGetFormData[CustomerTypeModel](KeystoreKeys.customerType).flatMap {
      case Some(CustomerTypeModel("individual")) =>
        calcConnector.fetchAndGetFormData[CurrentIncomeModel](KeystoreKeys.currentIncome).flatMap {
          case Some(data) if data.currentIncome == 0 => Future.successful(routes.CalculationController.currentIncome().url)
          case _ => Future.successful(routes.CalculationController.personalAllowance().url)
        }
      case Some(CustomerTypeModel("trustee")) => Future.successful(routes.CalculationController.disabledTrustee().url)
      case Some(_) => Future.successful(routes.CalculationController.customerType().url)
      case _ => Future.successful(missingDataRoute)
  }

  def showOtherPropertiesAmt(implicit hc: HeaderCarrier): Future[Boolean] = calcConnector.fetchAndGetFormData[CustomerTypeModel](KeystoreKeys.customerType).map {
      case Some(CustomerTypeModel("individual")) => true
      case _ => false
  }

  val otherProperties = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String, showHiddenQuestion: Boolean): Future[Result] = {
      calcConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.otherProperties).map {
        case Some(data) => Ok(calculation.otherProperties(otherPropertiesForm(showHiddenQuestion).fill(data), backUrl, showHiddenQuestion))
        case _ => Ok(calculation.otherProperties(otherPropertiesForm(showHiddenQuestion), backUrl, showHiddenQuestion))
      }
    }

    for {
      backUrl <- otherPropertiesBackUrl
      showHiddenQuestion <- showOtherPropertiesAmt
      finalResult <- routeRequest(backUrl, showHiddenQuestion)
    } yield finalResult
  }

  val submitOtherProperties = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String, showHiddenQuestion: Boolean): Future[Result] = {
      otherPropertiesForm(showHiddenQuestion).bindFromRequest.fold(
        errors =>
          Future.successful(BadRequest(calculation.otherProperties(errors, backUrl, showHiddenQuestion))),
        success => {
          calcConnector.saveFormData(KeystoreKeys.otherProperties, success)
          success match {
            case OtherPropertiesModel("Yes", Some(value)) if value.equals(BigDecimal(0)) => Future.successful(Redirect(routes.CalculationController.annualExemptAmount()))
            case OtherPropertiesModel("Yes", None) if !showHiddenQuestion => Future.successful(Redirect(routes.CalculationController.annualExemptAmount()))
            case _ => calcConnector.saveFormData("annualExemptAmount", AnnualExemptAmountModel(0))
              Future.successful(Redirect(routes.CalculationController.acquisitionDate()))
          }
        }
      )
    }
    for {
      backUrl <- otherPropertiesBackUrl
      showHiddenQuestion <- showOtherPropertiesAmt
      finalResult <- routeRequest(backUrl, showHiddenQuestion)
    } yield finalResult
  }

  //################### Annual Exempt Amount methods #######################
  val annualExemptAmount = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[AnnualExemptAmountModel](KeystoreKeys.annualExemptAmount).map {
      case Some(data) => Ok(calculation.annualExemptAmount(annualExemptAmountForm().fill(data)))
      case None => Ok(calculation.annualExemptAmount(annualExemptAmountForm()))
    }
  }

  val submitAnnualExemptAmount =  ValidateSession.async { implicit request =>

    def customerType(implicit hc: HeaderCarrier): Future[String] = {
      calcConnector.fetchAndGetFormData[CustomerTypeModel](KeystoreKeys.customerType).map {
        customerTypeModel => customerTypeModel.get.customerType
      }
    }

    def trusteeAEA(customerTypeVal: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
      customerTypeVal match {
        case CustomerTypeKeys.trustee =>
          calcConnector.fetchAndGetFormData[DisabledTrusteeModel](KeystoreKeys.disabledTrustee).map {
            disabledTrusteeModel => if (disabledTrusteeModel.get.isVulnerable == "No") false else true
          }
        case _ => Future.successful(true)
      }
    }

    def routeRequest(maxAEA: Option[BigDecimal])(implicit hc: HeaderCarrier): Future[Result] = {
      annualExemptAmountForm(maxAEA).bindFromRequest.fold(
        errors => Future.successful(BadRequest(calculation.annualExemptAmount(errors))),
        success => {
          calcConnector.saveFormData(KeystoreKeys.annualExemptAmount, success)
          Future.successful(Redirect(routes.CalculationController.acquisitionDate()))
        }
      )
    }

    def fetchAEA(isFullAEA: Boolean)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
      if (isFullAEA) {
        calcConnector.getFullAEA("2017")
      }
      else {
        calcConnector.getPartialAEA("2017")
      }
    }

    for {
      customerTypeVal <- customerType
      isDisabledTrustee <- trusteeAEA(customerTypeVal)
      maxAEA <- fetchAEA(isDisabledTrustee)
      finalResult <- routeRequest(maxAEA)
    } yield finalResult
  }

  //################### Acquisition Date methods #######################
  def acquisitionDateBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    calcConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.otherProperties).map {
      case Some(OtherPropertiesModel("Yes", Some(value))) if value == BigDecimal(0) => routes.CalculationController.annualExemptAmount().url
      case None => missingDataRoute
      case _ => routes.CalculationController.otherProperties().url
    }
  }


  val acquisitionDate = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String): Future[Result] = {
      calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).map {
        case Some(data) => Ok(calculation.acquisitionDate(acquisitionDateForm.fill(data), backUrl))
        case None => Ok(calculation.acquisitionDate(acquisitionDateForm, backUrl))
      }
    }

    for {
      backUrl <- acquisitionDateBackUrl
      finalResult <- routeRequest(backUrl)
    } yield finalResult
  }

  val submitAcquisitionDate = ValidateSession.async { implicit request =>
    def routeRequest(backUrl: String): Future[Result] = {
      acquisitionDateForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(calculation.acquisitionDate(errors, backUrl))),
        success => {
          calcConnector.saveFormData(KeystoreKeys.acquisitionDate, success)
          Future.successful(Redirect(routes.CalculationController.acquisitionValue()))
        }
      )
    }

    for {
      backUrl <- acquisitionDateBackUrl
      route <- routeRequest(backUrl)
    } yield route
  }

  //################### Acquisition Value methods #######################
  val acquisitionValue = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[AcquisitionValueModel](KeystoreKeys.acquisitionValue).map {
      case Some(data) => Ok(calculation.acquisitionValue(acquisitionValueForm.fill(data)))
      case None => Ok(calculation.acquisitionValue(acquisitionValueForm))
    }
  }

  val submitAcquisitionValue = ValidateSession.async { implicit request =>
    acquisitionValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(calculation.acquisitionValue(errors))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.acquisitionValue, success)
        calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap (connection =>
        if (!Dates.dateAfterStart(
            connection.get.day.getOrElse(0),
            connection.get.month.getOrElse(0),
            connection.get.year.getOrElse(0))
          )
        {
          Future.successful(Redirect(routes.CalculationController.rebasedValue()))
        }
        else {
          Future.successful(Redirect(routes.CalculationController.improvements()))
        }
        )
      }
    )
  }

  //################### Rebased value methods #######################
  val rebasedValue = ValidateSession.async {implicit request =>
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap(acquisitionDateModel =>
    calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).map {
      case Some(data) => Ok(calculation.rebasedValue(rebasedValueForm.fill(data), acquisitionDateModel.get.hasAcquisitionDate))
      case None => Ok(calculation.rebasedValue(rebasedValueForm, acquisitionDateModel.get.hasAcquisitionDate))
    })
  }

  val submitRebasedValue = ValidateSession.async { implicit request =>
    rebasedValueForm.bindFromRequest.fold(
      errors =>  calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap(acquisitionDateModel =>
        Future.successful(BadRequest(calculation.rebasedValue(errors, acquisitionDateModel.get.hasAcquisitionDate)))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.rebasedValue, success)
        success.hasRebasedValue match {
          case "Yes" => Future.successful(Redirect(routes.CalculationController.rebasedCosts()))
          case "No" => Future.successful(Redirect(routes.CalculationController.improvements()))
        }
      }
    )
  }

  //################### Rebased costs methods #######################
  val rebasedCosts = ValidateSession.async {implicit request =>
    calcConnector.fetchAndGetFormData[RebasedCostsModel](KeystoreKeys.rebasedCosts).map {
      case Some(data) => Ok(calculation.rebasedCosts(rebasedCostsForm.fill(data)))
      case None => Ok(calculation.rebasedCosts(rebasedCostsForm))
    }
  }

  val submitRebasedCosts = ValidateSession.async {implicit request =>
    rebasedCostsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(calculation.rebasedCosts(errors))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.rebasedCosts, success)
        Future.successful(Redirect(routes.CalculationController.improvements()))
      }
    )
  }

  //################### Improvements methods #######################
  def improvementsBackUrl(implicit hc: HeaderCarrier): Future[String] = {

    def checkRebasedValue = {
      calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap {
        case Some(RebasedValueModel("Yes", data)) => Future.successful(routes.CalculationController.rebasedCosts().url)
        case Some(RebasedValueModel("No", data)) => Future.successful(routes.CalculationController.rebasedValue().url)
        case _ => Future.successful(missingDataRoute)
      }
    }

    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap {
      case Some(AcquisitionDateModel("Yes", Some(day), Some(month), Some(year))) if Dates.dateAfterStart(day, month, year) =>
        Future.successful(routes.CalculationController.acquisitionValue().url)
      case None => Future.successful(missingDataRoute)
      case _ => checkRebasedValue
    }
  }

  val improvements = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String): Future[Result] = {
      calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap(rebasedValueModel =>
        calcConnector.fetchAndGetFormData[ImprovementsModel](KeystoreKeys.improvements).map {
          case Some(data) =>
            Ok(calculation.improvements(improvementsForm.fill(data), rebasedValueModel.getOrElse(RebasedValueModel("No", None)).hasRebasedValue, backUrl))
          case None =>
            Ok(calculation.improvements(improvementsForm, rebasedValueModel.getOrElse(RebasedValueModel("No", None)).hasRebasedValue, backUrl))
        }
      )
    }

    for {
      backUrl <- improvementsBackUrl
      route <- routeRequest(backUrl)
    } yield route
  }

  val submitImprovements = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String): Future[Result] = {
      improvementsForm.bindFromRequest.fold(
        errors => {
          calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap(rebasedValueModel =>
            Future.successful(BadRequest(calculation.improvements(errors, rebasedValueModel.getOrElse(RebasedValueModel("No", None)).hasRebasedValue, backUrl))))
        },
        success => {
          calcConnector.saveFormData(KeystoreKeys.improvements, success)
          Future.successful(Redirect(routes.CalculationController.disposalDate()))
        }
      )
    }

    for {
      backUrl <- improvementsBackUrl
      route <- routeRequest(backUrl)
    } yield route
  }

  //################### Disposal Date methods #######################
  val disposalDate = ValidateSession.async { implicit request =>

    def routeRequest(acquisitionDate: Option[Date]): Future[Result] = {
      calcConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate).map {
        case Some(data) => Ok(calculation.disposalDate(disposalDateForm(acquisitionDate).fill(data)))
        case None => Ok(calculation.disposalDate(disposalDateForm(acquisitionDate)))
      }
    }

    for {
      acquisitionDate <- getAcquisitionDate
      route <- routeRequest(acquisitionDate)
    } yield route
  }

  val submitDisposalDate = ValidateSession.async { implicit request =>

    def routeRequest(acquisitionDate: Option[Date]): Future[Result] = {
      disposalDateForm(acquisitionDate).bindFromRequest.fold(
        errors => Future.successful(BadRequest(calculation.disposalDate(errors))),
        success => {
          calcConnector.saveFormData(KeystoreKeys.disposalDate, success)
          if (!Dates.dateAfterStart(success.day, success.month, success.year)) {
            Future.successful(Redirect(routes.CalculationController.noCapitalGainsTax()))
          } else {
            Future.successful(Redirect(routes.CalculationController.disposalValue()))
          }
        }
      )
    }

    for {
      acquisitionDate <- getAcquisitionDate
      route <- routeRequest(acquisitionDate)
    } yield route
  }

  //################### No Capital Gains Tax #######################

  val noCapitalGainsTax = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate).map {
      result => Ok(calculation.noCapitalGainsTax(result.get))
    }
  }

  //################### Disposal Value methods #######################
  val disposalValue = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisposalValueModel](KeystoreKeys.disposalValue).map {
      case Some(data) => Ok(calculation.disposalValue(disposalValueForm.fill(data)))
      case None => Ok(calculation.disposalValue(disposalValueForm))
    }
  }

  val submitDisposalValue = ValidateSession.async { implicit request =>
    disposalValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(calculation.disposalValue(errors))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.disposalValue, success)
        Future.successful(Redirect(routes.CalculationController.acquisitionCosts()))
      }
    )
  }

  //################### Acquisition Costs methods #######################
  val acquisitionCosts = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[AcquisitionCostsModel](KeystoreKeys.acquisitionCosts).map {
      case Some(data) => Ok(calculation.acquisitionCosts(acquisitionCostsForm.fill(data)))
      case None => Ok(calculation.acquisitionCosts(acquisitionCostsForm))
    }
  }

  val submitAcquisitionCosts = ValidateSession.async { implicit request =>
    acquisitionCostsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(calculation.acquisitionCosts(errors))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.acquisitionCosts, success)
        Future.successful(Redirect(routes.CalculationController.disposalCosts()))
      }
    )
  }

  //################### Disposal Costs methods #######################
  val disposalCosts = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisposalCostsModel](KeystoreKeys.disposalCosts).map {
      case Some(data) => Ok(calculation.disposalCosts(disposalCostsForm.fill(data)))
      case None => Ok(calculation.disposalCosts(disposalCostsForm))
    }
  }

  val submitDisposalCosts = ValidateSession.async { implicit request =>
    disposalCostsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(calculation.disposalCosts(errors))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.disposalCosts, success)
        calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap {
          case Some(data) if data.hasAcquisitionDate == "Yes" =>
            Future.successful(Redirect(routes.CalculationController.privateResidenceRelief()))
          case _ => {
            calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap {
              case Some(rebasedData) if rebasedData.hasRebasedValue == "Yes" => {
                Future.successful(Redirect(routes.CalculationController.privateResidenceRelief()))
              }
              case _ => {
                Future.successful(Redirect(routes.CalculationController.allowableLosses()))
              }
            }
          }
        }
      }
    )
  }

  //################### Private Residence Relief methods #######################
  def getDisposalDate(implicit hc: HeaderCarrier): Future[Option[Date]] =
    calcConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate).map {
      case Some(data) => Some(Dates.constructDate(data.day, data.month, data.year))
      case _ => None
  }

  def getRebasedAmount(implicit hc: HeaderCarrier): Future[Boolean] =
    calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).map {
      case Some(data) if data.hasRebasedValue == "Yes" => true
      case _ => false
  }

  def displayBetweenQuestion(disposalDate: Option[Date], acquisitionDate: Option[Date], hasRebasedValue: Boolean): Boolean =
    (disposalDate, acquisitionDate) match {
      case (Some(dDate), Some(aDate)) if Dates.dateAfterOctober(dDate) && !Dates.dateAfterStart(aDate)  => true
      case (Some(dDate), aDateOption) if Dates.dateAfterOctober(dDate) && hasRebasedValue => true
      case _ => false
    }

  def displayBeforeQuestion(disposalDate: Option[Date], acquisitionDate: Option[Date], hasRebasedValue: Boolean): Boolean =
    (disposalDate, acquisitionDate) match {
      case (Some(dDate), Some(aDate)) if Dates.dateAfterOctober(dDate) => true
      case (Some(dDate), Some(aDate)) if !Dates.dateAfterStart(aDate) => true
      case _ => false
    }


  val privateResidenceRelief = ValidateSession.async { implicit request =>

    def action(disposalDate: Option[Date], acquisitionDate: Option[Date], hasRebasedValue: Boolean) = {

      val showBetweenQuestion = displayBetweenQuestion(disposalDate, acquisitionDate, hasRebasedValue)
      val showBeforeQuestion = displayBeforeQuestion(disposalDate, acquisitionDate, hasRebasedValue)
      val disposalDateLess18Months = Dates.dateMinusMonths(disposalDate,18)

      calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief).map {
        case Some(data) => Ok(calculation.privateResidenceRelief(privateResidenceReliefForm(showBeforeQuestion, showBetweenQuestion).fill(data), showBetweenQuestion, showBeforeQuestion, disposalDateLess18Months))
        case None => Ok(calculation.privateResidenceRelief(privateResidenceReliefForm(showBeforeQuestion, showBetweenQuestion), showBetweenQuestion, showBeforeQuestion, disposalDateLess18Months))
      }

    }

    for {
      disposalDate <- getDisposalDate
      acquisitionDate <- getAcquisitionDate
      hasRebasedValue <- getRebasedAmount
      finalResult <- action(disposalDate, acquisitionDate, hasRebasedValue)
    } yield finalResult
  }

  val submitPrivateResidenceRelief = ValidateSession.async { implicit request =>

    def action(disposalDate: Option[Date], acquisitionDate: Option[Date], hasRebasedValue: Boolean) = {
      val showBetweenQuestion = displayBetweenQuestion(disposalDate, acquisitionDate, hasRebasedValue)
      val showBeforeQuestion = displayBeforeQuestion(disposalDate, acquisitionDate, hasRebasedValue)
      val disposalDateLess18Months = Dates.dateMinusMonths(disposalDate,18)
      privateResidenceReliefForm(showBeforeQuestion, showBetweenQuestion).bindFromRequest.fold(
        errors => {
          Future.successful(BadRequest(calculation.privateResidenceRelief(errors,showBetweenQuestion, showBeforeQuestion, disposalDateLess18Months)))
        },
        success => {
          calcConnector.saveFormData(KeystoreKeys.privateResidenceRelief, success)
          Future.successful(Redirect(routes.CalculationController.allowableLosses()))
        }
      )
    }

    for {
      disposalDate <- getDisposalDate
      acquisitionDate <- getAcquisitionDate
      hasRebasedValue <- getRebasedAmount
      finalResult <- action(disposalDate, acquisitionDate, hasRebasedValue)
    } yield finalResult
  }

  //################### Allowable Losses methods #######################
  def allowableLossesBackLink(implicit hc: HeaderCarrier): Future[String] = {
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap {
      case Some(acquisitionData) if acquisitionData.hasAcquisitionDate == "Yes" =>
        Future.successful(routes.CalculationController.privateResidenceRelief().url)
      case _ => calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap {
        case Some(rebasedData) if rebasedData.hasRebasedValue == "Yes" =>
          Future.successful(routes.CalculationController.privateResidenceRelief().url)
        case _ => Future.successful(routes.CalculationController.disposalCosts().url)
      }
    }
  }

  val allowableLosses = ValidateSession.async { implicit request =>
    def routeRequest(backUrl: String) = {
      calcConnector.fetchAndGetFormData[AllowableLossesModel](KeystoreKeys.allowableLosses).map {
        case Some(data) => Ok(calculation.allowableLosses(allowableLossesForm.fill(data), backUrl))
        case None => Ok(calculation.allowableLosses(allowableLossesForm, backUrl))
      }
    }

    for {
      backUrl <- allowableLossesBackLink
      route <- routeRequest(backUrl)
    } yield route
  }

  val submitAllowableLosses = ValidateSession.async { implicit request =>
    def routeRequest(backUrl: String) = {
      allowableLossesForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(calculation.allowableLosses(errors, backUrl))),
        success => {
          calcConnector.saveFormData(KeystoreKeys.allowableLosses, success)
          calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap {
            case Some(data) if data.hasAcquisitionDate == "Yes" && !Dates.dateAfterStart(data.day.get, data.month.get, data.year.get) =>
              Future.successful(Redirect(routes.CalculationController.calculationElection()))
            case _ =>
              calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap {
                case Some(rebasedData) if rebasedData.hasRebasedValue == "Yes" =>
                  Future.successful(Redirect(routes.CalculationController.calculationElection()))
                case _ =>
                  calcConnector.saveFormData(KeystoreKeys.calculationElection, CalculationElectionModel("flat"))
                  Future.successful(Redirect(routes.CalculationController.otherReliefs()))
              }
          }
        }
      )
    }

    for {
      backUrl <- allowableLossesBackLink
      route <- routeRequest(backUrl)
    } yield route
  }

  //################### Calculation Election methods #######################
  def getOtherReliefsFlat(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] =
    calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat).map {
      case Some(data) => data.otherReliefs
      case _ => None
  }

  def getOtherReliefsTA(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] =
    calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA).map {
      case Some(data) => data.otherReliefs
      case _ => None
  }

  def getOtherReliefsRebased(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] =
    calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsRebased).map {
      case Some(data) => data.otherReliefs
      case _ => None
    }

  val calculationElection = ValidateSession.async { implicit request =>

    def action
    (
      construct: SummaryModel,
      content: Seq[(String, String, String, Option[String], String, Option[BigDecimal])]
    ) =
    calcConnector.fetchAndGetFormData[CalculationElectionModel](KeystoreKeys.calculationElection).map {
      case Some(data) =>
        Ok(calculation.calculationElection(
          calculationElectionForm.fill(data),
          construct,
          content)
        )
      case None =>
        Ok(calculation.calculationElection(
          calculationElectionForm,
          construct,
          content)
        )
    }

    def calcTimeCall(summary: SummaryModel): Future[Option[CalculationResultModel]] = {
      summary.acquisitionDateModel.hasAcquisitionDate match {
        case "Yes" if !Dates.dateAfterStart(summary.acquisitionDateModel.day.get, summary.acquisitionDateModel.month.get, summary.acquisitionDateModel.year.get) =>
          calcConnector.calculateTA(summary)
        case _ => Future(None)
      }
    }

    def calcRebasedCall(summary: SummaryModel): Future[Option[CalculationResultModel]] = {
      (summary.rebasedValueModel.getOrElse(RebasedValueModel("No", None)).hasRebasedValue, summary.acquisitionDateModel.hasAcquisitionDate) match {
        case ("Yes", "Yes") if !Dates.dateAfterStart(summary.acquisitionDateModel.day.get, summary.acquisitionDateModel.month.get, summary.acquisitionDateModel.year.get) =>
          calcConnector.calculateRebased(summary)
        case ("Yes", "No") => calcConnector.calculateRebased(summary)
        case _ => Future(None)
      }
    }

    for {
      construct <- calcConnector.createSummary(hc)
      calcFlat <- calcConnector.calculateFlat(construct)
      calcTA <- calcTimeCall(construct)
      calcRebased <- calcRebasedCall(construct)
      otherReliefsFlat <- getOtherReliefsFlat
      otherReliefsTA <- getOtherReliefsTA
      otherReliefsRebased <- getOtherReliefsRebased
      finalResult <- action(
        construct,
        calcElectionConstructor.generateElection(construct, hc, calcFlat, calcTA, calcRebased, otherReliefsFlat, otherReliefsTA, otherReliefsRebased)
      )
    } yield finalResult
  }

  val submitCalculationElection = ValidateSession.async { implicit request =>

    def calcTimeCall(summary: SummaryModel): Future[Option[CalculationResultModel]] = {
      summary.acquisitionDateModel.hasAcquisitionDate match {
        case "Yes" if !Dates.dateAfterStart(summary.acquisitionDateModel.day.get, summary.acquisitionDateModel.month.get, summary.acquisitionDateModel.year.get) =>
          calcConnector.calculateTA(summary)
        case _ => Future(None)
      }
    }

    def calcRebasedCall(summary: SummaryModel): Future[Option[CalculationResultModel]] = {
      (summary.rebasedValueModel.getOrElse(RebasedValueModel("No", None)).hasRebasedValue, summary.acquisitionDateModel.hasAcquisitionDate) match {
        case ("Yes", "Yes") if !Dates.dateAfterStart(summary.acquisitionDateModel.day.get, summary.acquisitionDateModel.month.get, summary.acquisitionDateModel.year.get) =>
          calcConnector.calculateRebased(summary)
        case ("Yes", "No") => calcConnector.calculateRebased(summary)
        case _ => Future(None)
      }
    }

    calculationElectionForm.bindFromRequest.fold(
      errors => {
        for{
          construct <- calcConnector.createSummary(hc)
          calcFlat <- calcConnector.calculateFlat(construct)
          calcTA <- calcTimeCall(construct)
          otherReliefsFlat <- getOtherReliefsFlat
          otherReliefsTA <- getOtherReliefsTA
          otherReliefsRebased <- getOtherReliefsRebased
          calcRebased <- calcRebasedCall(construct)
        } yield {BadRequest(calculation.calculationElection(
          errors,
          construct,
          calcElectionConstructor.generateElection(construct, hc, calcFlat, calcTA, calcRebased, otherReliefsFlat, otherReliefsTA, otherReliefsRebased)
        ))}
      },
      success => {
        calcConnector.saveFormData(KeystoreKeys.calculationElection, success)
        request.body.asFormUrlEncoded.get("action").headOption match {
          case Some("flat") => Future.successful(Redirect(routes.CalculationController.otherReliefs()))
          case Some("time") => Future.successful(Redirect(routes.CalculationController.otherReliefsTA()))
          case Some("rebased") => Future.successful(Redirect(routes.CalculationController.otherReliefsRebased()))
          case _ => Future.successful(Redirect(routes.CalculationController.summary()))
        }
      }
    )
  }

  //################### Other Reliefs methods #######################
  def otherReliefsBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap {
      case (Some(AcquisitionDateModel("Yes", day, month, year))) if Dates.dateAfterStart(day.get, month.get, year.get) =>
        Future.successful(routes.CalculationController.allowableLosses().url)
      case (Some(AcquisitionDateModel("Yes", day, month, year))) => Future.successful(routes.CalculationController.calculationElection().url)
      case (Some(AcquisitionDateModel("No", _, _, _))) =>
        calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap {
          case Some(RebasedValueModel("Yes", _)) => Future.successful(routes.CalculationController.calculationElection().url)
          case Some(RebasedValueModel("No", _)) => Future.successful(routes.CalculationController.allowableLosses().url)
          case _ => Future.successful(missingDataRoute)
        }
      case _ => Future.successful(missingDataRoute)
    }
  }

  val otherReliefs = ValidateSession.async { implicit request =>

    def action (dataResult: Option[CalculationResultModel], backUrl: String) = {
      calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat).map {
        case Some(data) if data.otherReliefs.isDefined => Ok(calculation.otherReliefs(otherReliefsForm.fill(data), dataResult.get, true, backUrl))
        case _ => Ok(calculation.otherReliefs(otherReliefsForm, dataResult.get, false, backUrl))
      }
    }

    for {
      construct <- calcConnector.createSummary(hc)
      calculation <- calcConnector.calculateFlat(construct)
      backUrl <- otherReliefsBackUrl
      finalResult <- action(calculation, backUrl)
    } yield finalResult
  }

  val submitOtherReliefs = ValidateSession.async { implicit request =>

    def action (dataResult: Option[CalculationResultModel], construct: SummaryModel, backUrl: String) = otherReliefsForm.bindFromRequest.fold(
      errors =>
        calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat).map {
          case Some(data) if data.otherReliefs.isDefined => BadRequest(calculation.otherReliefs(errors, dataResult.get, true, backUrl))
          case _ => BadRequest(calculation.otherReliefs(errors, dataResult.get, false, backUrl))
        },
      success => {
        calcConnector.saveFormData(KeystoreKeys.otherReliefsFlat, success)
        (construct.acquisitionDateModel.hasAcquisitionDate, construct.rebasedValueModel.getOrElse(RebasedValueModel("No", None)).hasRebasedValue) match {
          case ("Yes", _) if Dates.dateAfterStart(construct.acquisitionDateModel.day.get,
            construct.acquisitionDateModel.month.get, construct.acquisitionDateModel.year.get) => {
            Future.successful(Redirect(routes.CalculationController.summary()))
          }
          case ("No", "No") => Future.successful(Redirect(routes.CalculationController.summary()))
          case _ => Future.successful(Redirect(routes.CalculationController.calculationElection()))
        }
      }
    )

    for {
      construct <- calcConnector.createSummary(hc)
      calculation <- calcConnector.calculateFlat(construct)
      backUrl <- otherReliefsBackUrl
      finalResult <- action(calculation, construct, backUrl)
    } yield finalResult
  }

  //################### Time Apportioned Other Reliefs methods #######################
  val otherReliefsTA = ValidateSession.async { implicit request =>

    def action (dataResult: Option[CalculationResultModel]) = calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA).map {
      case Some(data) if data.otherReliefs.isDefined=> Ok(calculation.otherReliefsTA(otherReliefsForm.fill(data), dataResult.get, true))
      case _ => Ok(calculation.otherReliefsTA(otherReliefsForm, dataResult.get, false))
    }

    for {
      construct <- calcConnector.createSummary(hc)
      calculation <- calcConnector.calculateTA(construct)
      finalResult <- action(calculation)
    } yield finalResult
  }

  val submitOtherReliefsTA = ValidateSession.async { implicit request =>
    def action(dataResult: Option[CalculationResultModel]) = otherReliefsForm.bindFromRequest.fold(
      errors =>
        calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA).map {
          case Some(data) if data.otherReliefs.isDefined => BadRequest(calculation.otherReliefsTA(errors, dataResult.get, true))
          case _ => BadRequest(calculation.otherReliefsTA(errors, dataResult.get, false))
        },
      success => {
        calcConnector.saveFormData(KeystoreKeys.otherReliefsTA, success)
        Future.successful(Redirect(routes.CalculationController.calculationElection()))
      }
    )

    for {
      construct <- calcConnector.createSummary(hc)
      calculation <- calcConnector.calculateTA(construct)
      finalResult <- action(calculation)
    } yield finalResult
  }

  //################### Rebased Other Reliefs methods #######################
  val otherReliefsRebased = ValidateSession.async { implicit request =>
    def action (dataResult: Option[CalculationResultModel]) = calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsRebased).map {
      case Some(data) if data.otherReliefs.isDefined => Ok(calculation.otherReliefsRebased(otherReliefsForm.fill(data), dataResult.get, true))
      case _ => Ok(calculation.otherReliefsRebased(otherReliefsForm, dataResult.get, false))
    }

    for {
      construct <- calcConnector.createSummary(hc)
      calculation <- calcConnector.calculateRebased(construct)
      finalResult <- action(calculation)
    } yield finalResult
  }

  val submitOtherReliefsRebased = ValidateSession.async { implicit request =>
    def action(dataResult: Option[CalculationResultModel]) = otherReliefsForm.bindFromRequest.fold(
      errors =>
        calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsRebased).map {
          case Some(data) if data.otherReliefs.isDefined => BadRequest(calculation.otherReliefsRebased(errors, dataResult.get, true))
          case _ => BadRequest(calculation.otherReliefsRebased(errors, dataResult.get, false))
        },
      success => {
        calcConnector.saveFormData(KeystoreKeys.otherReliefsRebased, success)
        Future.successful(Redirect(routes.CalculationController.calculationElection()))
      }
    )

    for {
      construct <- calcConnector.createSummary(hc)
      calculation <- calcConnector.calculateRebased(construct)
      finalResult <- action(calculation)
    } yield finalResult
  }

  //################### Summary Methods ##########################
  def summaryBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap {
      case Some(AcquisitionDateModel("Yes", day, month, year)) if Dates.dateAfterStart(day.get, month.get, year.get) =>
        Future.successful(routes.CalculationController.otherReliefs().url)
      case Some(AcquisitionDateModel("Yes", _, _, _)) => Future.successful(routes.CalculationController.calculationElection().url)
      case Some(AcquisitionDateModel("No", _, _, _)) =>
        calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap {
          case Some(RebasedValueModel("Yes", _)) => Future.successful(routes.CalculationController.calculationElection().url)
          case Some(RebasedValueModel("No", _)) => Future.successful(routes.CalculationController.otherReliefs().url)
          case _ => Future.successful(missingDataRoute)
        }
      case _ => Future.successful(missingDataRoute)
    }
  }
  val summary = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String) = {
      calcConnector.createSummary(hc).flatMap(summaryData =>
        summaryData.calculationElectionModel.calculationType match {
          case "flat" =>
            calcConnector.calculateFlat(summaryData).map(result =>
              Ok(calculation.summary(summaryData, result.get, backUrl)))
          case "time" =>
            calcConnector.calculateTA(summaryData).map(result =>
              Ok(calculation.summary(summaryData, result.get, backUrl)))
          case "rebased" =>
            calcConnector.calculateRebased(summaryData).map(result =>
              Ok(calculation.summary(summaryData, result.get, backUrl)))
        })
    }

    for {
      backUrl <- summaryBackUrl
      route <- routeRequest(backUrl)
    } yield route
  }

  def restart(): Action[AnyContent] = Action.async { implicit request =>
    calcConnector.clearKeystore()
    Future.successful(Redirect(routes.CalculationController.customerType()))
  }
}

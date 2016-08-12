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

package controllers.nonresident

import common.{Dates, KeystoreKeys}
import common.nonresident.CustomerTypeKeys
import forms.nonresident.ImprovementsForm._
import forms.nonresident.PrivateResidenceReliefForm._
import forms.nonresident.OtherReliefsForm._
import forms.nonresident.OtherPropertiesForm._
import forms.nonresident.PersonalAllowanceForm._
import forms.nonresident.RebasedCostsForm._
import forms.nonresident.RebasedValueForm._
import forms.nonresident.AllowableLossesForm._
import forms.nonresident.AnnualExemptAmountForm._
import forms.nonresident.AcquisitionValueForm._
import forms.nonresident.CalculationElectionForm._
import forms.nonresident.DisposalDateForm._
import forms.nonresident.DisposalCostsForm._
import forms.nonresident.DisposalValueForm._
import forms.nonresident.DisabledTrusteeForm._
import java.util.{Date, UUID}

import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future
import views.html._
import common.DefaultRoutes._
import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import models.nonresident._

trait CalculationController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.CalculationController.restart().url
  val calcConnector: CalculatorConnector
  val calcElectionConstructor: CalculationElectionConstructor

  //################### Shared/Common methods #######################
  def getAcquisitionDate(implicit hc: HeaderCarrier): Future[Option[Date]] =
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).map {
      case Some(AcquisitionDateModel("Yes", Some(day), Some(month), Some(year))) => Some(Dates.constructDate(day, month, year))
      case _ => None
    }

  //################### Disabled Trustee methods #######################

  //################### Personal Allowance methods #######################
  val personalAllowance = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[PersonalAllowanceModel](KeystoreKeys.personalAllowance).map {
      case Some(data) => Ok(calculation.nonresident.personalAllowance(personalAllowanceForm().fill(data)))
      case None => Ok(calculation.nonresident.personalAllowance(personalAllowanceForm()))
    }
  }

  val submitPersonalAllowance = ValidateSession.async { implicit request =>
    calcConnector.getPA(2017).flatMap { pa =>
      personalAllowanceForm(pa.get).bindFromRequest.fold(
        errors => Future.successful(BadRequest(calculation.nonresident.personalAllowance(errors))),
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
          case Some(data) if data.currentIncome == 0 => Future.successful(routes.CurrentIncomeController.currentIncome().url)
          case _ => Future.successful(routes.CalculationController.personalAllowance().url)
        }
      case Some(CustomerTypeModel("trustee")) => Future.successful(routes.DisabledTrusteeController.disabledTrustee().url)
      case Some(_) => Future.successful(routes.CustomerTypeController.customerType().url)
      case _ => Future.successful(missingDataRoute)
    }

  def showOtherPropertiesAmt(implicit hc: HeaderCarrier): Future[Boolean] = calcConnector.fetchAndGetFormData[CustomerTypeModel](KeystoreKeys.customerType).map {
    case Some(CustomerTypeModel("individual")) => true
    case _ => false
  }

  val otherProperties = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String, showHiddenQuestion: Boolean): Future[Result] = {
      calcConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.otherProperties).map {
        case Some(data) => Ok(calculation.nonresident.otherProperties(otherPropertiesForm(showHiddenQuestion).fill(data), backUrl, showHiddenQuestion))
        case _ => Ok(calculation.nonresident.otherProperties(otherPropertiesForm(showHiddenQuestion), backUrl, showHiddenQuestion))
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
          Future.successful(BadRequest(calculation.nonresident.otherProperties(errors, backUrl, showHiddenQuestion))),
        success => {
          calcConnector.saveFormData(KeystoreKeys.otherProperties, success)
          success match {
            case OtherPropertiesModel("Yes", Some(value)) if value.equals(BigDecimal(0)) => Future.successful(Redirect(routes.AnnualExemptAmountController.annualExemptAmount()))
            case OtherPropertiesModel("Yes", None) if !showHiddenQuestion => Future.successful(Redirect(routes.AnnualExemptAmountController.annualExemptAmount()))
            case _ => calcConnector.saveFormData(KeystoreKeys.annualExemptAmount, AnnualExemptAmountModel(0))
              Future.successful(Redirect(routes.AcquisitionDateController.acquisitionDate()))
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



  //################### Rebased value methods #######################
  val rebasedValue = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap(acquisitionDateModel =>
      calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).map {
        case Some(data) => Ok(calculation.nonresident.rebasedValue(rebasedValueForm.fill(data), acquisitionDateModel.get.hasAcquisitionDate))
        case None => Ok(calculation.nonresident.rebasedValue(rebasedValueForm, acquisitionDateModel.get.hasAcquisitionDate))
      })
  }

  val submitRebasedValue = ValidateSession.async { implicit request =>
    rebasedValueForm.bindFromRequest.fold(
      errors => calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap(acquisitionDateModel =>
        Future.successful(BadRequest(calculation.nonresident.rebasedValue(errors, acquisitionDateModel.get.hasAcquisitionDate)))),
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
  val rebasedCosts = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[RebasedCostsModel](KeystoreKeys.rebasedCosts).map {
      case Some(data) => Ok(calculation.nonresident.rebasedCosts(rebasedCostsForm.fill(data)))
      case None => Ok(calculation.nonresident.rebasedCosts(rebasedCostsForm))
    }
  }

  val submitRebasedCosts = ValidateSession.async { implicit request =>
    rebasedCostsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(calculation.nonresident.rebasedCosts(errors))),
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
        Future.successful(routes.AcquisitionValueController.acquisitionValue().url)
      case None => Future.successful(missingDataRoute)
      case _ => checkRebasedValue
    }
  }

  val improvements = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String): Future[Result] = {
      calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap(rebasedValueModel =>
        calcConnector.fetchAndGetFormData[ImprovementsModel](KeystoreKeys.improvements).map {
          case Some(data) =>
            Ok(calculation.nonresident.improvements(improvementsForm.fill(data), rebasedValueModel.getOrElse(RebasedValueModel("No", None)).hasRebasedValue, backUrl))
          case None =>
            Ok(calculation.nonresident.improvements(improvementsForm, rebasedValueModel.getOrElse(RebasedValueModel("No", None)).hasRebasedValue, backUrl))
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
            Future.successful(BadRequest(calculation.nonresident.improvements(errors, rebasedValueModel.getOrElse(RebasedValueModel("No", None)).hasRebasedValue, backUrl))))
        },
        success => {
          calcConnector.saveFormData(KeystoreKeys.improvements, success)
          Future.successful(Redirect(routes.DisposalDateController.disposalDate()))
        }
      )
    }

    for {
      backUrl <- improvementsBackUrl
      route <- routeRequest(backUrl)
    } yield route
  }

  //################### Disposal Date methods #######################

  //################### No Capital Gains Tax #######################

  //################### Disposal Value methods #######################

  //################### Disposal Costs methods #######################

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
      case (Some(dDate), Some(aDate)) if Dates.dateAfterOctober(dDate) && !Dates.dateAfterStart(aDate) => true
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
      val disposalDateLess18Months = Dates.dateMinusMonths(disposalDate, 18)

      calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief).map {
        case Some(data) => Ok(calculation.nonresident.privateResidenceRelief(privateResidenceReliefForm(showBeforeQuestion, showBetweenQuestion).fill(data), showBetweenQuestion, showBeforeQuestion, disposalDateLess18Months))
        case None => Ok(calculation.nonresident.privateResidenceRelief(privateResidenceReliefForm(showBeforeQuestion, showBetweenQuestion), showBetweenQuestion, showBeforeQuestion, disposalDateLess18Months))
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
      val disposalDateLess18Months = Dates.dateMinusMonths(disposalDate, 18)
      privateResidenceReliefForm(showBeforeQuestion, showBetweenQuestion).bindFromRequest.fold(
        errors => {
          Future.successful(BadRequest(calculation.nonresident.privateResidenceRelief(errors, showBetweenQuestion, showBeforeQuestion, disposalDateLess18Months)))
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
        case _ => Future.successful(routes.DisposalCostsController.disposalCosts().url)
      }
    }
  }

  val allowableLosses = ValidateSession.async { implicit request =>
    def routeRequest(backUrl: String) = {
      calcConnector.fetchAndGetFormData[AllowableLossesModel](KeystoreKeys.allowableLosses).map {
        case Some(data) => Ok(calculation.nonresident.allowableLosses(allowableLossesForm.fill(data), backUrl))
        case None => Ok(calculation.nonresident.allowableLosses(allowableLossesForm, backUrl))
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
        errors => Future.successful(BadRequest(calculation.nonresident.allowableLosses(errors, backUrl))),
        success => {
          calcConnector.saveFormData(KeystoreKeys.allowableLosses, success)
          calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap {
            case Some(data) if data.hasAcquisitionDate == "Yes" && !Dates.dateAfterStart(data.day.get, data.month.get, data.year.get) =>
              Future.successful(Redirect(routes.CalculationElectionController.calculationElection()))
            case _ =>
              calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap {
                case Some(rebasedData) if rebasedData.hasRebasedValue == "Yes" =>
                  Future.successful(Redirect(routes.CalculationElectionController.calculationElection()))
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

  //################### Other Reliefs with no calc selection methods (flat) #######################
  def otherReliefsBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap {
      case (Some(AcquisitionDateModel("Yes", day, month, year))) if Dates.dateAfterStart(day.get, month.get, year.get) =>
        Future.successful(routes.CalculationController.allowableLosses().url)
      case (Some(AcquisitionDateModel("Yes", day, month, year))) => Future.successful(routes.CalculationElectionController.calculationElection().url)
      case (Some(AcquisitionDateModel("No", _, _, _))) =>
        calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap {
          case Some(RebasedValueModel("Yes", _)) => Future.successful(routes.CalculationElectionController.calculationElection().url)
          case Some(RebasedValueModel("No", _)) => Future.successful(routes.CalculationController.allowableLosses().url)
          case _ => Future.successful(missingDataRoute)
        }
      case _ => Future.successful(missingDataRoute)
    }
  }

  val otherReliefs = ValidateSession.async { implicit request =>

    def action(dataResult: Option[CalculationResultModel], backUrl: String) = {
      calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat).map {
        case Some(data) if data.otherReliefs.isDefined => Ok(calculation.nonresident.otherReliefs(otherReliefsForm(false).fill(data), dataResult.get))
        case _ => Ok(calculation.nonresident.otherReliefs(otherReliefsForm(true), dataResult.get))
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

    def action(dataResult: Option[CalculationResultModel], construct: SummaryModel, backUrl: String) = otherReliefsForm(false).bindFromRequest.fold(
      errors =>
        calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat).map {
          case Some(data) if data.otherReliefs.isDefined => BadRequest(calculation.nonresident.otherReliefs(errors, dataResult.get))
          case _ => BadRequest(calculation.nonresident.otherReliefs(errors, dataResult.get))
        },
      success => {
        calcConnector.saveFormData(KeystoreKeys.otherReliefsFlat, success)
        (construct.acquisitionDateModel.hasAcquisitionDate, construct.rebasedValueModel.getOrElse(RebasedValueModel("No", None)).hasRebasedValue) match {
          case ("Yes", _) if Dates.dateAfterStart(construct.acquisitionDateModel.day.get,
            construct.acquisitionDateModel.month.get, construct.acquisitionDateModel.year.get) => {
            Future.successful(Redirect(routes.CalculationController.summary()))
          }
          case ("No", "No") => Future.successful(Redirect(routes.CalculationController.summary()))
          case _ => Future.successful(Redirect(routes.CalculationElectionController.calculationElection()))
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

  //################### Flat Other Reliefs methods #######################

  //################### Time Apportioned Other Reliefs methods #######################
  val otherReliefsTA = ValidateSession.async { implicit request =>

    def action(dataResult: Option[CalculationResultModel]) = calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA).map {
      case Some(data) if data.otherReliefs.isDefined => Ok(calculation.nonresident.otherReliefsTA(otherReliefsForm(false).fill(data), dataResult.get, true))
      case _ => Ok(calculation.nonresident.otherReliefsTA(otherReliefsForm(true), dataResult.get, false))
    }

    for {
      construct <- calcConnector.createSummary(hc)
      calculation <- calcConnector.calculateTA(construct)
      finalResult <- action(calculation)
    } yield finalResult
  }

  val submitOtherReliefsTA = ValidateSession.async { implicit request =>
    def action(dataResult: Option[CalculationResultModel]) = otherReliefsForm(true).bindFromRequest.fold(
      errors =>
        calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA).map {
          case Some(data) if data.otherReliefs.isDefined => BadRequest(calculation.nonresident.otherReliefsTA(errors, dataResult.get, true))
          case _ => BadRequest(calculation.nonresident.otherReliefsTA(errors, dataResult.get, false))
        },
      success => {
        calcConnector.saveFormData(KeystoreKeys.otherReliefsTA, success)
        Future.successful(Redirect(routes.CalculationElectionController.calculationElection()))
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
    def action(dataResult: Option[CalculationResultModel]) = calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsRebased).map {
      case Some(data) if data.otherReliefs.isDefined => Ok(calculation.nonresident.otherReliefsRebased(otherReliefsForm(true).fill(data), dataResult.get, true))
      case _ => Ok(calculation.nonresident.otherReliefsRebased(otherReliefsForm(true), dataResult.get, false))
    }

    for {
      construct <- calcConnector.createSummary(hc)
      calculation <- calcConnector.calculateRebased(construct)
      finalResult <- action(calculation)
    } yield finalResult
  }

  val submitOtherReliefsRebased = ValidateSession.async { implicit request =>
    def action(dataResult: Option[CalculationResultModel]) = otherReliefsForm(true).bindFromRequest.fold(
      errors =>
        calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsRebased).map {
          case Some(data) if data.otherReliefs.isDefined => BadRequest(calculation.nonresident.otherReliefsRebased(errors, dataResult.get, true))
          case _ => BadRequest(calculation.nonresident.otherReliefsRebased(errors, dataResult.get, false))
        },
      success => {
        calcConnector.saveFormData(KeystoreKeys.otherReliefsRebased, success)
        Future.successful(Redirect(routes.CalculationElectionController.calculationElection()))
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
      case Some(AcquisitionDateModel("Yes", _, _, _)) => Future.successful(routes.CalculationElectionController.calculationElection().url)
      case Some(AcquisitionDateModel("No", _, _, _)) =>
        calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap {
          case Some(RebasedValueModel("Yes", _)) => Future.successful(routes.CalculationElectionController.calculationElection().url)
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
              Ok(calculation.nonresident.summary(summaryData, result.get, backUrl)))
          case "time" =>
            calcConnector.calculateTA(summaryData).map(result =>
              Ok(calculation.nonresident.summary(summaryData, result.get, backUrl)))
          case "rebased" =>
            calcConnector.calculateRebased(summaryData).map(result =>
              Ok(calculation.nonresident.summary(summaryData, result.get, backUrl)))
        })
    }

    for {
      backUrl <- summaryBackUrl
      route <- routeRequest(backUrl)
    } yield route
  }

  def restart(): Action[AnyContent] = Action.async { implicit request =>
    calcConnector.clearKeystore(hc)
    Future.successful(Redirect(routes.CustomerTypeController.customerType()))
  }
}

object CalculationController extends CalculationController {
  val calcConnector = CalculatorConnector
  val calcElectionConstructor = CalculationElectionConstructor
}
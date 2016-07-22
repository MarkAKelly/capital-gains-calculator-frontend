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

package controllers.resident.properties

import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import models.resident._
import models.resident.AllowableLossesValueModel
import forms.resident.LossesBroughtForwardForm._
import forms.resident.LossesBroughtForwardValueForm._
import forms.resident.AllowableLossesForm._
import forms.resident.AllowableLossesValueForm._
import forms.resident.AnnualExemptAmountForm._
import forms.resident.OtherPropertiesForm._
import forms.resident.properties.ReliefsForm._
import forms.resident.properties.ReliefsValueForm._
import models.resident.properties.{ReliefsModel, ReliefsValueModel, YourAnswersSummaryModel}
import play.api.mvc.{Action, Result}
import play.api.data.Form
import views.html.calculation.{resident => commonViews}
import views.html.calculation.resident.properties.{deductions => views}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object DeductionsController extends DeductionsController {
  val calcConnector = CalculatorConnector
}

trait DeductionsController extends FeatureLock {

  val calcConnector: CalculatorConnector

  def getDisposalDate(implicit hc: HeaderCarrier): Future[Option[DisposalDateModel]] = {
    calcConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
  }

  def formatDisposalDate(disposalDateModel: DisposalDateModel): Future[String] = {
    Future.successful(s"${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}")
  }

  //################# Reliefs Actions ########################

  def totalGain(answerSummary: YourAnswersSummaryModel, hc: HeaderCarrier): Future[BigDecimal] = calcConnector.calculateRttPropertyGrossGain(answerSummary)(hc)

  def answerSummary(hc: HeaderCarrier): Future[YourAnswersSummaryModel] = calcConnector.getPropertyGainAnswers(hc)

  val reliefs = FeatureLockForRTT.async { implicit request =>

    def routeRequest(totalGain: BigDecimal): Future[Result] = {
      calcConnector.fetchAndGetFormData[ReliefsModel](keystoreKeys.reliefs).map {
        case Some(data) => Ok(views.reliefs(reliefsForm(totalGain).fill(data), totalGain))
        case None => Ok(views.reliefs(reliefsForm(totalGain), totalGain))
      }
    }

    for {
      answerSummary <- answerSummary(hc)
      totalGain <- totalGain(answerSummary, hc)
      route <- routeRequest(totalGain)
    } yield route
  }

  val submitReliefs = FeatureLockForRTT.async { implicit request =>

    def routeRequest (totalGain: BigDecimal) = {
      reliefsForm(totalGain).bindFromRequest().fold(
        errors => Future.successful(BadRequest(views.reliefs(errors, totalGain))),
        success => {
          calcConnector.saveFormData[ReliefsModel](keystoreKeys.reliefs, success)
          success match {
            case ReliefsModel(true) => Future.successful(Redirect(routes.DeductionsController.reliefsValue()))
            case _ => Future.successful(Redirect(routes.DeductionsController.otherProperties()))
          }
        }
      )
    }
    for {
      answerSummary <- answerSummary(hc)
      totalGain <- totalGain(answerSummary, hc)
      route <- routeRequest(totalGain)
    } yield route
  }

  //################# Reliefs Value Input Actions ########################

  val reliefsValue = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[ReliefsValueModel](keystoreKeys.reliefsValue).map {
      case Some(data) => Ok(views.reliefsValue(reliefsValueForm.fill(data)))
      case None => Ok(views.reliefsValue(reliefsValueForm))
    }
  }

  val submitReliefsValue = FeatureLockForRTT.async { implicit request =>
    reliefsValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.reliefsValue(errors))),
      success => {
        calcConnector.saveFormData[ReliefsValueModel](keystoreKeys.reliefsValue, success)
        Future.successful(Redirect(routes.DeductionsController.otherProperties()))
      }
    )
  }

  //################# Other Properties Actions #########################
  def otherPropertiesBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    calcConnector.fetchAndGetFormData[ReliefsModel](keystoreKeys.reliefs).flatMap {
      case Some(ReliefsModel(true)) => Future.successful(routes.DeductionsController.reliefsValue().url)
      case _ => Future.successful(routes.DeductionsController.reliefs().url)
    }
  }

  val otherProperties = FeatureLockForRTT.async { implicit request =>

    def routeRequest(backUrl: String, taxYear: TaxYearModel): Future[Result] = {
      calcConnector.fetchAndGetFormData[OtherPropertiesModel](keystoreKeys.otherProperties).map {
        case Some(data) => Ok(views.otherProperties(otherPropertiesForm.fill(data), backUrl, taxYear))
        case None => Ok(views.otherProperties(otherPropertiesForm, backUrl, taxYear))
      }
    }

    for {
      backUrl <- otherPropertiesBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      finalResult <- routeRequest(backUrl, taxYear.get)
    } yield finalResult
  }

  val submitOtherProperties = FeatureLockForRTT.async { implicit request =>

    def routeRequest(backUrl: String, taxYearModel: TaxYearModel): Future[Result] = {
      otherPropertiesForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(views.otherProperties(errors, backUrl, taxYearModel))),
        success => {
          calcConnector.saveFormData[OtherPropertiesModel](keystoreKeys.otherProperties, success)
          if (success.hasOtherProperties) {
            Future.successful(Redirect(routes.DeductionsController.allowableLosses()))
          } else {
            Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
          }
        }
      )
    }
    for {
      backUrl <- otherPropertiesBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      route <- routeRequest(backUrl, taxYear.get)
    } yield route
  }

  //################# Allowable Losses Actions #########################
  val allowableLosses = FeatureLockForRTT.async { implicit request =>

    def routeRequest(taxYear: TaxYearModel): Future[Result] = {
      calcConnector.fetchAndGetFormData[AllowableLossesModel](keystoreKeys.allowableLosses).map {
        case Some(data) => Ok(commonViews.allowableLosses(allowableLossesForm.fill(data), taxYear))
        case None => Ok(commonViews.allowableLosses(allowableLossesForm, taxYear))
      }
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      finalResult <- routeRequest(taxYear.get)
    } yield finalResult
  }

  val submitAllowableLosses = FeatureLockForRTT.async { implicit request =>

    def routeRequest(taxYear: TaxYearModel): Future[Result] = {
      allowableLossesForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.allowableLosses(errors, taxYear))),
        success => {
          calcConnector.saveFormData[AllowableLossesModel](keystoreKeys.allowableLosses, success)
          if (success.isClaiming) {
            Future.successful(Redirect(routes.DeductionsController.allowableLossesValue()))
          }
          else {
            Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
          }
        }
      )
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      finalResult <- routeRequest(taxYear.get)
    } yield finalResult
  }

  //################# Allowable Losses Value Actions ############################
  val allowableLossesValue = FeatureLockForRTT.async { implicit request =>

    def routeRequest(taxYear: TaxYearModel): Future[Result] = {
      calcConnector.fetchAndGetFormData[AllowableLossesValueModel](keystoreKeys.allowableLossesValue).map {
        case Some(data) => Ok(commonViews.allowableLossesValue(allowableLossesValueForm.fill(data), taxYear))
        case None => Ok(commonViews.allowableLossesValue(allowableLossesValueForm, taxYear))
      }
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      finalResult <- routeRequest(taxYear.get)
    } yield finalResult
  }

  val submitAllowableLossesValue = FeatureLockForRTT.async { implicit request =>

    def routeRequest(taxYearModel: TaxYearModel): Future [Result] = {
      allowableLossesValueForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.allowableLossesValue(errors, taxYearModel))),
        success => {
          calcConnector.saveFormData[AllowableLossesValueModel](keystoreKeys.allowableLossesValue, success)
          Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
        }
      )
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      route <- routeRequest(taxYear.get)
    } yield route
  }

  //################# Brought Forward Losses Actions ############################

  def otherPropertiesCheck(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[OtherPropertiesModel](keystoreKeys.otherProperties).map {
      case Some(data) => data.hasOtherProperties
      case None => false
    }
  }

  def allowableLossesCheck(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[AllowableLossesModel](keystoreKeys.allowableLosses).map {
      case Some(data) => data.isClaiming
      case None => false
    }
  }

  def displayAnnualExemptAmountCheck(claimedOtherProperties: Boolean, claimedAllowableLosses: Boolean)(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[AllowableLossesValueModel](keystoreKeys.allowableLossesValue).map {
      case Some(result) if claimedAllowableLosses && claimedOtherProperties => result.amount == 0
      case _ if claimedOtherProperties && !claimedAllowableLosses => true
      case _ => false
    }
  }

  def displayAnnualExemptAmountCheck(implicit hc: HeaderCarrier): Future[Boolean] = {
    for {
      disposedOtherProperties <- otherPropertiesCheck
      claimedAllowableLosses <- allowableLossesCheck
      displayAnnualExemptAmount <- displayAnnualExemptAmountCheck(disposedOtherProperties, claimedAllowableLosses)
    } yield displayAnnualExemptAmount
  }

  def lossesBroughtForwardBackUrl(implicit hc: HeaderCarrier): Future[String] = {

    for {
      otherPropertiesClaimed <- otherPropertiesCheck
      allowableLossesClaimed <- allowableLossesCheck
    } yield (otherPropertiesClaimed, allowableLossesClaimed)

    match {
      case (false, _) => routes.DeductionsController.otherProperties().url
      case (true, false) => routes.DeductionsController.allowableLosses().url
      case (true, true) => routes.DeductionsController.allowableLossesValue().url
    }
  }

  val lossesBroughtForward = FeatureLockForRTT.async { implicit request =>

    def routeRequest(backLinkUrl: String, taxYear: TaxYearModel): Future[Result] = {
      calcConnector.fetchAndGetFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward).map {
        case Some(data) => Ok(commonViews.lossesBroughtForward(lossesBroughtForwardForm.fill(data), backLinkUrl, taxYear))
        case _ => Ok(commonViews.lossesBroughtForward(lossesBroughtForwardForm, backLinkUrl, taxYear))
      }
    }

    for {
      backLinkUrl <- lossesBroughtForwardBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      finalResult <- routeRequest(backLinkUrl, taxYear.get)
    } yield finalResult

  }

  def positiveChargeableGainCheck(implicit hc: HeaderCarrier): Future[Boolean] = {
    for {
      gainAnswers <- calcConnector.getPropertyGainAnswers
      chargeableGainAnswers <- calcConnector.getPropertyDeductionAnswers
      chargeableGain <- calcConnector.calculateRttPropertyChargeableGain(gainAnswers, chargeableGainAnswers, 11000).map(_.get.chargeableGain)
    } yield chargeableGain

    match {
      case result if result.>(0) => true
      case _ => false
    }
  }

  val submitLossesBroughtForward = FeatureLockForRTT.async { implicit request =>

    def routeRequest(backUrl: String, taxYearModel: TaxYearModel): Future[Result] = {
      lossesBroughtForwardForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.lossesBroughtForward(errors, backUrl, taxYearModel))),
        success => {
          calcConnector.saveFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward, success)

          if (success.option) Future.successful(Redirect(routes.DeductionsController.lossesBroughtForwardValue()))
          else {
            displayAnnualExemptAmountCheck.flatMap { displayAnnualExemptAmount =>
              if (displayAnnualExemptAmount) Future.successful(Redirect(routes.DeductionsController.annualExemptAmount()))
              else {
                positiveChargeableGainCheck.map { positiveChargeableGain =>
                  if (positiveChargeableGain) Redirect(routes.IncomeController.currentIncome())
                  else Redirect(routes.SummaryController.summary())
                }
              }
            }
          }
        }
      )
    }

    for {
      backUrl <- lossesBroughtForwardBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      route <- routeRequest(backUrl, taxYear.get)
    } yield route

  }

  //################# Brought Forward Losses Value Actions ##############################
  val lossesBroughtForwardValue = FeatureLockForRTT.async { implicit request =>

    def retrieveKeystoreData(): Future[Form[LossesBroughtForwardValueModel]] = {
      calcConnector.fetchAndGetFormData[LossesBroughtForwardValueModel](keystoreKeys.lossesBroughtForwardValue).map {
        case Some(data) => lossesBroughtForwardValueForm.fill(data)
        case _ => lossesBroughtForwardValueForm
      }
    }

    def routeRequest(taxYear: TaxYearModel, formData: Form[LossesBroughtForwardValueModel]): Future[Result] = {
      Future.successful(Ok(commonViews.lossesBroughtForwardValue(
        formData,
        taxYear,
        navBackLink = routes.DeductionsController.lossesBroughtForward().url,
        navHomeLink = routes.GainController.disposalDate().url,
        postAction = routes.DeductionsController.submitLossesBroughtForwardValue()
      )))
    }

    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      formData <- retrieveKeystoreData()
      route <- routeRequest(taxYear.get, formData)
    } yield route
  }

  val submitLossesBroughtForwardValue = FeatureLockForRTT.async { implicit request =>

    lossesBroughtForwardValueForm.bindFromRequest.fold(
      errors => { for {
          disposalDate <- getDisposalDate
          disposalDateString <- formatDisposalDate(disposalDate.get)
          taxYear <- calcConnector.getTaxYear(disposalDateString)
        } yield {
          BadRequest(commonViews.lossesBroughtForwardValue(
            errors,
            taxYear.get,
            navBackLink = routes.DeductionsController.lossesBroughtForward().url,
            navHomeLink = routes.GainController.disposalDate().url,
            postAction = routes.DeductionsController.submitLossesBroughtForwardValue()))
        }
      },
      success => {
        calcConnector.saveFormData[LossesBroughtForwardValueModel](keystoreKeys.lossesBroughtForwardValue, success)

        displayAnnualExemptAmountCheck.flatMap { displayAnnualExemptAmount =>
          if (displayAnnualExemptAmount) Future.successful(Redirect(routes.DeductionsController.annualExemptAmount()))
          else {
            positiveChargeableGainCheck.map { positiveChargeableGain =>
              if (positiveChargeableGain) Redirect(routes.IncomeController.currentIncome())
              else Redirect(routes.SummaryController.summary())
            }
          }
        }
      }
    )
  }

  //################# Annual Exempt Amount Input Actions #############################
  val annualExemptAmount = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[AnnualExemptAmountModel](keystoreKeys.annualExemptAmount).map {
      case Some(data) => Ok(commonViews.annualExemptAmount(annualExemptAmountForm().fill(data)))
      case None => Ok(commonViews.annualExemptAmount(annualExemptAmountForm()))
    }
  }

  def positiveAEACheck(model: AnnualExemptAmountModel)(implicit hc: HeaderCarrier): Future[Boolean] = {
    Future(model.amount > 0)
  }

  val submitAnnualExemptAmount = FeatureLockForRTT.async { implicit request =>

    def taxYearStringToInteger (taxYear: String): Future[Int] = {
      Future.successful((taxYear.take(2) + taxYear.takeRight(2)).toInt)
    }

    def formatDisposalDate(disposalDateModel: DisposalDateModel): Future[String] = {
      Future.successful(s"${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}")
    }

    def getMaxAEA(taxYear: Int): Future[Option[BigDecimal]] = {
      calcConnector.getFullAEA(taxYear)
    }

    def routeRequest(maxAEA: BigDecimal): Future[Result] = {
      annualExemptAmountForm(maxAEA).bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.annualExemptAmount(errors))),
        success => {
          for {
            save <- calcConnector.saveFormData(keystoreKeys.annualExemptAmount, success)
            positiveAEA <- positiveAEACheck(success)
            positiveChargeableGain <- positiveChargeableGainCheck
          } yield (positiveAEA, positiveChargeableGain)

          match {
            case (false, true) => Redirect(routes.IncomeController.previousTaxableGains())
            case (_, false) => Redirect(routes.SummaryController.summary())
            case _ => Redirect(routes.IncomeController.currentIncome())
          }
        }
      )
    }
    for {
      disposalDate <- calcConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      year <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- getMaxAEA(year)
      route <- routeRequest(maxAEA.get)
    } yield route
  }
}
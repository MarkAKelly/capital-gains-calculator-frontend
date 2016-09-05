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
import common.resident.JourneyKeys
import config.{AppConfig, ApplicationConfig}
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
import models.resident.properties._
import play.api.mvc.Result
import play.api.data.Form
import play.api.i18n.Messages
import views.html.calculation.{resident => commonViews}
import views.html.calculation.resident.properties.{deductions => views}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object DeductionsController extends DeductionsController {
  val calcConnector = CalculatorConnector
  val config = ApplicationConfig
}

trait DeductionsController extends FeatureLock {

  val calcConnector: CalculatorConnector
  val config: AppConfig

  val navTitle = Messages("calc.base.resident.properties.home")

  def getDisposalDate(implicit hc: HeaderCarrier): Future[Option[DisposalDateModel]] = {
    calcConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
  }

  def formatDisposalDate(disposalDateModel: DisposalDateModel): Future[String] = {
    Future.successful(s"${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}")
  }

  def totalGain(answerSummary: YourAnswersSummaryModel, hc: HeaderCarrier): Future[BigDecimal] = calcConnector.calculateRttPropertyGrossGain(answerSummary)(hc)

  def answerSummary(hc: HeaderCarrier): Future[YourAnswersSummaryModel] = calcConnector.getPropertyGainAnswers(hc)

  override val homeLink = controllers.resident.properties.routes.GainController.disposalDate().url
  override val sessionTimeoutUrl = homeLink

  //########## Private Residence Relief Actions ##############

  //########## Private Residence Relief Value Actions ##############

  //################# Reliefs Actions ########################

  //################# Reliefs Value Input Actions ########################

  //################# Other Properties Actions #########################

  private val otherPropertiesBackUrl = routes.GainController.improvements().url

  val otherProperties = FeatureLockForRTT.async { implicit request =>

    def routeRequest(backUrl: String, taxYear: TaxYearModel): Future[Result] = {
      calcConnector.fetchAndGetFormData[OtherPropertiesModel](keystoreKeys.otherProperties).map {
        case Some(data) => Ok(views.otherProperties(otherPropertiesForm.fill(data), backUrl, taxYear))
        case None => Ok(views.otherProperties(otherPropertiesForm, backUrl, taxYear))
      }
    }

    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      finalResult <- routeRequest(otherPropertiesBackUrl, taxYear.get)
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
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      route <- routeRequest(otherPropertiesBackUrl, taxYear.get)
    } yield route
  }

  //################# Allowable Losses Actions #########################
  val allowableLosses = FeatureLockForRTT.async { implicit request =>

    val postAction = controllers.resident.properties.routes.DeductionsController.submitAllowableLosses()
    val backLink = Some(controllers.resident.properties.routes.DeductionsController.otherProperties().toString())

    def routeRequest(taxYear: TaxYearModel): Future[Result] = {
      calcConnector.fetchAndGetFormData[AllowableLossesModel](keystoreKeys.allowableLosses).map {
        case Some(data) => Ok(commonViews.allowableLosses(allowableLossesForm.fill(data), taxYear, postAction, backLink, homeLink, navTitle))
        case None => Ok(commonViews.allowableLosses(allowableLossesForm, taxYear, postAction, backLink, homeLink, navTitle))
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

    val postAction = controllers.resident.properties.routes.DeductionsController.submitAllowableLosses()
    val backLink = Some(controllers.resident.properties.routes.DeductionsController.otherProperties().toString())

    def routeRequest(taxYear: TaxYearModel): Future[Result] = {
      allowableLossesForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.allowableLosses(errors, taxYear, postAction, backLink, homeLink, navTitle))),
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

  private val allowableLossesValueHomeLink = controllers.resident.properties.routes.GainController.disposalDate().toString
  private val allowableLossesValuePostAction = controllers.resident.properties.routes.DeductionsController.submitAllowableLossesValue()
  private val allowableLossesValueBackLink = Some(controllers.resident.properties.routes.DeductionsController.allowableLosses().toString)

  val allowableLossesValue = FeatureLockForRTT.async { implicit request =>

    def fetchStoredAllowableLosses(): Future[Form[AllowableLossesValueModel]] = {
      calcConnector.fetchAndGetFormData[AllowableLossesValueModel](keystoreKeys.allowableLossesValue).map {
        case Some(data) => allowableLossesValueForm.fill(data)
        case _ => allowableLossesValueForm
      }
    }

    def routeRequest(taxYear: TaxYearModel, formData: Form[AllowableLossesValueModel]): Future[Result] = {
        Future.successful(Ok(commonViews.allowableLossesValue(formData, taxYear,
          allowableLossesValueHomeLink,
          allowableLossesValuePostAction,
          allowableLossesValueBackLink,
          navTitle)))
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      formData <- fetchStoredAllowableLosses()
      finalResult <- routeRequest(taxYear.get, formData)
    } yield finalResult
  }

  val submitAllowableLossesValue = FeatureLockForRTT.async { implicit request =>

    def routeRequest(taxYearModel: TaxYearModel): Future[Result] = {
      allowableLossesValueForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.allowableLossesValue(errors, taxYearModel,
          allowableLossesValueHomeLink,
          allowableLossesValuePostAction,
          allowableLossesValueBackLink,
          navTitle))),
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

  private val lossesBroughtForwardPostAction = controllers.resident.properties.routes.DeductionsController.submitLossesBroughtForward()

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

    def routeRequest(backLinkUrl: String, taxYear: TaxYearModel, otherPropertiesClaimed: Boolean): Future[Result] = {
      calcConnector.fetchAndGetFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward).map {
        case Some(data) => Ok(commonViews.lossesBroughtForward(lossesBroughtForwardForm.fill(data), lossesBroughtForwardPostAction,
          backLinkUrl, taxYear, otherPropertiesClaimed, homeLink, navTitle))
        case _ => Ok(commonViews.lossesBroughtForward(lossesBroughtForwardForm, lossesBroughtForwardPostAction, backLinkUrl,
          taxYear, otherPropertiesClaimed, homeLink, navTitle))
      }
    }

    for {
      backLinkUrl <- lossesBroughtForwardBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      otherPropertiesClaimed <- otherPropertiesCheck
      finalResult <- routeRequest(backLinkUrl, taxYear.get, otherPropertiesClaimed)
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

    def routeRequest(backUrl: String, taxYearModel: TaxYearModel, otherPropertiesClaimed: Boolean): Future[Result] = {
      lossesBroughtForwardForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.lossesBroughtForward(errors, lossesBroughtForwardPostAction, backUrl,
          taxYearModel, otherPropertiesClaimed, homeLink, navTitle))),
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
      otherPropertiesClaimed <- otherPropertiesCheck
      route <- routeRequest(backUrl, taxYear.get, otherPropertiesClaimed)
    } yield route

  }

  //################# Brought Forward Losses Value Actions ##############################
  private val lossesBroughtForwardValueBackLink   = routes.DeductionsController.lossesBroughtForward().url
  private val lossesBroughtForwardValuePostAction = routes.DeductionsController.submitLossesBroughtForwardValue()

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
        navBackLink = lossesBroughtForwardValueBackLink,
        navHomeLink = homeLink,
        postAction = lossesBroughtForwardValuePostAction,
        navTitle = navTitle
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
            navBackLink = lossesBroughtForwardValueBackLink,
            navHomeLink = homeLink,
            postAction = lossesBroughtForwardValuePostAction,
            navTitle = navTitle))
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

  private def annualExemptAmountBackLink(implicit hc: HeaderCarrier): Future[Option[String]] = calcConnector
    .fetchAndGetFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward).map {
    case Some(LossesBroughtForwardModel(true)) =>
      Some(controllers.resident.properties.routes.DeductionsController.lossesBroughtForwardValue().toString)
    case _ =>
      Some(controllers.resident.properties.routes.DeductionsController.lossesBroughtForward().toString)
  }
  private val annualExemptAmountPostAction = controllers.resident.properties.routes.DeductionsController.submitAnnualExemptAmount()

  val annualExemptAmount = FeatureLockForRTT.async { implicit request =>

    def routeRequest(backLink: Option[String]) = {
      calcConnector.fetchAndGetFormData[AnnualExemptAmountModel](keystoreKeys.annualExemptAmount).map {
        case Some(data) => Ok(commonViews.annualExemptAmount(annualExemptAmountForm().fill(data), backLink, annualExemptAmountPostAction,
          homeLink, JourneyKeys.properties, navTitle))
        case None => Ok(commonViews.annualExemptAmount(annualExemptAmountForm(), backLink, annualExemptAmountPostAction, homeLink,
          JourneyKeys.properties, navTitle))
      }
    }

    for {
      backLink <- annualExemptAmountBackLink(hc)
      result <- routeRequest(backLink)
    } yield result
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

    def routeRequest(maxAEA: BigDecimal, backLink: Option[String]): Future[Result] = {
      annualExemptAmountForm(maxAEA).bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.annualExemptAmount(errors, backLink, annualExemptAmountPostAction, homeLink,
          JourneyKeys.properties, navTitle))),
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
      backLink <- annualExemptAmountBackLink(hc)
      route <- routeRequest(maxAEA.get, backLink)
    } yield route
  }
}
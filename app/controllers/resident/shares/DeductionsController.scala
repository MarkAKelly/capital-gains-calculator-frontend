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

package controllers.resident.shares

import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import models.resident._
import models.resident.AllowableLossesValueModel
import models.resident.shares.GainAnswersModel
import forms.resident.LossesBroughtForwardValueForm._
import forms.resident.AllowableLossesForm._
import play.api.mvc.Result
import play.api.data.Form
import views.html.calculation.{resident => commonViews}
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

  def totalGain(answerSummary: GainAnswersModel, hc: HeaderCarrier): Future[BigDecimal] = calcConnector.calculateRttShareGrossGain(answerSummary)(hc)

  def answerSummary(hc: HeaderCarrier): Future[GainAnswersModel] = calcConnector.getShareGainAnswers(hc)


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

  def positiveChargeableGainCheck(implicit hc: HeaderCarrier): Future[Boolean] = {
    for {
      gainAnswers <- calcConnector.getShareGainAnswers
      chargeableGainAnswers <- calcConnector.getShareDeductionAnswers
      chargeableGain <- calcConnector.calculateRttShareChargeableGain(gainAnswers, chargeableGainAnswers, 11000).map(_.get.chargeableGain)
    } yield chargeableGain

    match {
      case result if result.>(0) => true
      case _ => false
    }
  }

  //################# Brought Forward Losses Actions ##############################
  val lossesBroughtForward = TODO

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
          postAction = routes.DeductionsController.submitLossesBroughtForwardValue))
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

  //################# Other Disposal Actions #########################
  val otherDisposals = TODO

  //################# Allowable Losses Actions #########################
  val allowableLosses = FeatureLockForRTT.async { implicit request =>

    val postAction = controllers.resident.shares.routes.DeductionsController.submitAllowableLosses()
    val backLink = Some(controllers.resident.shares.routes.DeductionsController.otherDisposals.toString())

    def routeRequest(taxYear: TaxYearModel): Future[Result] = {
      calcConnector.fetchAndGetFormData[AllowableLossesModel](keystoreKeys.allowableLosses).map {
        case Some(data) => Ok(commonViews.allowableLosses(allowableLossesForm.fill(data), taxYear, postAction, backLink))
        case None => Ok(commonViews.allowableLosses(allowableLossesForm, taxYear, postAction, backLink))
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

    val postAction = controllers.resident.shares.routes.DeductionsController.submitAllowableLosses()
    val backLink = Some(controllers.resident.properties.routes.DeductionsController.otherProperties.toString())

    def routeRequest(taxYear: TaxYearModel): Future[Result] = {
      allowableLossesForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.allowableLosses(errors, taxYear, postAction, backLink))),
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
  val allowableLossesValue = TODO

  //################# Annual Exempt Amount Actions ############################
  val annualExemptAmount = TODO

}
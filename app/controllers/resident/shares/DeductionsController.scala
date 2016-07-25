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

  //################# Other Disposal Actions #########################

  val otherDisposals = TODO

  //################# Allowable Losses Actions #########################

  val allowableLosses = FeatureLockForRTT.async { implicit request =>

    val postAction = controllers.resident.shares.routes.DeductionsController.submitAllowableLosses()
    val backLink = Some(controllers.resident.shares.routes.DeductionsController.otherDisposals().toString())

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
    val backLink = Some(controllers.resident.shares.routes.DeductionsController.otherDisposals().toString())

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

  private val allowableLossesValueHomeLink = controllers.resident.shares.routes.GainController.disposalDate().toString
  private val allowableLossesValuePostAction = controllers.resident.shares.routes.DeductionsController.submitAllowableLossesValue()
  private val allowableLossesValueBackLink = Some(controllers.resident.shares.routes.DeductionsController.allowableLosses().toString)

  val allowableLossesValue = FeatureLockForRTT.async { implicit request =>

    def fetchStoredAllowableLosses(): Future[Form[AllowableLossesValueModel]]  = {
      calcConnector.fetchAndGetFormData[AllowableLossesValueModel](keystoreKeys.allowableLossesValue).map {
        case Some(data) => allowableLossesValueForm.fill(data)
        case _ => allowableLossesValueForm
      }
    }

    def routeRequest(taxYear: TaxYearModel, formData: Form[AllowableLossesValueModel]): Future[Result] = {
      Future.successful(Ok(commonViews.allowableLossesValue(formData, taxYear,
        allowableLossesValueHomeLink,
        allowableLossesValuePostAction,
        allowableLossesValueBackLink)))
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
          allowableLossesValueBackLink))),
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

  val lossesBroughtForward = TODO

  //################# Brought Forward Losses Value Actions ##############################

  val lossesBroughtForwardValue = TODO

  //################# Annual Exempt Amount Input Actions #############################

  val annualExemptAmount = TODO
}

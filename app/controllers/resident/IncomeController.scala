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

package controllers.resident

import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import views.html.calculation.resident.{income => views}
import scala.concurrent.Future
import forms.resident.income.PreviousTaxableGainsForm._
import forms.resident.PersonalAllowanceForm._
import models.resident._
import models.resident.income._
import play.api.mvc.Result
import uk.gov.hmrc.play.http.HeaderCarrier

object IncomeController extends IncomeController {
  val calcConnector = CalculatorConnector
}

trait IncomeController extends FeatureLock {

  val calcConnector: CalculatorConnector

  def otherPropertiesResponse(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.ResidentKeys.otherProperties).map {
      case Some(OtherPropertiesModel(response)) => response
      case None => false
    }
  }

  def lossesBroughtForwardResponse(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[LossesBroughtForwardModel](KeystoreKeys.ResidentKeys.lossesBroughtForward).map {
      case Some(LossesBroughtForwardModel(response)) => response
      case None => false
    }
  }

  //################################# Previous Taxable Gain Actions ##########################################
  def buildPreviousTaxableGainsBackUrl(implicit hc: HeaderCarrier): Future[String] = {

    for {
      hasOtherProperties <- otherPropertiesResponse
      hasLossesBroughtForward <- lossesBroughtForwardResponse
    } yield (hasOtherProperties, hasLossesBroughtForward)

    match {
      case (true, _) => routes.DeductionsController.annualExemptAmount().url
      case (false, true) => routes.DeductionsController.lossesBroughtForwardValue().url
      case (false, false) => routes.DeductionsController.lossesBroughtForward().url
    }
  }

  val previousTaxableGains = FeatureLockForRTT.async { implicit request =>

    def routeRequest(backUrl: String): Future[Result] = {
      calcConnector.fetchAndGetFormData[PreviousTaxableGainsModel](KeystoreKeys.ResidentKeys.previousTaxableGains).map {
        case Some(data) => Ok(views.previousTaxableGains(previousTaxableGainsForm.fill(data), backUrl))
        case None => Ok(views.previousTaxableGains(previousTaxableGainsForm, backUrl))
      }
    }

    for {
      backUrl <- buildPreviousTaxableGainsBackUrl
      finalResult <- routeRequest(backUrl)
    } yield finalResult
  }

  val submitPreviousTaxableGains = FeatureLockForRTT.async { implicit request =>

    previousTaxableGainsForm.bindFromRequest.fold(
      errors => buildPreviousTaxableGainsBackUrl.flatMap(url => Future.successful(BadRequest(views.previousTaxableGains(errors, url)))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.ResidentKeys.previousTaxableGains, success)
        Future.successful(Redirect(routes.IncomeController.previousTaxableGains()))
      }
    )
  }

  //################################# Current Income Actions ##########################################
  val currentIncome = FeatureLockForRTT.async { implicit request =>
    Future.successful(Ok(views.currentIncome()))
  }

  //################################# Personal Allowance Actions ##########################################
  val personalAllowance = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[PersonalAllowanceModel](KeystoreKeys.ResidentKeys.personalAllowance).map {
      case Some(data) => Ok(views.personalAllowance(personalAllowanceForm.fill(data)))
      case None => Ok(views.personalAllowance(personalAllowanceForm))
    }
  }
  val submitPersonalAllowance = FeatureLockForRTT.async { implicit request =>
    personalAllowanceForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.personalAllowance(errors))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.ResidentKeys.personalAllowance, success)
        Future.successful(Redirect(routes.SummaryController.summary()))
      }
    )

  }
}

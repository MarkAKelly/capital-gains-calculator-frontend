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
import forms.resident.income.PreviousTaxableGainsForm._
import forms.resident.income.PersonalAllowanceForm._
import forms.resident.income.CurrentIncomeForm._
import models.resident._
import models.resident.income._
import play.api.mvc.Result
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

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

  def annualExemptAmountEntered(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[AnnualExemptAmountModel](KeystoreKeys.ResidentKeys.annualExemptAmount).map {
      case Some(data) =>
        if(data.amount.equals(0)) true
        else false
      case None => false
    }
  }

  def allowableLossesCheck(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[AllowableLossesModel](KeystoreKeys.ResidentKeys.allowableLosses).map {
      case Some(data) => data.isClaiming
      case None => false
    }
  }

  def displayAEACheck(claimedOtherProperties: Boolean, claimedAllowableLosses: Boolean)(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[AllowableLossesValueModel](KeystoreKeys.ResidentKeys.allowableLossesValue).map {
      case Some(result) if claimedAllowableLosses && claimedOtherProperties => result.amount != 0
      case _ if claimedOtherProperties && !claimedAllowableLosses => true
      case _ => false
    }
  }

  //################################# Previous Taxable Gain Actions ##########################################
  def buildPreviousTaxableGainsBackUrl(implicit hc: HeaderCarrier): Future[String] = {

    for {
      hasOtherProperties <- otherPropertiesResponse
      hasAllowableLosses <- allowableLossesCheck
      displayAEA <- displayAEACheck(hasOtherProperties, hasAllowableLosses)
      hasLossesBroughtForward <- lossesBroughtForwardResponse
    } yield (displayAEA, hasLossesBroughtForward)

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
        Future.successful(Redirect(routes.IncomeController.currentIncome()))
      }
    )
  }

  //################################# Current Income Actions ##########################################

  def buildCurrentIncomeBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    for {
      hasOtherProperties <- otherPropertiesResponse
      hasAllowableLosses <- allowableLossesCheck
      displayAEA <- displayAEACheck(hasOtherProperties, hasAllowableLosses)
      hasLossesBroughtForward <- lossesBroughtForwardResponse
      enteredAnnualExemptAmount <- annualExemptAmountEntered
    } yield (displayAEA, hasLossesBroughtForward, enteredAnnualExemptAmount)

    match {
      case (true, _, true) => routes.IncomeController.previousTaxableGains().url
      case (true, _, _) => routes.DeductionsController.annualExemptAmount().url
      case (false, true, _) => routes.DeductionsController.lossesBroughtForwardValue().url
      case (false, false, _) => routes.DeductionsController.lossesBroughtForward().url
    }
  }

  val currentIncome = FeatureLockForRTT.async { implicit request =>

    def routeRequest(backUrl: String): Future[Result] = {
      calcConnector.fetchAndGetFormData[CurrentIncomeModel](KeystoreKeys.ResidentKeys.currentIncome).map {
        case Some(data) => Ok(views.currentIncome(currentIncomeForm.fill(data), backUrl))
        case None => Ok(views.currentIncome(currentIncomeForm, backUrl))
      }
    }

    for {
      backUrl <- buildCurrentIncomeBackUrl
      finalResult <- routeRequest(backUrl)
    } yield finalResult
  }

  val submitCurrentIncome = FeatureLockForRTT.async { implicit request =>
    currentIncomeForm.bindFromRequest.fold(
      errors => buildCurrentIncomeBackUrl.flatMap(url => Future.successful(BadRequest(views.currentIncome(errors, url)))),
      success => {
        calcConnector.saveFormData[CurrentIncomeModel](KeystoreKeys.ResidentKeys.currentIncome, success)
        Future.successful(Redirect(routes.IncomeController.personalAllowance()))
      }
    )
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

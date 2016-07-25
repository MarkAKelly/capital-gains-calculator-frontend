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

import common.Dates
import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import views.html.calculation.{resident => commonViews}
//import views.html.calculation.resident.shares.{income => views}
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
    calcConnector.fetchAndGetFormData[OtherPropertiesModel](keystoreKeys.otherProperties).map {
      case Some(OtherPropertiesModel(response)) => response
      case None => false
    }
  }

  def lossesBroughtForwardResponse(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward).map {
      case Some(LossesBroughtForwardModel(response)) => response
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

  private val homeLink = controllers.resident.shares.routes.GainController.disposalDate().url

  //################################# Previous Taxable Gain Actions ##########################################

  private val previousTaxableGainsPostAction = controllers.resident.shares.routes.IncomeController.submitPreviousTaxableGains

  def buildPreviousTaxableGainsBackUrl(implicit hc: HeaderCarrier): Future[String] = {

    for {
      hasOtherProperties <- otherPropertiesResponse
      hasAllowableLosses <- allowableLossesCheck
      displayAnnualExemptAmount <- displayAnnualExemptAmountCheck(hasOtherProperties, hasAllowableLosses)
      hasLossesBroughtForward <- lossesBroughtForwardResponse
    } yield (displayAnnualExemptAmount, hasLossesBroughtForward)

    match {
      case (true, _) => routes.DeductionsController.annualExemptAmount().url
      case (false, true) => routes.DeductionsController.lossesBroughtForwardValue().url
      case (false, false) => routes.DeductionsController.lossesBroughtForward().url
    }
  }

  val previousTaxableGains = FeatureLockForRTTShares.async { implicit request =>

    def routeRequest(backUrl: String): Future[Result] = {
      calcConnector.fetchAndGetFormData[PreviousTaxableGainsModel](keystoreKeys.previousTaxableGains).map {
        case Some(data) => Ok(commonViews.previousTaxableGains(previousTaxableGainsForm.fill(data), backUrl, previousTaxableGainsPostAction, homeLink))
        case None => Ok(commonViews.previousTaxableGains(previousTaxableGainsForm, backUrl, previousTaxableGainsPostAction, homeLink))
      }
    }

    for {
      backUrl <- buildPreviousTaxableGainsBackUrl
      finalResult <- routeRequest(backUrl)
    } yield finalResult
  }

  val submitPreviousTaxableGains = FeatureLockForRTTShares.async { implicit request =>

    previousTaxableGainsForm.bindFromRequest.fold(
      errors => buildPreviousTaxableGainsBackUrl.flatMap(url =>
        Future.successful(BadRequest(commonViews.previousTaxableGains(errors, url, previousTaxableGainsPostAction, homeLink)))),
      success => {
        calcConnector.saveFormData(keystoreKeys.previousTaxableGains, success)
        Future.successful(Redirect(routes.IncomeController.currentIncome()))
      }
    )
  }
  //################################# Current Income Actions ##########################################

  val currentIncome = TODO

  val submitCurrentIncome = TODO

  //################################# Personal Allowance Actions ##########################################

  val personalAllowance = TODO

  val submitPersonalAllowance = TODO
}

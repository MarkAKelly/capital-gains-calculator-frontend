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
import views.html.calculation.resident.shares.{income => views}
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

  def annualExemptAmountEntered(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[AnnualExemptAmountModel](keystoreKeys.annualExemptAmount).map {
      case Some(data) => data.amount == 0
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

  def getDisposalDate(implicit hc: HeaderCarrier): Future[Option[DisposalDateModel]] = {
    calcConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
  }

  def formatDisposalDate(disposalDateModel: DisposalDateModel): Future[String] = {
    Future.successful(s"${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}")
  }

  //################################# Previous Taxable Gain Actions ##########################################

  val previousTaxableGains = TODO

  val submitPreviousTaxableGains = TODO

  //################################# Current Income Actions ##########################################

  def buildCurrentIncomeBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    for {
      hasOtherProperties <- otherPropertiesResponse
      hasAllowableLosses <- allowableLossesCheck
      displayAnnualExemptAmount <- displayAnnualExemptAmountCheck(hasOtherProperties, hasAllowableLosses)
      hasLossesBroughtForward <- lossesBroughtForwardResponse
      enteredAnnualExemptAmount <- annualExemptAmountEntered
    } yield (displayAnnualExemptAmount, hasLossesBroughtForward, enteredAnnualExemptAmount)

    match {
      case (true, _, true) => routes.IncomeController.previousTaxableGains().url
      case (true, _, _) => routes.DeductionsController.annualExemptAmount().url
      case (false, true, _) => routes.DeductionsController.lossesBroughtForwardValue().url
      case (false, false, _) => routes.DeductionsController.lossesBroughtForward().url
    }
  }

  val currentIncome = FeatureLockForRTT.async { implicit request =>

    def routeRequest(backUrl: String, taxYear: TaxYearModel): Future[Result] = {
      calcConnector.fetchAndGetFormData[CurrentIncomeModel](keystoreKeys.currentIncome).map {
        case Some(data) => Ok(views.currentIncome(currentIncomeForm.fill(data), backUrl, taxYear))
        case None => Ok(views.currentIncome(currentIncomeForm, backUrl, taxYear))
      }
    }

    for {
      backUrl <- buildCurrentIncomeBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      finalResult <- routeRequest(backUrl, taxYear.get)
    } yield finalResult
  }

  val submitCurrentIncome = FeatureLockForRTT.async { implicit request =>

    def routeRequest(taxYearModel: TaxYearModel): Future[Result] = {
      currentIncomeForm.bindFromRequest.fold(
        errors => buildCurrentIncomeBackUrl.flatMap(url => Future.successful(BadRequest(views.currentIncome(errors, url, taxYearModel)))),
        success => {
          calcConnector.saveFormData[CurrentIncomeModel](keystoreKeys.currentIncome, success)
          Future.successful(Redirect(routes.IncomeController.personalAllowance()))
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

  //################################# Personal Allowance Actions ##########################################

  val personalAllowance = TODO

  val submitPersonalAllowance = TODO
}

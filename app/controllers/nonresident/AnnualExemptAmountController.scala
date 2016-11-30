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

import common.KeystoreKeys
import common.nonresident.CustomerTypeKeys
import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import forms.nonresident.AnnualExemptAmountForm._
import models.nonresident._
import common.Dates
import play.api.data.Form
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation
import common.DefaultRoutes._

import scala.concurrent.Future

object AnnualExemptAmountController extends AnnualExemptAmountController{
  val calcConnector = CalculatorConnector
  val calcElectionConstructor = CalculationElectionConstructor
}

trait AnnualExemptAmountController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url
  val calcConnector: CalculatorConnector
  val calcElectionConstructor: CalculationElectionConstructor

  private def fetchMaxAEA(isFullAEA: Boolean, taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    if (isFullAEA) {
      calcConnector.getFullAEA(taxYear)
    }
    else {
      calcConnector.getPartialAEA(taxYear)
    }
  }

  private def fetchAnnualExemptAmount(implicit headerCarrier: HeaderCarrier): Future[Option[AnnualExemptAmountModel]] = {
    calcConnector.fetchAndGetFormData[AnnualExemptAmountModel](KeystoreKeys.annualExemptAmount)
  }

  private def fetchPreviousGainOrLoss(implicit hc: HeaderCarrier): Future[String] = {
    calcConnector.fetchAndGetFormData[PreviousLossOrGainModel](KeystoreKeys.previousLossOrGain).map {
      previousLossOrGainModel => previousLossOrGainModel.get.previousLossOrGain
    }
  }

  private def fetchPreviousLossAmount(gainOrLoss: String): Future[Option[HowMuchLossModel]] = {
    gainOrLoss match {
      case "Gain" => calcConnector.fetchAndGetFormData[HowMuchLossModel](KeystoreKeys.howMuchLoss)
      case _ => Future.successful(None)
    }
  }

  private def fetchPreviousGainAmount(gainOrLoss: String): Future[Option[HowMuchGainModel]] = {
    gainOrLoss match {
      case "Loss" => calcConnector.fetchAndGetFormData[HowMuchGainModel](KeystoreKeys.howMuchGain)
      case _ => Future.successful(None)
    }
  }

  private def fetchDisposalDate(implicit hc: HeaderCarrier): Future[Option[DisposalDateModel]] = {
    calcConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate)
  }

  private def customerType(implicit hc: HeaderCarrier): Future[String] = {
    calcConnector.fetchAndGetFormData[CustomerTypeModel](KeystoreKeys.customerType).map {
      customerTypeModel => customerTypeModel.get.customerType
    }
  }

  private def trusteeAEA(customerTypeVal: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    customerTypeVal match {
      case CustomerTypeKeys.trustee =>
        calcConnector.fetchAndGetFormData[DisabledTrusteeModel](KeystoreKeys.disabledTrustee).map {
          disabledTrusteeModel => if (disabledTrusteeModel.get.isVulnerable == "No") false else true
        }
      case _ => Future.successful(true)
    }
  }

  private def backUrl(lossOrGain: PreviousLossOrGainModel, gainAmount: Option[HowMuchGainModel], lossAmount: Option[HowMuchLossModel]): Future[String] = {
    (lossOrGain, gainAmount, lossAmount) match {
      case (Some(PreviousLossOrGainModel("Neither")), None, None) =>
        Future.successful(routes.PreviousGainOrLossController.previousGainOrLoss().url)
      case (Some(PreviousLossOrGainModel("Gain")), Some(data), None) if data.howMuchGain == 0 =>
        Future.successful(routes.HowMuchGainController.howMuchGain().url)
      case (Some(PreviousLossOrGainModel("Loss")), None, Some(data)) if data.loss == 0 =>
        Future.successful(routes.HowMuchLossController.howMuchLoss().url)
      case _ => Future.successful(missingDataRoute)
    }
  }

  val annualExemptAmount = ValidateSession.async { implicit request =>

    def routeRequest(model: Option[AnnualExemptAmountModel], maxAEA: BigDecimal, backUrl: String): Future[Result] = {
      calcConnector.fetchAndGetFormData[AnnualExemptAmountModel](KeystoreKeys.annualExemptAmount).map {
        case Some(data) => Ok(calculation.nonresident.annualExemptAmount(annualExemptAmountForm().fill(data), maxAEA))
        case None => Ok(calculation.nonresident.annualExemptAmount(annualExemptAmountForm(), maxAEA))
      }
    }

    for {
      disposalDate <- fetchDisposalDate(hc)
      customerTypeVal <- customerType(hc)
      isDisabledTrustee <- trusteeAEA(customerTypeVal)
      taxYear <- Future.successful(Dates.getDisposalYear(disposalDate.get.day, disposalDate.get.month, disposalDate.get.year))
      maxAEA <- fetchMaxAEA(isDisabledTrustee, taxYear)
      annualExemptAmount <- fetchAnnualExemptAmount(hc)
      previousLossOrGain <- fetchPreviousGainOrLoss(hc)
      gainAmount <- fetchPreviousGainAmount(previousLossOrGain)
      lossAmount <- fetchPreviousLossAmount(previousLossOrGain)
      backUrl <- backUrl(previousLossOrGain, gainAmount, lossAmount)
      finalResult <- routeRequest(annualExemptAmount, maxAEA.get, backUrl)
    } yield finalResult
  }

  val submitAnnualExemptAmount = ValidateSession.async { implicit request =>

    def errorAction(form: Form[AnnualExemptAmountModel], maxAEA: BigDecimal) = {
      Future.successful(BadRequest(calculation.nonresident.annualExemptAmount(form, maxAEA)))
    }

    def successAction(model: AnnualExemptAmountModel) = {
      calcConnector.saveFormData(KeystoreKeys.annualExemptAmount, model)
      Future.successful(Redirect(routes.BroughtForwardLossesController.broughtForwardLosses()))
    }

    def routeRequest(maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Result] = {
      annualExemptAmountForm(maxAEA).bindFromRequest.fold(
        errors => errorAction(errors, maxAEA),
        success => successAction(success))
    }

    for {
      disposalDate <- fetchDisposalDate(hc)
      customerTypeVal <- customerType(hc)
      isDisabledTrustee <- trusteeAEA(customerTypeVal)
      taxYear <- Future.successful(Dates.getDisposalYear(disposalDate.get.day, disposalDate.get.month, disposalDate.get.year))
      maxAEA <- fetchMaxAEA(isDisabledTrustee, taxYear)
      finalResult <- routeRequest(maxAEA.get)
    } yield finalResult
  }
}
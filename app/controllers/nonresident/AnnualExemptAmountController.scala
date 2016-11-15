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
import models.nonresident.{AnnualExemptAmountModel, CustomerTypeModel, DisabledTrusteeModel}
import play.api.data.Form
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

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

  val annualExemptAmount = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[AnnualExemptAmountModel](KeystoreKeys.annualExemptAmount).map {
      case Some(data) => Ok(calculation.nonresident.annualExemptAmount(annualExemptAmountForm().fill(data)))
      case None => Ok(calculation.nonresident.annualExemptAmount(annualExemptAmountForm()))
    }
  }

  val submitAnnualExemptAmount = ValidateSession.async { implicit request =>

    def customerType(implicit hc: HeaderCarrier): Future[String] = {
      calcConnector.fetchAndGetFormData[CustomerTypeModel](KeystoreKeys.customerType).map {
        customerTypeModel => customerTypeModel.get.customerType
      }
    }

    def trusteeAEA(customerTypeVal: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
      customerTypeVal match {
        case CustomerTypeKeys.trustee =>
          calcConnector.fetchAndGetFormData[DisabledTrusteeModel](KeystoreKeys.disabledTrustee).map {
            disabledTrusteeModel => if (disabledTrusteeModel.get.isVulnerable == "No") false else true
          }
        case _ => Future.successful(true)
      }
    }

    def errorAction(form: Form[AnnualExemptAmountModel]) = Future.successful(BadRequest(calculation.nonresident.annualExemptAmount(form)))

    def successAction(model: AnnualExemptAmountModel) = {
      calcConnector.saveFormData(KeystoreKeys.annualExemptAmount, model)
      Future.successful(Redirect(routes.AcquisitionDateController.acquisitionDate()))
    }

    def routeRequest(maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Result] = {
      annualExemptAmountForm(maxAEA).bindFromRequest.fold(errorAction, successAction)
    }

    def fetchAEA(isFullAEA: Boolean)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
      if (isFullAEA) {
        calcConnector.getFullAEA(2017)
      }
      else {
        calcConnector.getPartialAEA(2017)
      }
    }

    for {
      customerTypeVal <- customerType
      isDisabledTrustee <- trusteeAEA(customerTypeVal)
      maxAEA <- fetchAEA(isDisabledTrustee)
      finalResult <- routeRequest(maxAEA.get)
    } yield finalResult
  }
}
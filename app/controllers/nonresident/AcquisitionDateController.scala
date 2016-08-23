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

import common.DefaultRoutes._
import common.KeystoreKeys
import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import forms.nonresident.AcquisitionDateForm._
import models.nonresident.{AcquisitionDateModel, OtherPropertiesModel}
import play.api.data.Form
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object AcquisitionDateController extends AcquisitionDateController {
  val calcConnector = CalculatorConnector
  val calcElectionConstructor = CalculationElectionConstructor
}

trait AcquisitionDateController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.CustomerTypeController.customerType().url
  val calcConnector: CalculatorConnector
  val calcElectionConstructor: CalculationElectionConstructor

  def acquisitionDateBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    calcConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.otherProperties).map {
      case Some(OtherPropertiesModel("Yes", Some(value))) if value == BigDecimal(0) => routes.AnnualExemptAmountController.annualExemptAmount().url
      case None => missingDataRoute
      case _ => routes.OtherPropertiesController.otherProperties().url
    }
  }

  val acquisitionDate = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String): Future[Result] = {
      calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).map {
        case Some(data) => Ok(calculation.nonresident.acquisitionDate(acquisitionDateForm.fill(data), backUrl))
        case None => Ok(calculation.nonresident.acquisitionDate(acquisitionDateForm, backUrl))
      }
    }

    for {
      backUrl <- acquisitionDateBackUrl
      finalResult <- routeRequest(backUrl)
    } yield finalResult
  }

  val submitAcquisitionDate = ValidateSession.async { implicit request =>

    def errorAction(form: Form[AcquisitionDateModel]) = {
      for {
        backUrl <- acquisitionDateBackUrl(hc)
        route <- Future.successful(BadRequest(calculation.nonresident.acquisitionDate(form, backUrl)))
      } yield route
    }

    def successAction(model: AcquisitionDateModel) = {
      calcConnector.saveFormData(KeystoreKeys.acquisitionDate, model)
      Future.successful(Redirect(routes.AcquisitionValueController.acquisitionValue()))
    }

    acquisitionDateForm.bindFromRequest.fold(errorAction, successAction)
  }
}

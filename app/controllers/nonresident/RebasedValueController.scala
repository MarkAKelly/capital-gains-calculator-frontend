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

import common.{KeystoreKeys, TaxDates}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.nonresident.RebasedValueForm._
import models.nonresident.{AcquisitionDateModel, RebasedValueModel}
import play.api.data.Form
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object RebasedValueController extends RebasedValueController {
  val calcConnector = CalculatorConnector
}

trait RebasedValueController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.CustomerTypeController.customerType().url
  val calcConnector: CalculatorConnector

  private def routeToMandatory(data: AcquisitionDateModel): Boolean = data.hasAcquisitionDate match {
    case "Yes" if !TaxDates.dateAfterStart(data.day.getOrElse(0), data.month.getOrElse(0), data.year.getOrElse(0)) => true
    case _ => false
  }

  private def fetchAcquisitionDate(implicit headerCarrier: HeaderCarrier): Future[Option[AcquisitionDateModel]] = {
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate)
  }

  private def fetchRebasedValue(implicit headerCarrier: HeaderCarrier): Future[Option[RebasedValueModel]] = {
    calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue)
  }

  val rebasedValue = ValidateSession.async { implicit request =>
    def routeRequest(acquisitionDate: AcquisitionDateModel, rebasedValue: Option[RebasedValueModel]): Future[Result] =
      (routeToMandatory(acquisitionDate), rebasedValue) match {
        case (true, Some(data)) => Future.successful(Ok(calculation.nonresident.mandatoryRebasedValue(rebasedValueForm.fill(data))))
        case (false, Some(data)) => Future.successful(Ok(calculation.nonresident.rebasedValue(rebasedValueForm.fill(data), acquisitionDate.hasAcquisitionDate)))
        case (true, None) => Future.successful(Ok(calculation.nonresident.mandatoryRebasedValue(rebasedValueForm)))
        case (false, None) => Future.successful(Ok(calculation.nonresident.rebasedValue(rebasedValueForm, acquisitionDate.hasAcquisitionDate)))
    }

    for {
      acquisitionDate <- fetchAcquisitionDate(hc)
      rebasedVal <- fetchRebasedValue(hc)
      route <- routeRequest(acquisitionDate.get, rebasedVal)
    } yield route
  }

  val submitRebasedValue = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[RebasedValueModel], acquisitionDate: AcquisitionDateModel) =
      routeToMandatory(acquisitionDate) match {
        case true => Future.successful(BadRequest(calculation.nonresident.mandatoryRebasedValue(errors)))
        case false => Future.successful(BadRequest(calculation.nonresident.rebasedValue(errors, acquisitionDate.hasAcquisitionDate)))
      }

    def successAction(model: RebasedValueModel) = {
      calcConnector.saveFormData(KeystoreKeys.rebasedValue, model)
      model.hasRebasedValue match {
        case "Yes" => Future.successful(Redirect(routes.RebasedCostsController.rebasedCosts()))
        case "No" => Future.successful(Redirect(routes.ImprovementsController.improvements()))
      }
    }

    def routeRequest(acquisitionDate: AcquisitionDateModel): Future[Result] = {
      rebasedValueForm.bindFromRequest.fold(
        errors => errorAction(errors, acquisitionDate),
        success => successAction(success)
      )
    }
    for {
      acquisitionDate <- fetchAcquisitionDate(hc)
      route <- routeRequest(acquisitionDate.get)
    } yield route
  }
}

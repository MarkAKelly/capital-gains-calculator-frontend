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

import common.{Dates, KeystoreKeys}
import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import forms.nonresident.AcquisitionValueForm._
import models.nonresident.{AcquisitionDateModel, AcquisitionValueModel}
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation

import scala.concurrent.Future

object AcquisitionValueController extends AcquisitionValueController {
  val calcConnector = CalculatorConnector
  val calcElectionConstructor = CalculationElectionConstructor
}

trait AcquisitionValueController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.CalculationController.restart().url
  val calcConnector: CalculatorConnector
  val calcElectionConstructor: CalculationElectionConstructor

  val acquisitionValue = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[AcquisitionValueModel](KeystoreKeys.acquisitionValue).map {
      case Some(data) => Ok(calculation.nonresident.acquisitionValue(acquisitionValueForm.fill(data)))
      case None => Ok(calculation.nonresident.acquisitionValue(acquisitionValueForm))
    }
  }

  val submitAcquisitionValue = ValidateSession.async { implicit request =>

    def errorAction(form: Form[AcquisitionValueModel]) = {
      Future.successful(BadRequest(calculation.nonresident.acquisitionValue(form)))
    }

    def successAction(model: AcquisitionValueModel) = {
      calcConnector.saveFormData(KeystoreKeys.acquisitionValue, model)
      for {
        data <- fetchData()
        route <- getRoute(data.get)
      } yield route
    }

    def fetchData() = calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate)

    def getRoute(date: AcquisitionDateModel) = date.hasAcquisitionDate match {
      case "Yes" if !Dates.dateAfterStart(date.day.getOrElse(0), date.month.getOrElse(0), date.year.getOrElse(0)) =>
                      Future.successful(Redirect(routes.RebasedValueController.rebasedValue()))
      case "No" => Future.successful(Redirect(routes.RebasedValueController.rebasedValue()))
      case _ => Future.successful(Redirect(routes.ImprovementsController.improvements()))
    }

    acquisitionValueForm.bindFromRequest.fold(
      errors => errorAction(errors),
      success => successAction(success)
    )
  }
}

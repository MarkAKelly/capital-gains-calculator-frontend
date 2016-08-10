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
import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import forms.nonresident.AcquisitionCostsForm._
import models.nonresident.AcquisitionCostsModel
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation

import scala.concurrent.Future

object AcquisitionCostsController extends AcquisitionCostsController{
  val calcConnector = CalculatorConnector
  val calcElectionConstructor = CalculationElectionConstructor
}

trait AcquisitionCostsController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.CalculationController.restart().url
  val calcConnector: CalculatorConnector
  val calcElectionConstructor: CalculationElectionConstructor

  val acquisitionCosts = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[AcquisitionCostsModel](KeystoreKeys.acquisitionCosts).map {
      case Some(data) => Ok(calculation.nonresident.acquisitionCosts(acquisitionCostsForm.fill(data)))
      case None => Ok(calculation.nonresident.acquisitionCosts(acquisitionCostsForm))
    }
  }

  val submitAcquisitionCosts = ValidateSession.async { implicit request =>

    def successAction(model: AcquisitionCostsModel) = {
      calcConnector.saveFormData(KeystoreKeys.acquisitionCosts, model)
      Future.successful(Redirect(routes.CalculationController.disposalCosts()))
    }

    def errorAction(form: Form[AcquisitionCostsModel]) = {
      Future.successful(BadRequest(calculation.nonresident.acquisitionCosts(form)))
    }

    acquisitionCostsForm.bindFromRequest.fold(errorAction, successAction)
  }
}

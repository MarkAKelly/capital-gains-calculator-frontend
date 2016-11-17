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
import controllers.predicates.ValidActiveSession
import models.nonresident.WorthWhenBoughtForLessModel
import forms.nonresident.WorthWhenBoughtForLessForm._
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation
import scala.concurrent.Future

object WorthWhenBoughtForLessController extends WorthWhenBoughtForLessController {
  val calcConnector = CalculatorConnector
}

trait WorthWhenBoughtForLessController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url

  val worthWhenBoughtForLess = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[WorthWhenBoughtForLessModel](KeystoreKeys.worthWhenBoughtForLess).map {
      case Some(data) => Ok(calculation.nonresident.worthWhenBoughtForLess(worthWhenBoughtForLessForm.fill(data)))
      case None => Ok(calculation.nonresident.worthWhenBoughtForLess(worthWhenBoughtForLessForm))
    }
  }

  val submitWorthWhenBoughtForLess = ValidateSession.async { implicit request =>

    def errorAction(form: Form[WorthWhenBoughtForLessModel]) = Future.successful(BadRequest(calculation.nonresident.worthWhenBoughtForLess(form)))

    def successAction(model: WorthWhenBoughtForLessModel) = {
      calcConnector.saveFormData(KeystoreKeys.worthWhenBoughtForLess, model)
      Future.successful(Redirect(routes.AcquisitionCostsController.acquisitionCosts()))
    }

    worthWhenBoughtForLessForm.bindFromRequest.fold(errorAction, successAction)
  }
}

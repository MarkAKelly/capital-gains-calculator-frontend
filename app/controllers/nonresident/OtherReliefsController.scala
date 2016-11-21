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
import constructors.nonresident.AnswersConstructor
import controllers.predicates.ValidActiveSession
import forms.nonresident.OtherReliefsForm._
import models.nonresident._
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation

import scala.concurrent.Future

object OtherReliefsController extends OtherReliefsController {
  val calcConnector = CalculatorConnector
  val answersConstructor = AnswersConstructor
}

trait OtherReliefsController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  val answersConstructor: AnswersConstructor

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url

  val otherReliefs = ValidateSession.async { implicit request =>

    def routeRequest(model: Option[OtherReliefsModel], totalGain: Option[TotalGainResultsModel]) = {
      val gain = totalGain.fold(BigDecimal(0))(_.flatGain)
      val chargeableGain = BigDecimal(0)

      val result = model.fold(calculation.nonresident.otherReliefs(otherReliefsForm, chargeableGain, gain)) { data =>
        calculation.nonresident.otherReliefs(otherReliefsForm.fill(data), chargeableGain, gain)
      }

      Ok(result)
    }

    for {
      answers <- answersConstructor.getNRTotalGainAnswers(hc)
      gain <- calcConnector.calculateTotalGain(answers)
      reliefs <- calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat)
    } yield routeRequest(reliefs, gain)

  }

  val submitOtherReliefs = ValidateSession.async { implicit request =>

    def errorAction(form: Form[OtherReliefsModel]) = {

      def routeRequest(totalGain: Option[TotalGainResultsModel]) = {
        val gain = totalGain.fold(BigDecimal(0))(_.flatGain)
        val chargeableGain = BigDecimal(0)

        BadRequest(calculation.nonresident.otherReliefs(form, chargeableGain, gain))
      }

      for {
        answers <- answersConstructor.getNRTotalGainAnswers(hc)
        gain <- calcConnector.calculateTotalGain(answers)
      } yield routeRequest(gain)
    }

    def successAction(model: OtherReliefsModel) = {
      calcConnector.saveFormData(KeystoreKeys.otherReliefsFlat, model)
      Future.successful(Redirect(routes.CheckYourAnswersController.checkYourAnswers()))
    }

    otherReliefsForm.bindFromRequest.fold(
      errors => errorAction(errors),
      success => successAction(success))
  }
}

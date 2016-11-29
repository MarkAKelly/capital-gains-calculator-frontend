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
import models.nonresident.BroughtForwardLossesModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import forms.nonresident.BroughtForwardLossesForm._
import play.api.data.Form
import views.html.calculation

import scala.concurrent.Future

object BroughtForwardLossesController extends BroughtForwardLossesController {
  val calcConnector = CalculatorConnector
}

trait BroughtForwardLossesController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url

  def generateBackLink(): Future[String] = {
    //TODO add logic based on to be created models
    Future.successful("")
  }

  val broughtForwardLosses = ValidateSession.async { implicit request =>

    val generateForm = calcConnector.fetchAndGetFormData[BroughtForwardLossesModel](KeystoreKeys.broughtForwardLosses).map {
      case Some(data) => broughtForwardLossesForm.fill(data)
      case _ => broughtForwardLossesForm
    }

    for {
      backLink <- generateBackLink()
      form <- generateForm
    } yield Ok(calculation.nonresident.broughtForwardLosses(form, backLink))
  }

  val submitBroughtForwardLosses = ValidateSession.async { implicit request =>

    def successAction(model: BroughtForwardLossesModel) = {
      calcConnector.saveFormData[BroughtForwardLossesModel](KeystoreKeys.broughtForwardLosses, model)
      Future.successful(Redirect(controllers.nonresident.routes.CheckYourAnswersController.checkYourAnswers()))
    }

    def errorAction(form: Form[BroughtForwardLossesModel]) = {
      for {
        backLink <- generateBackLink()
      } yield BadRequest(calculation.nonresident.broughtForwardLosses(form, backLink))
    }

    broughtForwardLossesForm.bindFromRequest.fold(
      errorAction,
      successAction
    )
  }
}
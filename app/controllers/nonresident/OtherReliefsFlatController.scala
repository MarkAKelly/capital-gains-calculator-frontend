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
import forms.nonresident.OtherReliefsForm._
import models.nonresident.{CalculationResultModel, OtherReliefsModel}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation

import scala.concurrent.Future

object OtherReliefsFlatController extends OtherReliefsFlatController {
  val calcConnector = CalculatorConnector
}

trait OtherReliefsFlatController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  val calcConnector: CalculatorConnector

  val otherReliefsFlat: Action[AnyContent] = Action.async { implicit request =>

    def action(dataResult: Option[CalculationResultModel]) = calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat).map {
      case Some(data) if data.otherReliefs.isDefined => Ok(calculation.nonresident.otherReliefsFlat(otherReliefsForm(true).fill(data),
        dataResult.get, hasExistingReliefAmount = true))
      case _ => Ok(calculation.nonresident.otherReliefsFlat(otherReliefsForm(false), dataResult.get, hasExistingReliefAmount = false))
    }

    for {
      construct <- calcConnector.createSummary(hc)
      calculation <- calcConnector.calculateFlat(construct)
      finalResult <- action(calculation)
    } yield finalResult
  }

  val submitOtherReliefsFlat = Action.async { implicit request =>

    def errorAction(form: Form[OtherReliefsModel]) = {
      for {
        construct <- calcConnector.createSummary(hc)
        calculation <- calcConnector.calculateFlat(construct)
        route <- errorRoute(calculation, form)
      } yield route
    }

    def successAction(model: OtherReliefsModel) = {
      calcConnector.saveFormData(KeystoreKeys.otherReliefsFlat, model)
      Future.successful(Redirect(routes.CalculationElectionController.calculationElection()))
    }

    def errorRoute(dataResult: Option[CalculationResultModel], form: Form[OtherReliefsModel]) = {
      calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat).map {
        case Some(data) if data.otherReliefs.isDefined => BadRequest(calculation.nonresident.otherReliefsFlat(form,
          dataResult.get, hasExistingReliefAmount = true))
        case _ => BadRequest(calculation.nonresident.otherReliefsFlat(form, dataResult.get, hasExistingReliefAmount = false))
      }
    }

    otherReliefsForm(true).bindFromRequest.fold(errorAction, successAction)
  }
}

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
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation

import scala.concurrent.Future

object OtherReliefsRebasedController extends OtherReliefsRebasedController{
  val calcConnector = CalculatorConnector
}

trait OtherReliefsRebasedController extends FrontendController with ValidActiveSession {
  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  val calcConnector: CalculatorConnector

  val otherReliefsRebased = ValidateSession.async { implicit request =>
    def action(dataResult: Option[CalculationResultModel]) = calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsRebased).map {
      case Some(data) if data.otherReliefs.isDefined => Ok(calculation.nonresident.otherReliefsRebased(otherReliefsForm(true).fill(data),
        dataResult.get, hasExistingReliefAmount = true))
      case _ => Ok(calculation.nonresident.otherReliefsRebased(otherReliefsForm(true), dataResult.get, hasExistingReliefAmount = false))
    }

    for {
      construct <- calcConnector.createSummary(hc)
      calculation <- calcConnector.calculateRebased(construct)
      finalResult <- action(calculation)
    } yield finalResult
  }

  val submitOtherReliefsRebased = ValidateSession.async { implicit request =>

    def errorAction(form: Form[OtherReliefsModel]) = {
      for {
        construct <- calcConnector.createSummary(hc)
        calculation <- calcConnector.calculateRebased(construct)
        route <- errorRoute(form, calculation)
      } yield route
    }

    def errorRoute(form: Form[OtherReliefsModel], dataResult: Option[CalculationResultModel]) = {
      calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsRebased).map {
        case Some(data) if data.otherReliefs.isDefined => BadRequest(calculation.nonresident.otherReliefsRebased(form, dataResult.get,
          hasExistingReliefAmount = true))
        case _ => BadRequest(calculation.nonresident.otherReliefsRebased(form, dataResult.get, hasExistingReliefAmount = false))
      }
    }

    def successAction(model: OtherReliefsModel) = {
      calcConnector.saveFormData(KeystoreKeys.otherReliefsRebased, model)
      Future.successful(Redirect(routes.CalculationElectionController.calculationElection()))
    }

    otherReliefsForm(true).bindFromRequest.fold(errorAction, successAction)
  }
}

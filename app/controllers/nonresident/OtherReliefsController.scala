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
import common.{Dates, KeystoreKeys, TaxDates}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.nonresident.OtherReliefsForm._
import models.nonresident._
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object OtherReliefsController extends OtherReliefsController {
  val calcConnector = CalculatorConnector
}

trait OtherReliefsController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.CustomerTypeController.customerType().url

  val otherReliefs = ValidateSession.async { implicit request =>

    def action(dataResult: Option[CalculationResultModel]) = {
      calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat).map {
        case Some(data) => Ok(calculation.nonresident.otherReliefs(otherReliefsForm(false).fill(data), dataResult.get))
        case _ => Ok(calculation.nonresident.otherReliefs(otherReliefsForm(false), dataResult.get))
      }
    }

    for {
      construct <- calcConnector.createSummary(hc)
      calculation <- calcConnector.calculateFlat(construct)
      finalResult <- action(calculation)
    } yield finalResult
  }

  val submitOtherReliefs = ValidateSession.async { implicit request =>

    def errorAction(form: Form[OtherReliefsModel], construct: SummaryModel) = {
      for {
        calculation <- calcConnector.calculateFlat(construct)
        route <- errorRoute(form, calculation)
      } yield route
    }

    def errorRoute(form: Form[OtherReliefsModel], dataResult: Option[CalculationResultModel]) = {
      calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat).map {
        case Some(data) if data.otherReliefs.isDefined => BadRequest(calculation.nonresident.otherReliefs(form, dataResult.get))
        case _ => BadRequest(calculation.nonresident.otherReliefs(form, dataResult.get))
      }
    }

    def successAction(model: OtherReliefsModel, construct: SummaryModel) = {
      calcConnector.saveFormData(KeystoreKeys.otherReliefsFlat, model)
      Future.successful(Redirect(routes.CheckYourAnswersController.checkYourAnswers()))
    }

    def action(construct: SummaryModel) = otherReliefsForm(false).bindFromRequest.fold(
      errors => errorAction(errors, construct),
      success => successAction(success, construct))

    for {
      construct <- calcConnector.createSummary(hc)
      finalResult <- action(construct)
    } yield finalResult
  }
}

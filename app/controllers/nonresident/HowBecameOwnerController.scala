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

import java.util.UUID

import common.KeystoreKeys
import common.nonresident.{CustomerTypeKeys, HowBecameOwnerKeys}
import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import forms.nonresident.HowBecameOwnerForm._
import models.nonresident.{CustomerTypeModel, HowBecameOwnerModel}
import play.api.mvc.{Action, Result}
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.SessionKeys
import views.html.calculation.{nonresident => views}

import scala.concurrent.Future

object HowBecameOwnerController extends HowBecameOwnerController {
  val calcConnector = CalculatorConnector
  val calcElectionConstructor = CalculationElectionConstructor
}

trait HowBecameOwnerController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.HowBecameOwnerController.howBecameOwner().url
  val calcConnector: CalculatorConnector
  val calcElectionConstructor: CalculationElectionConstructor

  val howBecameOwner = Action.async { implicit request =>
    if (request.session.get(SessionKeys.sessionId).isEmpty) {
      val sessionId = UUID.randomUUID.toString
      Future.successful(Ok(views.howBecameOwner(howBecameOwnerForm)).withSession(request.session + (SessionKeys.sessionId -> s"session-$sessionId")))
    }
    else {
      calcConnector.fetchAndGetFormData[HowBecameOwnerModel](KeystoreKeys.howBecameOwner).map {
        case Some(data) => Ok(views.howBecameOwner(howBecameOwnerForm.fill(data)))
        case None => Ok(views.howBecameOwner(howBecameOwnerForm))
      }
    }
  }

  val submitHowBecameOwner = ValidateSession.async { implicit request =>

    def errorAction(form: Form[HowBecameOwnerModel]) = {
      Future.successful(BadRequest(views.howBecameOwner(form)))
    }

    def successAction(model: HowBecameOwnerModel) = {
      for {
        save <- calcConnector.saveFormData[HowBecameOwnerModel](KeystoreKeys.howBecameOwner, model)
        route <- routeRequest(model)
      } yield route
    }

    def routeRequest(data: HowBecameOwnerModel): Future[Result] = {
      data.gainedBy match {

        case HowBecameOwnerKeys.bought => Future.successful(Redirect(routes.CurrentIncomeController.currentIncome()))
        case HowBecameOwnerKeys.inherited => Future.successful(Redirect(routes.DisabledTrusteeController.disabledTrustee()))
        case HowBecameOwnerKeys.gifted => Future.successful(Redirect(routes.OtherPropertiesController.otherProperties()))
      }
    }

    howBecameOwnerForm.bindFromRequest.fold(errorAction, successAction)
  }
}

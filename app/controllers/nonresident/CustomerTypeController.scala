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
import common.nonresident.CustomerTypeKeys
import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import forms.nonresident.CustomerTypeForm._
import models.nonresident.CustomerTypeModel
import play.api.mvc.{Action, Result}
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.SessionKeys
import views.html.calculation.{nonresident => views}

import scala.concurrent.Future

object CustomerTypeController extends CustomerTypeController {
  val calcConnector = CalculatorConnector
  val calcElectionConstructor = CalculationElectionConstructor
}

trait CustomerTypeController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.CalculationController.restart().url
  val calcConnector: CalculatorConnector
  val calcElectionConstructor: CalculationElectionConstructor

  //################### Customer Type methods #######################
  val customerType = Action.async { implicit request =>
    if (request.session.get(SessionKeys.sessionId).isEmpty) {
      val sessionId = UUID.randomUUID.toString
      Future.successful(Ok(views.customerType(customerTypeForm)).withSession(request.session + (SessionKeys.sessionId -> s"session-$sessionId")))
    }
    else {
      calcConnector.fetchAndGetFormData[CustomerTypeModel](KeystoreKeys.customerType).map {
        case Some(data) => Ok(views.customerType(customerTypeForm.fill(data)))
        case None => Ok(views.customerType(customerTypeForm))
      }
    }
  }

  val submitCustomerType = ValidateSession.async { implicit request =>

    def errorAction(form: Form[CustomerTypeModel]) = {
      Future.successful(BadRequest(views.customerType(form)))
    }

    def successAction(model: CustomerTypeModel) = {
      for {
        save <- calcConnector.saveFormData[CustomerTypeModel](KeystoreKeys.customerType, model)
        route <- routeRequest(model)
      } yield route
    }

    def routeRequest(data: CustomerTypeModel): Future[Result] = {
      data.customerType match {
        case CustomerTypeKeys.individual => Future.successful(Redirect(routes.CalculationController.currentIncome()))
        case CustomerTypeKeys.trustee => Future.successful(Redirect(routes.CalculationController.disabledTrustee()))
        case CustomerTypeKeys.personalRep => Future.successful(Redirect(routes.CalculationController.otherProperties()))
      }
    }

    customerTypeForm.bindFromRequest.fold(
      errors => errorAction(errors),
      success => successAction(success)
    )
  }
}

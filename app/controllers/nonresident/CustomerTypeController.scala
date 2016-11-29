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
import models.nonresident.{AcquisitionDateModel, CustomerTypeModel, RebasedValueModel}
import play.api.mvc.{Action, Result}
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import views.html.calculation.{nonresident => views}

import scala.concurrent.Future

object CustomerTypeController extends CustomerTypeController {
  val calcConnector = CalculatorConnector
  val calcElectionConstructor = CalculationElectionConstructor
}

trait CustomerTypeController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url
  val calcConnector: CalculatorConnector
  val calcElectionConstructor: CalculationElectionConstructor

  private def customerTypeBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap {
      case ("No", _, _, _)=>
        calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap {
          case ("No", _) => Future.successful(routes.ImprovementsController.improvements().url)
          case _ => Future.successful(routes.PrivateResidenceReliefController.privateResidenceRelief().url)
        }
      case _ => Future.successful(routes.PrivateResidenceReliefController.privateResidenceRelief().url)
    }
  }

  val customerType = Action.async { implicit request =>

    def routeRequest(backUrl: String): Future[Result] =
      calcConnector.fetchAndGetFormData[CustomerTypeModel](KeystoreKeys.customerType).map{
      case Some (data) => Ok (views.customerType (customerTypeForm.fill (data), backUrl) )
      case None => Ok (views.customerType (customerTypeForm, backUrl) )
    }

    for {
      backUrl <- customerTypeBackUrl
      result <- routeRequest(backUrl)
    } yield result
  }

  val submitCustomerType = ValidateSession.async { implicit request =>

    def errorAction(form: Form[CustomerTypeModel]) = {
      for {
        backUrl <- customerTypeBackUrl
        result <- Future.successful(BadRequest(views.customerType(form, backUrl)))
      } yield result
    }

    def successAction(model: CustomerTypeModel) = {
      for {
        save <- calcConnector.saveFormData[CustomerTypeModel](KeystoreKeys.customerType, model)
        route <- routeRequest(model)
      } yield route
    }

    def routeRequest(data: CustomerTypeModel): Future[Result] = {
      data.customerType match {

        case CustomerTypeKeys.individual => Future.successful(Redirect(routes.CurrentIncomeController.currentIncome()))
        case CustomerTypeKeys.trustee => Future.successful(Redirect(routes.DisabledTrusteeController.disabledTrustee()))
        case CustomerTypeKeys.personalRep => Future.successful(Redirect(routes.OtherPropertiesController.otherProperties()))
      }
    }

    customerTypeForm.bindFromRequest.fold(errorAction, successAction)
  }
}

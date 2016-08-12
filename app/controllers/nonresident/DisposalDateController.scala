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

import java.time.LocalDate
import java.util.Date

import common.{Dates, KeystoreKeys}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.nonresident.DisposalDateForm._
import models.nonresident.{AcquisitionDateModel, DisposalDateModel}
import play.api.data.Form
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object DisposalDateController extends DisposalDateController {
  val calcConnector = CalculatorConnector
}

trait DisposalDateController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.nonresident.routes.CalculationController.restart().url

  private def getAcquisitionDate(implicit hc: HeaderCarrier): Future[Option[LocalDate]] =
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).map {
      case Some(AcquisitionDateModel("Yes", Some(day), Some(month), Some(year))) => Some(Dates.constructDate(day, month, year))
      case _ => None
    }

  val disposalDate = ValidateSession.async { implicit request =>

    def routeRequest(acquisitionDate: Option[LocalDate]): Future[Result] = {
      calcConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate).map {
        case Some(data) => Ok(calculation.nonresident.disposalDate(disposalDateForm(acquisitionDate).fill(data)))
        case None => Ok(calculation.nonresident.disposalDate(disposalDateForm(acquisitionDate)))
      }
    }

    for {
      acquisitionDate <- getAcquisitionDate
      route <- routeRequest(acquisitionDate)
    } yield route
  }

  val submitDisposalDate = ValidateSession.async { implicit request =>

    def errorAction(form: Form[DisposalDateModel]) = Future.successful(BadRequest(calculation.nonresident.disposalDate(form)))

    def successAction(model: DisposalDateModel) = {
      calcConnector.saveFormData(KeystoreKeys.disposalDate, model)
      if (!Dates.dateAfterStart(model.day, model.month, model.year)) {
        Future.successful(Redirect(routes.NoCapitalGainsTaxController.noCapitalGainsTax()))
      } else {
        Future.successful(Redirect(routes.DisposalValueController.disposalValue()))
      }
    }

    def routeRequest(acquisitionDate: Option[LocalDate]): Future[Result] = disposalDateForm(acquisitionDate).bindFromRequest.fold(errorAction, successAction)

    for {
      acquisitionDate <- getAcquisitionDate
      route <- routeRequest(acquisitionDate)
    } yield route
  }
}

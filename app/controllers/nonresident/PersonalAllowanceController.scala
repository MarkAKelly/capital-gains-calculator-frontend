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
import forms.nonresident.PersonalAllowanceForm._
import models.nonresident.{DisposalDateModel, OtherReliefsModel, PersonalAllowanceModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation
import common.Dates._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object PersonalAllowanceController extends PersonalAllowanceController {
  val calcConnector = CalculatorConnector
}

trait PersonalAllowanceController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url
  val calcConnector: CalculatorConnector

  val personalAllowance = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[PersonalAllowanceModel](KeystoreKeys.personalAllowance).map {
      case Some(data) => Ok(calculation.nonresident.personalAllowance(personalAllowanceForm().fill(data)))
      case None => Ok(calculation.nonresident.personalAllowance(personalAllowanceForm()))
    }
  }

  val submitPersonalAllowance = ValidateSession.async { implicit request =>

//    def getDisposalDate(implicit hc: HeaderCarrier): Future[Option[DisposalDateModel]] = {
//      calcConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate)
//    }
//
//    def getDisposalYearPersonalAllowance()(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
//
//    }



    // get the disposal date
    // parse it into an int
    // get the personal allowance from the tax year of disposal

    calcConnector.getPA(2017).flatMap { pa =>
      personalAllowanceForm(pa.get).bindFromRequest.fold(
        errors => Future.successful(BadRequest(calculation.nonresident.personalAllowance(errors))),
        success => {
          calcConnector.saveFormData(KeystoreKeys.personalAllowance, success)
          Future.successful(Redirect(routes.OtherPropertiesController.otherProperties()))
        }
      )
    }
  }

}

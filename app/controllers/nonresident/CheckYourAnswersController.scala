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
import java.util.concurrent.Future

import common.KeystoreKeys
import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import uk.gov.hmrc.play.frontend.controller.FrontendController
import controllers.predicates.ValidActiveSession
import forms.nonresident.AcquisitionCostsForm._
import forms.nonresident.DisposalDateForm._
import models.nonresident.{AcquisitionCostsModel, DisposalDateModel, QuestionAnswerModel}
import play.api.mvc.Action
import uk.gov.hmrc.play.http.SessionKeys
import views.html.calculation

object CheckYourAnswersController extends CheckYourAnswersController {
  val calcConnector = CalculatorConnector
  val calcElectionConstructor = CalculationElectionConstructor
}

trait CheckYourAnswersController extends FrontendController with ValidActiveSession {

  val mockQuestionAnswersSeq = Seq(QuestionAnswerModel("dummyId", 200, "dummyQuestion", Some("google.com")))
  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.CustomerTypeController.customerType().url
  val calcConnector: CalculatorConnector
  val calcElectionConstructor: CalculationElectionConstructor

  val checkYourAnswers = Action { implicit request =>
    Ok(calculation.nonresident.checkYourAnswers(mockQuestionAnswersSeq, "google.com"))
  }
  val submitCheckYourAnswers = TODO
}

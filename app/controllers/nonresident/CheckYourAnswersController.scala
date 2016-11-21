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

import constructors.nonresident.{AnswersConstructor, CalculationElectionConstructor, YourAnswersConstructor}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import controllers.predicates.ValidActiveSession
import views.html.calculation

import scala.concurrent.Future

object CheckYourAnswersController extends CheckYourAnswersController {
  val calcElectionConstructor = CalculationElectionConstructor
  val answersConstructor = AnswersConstructor
}

trait CheckYourAnswersController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url
  val answersConstructor: AnswersConstructor
  val backLink = controllers.nonresident.routes.OtherReliefsController.otherReliefs().url


  val checkYourAnswers = ValidateSession.async { implicit request =>

    for {
      model <- answersConstructor.getNRTotalGainAnswers
      answers <- Future.successful(YourAnswersConstructor.fetchYourAnswers(model))
    } yield {
      Ok(calculation.nonresident.checkYourAnswers(answers, backLink))
    }
  }

  val submitCheckYourAnswers = ValidateSession.async { implicit request =>
    Future.successful(Redirect(routes.SummaryController.summary()))
  }
}

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

package controllers.resident

import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import models.resident._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object SummaryController extends SummaryController {
  val calculatorConnector = CalculatorConnector
}

trait SummaryController extends FeatureLock {

  val calculatorConnector: CalculatorConnector

  val summary = FeatureLockForRTT.async { implicit request =>
    for {
      answers <- calculatorConnector.getYourAnswers
      grossGain <- calculatorConnector.calculateRttGrossGain(answers)
    } yield Ok(views.html.calculation.resident.gainSummary(answers, grossGain))
  }
}



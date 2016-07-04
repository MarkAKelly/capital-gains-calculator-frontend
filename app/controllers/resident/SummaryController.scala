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
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

trait SummaryController extends FeatureLock {

  val calculatorConnector: CalculatorConnector

  val summary = FeatureLockForRTT.async { implicit request =>
    def chargeableGain (grossGain: BigDecimal,
                        yourAnswersSummaryModel: YourAnswersSummaryModel,
                        chargeableGainAnswers: ChargeableGainAnswers)(implicit hc: HeaderCarrier): Future[Option[ChargeableGainResultModel]] = {
      if (grossGain > 0) calculatorConnector.calculateRttChargeableGain(yourAnswersSummaryModel, chargeableGainAnswers, BigDecimal(11100))
      else Future.successful(None)
    }

    def routeRequest (totalGainAnswers: YourAnswersSummaryModel,
                      grossGain: BigDecimal,
                      chargeableGainAnswers: ChargeableGainAnswers,
                      chargeableGain: Option[ChargeableGainResultModel])(implicit hc: HeaderCarrier): Future[Result] = {
      if (grossGain > 0) Future.successful(Ok(views.html.calculation.resident.deductionsSummary(totalGainAnswers, chargeableGainAnswers, chargeableGain.get)))
      else Future.successful(Ok(views.html.calculation.resident.gainSummary(totalGainAnswers, grossGain)))
    }

    for {
      answers <- calculatorConnector.getYourAnswers
      grossGain <- calculatorConnector.calculateRttGrossGain(answers)
      deductionAnswers <- calculatorConnector.getChargeableGainAnswers
      chargeableGain <- chargeableGain(grossGain, answers, deductionAnswers)
      routeRequest <- routeRequest(answers, grossGain, deductionAnswers, chargeableGain)
    } yield routeRequest
  }
}

object SummaryController extends SummaryController {
  val calculatorConnector = CalculatorConnector
}

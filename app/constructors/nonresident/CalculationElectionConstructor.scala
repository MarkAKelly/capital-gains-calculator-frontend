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

package constructors.nonresident

import connectors.CalculatorConnector
import models.nonresident.{CalculationResultModel, SummaryModel}
import play.api.i18n.Messages
import uk.gov.hmrc.play.http.HeaderCarrier

object CalculationElectionConstructor extends CalculationElectionConstructor {
  val calcConnector = CalculatorConnector
}

trait CalculationElectionConstructor {

  val calcConnector: CalculatorConnector

  def generateElection(
                        summary: SummaryModel,
                        hc: HeaderCarrier,
                        flatResult: Option[CalculationResultModel],
                        timeResult: Option[CalculationResultModel],
                        rebasedResult: Option[CalculationResultModel]
                      ): Seq[(String, String, String, Option[String])] = {

    (flatResult, timeResult, rebasedResult) match {
      case (Some(flat), Some(time), Some(rebased)) =>
        Seq(
          (flatElementConstructor(flatResult), flatResult.get.totalGain),
          (timeElementConstructor(timeResult), timeResult.get.totalGain),
          (rebasedElementConstructor(rebasedResult), rebasedResult.get.totalGain)
        ).sortBy(_._2).reverse.map(_._1)
      case (Some(flat), Some(time), None) =>
        Seq(
          (flatElementConstructor(flatResult), flatResult.get.totalGain), (timeElementConstructor(timeResult), timeResult.get.totalGain)
        ).sortBy(_._2).reverse.map(_._1)
      case (Some(flat), None, Some(rebased)) =>
        Seq(
          (flatElementConstructor(flatResult), flatResult.get.totalGain), (rebasedElementConstructor(rebasedResult), rebasedResult.get.totalGain)
        ).sortBy(_._2).reverse.map(_._1)
      case (_, _, _) =>
        Seq(flatElementConstructor(flatResult))
    }
  }

  private def rebasedElementConstructor(rebasedResult: Option[CalculationResultModel]) = {
    (
      "rebased",
      rebasedResult.get.taxOwed.setScale(2).toString(),
      Messages("calc.calculationElection.message.rebased"),
      Some(Messages("calc.calculationElection.message.rebasedDate"))
      )
  }

  private def flatElementConstructor(flatResult: Option[CalculationResultModel]) = {
    (
      "flat",
      flatResult.get.taxOwed.setScale(2).toString(),
      Messages("calc.calculationElection.message.flat"),
      None
      )
  }

  private def timeElementConstructor(timeResult: Option[CalculationResultModel]) = {
    (
      "time",
      timeResult.get.taxOwed.setScale(2).toString(),
      Messages("calc.calculationElection.message.time"),
      Some(Messages("calc.calculationElection.message.timeDate"))
      )
  }
}

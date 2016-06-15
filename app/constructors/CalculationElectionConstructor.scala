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

package constructors

import common.Dates
import connectors.CalculatorConnector
import controllers.nonresident.{routes, CalculationController}
import models.{CalculationResultModel, SummaryModel}
import play.api.i18n.Messages
import uk.gov.hmrc.play.http.HeaderCarrier
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object CalculationElectionConstructor extends CalculationElectionConstructor {
  val calcConnector = CalculatorConnector
}

trait CalculationElectionConstructor {

  val calcConnector: CalculatorConnector

  //scalastyle:off
  def generateElection(
                        summary: SummaryModel,
                        hc: HeaderCarrier,
                        flatResult: Option[CalculationResultModel],
                        timeResult: Option[CalculationResultModel],
                        rebasedResult: Option[CalculationResultModel],
                        otherReliefsFlat: Option[BigDecimal],
                        otherReliefsTA: Option[BigDecimal],
                        otherReliefsRebased: Option[BigDecimal]
                      ): Seq[(String, String, String, Option[String], String, Option[BigDecimal])] = {
    summary.acquisitionDateModel.hasAcquisitionDate match {
      case "Yes" if Dates.dateAfterStart(summary.acquisitionDateModel.day.get,
        summary.acquisitionDateModel.month.get, summary.acquisitionDateModel.year.get) => {
        Seq(("flat", flatResult.get.taxOwed.setScale(2).toString(),
          Messages("calc.calculationElection.message.flat"),
          None,
          routes.CalculationController.otherReliefs().toString(),
          otherReliefsFlat))
      }
      case "Yes" if !Dates.dateAfterStart(summary.acquisitionDateModel.day.get,
        summary.acquisitionDateModel.month.get,
        summary.acquisitionDateModel.year.get) => {

        if (summary.rebasedValueModel.get.hasRebasedValue.equals("Yes")) {
          Seq(
            ("flat", flatResult.get.taxOwed.setScale(2).toString(),
              Messages("calc.calculationElection.message.flat"),
              None,
              routes.CalculationController.otherReliefs().toString(),
              otherReliefsFlat),
            ("time", timeResult.get.taxOwed.setScale(2).toString(),
              Messages("calc.calculationElection.message.time"),
              Some(Messages("calc.calculationElection.message.timeDate")),
              routes.CalculationController.otherReliefsTA().toString(),
              otherReliefsTA),
            ("rebased", rebasedResult.get.taxOwed.setScale(2).toString(),
              Messages("calc.calculationElection.message.rebased"),
              Some(Messages("calc.calculationElection.message.rebasedDate")),
              routes.CalculationController.otherReliefsRebased().toString(),
              otherReliefsRebased)
          )
        }
        else {
          Seq(
            ("flat", flatResult.get.taxOwed.setScale(2).toString(),
              Messages("calc.calculationElection.message.flat"),
              None,
              routes.CalculationController.otherReliefs().toString(),
              otherReliefsFlat),
            ("time", timeResult.get.taxOwed.setScale(2).toString(),
              Messages("calc.calculationElection.message.time"),
              Some(Messages("calc.calculationElection.message.timeDate")),
              routes.CalculationController.otherReliefsTA().toString(),
              otherReliefsTA)
          )
        }
      }
      case "No" => {
        if (summary.rebasedValueModel.get.hasRebasedValue.equals("Yes")) {
          Seq(
            ("flat", flatResult.get.taxOwed.setScale(2).toString(),
              Messages("calc.calculationElection.message.flat"),
              None,
              routes.CalculationController.otherReliefs().toString(),
              otherReliefsFlat),
            ("rebased", rebasedResult.get.taxOwed.setScale(2).toString(),
              Messages("calc.calculationElection.message.rebased"),
              Some(Messages("calc.calculationElection.message.rebasedDate")),
              routes.CalculationController.otherReliefsRebased().toString(),
              otherReliefsRebased)
          )
        }
        else {
          Seq(("flat", flatResult.get.taxOwed.setScale(2).toString(),
            Messages("calc.calculationElection.message.flat"),
            None,
            routes.CalculationController.otherReliefs().toString(),
            otherReliefsFlat)
          )
        }
      }
    }
  }
}

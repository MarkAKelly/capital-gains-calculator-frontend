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

import common.{Dates, TaxDates}
import connectors.CalculatorConnector
import controllers.nonresident.routes
import models.nonresident.{AcquisitionDateModel, CalculationResultModel, SummaryModel}
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
                        rebasedResult: Option[CalculationResultModel],
                        otherReliefsFlat: Option[BigDecimal],
                        otherReliefsTA: Option[BigDecimal],
                        otherReliefsRebased: Option[BigDecimal]
                      ): Seq[(String, String, String, Option[String], String, Option[BigDecimal])] = {
    val flatSequence = Seq(flatElementConstructor(flatResult, otherReliefsFlat))

    def withTimeCheck(sequence: Seq[(String, String, String, Option[String], String, Option[BigDecimal])]) = {
      if (summary.acquisitionDateModel.hasAcquisitionDate.equals("Yes") &&
        !TaxDates.dateAfterStart(summary.acquisitionDateModel.day.get,
          summary.acquisitionDateModel.month.get,
          summary.acquisitionDateModel.year.get)) {
        sequence.++(Seq(timeElementConstructor(timeResult, otherReliefsTA)))
      }
      else sequence
    }

    def withRebasedCheck(sequence: Seq[(String, String, String, Option[String], String, Option[BigDecimal])]) = {
      summary.acquisitionDateModel match {
        case AcquisitionDateModel("Yes", day, month, year)
          if !TaxDates.dateAfterStart(day.get, month.get, year.get) && summary.rebasedValueModel.get.hasRebasedValue.equals("Yes") =>
          sequence.++(Seq(rebasedElementConstructor(rebasedResult, otherReliefsRebased)))
        case AcquisitionDateModel("No", _, _, _) if summary.rebasedValueModel.get.hasRebasedValue.equals("Yes") =>
          sequence.++(Seq(rebasedElementConstructor(rebasedResult, otherReliefsRebased)))
        case _ => sequence
      }
    }

    withRebasedCheck(withTimeCheck(flatSequence))
  }

  private def rebasedElementConstructor(rebasedResult: Option[CalculationResultModel], otherReliefsRebased: Option[BigDecimal]) = {
    ("rebased", rebasedResult.get.taxOwed.setScale(2).toString(),
      Messages("calc.calculationElection.message.rebased"),
      Some(Messages("calc.calculationElection.message.rebasedDate")),
      routes.OtherReliefsRebasedController.otherReliefsRebased().toString(),
      otherReliefsRebased)
  }
  
  private def flatElementConstructor(flatResult: Option[CalculationResultModel], otherReliefsFlat: Option[BigDecimal]) = {
    ("flat", flatResult.get.taxOwed.setScale(2).toString(),
      Messages("calc.calculationElection.message.flat"),
      None,
      routes.OtherReliefsController.otherReliefs().toString(),
      otherReliefsFlat)
  }

  private def timeElementConstructor(timeResult: Option[CalculationResultModel], otherReliefsTa: Option[BigDecimal]) = {
    ("time", timeResult.get.taxOwed.setScale(2).toString(),
      Messages("calc.calculationElection.message.time"),
      Some(Messages("calc.calculationElection.message.timeDate")),
      routes.OtherReliefsTAController.otherReliefsTA().toString(),
      otherReliefsTa)
  }
}

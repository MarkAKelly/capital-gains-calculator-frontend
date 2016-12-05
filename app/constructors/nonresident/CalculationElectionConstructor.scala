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

import models.nonresident._
import play.api.i18n.Messages
import scala.concurrent.Future

object CalculationElectionConstructor extends CalculationElectionConstructor

trait CalculationElectionConstructor {

  case class CalculationElectionOption[T](calcType: String, amount: BigDecimal, message: String, date: Option[String], data: T)

  def generateElection(totalGain: TotalGainResultsModel,
                       totalGainWithPrr: Option[CalculationResultsWithPRRModel],
                       taxOwed: Option[CalculationResultsWithTaxOwedModel]
                      ): Future[Seq[(String, String, String, Option[String])]] = {

    val electionOptions = (totalGain, totalGainWithPrr, taxOwed) match {
      case (_, _, Some(data)) => buildElectionWithTaxOwed(data)
      case (_, Some(data), _) => buildElectionWithPrr(data)
      case _ => buildElectionWithTotalGain(totalGain)
    }

    Future.successful(electionOptions)
  }

  private def buildElectionWithTotalGain(data: TotalGainResultsModel) = {
    val flatElement = Some(flatElementConstructor(0.0, data))
    val rebasedElement = data.rebasedGain.collect { case amount => rebasedElementConstructor(0.0, data) }
    val timeElement = data.timeApportionedGain.collect { case amount => timeElementConstructor(0.0, data) }
    val options = Seq(flatElement, rebasedElement, timeElement).flatten

    options
      .sortBy(option => option.amount)
      .map(o => (o.calcType, o.amount.toString(), o.message, o.date))
  }

  private def buildElectionWithPrr(data: CalculationResultsWithPRRModel) = {
    val flatElement = Some(flatElementConstructor(0.0, data.flatResult))
    val rebasedElement = data.rebasedResult.collect { case result => rebasedElementConstructor(0.0, result) }
    val timeElement = data.timeApportionedResult.collect { case result => timeElementConstructor(0.0, result) }
    val options = Seq(flatElement, rebasedElement, timeElement).flatten

    options
      .sortBy(option => (option.data.totalGain, option.data.taxableGain))
      .map(o => (o.calcType, o.amount.toString(), o.message, o.date))
  }

  private def buildElectionWithTaxOwed(data: CalculationResultsWithTaxOwedModel) = {
    val flatElement = Some(flatElementConstructor(data.flatResult.taxOwed, data.flatResult))
    val rebasedElement = data.rebasedResult.collect { case result => rebasedElementConstructor(result.taxOwed, result) }
    val timeElement = data.timeApportionedResult.collect { case result => timeElementConstructor(result.taxOwed, result) }
    val options = Seq(flatElement, rebasedElement, timeElement).flatten

    options
      .sortBy(option => (option.data.totalGain, option.data.taxableGain, option.data.taxOwed))
      .map(o => (o.calcType, o.amount.toString(), o.message, o.date))
  }

  private def rebasedElementConstructor[T](amount: BigDecimal, data: T) = {
    CalculationElectionOption(
      "rebased",
      amount.setScale(2),
      Messages("calc.calculationElection.message.rebased"),
      Some(Messages("calc.calculationElection.message.rebasedDate")),
      data
    )
  }

  private def flatElementConstructor[T](amount: BigDecimal, data: T) = {
    CalculationElectionOption(
      "flat",
      amount.setScale(2),
      Messages("calc.calculationElection.message.flat"),
      None,
      data
    )
  }

  private def timeElementConstructor[T](amount: BigDecimal, data: T) = {
    CalculationElectionOption(
      "time",
      amount.setScale(2),
      Messages("calc.calculationElection.message.time"),
      Some(Messages("calc.calculationElection.message.timeDate")),
      data
    )
  }
}
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

import common.KeystoreKeys
import controllers.nonresident.routes
import models.nonresident.{CalculationResultModel, QuestionAnswerModel}
import play.api.i18n.Messages
import uk.gov.hmrc.play.views.helpers.MoneyPounds

object CalculationDetailsConstructor {

  def buildSection(calculation: CalculationResultModel, calculationType: String): Seq[QuestionAnswerModel[Any]] = {
    val electionDetails = calculationElection(calculationType)
    val totalGainDetails = totalGain(calculation)
    val totalLossDetails = totalLoss(calculation)
    val usedAeaDetails = usedAea(calculation)
    val taxableGainDetails = taxableGain(calculation)
    val taxableRateDetails = taxableRate(calculation)
    val lossToCarryForwardDetails = lossToCarryForward(calculation)
    Seq(
      electionDetails,
      totalGainDetails,
      totalLossDetails,
      usedAeaDetails,
      taxableGainDetails,
      taxableRateDetails,
      lossToCarryForwardDetails).flatten
  }

  def calculationElection(calculationType: String): Option[QuestionAnswerModel[String]] = {

    val id = KeystoreKeys.calculationElection

    val question = Messages("calc.summary.calculation.details.calculationElection")

    val answer = calculationType match {
      case "flat" => Messages("calc.summary.calculation.details.flatCalculation")
      case "time" => Messages("calc.summary.calculation.details.timeCalculation")
      case "rebased" => Messages("calc.summary.calculation.details.rebasedCalculation")
    }

    val link = routes.CalculationElectionController.calculationElection().url

    Some(QuestionAnswerModel(id, answer, question, Some(link)))
  }

  def lossToCarryForward(model: CalculationResultModel): Option[QuestionAnswerModel[BigDecimal]] = {
    if (model.taxableGain < BigDecimal(0)) {
      val id = "calcDetails:lossToCarryForward"

      val question = Messages("calc.summary.calculation.details.lossCarriedForward")

      val answer = model.taxableGain.abs

      Some(QuestionAnswerModel(id, answer, question, None))
    }
    else None
  }

  def taxableRate(model: CalculationResultModel): Option[QuestionAnswerModel[String]] = {

    def toDisplayText(rate: (Int, BigDecimal)): String = s"£${MoneyPounds(rate._2, 0).quantity} at ${rate._1}%"

    val lowerRates = if (model.baseTaxGain > BigDecimal(0)) Some(model.baseTaxRate -> model.baseTaxGain) else None

    val higherRates = if (model.upperTaxGain.isDefined) Some(model.upperTaxRate.get -> model.upperTaxGain.get) else None

    val rates = Seq(lowerRates, higherRates).flatten

    if (rates.nonEmpty) {
      val id = "calcDetails:taxableRate"

      val question = Messages("calc.summary.calculation.details.taxRate")

      val answer = if (rates.size == 1) s"${rates.head._1}%" else rates.map(toDisplayText).mkString("\n")

      Some(QuestionAnswerModel(id, answer, question, None))
    }
    else None
  }

  def taxableGain(model: CalculationResultModel): Option[QuestionAnswerModel[BigDecimal]] = {
    if (model.totalGain > BigDecimal(0)) {
      val id = "calcDetails:taxableGain"

      val question = Messages("calc.summary.calculation.details.taxableGain")

      val answer = model.taxableGain

      Some(QuestionAnswerModel(id, answer, question, None))
    }
    else None
  }

  def usedAea(model: CalculationResultModel): Option[QuestionAnswerModel[BigDecimal]] = {
    if (model.totalGain > BigDecimal(0)) {
      val id = "calcDetails:aea"

      val question = Messages("calc.summary.calculation.details.usedAEA")

      val answer = model.usedAnnualExemptAmount

      Some(QuestionAnswerModel(id, answer, question, None))
    }
    else None
  }

  def totalLoss(model: CalculationResultModel): Option[QuestionAnswerModel[BigDecimal]] = {
    if (model.totalGain >= BigDecimal(0)) None
    else {
      val id = "calcDetails:totalLoss"

      val question = Messages("calc.summary.calculation.details.totalLoss")

      val answer = model.totalGain.abs

      Some(QuestionAnswerModel(id, answer, question, None))
    }
  }

  def totalGain(model: CalculationResultModel): Option[QuestionAnswerModel[BigDecimal]] = {
    if (model.totalGain < BigDecimal(0)) None
    else {
      val id = "calcDetails:totalGain"

      val question = Messages("calc.summary.calculation.details.totalGain")

      val answer = model.totalGain

      Some(QuestionAnswerModel(id, answer, question, None))
    }
  }

}

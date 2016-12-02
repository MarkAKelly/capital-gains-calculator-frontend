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

import common.nonresident.CalculationType
import models.nonresident.{CalculationResultsWithTaxOwedModel, QuestionAnswerModel}
import play.api.i18n.Messages
import uk.gov.hmrc.play.views.helpers.MoneyPounds

object CalculationDetailsWithAllAnswersConstructor {

  def buildSection(calculation: CalculationResultsWithTaxOwedModel, calculationType: String, taxYear: String): Seq[QuestionAnswerModel[Any]] = {
    val electionDetails = CalculationDetailsConstructor.calculationElection(calculationType)
    val correctModel = calculationType match {
      case CalculationType.flat => calculation.flatResult
      case CalculationType.rebased => calculation.rebasedResult.get
      case CalculationType.timeApportioned => calculation.timeApportionedResult.get
    }
    val taxableGainDetails = CalculationDetailsWithPRRConstructor.taxableGain(correctModel.taxableGain)
    val totalGainDetails = CalculationDetailsConstructor.totalGain(correctModel.totalGain)
    val totalLossDetails = CalculationDetailsConstructor.totalLoss(correctModel.totalGain)
    val prrDetails = correctModel.prrUsed match {
      case Some(value) => CalculationDetailsWithPRRConstructor.prrUsedDetails(value)
      case _ => None
    }
    val allowableLossesUsed = allowableLossesUsed(correctModel.allowableLossesUsed)
    val annualExemptAmountUsed = annualExemptAmountUsed(correctModel.aeaUsed)
    val annualExemptAmountRemaining = annualExemptAmountRemaining(correctModel.aeaRemaining)
    val lossesRemaining = lossesRemaining(correctModel.taxableGain)
    val taxRates = taxRates(correctModel.taxGain, correctModel.taxOwed, correctModel.upperTaxGain, correctModel.upperTaxRate)

    Seq(
      electionDetails,
      totalGainDetails,
      totalLossDetails,
      prrDetails,
      allowableLossesUsed,
      annualExemptAmountUsed,
      annualExemptAmountRemaining,
      taxableGainDetails,
      lossesRemaining,
      taxRates
    ).flatten
  }

  def allowableLossesUsed(allowableLossUsed: Option[BigDecimal], taxYear: String): Option[QuestionAnswerModel[BigDecimal]] = {
    allowableLossUsed match {
      case Some(data) if data > 0 =>
        val id = "calcDetails:allowableLossesUsed"
        val question = Messages("calc.summary.calculation.details.allowableLossesUsed", taxYear)
        Some(QuestionAnswerModel(id, data, question, None))
      case _ => None
    }
  }

  def aeaUsed(annualExemptAmountUsed: Option[BigDecimal]): Option[QuestionAnswerModel[BigDecimal]] = {
    annualExemptAmountUsed match {
      case Some(data) if data > 0 =>
        val id = "calcDetails:annualExemptAmountUsed"
        val question = Messages("calc.summary.calculation.details.usedAEA")
        Some(QuestionAnswerModel(id, data, question, None))
      case _ => None
    }
  }

  def aeaRemaining(annualExemptAmountRemaining: BigDecimal): Option[QuestionAnswerModel[BigDecimal]] = {
    val id = "calcDetails:annualExemptAmountRemaining"
    val question = Messages("calc.summary.calculation.details.remainingAEA")
    Some(QuestionAnswerModel(id, annualExemptAmountRemaining, question, None))
  }

  def broughtForwardLossesRemaining(broughtForwardLossesRemaining: Option[BigDecimal], taxYear: String): Option[QuestionAnswerModel[BigDecimal]] = {
    broughtForwardLossesRemaining match {
      case Some(data) if data > 0 =>
        val id = "calcDetails:broughtForwardLossesUsed"
        val question = Messages("calc.summary.calculation.details.broughtForwardLossesUsed", taxYear)
        Some(QuestionAnswerModel(id, data, question, None))
      case _ => None
    }
  }

  def lossesRemaining(taxableGain: BigDecimal): Option[QuestionAnswerModel[BigDecimal]] = {
    //TODO add correct wording from designers to messages
    if (taxableGain <= 0) {
      val id = "calcDetails:lossesRemaining"
      val question = Messages("calc.summary.calculation.details.lossesRemaining")
      Some(QuestionAnswerModel(id, taxableGain.abs, question, None))
    } else None
  }

  def taxRates(taxableGain: BigDecimal,
               taxRate: Int,
               additionalTaxableGain: Option[BigDecimal],
               additionalTaxRate: Option[Int]): Option[QuestionAnswerModel[Any]] = {
    val id = "calcDetails:taxRate"
    val question = Messages("calc.summary.calculation.details.taxRate ")

    (taxableGain, additionalTaxableGain) match {
      case (_, Some(value)) if taxableGain > 0 =>
        val value = (taxableGain,value)
        Some(QuestionAnswerModel(id, value, question, None))
      case _ if taxableGain > 0 =>
        val value = Messages("calc.summary.calculation.details.taxRateValue", s"£${MoneyPounds(taxableGain, 2)}")
        Some(QuestionAnswerModel(id, value, question, None))
      case _ => None
    }
  }
}

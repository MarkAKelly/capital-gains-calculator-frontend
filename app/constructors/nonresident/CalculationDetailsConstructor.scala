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
import models.nonresident.{CalculationResultModel, QuestionAnswerModel, SummaryModel}
import play.api.i18n.Messages

object CalculationDetailsConstructor {
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

  def buildSection(calculationResult: CalculationResultModel, userResponses: SummaryModel): Seq[QuestionAnswerModel[Any]] = {
    Seq(calculationElection(userResponses)).flatten
  }

  def calculationElection(model: SummaryModel): Option[QuestionAnswerModel[String]] = {

    val id = KeystoreKeys.calculationElection

    val question = Messages("calc.summary.calculation.details.calculationElection")

    val answer = model.calculationElectionModel.calculationType match {
      case "flat" => Messages("calc.summary.calculation.details.flatCalculation")
      case "time" => Messages("calc.summary.calculation.details.timeCalculation")
      case "rebased" => Messages("calc.summary.calculation.details.rebasedCalculation")
    }

    val link = routes.CalculationElectionController.calculationElection().url

    Some(QuestionAnswerModel(id, answer, question, Some(link)))
  }

}

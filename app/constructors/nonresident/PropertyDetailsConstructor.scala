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

import models.nonresident.{QuestionAnswerModel, SummaryModel}
import common.{KeystoreKeys => keys}
import common.nonresident.{CalculationType => calcType}
import play.api.i18n.Messages

object PropertyDetailsConstructor {

  def propertyDetailsRows(answers: SummaryModel): Seq[QuestionAnswerModel[Any]] = {

    val rebasedImprovements =
      if(answers.improvementsModel.isClaimingImprovements == "Yes" &&
        answers.calculationElectionModel.calculationType == calcType.rebased
      ) true
      else false

    val totalImprovements =
      if(answers.improvementsModel.isClaimingImprovements == "Yes" &&
        answers.calculationElectionModel.calculationType == calcType.flat ||
        answers.calculationElectionModel.calculationType == calcType.timeApportioned
      ) true
      else false

    val improvementsIsClaiming = improvementsIsClaimingRow(answers)
    val improvementsTotal = improvementsTotalRow(answers, totalImprovements)
    val improvementsAfter = improvementsAfterRow(answers, rebasedImprovements)

    val sequence = Seq(improvementsIsClaiming, improvementsTotal, improvementsAfter)
    sequence.flatten
  }

  def improvementsIsClaimingRow(answers: SummaryModel): Option[QuestionAnswerModel[String]] = {
    Some(QuestionAnswerModel[String](s"${keys.improvements}-isClaiming",
      answers.improvementsModel.isClaimingImprovements,
      Messages("calc.improvements.question"),
      Some(controllers.nonresident.routes.ImprovementsController.improvements().url)
    ))
  }

  def improvementsTotalRow(answers: SummaryModel, display: Boolean): Option[QuestionAnswerModel[BigDecimal]] = {
    if(display) {
      val total: BigDecimal = answers.improvementsModel.improvementsAmt.getOrElse(BigDecimal(0))
                        . +(answers.improvementsModel.improvementsAmtAfter.getOrElse(BigDecimal(0)))
      Some(QuestionAnswerModel[BigDecimal](s"${keys.improvements}-total",
        total,
        Messages("calc.improvements.questionTwo"),
        Some(controllers.nonresident.routes.ImprovementsController.improvements().url)
      ))
    }
    else None
  }

  def improvementsAfterRow(answers: SummaryModel, display: Boolean): Option[QuestionAnswerModel[BigDecimal]] = {
    if (display) {
      Some(QuestionAnswerModel[BigDecimal](s"${keys.improvements}-after",
        answers.improvementsModel.improvementsAmtAfter.getOrElse(0),
        Messages("calc.improvements.questionFour"),
        Some(controllers.nonresident.routes.ImprovementsController.improvements().url)
      ))
    }
    else None
  }
}



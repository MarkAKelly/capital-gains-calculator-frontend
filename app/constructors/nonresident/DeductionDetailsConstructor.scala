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

import models.nonresident.{AllowableLossesModel, CalculationResultModel, QuestionAnswerModel, SummaryModel}
import common.{KeystoreKeys => keys}
import controllers.nonresident.routes
import play.api.i18n.Messages

object DeductionDetailsConstructor {

  def privateResidenceReliefRow(results: CalculationResultModel): Option[QuestionAnswerModel[BigDecimal]] = {
    if (results.simplePRR.isDefined) {
      Some(QuestionAnswerModel(keys.privateResidenceRelief,
        results.simplePRR.get,
        Messages("calc.privateResidenceRelief.question"),
        Some(routes.PrivateResidenceReliefController.privateResidenceRelief().toString())
      ))
    } else None
  }

  def allowableLossesRow(answers: SummaryModel): Option[QuestionAnswerModel[BigDecimal]] = {
    Some(QuestionAnswerModel(keys.allowableLosses,
      answers.allowableLossesModel match {
        case AllowableLossesModel("Yes", Some(value)) => value
        case _ => BigDecimal(0.0)
      },
      Messages("calc.allowableLosses.question.two"),
      Some(routes.AllowableLossesController.allowableLosses().toString())))
  }
}

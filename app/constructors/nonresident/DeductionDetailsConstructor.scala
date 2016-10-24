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
import common.{KeystoreKeys => keys}
import common.nonresident.{CalculationType => calculationKeys}
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

  def otherReliefsFlatValueRow(answers: SummaryModel): Option[QuestionAnswerModel[BigDecimal]] = {
    (answers.calculationElectionModel.calculationType, answers.otherReliefsModelFlat) match {
      case (calculationKeys.flat, OtherReliefsModel(Some("Yes"), Some(value))) =>
        Some(QuestionAnswerModel(keys.otherReliefsFlat,
          value,
          Messages("calc.otherReliefs.question"),
          Some(routes.OtherReliefsController.otherReliefs().toString())))
      case (calculationKeys.flat, OtherReliefsModel(Some("Yes"), None)) =>
        Some(QuestionAnswerModel(keys.otherReliefsFlat,
          BigDecimal(0),
          Messages("calc.otherReliefs.question"),
          Some(routes.OtherReliefsController.otherReliefs().toString())))
      case (calculationKeys.flat, OtherReliefsModel(None, Some(value))) =>
        Some(QuestionAnswerModel(keys.otherReliefsFlat,
          value,
          Messages("calc.otherReliefs.question"),
          Some(routes.OtherReliefsFlatController.otherReliefsFlat().toString())))
      case (calculationKeys.flat, OtherReliefsModel(None, None)) =>
        Some(QuestionAnswerModel(keys.otherReliefsFlat,
          BigDecimal(0),
          Messages("calc.otherReliefs.question"),
          Some(routes.OtherReliefsFlatController.otherReliefsFlat().toString())))
      case _ => None
    }
  }
}

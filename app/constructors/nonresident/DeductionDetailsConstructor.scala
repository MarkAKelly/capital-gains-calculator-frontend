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

  def deductionDetailsRows(answers: TotalGainAnswersModel,
                           privateResidenceReliefModel: Option[PrivateResidenceReliefModel] = None): Seq[QuestionAnswerModel[Any]] = {
    val otherReliefsFlatValue = otherReliefsFlatValueRow(answers)

    val sequence = Seq(otherReliefsFlatValue)

    sequence.flatten
  }

  def otherReliefsFlatValueRow(answers: TotalGainAnswersModel): Option[QuestionAnswerModel[BigDecimal]] = {
    answers.otherReliefsFlat match {
      case Some(OtherReliefsModel(value)) => Some(QuestionAnswerModel(
        keys.otherReliefsFlat,
        value,
        Messages("calc.otherReliefs.question"),
        Some(controllers.nonresident.routes.OtherReliefsController.otherReliefs().url)
      ))
      case _ => None
    }
  }
}

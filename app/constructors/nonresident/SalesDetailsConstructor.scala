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

import java.time.LocalDate

import models.nonresident.{QuestionAnswerModel, SummaryModel}
import common.{Dates, KeystoreKeys => keys}
import play.api.i18n.Messages

object SalesDetailsConstructor {

  def salesDetailsRows(answers: SummaryModel): Seq[QuestionAnswerModel[Any]] = {
    val disposalDate = disposalDateRow(answers)
    val disposalValue = disposalValueRow(answers)
    val disposalCosts = disposalCostsRow(answers)

    Seq(disposalDate, disposalValue, disposalCosts)
  }

  def disposalDateRow(answers: SummaryModel): QuestionAnswerModel[LocalDate] = {
    val dateModel = answers.disposalDateModel
    val date = Dates.constructDate(dateModel.day, dateModel.month, dateModel.year)

    QuestionAnswerModel[LocalDate](keys.disposalDate,
      date,
      Messages("calc.disposalDate.question"),
      Some(controllers.nonresident.routes.DisposalDateController.disposalDate().url))
  }

  def disposalValueRow(answers: SummaryModel): QuestionAnswerModel[BigDecimal] = {
    QuestionAnswerModel[BigDecimal](keys.disposalValue,
      answers.disposalValueModel.disposalValue,
      Messages("calc.disposalValue.question"),
      Some(controllers.nonresident.routes.DisposalValueController.disposalValue().url))
  }

  def disposalCostsRow(answers: SummaryModel): QuestionAnswerModel[BigDecimal] = {
    QuestionAnswerModel[BigDecimal](keys.disposalCosts,
      answers.disposalCostsModel.disposalCosts,
      Messages("calc.disposalCosts.question"),
      Some(controllers.nonresident.routes.DisposalCostsController.disposalCosts().url))
  }
}
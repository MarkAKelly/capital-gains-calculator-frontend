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
import common.nonresident.CalculationType
import common.{Dates, KeystoreKeys}
import models.nonresident.{QuestionAnswerModel, SummaryModel}
import play.api.i18n.Messages


object PurchaseDetailsConstructor {

  def getPurchaseDetailsSection(summaryModel: SummaryModel): Seq[QuestionAnswerModel[Any]] = {

    val acquisitionDateData = getAcquisitionDateAnswer(summaryModel)
    val acquisitionValueData = getAcquisitionValueAnswer(summaryModel)
    val acquisitionCostsData = getAcquisitionCostsAnswer(summaryModel)
    val rebasedValueData = getRebasedValueAnswer(summaryModel)
    val rebasedCostsData = getRebasedCostsAnswer(summaryModel)

    val items = Seq(
      acquisitionDateData,
      acquisitionValueData,
      acquisitionCostsData,
      rebasedValueData,
      rebasedCostsData
    )
    items.flatten
  }

  def getAcquisitionDateAnswer(summaryModel: SummaryModel): Option[QuestionAnswerModel[LocalDate]] = {
      Some(QuestionAnswerModel(
        KeystoreKeys.acquisitionDate,
        Dates.constructDate(summaryModel.acquisitionDateModel.day.get,
          summaryModel.acquisitionDateModel.month.get, summaryModel.acquisitionDateModel.year.get),
        Messages("calc.acquisitionDate.questionTwo"),
        Some(controllers.nonresident.AcquisitionDateController.acquisitionDate().toString())
      ))
  }

  def getAcquisitionValueAnswer(summaryModel: SummaryModel): Option[QuestionAnswerModel[BigDecimal]] =  {
    if (summaryModel.calculationElectionModel.calculationType.equals(CalculationType.flat) ||
      !(summaryModel.calculationElectionModel.calculationType.equals(CalculationType.rebased)))
      Some(QuestionAnswerModel(
        KeystoreKeys.acquisitionValue,
        summaryModel.acquisitionValueModel.acquisitionValueAmt,
        Messages("calc.acquisitionValue.question"),
        Some(controllers.nonresident.AcquisitionValueController.acquisitionValue().toString())
      ))
    else
      None
  }

  def getAcquisitionCostsAnswer(summaryModel: SummaryModel): Option[QuestionAnswerModel[BigDecimal]] = {
    if (summaryModel.calculationElectionModel.calculationType.equals(CalculationType.flat))
      Some(QuestionAnswerModel(
        KeystoreKeys.acquisitionCosts,
        summaryModel.acquisitionCostsModel.acquisitionCostsAmt,
        Messages("calc.acquisitionCosts.question"),
        Some(controllers.nonresident.AcquisitionCostsController.acquisitionCosts.toString())
      ))
    else
      None
  }

  def getRebasedValueAnswer(summaryModel: SummaryModel): Option[QuestionAnswerModel[Option[BigDecimal]]] =
    (summaryModel.rebasedValueModel, summaryModel.calculationElectionModel.calculationType) match {
    case(Some(rebasedValueModel), CalculationType.rebased) =>
      Some(QuestionAnswerModel(
        KeystoreKeys.rebasedValue,
        rebasedValueModel.rebasedValueAmt,
        Messages("calc.rebasedValue.questionTwo"),
        Some(controllers.nonresident.RebasedValueController.rebasedValue.toString())
      ))
    case _ => None
  }

  def getRebasedCostsAnswer(summaryModel: SummaryModel): Option[QuestionAnswerModel[Option[BigDecimal]]] =
    (summaryModel.rebasedCostsModel, summaryModel.calculationElectionModel.calculationType) match {
      case(Some(rebasedCostsModel), CalculationType.rebased)  =>
        Some(QuestionAnswerModel(
        KeystoreKeys.rebasedCosts,
          rebasedCostsModel.rebasedCosts,
          Messages("calc.rebasedCosts.questionTwo"),
          Some(controllers.nonresident.RebasedCostsController.rebasedCosts.toString())
    ))
        case _ => None
    }

}

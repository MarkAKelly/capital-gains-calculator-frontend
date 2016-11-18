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

import common.{KeystoreKeys, TaxDates}
import models.nonresident._
import play.api.i18n.Messages


object PurchaseDetailsConstructor {

  def getPurchaseDetailsSection(totalGainAnswersModel: TotalGainAnswersModel): Seq[QuestionAnswerModel[Any]] = {

    val useRebasedValues =
      totalGainAnswersModel.acquisitionDateModel match {
        case AcquisitionDateModel("Yes",_,_,_) if !TaxDates.dateAfterStart(totalGainAnswersModel.acquisitionDateModel.get) => true
        case AcquisitionDateModel("No",_,_,_) if totalGainAnswersModel.rebasedValueModel.get.rebasedValueAmt.isDefined => true
        case _ => false
      }

    val acquisitionDateAnswerData = acquisitionDateAnswerRow(totalGainAnswersModel)
    val acquisitionDateData = acquisitionDateRow(totalGainAnswersModel)
    val howBecameOwnerData = howBecameOwnerRow(totalGainAnswersModel)
    val boughtForLessData = boughtForLessRow(totalGainAnswersModel)
    val acquisitionValueData = acquisitionValueRow(totalGainAnswersModel)
    val acquisitionCostsData = acquisitionCostsRow(totalGainAnswersModel)
    val rebasedValueData = rebasedValueRow(totalGainAnswersModel.rebasedValueModel, useRebasedValues)
    val rebasedCostsQuestionData = rebasedCostsQuestionRow(totalGainAnswersModel.rebasedCostsModel, useRebasedValues)
    val rebasedCostsData = rebasedCostsRow(totalGainAnswersModel.rebasedCostsModel, useRebasedValues)

    val items = Seq(
      acquisitionDateAnswerData,
      acquisitionDateData,
      howBecameOwnerData,
      boughtForLessData,
      acquisitionValueData,
      acquisitionCostsData,
      rebasedValueData,
      rebasedCostsQuestionData,
      rebasedCostsData
    )
    items.flatten
  }

  def acquisitionDateAnswerRow(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[String]] = {
    Some(QuestionAnswerModel(
      s"${KeystoreKeys.acquisitionDate}-question",
      totalGainAnswersModel.acquisitionDateModel.hasAcquisitionDate,
      Messages("calc.acquisitionDate.question"),
      Some(controllers.nonresident.routes.AcquisitionDateController.acquisitionDate().url)
    ))
  }

  def acquisitionDateRow(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[LocalDate]] = {
    if (totalGainAnswersModel.acquisitionDateModel.hasAcquisitionDate.equals("Yes")) {
      Some(QuestionAnswerModel(
        s"${KeystoreKeys.acquisitionDate}",
        totalGainAnswersModel.acquisitionDateModel.get,
        Messages("calc.acquisitionDate.questionTwo"),
        Some(controllers.nonresident.routes.AcquisitionDateController.acquisitionDate().url)
      ))
    } else None
  }

  def howBecameOwnerRow(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[String]] = {

    val answer = totalGainAnswersModel.howBecameOwnerModel.gainedBy match {
      case "Bought" => Messages("calc.howBecameOwner.bought")
      case "Inherited" => Messages("calc.howBecameOwner.inherited")
      case _ => Messages("calc.howBecameOwner.gifted")
    }

    Some(QuestionAnswerModel(
      s"${KeystoreKeys.howBecameOwner}",
      answer,
      Messages("calc.howBecameOwner.question"),
      Some(controllers.nonresident.routes.HowBecameOwnerController.howBecameOwner().url)
    ))
  }

  def boughtForLessRow(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[Boolean]] = {
    totalGainAnswersModel.howBecameOwnerModel.gainedBy match {
      case "Bought" => Some(QuestionAnswerModel(
        s"${KeystoreKeys.boughtForLess}",
        totalGainAnswersModel.boughtForLessModel.get.boughtForLess,
        Messages("calc.boughtForLess.question"),
        Some(controllers.nonresident.routes.BoughtForLessController.boughtForLess().url)
      ))
      case _ => None
    }
  }

  def acquisitionValueRow(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[BigDecimal]] = {
    Some(QuestionAnswerModel(
      KeystoreKeys.acquisitionValue,
      totalGainAnswersModel.acquisitionValueModel.acquisitionValueAmt,
      Messages("calc.acquisitionValue.question"),
      Some(controllers.nonresident.routes.AcquisitionValueController.acquisitionValue().url)
    ))
  }

  def acquisitionCostsRow(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[BigDecimal]] = {
    Some(QuestionAnswerModel(
      KeystoreKeys.acquisitionCosts,
      totalGainAnswersModel.acquisitionCostsModel.acquisitionCostsAmt,
      Messages("calc.acquisitionCosts.question"),
      Some(controllers.nonresident.routes.AcquisitionCostsController.acquisitionCosts().url)
    ))
  }

  def rebasedValueRow(rebasedValueModel: Option[RebasedValueModel], useRebasedValues: Boolean): Option[QuestionAnswerModel[BigDecimal]] = {
    if (useRebasedValues) {
      Some(QuestionAnswerModel(
        KeystoreKeys.rebasedValue,
        rebasedValueModel.get.rebasedValueAmt.get,
        s"${Messages("calc.nonResident.rebasedValue.question")} ${Messages("calc.nonResident.rebasedValue.date")}",
        Some(controllers.nonresident.routes.RebasedValueController.rebasedValue().url)
      ))
    }
    else None
  }

  def rebasedCostsQuestionRow(rebasedCostsModel: Option[RebasedCostsModel], useRebasedValues: Boolean): Option[QuestionAnswerModel[String]] = {
    if (useRebasedValues) {
      Some(QuestionAnswerModel(
        s"${KeystoreKeys.rebasedCosts}-question",
        rebasedCostsModel.get.hasRebasedCosts,
        Messages("calc.rebasedCosts.question"),
        Some(controllers.nonresident.routes.RebasedCostsController.rebasedCosts().url)
      ))
    } else None
  }

  def rebasedCostsRow(rebasedCostsModel: Option[RebasedCostsModel], useRebasedValues: Boolean): Option[QuestionAnswerModel[BigDecimal]] = {
    (useRebasedValues, rebasedCostsModel) match {
      case (true, Some(RebasedCostsModel("Yes", Some(value)))) => Some(QuestionAnswerModel(
        KeystoreKeys.rebasedCosts,
        value,
        Messages("calc.rebasedCosts.questionTwo"),
        Some(controllers.nonresident.routes.RebasedCostsController.rebasedCosts().url)
      ))
      case _ => None
    }
  }
}

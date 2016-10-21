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
import common.nonresident.CustomerTypeKeys
import models.nonresident.{QuestionAnswerModel, SummaryModel}
import play.api.i18n.Messages

import scala.math.BigDecimal

object PersonalDetailsConstructor {

  def getPersonalDetailsSection(summaryModel: SummaryModel) : Seq[QuestionAnswerModel[Any]] = {

    val customerTypeData = getCustomerTypeAnswer(summaryModel)
    val currentIncomeData = getCurrentIncomeAnswer(summaryModel)
    val personalAllowanceData = getPersonalAllowanceAnswer(summaryModel)
    val disabledTrusteeData = getDisabledTrusteeAnswer(summaryModel)
    val otherPropertiesData = getOtherPropertiesAnswer(summaryModel)
    val otherPropertiesAmountData = getOtherPropertiesAmountAnswer(summaryModel)
    val annualExemptAmountData = getAnnualExemptAmountAnswer(summaryModel)

    val items = Seq(
      customerTypeData,
      currentIncomeData,
      personalAllowanceData,
      disabledTrusteeData,
      otherPropertiesData,
      otherPropertiesAmountData,
      annualExemptAmountData
    )

    items.flatten
  }

  def getCustomerTypeAnswer(summaryModel: SummaryModel): Option[QuestionAnswerModel[String]] = {
    Some(QuestionAnswerModel(
      KeystoreKeys.customerType,
      summaryModel.customerTypeModel.customerType,
      Messages("calc.customerType.question"),
      Some(controllers.nonresident.routes.CustomerTypeController.customerType().url)
    ))
  }

  //Customer type needs to be individual
  def getCurrentIncomeAnswer(summaryModel: SummaryModel): Option[QuestionAnswerModel[BigDecimal]] =
    (summaryModel.customerTypeModel.customerType, summaryModel.currentIncomeModel) match {
    case (CustomerTypeKeys.individual, Some(currentIncomeModel)) =>
      Some(QuestionAnswerModel(
        KeystoreKeys.currentIncome,
        currentIncomeModel.currentIncome,
        Messages("calc.currentIncome.question"),
        Some(controllers.nonresident.routes.CurrentIncomeController.currentIncome().url))
      )
    case _ => None
  }

  //Customer type needs to be individual
  def getPersonalAllowanceAnswer(summaryModel: SummaryModel): Option[QuestionAnswerModel[BigDecimal]] =
    (summaryModel.customerTypeModel.customerType, summaryModel.personalAllowanceModel)  match {
    case (CustomerTypeKeys.individual, Some(personalAllowanceModel)) =>
      Some(QuestionAnswerModel(
        KeystoreKeys.personalAllowance,
        personalAllowanceModel.personalAllowanceAmt,
        Messages("calc.personalAllowance.question"),
        Some(controllers.nonresident.routes.PersonalAllowanceController.personalAllowance().url))
      )
    case _ => None
  }

  //Customer type needs to be trustee
  def getDisabledTrusteeAnswer(summaryModel: SummaryModel): Option[QuestionAnswerModel[String]] =
    (summaryModel.customerTypeModel.customerType, summaryModel.disabledTrusteeModel)  match {
    case (CustomerTypeKeys.trustee, Some(disabledTrusteeModel)) => Some(QuestionAnswerModel(
      KeystoreKeys.disabledTrustee,
      disabledTrusteeModel.isVulnerable,
      Messages("calc.disabledTrustee.question"),
      Some(controllers.nonresident.routes.DisabledTrusteeController.disabledTrustee().url)))
    case _ => None
  }

  def getOtherPropertiesAnswer(summaryModel: SummaryModel): Option[QuestionAnswerModel[String]] = {
    Some(QuestionAnswerModel(
      KeystoreKeys.otherProperties,
      summaryModel.otherPropertiesModel.otherProperties,
      Messages("calc.otherProperties.question"),
      Some(controllers.nonresident.routes.OtherPropertiesController.otherProperties().url)
    ))
  }

  def getOtherPropertiesAmountAnswer(summaryModel: SummaryModel): Option[QuestionAnswerModel[BigDecimal]] =
    (summaryModel.otherPropertiesModel.otherProperties,
      summaryModel.otherPropertiesModel.otherPropertiesAmt,
      summaryModel.customerTypeModel.customerType) match {
    case ("Yes", Some(otherPropertiesAmount), CustomerTypeKeys.individual) => Some(QuestionAnswerModel(
      KeystoreKeys.otherProperties + "Amount",
      otherPropertiesAmount,
      Messages("calc.otherProperties.questionTwo"),
      Some(controllers.nonresident.routes.OtherPropertiesController.otherProperties().url)))
    case _ => None
  }

  //Only if otherProperties is yes and taxable gain is 0
  def getAnnualExemptAmountAnswer(summaryModel: SummaryModel): Option[QuestionAnswerModel[BigDecimal]] =
    (summaryModel.otherPropertiesModel.otherProperties, summaryModel.otherPropertiesModel.otherPropertiesAmt) match {
      case ("Yes", Some(x)) if x == BigDecimal(0) =>
        Some(QuestionAnswerModel(
          KeystoreKeys.annualExemptAmount,
          summaryModel.annualExemptAmountModel.get.annualExemptAmount,
          Messages("calc.annualExemptAmount.question"),
          Some(controllers.nonresident.routes.AnnualExemptAmountController.annualExemptAmount().url))
        )
      case _ => None
  }

}

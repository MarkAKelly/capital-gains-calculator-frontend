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
import models.nonresident.{QuestionAnswerModel, SummaryModel}
import play.api.i18n.Messages

object PersonalDetailsConstructor {

  def getCustomerTypeAnswer(summaryModel: SummaryModel): QuestionAnswerModel[String] = {
    QuestionAnswerModel(KeystoreKeys.customerType, summaryModel.customerTypeModel.customerType,
      Messages("calc.customerType.question"), Some(controllers.nonresident.routes.CustomerTypeController.customerType().url))
  }

  //Customer type needs to be individual
  def getCurrentIncomeAnswer(summaryModel: SummaryModel): Option[QuestionAnswerModel[BigDecimal]] = summaryModel.customerTypeModel.customerType match {
    case "individual" => Some(QuestionAnswerModel(KeystoreKeys.currentIncome, summaryModel.currentIncomeModel.get.currentIncome,
      Messages("calc.currentIncome.question"), Some(controllers.nonresident.routes.CurrentIncomeController.currentIncome().url)))
    case _ => None
  }

  //Customer type needs to be individual
  def getPersonalAllowanceAnswer(summaryModel: SummaryModel): Option[QuestionAnswerModel[BigDecimal]] = summaryModel.customerTypeModel.customerType match {
    case "individual" => Some(QuestionAnswerModel(KeystoreKeys.personalAllowance, summaryModel.personalAllowanceModel.get.personalAllowanceAmt,
      Messages("calc.personalAllowance.question"), Some(controllers.nonresident.routes.PersonalAllowanceController.personalAllowance().url)))
    case _ => None
  }

  //Customer type needs to be trustee
  def getDisabledTrusteesAnswer(summaryModel: SummaryModel): Option[String] = summaryModel.disabledTrusteeModel.isEmpty match {
    case true => None
    case false => Some(summaryModel.disabledTrusteeModel.get.isVulnerable)
  }

  def getOtherPropertiesAnswer(summaryModel: SummaryModel): BigDecimal = {
    summaryModel.otherPropertiesModel.otherPropertiesAmt.get
  }

  //Only if otherProperties is yes and taxable gain is 0
  def getAEAAnswer(summaryModel: SummaryModel): Option[BigDecimal] = summaryModel.annualExemptAmountModel.isEmpty match {
    case true => None
    case false => Some(summaryModel.annualExemptAmountModel.get.annualExemptAmount)
  }

}

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
import common.nonresident.{CustomerTypeKeys, PreviousGainOrLossKeys}
import models.nonresident._
import play.api.i18n.Messages

import scala.math.BigDecimal

object PersonalDetailsConstructor {

  def getPersonalDetailsSection(summaryModel: TotalPersonalDetailsCalculationModel): Seq[QuestionAnswerModel[Any]] = {

    val customerTypeData = getCustomerTypeAnswer(summaryModel.customerTypeModel)
    val currentIncomeData = getCurrentIncomeAnswer(summaryModel.customerTypeModel, summaryModel.currentIncomeModel)
    val personalAllowanceData = getPersonalAllowanceAnswer(summaryModel.customerTypeModel, summaryModel.personalAllowanceModel)
    val disabledTrusteeData = getDisabledTrusteeAnswer(summaryModel.customerTypeModel, summaryModel.trusteeModel)
    val otherPropertiesData = getOtherPropertiesAnswer(summaryModel.otherPropertiesModel)
    val annualExemptAmountData = getAnnualExemptAmountAnswer(summaryModel.otherPropertiesModel,
      summaryModel.previousGainOrLoss, summaryModel.annualExemptAmountModel, summaryModel.howMuchGainModel, summaryModel.howMuchLossModel)

    val items = Seq(
      customerTypeData,
      currentIncomeData,
      personalAllowanceData,
      disabledTrusteeData,
      otherPropertiesData,
      annualExemptAmountData
    )

    items.flatten
  }

  def getCustomerTypeAnswer(customerTypeModel: CustomerTypeModel): Option[QuestionAnswerModel[String]] = {
    val answer = customerTypeModel.customerType match {
      case CustomerTypeKeys.individual => Messages("calc.customerType.individual")
      case CustomerTypeKeys.trustee => Messages("calc.customerType.trustee")
      case CustomerTypeKeys.personalRep => Messages("calc.customerType.personalRep")
    }

    Some(QuestionAnswerModel(
      KeystoreKeys.customerType,
      answer,
      Messages("calc.customerType.question"),
      Some(controllers.nonresident.routes.CustomerTypeController.customerType().url)
    ))
  }

  //Customer type needs to be individual
  def getCurrentIncomeAnswer(customerTypeModel: CustomerTypeModel, currentIncomeModel: Option[CurrentIncomeModel]): Option[QuestionAnswerModel[BigDecimal]] =
    (customerTypeModel.customerType, currentIncomeModel) match {
      case (CustomerTypeKeys.individual, Some(CurrentIncomeModel(value))) =>
        Some(QuestionAnswerModel(
          KeystoreKeys.currentIncome,
          value,
          Messages("calc.currentIncome.question"),
          Some(controllers.nonresident.routes.CurrentIncomeController.currentIncome().url))
        )
      case _ => None
    }

  //Customer type needs to be individual
  def getPersonalAllowanceAnswer(customerTypeModel: CustomerTypeModel,
                                 personalAllowanceModel: Option[PersonalAllowanceModel]): Option[QuestionAnswerModel[BigDecimal]] =
    (customerTypeModel.customerType, personalAllowanceModel) match {
      case (CustomerTypeKeys.individual, Some(PersonalAllowanceModel(value))) =>
        Some(QuestionAnswerModel(
          KeystoreKeys.personalAllowance,
          value,
          Messages("calc.personalAllowance.question"),
          Some(controllers.nonresident.routes.PersonalAllowanceController.personalAllowance().url))
        )
      case _ => None
    }

  //Customer type needs to be trustee
  def getDisabledTrusteeAnswer(customerTypeModel: CustomerTypeModel, trusteeModel: Option[DisabledTrusteeModel]): Option[QuestionAnswerModel[String]] =
    (customerTypeModel.customerType, trusteeModel) match {
      case (CustomerTypeKeys.trustee, Some(disabledTrusteeModel)) => Some(QuestionAnswerModel(
        KeystoreKeys.disabledTrustee,
        disabledTrusteeModel.isVulnerable,
        Messages("calc.disabledTrustee.question"),
        Some(controllers.nonresident.routes.DisabledTrusteeController.disabledTrustee().url)))
      case _ => None
    }

  def getOtherPropertiesAnswer(otherPropertiesModel: OtherPropertiesModel): Option[QuestionAnswerModel[String]] = {
    Some(QuestionAnswerModel(
      KeystoreKeys.otherProperties,
      otherPropertiesModel.otherProperties,
      Messages("calc.otherProperties.question"),
      Some(controllers.nonresident.routes.OtherPropertiesController.otherProperties().url)
    ))
  }

  def previousGainOrLossAnswer(otherPropertiesModel: OtherPropertiesModel,
                               previousLossOrGainModel: Option[PreviousLossOrGainModel]): Option[QuestionAnswerModel[String]] = {
    otherPropertiesModel match {
      case OtherPropertiesModel("Yes") =>
        Some(QuestionAnswerModel(
          KeystoreKeys.NonResidentKeys.previousLossOrGain,
          previousLossOrGainModel.get.previousLossOrGain,
          Messages("calc.previousLossOrGain.question"),
          Some(controllers.nonresident.routes.PreviousGainOrLossController.previousGainOrLoss().url)
        ))
      case _ => None
    }
  }

  def howMuchGainAnswer(otherPropertiesModel: OtherPropertiesModel,
                        previousLossOrGainModel: Option[PreviousLossOrGainModel],
                        howMuchGainModel: Option[HowMuchGainModel]): Option[QuestionAnswerModel[BigDecimal]] = {
    (otherPropertiesModel, previousLossOrGainModel) match {
      case (OtherPropertiesModel("Yes"), Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain))) =>
        Some(QuestionAnswerModel(
          KeystoreKeys.howMuchGain,
          howMuchGainModel.get.howMuchGain,
          Messages("calc.howMuchGain.question"),
          Some(controllers.nonresident.routes.HowMuchGainController.howMuchGain().url)
        ))
      case _ => None
    }
  }

  def howMuchLoss(otherPropertiesModel: OtherPropertiesModel,
                  previousLossOrGainModel: Option[PreviousLossOrGainModel],
                  howMuchLossModel: Option[HowMuchLossModel]): Option[QuestionAnswerModel[BigDecimal]] = {
    (otherPropertiesModel, previousLossOrGainModel) match {
      case (OtherPropertiesModel("Yes"), Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss))) =>
        Some(QuestionAnswerModel(
          KeystoreKeys.howMuchLoss,
          howMuchLossModel.get.loss,
          Messages("calc.howMuchLoss.question"),
          Some(controllers.nonresident.routes.HowMuchLossController.howMuchLoss().url)
        ))
      case _ => None
    }
  }

  //TEST
  def getAnnualExemptAmountAnswer(otherPropertiesModel: OtherPropertiesModel,
                                  previousLossOrGainModel: Option[PreviousLossOrGainModel],
                                  annualExemptAmountModel: Option[AnnualExemptAmountModel],
                                  howMuchGainModel: Option[HowMuchGainModel],
                                  howMuchLossModel: Option[HowMuchLossModel]): Option[QuestionAnswerModel[BigDecimal]] = {
    val id = KeystoreKeys.annualExemptAmount
    val question = Messages("calc.annualExemptAmount.question")
    val route = Some(controllers.nonresident.routes.AnnualExemptAmountController.annualExemptAmount().url)

    (otherPropertiesModel, previousLossOrGainModel) match {
      case (OtherPropertiesModel("Yes"), Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.neither))) =>
        Some(QuestionAnswerModel(
          id,
          annualExemptAmountModel.get.annualExemptAmount,
          question,
          route)
        )
      case (OtherPropertiesModel("Yes"), Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.gain)))
        if howMuchGainModel.get.howMuchGain == 0 =>
        Some(QuestionAnswerModel(
          id,
          annualExemptAmountModel.get.annualExemptAmount,
          question,
          route)
        )
      case (OtherPropertiesModel("Yes"), Some(PreviousLossOrGainModel(PreviousGainOrLossKeys.loss)))
        if howMuchLossModel.get.loss == 0 =>
        Some(QuestionAnswerModel(
          id,
          annualExemptAmountModel.get.annualExemptAmount,
          question,
          route)
        )
      case _ => None
    }
  }

}

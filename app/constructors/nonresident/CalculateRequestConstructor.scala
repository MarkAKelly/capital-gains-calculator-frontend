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

import common.Dates
import models.nonresident._

import scala.math.BigDecimal

object CalculateRequestConstructor {

  def baseCalcUrl(input: SummaryModel): String = {
    customerType(input.customerTypeModel.customerType) +
    priorDisposal(input.otherPropertiesModel.otherProperties) +
    annualExemptAmount(input.otherPropertiesModel, input.annualExemptAmountModel) +
    otherPropertiesAmount(input.otherPropertiesModel) +
    isVulnerableTrustee(input.customerTypeModel.customerType, input.disabledTrusteeModel) +
    currentIncome(input.customerTypeModel.customerType, input.currentIncomeModel) +
    personalAllowanceAmount(input.customerTypeModel.customerType, input.personalAllowanceModel) +
    disposalValue(input.disposalValueModel.disposalValue) +
    disposalCosts(input.disposalCostsModel.disposalCosts) +
    allowableLossesAmount(input.allowableLossesModel) +
    disposalDate(input.disposalDateModel)
  }

  def customerType(customerType: String): String = s"customerType=$customerType"

  def priorDisposal(otherProperties: String): String = s"&priorDisposal=$otherProperties"

  def annualExemptAmount(otherPropertiesModel: OtherPropertiesModel, annualExemptAmountModel: Option[AnnualExemptAmountModel]): String = {
    otherPropertiesModel match {
      case OtherPropertiesModel("Yes", data) if data.get == 0 => s"&annualExemptAmount=${annualExemptAmountModel.get.annualExemptAmount}"
      case _ => ""
    }
  }

  def otherPropertiesAmount(otherPropertiesModel: OtherPropertiesModel): String = {
    if (otherPropertiesModel.otherProperties.equals("Yes")) s"&otherPropertiesAmt=${otherPropertiesModel.otherPropertiesAmt.get}"
    else ""
  }

  def isVulnerableTrustee(customerType: String, disabledTrusteeModel: Option[DisabledTrusteeModel]): String = {
    if (customerType.equals("trustee")) s"&isVulnerable=${disabledTrusteeModel.get.isVulnerable}"
    else ""
  }

  def currentIncome(customerType: String, currentIncomeModel: Option[CurrentIncomeModel]): String = {
    if (customerType.equals("individual")) s"&currentIncome=${currentIncomeModel.get.currentIncome}"
    else ""
  }

  def personalAllowanceAmount(customerType: String, personalAllowanceModel: Option[PersonalAllowanceModel]): String = {
    if (customerType.equals("individual")) s"&personalAllowanceAmt=${personalAllowanceModel.get.personalAllowanceAmt}"
    else ""
  }

  def disposalValue(disposalValue: BigDecimal): String = s"&disposalValue=$disposalValue"

  def disposalCosts(disposalCosts: BigDecimal): String = s"&disposalCosts=$disposalCosts"

  def allowableLossesAmount(allowableLossesModel: AllowableLossesModel): String = {
    s"&allowableLossesAmt=${
      if (allowableLossesModel.isClaimingAllowableLosses.equals("Yes")) allowableLossesModel.allowableLossesAmt.get
      else "0"
    }"
  }

  def disposalDate(disposalDateModel: DisposalDateModel): String = {
    s"&disposalDate=${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}"
  }

  def flatCalcUrlExtra(input: SummaryModel): String = {
    val isClaimingPrr = isClaimingPRR(input)
    improvements(input) +
      acquisition(input) +
      flatReliefs(input.otherReliefsModelFlat.otherReliefs) +
      privateResidenceReliefFlat(input) +
      isClaimingPrr +
      flatAcquisitionDate(isClaimingPrr, input.acquisitionDateModel)
  }

  def flatReliefs(reliefsValue: Option[BigDecimal]): String = {
    s"&reliefs=${reliefsValue.getOrElse(0)}"
  }

  def flatAcquisitionDate(isClaimingPrr: String, acquisitionDateModel: AcquisitionDateModel): String = {
    if (isClaimingPrr.contains("Yes"))
      s"&acquisitionDate=${acquisitionDateModel.year.get}-${acquisitionDateModel.month.get}-${acquisitionDateModel.day.get}"
    else ""
  }

  def taCalcUrlExtra(input: SummaryModel): String = {
    s"${
      improvements(input)
    }&acquisitionDate=${
      input.acquisitionDateModel.year.get
    }-${input.acquisitionDateModel.month.get}-${
      input.acquisitionDateModel.day.get
    }${
      acquisition(input)
    }&reliefs=${
      input.otherReliefsModelTA.otherReliefs.getOrElse(0)
    }${
      privateResidenceReliefTA(input)
    }${
      isClaimingPRR(input)
    }"
  }

  def rebasedCalcUrlExtra(input: SummaryModel): String = {
    s"&improvementsAmt=${
      input.improvementsModel.isClaimingImprovements match {
        case "Yes" => input.improvementsModel.improvementsAmtAfter.getOrElse(0)
        case "No" => 0
      }
    }&rebasedValue=${
      input.rebasedValueModel.get.rebasedValueAmt.get
    }&revaluationCost=${
      input.rebasedCostsModel.get.hasRebasedCosts match {
        case "Yes" => input.rebasedCostsModel.get.rebasedCosts.get
        case "No" => 0
      }
    }&reliefs=${
      input.otherReliefsModelRebased.otherReliefs.getOrElse(0)
    }${
      privateResidenceReliefRebased(input)
    }&isClaimingPRR=${
      input.privateResidenceReliefModel match {
        case Some(PrivateResidenceReliefModel("Yes", claimed, after)) => "Yes"
        case _ => "No"
      }
    }"
  }

  def improvements(input: SummaryModel): String = s"&improvementsAmt=${
    (input.improvementsModel.isClaimingImprovements, input.rebasedValueModel) match {
      case ("Yes", Some(RebasedValueModel("Yes", _))) =>
        input.improvementsModel.improvementsAmtAfter.getOrElse(BigDecimal(0)) + input.improvementsModel.improvementsAmt.getOrElse(BigDecimal(0))
      case ("No", _) => 0
      case _ => input.improvementsModel.improvementsAmt.getOrElse(0)
    }
  }"

  def privateResidenceReliefFlat(input: SummaryModel): String = s"${
    (input.acquisitionDateModel, input.privateResidenceReliefModel) match {
      case (AcquisitionDateModel("Yes", day, month, year), Some(PrivateResidenceReliefModel("Yes", claimed, after))) if claimed.isDefined =>
        s"&daysClaimed=${claimed.get}"
      case _ => ""
    }
  }"

  def privateResidenceReliefTA(input: SummaryModel): String = s"${
    (input.acquisitionDateModel, input.privateResidenceReliefModel) match {
      case (AcquisitionDateModel("Yes", day, month, year), Some(PrivateResidenceReliefModel("Yes", claimed, after)))
        if Dates.dateAfter18Months(input.disposalDateModel.day, input.disposalDateModel.month, input.disposalDateModel.year) && after.isDefined =>
        s"&daysClaimedAfter=${after.get}"

      case _ => ""
    }
  }"

  def privateResidenceReliefRebased(input: SummaryModel): String = s"${
    (input.rebasedValueModel, input.privateResidenceReliefModel) match {
      case (Some(RebasedValueModel("Yes", rebasedValue)), Some(PrivateResidenceReliefModel("Yes", claimed, after)))
        if Dates.dateAfter18Months(input.disposalDateModel.day, input.disposalDateModel.month, input.disposalDateModel.year) && after.isDefined =>
        s"&daysClaimedAfter=${after.get}"
      case _ => ""
    }
  }"

  def isClaimingPRR(input: SummaryModel): String = s"&isClaimingPRR=${
    (input.acquisitionDateModel, input.privateResidenceReliefModel) match {
      case (AcquisitionDateModel("Yes", day, month, year), Some(PrivateResidenceReliefModel("Yes", claimed, after))) => "Yes"
      case _ => "No"
    }
  }"

  def acquisition(input: SummaryModel): String = s"&acquisitionValueAmt=${
    input.acquisitionValueModel.acquisitionValueAmt
  }&acquisitionCostsAmt=${
    input.acquisitionCostsModel.acquisitionCostsAmt
  }"
}

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

package models.nonresident

import common.nonresident.CalculationType
import constructors.nonresident.{DeductionDetailsConstructor, PersonalDetailsConstructor, PropertyDetailsConstructor, SalesDetailsConstructor}

case class SummaryModel(
                          customerTypeModel: CustomerTypeModel,
                          disabledTrusteeModel: Option[DisabledTrusteeModel],
                          currentIncomeModel : Option[CurrentIncomeModel],
                          personalAllowanceModel: Option[PersonalAllowanceModel],
                          otherPropertiesModel: OtherPropertiesModel,
                          annualExemptAmountModel: Option[AnnualExemptAmountModel],
                          acquisitionDateModel: AcquisitionDateModel,
                          acquisitionValueModel: AcquisitionValueModel,
                          rebasedValueModel: Option[RebasedValueModel],
                          rebasedCostsModel: Option[RebasedCostsModel],
                          improvementsModel: ImprovementsModel,
                          disposalDateModel: DisposalDateModel,
                          disposalValueModel: DisposalValueModel,
                          acquisitionCostsModel: AcquisitionCostsModel,
                          disposalCostsModel : DisposalCostsModel,
                          allowableLossesModel : AllowableLossesModel,
                          calculationElectionModel: CalculationElectionModel,
                          otherReliefsModelFlat : OtherReliefsModel,
                          otherReliefsModelTA: OtherReliefsModel,
                          otherReliefsModelRebased: OtherReliefsModel,
                          privateResidenceReliefModel: Option[PrivateResidenceReliefModel]
                        ) {

  def reliefApplied(): String = calculationElectionModel.calculationType match {
    case CalculationType.flat if otherReliefsModelFlat.otherReliefs.getOrElse(BigDecimal(0)) > 0 => CalculationType.flat
    case CalculationType.rebased if otherReliefsModelRebased.otherReliefs.getOrElse(BigDecimal(0)) > 0 => CalculationType.rebased
    case CalculationType.timeApportioned if otherReliefsModelTA.otherReliefs.getOrElse(BigDecimal(0)) > 0 => CalculationType.timeApportioned
    case _ => "none"
  }

  val personalDetailsRows: Seq[QuestionAnswerModel[Any]] = PersonalDetailsConstructor.getPersonalDetailsSection(this)
  val saleDetailsRows: Seq[QuestionAnswerModel[Any]] = SalesDetailsConstructor.salesDetailsRows(this)
  val purchaseDetailsRows: Seq[QuestionAnswerModel[Any]] = Seq(QuestionAnswerModel("purchase", 500, "question", Some("")))
  val propertyDetailsRows: Seq[QuestionAnswerModel[Any]] = PropertyDetailsConstructor.propertyDetailsRows(this)
  def deductionsDetailsRows(result: CalculationResultModel): Seq[QuestionAnswerModel[Any]] =
    DeductionDetailsConstructor.deductionDetailsRows(this, result)
}

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

package common

import models._
import common.nonresident.CustomerTypeKeys
import models.nonresident._

object TestModels {

  val sumModelFlat = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(11100)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("No", None, None, None),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("No", None)),
    None,
    ImprovementsModel("No", None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(None),
    DisposalCostsModel(None),
    AllowableLossesModel("No", None),
    CalculationElectionModel("flat"),
    OtherReliefsModel(Some("No)"), None),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    None
  )

  val sumModelTA = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(11100)),
    OtherPropertiesModel("Yes", Some(2100)),
    Some(AnnualExemptAmountModel(9000)),
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(9)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("No", None)),
    None,
    ImprovementsModel("Yes", Some(500)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(650)),
    DisposalCostsModel(Some(850)),
    AllowableLossesModel("No", None),
    CalculationElectionModel("time"),
    OtherReliefsModel(None, Some(2000)),
    OtherReliefsModel(None, Some(1000)),
    OtherReliefsModel(None, Some(500)),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val sumModelRebased = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(11100)),
    OtherPropertiesModel("Yes", Some(2100)),
    Some(AnnualExemptAmountModel(9000)),
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(9)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("No", None)),
    None,
    ImprovementsModel("Yes", Some(500)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(650)),
    DisposalCostsModel(Some(850)),
    AllowableLossesModel("No", None),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(None, Some(2000)),
    OtherReliefsModel(None, Some(1000)),
    OtherReliefsModel(None, Some(500)),
    Some(PrivateResidenceReliefModel("No", None, None))
  )


  val calcModelTwoRates = CalculationResultModel(8000, 40000, 32000, 18, 0, Some(8000), Some(28), None)
  val calcModelOneRate = CalculationResultModel(8000, 40000, 32000, 20, 8000, None, None, None)
  val calcModelLoss = CalculationResultModel(0, -10000, 0, 18, 0, None, None, None)
  val calcModelUpperRate = CalculationResultModel(8000, 40000, 0, 0, 8000, Some(32000), Some(28), None)
  val calcModelNegativeTaxable = CalculationResultModel(0, 50000, -10000, 0, 0, None, None, None)
  val calcModelZeroTaxable = CalculationResultModel(0, 50000, 0, 0, 11000, None, None, None)
  val calcModelZeroTotal = CalculationResultModel(0, 0, 0, 0, 0, None, None, None)
  val calcModelSomePRR = CalculationResultModel(0, 0, 0, 0, 0, None, None, Some(10000))


  val summaryIndividualFlatNoIncomeOtherPropNo = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(0)),
    None,
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("No", None, None, None),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("No", None)),
    None,
    ImprovementsModel("No", None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(None),
    DisposalCostsModel(None),
    AllowableLossesModel("No", None),
    CalculationElectionModel("flat"),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    None
  )

  val summaryIndividualFlatNoIncomeOtherPropYes = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(0)),
    None,
    OtherPropertiesModel("Yes", Some(0)),
    Some(AnnualExemptAmountModel(1500)),
    AcquisitionDateModel("No", None, None, None),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("No", None)),
    None,
    ImprovementsModel("No", None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(None),
    DisposalCostsModel(None),
    AllowableLossesModel("No", None),
    CalculationElectionModel("flat"),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    None
  )

  val summaryIndividualFlatLoss = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("No", None, None, None),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("No", None)),
    None,
    ImprovementsModel("Yes", Some(0)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(90000),
    AcquisitionCostsModel(Some(0)),
    DisposalCostsModel(Some(0)),
    AllowableLossesModel("Yes", Some(0)),
    CalculationElectionModel("flat"),
    OtherReliefsModel(Some("No"), Some(0)),
    OtherReliefsModel(None, Some(0)),
    OtherReliefsModel(None, Some(0)),
    None
  )

  val summaryIndividualFlatWithoutAEA = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("No", None, None, None),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("No", None)),
    None,
    ImprovementsModel("Yes", Some(8000)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("flat"),
    OtherReliefsModel(Some("Yes"), Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, Some(777)),
    None
  )

  val summaryIndividualFlatWithAEA = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("Yes", Some(9600)),
    Some(AnnualExemptAmountModel(1500)),
    AcquisitionDateModel("No", None, None, None),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("No", None)),
    None,
    ImprovementsModel("No", None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(None),
    DisposalCostsModel(None),
    AllowableLossesModel("No", None),
    CalculationElectionModel("flat"),
    OtherReliefsModel(Some("No"), None),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    None
  )

  val summaryTrusteeTAWithAEA = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.trustee),
    Some(DisabledTrusteeModel("No")),
    None,
    None,
    OtherPropertiesModel("Yes", None),
    Some(AnnualExemptAmountModel(1500)),
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(1999)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("No", None)),
    None,
    ImprovementsModel("No", None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(None),
    DisposalCostsModel(None),
    AllowableLossesModel("No", None),
    CalculationElectionModel("time"),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryTrusteeTAWithoutAEA = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.trustee),
    Some(DisabledTrusteeModel("No")),
    None,
    None,
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(1999)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("No", None)),
    None,
    ImprovementsModel("Yes", Some(8000)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(None),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("time"),
    OtherReliefsModel(None, Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, Some(777)),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryDisabledTrusteeTAWithAEA = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.trustee),
    Some(DisabledTrusteeModel("Yes")),
    None,
    None,
    OtherPropertiesModel("Yes", None),
    Some(AnnualExemptAmountModel(1500)),
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(1999)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("No", None)),
    None,
    ImprovementsModel("No", None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(None),
    DisposalCostsModel(None),
    AllowableLossesModel("No", None),
    CalculationElectionModel("time"),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryDisabledTrusteeTAWithoutAEA = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.trustee),
    Some(DisabledTrusteeModel("Yes")),
    None,
    None,
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(1999)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("No", None)),
    None,
    ImprovementsModel("Yes", Some(8000)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(None),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("time"),
    OtherReliefsModel(None, Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, Some(777)),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryRepresentativeFlatWithoutAEA = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.personalRep),
    None,
    None,
    None,
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("No", None, None, None),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("No", None)),
    None,
    ImprovementsModel("Yes", Some(8000)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(None),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("flat"),
    OtherReliefsModel(Some("Yes"), Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, Some(777)),
    None
  )

  val summaryRepresentativeFlatWithAEA = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.personalRep),
    None,
    None,
    None,
    OtherPropertiesModel("Yes", None),
    Some(AnnualExemptAmountModel(1500)),
    AcquisitionDateModel("No", None, None, None),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("No", None)),
    None,
    ImprovementsModel("No", None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(None),
    DisposalCostsModel(None),
    AllowableLossesModel("No", None),
    CalculationElectionModel("flat"),
    OtherReliefsModel(Some("No"), None),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    None
  )

  val summaryIndividualAcqDateAfter = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("Yes", Some(6), Some(6), Some(2016)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("No", None)),
    None,
    ImprovementsModel("Yes", Some(8000)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("flat"),
    OtherReliefsModel(Some("Yes"), Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, Some(777)),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryIndividualImprovementsNoRebasedModel = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(1999)),
    AcquisitionValueModel(100000),
    None,
    None,
    ImprovementsModel("Yes", Some(8000)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("flat"),
    OtherReliefsModel(None, Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, Some(777)),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryIndividualImprovementsWithRebasedModel = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(1999)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("Yes", Some(1000))),
    None,
    ImprovementsModel("Yes", Some(8000), Some(1000)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("flat"),
    OtherReliefsModel(None, Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, Some(777)),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryIndividualRebased = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(1999)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("Yes", Some(150000))),
    Some(RebasedCostsModel("Yes", Some(1000))),
    ImprovementsModel("Yes", Some(2000), Some(3000)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(None, Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, Some(777)),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryIndividualRebasedNoAcqDate = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("No", None, None, None),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("Yes", Some(150000))),
    Some(RebasedCostsModel("Yes", Some(1000))),
    ImprovementsModel("Yes", Some(2000), Some(3000)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("No", None),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryIndividualRebasedNoRebasedCosts = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(1999)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("Yes", Some(150000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", Some(2000), Some(3000)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(None, Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, Some(777)),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryIndividualRebasedNoAcqDateOrRebasedCosts = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("No", None, None, None),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("Yes", Some(150000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", Some(2000), Some(3000)),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("No", None),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryIndividualRebasedNoImprovements = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(1999)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("Yes", Some(150000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("No", None, None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(None, Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, None),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryIndividualRebasedNoneImprovements = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(1999)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("Yes", Some(150000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", None, None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(None, Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, None),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryIndividualRebasedAcqDateAfter = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(2016)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("Yes", Some(150000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", None, None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(None, Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, None),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

  val summaryIndividualPRRAcqDateAfterAndDisposalDateBefore = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(2016)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("Yes", Some(150000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", None, None),
    DisposalDateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(None, Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, None),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val summaryIndividualPRRAcqDateAfterAndNoRebased = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(2012)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("No", None)),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", None, None),
    DisposalDateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(None, Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, None),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val summaryIndividualPRRAcqDateAfterAndDisposalDateAfter = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(2012)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("Yes", Some(1000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", None, None),
    DisposalDateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(None, Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, None),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val summaryIndividualPRRAcqDateAfterAndDisposalDateBeforeWithRebased = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(2012)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("Yes", Some(1000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", None, None),
    DisposalDateModel(10, 10, 2015),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(None, Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, None),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val summaryIndividualPRRNoAcqDateAndDisposalDateAfterWithRebased = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("No", None, None, None),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("Yes", Some(1000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", None, None),
    DisposalDateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(None, Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, None),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val summaryIndividualPRRAcqDateAfterAndDisposalDateAfterWithRebased = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(2016)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("Yes", Some(1000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", None, None),
    DisposalDateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(None, Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, None),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val summaryIndividualPRRAcqDateBeforeAndDisposalDateAfterWithRebased = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(2012)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("Yes", Some(1000))),
    Some(RebasedCostsModel("No", None)),
    ImprovementsModel("Yes", None, None),
    DisposalDateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(None, Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, None),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val summaryIndividualWithAllOptions = SummaryModel (
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(9000)),
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(1999)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("Yes", Some(1000))),
    Some(RebasedCostsModel("Yes", Some(500))),
    ImprovementsModel("Yes", Some(8000)),
    DisposalDateModel(10, 10, 2018),
    DisposalValueModel(150000),
    AcquisitionCostsModel(Some(300)),
    DisposalCostsModel(Some(600)),
    AllowableLossesModel("Yes", Some(50000)),
    CalculationElectionModel("flat"),
    OtherReliefsModel(None, Some(999)),
    OtherReliefsModel(None, Some(888)),
    OtherReliefsModel(None, Some(777)),
    Some(PrivateResidenceReliefModel("Yes", Some(100), Some(50)))
  )

  val summaryPriorDisposalNoTaxableGain = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(11100)),
    OtherPropertiesModel("Yes", Some(0)),
    Some(AnnualExemptAmountModel(4300)),
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(1990)),
    AcquisitionValueModel(100000),
    Some(RebasedValueModel("No", None)),
    None,
    ImprovementsModel("No", None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(None),
    DisposalCostsModel(None),
    AllowableLossesModel("No", None),
    CalculationElectionModel("flat"),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    Some(PrivateResidenceReliefModel("No", None, None))
  )

}

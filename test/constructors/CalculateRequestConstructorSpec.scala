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

package constructors

import models._
import uk.gov.hmrc.play.test.UnitSpec

class CalculateRequestConstructorSpec extends UnitSpec {

  val sumModel = SummaryModel(
    CustomerTypeModel("individual"),
    None,
    Some(CurrentIncomeModel(1000)),
    Some(PersonalAllowanceModel(11100)),
    OtherPropertiesModel("No"),
    None,
    AcquisitionDateModel("Yes", Some(9), Some(9), Some(1990)),
    AcquisitionValueModel(100000),
    ImprovementsModel("No", None),
    DisposalDateModel(10, 10, 2010),
    DisposalValueModel(150000),
    AcquisitionCostsModel(None),
    DisposalCostsModel(None),
    EntrepreneursReliefModel("No"),
    AllowableLossesModel("No", None),
    CalculationElectionModel("flat"),
    OtherReliefsModel(None),
    OtherReliefsModel(None)
  )

  "CalculateRequest Controller" should {
    "return a string from the baseCalcUrl as an individual with no prior disposal" in {
      CalculateRequestConstructor.baseCalcUrl(sumModel) shouldEqual "customerType=individual&priorDisposal=No&currentIncome=1000" +
        "&personalAllowanceAmt=11100&disposalValue=150000&disposalCosts=0&acquisitionValueAmt=100000" +
        "&acquisitionCostsAmt=0&reliefs=0&allowableLossesAmt=0&entReliefClaimed=No"
    }

    "return a string from the baseCalcUrl as a trustee with a prior disposal" in {
      val sumModelTrustee = SummaryModel(
        CustomerTypeModel("trustee"),
        Some(DisabledTrusteeModel("No")),
        None,
        None,
        OtherPropertiesModel("Yes"),
        Some(AnnualExemptAmountModel(5000)),
        AcquisitionDateModel("Yes", Some(9), Some(9), Some(1990)),
        AcquisitionValueModel(100000),
        ImprovementsModel("No", None),
        DisposalDateModel(10, 10, 2010),
        DisposalValueModel(150000),
        AcquisitionCostsModel(None),
        DisposalCostsModel(None),
        EntrepreneursReliefModel("No"),
        AllowableLossesModel("No", None),
        CalculationElectionModel("flat"),
        OtherReliefsModel(None),
        OtherReliefsModel(None)
      )
      CalculateRequestConstructor.baseCalcUrl(sumModelTrustee) shouldEqual "customerType=trustee&priorDisposal=Yes&annualExemptAmount=Some(5000)&isVulnerable=Some(No)" +
        "&disposalValue=150000&disposalCosts=0&acquisitionValueAmt=100000" +
        "&acquisitionCostsAmt=0&reliefs=0&allowableLossesAmt=0&entReliefClaimed=No"
    }

    "return a string from the flatCalcUrlExtra" in {
      CalculateRequestConstructor.flatCalcUrlExtra(sumModel) shouldEqual "&improvementsAmt=0"
    }

    "return a string from the taCalcUrlExtra" in {
      CalculateRequestConstructor.taCalcUrlExtra(sumModel) shouldEqual "&improvementsAmt=0&disposalDate=2010-10-10" +
        "&acquisitionDate=1990-9-9"
    }
  }

}
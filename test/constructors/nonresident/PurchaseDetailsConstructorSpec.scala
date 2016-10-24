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

import common.nonresident.CustomerTypeKeys
import models.nonresident._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import constructors.nonresident.PurchaseDetailsConstructor

/**
  * Created by emma on 24/10/16.
  */
object PurchaseDetailsConstructorSpec extends UnitSpec with WithFakeApplication {

  val summaryWithAllOptionValuesModel = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(30000.0)),
    Some(PersonalAllowanceModel(11000.0)),
    OtherPropertiesModel("Yes", Some(250000.0)),
    Some(AnnualExemptAmountModel(10000.0)),
    AcquisitionDateModel("Yes", Some(4), Some(9), Some(2016)),
    AcquisitionValueModel(300000.0),
    Some(RebasedValueModel("Yes", Some(350000.0))),
    Some(RebasedCostsModel("Yes", Some(4000.0))),
    ImprovementsModel("Yes", Some(2000.0)),
    DisposalDateModel(5, 9, 2016),
    DisposalValueModel(5000),
    AcquisitionCostsModel(250000.0),
    DisposalCostsModel(5000.0),
    AllowableLossesModel("Yes", Some(20000.0)),
    CalculationElectionModel("flat"),
    OtherReliefsModel(Some("Yes"), Some(100.0)),
    OtherReliefsModel(Some("Yes"), Some(100.0)),
    OtherReliefsModel(Some("Yes"), Some(100.0)),
    Some(PrivateResidenceReliefModel("Yes", Some(2500.0), Some(0.0)))
  )

  "Calling the PurchaseDetailsConstructor" when {

    "using the summaryWithAllOptionsValuesModel" should {
      ".getPurchaseDetailsSection will return a Sequence[QuestionAnswerModel[Any]] with size 5" in {
        PurchaseDetailsConstructor.getPurchaseDetailsSection(summaryWithAllOptionValuesModel).size shouldBe 5
      }
    }
  }
}

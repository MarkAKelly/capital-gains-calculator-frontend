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

import assets.MessageLookup
import common.KeystoreKeys
import common.nonresident.CustomerTypeKeys
import models.SummaryDataItemModel
import models.nonresident._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class PersonalDetailsConstructorSpec extends UnitSpec with WithFakeApplication {

  val summaryWithAllOptionValuesModel = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    Some(DisabledTrusteeModel("Yes")),
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

  val summaryWithNoOptionValuesModel = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    None,
    None,
    OtherPropertiesModel("No", None),
    None,
    AcquisitionDateModel("No", None, None, None),
    AcquisitionValueModel(300000.0),
    None,
    None,
    ImprovementsModel("No", None),
    DisposalDateModel(5, 9, 2016),
    DisposalValueModel(5000),
    AcquisitionCostsModel(250000.0),
    DisposalCostsModel(5000.0),
    AllowableLossesModel("No", None),
    CalculationElectionModel("flat"),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    None
  )

  "Calling PersonalDetailsConstructor" should {

    "when using the summaryWithAllOptionsValuesModel" should {

//      ".getPurchaseDetailsItem will return a Sequence[DataItems]" in{
//        PersonalDetailsConstructor.getPurchaseDetailsItem(summaryWithAllOptionValuesModel) shouldBe
//          Seq(QuestionAnswerModel(common.KeystoreKeys.customerType, CustomerTypeKeys.individual))
//      }

      ".getCustomerTypeAnswer with a customer type of individual will return an id of " +
         s"${KeystoreKeys.customerType}"in {
        PersonalDetailsConstructor.getCustomerTypeAnswer(summaryWithAllOptionValuesModel).id shouldBe
          KeystoreKeys.customerType
      }

      ".getCustomerTypeAnswer with a customer type of individual will return a question of " +
        s"${MessageLookup.NonResident.CustomerType.question}" in{
        PersonalDetailsConstructor.getCustomerTypeAnswer(summaryWithAllOptionValuesModel).question shouldBe
          MessageLookup.NonResident.CustomerType.question
      }

      ".getCustomerTypeAnswer with a customer type of individual will return data of " +
        s"${CustomerTypeKeys.individual}" in {
        PersonalDetailsConstructor.getCustomerTypeAnswer(summaryWithAllOptionValuesModel).data shouldBe
          CustomerTypeKeys.individual
      }

      ".getCustomerTypeAnswer with a customer type of individual will a link of " +
         s"${controllers.nonresident.routes.CustomerTypeController.customerType().url}" in {
        PersonalDetailsConstructor.getCustomerTypeAnswer(summaryWithAllOptionValuesModel).link shouldBe
          Some(controllers.nonresident.routes.CustomerTypeController.customerType().url)
      }

//      ".getCurrentIncomeAnswer with an income of 30000.0 will return 30000.0" in {
//        PersonalDetailsConstructor.getCurrentIncomeAnswer(summaryWithAllOptionValuesModel) shouldBe Some(30000.0)
//      }
//
//      ".getPersonalAllowanceAnswer with a personal allowance of 11000.0" in {
//        PersonalDetailsConstructor.getPersonalAllowanceAnswer(summaryWithAllOptionValuesModel) shouldBe Some(11000.0)
//      }
//
//      ".getDisabledTrusteeAnswer with a disabled trustee will return Yes" in {
//        PersonalDetailsConstructor.getDisabledTrusteesAnswer(summaryWithAllOptionValuesModel) shouldBe Some("Yes")
//      }
//
//      ".getOtherPropertiesAnswer with another property and a value of 250000.0" in {
//        PersonalDetailsConstructor.getOtherPropertiesAnswer(summaryWithAllOptionValuesModel) shouldBe 250000.0
//      }
//
//      ".getAEAAnswer with a an annual exempt amount of 10000.0" in {
//        PersonalDetailsConstructor.getAEAAnswer(summaryWithAllOptionValuesModel) shouldBe Some(10000.0)
//      }
//    }
//
//    "when using the summaryWithNoOptionsValuesModel" should {
//      ".getCurrentIncomeAnswer with a value of None will return None" in {
//        PersonalDetailsConstructor.getCurrentIncomeAnswer(summaryWithNoOptionValuesModel) shouldBe None
//      }
//
//      ".getPersonaAllowanceAnswer with a value of None will return None" in {
//        PersonalDetailsConstructor.getPersonalAllowanceAnswer(summaryWithNoOptionValuesModel) shouldBe None
//      }
//
//      ".getDisabledTrusteeAnswer with a value of None will return None" in {
//        PersonalDetailsConstructor.getDisabledTrusteesAnswer(summaryWithNoOptionValuesModel) shouldBe None
//      }
//
//      ".getAnnualExemptAmount with a value of None will return None" in {
//        PersonalDetailsConstructor.getAEAAnswer(summaryWithNoOptionValuesModel) shouldBe None
//      }
    }
  }
}

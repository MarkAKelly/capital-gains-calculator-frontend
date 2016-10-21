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

  val summaryWithNoOptionValuesModel = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.trustee),
    Some(DisabledTrusteeModel("Yes")),
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

  "Calling PersonalDetailsConstructor" when {

    "using the summaryWithAllOptionsValuesModel" should {

      //      ".getPurchaseDetailsItem will return a Sequence[DataItems]" in{
      //        PersonalDetailsConstructor.getPurchaseDetailsItem(summaryWithAllOptionValuesModel) shouldBe
      //          Seq(QuestionAnswerModel(common.KeystoreKeys.customerType, CustomerTypeKeys.individual))
      //      }

      ".getCustomerTypeAnswer with a customer type of individual will return an id of " +
        s"${KeystoreKeys.customerType}" in {
        PersonalDetailsConstructor.getCustomerTypeAnswer(summaryWithAllOptionValuesModel).id shouldBe
          KeystoreKeys.customerType
      }

      ".getCustomerTypeAnswer with a customer type of individual will return a question of " +
        s"${MessageLookup.NonResident.CustomerType.question}" in {
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

      ".getCurrentIncomeAnswer with an income of 30000.0 will return and id of " +
        s"${KeystoreKeys.currentIncome}" in {
        PersonalDetailsConstructor.getCurrentIncomeAnswer(summaryWithAllOptionValuesModel).get.id shouldBe
          KeystoreKeys.currentIncome
      }

      ".getCurrentIncomeAnswer with an income of 30000.0 will return a question of " +
        s"${MessageLookup.NonResident.CurrentIncome.question}" in {
        PersonalDetailsConstructor.getCurrentIncomeAnswer(summaryWithAllOptionValuesModel).get.question shouldBe
          MessageLookup.NonResident.CurrentIncome.question
      }

      ".getCurrentIncomeAnswer with an income of 30000.0 will return data of 30000.0" in {
        PersonalDetailsConstructor.getCurrentIncomeAnswer(summaryWithAllOptionValuesModel).get.data shouldBe 30000.0
      }

      ".getCurrentIncomeAnswer with an income of 30000.0 will return a link of " +
        s"${controllers.nonresident.routes.CurrentIncomeController.currentIncome().url}" in {
        PersonalDetailsConstructor.getCurrentIncomeAnswer(summaryWithAllOptionValuesModel).get.link shouldBe
          Some(controllers.nonresident.routes.CurrentIncomeController.currentIncome().url)
      }

      ".getPersonalAllowanceAnswer with an allowance of 11000.0 will return an id of " +
        s"${KeystoreKeys.personalAllowance}" in {
        PersonalDetailsConstructor.getPersonalAllowanceAnswer(summaryWithAllOptionValuesModel).get.id shouldBe
          KeystoreKeys.personalAllowance
      }

      ".getPersonalAllowanceAnswer with an allowance of 11000.0 will return data of 11000.0 " in {
        PersonalDetailsConstructor.getPersonalAllowanceAnswer(summaryWithAllOptionValuesModel).get.data shouldBe 11000.0
      }

      ".getPersonalAllowanceAnswer with an allowance of 11000.0 will return a question of " +
        s"${MessageLookup.NonResident.PersonalAllowance.question}" in {
        PersonalDetailsConstructor.getPersonalAllowanceAnswer(summaryWithAllOptionValuesModel).get.question shouldBe
          MessageLookup.NonResident.PersonalAllowance.question
      }

      ".getPersonalAllowanceAnswer with an allowance of 11000.0 will return an id of " +
        s"${controllers.nonresident.routes.PersonalAllowanceController.personalAllowance().url}" in {
        PersonalDetailsConstructor.getPersonalAllowanceAnswer(summaryWithAllOptionValuesModel).get.link shouldBe
          Some(controllers.nonresident.routes.PersonalAllowanceController.personalAllowance().url)
      }

      ".getDisabledTrusteeAnswer with a customer type of individual will return a None" in {
        PersonalDetailsConstructor.getDisabledTrusteeAnswer(summaryWithAllOptionValuesModel) shouldBe None
      }
    }

    "when using the summaryWithNoOptionsValuesModel" should {

      ".getCustomerTypeAnswer with a customer type of trustee will return data of " +
        s"${CustomerTypeKeys.trustee}" in {
        PersonalDetailsConstructor.getCustomerTypeAnswer(summaryWithNoOptionValuesModel).data shouldBe
          CustomerTypeKeys.trustee
      }

      ".getCurrentIncomeAnswer with a customer type of trustee will return a None" in {
        PersonalDetailsConstructor.getCurrentIncomeAnswer(summaryWithNoOptionValuesModel) shouldBe None
      }

      ".getPersonalAllowanceAnswer with a customer type of trustee will return a None" in {
        PersonalDetailsConstructor.getPersonalAllowanceAnswer(summaryWithNoOptionValuesModel) shouldBe None
      }

      ".getDisabledTrusteeAnswer with an answer of yes will return an id of " +
        s"${KeystoreKeys.disabledTrustee}" in {
        PersonalDetailsConstructor.getDisabledTrusteeAnswer(summaryWithNoOptionValuesModel).get.id shouldBe
          KeystoreKeys.disabledTrustee
      }

      ".getDisabledTrusteeAnswer with an answer of yes will return data of 11000.0 " in {
        PersonalDetailsConstructor.getDisabledTrusteeAnswer(summaryWithNoOptionValuesModel).get.data shouldBe "Yes"
      }

      ".getDisabledTrusteeAnswer with an answer of yes will return a question of " +
        s"${MessageLookup.NonResident.DisabledTrustee.question}" in {
        PersonalDetailsConstructor.getDisabledTrusteeAnswer(summaryWithNoOptionValuesModel).get.question shouldBe
          MessageLookup.NonResident.DisabledTrustee.question
      }

      ".getDisabledTrusteeAnswer with an answer of yes will return an id of " +
        s"${controllers.nonresident.routes.DisabledTrusteeController.disabledTrustee().url}" in {
        PersonalDetailsConstructor.getDisabledTrusteeAnswer(summaryWithNoOptionValuesModel).get.link shouldBe
          Some(controllers.nonresident.routes.DisabledTrusteeController.disabledTrustee().url)
      }
    }

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

//
//    "when using the summaryWithNoOptionsValuesModel" should {
//      ".getDisabledTrusteeAnswer with a value of None will return None" in {
//        PersonalDetailsConstructor.getDisabledTrusteesAnswer(summaryWithNoOptionValuesModel) shouldBe None
//      }
//
//      ".getAnnualExemptAmount with a value of None will return None" in {
//        PersonalDetailsConstructor.getAEAAnswer(summaryWithNoOptionValuesModel) shouldBe None
//      }
    }

}

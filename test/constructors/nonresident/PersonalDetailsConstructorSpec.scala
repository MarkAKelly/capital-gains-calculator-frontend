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

  val summaryWithTrusteeValuesModel = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.trustee),
    Some(DisabledTrusteeModel("Yes")),
    None,
    None,
    OtherPropertiesModel("Yes", Some(0)),
    Some(AnnualExemptAmountModel(10000.0)),
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

  val summaryNoOptionsIndividualModel = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    None,
    None,
    OtherPropertiesModel("Yes", None),
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

  val summaryNoOptionsTrusteeModel = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.trustee),
    None,
    None,
    None,
    OtherPropertiesModel("Yes", Some(0)),
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

      ".getPersonalDetailsItem will return a Sequence[QuestionAnswerModel[Any]] with size 5" in{
        PersonalDetailsConstructor.getPersonalDetailsSection(summaryWithAllOptionValuesModel).size shouldBe 5
      }

      ".getPersonalDetailsItem will return a Sequence[QuestionAnswerModel[Any]] will contain a PersonalDetails.getCustomerType item" in {
        PersonalDetailsConstructor.getPersonalDetailsSection(summaryWithAllOptionValuesModel)
          .count(_.equals(PersonalDetailsConstructor.getCustomerTypeAnswer(summaryWithAllOptionValuesModel).get)) shouldBe 1
      }

      ".getPersonalDetailsItem will return a Sequence[QuestionAnswerModel[Any]] will contain a PersonalDetails.getCurrentIncomeAnswer item" in {
        PersonalDetailsConstructor.getPersonalDetailsSection(summaryWithAllOptionValuesModel)
          .count(_.equals(PersonalDetailsConstructor.getCurrentIncomeAnswer(summaryWithAllOptionValuesModel).get)) shouldBe 1
      }

      ".getPersonalDetailsItem will return a Sequence[QuestionAnswerModel[Any]] will contain a PersonalDetails.getPersonalAllowanceAnswer item" in {
        PersonalDetailsConstructor.getPersonalDetailsSection(summaryWithAllOptionValuesModel)
          .count(_.equals(PersonalDetailsConstructor.getPersonalAllowanceAnswer(summaryWithAllOptionValuesModel).get)) shouldBe 1
      }

      ".getPersonalDetailsItem will return a Sequence[QuestionAnswerModel[Any]] will contain a PersonalDetails.getOtherPropertiesAnswer item" in {
        PersonalDetailsConstructor.getPersonalDetailsSection(summaryWithAllOptionValuesModel)
          .count(_.equals(PersonalDetailsConstructor.getOtherPropertiesAnswer(summaryWithAllOptionValuesModel).get)) shouldBe 1
      }

      ".getPersonalDetailsItem will return a Sequence[QuestionAnswerModel[Any]] will contain a PersonalDetails.getOtherPropertiesAmountAnswer item" in {
        PersonalDetailsConstructor.getPersonalDetailsSection(summaryWithAllOptionValuesModel)
          .count(_.equals(PersonalDetailsConstructor.getOtherPropertiesAmountAnswer(summaryWithAllOptionValuesModel).get)) shouldBe 1
      }

      ".getCustomerTypeAnswer with a customer type of individual will return an id of " +
        s"${KeystoreKeys.customerType}" in {
        PersonalDetailsConstructor.getCustomerTypeAnswer(summaryWithAllOptionValuesModel).get.id shouldBe
          KeystoreKeys.customerType
      }

      ".getCustomerTypeAnswer with a customer type of individual will return a question of " +
        s"${MessageLookup.NonResident.CustomerType.question}" in {
        PersonalDetailsConstructor.getCustomerTypeAnswer(summaryWithAllOptionValuesModel).get.question shouldBe
          MessageLookup.NonResident.CustomerType.question
      }

      ".getCustomerTypeAnswer with a customer type of individual will return data of " +
        s"${CustomerTypeKeys.individual}" in {
        PersonalDetailsConstructor.getCustomerTypeAnswer(summaryWithAllOptionValuesModel).get.data shouldBe
          CustomerTypeKeys.individual
      }

      ".getCustomerTypeAnswer with a customer type of individual will a link of " +
        s"${controllers.nonresident.routes.CustomerTypeController.customerType().url}" in {
        PersonalDetailsConstructor.getCustomerTypeAnswer(summaryWithAllOptionValuesModel).get.link shouldBe
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

      ".getOtherPropertiesAnswer with a value of Yes and 250000.0 should return Yes" in {
        PersonalDetailsConstructor.getOtherPropertiesAnswer(summaryWithAllOptionValuesModel).get.data shouldBe "Yes"
      }

      ".getOtherPropertiesAnswer with a value of Yes and 250000.0 should return an ID of" +
        s"${KeystoreKeys.otherProperties} " in {
        PersonalDetailsConstructor.getOtherPropertiesAnswer(summaryWithAllOptionValuesModel).get.id shouldBe
          KeystoreKeys.otherProperties
      }

      ".getOtherPropertiesAnswers with a value of yes and 250000.0 should return a URL of" +
        s"${controllers.nonresident.routes.OtherPropertiesController.otherProperties().url}" in {
        PersonalDetailsConstructor.getOtherPropertiesAnswer(summaryWithAllOptionValuesModel).get.link shouldBe
          Some(controllers.nonresident.routes.OtherPropertiesController.otherProperties().url)
      }

      ".getOtherPropertiesAnswers with a value of yes and 250000.0 should return a question of" +
        s"${MessageLookup.NonResident.OtherProperties.question} " in {
        PersonalDetailsConstructor.getOtherPropertiesAnswer(summaryWithAllOptionValuesModel).get.question shouldBe
          MessageLookup.NonResident.OtherProperties.question
      }

      ".getOtherPropertiesAmountAnswer with a value of Yes and 250000.0 should return Yes" in {
        PersonalDetailsConstructor.getOtherPropertiesAmountAnswer(summaryWithAllOptionValuesModel).get.data shouldBe 250000.0
      }

      ".getOtherPropertiesAmountAnswer with a value of Yes and 250000.0 should return an ID of" +
        s"${KeystoreKeys.otherProperties} + Amount" in {
        PersonalDetailsConstructor.getOtherPropertiesAmountAnswer(summaryWithAllOptionValuesModel).get.id shouldBe
          KeystoreKeys.otherProperties + "Amount"
      }

      ".getOtherPropertiesAmountAnswer with a value of yes and 250000.0 should return a URL of" +
        s"${controllers.nonresident.routes.OtherPropertiesController.otherProperties().url}" in {
        PersonalDetailsConstructor.getOtherPropertiesAmountAnswer(summaryWithAllOptionValuesModel).get.link shouldBe
          Some(controllers.nonresident.routes.OtherPropertiesController.otherProperties().url)
      }

      ".getOtherPropertiesAmountAnswer with a value of yes and 250000.0 should return a question of" +
        s"${MessageLookup.NonResident.OtherProperties.questionTwo} " in {
        PersonalDetailsConstructor.getOtherPropertiesAmountAnswer(summaryWithAllOptionValuesModel).get.question shouldBe
          MessageLookup.NonResident.OtherProperties.questionTwo
      }

      ".getAnnualExemptAmountAnswer with otherProperties is a yes and taxable gain is above 0 will return None" in {
        PersonalDetailsConstructor.getAnnualExemptAmountAnswer(summaryWithAllOptionValuesModel) shouldBe None
      }
    }

    "when using the summaryWithTrusteeValuesModel" should {

      ".getCustomerTypeAnswer with a customer type of trustee will return data of " +
        s"${CustomerTypeKeys.trustee}" in {
        PersonalDetailsConstructor.getCustomerTypeAnswer(summaryWithTrusteeValuesModel).get.data shouldBe
          CustomerTypeKeys.trustee
      }

      ".getCurrentIncomeAnswer with a customer type of trustee will return a None" in {
        PersonalDetailsConstructor.getCurrentIncomeAnswer(summaryWithTrusteeValuesModel) shouldBe None
      }

      ".getPersonalAllowanceAnswer with a customer type of trustee will return a None" in {
        PersonalDetailsConstructor.getPersonalAllowanceAnswer(summaryWithTrusteeValuesModel) shouldBe None
      }

      ".getDisabledTrusteeAnswer with an answer of yes will return an id of " +
        s"${KeystoreKeys.disabledTrustee}" in {
        PersonalDetailsConstructor.getDisabledTrusteeAnswer(summaryWithTrusteeValuesModel).get.id shouldBe
          KeystoreKeys.disabledTrustee
      }

      ".getDisabledTrusteeAnswer with an answer of yes will return data of 11000.0 " in {
        PersonalDetailsConstructor.getDisabledTrusteeAnswer(summaryWithTrusteeValuesModel).get.data shouldBe "Yes"
      }

      ".getDisabledTrusteeAnswer with an answer of yes will return a question of " +
        s"${MessageLookup.NonResident.DisabledTrustee.question}" in {
        PersonalDetailsConstructor.getDisabledTrusteeAnswer(summaryWithTrusteeValuesModel).get.question shouldBe
          MessageLookup.NonResident.DisabledTrustee.question
      }

      ".getDisabledTrusteeAnswer with an answer of yes will return an id of " +
        s"${controllers.nonresident.routes.DisabledTrusteeController.disabledTrustee().url}" in {
        PersonalDetailsConstructor.getDisabledTrusteeAnswer(summaryWithTrusteeValuesModel).get.link shouldBe
          Some(controllers.nonresident.routes.DisabledTrusteeController.disabledTrustee().url)
      }

      ".getOtherPropertiesAnswer with a customer type of trustee will return data of Yes" in {
        PersonalDetailsConstructor.getOtherPropertiesAnswer(summaryWithTrusteeValuesModel).get.data shouldBe "Yes"
      }

      ".getAnnualExemptAmountAnswer with otherProperties is a yes and and no taxable gain" should {

        s"return a valid id of ${KeystoreKeys.annualExemptAmount}" in {
          PersonalDetailsConstructor.getAnnualExemptAmountAnswer(summaryWithTrusteeValuesModel).get.id shouldBe
            KeystoreKeys.annualExemptAmount
        }

        s"return a valid data of 10000.0" in {
          PersonalDetailsConstructor.getAnnualExemptAmountAnswer(summaryWithTrusteeValuesModel).get.data shouldBe 10000.0
        }

        s"return a valid question of ${MessageLookup.NonResident.AnnualExemptAmount.question}" in {
          PersonalDetailsConstructor.getAnnualExemptAmountAnswer(summaryWithTrusteeValuesModel).get.question shouldBe
            MessageLookup.NonResident.AnnualExemptAmount.question
        }

        s"return a valid link of ${controllers.nonresident.routes.AnnualExemptAmountController.annualExemptAmount().url}" in {
          PersonalDetailsConstructor.getAnnualExemptAmountAnswer(summaryWithTrusteeValuesModel).get.link shouldBe
            Some(controllers.nonresident.routes.AnnualExemptAmountController.annualExemptAmount().url)
        }

      }
    }

    "when using the summaryNoOptionsIndividualModel" should {

      ".getCurrentIncomeAnswer with an individual but no CurrentIncomeAnswerModel will return None" in {
        PersonalDetailsConstructor.getCurrentIncomeAnswer(summaryNoOptionsIndividualModel) shouldBe None
      }

      ".getPersonalAllowanceAnswer with an individual but no PersonalAllowanceModel will return None" in {
        PersonalDetailsConstructor.getPersonalAllowanceAnswer(summaryNoOptionsIndividualModel) shouldBe None
      }

      ".getPersonalAllowanceAnswer with an trustee but no DisabledTrusteeModel will return None" in {
        PersonalDetailsConstructor.getDisabledTrusteeAnswer(summaryNoOptionsTrusteeModel) shouldBe None
      }

      ".getOtherPropertiesAmountAnswer with a OtherPropertiesModel but with no Amount value will return None" in {
        PersonalDetailsConstructor.getOtherPropertiesAmountAnswer(summaryNoOptionsIndividualModel) shouldBe None
      }

      ".getAnnualExemptAmountAnswer with a otherProperties but no taxable gain will return None" in {
        PersonalDetailsConstructor.getAnnualExemptAmountAnswer(summaryNoOptionsIndividualModel) shouldBe None
      }

    }
  }
}

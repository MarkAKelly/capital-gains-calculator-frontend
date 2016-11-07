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

import assets.MessageLookup.{NonResident => messages}
import controllers.nonresident.{routes => routes}
import common.KeystoreKeys
import common.TestModels._
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
    Some(PersonalAllowanceModel(0)),
    OtherPropertiesModel("Yes", Some(0)),
    Some(AnnualExemptAmountModel(0)),
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
    Some(AnnualExemptAmountModel(100)),
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

  val target = PersonalDetailsConstructor

  "calling .getPersonalDetailsSection" when {

    "using the summaryWithAllOptionsValuesModel" should {

      lazy val result = target.getPersonalDetailsSection(sumModelTA)

      "return a Sequence[QuestionAnswerModel[Any]] with size 5" in {
        result.size shouldBe 5
      }

      "return a CustomerType item" in {
        result.exists(qa => qa.id == KeystoreKeys.customerType) shouldBe true
      }

      "return a CurrentIncomeAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.currentIncome) shouldBe true
      }

      "return a PersonalAllowanceAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.personalAllowance) shouldBe true
      }

      "not return a DisabledTrusteeDataAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.disabledTrustee) shouldBe false
      }

      "return a OtherPropertiesAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.otherProperties) shouldBe true
      }

      "return a OtherPropertiesAmountAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.otherProperties + "Amount") shouldBe true
      }

      "not return a AnnualExemptAmountDataAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.annualExemptAmount) shouldBe false
      }
    }

    "using the summaryWithTrusteeValuesModel" should {

      lazy val result = target.getPersonalDetailsSection(summaryWithTrusteeValuesModel)

      "return a Sequence[QuestionAnswerModel[Any]] with size 5" in {
        result.size shouldBe 5
      }

      "return a CustomerType item" in {
        result.exists(qa => qa.id == KeystoreKeys.customerType) shouldBe true
      }

      "not return a CurrentIncomeAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.currentIncome) shouldBe false
      }

      "not return a PersonalAllowanceAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.personalAllowance) shouldBe false
      }

      "return a DisabledTrusteeDataAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.disabledTrustee) shouldBe true
      }

      "return a OtherPropertiesAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.otherProperties) shouldBe true
      }

      "return a OtherPropertiesAmountAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.otherProperties + "Amount") shouldBe true
      }

      "return a AnnualExemptAmountDataAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.annualExemptAmount) shouldBe true
      }
    }

    "using the summaryNoOptionsIndividualModel" should {

      lazy val result = target.getPersonalDetailsSection(summaryNoOptionsIndividualModel)

      "return a Sequence[QuestionAnswerModel[Any]] with size 5" in {
        result.size shouldBe 5
      }

      "return a CustomerType item" in {
        result.exists(qa => qa.id == KeystoreKeys.customerType) shouldBe true
      }

      "not return a CurrentIncomeAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.currentIncome) shouldBe false
      }

      "return a PersonalAllowanceAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.personalAllowance) shouldBe true
      }

      "not return a DisabledTrusteeDataAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.disabledTrustee) shouldBe false
      }

      "return a OtherPropertiesAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.otherProperties) shouldBe true
      }

      "return a OtherPropertiesAmountAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.otherProperties + "Amount") shouldBe true
      }

      "return a AnnualExemptAmountDataAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.annualExemptAmount) shouldBe true
      }
    }

    "using the summaryNoOptionsTrusteeModel" should {

      lazy val result = target.getPersonalDetailsSection(summaryWithTrusteeValuesModel)

      "return a Sequence[QuestionAnswerModel[Any]] with size 5" in {
        result.size shouldBe 5
      }

      "return a CustomerType item" in {
        result.exists(qa => qa.id == KeystoreKeys.customerType) shouldBe true
      }

      "not return a CurrentIncomeAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.currentIncome) shouldBe false
      }

      "not return a PersonalAllowanceAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.personalAllowance) shouldBe false
      }

      "return a DisabledTrusteeDataAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.disabledTrustee) shouldBe true
      }

      "return a OtherPropertiesAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.otherProperties) shouldBe true
      }

      "return a OtherPropertiesAmountAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.otherProperties + "Amount") shouldBe true
      }

      "return a AnnualExemptAmountDataAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.annualExemptAmount) shouldBe true
      }
    }
  }

  "calling .getCustomerTypeAnswer" when {

    "a customer type of individual" should {

      lazy val result = target.getCustomerTypeAnswer(summaryWithAllOptionValuesModel)

      "return some details for the CustomerType" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.customerType}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.customerType
        }
      }

      s"return a question of ${messages.CustomerType.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.CustomerType.question
        }
      }

      s"return data of ${CustomerTypeKeys.individual}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe CustomerTypeKeys.individual
        }
      }

      s"return a link of ${routes.CustomerTypeController.customerType().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.CustomerTypeController.customerType().url
          }
        }
      }
    }

    "a customer type of trustee" should {

      lazy val result = target.getCustomerTypeAnswer(summaryWithTrusteeValuesModel)

      "return some details for the CustomerType" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.customerType}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.customerType
        }
      }

      s"return a question of ${messages.CustomerType.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.CustomerType.question
        }
      }

      s"return data of ${CustomerTypeKeys.trustee}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe CustomerTypeKeys.trustee
        }
      }

      s"return a link of ${routes.CustomerTypeController.customerType().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.CustomerTypeController.customerType().url
          }
        }
      }
    }

    "a customer type of personal rep" should {

      lazy val result = target.getCustomerTypeAnswer(summaryRepresentativeFlatWithoutAEA)

      "return some details for the CustomerType" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.customerType}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.customerType
        }
      }

      s"return a question of ${messages.CustomerType.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.CustomerType.question
        }
      }

      s"return data of ${CustomerTypeKeys.personalRep}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe CustomerTypeKeys.personalRep
        }
      }

      s"return a link of ${routes.CustomerTypeController.customerType().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.CustomerTypeController.customerType().url
          }
        }
      }
    }
  }

  "calling .getCurrentIncomeAnswer" when {

    "a current income greater than 0" should {

      lazy val result = target.getCurrentIncomeAnswer(summaryWithAllOptionValuesModel)

      "return some details for the CurrentIncome" in {
        result should not be None
      }

      s"return and id of ${KeystoreKeys.currentIncome}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.currentIncome
        }
      }

      s"return a question of ${messages.CurrentIncome.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.CurrentIncome.question
        }
      }

      "return data of greater than 0" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe summaryWithAllOptionValuesModel.currentIncomeModel.get.currentIncome
        }
      }

      s"return a link of ${routes.CurrentIncomeController.currentIncome().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.CurrentIncomeController.currentIncome().url
          }
        }
      }
    }

    "a current income of 0.0" should {

      lazy val result = target.getCurrentIncomeAnswer(summaryIndividualFlatNoIncomeOtherPropNo)

      "return some details for the CurrentIncome" in {
        result should not be None
      }

      s"return and id of ${KeystoreKeys.currentIncome}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.currentIncome
        }
      }

      s"return a question of ${messages.CurrentIncome.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.CurrentIncome.question
        }
      }

      "return data of 0.0" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe summaryIndividualFlatNoIncomeOtherPropNo.currentIncomeModel.get.currentIncome
        }
      }

      s"return a link of ${routes.CurrentIncomeController.currentIncome().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.CurrentIncomeController.currentIncome().url
          }
        }
      }

    }

    "no current income is given" should {

      lazy val result = target.getCurrentIncomeAnswer(summaryWithTrusteeValuesModel)

      "return a None" in {
        result shouldBe None
      }
    }
  }

  "calling .getPersonalAllowanceAnswer" when {

    "a personal allowance of greater than 0 is given" should {

      lazy val result = target.getPersonalAllowanceAnswer(summaryWithAllOptionValuesModel)

      "return some details for the PersonalAllowance" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.personalAllowance}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.personalAllowance
        }
      }

      "return data of greater than 0 " in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe summaryWithAllOptionValuesModel.personalAllowanceModel.get.personalAllowanceAmt
        }
      }

      s"return a question of ${messages.PersonalAllowance.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.PersonalAllowance.question
        }
      }

      s"return a link of ${routes.PersonalAllowanceController.personalAllowance().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.PersonalAllowanceController.personalAllowance().url
          }
        }
      }
    }

    "a personal allowance of 0 is given" should {

      lazy val result = target.getPersonalAllowanceAnswer(summaryNoOptionsIndividualModel)

      "return some details for the PersonalAllowance" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.personalAllowance}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.personalAllowance
        }
      }

      "return data of 0.0 " in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe summaryNoOptionsIndividualModel.personalAllowanceModel.get.personalAllowanceAmt
        }
      }

      s"return a question of ${messages.PersonalAllowance.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.PersonalAllowance.question
        }
      }

      s"return a link of ${routes.PersonalAllowanceController.personalAllowance().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.PersonalAllowanceController.personalAllowance().url
          }
        }
      }
    }

    "no personal allowance is given" should {

      lazy val result = target.getPersonalAllowanceAnswer(summaryWithTrusteeValuesModel)

      "return a None" in {
        result shouldBe None
      }
    }
  }

  "calling .getDisabledTrusteeAnswer" when {

    "no disabled trustee is given" should {

      lazy val result = target.getDisabledTrusteeAnswer(summaryWithAllOptionValuesModel)

      "return a None" in {
        result shouldBe None
      }
    }

    "a disabled trustee with Yes is supplied" should {

      lazy val result = target.getDisabledTrusteeAnswer(summaryWithTrusteeValuesModel)

      "return some details for the DisabledTrustee" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.disabledTrustee}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.disabledTrustee
        }
      }

      s"return data of ${messages.yes}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe messages.yes
        }
      }

      s"return a question of ${messages.DisabledTrustee.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.DisabledTrustee.question
        }
      }

      s"return an id of ${routes.DisabledTrusteeController.disabledTrustee().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.DisabledTrusteeController.disabledTrustee().url
          }
        }
      }
    }

    "a disabled trustee with No is supplied" should {

      lazy val result = target.getDisabledTrusteeAnswer(summaryTrusteeTAWithAEA)

      "return some details for the DisabledTrustee" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.disabledTrustee}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.disabledTrustee
        }
      }

      s"return data of ${messages.no}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe messages.no
        }
      }

      s"return a question of ${messages.DisabledTrustee.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.DisabledTrustee.question
        }
      }

      s"return an id of ${routes.DisabledTrusteeController.disabledTrustee().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.DisabledTrusteeController.disabledTrustee().url
          }
        }
      }
    }
  }

  "calling .getOtherPropertiesAnswer" when {

    "a otherPropertiesAnswer of yes is given" should {

      lazy val result = PersonalDetailsConstructor.getOtherPropertiesAnswer(summaryWithAllOptionValuesModel)

      "return some details for the OtherProperties" in {
        result should not be None
      }

      s"return ${messages.yes}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe messages.yes
        }
      }

      s"return an ID of ${KeystoreKeys.otherProperties}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.otherProperties
        }
      }

      s"return a question of ${messages.OtherProperties.question} " in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.OtherProperties.question
        }
      }

      s"return a link of ${routes.OtherPropertiesController.otherProperties().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.OtherPropertiesController.otherProperties().url
          }
        }
      }
    }

    "a otherPropertiesAnswer of no is given" should {

      lazy val result = PersonalDetailsConstructor.getOtherPropertiesAnswer(summaryRepresentativeFlatWithoutAEA)

      "return some details for the OtherProperties" in {
        result should not be None
      }

      s"return data of ${messages.no}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe messages.no
        }
      }

      s"return an ID of ${KeystoreKeys.otherProperties}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.otherProperties
        }
      }

      s"return a question of ${messages.OtherProperties.question} " in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.OtherProperties.question
        }
      }

      s"return a link of ${routes.OtherPropertiesController.otherProperties().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.OtherPropertiesController.otherProperties().url
          }
        }
      }
    }
  }

  "calling .getOtherPropertiesAmountAnswer" when {

    "an otherPropertiesAmount of greater than 0 is given" should {

      lazy val result = PersonalDetailsConstructor.getOtherPropertiesAmountAnswer(summaryWithAllOptionValuesModel)

      "return some details for the OtherPropertiesAmount" in {
        result should not be None
      }

      "return greater than 0" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe summaryWithAllOptionValuesModel.otherPropertiesModel.otherPropertiesAmt.get
        }
      }

      s"return an ID of ${KeystoreKeys.otherProperties} + Amount" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.otherProperties + "Amount"
        }
      }

      s"return a question of ${messages.OtherProperties.questionTwo} " in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.OtherProperties.questionTwo
        }
      }

      s"return a URL of ${routes.OtherPropertiesController.otherProperties().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.OtherPropertiesController.otherProperties().url
          }
        }
      }
    }

    "an otherPropertiesAmount of 0.0 is given" should {

      lazy val result = target.getOtherPropertiesAmountAnswer(summaryWithTrusteeValuesModel)

      "return some details for the OtherPropertiesAmount" in {
        result should not be None
      }

      "return 0.0" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe summaryWithTrusteeValuesModel.otherPropertiesModel.otherPropertiesAmt.get
        }
      }

      s"return an ID of ${KeystoreKeys.otherProperties} + Amount" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.otherProperties + "Amount"
        }
      }

      s"return a question of $messages.OtherProperties.questionTwo} " in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.OtherProperties.questionTwo
        }
      }

      s"return a URL of ${routes.OtherPropertiesController.otherProperties().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.OtherPropertiesController.otherProperties().url
          }
        }
      }

    }

    "no answer for otherPropertiesAmount is given" should {

      lazy val result = target.getOtherPropertiesAmountAnswer(summaryOtherReliefsFlatYesNoValue)

      "return a None" in {
        result shouldBe None
      }
    }
  }

  "calling .getAnnualExemptAmountAnswer" when {

    "no AnnualExemptAmount is given" should {

      lazy val result = target.getAnnualExemptAmountAnswer(summaryWithAllOptionValuesModel)

      "return a None" in {
        result shouldBe None
      }
    }

    "an AnnualExemptAmount of greater than 0 is given" should {

      lazy val result = target.getAnnualExemptAmountAnswer(summaryWithTrusteeValuesModel)

      "return some details for the AnnualExemptAmount" in {
        result should not be None
      }

      s"return a valid id of ${KeystoreKeys.annualExemptAmount}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.annualExemptAmount
        }
      }

      "return a valid data that is greater than 0" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe summaryWithTrusteeValuesModel.annualExemptAmountModel.get.annualExemptAmount
        }
      }

      s"return a valid question of ${messages.AnnualExemptAmount.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.AnnualExemptAmount.question
        }
      }

      s"return a valid link of ${routes.AnnualExemptAmountController.annualExemptAmount().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.AnnualExemptAmountController.annualExemptAmount().url
          }
        }
      }
    }

    "an AnnualExemptAmount of 0.0 is given" should {

      lazy val result = target.getAnnualExemptAmountAnswer(summaryNoOptionsIndividualModel)

      "return some details for the AnnualExemptAmount" in {
        result should not be None
      }

      s"return a valid id of ${KeystoreKeys.annualExemptAmount}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.annualExemptAmount
        }
      }

      "return a valid data of 0.0" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe summaryNoOptionsIndividualModel.annualExemptAmountModel.get.annualExemptAmount
        }
      }

      s"return a valid question of ${messages.AnnualExemptAmount.question}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.AnnualExemptAmount.question
        }
      }

      s"return a valid link of ${routes.AnnualExemptAmountController.annualExemptAmount().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.AnnualExemptAmountController.annualExemptAmount().url
          }
        }
      }

    }
  }
}

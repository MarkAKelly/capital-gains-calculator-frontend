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

  val target = PersonalDetailsConstructor

  "calling .getPersonalDetailsSection" when {

    "using the summaryWithAllOptionsValuesModel" should {

      lazy val result = target.getPersonalDetailsSection(summaryWithAllOptionValuesModel)

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

      "return a DisabledTrusteeDataAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.disabledTrustee) shouldBe false
      }

      "return a OtherPropertiesAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.otherProperties) shouldBe true
      }

      "return a OtherPropertiesAmountAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.otherProperties + "Amount") shouldBe true
      }

      "return a AnnualExemptAmountDataAnswer item" in {
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

      "return a CurrentIncomeAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.currentIncome) shouldBe false
      }

      "return a PersonalAllowanceAnswer item" in {
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
        result.size shouldBe 2
      }

      "return a CustomerType item" in {
        result.exists(qa => qa.id == KeystoreKeys.customerType) shouldBe true
      }

      "return a CurrentIncomeAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.currentIncome) shouldBe false
      }

      "return a PersonalAllowanceAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.personalAllowance) shouldBe false
      }

      "return a DisabledTrusteeDataAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.disabledTrustee) shouldBe false
      }

      "return a OtherPropertiesAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.otherProperties) shouldBe true
      }

      "return a OtherPropertiesAmountAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.otherProperties + "Amount") shouldBe false
      }

      "return a AnnualExemptAmountDataAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.annualExemptAmount) shouldBe false
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

      "return a CurrentIncomeAnswer item" in {
        result.exists(qa => qa.id == KeystoreKeys.currentIncome) shouldBe false
      }

      "return a PersonalAllowanceAnswer item" in {
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

    "using the summaryWithAllOptionsValuesModel" should {

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

      s"return a link of ${controllers.nonresident.routes.CustomerTypeController.customerType().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.CustomerTypeController.customerType().url
          }
        }
      }
    }

    "using the summaryWithTrusteeValuesModel" should {

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

      s"return a link of ${controllers.nonresident.routes.CustomerTypeController.customerType().url}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.CustomerTypeController.customerType().url
          }
        }
      }
    }

  }

  "calling .getCurrentIncomeAnswer" when {

    "using the summaryWithAllOptionsValuesModel" should {

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

      "return data of 30000.0" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 30000.0
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

    "using the summaryWithTrusteeValuesModel" should {

      lazy val result = target.getCurrentIncomeAnswer(summaryWithTrusteeValuesModel)

      "return a None" in {
        result shouldBe None
      }
    }

    "using the summaryNoOptionsTrusteeModel" should {

      lazy val result = target.getCurrentIncomeAnswer(summaryNoOptionsTrusteeModel)

      "return a None" in {
        result shouldBe None
      }
    }

    "using the summaryNoOptionsIndividualModel" should {

      lazy val result = target.getCurrentIncomeAnswer(summaryNoOptionsIndividualModel)

      "return None" in {
        result shouldBe None
      }
    }


  }

  "calling .getPersonalAllowanceAnswer" when {

    "using the summaryWithAllOptionValuesModel" should {

      lazy val result = target.getPersonalAllowanceAnswer(summaryWithAllOptionValuesModel)

      "return some details for the PersonalAllowance" in {
        result should not be None
      }

      s"return an id of ${KeystoreKeys.personalAllowance}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.personalAllowance
        }
      }

      "return data of 11000.0 " in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 11000.0
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

    "using the summaryWithTrusteeValuesModel" should {

      lazy val result = target.getPersonalAllowanceAnswer(summaryWithTrusteeValuesModel)

      "return a None" in {
        result shouldBe None
      }
    }

    "using the summaryNoOptionsIndividualModel" should {

      lazy val result = target.getPersonalAllowanceAnswer(summaryWithTrusteeValuesModel)

      "return a None" in {
        result shouldBe None
      }
    }
  }

  "calling .getDisabledTrusteeAnswer" when {

    "using the summaryWithAllOptionsValuesModel" should {

      lazy val result = target.getDisabledTrusteeAnswer(summaryWithAllOptionValuesModel)

      "return a None" in {
        result shouldBe None
      }
    }

    "using the summaryWithTrusteeValuesModel" should {

      lazy val result = target.getDisabledTrusteeAnswer(summaryWithTrusteeValuesModel)

      s"return an id of ${KeystoreKeys.disabledTrustee}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.disabledTrustee
        }
      }

      "return data of 11000.0 " in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe "Yes"
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

    "using the summaryWithAllOptionsValuesModel" should {

      lazy val result = PersonalDetailsConstructor.getOtherPropertiesAnswer(summaryWithAllOptionValuesModel)

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

    "using the summaryWithTrusteeValuesModel" should {

      lazy val result = PersonalDetailsConstructor.getOtherPropertiesAnswer(summaryWithTrusteeValuesModel)

      s"return data of ${messages.yes}" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe messages.yes
        }
      }
    }
  }

  "calling .getOtherPropertiesAmountAnswer" when {

    "using the summaryWithAllOptionValuesModel" should {

      lazy val result = PersonalDetailsConstructor.getOtherPropertiesAmountAnswer(summaryWithAllOptionValuesModel)

      "return 250000.0" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe 250000.0
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
            link shouldBe controllers.nonresident.routes.OtherPropertiesController.otherProperties().url
          }
        }
      }
    }
  }
}



//
//    ".getAnnualExemptAmountAnswer with otherProperties is a yes and taxable gain is above 0 will return None" in {
//      PersonalDetailsConstructor.getAnnualExemptAmountAnswer(summaryWithAllOptionValuesModel) shouldBe None
//    }
//  }
//
//  "when using the summaryWithTrusteeValuesModel" should {
//
//
//    ".getAnnualExemptAmountAnswer with otherProperties is a yes and and no taxable gain" should {
//
//      s"return a valid id of ${KeystoreKeys.annualExemptAmount}" in {
//        PersonalDetailsConstructor.getAnnualExemptAmountAnswer(summaryWithTrusteeValuesModel).get.id shouldBe
//          KeystoreKeys.annualExemptAmount
//      }
//
//      s"return a valid data of 10000.0" in {
//        PersonalDetailsConstructor.getAnnualExemptAmountAnswer(summaryWithTrusteeValuesModel).get.data shouldBe 10000.0
//      }
//
//      s"return a valid question of ${MessageLookup.NonResident.AnnualExemptAmount.question}" in {
//        PersonalDetailsConstructor.getAnnualExemptAmountAnswer(summaryWithTrusteeValuesModel).get.question shouldBe
//          MessageLookup.NonResident.AnnualExemptAmount.question
//      }
//
//      s"return a valid link of ${controllers.nonresident.routes.AnnualExemptAmountController.annualExemptAmount().url}" in {
//        PersonalDetailsConstructor.getAnnualExemptAmountAnswer(summaryWithTrusteeValuesModel).get.link shouldBe
//          Some(controllers.nonresident.routes.AnnualExemptAmountController.annualExemptAmount().url)
//      }
//
//    }
//  }
//
//  "when using the summaryNoOptionsIndividualModel" should {
//

//
//    ".getPersonalAllowanceAnswer with an individual but no PersonalAllowanceModel will return None" in {
//      PersonalDetailsConstructor.getPersonalAllowanceAnswer(summaryNoOptionsIndividualModel) shouldBe None
//    }
//
//    ".getPersonalAllowanceAnswer with an trustee but no DisabledTrusteeModel will return None" in {
//      PersonalDetailsConstructor.getDisabledTrusteeAnswer(summaryNoOptionsTrusteeModel) shouldBe None
//    }
//
//    ".getOtherPropertiesAmountAnswer with a OtherPropertiesModel but with no Amount value will return None" in {
//      PersonalDetailsConstructor.getOtherPropertiesAmountAnswer(summaryNoOptionsIndividualModel) shouldBe None
//    }
//
//    ".getAnnualExemptAmountAnswer with a otherProperties but no taxable gain will return None" in {
//      PersonalDetailsConstructor.getAnnualExemptAmountAnswer(summaryNoOptionsIndividualModel) shouldBe None
//    }
//
//  }
//
//}
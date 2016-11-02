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

import common.{KeystoreKeys, TestModels}
import assets.MessageLookup.NonResident.{Summary => messages}
import controllers.nonresident.routes
import models.nonresident.CalculationResultModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class CalculationDetailsConstructorSpec extends UnitSpec with WithFakeApplication {

  val target = CalculationDetailsConstructor

  "Calling buildSection" when {

    "a loss has been made" should {
      val calculation = TestModels.calcModelLoss
      val answers = TestModels.summaryIndividualImprovementsWithRebasedModel
      lazy val result = target.buildSection(calculation, answers)

      "have a calc election question" in {
        result.exists(qa => qa.id == KeystoreKeys.calculationElection) shouldBe true
      }

      "not have a gain question" in {
        result.exists(qa => qa.id == "calcDetails:totalGain") shouldBe false
      }

      "have a loss question" in {
        result.exists(qa => qa.id == "calcDetails:totalLoss") shouldBe true
      }

      "not have an AEA question" in {
        result.exists(qa => qa.id == "calcDetails:aea") shouldBe false
      }

      "not have a taxable gain question" in {
        result.exists(qa => qa.id == "calcDetails:taxableGain") shouldBe false
      }

      "not have a taxable rate question" in {
        result.exists(qa => qa.id == "calcDetails:taxableRate") shouldBe false
      }

      "not have a loss to carry forward question" in {
        result.exists(qa => qa.id == "calcDetails:lossToCarryForward") shouldBe false
      }
    }

    "a gain has been made" should {
      val calculation = TestModels.calcModelOneRate
      val answers = TestModels.summaryIndividualImprovementsWithRebasedModel
      lazy val result = target.buildSection(calculation, answers)

      "have a calc election question" in {
        result.exists(qa => qa.id == KeystoreKeys.calculationElection) shouldBe true
      }

      "have a gain question" in {
        result.exists(qa => qa.id == "calcDetails:totalGain") shouldBe true
      }

      "not have a loss question" in {
        result.exists(qa => qa.id == "calcDetails:totalLoss") shouldBe false
      }

      "have an AEA question" in {
        result.exists(qa => qa.id == "calcDetails:aea") shouldBe true
      }

      "have a taxable gain question" in {
        result.exists(qa => qa.id == "calcDetails:taxableGain") shouldBe true
      }

      "have a taxable rate question" in {
        result.exists(qa => qa.id == "calcDetails:taxableRate") shouldBe true
      }

      "not have a loss to carry forward question" in {
        result.exists(qa => qa.id == "calcDetails:lossToCarryForward") shouldBe false
      }
    }

    "a zero gain has been made" should {
      val calculation = TestModels.calcModelZeroTotal
      val answers = TestModels.summaryIndividualImprovementsWithRebasedModel
      lazy val result = target.buildSection(calculation, answers)

      "have a calc election question" in {
        result.exists(qa => qa.id == KeystoreKeys.calculationElection) shouldBe true
      }

      "have a gain question" in {
        result.exists(qa => qa.id == "calcDetails:totalGain") shouldBe true
      }

      "not have a loss question" in {
        result.exists(qa => qa.id == "calcDetails:totalLoss") shouldBe false
      }

      "not have an AEA question" in {
        result.exists(qa => qa.id == "calcDetails:aea") shouldBe false
      }

      "not have a taxable gain question" in {
        result.exists(qa => qa.id == "calcDetails:taxableGain") shouldBe false
      }

      "not have a taxable rate question" in {
        result.exists(qa => qa.id == "calcDetails:taxableRate") shouldBe false
      }

      "not have a loss to carry forward question" in {
        result.exists(qa => qa.id == "calcDetails:lossToCarryForward") shouldBe false
      }
    }

    "a negative taxable gain has been made" should {
      val calculation = TestModels.calcModelNegativeTaxable
      val answers = TestModels.summaryIndividualImprovementsWithRebasedModel
      lazy val result = target.buildSection(calculation, answers)

      "have a calc election question" in {
        result.exists(qa => qa.id == KeystoreKeys.calculationElection) shouldBe true
      }

      "have a gain question" in {
        result.exists(qa => qa.id == "calcDetails:totalGain") shouldBe true
      }

      "not have a loss question" in {
        result.exists(qa => qa.id == "calcDetails:totalLoss") shouldBe false
      }

      "have an AEA question" in {
        result.exists(qa => qa.id == "calcDetails:aea") shouldBe true
      }

      "have a taxable gain question" in {
        result.exists(qa => qa.id == "calcDetails:taxableGain") shouldBe true
      }

      "not have a taxable rate question" in {
        result.exists(qa => qa.id == "calcDetails:taxableRate") shouldBe false
      }

      "have a loss to carry forward question" in {
        result.exists(qa => qa.id == "calcDetails:lossToCarryForward") shouldBe true
      }
    }

  }

  "Calling calculationElection" when {

    "the calculation type is a flat calc" should {
      val model = TestModels.sumModelFlat
      lazy val result = target.calculationElection(model)

      "return some details for the calculation election" in {
        result should not be None
      }

      "return correct ID for the calculation election details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.calculationElection
        }
      }

      "return correct question for the calculation election details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.calculationElection
        }
      }

      "return correct answer for the calculation election details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe messages.flatCalculation
        }
      }

      "return a link for the calculation election details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link should not be None
        }
      }

      "return correct link for the calculation election details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.CalculationElectionController.calculationElection().url
          }
        }
      }
    }

    "the calculation type is a rebased calc" should {
      val model = TestModels.sumModelRebased
      lazy val result = target.calculationElection(model)

      "return some details for the calculation election" in {
        result should not be None
      }

      "return correct ID for the calculation election details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.calculationElection
        }
      }

      "return correct question for the calculation election details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.calculationElection
        }
      }

      "return correct answer for the calculation election details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe messages.rebasedCalculation
        }
      }

      "return a link for the calculation election details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link should not be None
        }
      }

      "return correct link for the calculation election details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.CalculationElectionController.calculationElection().url
          }
        }
      }
    }

    "the calculation type is a time apportioned calc" should {
      val model = TestModels.sumModelTA
      lazy val result = target.calculationElection(model)

      "return some details for the calculation election" in {
        result should not be None
      }

      "return correct ID for the calculation election details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe KeystoreKeys.calculationElection
        }
      }

      "return correct question for the calculation election details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.calculationElection
        }
      }

      "return correct answer for the calculation election details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe messages.timeCalculation
        }
      }

      "return a link for the calculation election details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link should not be None
        }
      }

      "return correct link for the calculation election details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link.fold(cancel("link not supplied when expected")) { link =>
            link shouldBe routes.CalculationElectionController.calculationElection().url
          }
        }
      }
    }

  }

  "Calling totalGain" when {

    "the gain is zero" should {

      val model = TestModels.calcModelZeroTotal
      lazy val result = target.totalGain(model)

      "return some total gain details" in {
        result should not be None
      }

      "return correct ID for the total gain details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe "calcDetails:totalGain"
        }
      }

      "return correct question for the total gain details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.totalGain
        }
      }

      "return correct answer for the total gain details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe model.totalGain
        }
      }

      "not return a link for the total gain details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe None
        }
      }
    }

    "the gain is greater than zero" should {

      val model = TestModels.calcModelOneRate
      lazy val result = target.totalGain(model)

      "return some total gain details" in {
        result should not be None
      }

      "return correct ID for the total gain details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe "calcDetails:totalGain"
        }
      }

      "return correct question for the total gain details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.totalGain
        }
      }

      "return correct answer for the total gain details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe model.totalGain
        }
      }

      "not return a link for the total gain details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe None
        }
      }
    }

    "the total gain is less than zero" should {

      val model = TestModels.calcModelLoss
      lazy val result = target.totalGain(model)

      "return no total gain details" in {
        result shouldBe None
      }
    }
  }

  "Calling totalLoss" when {

    "the gain is zero" should {

      val model = TestModels.calcModelZeroTotal
      lazy val result = target.totalLoss(model)

      "return no total loss details" in {
        result shouldBe None
      }
    }

    "the gain is greater than zero" should {

      val model = TestModels.calcModelOneRate
      lazy val result = target.totalLoss(model)

      "return no total loss details" in {
        result shouldBe None
      }
    }

    "the total gain is less than zero" should {

      val model = TestModels.calcModelLoss
      lazy val result = target.totalLoss(model)

      "return some total loss details" in {
        result should not be None
      }

      "return correct ID for the total loss details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe "calcDetails:totalLoss"
        }
      }

      "return correct question for the total loss details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.totalLoss
        }
      }

      "return correct answer for the total loss details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe model.totalGain.abs
        }
      }

      "not return a link for the total loss details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe None
        }
      }
    }

  }

  "Calling usedAEA" when {

    "the total gain is greater than zero" should {
      val model = TestModels.calcModelOneRate
      lazy val result = target.usedAea(model)

      "return some AEA details" in {
        result should not be None
      }

      "return correct ID for the AEA details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe "calcDetails:aea"
        }
      }

      "return correct question for the AEA details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.usedAEA
        }
      }

      "return correct answer for the AEA details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe model.usedAnnualExemptAmount
        }
      }

      "not return a link for the AEA details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe None
        }
      }
    }

    "the total gain is less than zero" should {
      val model = TestModels.calcModelLoss
      lazy val result = target.usedAea(model)

      "return some AEA details" in {
        result shouldBe None
      }
    }

    "the total gain is zero" should {
      val model = TestModels.calcModelZeroTotal
      lazy val result = target.usedAea(model)

      "return some AEA details" in {
        result shouldBe None
      }
    }
  }

  "Calling taxableGain" when {

    "the total gain is greater than zero" should {
      val model = TestModels.calcModelOneRate
      lazy val result = target.taxableGain(model)

      "return some taxable gain details" in {
        result should not be None
      }

      "return correct ID for the taxable gain details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe "calcDetails:taxableGain"
        }
      }

      "return correct question for the taxable gain details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.taxableGain
        }
      }

      "return correct answer for the taxable gain details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe model.taxableGain
        }
      }

      "not return a link for the taxable gain details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe None
        }
      }
    }

    "the total gain is less than zero" should {
      val model = TestModels.calcModelLoss
      lazy val result = target.taxableGain(model)

      "return some taxable gain details" in {
        result shouldBe None
      }
    }

    "the total gain is zero" should {
      val model = TestModels.calcModelZeroTotal
      lazy val result = target.taxableGain(model)

      "return some taxable gain details" in {
        result shouldBe None
      }
    }
  }

  "Calling taxableRate" when {

    "base gain is zero and upper gain is not present" should {
      val model = TestModels.calcModelLoss
      lazy val result = target.taxableRate(model)

      "return no taxable rate details" in {
        result shouldBe None
      }
    }

    "base gain is greater than zero and upper gain is not present" should {
      val model = CalculationResultModel(
        taxOwed = 8000,
        totalGain = 40000,
        baseTaxGain = 32000,
        baseTaxRate = 20,
        usedAnnualExemptAmount = 8000,
        upperTaxGain = None,
        upperTaxRate = None,
        simplePRR = None)
      lazy val result = target.taxableRate(model)

      "return some taxable gain details" in {
        result should not be None
      }

      "return correct ID for the taxable rate details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe "calcDetails:taxableRate"
        }
      }

      "return correct question for the taxable rate details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.taxRate
        }
      }

      "return correct answer for the taxable rate details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe "20%"
        }
      }

      "not return a link for the taxable rate details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe None
        }
      }
    }

    "base gain is zero and upper gain has a value" should {
      val model = CalculationResultModel(
        taxOwed = 8000,
        totalGain = 40000,
        baseTaxGain = 0,
        baseTaxRate = 0,
        usedAnnualExemptAmount = 8000,
        upperTaxGain = Some(32000),
        upperTaxRate = Some(50),
        simplePRR = None)
      lazy val result = target.taxableRate(model)

      "return some taxable gain details" in {
        result should not be None
      }

      "return correct ID for the taxable rate details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe "calcDetails:taxableRate"
        }
      }

      "return correct question for the taxable rate details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.taxRate
        }
      }

      "return correct answer for the taxable rate details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe "50%"
        }
      }

      "not return a link for the taxable rate details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe None
        }
      }
    }


    "base gain is greater than zero and upper gain has a value" should {
      val model = CalculationResultModel(
        taxOwed = 8000,
        totalGain = 40000,
        baseTaxGain = 10000,
        baseTaxRate = 18,
        usedAnnualExemptAmount = 8000,
        upperTaxGain = Some(32000),
        upperTaxRate = Some(50),
        simplePRR = None)
      lazy val result = target.taxableRate(model)

      "return some taxable gain details" in {
        result should not be None
      }

      "return correct ID for the taxable rate details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe "calcDetails:taxableRate"
        }
      }

      "return correct question for the taxable rate details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.taxRate
        }
      }

      "return correct answer for the taxable rate details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe "£10,000 at 18%\n£32,000 at 50%"
        }
      }

      "not return a link for the taxable rate details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe None
        }
      }
    }
  }

  "Calling lossToCarryForward" when {

    "the taxable gain is zero" should {

      val model = TestModels.calcModelZeroTotal
      lazy val result = target.lossToCarryForward(model)

      "return no loss to carry forward details" in {
        result shouldBe None
      }
    }

    "the taxable gain is greater than zero" should {

      val model = TestModels.calcModelOneRate
      lazy val result = target.lossToCarryForward(model)

      "return no loss to carry forward details" in {
        result shouldBe None
      }
    }

    "the taxable gain is less than zero" should {

      val model = CalculationResultModel(
        taxOwed = 0,
        totalGain = 0,
        baseTaxGain = -55,
        baseTaxRate = 20,
        usedAnnualExemptAmount = 0,
        upperTaxGain = Some(BigDecimal(-55)),
        upperTaxRate = Some(50),
        simplePRR = None)
      lazy val result = target.lossToCarryForward(model)

      "return some loss to carry forward details" in {
        result should not be None
      }

      "return correct ID for the loss to carry forward details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.id shouldBe "calcDetails:lossToCarryForward"
        }
      }

      "return correct question for the loss to carry forward details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.question shouldBe messages.lossesCarriedForward
        }
      }

      "return correct answer for the loss to carry forward details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.data shouldBe model.taxableGain.abs
        }
      }

      "not return a link for the loss to carry forward details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe None
        }
      }
    }

  }
}

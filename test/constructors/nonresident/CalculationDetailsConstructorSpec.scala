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
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class CalculationDetailsConstructorSpec extends UnitSpec with WithFakeApplication {

  val target = CalculationDetailsConstructor

  "Calling calculationElection" when {

    "the calculation type is a flat calc" should {
      lazy val model = TestModels.sumModelFlat
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
      lazy val model = TestModels.sumModelRebased
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
      lazy val model = TestModels.sumModelTA
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

      lazy val model = TestModels.calcModelZeroTotal
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

      "return a link for the total gain details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe None
        }
      }
    }

    "the gain is greater than zero" should {

      lazy val model = TestModels.calcModelOneRate
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

      "return a link for the total gain details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe None
        }
      }
    }

    "the total gain is less than zero" should {

      lazy val model = TestModels.calcModelLoss
      lazy val result = target.totalGain(model)

      "return no total gain details" in {
        result shouldBe None
      }
    }
  }

  "Calling totalLoss" when {

    "the gain is zero" should {

      lazy val model = TestModels.calcModelZeroTotal
      lazy val result = target.totalLoss(model)

      "return no total loss details" in {
        result shouldBe None
      }
    }

    "the gain is greater than zero" should {

      lazy val model = TestModels.calcModelOneRate
      lazy val result = target.totalLoss(model)

      "return no total loss details" in {
        result shouldBe None
      }
    }

    "the total gain is less than zero" should {

      lazy val model = TestModels.calcModelLoss
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

      "return a link for the total loss details" in {
        result.fold(cancel("expected result not computed")) { item =>
          item.link shouldBe None
        }
      }
    }

  }
//
//  "Calling usedAEA" when {
//
//    /*
//          if isGreaterThanZero(totalGain) && isGreaterThanZero(taxableGain)
//          None
//
//          if !isPositive(taxableGain)
//          None
//
//          if !isPositive(totalGain)
//          None
//
//          if isGreaterThanZero(totalGain) && isPositive(taxableGain) && !isGreaterThanZero(taxableGain)
//          None
//
//    */
//
//  }
//
//
//  "Calling taxableGain" when {
//
//    /*
//          if isGreaterThanZero(totalGain) && isGreaterThanZero(taxableGain)
//          None
//
//          if !isPositive(taxableGain)
//          None
//
//          if !isPositive(totalGain)
//          None
//
//          if isGreaterThanZero(totalGain) && isPositive(taxableGain) && !isGreaterThanZero(taxableGain)
//          None
//
//    */
//
//  }
//
//  "Calling taxableRate" when {
//
//    /*
//          if isGreaterThanZero(totalGain) && isGreaterThanZero(taxableGain)
//          None
//
//          if !isPositive(taxableGain)
//          None
//
//          if !isPositive(totalGain)
//          None
//
//          if isGreaterThanZero(totalGain) && isPositive(taxableGain) && !isGreaterThanZero(taxableGain)
//          None
//
//    */
//
//  }
//
//  "Calling lossCarriedForward" when {
//
//    /*
//          if isGreaterThanZero(totalGain) && isGreaterThanZero(taxableGain)
//          None
//
//          if !isPositive(taxableGain)
//          None
//
//          if !isPositive(totalGain)
//          None
//
//          if isGreaterThanZero(totalGain) && isPositive(taxableGain) && !isGreaterThanZero(taxableGain)
//          None
//
//    */
//
//  }
}

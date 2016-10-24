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
}

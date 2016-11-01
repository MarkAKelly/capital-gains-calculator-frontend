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

package forms.nonResident

import assets.MessageLookup.{NonResident => messages}
import forms.nonresident.ImprovementsForm._
import models.nonresident.ImprovementsModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class ImprovementsFormSpec extends UnitSpec with WithFakeApplication{

  "Creating a form" when {

    "passing a in a valid model" should {
      val model = ImprovementsModel(messages.yes, Some(1000.0), Some(1000.0))
      lazy val form = improvementsForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "1000.0", "improvementsAmtAfter" -> "1000.0")
      }
    }

    "passing a in a valid yes map" should {
      val map = Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "3000.0", "improvementsAmtAfter" -> "3000.0")
      lazy val form = improvementsForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "3000.0", "improvementsAmtAfter" -> "3000.0")
      }
    }

    "passing a in a valid no map" should {
      val map = Map("isClaimingImprovements" -> messages.no, "improvementsAmt" -> "", "improvementsAmtAfter" -> "")
      lazy val form = improvementsForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("isClaimingImprovements" -> messages.no, "improvementsAmt" -> "", "improvementsAmtAfter" -> "")
      }
    }

    "passing a in a valid map with two decimal places" should {
      val map = Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "3000.05", "improvementsAmtAfter" -> "3000.0")
      lazy val form = improvementsForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "3000.05", "improvementsAmtAfter" -> "3000.0")
      }
    }

    "passing in a valid map with a improvementsAmt on the max amount" should {
      val map = Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "1000000000.00", "improvementsAmtAfter" -> "3000.0")
      lazy val form = improvementsForm.bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe false
      }

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "1000000000.00", "improvementsAmtAfter" -> "3000.0")
      }
    }

    "passing a in a valid map with three decimal places for improvementsAmt" should {
      val map = Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "3000.051", "improvementsAmtAfter" -> "3000.0")
      lazy val form = improvementsForm.bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.Improvements.excessDecimalPlacesError} containing the data" in {
        form.error("").get.message shouldBe messages.Improvements.excessDecimalPlacesError
      }
    }

    "passing a in a valid map with three decimal places for improvementsAmtAfter" should {
      val map = Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "3000.51", "improvementsAmtAfter" -> "3000.009")
      lazy val form = improvementsForm.bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.Improvements.excessDecimalPlacesError} containing the data" in {
        form.error("").get.message shouldBe messages.Improvements.excessDecimalPlacesError
      }
    }

    "passing a in a valid map with a negative number for improvementsAmt" should {
      val map = Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "-3000.01", "improvementsAmtAfter" -> "3000.0")
      lazy val form = improvementsForm.bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.Improvements.negativeValueError} containing the data" in {
        form.error("").get.message shouldBe messages.Improvements.negativeValueError
      }
    }

    "passing a in a valid map with a negative number for improvementsAmtAfter" should {
      val map = Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "3000.01", "improvementsAmtAfter" -> "-3000.0")
      lazy val form = improvementsForm.bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.Improvements.negativeValueError} containing the data" in {
        form.error("").get.message shouldBe messages.Improvements.negativeValueError
      }
    }

    "passing in a valid map with a improvementsAmt over the max amount" should {
      val map = Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "1000000000.01", "improvementsAmtAfter" -> "3000.0")
      lazy val form = improvementsForm.bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.Improvements.errorMaximum("1000000000.01")} containing the data" in {
        form.error("").get.message shouldBe messages.Improvements.errorMaximum("1,000,000,000")
      }
    }

    "passing in a valid map with a improvementsAmtAfter over the max amount" should {
      val map = Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "1000.01", "improvementsAmtAfter" -> "1000000000.01")
      lazy val form = improvementsForm.bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.Improvements.errorMaximum("1000000000.01")} containing the data" in {
        form.error("").get.message shouldBe messages.Improvements.errorMaximum("1,000,000,000")
      }
    }

    "passing in a valid map with a string for improvementsAmt" should {
      val map = Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "testData", "improvementsAmtAfter" -> "3000.0")
      lazy val form = improvementsForm.bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.numericPlayErrorOverride} containing the data" in {
        form.error("").get.message shouldBe messages.numericPlayErrorOverride
      }
    }
  }

}

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

import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{RebasedValue => messages}
import forms.nonresident.RebasedValueForm._
import models.nonresident.RebasedValueModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class RebasedValueFormSpec extends UnitSpec with WithFakeApplication {

  "Rebased Value form" when {

    "passing in a valid model with yes" should {
      val model = RebasedValueModel("Yes", Some(1000))
      lazy val form = rebasedValueForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map(
          "hasRebasedValue" -> "Yes",
          "rebasedValueAmt" -> "1000"
        )
      }
    }

    "passing in a valid map with a yes" should {
      val map = Map(
        "hasRebasedValue" -> "Yes",
        "rebasedValueAmt" -> "1000"
      )
      lazy val form = rebasedValueForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(RebasedValueModel("Yes", Some(1000)))
      }
    }

    "passing in a valid map with a no" should {
      val map = Map(
        "hasRebasedValue" -> "No",
        "rebasedValueAmt" -> ""
      )
      lazy val form = rebasedValueForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(RebasedValueModel("No", None))
      }
    }

    "passing in a valid map with a no but an invalid amount" should {
      val map = Map(
        "hasRebasedValue" -> "No",
        "rebasedValueAmt" -> "-1000.065"
      )
      lazy val form = rebasedValueForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(RebasedValueModel("No", Some(-1000.065)))
      }
    }

    "passing in a valid map with a no but an amount above the max" should {
      val map = Map(
        "hasRebasedValue" -> "No",
        "rebasedValueAmt" -> "1000000001"
      )
      lazy val form = rebasedValueForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(RebasedValueModel("No", Some(1000000001)))
      }
    }

    "passing in an invalid map with an empty answer when calculation has not been chosen" should {
      val map = Map(
        "hasRebasedValue" -> "",
        "rebasedValueAmt" -> ""
      )
      lazy val form = rebasedValueForm.bind(map)

      "return an invalid form with one errors" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${commonMessages.errorRequired}" in {
        form.error("hasRebasedValue").get.message shouldBe commonMessages.errorRequired
      }
    }

    "passing in an invalid map with an invalid answer when calculation has not been chosen" should {
      val map = Map(
        "hasRebasedValue" -> "a",
        "rebasedValueAmt" -> ""
      )
      lazy val form = rebasedValueForm.bind(map)

      "return an invalid form with one errors" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${commonMessages.errorRequired}" in {
        form.error("hasRebasedValue").get.message shouldBe commonMessages.errorRequired
      }
    }

    "passing in an invalid map with an empty value" should {
      val map = Map(
        "hasRebasedValue" -> "Yes",
        "rebasedValueAmt" -> ""
      )
      lazy val form = rebasedValueForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.errorNoValue}" in {
        form.error("").get.message shouldBe messages.errorNoValue
      }
    }

    "passing in an invalid map with a non-numeric value" should {
      val map = Map(
        "hasRebasedValue" -> "Yes",
        "rebasedValueAmt" -> "a"
      )
      lazy val form = rebasedValueForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.errorNoValue}" in {
        form.error("").get.message shouldBe messages.errorNoValue
      }
    }

    "passing in an invalid map with an invalid number of decimal places" should {
      val map = Map(
        "hasRebasedValue" -> "Yes",
        "rebasedValueAmt" -> "1000.056"
      )
      lazy val form = rebasedValueForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.errorDecimalPlaces}" in {
        form.error("").get.message shouldBe messages.errorDecimalPlaces
      }
    }

    "passing in an invalid map with a negative number" should {
      val map = Map(
        "hasRebasedValue" -> "Yes",
        "rebasedValueAmt" -> "-1000"
      )
      lazy val form = rebasedValueForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.errorNegative}" in {
        form.error("").get.message shouldBe messages.errorNegative
      }
    }

    "passing in an invalid map with a number higher than the maximum" should {
      val map = Map(
        "hasRebasedValue" -> "Yes",
        "rebasedValueAmt" -> "1000000000.01"
      )
      lazy val form = rebasedValueForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.errorMaximum("1,000,000,000")}" in {
        form.error("").get.message shouldBe messages.errorMaximum("1,000,000,000")
      }
    }

    "passing in an invalid map with multiple errors" should {
      val map = Map(
        "hasRebasedValue" -> "Yes",
        "rebasedValueAmt" -> "-500.345"
      )
      lazy val form = rebasedValueForm.bind(map)

      "return an invalid form with two errors" in {
        form.errors.size shouldBe 2
      }
    }
  }

}

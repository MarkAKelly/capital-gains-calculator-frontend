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

import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import forms.nonresident.OtherReliefsForm._
import models.nonresident.OtherReliefsModel
import assets.MessageLookup.{NonResident => messages}

class OtherReliefsFormSpec extends UnitSpec with WithFakeApplication {

  "Other Reliefs form" when {

    "passing in a valid model" should {
      val model = OtherReliefsModel(Some("Yes"), Some(1000))
      lazy val form = otherReliefsForm(false).fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map(
          "isClaimingOtherReliefs" -> "Yes",
          "otherReliefs" -> "1000"
        )
      }
    }

    "passing in a valid map with a yes" should {
      val map = Map(
        "isClaimingOtherReliefs" -> "Yes",
        "otherReliefs" -> "1000"
      )
      lazy val form = otherReliefsForm(false).bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(OtherReliefsModel(Some("Yes"), Some(1000)))
      }
    }

    "passing in a valid map with a no" should {
      val map = Map(
        "isClaimingOtherReliefs" -> "No",
        "otherReliefs" -> ""
      )
      lazy val form = otherReliefsForm(false).bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(OtherReliefsModel(Some("No"), None))
      }
    }

    "passing in a valid map with a no but an invalid amount" should {
      val map = Map(
        "isClaimingOtherReliefs" -> "No",
        "otherReliefs" -> "-1000.065"
      )
      lazy val form = otherReliefsForm(false).bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(OtherReliefsModel(Some("No"), Some(-1000.065)))
      }
    }

    "passing in a valid map with a no but an amount above the max" should {
      val map = Map(
        "isClaimingOtherReliefs" -> "No",
        "otherReliefs" -> "1000000001"
      )
      lazy val form = otherReliefsForm(false).bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(OtherReliefsModel(Some("No"), Some(1000000001)))
      }
    }

    "passing in a valid map with no answer and a valid result when calculation has been chosen" should {
      val map = Map(
        "otherReliefs" -> "1000.05"
      )
      lazy val form = otherReliefsForm(true).bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value shouldBe Some(OtherReliefsModel(None, Some(1000.05)))
      }
    }

    "passing in an invalid map with an empty answer when calculation has not been chosen" should {
      val map = Map(
        "isClaimingOtherReliefs" -> "",
        "otherReliefs" -> ""
      )
      lazy val form = otherReliefsForm(false).bind(map)

      "return an invalid form with one errors" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.errorRequired}" in {
        form.error("isClaimingOtherReliefs").get.message shouldBe messages.errorRequired
      }
    }

    "passing in an invalid map with an invalid answer when calculation has not been chosen" should {
      val map = Map(
        "isClaimingOtherReliefs" -> "a",
        "otherReliefs" -> ""
      )
      lazy val form = otherReliefsForm(false).bind(map)

      "return an invalid form with one errors" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.errorRequired}" in {
        form.error("isClaimingOtherReliefs").get.message shouldBe messages.errorRequired
      }
    }

    "passing in an invalid map with an empty value" should {
      val map = Map(
        "isClaimingOtherReliefs" -> "Yes",
        "otherReliefs" -> ""
      )
      lazy val form = otherReliefsForm(false).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.errorRealNumber}" in {
        form.error("").get.message shouldBe messages.errorRealNumber
      }
    }

    "passing in an invalid map with a non-numeric value" should {
      val map = Map(
        "isClaimingOtherReliefs" -> "Yes",
        "otherReliefs" -> "a"
      )
      lazy val form = otherReliefsForm(false).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.errorRealNumber}" in {
        form.error("").get.message shouldBe messages.errorRealNumber
      }
    }

    "passing in an invalid map with an invalid number of decimal places" should {
      val map = Map(
        "isClaimingOtherReliefs" -> "",
        "otherReliefs" -> "1000.056"
      )
      lazy val form = otherReliefsForm(true).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.OtherReliefs.errorDecimal}" in {
        form.error("").get.message shouldBe messages.OtherReliefs.errorDecimal
      }
    }

    "passing in an invalid map with a negative number" should {
      val map = Map(
        "isClaimingOtherReliefs" -> "Yes",
        "otherReliefs" -> "-1000"
      )
      lazy val form = otherReliefsForm(false).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.OtherReliefs.errorNegative}" in {
        form.error("").get.message shouldBe messages.OtherReliefs.errorNegative
      }
    }

    "passing in an invalid map with a number higher than the maximum" should {
      val map = Map(
        "isClaimingOtherReliefs" -> "Yes",
        "otherReliefs" -> "1000000000.01"
      )
      lazy val form = otherReliefsForm(false).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of ${messages.OtherReliefs.errorMaximum("1,000,000,000")}" in {
        form.error("").get.message shouldBe messages.OtherReliefs.errorMaximum("1,000,000,000")
      }
    }

    "passing in an invalid map with multiple errors" should {
      val map = Map(
        "isClaimingOtherReliefs" -> "Yes",
        "otherReliefs" -> "-500.345"
      )
      lazy val form = otherReliefsForm(false).bind(map)

      "return an invalid form with two errors" in {
        form.errors.size shouldBe 2
      }
    }
  }
}
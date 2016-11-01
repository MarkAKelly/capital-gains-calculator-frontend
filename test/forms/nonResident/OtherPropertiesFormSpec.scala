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

import forms.nonresident.OtherPropertiesForm._
import models.nonresident.OtherPropertiesModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.NonResident.{OtherProperties => messages}
import assets.MessageLookup.{NonResident => commonMessages}

class OtherPropertiesFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form" when {

    "passing in a valid model with Yes" should {
      lazy val model = OtherPropertiesModel("Yes", Some(BigDecimal(1500)))
      lazy val form = otherPropertiesForm(true).fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("otherProperties" -> "Yes", "otherPropertiesAmt" -> "1500")
      }
    }

    "passing in a valid model with No" should {
      lazy val model = OtherPropertiesModel("No", None)
      lazy val form = otherPropertiesForm(true).fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("otherProperties" -> "No", "otherPropertiesAmt" -> "")
      }
    }

    "passing in a valid map with Yes" should {
      lazy val map = Map("otherProperties" -> "Yes", "otherPropertiesAmt" -> "1500")
      lazy val form = otherPropertiesForm(true).bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.value.get shouldBe OtherPropertiesModel("Yes", Some(BigDecimal(1500)))
      }
    }

    "passing in an invalid map with an empty string instead of Yes or No" should {
      lazy val map = Map("otherProperties" -> "", "otherPropertiesAmt" -> "1500")
      lazy val form = otherPropertiesForm(true).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.errorRequired}" in {
        form.error("otherProperties").get.message shouldBe commonMessages.errorRequired
      }
    }

    "passing in an invalid map with an random string instead of Yes or No" should {
      lazy val map = Map("otherProperties" -> "a", "otherPropertiesAmt" -> "1500")
      lazy val form = otherPropertiesForm(true).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.errorRequired}" in {
        form.error("otherProperties").get.message shouldBe commonMessages.errorRequired
      }
    }

    "passing in an invalid map with an random string instead of an amount" should {
      lazy val map = Map("otherProperties" -> "Yes", "otherPropertiesAmt" -> "a")
      lazy val form = otherPropertiesForm(true).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorQuestion}" in {
        form.error("").get.message shouldBe messages.errorQuestion
      }
    }

    "passing in an invalid map with a negative number" should {
      lazy val map = Map("otherProperties" -> "Yes", "otherPropertiesAmt" -> "-1500")
      lazy val form = otherPropertiesForm(true).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorNegative}" in {
        form.error("").get.message shouldBe messages.errorNegative
      }
    }

    "passing in an invalid map with a number with too many decimal places" should {
      lazy val map = Map("otherProperties" -> "Yes", "otherPropertiesAmt" -> "1500.1823")
      lazy val form = otherPropertiesForm(true).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorDecimalPlaces}" in {
        form.error("").get.message shouldBe messages.errorDecimalPlaces
      }
    }

    "passing in an invalid map with an empty number" should {
      lazy val map = Map("otherProperties" -> "Yes", "otherPropertiesAmt" -> "")
      lazy val form = otherPropertiesForm(true).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.errorQuestion}" in {
        form.error("").get.message shouldBe messages.errorQuestion
      }
    }

    "passing in an invalid map with a number that exceeds the maimum numeric answer" should {
      lazy val map = Map("otherProperties" -> "Yes", "otherPropertiesAmt" -> "123000000001230")
      lazy val form = otherPropertiesForm(true).bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${commonMessages.maximumLimit("1,000,000,000")}" in {
        form.error("").get.message shouldBe commonMessages.maximumLimit("1,000,000,000")
      }
    }

    "passing in an invalid map with a number that is both negative and has too many decimal places" should {
      lazy val map = Map("otherProperties" -> "Yes", "otherPropertiesAmt" -> "-123812.437834")
      lazy val form = otherPropertiesForm(true).bind(map)

      "return an invalid form with two errors" in {
        form.errors.size shouldBe 2
      }

      s"include an error message of '${messages.errorNegative}" in {
        form.errors.head.message shouldEqual messages.errorNegative
      }

      s"include an error message of '${messages.errorDecimalPlaces}" in {
        form.errors(1).message shouldEqual messages.errorDecimalPlaces
      }
    }
  }
}

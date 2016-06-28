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

package forms.resident

import assets.MessageLookup
import controllers.helpers.FakeRequestHelper
import forms.resident.ImprovementsForm._
import models.resident.ImprovementsModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class ImprovementsFormSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Creating a form using an empty model" should {

    lazy val form = improvementsForm

    "return an empty string for amount" in {
      form.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {

    "return a form with the data specified in the model" in {
      val model = ImprovementsModel(1)
      val form = improvementsForm.fill(model)
      form.data("amount") shouldBe "1"
    }

  }

  "Creating a form using an invalid post" when {

    "supplied with no data for amount" should {

      lazy val form = improvementsForm.bind(Map("amount" -> ""))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe MessageLookup.undefinedMessage
      }
    }

    "supplied with empty space for amount" should {

      lazy val form = improvementsForm.bind(Map("amount" -> "  "))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe MessageLookup.undefinedMessage
      }
    }

    "supplied with non numeric input for amount" should {

      lazy val form = improvementsForm.bind(Map("amount" -> "a"))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe MessageLookup.undefinedMessage
      }
    }

    "supplied with an amount with 3 numbers after the decimal" should {

      lazy val form = improvementsForm.bind(Map("amount" -> "1.000"))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe MessageLookup.undefinedMessage
      }
    }

    "supplied with an amount that's greater than the max" should {

      lazy val form = improvementsForm.bind(Map("amount" -> "1000000000.01"))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe MessageLookup.undefinedMessage
      }
    }

    "supplied with an amount that's less than the zero" should {

      lazy val form = improvementsForm.bind(Map("amount" -> "-0.01"))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe MessageLookup.undefinedMessage
      }
    }
  }

  "Creating a form using a valid post" when {

    "supplied with valid amount" should {
      "build a model with the correct amount" in {
        val form = improvementsForm.bind(Map("amount" -> "1"))
        form.value.get shouldBe ImprovementsModel(BigDecimal(1))
      }
    }

    "supplied with valid amount" should {
      "not raise form error" in {
        val form = improvementsForm.bind(Map("amount" -> "1"))
        form.hasErrors shouldBe false
      }
    }

    "supplied with an amount with 1 number after the decimal" should {
      "not raise form error" in {
        val form = improvementsForm.bind(Map("amount" -> "1.1"))
        form.hasErrors shouldBe false
      }
    }

    "supplied with an amount with 2 numbers after the decimal" should {
      "not raise form error" in {
        val form = improvementsForm.bind(Map("amount" -> "1.11"))
        form.hasErrors shouldBe false
      }
    }

    "supplied with an amount that's equal to the max" should {
      "not raise form error" in {
        val form = improvementsForm.bind(Map("amount" -> "1000000000"))
        form.hasErrors shouldBe false
      }
    }

    "supplied with an amount that's equal to the min" should {
      "not raise form error" in {
        val form = improvementsForm.bind(Map("amount" -> "0"))
        form.hasErrors shouldBe false
      }
    }
  }
}
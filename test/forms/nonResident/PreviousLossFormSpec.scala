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

import models.nonresident.PreviousLossModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import forms.nonresident.PreviousLossForm._
import assets.MessageLookup.{NonResident => messages}

class PreviousLossFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form" when {

    "passing in a model" should {
      val model = PreviousLossModel(200)
      lazy val form = previousLossForm.fill(model)

      "return a valid form" in {
        form.data shouldBe Map("loss" -> "200")
      }
    }

    "passing in a valid map" should {
      val map = Map("loss" -> "200")
      lazy val form = previousLossForm.bind(map)

      "return a form with no errors" in {
        form.errors.isEmpty shouldBe true
      }

      "return a valid form" in {
        form.value shouldBe Some(PreviousLossModel(200))
      }
    }

    "passing in an invalid map with an empty value" should {
      val map = Map("loss" -> "")
      lazy val form = previousLossForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.mandatoryAmount}" in {
        form.error("loss").get.message shouldBe messages.mandatoryAmount
      }
    }

    "passing in an invalid map with a non-numeric value" should {
      val map = Map("loss" -> "a")
      lazy val form = previousLossForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.mandatoryAmount}" in {
        form.error("loss").get.message shouldBe messages.mandatoryAmount
      }
    }

    "passing in an invalid map with three decimal places" should {
      val map = Map("loss" -> "1850.456")
      lazy val form = previousLossForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.PreviousLoss.errorDecimalPlaces}" in {
        form.error("loss").get.message shouldBe messages.PreviousLoss.errorDecimalPlaces
      }
    }

    "passing in an invalid map with a negative value" should {
      val map = Map("loss" -> "-1200")
      lazy val form = previousLossForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.PreviousLoss.errorNegative}" in {
        form.error("loss").get.message shouldBe messages.PreviousLoss.errorNegative
      }
    }

    "passing in an invalid map with a value above 1 billion" should {
      val map = Map("loss" -> "1000000000.01")
      lazy val form = previousLossForm.bind(map)

      "return an invalid form with one error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message of '${messages.PreviousLoss.errorMaximum("1,000,000,000")}" in {
        form.error("loss").get.message shouldBe messages.PreviousLoss.errorMaximum("1,000,000,000")
      }
    }
  }
}

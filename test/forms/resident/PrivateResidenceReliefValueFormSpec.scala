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

import assets.MessageLookup._
import forms.resident.properties.PrivateResidenceReliefValueForm._
import models.resident.properties.PrivateResidenceReliefValueModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class PrivateResidenceReliefValueFormSpec extends UnitSpec with WithFakeApplication {

  "Creating the form for Private Residence Relief Value from a valid input" should {
    "return a populated form using .fill" in {
      val model = PrivateResidenceReliefValueModel(1000)
      val form = privateResidenceReliefValueForm.fill(model)
      form.value.get shouldBe PrivateResidenceReliefValueModel(1000)
    }

    "return a valid model if supplied with valid inputs" in {
      val form = privateResidenceReliefValueForm.bind(Map(("amount", "1000")))
      form.value shouldBe Some(PrivateResidenceReliefValueModel(1000))
    }
  }

  "Creating the form for Private Residence Relief Value from an invalid input" when {

    "supplied with no data for amount" should {
      lazy val form = privateResidenceReliefValueForm.bind(Map(("amount", "")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return a form with the error message '${errorMessages.mandatoryAmount}'" in {
        form.error("amount").get.message shouldBe errorMessages.mandatoryAmount
      }
    }

    "supplied with a non-numeric value for amount" should {
      lazy val form = privateResidenceReliefValueForm.bind(Map(("amount", "a")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return a form with the error message '${errorMessages.invalidAmount}'" in {
        form.error("amount").get.message shouldBe errorMessages.invalidAmount
      }
    }

    "supplied with an amount that is too big" should {
      lazy val form = privateResidenceReliefValueForm.bind(Map(("amount", "9999999999999")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return a form with the error message '${errorMessages.maximumAmount}'" in {
        form.error("amount").get.message shouldBe errorMessages.maximumAmount
      }
    }

    "supplied with a negative amount" should {
      lazy val form = privateResidenceReliefValueForm.bind(Map(("amount", "-1000")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return a form with the error message '${errorMessages.minimumAmount}'" in {
        form.error("amount").get.message shouldBe errorMessages.minimumAmount
      }
    }

    "supplied with an amount that has too many decimal places" should {
      lazy val form = privateResidenceReliefValueForm.bind(Map(("amount", "0.001")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return a form with the error message '${errorMessages.invalidAmount}'" in {
        form.error("amount").get.message shouldBe errorMessages.invalidAmount
      }
    }
  }
}

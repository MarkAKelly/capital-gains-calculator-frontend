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

package forms.resident.properties

import assets.MessageLookup.errorMessages
import assets.MessageLookup.whoDidYouGiveItTo
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import forms.resident.properties.gain.PropertyRecipientForm._
import models.resident.properties.gain.PropertyRecipientModel

class PropertyRecipientFormSpec extends UnitSpec with WithFakeApplication {
  "Creating the form from an empty model" should {

    "create an empty form when the model is empty" in {
      lazy val form = propertyRecipientForm
      form.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {

    "return a form with the data specified in the model" in {
      lazy val form = propertyRecipientForm.fill(PropertyRecipientModel("Charity"))
      form.data("propertyRecipient") shouldBe "Charity"
    }

    "return a form with the data specified from the map" in {
      lazy val form = propertyRecipientForm.bind(Map("propertyRecipient" -> "Charity"))
      form.data("propertyRecipient") shouldBe "Charity"
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for property recipient" should {

      lazy val map = Map(("propertyRecipient" -> ""))
      lazy val form = propertyRecipientForm.bind(map)

      "raise a form error" in {
        form.hasErrors shouldBe true
      }

      "raise only one error" in {
        form.errors.length shouldBe 1
      }

      s"error with message '${whoDidYouGiveItTo.errormandatory}" in {
        form.error("propertyRecipient").get.message shouldBe whoDidYouGiveItTo.errormandatory
      }
    }

  }

    "throw an error when supplied with an empty value" in {
      lazy val map = Map(("propertyRecipient", ""))
      lazy val form = propertyRecipientForm.bind(map)

      form.hasErrors shouldBe true
    }

    "not throw an error when supplied with correct/valid mapping for Spouse option" in {
      lazy val map = Map(("propertyRecipient", "Spouse"))
      lazy val form = propertyRecipientForm.bind(map)
      form.hasErrors shouldBe false
    }

    "not throwing an error when supplied with the correct/valid mappings for Charity option" in {
      lazy val map = Map(("propertyRecipient", "Charity"))
      lazy val form = propertyRecipientForm.bind(map)
      form.hasErrors shouldBe false
    }

    "not throwing an error when supplied with the correct/valid mappings for Other option" in {
      lazy val map = Map(("propertyRecipient", "Other"))
      lazy val form = propertyRecipientForm.bind(map)

      form.hasErrors shouldBe false
    }
}

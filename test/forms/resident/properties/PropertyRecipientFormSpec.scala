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

import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import forms.resident.properties.gain.PropertyRecipientForm._

class PropertyRecipientFormSpec extends UnitSpec with WithFakeApplication {
  "Creating the form from a model" when  {

    "create an empty form when the model is empty" in {
      lazy val form = propertyRecipientForm
      form.data.isEmpty shouldBe true
    }

    "throw an error when supplied with an empty value" in {
      lazy val map = Map(("propertyRecipient", ""))
      lazy val form = propertyRecipientForm.bind(map)

      form.hasErrors shouldBe true
    }

    "throw an error when supplied with incorrect mappings" in {
      lazy val map = Map(("propertyRecipient", "Something"))
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
}

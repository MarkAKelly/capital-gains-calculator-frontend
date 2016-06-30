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

import models.resident.ReliefsModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import forms.resident.ReliefsForm._
import assets.{MessageLookup => commonMessages}

class ReliefsFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form using a valid model" should {

    "return a form with the data specified in the model" in {
      lazy val model = ReliefsModel(true)
      lazy val form = reliefsForm.fill(model)
      form.value shouldBe Some(ReliefsModel(true))
    }
  }

  "Creating a form using a valid map" should {

    "return a form with the data specified in the model" in {
      lazy val form = reliefsForm.bind(Map(("isClaiming", "Yes")))
      form.value shouldBe Some(ReliefsModel(true))
    }
  }

  "Creating a form using an invalid map" when {

    "supplied with no data" should {
      lazy val form = reliefsForm.bind(Map(("isClaiming", "")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return an error with message ${commonMessages.undefinedMessage}" in {
        form.error("isClaiming").get.message shouldBe commonMessages.undefinedMessage
      }
    }

    "supplied with invalid data" should {
      lazy val form = reliefsForm.bind(Map(("isClaiming", "a")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return an error with message ${commonMessages.undefinedMessage}" in {
        form.error("isClaiming").get.message shouldBe commonMessages.undefinedMessage
      }
    }
  }

}

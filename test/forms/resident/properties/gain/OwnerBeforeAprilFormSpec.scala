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

package forms.resident.properties.gain

import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import models.resident.properties.gain.OwnerBeforeAprilModel
import forms.resident.properties.gain.OwnerBeforeAprilForm._
import assets.MessageLookup.Resident.Properties.{ownerBeforeAprilNineteenEightyTwo => messages}

class OwnerBeforeAprilFormSpec  extends UnitSpec with WithFakeApplication {

  "Creating a form without a model" should {

    "create an empty form" in {
      lazy val form = ownerBeforeAprilForm
      form.data.isEmpty shouldEqual true
    }
  }

  "Creating a form using a valid model" should {

    "return a form with the answer of Yes" in {
      lazy val model = OwnerBeforeAprilModel(true)
      lazy val form = ownerBeforeAprilForm.fill(model)
      form.data.get("ownedBeforeAprilNineteenEightyTwo") shouldEqual Some("Yes")
    }

    "return a form with the answer of No" in {
      lazy val model = OwnerBeforeAprilModel(false)
      lazy val form = ownerBeforeAprilForm.fill(model)
      form.data.get("ownedBeforeAprilNineteenEightyTwo") shouldEqual Some("No")
    }
  }

  "Creating a form using a valid map" should {

    "return a form with a value of Yes" in {
      lazy val form = ownerBeforeAprilForm.bind(Map(("ownedBeforeAprilNineteenEightyTwo", "Yes")))
      form.value shouldEqual Some(OwnerBeforeAprilModel(true))
    }

    "return a form with a value of No" in {
      lazy val form = ownerBeforeAprilForm.bind(Map(("ownedBeforeAprilNineteenEightyTwo", "No")))
      form.value shouldEqual Some(OwnerBeforeAprilModel(false))
    }
  }

  "Creating a form using an invalid map" when {

    "supplied with no data" should {
      lazy val form = ownerBeforeAprilForm.bind(Map(("ownedBeforeAprilNineteenEightyTwo", "")))

      "return a form with errors" in {
        form.hasErrors shouldEqual true
      }

      "return 1 error" in {
        form.errors.size shouldEqual 1
      }

      s"return an error with message ${messages.errorSelectAnOption}" in {
        form.error("ownedBeforeAprilNineteenEightyTwo").get.message shouldEqual messages.errorSelectAnOption
      }
    }

    "supplied with invalid data" should {
      lazy val form = ownerBeforeAprilForm.bind(Map(("ownedBeforeAprilNineteenEightyTwo", "a")))

      "return a form with errors" in {
        form.hasErrors shouldEqual true
      }

      "return 1 error" in {
        form.errors.size shouldEqual 1
      }

      s"return an error with message ${messages.errorSelectAnOption}" in {
        form.error("ownedBeforeAprilNineteenEightyTwo").get.message shouldEqual messages.errorSelectAnOption
      }
    }
  }

}

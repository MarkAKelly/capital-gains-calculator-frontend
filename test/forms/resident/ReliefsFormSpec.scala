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

import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.{MessageLookup => commonMessages}
import commonMessages.{reliefs => messages}
import forms.resident.properties.ReliefsForm._
import models.resident.properties.ReliefsModel
import uk.gov.hmrc.play.views.helpers.MoneyPounds

class ReliefsFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form using a valid model" should {

    "return a form with the data specified in the model" in {
      lazy val model = ReliefsModel(true)
      lazy val form = reliefsForm(BigDecimal(0)).fill(model)
      form.value shouldBe Some(ReliefsModel(true))
    }
  }

  "Creating a form using a valid map" should {

    "return a form with the data specified in the model" in {
      lazy val form = reliefsForm(BigDecimal(0)).bind(Map(("isClaiming", "Yes")))
      form.value shouldBe Some(ReliefsModel(true))
    }
  }

  "Creating a form using an invalid map" when {

    "supplied with no data" should {
      lazy val form = reliefsForm(BigDecimal(10000)).bind(Map(("isClaiming", "")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return an error with message ${messages.errorSelect(MoneyPounds(10000, 0).quantity)}" in {
        form.error("isClaiming").get.message shouldBe messages.errorSelect(MoneyPounds(10000, 0).quantity)
      }
    }

    "supplied with invalid data" should {
      lazy val form = reliefsForm(BigDecimal(10000)).bind(Map(("isClaiming", "a")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return an error with message ${messages.errorSelect(MoneyPounds(10000, 0).quantity)}" in {
        form.error("isClaiming").get.message shouldBe messages.errorSelect(MoneyPounds(10000, 0).quantity)
      }
    }
  }

}

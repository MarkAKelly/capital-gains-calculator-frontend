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

import models.resident.properties.PrivateResidenceReliefModel
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}
import forms.resident.properties.PrivateResidenceReliefForm._
import assets.MessageLookup.{privateResidenceReilef => messages}

class PrivateResidenceReliefFormSpec extends UnitSpec with WithFakeApplication {

  "Creating the form for Private Residence Relief from a valid selection" should {
    "return a populated form using .fill" in {
      val model = PrivateResidenceReliefModel("Full")
      val form = privateResidenceReliefForm.fill(model)

      form.value.get shouldBe PrivateResidenceReliefModel("Full")
    }

    "return a valid model if supplied with valid selection 'Full'" in {
      val form = privateResidenceReliefForm.bind(Map(("prrClaiming", "Full")))
      form.value shouldBe Some(PrivateResidenceReliefModel("Full"))
    }

    "return a valid model if supplied with valid selection 'Part'" in {
      val form = privateResidenceReliefForm.bind(Map(("prrClaiming", "Part")))
      form.value shouldBe Some(PrivateResidenceReliefModel("Part"))
    }

    "return a valid model if supplied with valid selection 'None'" in {
      val form = privateResidenceReliefForm.bind(Map(("prrClaiming", "None")))
      form.value shouldBe Some(PrivateResidenceReliefModel("None"))
    }
  }

  "Creating the form for Private Residence Relief from invalid selection" when {

    "supplied with no selection" should {

      lazy val form = privateResidenceReliefForm.bind(Map(("prrClaiming", "")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return a form with the error message ${messages.errorSelect}" in {
        form.error("prrClaiming").get.message shouldBe messages.errorSelect
      }
    }

    "supplied with non Full/Part/None selection" should {
      lazy val form = privateResidenceReliefForm.bind(Map(("prrClaiming", "abc")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return a form with the error message ${messages.errorSelect}" in {
        form.error("prrClaiming").get.message shouldBe messages.errorSelect
      }
    }
  }

}
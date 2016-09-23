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

package forms.resident.shares

import forms.resident.shares.OwnedBeforeEightyTwoForm._
import models.resident.shares.OwnedBeforeEightyTwoModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class OwnerBeforeEightyTwoSpec extends UnitSpec with WithFakeApplication {

  "Creating the OwnedBeforeEightyTwo form from valid inputs" should {

    "return a populated form using .fill" in {
      val model = OwnedBeforeEightyTwoModel(true)
      val form = ownedBeforeEightyTwoForm.fill(model)
      form.value.get shouldBe OwnedBeforeEightyTwoModel(true)
    }

    "return a populated form using .bind with an answer of Yes" in {
      val form = ownedBeforeEightyTwoForm.bind(Map(("ownedBeforeEightyTwo", "Yes")))
      form.value.get shouldBe OwnedBeforeEightyTwoModel(true)
    }

    "return a populated form using .bind with an answer of No" in {
      val form = ownedBeforeEightyTwoForm.bind(Map(("ownedBeforeEightyTwo", "No")))
      form.value.get shouldBe OwnedBeforeEightyTwoModel(false)
    }
  }

  "Creating the OwnedBeforeEightyTwo form from invalid inputs" when {

    "supplied with no selection" should {
      lazy val form = ownedBeforeEightyTwoForm.bind(Map(("ownedBeforeEightyTwo", "")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

    }

    "supplied with an incorrect selection" should {
      lazy val form = ownedBeforeEightyTwoForm.bind(Map(("ownedBeforeEightyTwo", "true")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }
    }
  }
}

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

import assets.MessageLookup.{NonResident => messages}
import forms.nonresident.ImprovementsForm._
import models.nonresident.ImprovementsModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class ImprovementsFormSpec extends UnitSpec with WithFakeApplication{

  "Creating a form" when {

    "passing a in a valid model" should {
      val model = ImprovementsModel(messages.yes, Some(1000.0), Some(1000.0))
      val form = improvementsForm.fill(model)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "1000.0", "improvementsAmtAfter" -> "1000.0")
      }
    }

    "passing a in a valid yes map" should {
      val map = Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "3000.0", "improvementsAmtAfter" -> "3000.0")
      val form = improvementsForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "3000.0", "improvementsAmtAfter" -> "3000.0")
      }
    }

    "passing a in a valid no map" should {
      val map = Map("isClaimingImprovements" -> messages.no, "improvementsAmt" -> "", "improvementsAmtAfter" -> "")
      val form = improvementsForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("isClaimingImprovements" -> messages.no, "improvementsAmt" -> "", "improvementsAmtAfter" -> "")
      }
    }

    "passing a in a valid map with two decimal places" should {
      val map = Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "3000.05", "improvementsAmtAfter" -> "3000.0")
      val form = improvementsForm.bind(map)

      "return a valid form with no errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing the data" in {
        form.data shouldBe Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "3000.05", "improvementsAmtAfter" -> "3000.0")
      }
    }

    "passing a in a valid map with three decimal places" should {
      val map = Map("isClaimingImprovements" -> messages.yes, "improvementsAmt" -> "3000.051", "improvementsAmtAfter" -> "3000.0")
      val form = improvementsForm.bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return a valid form with no errors" in {
        form.errors.size shouldBe 1
      }

//      s"return an error message of ${messages.Improvements.excessDecimalPlacesError} containing the data" in {
//        form.error("improvementsAmt").get.message shouldBe messages.Improvements.excessDecimalPlacesError
//      }
    }
  }

}

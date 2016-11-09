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

import assets.MessageLookup.{NonResident => commonMessages}
//import assets.MessageLookup.NonResident.{SellOrGiveAway => messages}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class SellOrGiveAwayFormSpec extends UnitSpec with WithFakeApplication{

  "Sell Or Give Away Form" when {

    "passing in a valid model" should {
      val model = SellOrGiveAwayModel(true)
      val form = sellOrGiveAwayForm.fill(model)

      "return a form with 0 errors" in {
        form.errors.size shouldBe 0
      }

//      "return the correct data" in {
//        form.data shouldBe Map("SoldIt" -> true)
//      }
    }

//    "passing in a valid map" should {
//      val map = Map("SoldIt" -> true)
//      val form = sellOrGiveAwayForm.bind(map)
//
//      "return a form with 0 errros" in {
//        form.errors.size shouldBe 0
//      }
//
//      "return the correct data" in {
//        form.value shouldBe SellOrGiveAwayModel(true)
//      }
//    }
//
//    "passing in invalid data" should {
//      val map = Map("SoldIt", "true")
//      val form = sellOrGiveAwayForm.bind(map)
//
//      "return a form with 1 error" in {
//        form.errors.size shouldBe 1
//      }
//
//      s"return the error message of ${SellOrGiveAway.errorCompulsaryMessage}" in {
//        form.error("sellOrGiveAway") shouldbe SellOrGiveAway.errorCompulsaryMessage
//      }
//    }
//
//    "passing in an empty map" should {
//      val map = Map("SoldIt", None)
//      val form = sellOrGiveAwayform.bind(map)
//
//      "return a form with 1 error" in {
//        form.errors.size shouldBe 1
//      }
//
//      s"return the error message of ${SellOrGiveAway.errorCompulsaryMessage}" in {
//        form.error("sellOrGiveAway") shouldbe SellOrGiveAway.errorCompulsaryMessage
//      }
//    }
  }
}

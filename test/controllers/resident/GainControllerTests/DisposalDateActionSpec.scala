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

package controllers.resident.GainControllerTests

import controllers.resident.GainController
import controllers.helpers.FakeRequestHelper
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}

class DisposalDateActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Calling .disposalDate from the GainCalculationController" should {

    lazy val result = GainController.disposalDate(fakeRequestWithSession)

    "return a status of 200" in {
      status(result) shouldBe 200
    }

    "return some html" in {
      contentType(result) shouldBe Some("text/html")
    }
  }

  "Calling .disposalDate from the GainCalculationController with no session" should {

    lazy val result = GainController.disposalDate(fakeRequest)

    "return a status of 200" in {
      status(result) shouldBe 200
    }
  }

  "Calling .submitDisposalDate from the GainCalculationController" should {

    "when there is a valid form" should {

      lazy val fakePostRequest = fakeRequestToPOSTWithSession(("disposalDateDay", "30"), ("disposalDateMonth", "4"), ("disposalDateYear", "2016"))
      lazy val result = GainController.submitDisposalDate(fakePostRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Disposal Value page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/disposal-value")
      }
    }

    "when there is an invalid form" should {

      lazy val fakePostRequest = fakeRequestToPOSTWithSession(("disposalDateDay", "32"), ("disposalDateMonth", "4"), ("disposalDateYear", "2016"))
      lazy val result = GainController.submitDisposalDate(fakePostRequest)

      "return a status of 400 with an invalid POST" in {
        status(result) shouldBe 400
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }
    }
  }
}

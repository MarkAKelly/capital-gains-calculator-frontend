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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.SessionKeys
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}

class DisposalDateActionSpec extends UnitSpec with WithFakeApplication {

  "Calling .disposalDate from the GainCalculationController" should {

    "return a status of 200" in {
      val fakeRequest = FakeRequest("GET", "").withSession((SessionKeys.sessionId, ""))
      val result = GainController.disposalDate(fakeRequest)
      status(result) shouldBe 200
    }

    "return some html" in {
      val fakeRequest = FakeRequest("GET", "").withSession((SessionKeys.sessionId, ""))
      val result = GainController.disposalDate(fakeRequest)
      contentType(result) shouldBe Some("text/html")
    }
  }

  "Calling .disposalDate from the GainCalculationController with no session" should {
    "return a status of 303" in {
      val fakeRequest = FakeRequest("GET", "")
      val result = GainController.disposalDate(fakeRequest)
      status(result) shouldBe 303
    }
  }
}

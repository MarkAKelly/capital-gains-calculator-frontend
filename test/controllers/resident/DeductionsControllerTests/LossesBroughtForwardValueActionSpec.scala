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

package controllers.resident.DeductionsControllerTests

import assets.MessageLookup.{lossesBroughtForwardValue => messages}
import controllers.helpers.FakeRequestHelper
import controllers.resident.DeductionsController
import org.jsoup.Jsoup
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class LossesBroughtForwardValueActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Calling .lossesBroughtForwardValue from the resident DeductionsController" when {

    "request has a valid session" should {
      lazy val result = DeductionsController.lossesBroughtForwardValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with " in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title of ${messages.title}" in {
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }

    "request has an invalid session" should {
      lazy val result = DeductionsController.lossesBroughtForwardValue(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }
}

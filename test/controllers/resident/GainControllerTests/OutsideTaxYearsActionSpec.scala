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

import controllers.helpers.FakeRequestHelper
import controllers.resident.GainController
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.test.Helpers._
import assets.MessageLookup.{outsideTaxYears => messages}
import org.jsoup.Jsoup

class OutsideTaxYearsActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Calling .outsideTaxYears from the GainCalculationController" when {

    "there is a valid session" should {
      lazy val result = GainController.outsideTaxYears(fakeRequestWithSession)

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title of ${messages.title}" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }

    "there is no valid session" should {
      lazy val result = GainController.outsideTaxYears(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }
}

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
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.test.Helpers._
import assets.MessageLookup.{acquisitionCosts => messages}


class AcquisitionCostsActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper{

  "Calling .acquisitionCosts from the GainCalculationController" when {

    "request has a valid session" should {

      lazy val result = GainController.acquisitionCosts(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some Html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"have a title of ${messages.title}" in {
        doc.title() shouldBe messages.title
      }
    }

    "request has an invalid session" should {

      lazy val result = GainController.acquisitionCosts(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }
}

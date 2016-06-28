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

package controllers.resident

import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.test.Helpers._
import assets.MessageLookup.{summary => messages}


class SummaryActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Calling .summary from the SummaryController" should {

    lazy val result = SummaryController.summary(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(bodyOf(result))

    "return a status of 200" in {
      status(result) shouldBe 200
    }

    "return some html" in {
      contentType(result) shouldBe Some("text/html")
    }

    s"return a title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }
  }

  "Calling .summary from the SummaryController with no session" should {

    lazy val result = SummaryController.summary(fakeRequest)
    lazy val doc = Jsoup.parse(bodyOf(result))

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout view" in {
      redirectLocation(result).get shouldBe "/calculate-your-capital-gains/non-resident/session-timeout"
    }
  }
}
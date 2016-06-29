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

import assets.MessageLookup.{annualExemptAmount => messages}
import controllers.helpers.FakeRequestHelper
import controllers.resident.DeductionsController
import org.jsoup.Jsoup
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class AnnualExemptAmountActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {


  "Calling .reliefs from the resident DeductionsController" should {

    lazy val result = DeductionsController.annualExemptAmount(fakeRequestWithSession)

    "return a status of 200" in {
      status(result) shouldBe 200
    }

    "return some html with " in {
      contentType(result) shouldBe Some("text/html")
    }

    s"return some html with title of ${messages.title}" in {
      Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
    }
  }
}
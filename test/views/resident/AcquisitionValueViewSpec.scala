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

package views.resident

import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.{MessageLookup => commonMessages}
import assets.MessageLookup.{acquisitionValue => messages}
import controllers.helpers.FakeRequestHelper

class AcquisitionValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Acquisition Value view" should {

    lazy val view = views.html.calculation.resident.acquisitionValue()(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have title ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    "have a back button that" should {

      lazy val backLink = doc.select("a#back-link")

      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.calcBaseBack
      }

      "have the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "have a link to Disposal Value" in {
        backLink.attr("href") shouldBe controllers.resident.routes.GainController.disposalCosts().toString
      }
    }

    "have a H1 tag that" should {
      lazy val h1Tag = doc.select("H1")

      s"have the page heading '${messages.pageHeading}'" in {
        h1Tag.text shouldBe messages.pageHeading
      }

      "have the visuallyhidden class" in {
        h1Tag.hasClass("visuallyhidden") shouldBe true
      }
    }
  }
}

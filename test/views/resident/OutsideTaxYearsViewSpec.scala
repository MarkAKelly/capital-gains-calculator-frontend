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

import assets.MessageLookup.{outsideTaxYears => messages}
import assets.{MessageLookup => commonMessages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class OutsideTaxYearsViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Outside tax years views" should {
    lazy val view = views.html.calculation.resident.gain.outsideTaxYear()(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"return a title of ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    s"have a heading of ${messages.title}" in {
      doc.select("h1").text() shouldBe messages.title
    }

    s"have a message of ${messages.content("2015/16")}" in {
      doc.select("p.lede").text() shouldBe messages.content("2015/16")
    }

    "have a back link that" should {
      lazy val backLink = doc.select("a#back-link")

      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.calcBaseBack
      }

      "have the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "have a link to Disposal Value" in {
        backLink.attr("href") shouldBe controllers.resident.routes.GainController.disposalDate().toString
      }
    }

    "have a continue button that" should {
      lazy val button = doc.select("a#continue-button")

      "have the correct text 'Continue'" in {
        button.text() shouldBe commonMessages.calcBaseContinue
      }

      s"have an href to ${controllers.resident.routes.GainController.disposalValue().toString()}" in {
        button.attr("href") shouldBe controllers.resident.routes.GainController.disposalValue().toString()
      }
    }
  }
}
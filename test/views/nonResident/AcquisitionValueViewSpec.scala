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

package views.nonResident

import controllers.nonresident.routes
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import forms.nonresident.AcquisitionValueForm._
import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{AcquisitionValue => messages}
import controllers.helpers.FakeRequestHelper
import views.html.calculation.nonresident.acquisitionValue


class AcquisitionValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper{


  "the Acquisition Value View" should {

    lazy val view = acquisitionValue(acquisitionValueForm)(fakeRequest)
    lazy val document = Jsoup.parse(view.body)

    "have the title 'How much did you pay for the property?'" in {
      document.title shouldEqual messages.question
    }

    s"have the heading ${commonMessages.pageHeading}" in {
      document.body.getElementsByTag("h1").text shouldEqual commonMessages.pageHeading
    }

    "have a 'Back link' that" should{

      s"have the text of ${commonMessages.back}" in {
        document.body.getElementById("back-link").text shouldEqual commonMessages.back
      }

      s"have a link to ${routes.AcquisitionDateController.acquisitionDate()}" in {
       document.body.getElementById("back-link").attr("href") shouldEqual routes.AcquisitionDateController.acquisitionDate().toString()
      }
    }

    "have the question 'How much did you pay for the property?'" in {
      document.body.getElementsByTag("label").text should include (messages.question)
    }

    "have the bullet list content title and content" in {
      document.select("p#bullet-list-title").text shouldEqual messages.bulletTitle
    }

    s"Have the bullet content of ${messages.bulletOne}" in {
      document.select("ul li").text should include(messages.bulletOne)
    }

    s"Have the bullet content of ${messages.bulletTwo}" in {
      document.select("ul li").text should include(messages.bulletTwo)
    }

    s"Have the bullet content of ${messages.bulletThree}" in {
      document.select("ul li").text should include(messages.bulletThree)
    }

    s"Have the bullet content of ${messages.bulletFour}" in {
      document.select("ul li").text should include(messages.bulletFour)
    }

    s"Have the bullet content of ${messages.bulletFive}" in {
      document.select("ul li").text should include(messages.bulletFive)
    }

    s"have a link with a hidden external link field that contains the text of ${messages.bulletLink}" in {
      document.select("ul li a#lossesLink").text should include(messages.bulletLink)
    }

    s"have a link that contains the text of ${commonMessages.externalLink}" in {
      document.select("span#opensInANewTab").text shouldEqual commonMessages.externalLink
    }

    "display an input box for the Acquisition Value" in {
      document.body.getElementById("acquisitionValue").tagName shouldEqual "input"
    }

    "have no value auto-filled into the input box" in {
      document.getElementById("acquisitionValue").attr("value") shouldEqual ""
    }

    "display a 'Continue' button " in {
      document.body.getElementById("continue-button").text shouldEqual commonMessages.continue
    }
  }
}
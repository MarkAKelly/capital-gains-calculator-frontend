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
import views.html.calculation.{nonresident => views}


class AcquisitionValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper{


  "the Acquisition Value View" should {

    lazy val view = views.acquisitionValue(acquisitionValueForm)(fakeRequest)
    lazy val document = Jsoup.parse(view.body)

    "have a h1 tag that" should {

      s"have the heading ${commonMessages.pageHeading}" in {
        document.select("h1").text shouldEqual commonMessages.pageHeading
      }

      "have the heading-large class" in {
        document.select("h1").hasClass("heading-large") shouldBe true
      }
    }

    "have the title 'How much did you pay for the property?'" in {
      document.title shouldEqual messages.question
    }

    "have the home link to 'home'" in {
      document.select("#homeNavHref").attr("href") shouldBe "/calculate-your-capital-gains/non-resident/"
    }

    "have a 'Back link' that" should{

      s"have the text of ${commonMessages.back}" in {
        document.select("a#back-link").text shouldEqual commonMessages.back
      }

      s"have a link to ${routes.AcquisitionDateController.acquisitionDate()}" in {
       document.select("a#back-link").attr("href") shouldEqual routes.AcquisitionDateController.acquisitionDate().toString()
      }

      "has the back-link class" in {
        document.select("a#back-link").hasClass("back-link") shouldBe true
      }
    }

    "render a form tag" which {

      lazy val form = document.select("form")

      "has a submit action" in {
        form.attr("action") shouldEqual "/calculate-your-capital-gains/non-resident/acquisition-value"
      }

      "with method type POST" in {
        form.attr("method") shouldBe "POST"
      }
    }

    "have the question 'How much did you pay for the property?'" in {
      document.body.getElementsByTag("label").text should include (messages.question)
    }

    "have a bullet list that" should {

      "have a bullet list class present" in {
        document.select("ul").hasClass("list-bullet") shouldBe true
      }

      "have the bullet list content title and content" in {
        document.select("p#bullet-list-title").text shouldEqual messages.bulletTitle
      }

      s"Have the content of ${messages.bulletOne}" in {
        document.select("ul li").text should include(messages.bulletOne)
      }

      s"Have the content of ${messages.bulletTwo}" in {
        document.select("ul li").text should include(messages.bulletTwo)
      }

      s"Have the content of ${messages.bulletThree}" in {
        document.select("ul li").text should include(messages.bulletThree)
      }

      s"Have the content of ${messages.bulletFour}" in {
        document.select("ul li").text should include(messages.bulletFour)
      }

      s"Have the content of ${messages.bulletFive}" in {
        document.select("ul li").text should include(messages.bulletFive)
      }
    }

    "have a link that" should {

      s"have a hidden external link field that contains the text of ${messages.bulletLink}" in {
        document.select("ul li a#lossesLink").text should include(messages.bulletLink)
      }

      s"contains the text of ${commonMessages.externalLink}" in {
        document.select("span#opensInANewTab").text shouldEqual commonMessages.externalLink
      }

      "have the class of external-link" in {
        document.select("ul li a#lossesLink").hasClass("external-link") shouldBe true
      }
    }

    "display an input box for the Acquisition Value" in {
      document.select("input").attr("id") shouldBe "acquisitionValue"
    }

    "have a button that" should {
      lazy val button = document.select("button")

      s"have the message of ${commonMessages.continue}" in {
        button.text shouldEqual commonMessages.continue
      }

      "have the class 'button'" in {
        button.attr("class") shouldBe "button"
      }

      "have the type 'submit'" in {
        button.attr("type") shouldBe "submit"
      }

      "have the id 'continue-button'" in {
        button.attr("id") shouldBe "continue-button"
      }
    }

    "supplied with errors" should {
      lazy val form = acquisitionValueForm.bind(Map("acquisitionValue" -> "a"))
      lazy val view = views.acquisitionValue(form)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}
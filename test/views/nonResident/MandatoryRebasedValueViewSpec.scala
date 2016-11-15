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

import controllers.helpers.FakeRequestHelper
import models.nonresident.RebasedValueModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{nonresident => views}
import forms.nonresident.RebasedValueForm._
import org.jsoup.Jsoup
import assets.MessageLookup.NonResident.{RebasedValue => messages}
import assets.MessageLookup.{NonResident => commonMessages}

class MandatoryRebasedValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {



  "The Mandatory Rebased Value View" should {

    lazy val mandatoryRebasedValueForm = rebasedValueForm
    lazy val view = views.mandatoryRebasedValue(mandatoryRebasedValueForm)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title ${messages.inputQuestionMandatory}" in {
      doc.title shouldBe messages.inputQuestionMandatory
    }

    "have a H1 tag that" should {

      lazy val h1Tag = doc.select("h1")

      s"have the page heading '${commonMessages.pageHeading}'" in {
        h1Tag.text shouldBe commonMessages.pageHeading
      }

      "have the heading-large class" in {
        h1Tag.hasClass("heading-large") shouldBe true
      }
    }

    s"have a home link to '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      doc.select("#homeNavHref").attr("href") shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }

    "have a back button" which {

      lazy val backLink = doc.select("a#back-link")

      "has the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "has the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "has a back link to 'back'" in {
        backLink.attr("href") shouldBe "/calculate-your-capital-gains/non-resident/acquisition-value"
      }
    }

    "render a form tag" which {

      lazy val form = doc.select("form")

      "has a submit action" in {
        form.attr("action") shouldEqual "/calculate-your-capital-gains/non-resident/rebased-value"
      }

      "with method type POST" in {
        form.attr("method") shouldBe "POST"
      }
    }

    "have a hidden Yes input" which {

      lazy val dummyInput = doc.select("#hasRebasedValue")

      "should have the name hasRebasedValue" in {
        dummyInput.attr("name") shouldBe "hasRebasedValue"
      }

      "should have the value 'Yes'" in {
        dummyInput.attr("value") shouldBe "Yes"
      }

      "should be hidden" in {
        dummyInput.hasClass("visuallyhidden") shouldBe true
      }
    }

    "have an input for the amount" which {

      s"has a label with text ${messages.inputQuestionMandatory}" in {
        doc.body.select("label > div > span").text shouldEqual messages.inputQuestionMandatory
      }

      s"should be of tag type input" in {
        doc.body.getElementById("rebasedValueAmt").tagName() shouldEqual "input"
      }
    }

    "have a continue button" which {

      lazy val button = doc.select("button")

      "has class 'button'" in {
        button.hasClass("button") shouldEqual true
      }

      "has attribute 'type'" in {
        button.hasAttr("type") shouldEqual true
      }

      "has type value of 'submit'" in {
        button.attr("type") shouldEqual "submit"
      }

      "has attribute id" in {
        button.hasAttr("id") shouldEqual true
      }

      "has id equal to continue-button" in {
        button.attr("id") shouldEqual "continue-button"
      }

      s"has the text ${commonMessages.continue}" in {
        button.text shouldEqual s"${commonMessages.continue}"
      }
    }
  }
}

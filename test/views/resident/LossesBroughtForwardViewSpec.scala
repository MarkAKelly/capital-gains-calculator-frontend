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

import assets.MessageLookup
import assets.MessageLookup.{lossesBroughtForward => messages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{resident => views}
import forms.resident.LossesBroughtForwardForm._

class LossesBroughtForwardViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Reliefs view" should {

    lazy val view = views.lossesBroughtForward(lossesBroughtForwardForm, "")(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    s"have a back link with text ${MessageLookup.calcBaseBack}" in {
      doc.select("#back-link").text shouldEqual MessageLookup.calcBaseBack
    }

    s"have the question of the page ${messages.question}" in {
      doc.select("h1").text() shouldEqual messages.question
    }

    s"render a form tag with a POST action" in {
      doc.select("form").attr("method") shouldEqual "POST"
    }

    s"have a visually hidden legend for an input with text ${messages.question}" in {
      doc.select("legend.visuallyhidden").text() shouldEqual messages.question
    }

    s"have an input field with id option-yes " in {
      doc.body.getElementById("option-yes").tagName() shouldEqual "input"
    }

    s"have an input field with id option-no " in {
      doc.body.getElementById("option-no").tagName() shouldEqual "input"
    }

    "have a continue button " in {
      doc.body.getElementById("continue-button").text shouldEqual MessageLookup.calcBaseContinue
    }
  }

  "Losses Brought Forward view with pre-selected value of yes" should {
    lazy val form = lossesBroughtForwardForm.bind(Map(("option", "Yes")))
    lazy val view = views.lossesBroughtForward(form, "")(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have the option 'Yes' auto selected" in {
      doc.body.getElementById("option-yes").parent.className should include("selected")
    }
  }

  "Losses Brought Forward view with pre-selected value of no" should {
    lazy val form = lossesBroughtForwardForm.bind(Map(("option", "No")))
    lazy val view = views.lossesBroughtForward(form, "")(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have the option 'No' auto selected" in {
      doc.body.getElementById("option-no").parent.className should include("selected")
    }
  }

  "Losses Brought Forward view with errors" should {
    lazy val form = lossesBroughtForwardForm.bind(Map(("option", "")))
    lazy val view = views.lossesBroughtForward(form, "")(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#option-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select("span.error-notification").size shouldBe 1
    }
  }
}

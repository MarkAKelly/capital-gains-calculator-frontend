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

import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}
import assets.MessageLookup.{otherProperties => messages}
import assets.MessageLookup._
import views.html.calculation.{resident => views}
import forms.resident.OtherPropertiesForm._

class OtherPropertiesViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Other Properties view" should {

    lazy val view = views.otherProperties(otherPropertiesForm)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title of ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    "have an input field with id hasOtherProperties-yes" in {
      doc.body.getElementById("hasOtherProperties-yes").tagName() shouldEqual "input"
    }

    "have an input field with id hasOtherProperties-no" in {
      doc.body.getElementById("hasOtherProperties-no").tagName() shouldEqual "input"
    }

    s"have the help text ${messages.help}" in {
      doc.body.select("span.form-hint p").text shouldBe messages.help
    }

    s"have the help text ${messages.helpOne}" in {
      doc.body.select("ul.list-bullet li").get(0).text shouldBe messages.helpOne
    }

    s"have the help text ${messages.helpTwo}" in {
      doc.body.select("ul.list-bullet li").get(1).text shouldBe messages.helpTwo
    }

    s"have the help text ${messages.helpThree}" in {
      doc.body.select("ul.list-bullet li").get(2).text shouldBe messages.helpThree
    }

    "have a continue button " in {
      doc.body.getElementById("continue-button").text shouldEqual calcBaseContinue
    }

    "have a back button that" should {

      lazy val backLink = doc.select("a#back-link")

      "have the correct back link text" in {
        backLink.text shouldBe calcBaseBack
      }

      "have the correct back link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "have the correct back link" in {
        //merge other reliefs first
      }
    }

    "have a form" which {

      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.resident.routes.DeductionsController.submitOtherProperties().toString}'" in {
        form.attr("action") shouldBe controllers.resident.routes.DeductionsController.submitOtherProperties().toString()
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }

      s"have a legend for an input with text ${messages.pageHeading}" in {
        doc.select("legend.visuallyhidden").text() shouldEqual messages.pageHeading
      }
    }

    "Other Properties view with pre-selected values" should {

      lazy val form = otherPropertiesForm.bind(Map(("hasOtherProperties", "Yes")))
      lazy val view = views.otherProperties(form)(fakeRequest)
      lazy val doc = Jsoup.parse(view.body)

      "have the option 'Yes' auto selected" in {
        doc.body.getElementById("hasOtherProperties-yes").parent.className should include("selected")
      }
    }

    "Other Properties view with errors" should {

      lazy val form = otherPropertiesForm.bind(Map(("hasOtherProperties", "")))
      lazy val view = views.otherProperties(form)(fakeRequest)
      lazy val doc = Jsoup.parse(view.body)

      "display an error summary message for the amount" in {
        doc.body.select("#hasOtherProperties-error-summary").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select("span.error-notification").size shouldBe 1
      }
    }
  }
}

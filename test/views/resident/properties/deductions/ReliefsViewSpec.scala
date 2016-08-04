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

package views.resident.properties.deductions

import assets.MessageLookup
import assets.MessageLookup.{reliefs => messages}
import controllers.helpers.FakeRequestHelper
import forms.resident.properties.ReliefsForm._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{deductions => views}

class ReliefsViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Reliefs view" should {

    lazy val view = views.reliefs(reliefsForm(), "home-link", false, Some(controllers.resident.properties.routes.DeductionsController.privateResidenceRelief.toString()))(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    s"have a back link to the PRR page with text ${MessageLookup.calcBaseBack}" in {
      doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/private-residence-relief"
    }

    s"have the question of the page ${messages.title}" in {
      doc.select("h1").text() shouldEqual messages.title
    }

    "render a form tag with a submit action" in {
      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/properties/reliefs"
    }

    s"have a legend for an input with text ${messages.title}" in {
      doc.select("legend.visuallyhidden").text() shouldEqual messages.title
    }

    "have no help text" in {
      doc.select("span.form-hint").isEmpty shouldEqual true
    }

    s"have an input field with id isClaiming-yes " in {
      doc.body.getElementById("isClaiming-yes").tagName() shouldEqual "input"
    }

    s"have an input field with id isClaiming-no " in {
      doc.body.getElementById("isClaiming-no").tagName() shouldEqual "input"
    }

    "have a continue button " in {
      doc.body.getElementById("continue-button").text shouldEqual MessageLookup.calcBaseContinue
    }
  }

  "Reliefs view with pre-selected values and a having claimed PRR" should {
    lazy val form = reliefsForm().bind(Map(("isClaiming", "Yes")))
    lazy val view = views.reliefs(form, "home-link", true, Some(controllers.resident.properties.routes.DeductionsController.privateResidenceReliefValue.toString()))(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have the option 'Yes' auto selected" in {
      doc.body.getElementById("isClaiming-yes").parent.className should include("selected")
    }

    s"have a title ${messages.question}" in {
      doc.title() shouldBe messages.question
    }

    s"have a back link to the PRR value page with text ${MessageLookup.calcBaseBack}" in {
      doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/private-residence-relief-value"
    }

    s"have the question of the page ${messages.question}" in {
      doc.select("h1").text() shouldEqual messages.question
    }

    s"have a legend for an input with text ${messages.question}" in {
      doc.select("legend.visuallyhidden").text() shouldEqual messages.question
    }

    s"have help text with the message ${messages.help}" in {
      doc.select("span.form-hint").text() shouldEqual messages.help
    }
  }

  "Reliefs view with errors" should {
    lazy val form = reliefsForm().bind(Map(("isClaiming", "")))
    lazy val view = views.reliefs(form, "home-link", false, Some(controllers.resident.properties.routes.DeductionsController.privateResidenceRelief.toString()))(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    s"have a back link to the PRR page with text ${MessageLookup.calcBaseBack}" in {
      doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/private-residence-relief"
    }

    "display an error summary message for the amount" in {
      doc.body.select("#isClaiming-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select("span.error-notification").size shouldBe 1
    }
  }
}



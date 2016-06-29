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
import assets.MessageLookup.{reliefs => messages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{resident => views}
import forms.resident.ReliefsForm._

class ReliefsViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Reliefs view" should {

    lazy val view = views.reliefs(reliefsForm)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    s"have a back link to the Disposal Date Page with text ${MessageLookup.calcBaseBack}" in {
      doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/improvements"
    }

    s"have the question of the page ${messages.question}" in {
      doc.select("h1").text() shouldEqual messages.question
    }

    "render a form tag with a submit action" in {
      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/reliefs"
    }

    s"have a legend for an input with text ${messages.question}" in {
      doc.select("legend.visuallyhidden").text() shouldEqual messages.question
    }

    s"have help text with the message ${messages.help}" in {
      doc.select("span.form-hint").text() shouldEqual messages.help
    }

    s"have an input field with id isClaiming-yes " in {
      doc.body.getElementById("isClaiming-yes").tagName() shouldEqual "input"
    }

    s"have an input field with id isClaiming-no " in {
      doc.body.getElementById("isClaiming-no").tagName() shouldEqual "input"
    }
  }
}



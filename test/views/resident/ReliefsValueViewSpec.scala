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
import assets.MessageLookup.{reliefsValue => messages}
import controllers.helpers.FakeRequestHelper
import forms.resident.ReliefsValueForm._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident._

class ReliefsValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Reliefs Value view" should {

    lazy val form = reliefsValueForm.bind(Map("amount" -> "10"))
    lazy val view = reliefsValue(form)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    s"have a back link to the Reliefs Page with text ${MessageLookup.calcBaseBack}" in {
      doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/reliefs"
    }

    s"have the text ${messages.question} as the h1 tag" in {
      doc.select("h1").text shouldEqual messages.question
    }

    "render a form element" in {
      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/reliefs-value"
    }

    s"have a hidden legend with the text ${messages.question}" in {
      doc.select("label span.visuallyhidden").text shouldEqual messages.question
    }

    "render an input field for the reliefs amount" in {
      doc.select("input").attr("id") shouldBe "amount"
    }

    "not display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 0
    }

    "not display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 0
    }

    "have continue button " in {
      doc.body.getElementById("continue-button").text shouldEqual MessageLookup.calcBaseContinue
    }
  }

  "Improvements View with form with errors" should {

    val form = reliefsValueForm.bind(Map("amount" -> ""))
    lazy val view = reliefsValue(form)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }
}
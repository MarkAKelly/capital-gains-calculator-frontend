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

class ReliefsValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Reliefs Value view" should {

    lazy val form = reliefsValueForm.bind(Map("amount" -> "10"))
    lazy val view = views.html.calculation.resident.reliefsValue(form)(fakeRequest)
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

    s"have the question of the page ${messages.question}" in {
      doc.select("h1").text shouldEqual messages.question
    }

    "render a form element" in {
      
    }

    s"have a hidden legend with the text ${messages.question}" in {
      doc.select("label.visuallyhidden").text shouldEqual messages.question
    }

    "render an input field for the reliefs amount" in {
      doc.select("#amount").toString() shouldBe "<input type=\"number\" class=\"moneyField  input--no-spinner \" placeholder=\"eg. 25000.00\" name=\"amount\" id=\"amount\" value=\"10\" step=\"0.01\">"
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
}
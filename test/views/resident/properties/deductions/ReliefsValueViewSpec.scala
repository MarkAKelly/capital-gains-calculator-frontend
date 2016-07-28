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
import assets.MessageLookup.{reliefsValue => messages}
import controllers.helpers.FakeRequestHelper
import forms.resident.properties.ReliefsValueForm._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{deductions => views}

class ReliefsValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Reliefs Value view" should {

    lazy val form = reliefsValueForm.bind(Map("amount" -> "10"))
    lazy val view = views.reliefsValue(form, "home-link")(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    s"have a back link to the Reliefs Page with text ${MessageLookup.calcBaseBack}" in {
      doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/reliefs"
    }

    s"have the text ${messages.question} as the h1 tag" in {
      doc.select("h1").text shouldEqual messages.question
    }

    "render a form element" in {
      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/properties/reliefs-value"
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

    "have a Read more link for PRR" should {

      lazy val linkOne = doc.select("a#reliefsValuePrivateResidenceReliefLink")

      s"contain the text ${messages.prrLink}" in {
        linkOne.text should include(messages.prrLink)
      }

      "contain a link to 'https://www.gov.uk/tax-sell-home/absence-from-home'" in {
        linkOne.attr("href") shouldEqual "https://www.gov.uk/tax-sell-home/absence-from-home"
      }

      s"contain a visually-hidden legend with text ${messages.prrLink}" in {
        linkOne.select("span.visuallyhidden").text shouldEqual messages.prrLink
      }
    }

    "have a Read more link for Lettings Relief" should {

      lazy val linkOne = doc.select("a#reliefsValueLettingsReliefLink")

      s"contain the text ${messages.lettingsReliefLink}" in {
        linkOne.text should include(messages.lettingsReliefLink)
      }

      "contain a link to 'https://www.gov.uk/tax-sell-home/let-out-part-of-home'" in {
        linkOne.attr("href") shouldEqual "https://www.gov.uk/tax-sell-home/let-out-part-of-home"
      }

      s"contain a visually-hidden legend with text ${messages.lettingsReliefLink}" in {
        linkOne.select("span.visuallyhidden").text shouldEqual messages.lettingsReliefLink
      }
    }
  }

  "Reliefs Value View with form without errors" should {

    val form = reliefsValueForm.bind(Map("amount" -> "100"))
    lazy val view = views.reliefsValue(form, "home-link")(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "display the value of the form" in {
      doc.body.select("#amount").attr("value") shouldEqual "100"
    }

    "display no error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 0
    }

    "display no error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 0
    }
  }

  "Reliefs Value View with form with errors" should {

    val form = reliefsValueForm.bind(Map("amount" -> ""))
    lazy val view = views.reliefsValue(form, "home-link")(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }
}
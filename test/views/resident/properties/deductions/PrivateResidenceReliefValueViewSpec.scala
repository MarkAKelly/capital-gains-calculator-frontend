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
import assets.MessageLookup.{privateResidenceReliefValue => messages}
import controllers.helpers.FakeRequestHelper
import forms.resident.properties.PrivateResidenceReliefValueForm._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{deductions => views}

class PrivateResidenceReliefValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Private Residence Relief Value view" should {

    lazy val form = privateResidenceReliefValueForm(BigDecimal(400)).bind(Map("amount" -> "10"))
    lazy val view = views.privateResidenceReliefValue(form, BigDecimal(400), "home-link")(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title(400)}" in {
      doc.title shouldBe messages.title(400)
    }

    s"have the text ${messages.title(400)} as the h1 tag" in {
      doc.select("h1").text shouldEqual messages.title(400)
    }

    s"have a hidden legend with the text ${messages.title(400)}" in {
      doc.select("label span.visuallyhidden").text shouldEqual messages.title(400)
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

      lazy val linkOne = doc.select("a#privateResidenceReliefLink")

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
  }

  "Private Residence Relief Value View with form without errors" should {

    val form = privateResidenceReliefValueForm(BigDecimal(400)).bind(Map("amount" -> "100"))
    lazy val view = views.privateResidenceReliefValue(form, BigDecimal(400), "home-link")(fakeRequest)
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

  "Private Residence Relief Value View with form with errors" should {

    val form = privateResidenceReliefValueForm(BigDecimal(400)).bind(Map("amount" -> ""))
    lazy val view = views.privateResidenceReliefValue(form, BigDecimal(400), "home-link")(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }
}
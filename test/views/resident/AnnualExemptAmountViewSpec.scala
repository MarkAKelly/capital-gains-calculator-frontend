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
import assets.MessageLookup.{annualExemptAmount => messages}
import assets.{MessageLookup => commonMessages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{resident => views}
import forms.resident.AnnualExemptAmountForm._


class AnnualExemptAmountViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {
  "The Annual Exempt Amount view" should {
    lazy val view = views.annualExemptAmount(annualExemptAmountForm)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)
    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }
    s"have a title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }
    "have a back button that" should {
      lazy val backLink = doc.select("a#back-link")
      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.calcBaseBack
      }
      "have the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }
      "have a link to Brought Forward Losses" in {
        backLink.attr("href") shouldBe controllers.resident.routes.DeductionsController.lossesBroughtForward().toString
      }
    }
    "have a H1 tag that" should {
      lazy val h1Tag = doc.select("H1")
      s"have the page heading '${messages.title}'" in {
        h1Tag.text shouldBe messages.title
      }
      "have the heading-large class" in {
        h1Tag.hasClass("heading-large") shouldBe true
      }
    }
    "have a form" which {
      lazy val form = doc.getElementsByTag("form")
      s"has the action '${controllers.resident.routes.DeductionsController.submitAnnualExemptAmount().toString}'" in {
        form.attr("action") shouldBe controllers.resident.routes.DeductionsController.submitAnnualExemptAmount().toString
      }
      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }

      s"have a legend for an input with text ${messages.question}" in {
        doc.body.getElementsByClass("heading-large").text() shouldEqual messages.question
      }


      "has help one text that" should {
        s"have the text ${messages.help}" in {
          doc.body.getElementsByClass("form-hint").text contains messages.help
        }
      }

      "has help two text that" should {
        s"have the text ${messages.helpOne}" in {
          doc.body.getElementsByClass("form-hint").text contains messages.helpOne
        }
      }

      s"the Annual Exempt Amount Help link ${MessageLookup.annualExemptAmount.helpLinkOne} should " +
        "have the address Some(https://www.gov.uk/capital-gains-tax/losses)" in {
        doc.select("a#annualExemptAmountLink").attr("href") shouldEqual "https://www.gov.uk/capital-gains-tax/allowances"
      }

      "has a numeric input field" which {
        lazy val input = doc.body.getElementsByTag("input")
        "has the id 'amount'" in {
          input.attr("id") shouldBe "amount"
        }
        "has the name 'amount'" in {
          input.attr("name") shouldBe "amount"
        }
        "is of type number" in {
          input.attr("type") shouldBe "number"
        }
        "has a step value of '0.01'" in {
          input.attr("step") shouldBe "0.01"
        }
        s"has placeholder 'eg. 25000.00'" in {
          input.attr("placeholder") shouldBe "eg. 25000.00"
        }
      }
    }
    "have a continue button that" should {
      lazy val continueButton = doc.select("button#continue-button")
      s"have the button text '${commonMessages.calcBaseContinue}'" in {
        continueButton.text shouldBe commonMessages.calcBaseContinue
      }
      "be of type submit" in {
        continueButton.attr("type") shouldBe "submit"
      }
      "have the class 'button'" in {
        continueButton.hasClass("button") shouldBe true
      }
    }
  }
  "Annual Exempt Amount View with form with errors" which {
    "is due to mandatory field error" should {
      val form = annualExemptAmountForm.bind(Map("amount" -> ""))
      lazy val view = views.annualExemptAmount(form)(fakeRequest)
      lazy val doc = Jsoup.parse(view.body)
      s"output an error summary with message '${commonMessages.undefinedMessage}'" in {
        doc.body.getElementById("amount-error-summary").text should include(commonMessages.undefinedMessage)
      }
      s"have the input error message '${commonMessages.undefinedMessage}'" in {
        doc.body.getElementsByClass("error-notification").text should include (commonMessages.undefinedMessage)
      }
    }
  }
}

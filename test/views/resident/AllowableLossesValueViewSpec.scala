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

import assets.{MessageLookup => commonMessages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{allowableLossesValue => messages}
import forms.resident.AllowableLossesValueForm._

class AllowableLossesValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Allowable Losses Value view with no form errors" should {

    lazy val view = views.html.calculation.resident.allowableLossesValue(allowableLossesValueForm)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title of ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    "have a back button" which {

      lazy val backLink = doc.select("a#back-link")

      "has the correct back link text" in {
        backLink.text shouldBe commonMessages.calcBaseBack
      }

      "has the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "has a link to Allowable Losses" in {
        backLink.attr("href") shouldBe controllers.resident.routes.DeductionsController.allowableLosses().toString
      }
    }

    "have a H1 tag that" should {

      lazy val h1Tag = doc.select("H1")

      s"have the page heading '${messages.question}'" in {
        h1Tag.text shouldBe messages.question
      }

      "have the heading-large class" in {
        h1Tag.hasClass("heading-large") shouldBe true
      }
    }

    "have a form" which {

      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.resident.routes.DeductionsController.submitAllowableLossesValue().toString}'" in {
        form.attr("action") shouldBe controllers.resident.routes.DeductionsController.submitAllowableLossesValue().toString
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }

      "has a label that" should {

        lazy val label = doc.body.getElementsByTag("label")

        s"have the question ${messages.question}" in {
          label.text should include(messages.question)
        }

        "have the class 'visuallyhidden'" in {
          label.select("span.visuallyhidden").size shouldBe 1
        }
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

  "Allowable Losses Value View with form with errors" should {
    val form = allowableLossesValueForm.bind(Map("amount" -> ""))
    lazy val view = views.html.calculation.resident.allowableLossesValue(form)(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)

    "output an error summary" in {
      doc.body.getElementsByAttributeValueContaining("id", "amount-error-summary").isEmpty shouldBe false
    }

    s"contain an error summary message of ${commonMessages.errorMessages.mandatoryAmount}" in {
      doc.body.getElementById("amount-error-summary").text should include(commonMessages.errorMessages.mandatoryAmount)
    }

    "output an error notification" in {
      doc.body.getElementsByAttributeValueContaining("class", "error-notification").isEmpty shouldBe false
    }

    s"contain an error notification message of ${commonMessages.errorMessages.mandatoryAmount}" in {
      doc.body.getElementsByClass("error-notification").text should include(commonMessages.errorMessages.mandatoryAmount)
    }
  }
}

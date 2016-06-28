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

import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{acquisitionCosts => messages}
import forms.resident.AcquisitionCostsForm._
import assets.{MessageLookup => commonMessages}
import controllers.helpers.FakeRequestHelper


class AcquisitionCostsViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Acquisition Costs view" should {

    lazy val view = views.html.calculation.resident.acquisitionCosts(acquisitionCostsForm)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title of ${messages.title}" in {
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

      "have a link to Acquisition Value" in {
        backLink.attr("href") shouldBe controllers.resident.routes.GainController.acquisitionValue().toString
      }
    }

    "have a H1 tag that" should {

      lazy val h1Tag = doc.select("H1")

      s"have the page heading '${messages.pageHeading}'" in {
        h1Tag.text shouldBe messages.pageHeading
      }

      "have the visuallyhidden class" in {
        h1Tag.hasClass("visuallyhidden") shouldBe true
      }
    }

    "have a form" which {

      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.resident.routes.GainController.submitAcquisitionCosts().toString}'" in {
        form.attr("action") shouldBe controllers.resident.routes.GainController.submitAcquisitionCosts().toString
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }

      "has a label that" should {

        lazy val label = doc.body.getElementsByTag("label")

        s"have the question ${messages.pageHeading}" in {
          label.text should include(messages.pageHeading)
        }

        "have the class 'heading-large'" in {
          label.select("span").hasClass("heading-large") shouldBe true
        }
      }

      "has help text that" should {

        s"have the text ${messages.helpText}" in {
          doc.body.getElementsByClass("form-hint").text shouldBe messages.helpText
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

  "Acquisition Costs View with form with errors" which {

    "is due to mandatory field error" should {

      val form = acquisitionCostsForm.bind(Map("amount" -> ""))
      lazy val view = views.html.calculation.resident.acquisitionCosts(form)(fakeRequest)
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
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
import assets.{MessageLookup => commonMessages}
import assets.MessageLookup.{acquisitionValue => messages}
import controllers.helpers.FakeRequestHelper
import forms.resident.AcquisitionValueForm._

class AcquisitionValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Acquisition Value view" should {

    lazy val view = views.html.calculation.resident.acquisitionValue(acquisitionValueForm)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have title ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    "have a back button that" should {

      lazy val backLink = doc.select("a#back-link")

      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.calcBaseBack
      }

      "have the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "have a link to Disposal Value" in {
        backLink.attr("href") shouldBe controllers.resident.routes.GainController.disposalCosts().toString
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

      s"has the action '${controllers.resident.routes.GainController.submitAcquisitionValue().toString}'" in {
        form.attr("action") shouldBe controllers.resident.routes.GainController.submitAcquisitionValue().toString
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

      "has additional content that" should {

        s"have a bullet point list title of ${messages.bulletListTitle}" in {
          doc.select("div.indent p#bullet-list-title").text() shouldEqual messages.bulletListTitle
        }

        s"have a first bullet point of ${messages.bulletListOne}" in {
          doc.select("div.indent li#bullet-list-one").text() shouldEqual messages.bulletListOne
        }

        s"have a second bullet point of ${messages.bulletListTwo}" in {
          doc.select("div.indent li#bullet-list-two").text() shouldEqual messages.bulletListTwo
        }

        s"have a third bullet point of ${messages.bulletListThree} with link text ${messages.bulletListThreeLink}" in {
          doc.select("div.indent li#bullet-list-three").text() shouldEqual messages.bulletListThree +
            " " + messages.bulletListThreeLink + " " + commonMessages.calcBaseExternalLink
        }

        s"have a third bullet point link ${messages.bulletListThreeLink} with a visually hidden content span" in {
          doc.select("span.visuallyhidden").text() shouldEqual commonMessages.calcBaseExternalLink
        }

        s"have a link to ${messages.bulletLink}" in {
          doc.getElementById("lossesLink").attr("href") shouldBe messages.bulletLink
        }

        s"have a fourth bullet point of ${messages.bulletListFour}" in {
          doc.select("div.indent li#bullet-list-four").text() shouldEqual messages.bulletListFour
        }

        s"have a fifth bullet point of ${messages.bulletListFive}" in {
          doc.select("div.indent li#bullet-list-five").text() shouldEqual messages.bulletListFive
        }
      }

      "has a numeric input field that" should {

        lazy val input = doc.body.getElementsByTag("input")

        "have the id 'amount'" in {
          input.attr("id") shouldBe "amount"
        }

        "have the name 'amount'" in {
          input.attr("name") shouldBe "amount"
        }

        "have a type of number" in {
          input.attr("type") shouldBe "number"
        }

        "have a step value of '0.01'" in {
          input.attr("step") shouldBe "0.01"
        }

        s"have placeholder 'eg. 25000.00'" in {
          input.attr("placeholder") shouldBe "eg. 25000.00"
        }
      }

      "has a continue button that" should {

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
  }

  "Acquisition Value View with form with errors" should {
    val form = acquisitionValueForm.bind(Map("amount" -> ""))
    lazy val view = views.html.calculation.resident.acquisitionValue(form)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "output an error summary" in {
      doc.body.getElementsByAttributeValueContaining("id", "amount-error-summary").isEmpty shouldBe false
    }

    s"contain an error summary message of ${commonMessages.undefinedMessage}" in {
      doc.body.getElementById("amount-error-summary").text should include(commonMessages.undefinedMessage)
    }

    "output an error notification" in {
      doc.body.getElementsByAttributeValueContaining("class", "error-notification").isEmpty shouldBe false
    }

    s"contain an error notification message of ${commonMessages.undefinedMessage}" in {
      doc.body.getElementsByClass("error-notification").text should include(commonMessages.undefinedMessage)
    }
  }
}
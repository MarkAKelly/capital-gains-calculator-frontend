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

package views.nonResident

import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{RebasedValue => messages}
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.routes
import forms.nonresident.RebasedValueForm._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.rebasedValue

class RebasedValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "The rebased value view" when {

    "not supplied with a pre-existing stored model and no acquisition date" should {

      lazy val view = rebasedValue(rebasedValueForm, "No")(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"Have the title ${messages.question}" in {
        document.title shouldEqual messages.question
      }

      s"Have the heading '${commonMessages.pageHeading}" in {
        document.getElementsByTag("h1").text shouldBe commonMessages.pageHeading
      }

      s"have the question ${messages.question}" in {
        document.select("#hasRebasedValue").text should include(messages.question)
      }

      s"have help text with the wording${messages.questionHelpText}" in {
        document.getElementsByClass("form-hint").text should include(messages.questionHelpText)
      }

      s"contain an input with the question ${messages.inputQuestion}" in {
        document.getElementById("rebasedValueAmt").parent.text should include(messages.inputQuestion)
      }

      s"contain an input with the help text with the wording ${messages.inputHelpText}" in {
        document.getElementsByClass("form-hint").text should include(messages.inputHelpText)
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "back-link"
        }

        "has the text" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a route to 'customer-type'" in {
          backLink.attr("href") shouldBe routes.AcquisitionValueController.acquisitionValue().url
        }
      }

      s"have a home link to '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the class 'button'" in {
          button.attr("class") shouldBe "button"
        }

        "has the type 'submit'" in {
          button.attr("type") shouldBe "submit"
        }

        "has the id 'continue-button'" in {
          button.attr("id") shouldBe "continue-button"
        }

        "has the text 'Continue'" in {
          button.text shouldEqual commonMessages.continue
        }
      }

      s"Have a hidden help section" which {

        lazy val hiddenHelp = document.select("details")

        s"has a title ${messages.additionalContentTitle}" in {
          hiddenHelp.select(".summary").text shouldEqual messages.additionalContentTitle
        }

        s"has first paragraph content of ${}" in {
          hiddenHelp.select("p").first.text shouldEqual messages.helpHiddenContentOne
        }

        s"has second paragraph content of ${}" in {
          hiddenHelp.select("p").last.text shouldEqual messages.helpHiddenContentTwo
        }
      }
    }

    "not supplied with a pre-existing stored model and an acquisition date" should {

      lazy val view = rebasedValue(rebasedValueForm, "Yes")(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"Have the title ${messages.question}" in {
        document.title shouldEqual messages.question
      }

      s"not contain the help text over the input with wording ${messages.questionHelpText}" in {
        document.getElementsByClass("form-hint").text shouldNot include(messages.questionHelpText)
      }
    }

    "supplied with a form with errors" should {

      lazy val form = rebasedValueForm.bind(Map("hasRebasedValue" -> ""))
      lazy val view = rebasedValue(form, "Yes")(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}

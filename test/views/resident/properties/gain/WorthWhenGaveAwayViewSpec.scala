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

package views.resident.properties.gain

import assets.MessageLookup.{Resident => commonMessages}
import assets.MessageLookup.Resident.Properties.{PropertiesWorthWhenGaveAway => messages}
import controllers.helpers.FakeRequestHelper
import forms.resident.properties.WorthWhenGaveAwayForm._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{gain => views}
import controllers.resident.properties.routes

class WorthWhenGaveAwayViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  case class FakePOST(value: String) {
    lazy val request = fakeRequestToPOSTWithSession(("amount", value))
    lazy val form = worthWhenGaveAwayForm.bind(Map(("amount", value)))
    lazy val backLink = Some(controllers.resident.properties.routes.GainController.whoDidYouGiveItTo().toString())
    lazy val view = views.worthWhenGaveAway(worthWhenGaveAwayForm, backLink, "home-link", routes.GainController.submitWorthWhenGaveAway())(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)
  }

  "Worth when gave away View" should {

    lazy val backLink = Some(controllers.resident.properties.routes.GainController.whoDidYouGiveItTo().toString())
    lazy val view = views.worthWhenGaveAway(worthWhenGaveAwayForm, backLink, "home-link", routes.GainController.submitWorthWhenGaveAway())(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have the title of the page ${messages.title}" in {
      doc.title shouldEqual messages.title
    }

    s"have a back link to the Who did you give it to Page with text ${commonMessages.back}" in {
      doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/who-did-you-give-it-to"
    }

    "have a H1 tag that" should {

      lazy val heading = doc.select("H1")

      s"have the page heading '${messages.title}'" in {
        heading.text shouldBe messages.title
      }

      "have the heading-large class" in {
        heading.hasClass("heading-large") shouldEqual true
      }
    }

    "have a help paragraph" should {
      lazy val helpText = doc.select("#help")

      s"have the help text ${messages.helpMessage}" in {
        helpText.text shouldBe messages.helpMessage
      }
    }


    "have a form that" should {

      lazy val form = doc.select("form")

      "have the action /calculate-your-capital-gains/resident/properties/worth-when-gave-away" in {
        form.attr("action") shouldEqual "/calculate-your-capital-gains/resident/properties/worth-when-gave-away"
      }

      "have the method POST" in {
        form.attr("method") shouldEqual "POST"
      }

      "have an input for the amount" which {

        lazy val input = doc.select("#amount")

        "has a label" which {

          lazy val label = doc.select("label")

          s"has the text ${messages.title}" in {
            label.select("span").first().text() shouldEqual messages.title
          }

          "has the class visually hidden" in {
            label.select("span").hasClass("visuallyhidden") shouldEqual true
          }

          "is tied to the input field" in {
            label.attr("for") shouldEqual "amount"
          }
        }

        "renders in input tags" in {
          input.is("input") shouldEqual true
        }

        "has the field name as 'amount' to bind correctly to the form" in {

        }
      }

      "has a continue button" which {

        lazy val button = doc.select("#continue-button")

        "renders as button tags" in {
          button.is("button") shouldEqual true
        }

        "has type equal to 'submit'" in {
          button.attr("type") shouldEqual "submit"
        }

        "has class of button" in {
          button.hasClass("button") shouldEqual true
        }

        s"has the text ${commonMessages.continue}" in {
          button.text() shouldEqual commonMessages.continue
        }
      }
    }
  }

  "Worth When Gave Away View with form without errors" should {

    val form = worthWhenGaveAwayForm.bind(Map("amount" -> "100"))
    lazy val backLink = Some(controllers.resident.properties.routes.GainController.whoDidYouGiveItTo().toString())
    lazy val view = views.worthWhenGaveAway(form, backLink, "home-link", routes.GainController.submitWorthWhenGaveAway())(fakeRequest)
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

  "Worth When Gave Away View with form with errors" should {

    val form = worthWhenGaveAwayForm.bind(Map("amount" -> ""))
    lazy val backLink = Some(controllers.resident.properties.routes.GainController.whoDidYouGiveItTo().toString())
    lazy val view = views.worthWhenGaveAway(form, backLink, "home-link", routes.GainController.submitWorthWhenGaveAway())(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }
}

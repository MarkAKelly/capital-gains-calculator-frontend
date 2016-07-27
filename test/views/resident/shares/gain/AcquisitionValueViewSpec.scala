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

package views.resident.shares.gain

import assets.MessageLookup.{sharesAcquisitionValue => messages}
import assets.{MessageLookup => commonMessages}
import controllers.helpers.FakeRequestHelper
import forms.resident.AcquisitionValueForm._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.shares.{gain => views}

class AcquisitionValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Acquisition Value view" should {

    lazy val view = views.acquisitionValue(acquisitionValueForm, "home-link")(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    "have a home link to 'home-link'" in {
      doc.getElementById("homeNavHref").attr("href") shouldEqual "home-link"
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

      "have a link to Disposal Costs" in {
        backLink.attr("href") shouldBe controllers.resident.shares.routes.GainController.disposalCosts().toString
      }
    }

    "have a H1 tag that" should {
      lazy val heading = doc.select("h1")

      s"have the page heading '${messages.title}'" in {
        heading.text shouldBe messages.title
      }

      "have the heading-large class" in {
        heading.hasClass("heading-large") shouldBe true
      }
    }


    "have a form" which {

      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.resident.shares.routes.GainController.submitAcquisitionValue().toString}'" in {
        form.attr("action") shouldBe controllers.resident.shares.routes.GainController.submitAcquisitionValue().toString
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }

      "has a label that" should {

        lazy val label = doc.body.getElementsByTag("label")

        s"have the question ${messages.title}" in {
          label.text should include(messages.title)
        }

        "have the class 'visuallyhidden'" in {
          label.select("span").hasClass("visuallyhidden") shouldBe true
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

        s"have a third bullet point of ${messages.bulletListThree}" in {
          doc.select("div.indent li#bullet-list-three").text() shouldEqual messages.bulletListThree
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
    lazy val view = views.acquisitionValue(form, "home-link")(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }
}
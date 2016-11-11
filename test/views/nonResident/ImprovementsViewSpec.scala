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

import assets.MessageLookup.{NonResident => messages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import views.html.calculation.nonresident.improvements
import forms.nonresident.ImprovementsForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import controllers.nonresident.routes

class ImprovementsViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Improvements view" should {

    "supplied with no errors and improvementsOptions = true" should {

      lazy val view = improvements(improvementsForm(true), true, "back-link")(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "return some HTML that" should {

        s"has the question of ${messages.Improvements.question}" in {
          document.title shouldBe messages.Improvements.question
        }

        s"has the heading of ${messages.pageHeading}" in {
          document.body().getElementsByTag("h1").text shouldBe messages.pageHeading
        }

        "have a back link" which {

          lazy val backLink = document.body().select("#back-link")

          s"has the text ${messages.back}" in {
            backLink.text shouldEqual messages.back
          }

          "has a class of 'back-link'" in {
            backLink.attr("class") shouldBe "back-link"
          }

          s"has a route to 'back-link'" in {
            backLink.attr("href") shouldBe "back-link"
          }
        }

        "have that content" which {
          s"display the correct wording for radio option 'isClaimingImprovements'" in {
            document.body.select("input").attr("id") should include ("isClaimingImprovements")
          }
        }

        "have a form" which {
          lazy val form = document.body().select("form")

          "has a method of POST" in {
            form.attr("method") shouldBe "POST"
          }

          s"has an action of '${routes.ImprovementsController.submitImprovements().url}'" in {
            form.attr("action") shouldBe controllers.nonresident.routes.ImprovementsController.submitImprovements().url
          }
        }

        "have some hidden content" which {
          lazy val hiddenContent = document.body().select("#hidden")

          "which has a single div with a class of form-group" in {
            hiddenContent.select("div.form-group").size() shouldBe 3
          }

          s"contains the question ${messages.Improvements.questionThree}" in {
            hiddenContent.select("label").text() should include(messages.Improvements.questionThree)
          }

          s"contains the question ${messages.Improvements.questionFour}" in {
            hiddenContent.select("label").text() should include(messages.Improvements.questionFour)
          }
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
        }
      }
    }

    "supplied with no errors and improvementsOptions = false" should {

      lazy val view = improvements(improvementsForm(true), false, "back-link")(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "return some HTML that" should {

        "have that content" which {
          s"display the correct wording for radio option 'isClaimingImprovements'" in {
            document.body.select("input").attr("id") should include ("isClaimingImprovements")
          }

          "does not contain another component with an input box" in {
            document.body.select("input").attr("id") should not include "improvementsAmtAfter"
          }
        }

        "have some hidden content" which {
          lazy val hiddenContent = document.body().select("#hidden")

          "which has a single div with a class of form-group" in {
            hiddenContent.select("div.form-group").size() shouldBe 1
          }

          "contains an input with the id 'improvementsAmt'" in {
            hiddenContent.select("input").attr("id") shouldBe "improvementsAmt"
          }

          s"contains the question ${messages.Improvements.questionTwo}" in {
            hiddenContent.select("label").text() startsWith messages.Improvements.questionTwo
          }
        }
      }
    }

    "supplied with errors" should {
      lazy val form = improvementsForm(true).bind(Map("improvements" -> "testData"))
      lazy val view = improvements(form, true, "back-link-two")(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"have a route to 'back-link-two'" in {
        document.body.getElementById("back-link").attr("href") shouldEqual "back-link-two"
      }

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}

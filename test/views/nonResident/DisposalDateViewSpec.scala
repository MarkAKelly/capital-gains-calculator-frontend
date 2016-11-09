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

import java.time.LocalDate

import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{DisposalDate => messages}
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.routes
import forms.nonresident.DisposalDateForm._
import org.jsoup.Jsoup
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.disposalDate

class DisposalDateViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "The Disposal Date View" should {

    "return some HTML" which {

      lazy val acquisitionDate = LocalDate.of(2010, 3, 1)
      lazy val view = disposalDate(disposalDateForm(Some(acquisitionDate)))(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have the title 'When did you sign the contract that made someone else the owner?'" in {
        document.title shouldEqual messages.question
      }

      s"have the heading ${Messages("calc.disposalDate.question")} " in {
        document.body.getElementsByTag("h1").text shouldEqual messages.question
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
          backLink.attr("href") shouldBe routes.ImprovementsController.improvements().url
        }
      }

      s"have the question '${Messages("calc.disposalDate.question")}'" in {
        document.body.getElementsByTag("fieldset").text should include(messages.question)
      }

      "have inputs using the id acquisitionDate" in {
        document.body().select("input[type=number]").attr("id") should include ("disposalDate")
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
    }

    "supplied with errors" should {
      lazy val acquisitionDate = LocalDate.of(2010, 3, 1)
      lazy val form = disposalDateForm(Some(acquisitionDate)).bind(Map("disposalDateDay" -> "a"))
      lazy val view = disposalDate(form)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}

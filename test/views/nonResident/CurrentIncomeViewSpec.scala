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
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import forms.nonresident.CurrentIncomeForm._
import org.jsoup.Jsoup
import views.html.calculation.nonresident.currentIncome

class CurrentIncomeViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  "Current Income view" when {

    "supplied with no errors" should {
      lazy val view = currentIncome(currentIncomeForm)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.CurrentIncome.question}'" in {
        document.title() shouldBe messages.CurrentIncome.question
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "back-link"
        }

        "has the text" in {
          backLink.text shouldBe messages.back
        }

        s"has a route to 'customer-type'" in {
          backLink.attr("href") shouldBe controllers.nonresident.routes.CustomerTypeController.customerType().url
        }
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.nonresident.routes.CurrentIncomeController.submitCurrentIncome().url}'" in {
          form.attr("action") shouldBe controllers.nonresident.routes.CurrentIncomeController.submitCurrentIncome().url
        }
      }

      s"have the question '${messages.CurrentIncome.question}'" in {
        document.body.select("label span").first().text shouldBe messages.CurrentIncome.question
      }

      s"have the help text '${messages.CurrentIncome.helpText}'" in {
        document.body.select("label span.form-hint").text() shouldBe messages.CurrentIncome.helpText
      }

      "have an input with the id 'currentIncome" in {
        document.body().select("input").attr("id") shouldBe "currentIncome"
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

      "have a sidebar" which {
        lazy val sidebar = document.body().select("aside")
        lazy val links = sidebar.select("a")

        "contains two links" in {
          links.size() shouldBe 2
        }

        "has links with the class 'external-link'" in {
          links.attr("class") shouldBe "external-link"
        }

        "has links with a rel of 'external'" in {
          links.attr("rel") shouldBe "external"
        }

        "has links with a target of '_blank'" in {
          links.attr("target") shouldBe "_blank"
        }

        "has the first link with an href to 'https://www.gov.uk/income-tax/overview'" in {
          sidebar.select("#helpLink1").attr("href") shouldBe "https://www.gov.uk/income-tax/overview"
        }

        "has the first link with the correct text" in {
          sidebar.select("#helpLink1").text() shouldBe s"${messages.CurrentIncome.linkOne} ${messages.externalLink}"
        }

        "has the second link with an href to 'https://www.gov.uk/income-tax-rates/previous-tax-years'" in {
          sidebar.select("#helpLink2").attr("href") shouldBe "https://www.gov.uk/income-tax-rates/previous-tax-years"
        }

        "has the second link with the correct text" in {
          sidebar.select("#helpLink2").text() shouldBe s"${messages.CurrentIncome.linkTwo} ${messages.externalLink}"
        }
      }
    }

    "supplied with errors" should {
      lazy val form = currentIncomeForm.bind(Map("currentIncome" -> "a"))
      lazy val view = currentIncome(form)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}

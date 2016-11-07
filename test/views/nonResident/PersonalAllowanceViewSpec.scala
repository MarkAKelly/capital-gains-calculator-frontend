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
import assets.MessageLookup.NonResident.{PersonalAllowance => messages}
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.routes
import forms.nonresident.PersonalAllowanceForm._
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.personalAllowance

class PersonalAllowanceViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  "The Personal Allowance View" should {

    "return some HTML" which {

      lazy val view = personalAllowance(personalAllowanceForm(11000))(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"has the title ${messages.question}" in {
        document.title shouldEqual messages.question
      }

      s"has the heading ${commonMessages.pageHeading}" in {
        document.body.getElementsByTag("h1").text shouldEqual commonMessages.pageHeading
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
          backLink.attr("href") shouldBe routes.CurrentIncomeController.currentIncome().url
        }
      }

      s"has the question '${messages.question}' as the label of the input" in {
        document.body.getElementsByTag("label").text should include(messages.question)
      }

      "display an input box for the Personal Allowance" in {
        document.body.getElementById("personalAllowance").tagName() shouldEqual "input"
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

      "should contain a Read more sidebar that" should {

        lazy val sidebar = document.select("aside")

        "have a header" in {
          sidebar.select("h2").text shouldEqual commonMessages.readMore
        }

        "have two links of" which {

          lazy val linkOne = sidebar.select("a").first
          lazy val linkTwo = sidebar.select("a").last

          "the first" should {

            s"have text ${messages.linkOne} ${commonMessages.externalLink}" in {
              linkOne.text shouldEqual s"${messages.linkOne} ${commonMessages.externalLink}"
            }

            s"have an href to 'https://www.gov.uk/income-tax-rates/current-rates-and-allowances'" in {
              linkOne.attr("href") shouldEqual "https://www.gov.uk/income-tax-rates/current-rates-and-allowances"
            }

            "have the class 'external-link'" in {
              linkOne.attr("class") shouldBe "external-link"
            }

            "have a rel of 'external'" in {
              linkOne.attr("rel") shouldBe "external"
            }

            "have a target of '_blank'" in {
              linkOne.attr("target") shouldBe "_blank"
            }
          }

          "the second" should{

            s"have text ${messages.linkTwo} ${commonMessages.externalLink}" in {
              linkTwo.text shouldEqual s"${messages.linkTwo} ${commonMessages.externalLink}"
            }

            s"have an href to 'https://www.gov.uk/tax-uk-income-live-abroad/personal-allowance'" in {
              linkTwo.attr("href") shouldEqual "https://www.gov.uk/tax-uk-income-live-abroad/personal-allowance"
            }

            "have the class 'external-link'" in {
              linkTwo.attr("class") shouldBe "external-link"
            }

            "have a rel of 'external'" in {
              linkTwo.attr("rel") shouldBe "external"
            }

            "have a target of '_blank'" in {
              linkTwo.attr("target") shouldBe "_blank"
            }
          }
        }
      }
    }

    "when supplied with a form with errors" should {

      lazy val form = personalAllowanceForm(11000).bind(Map("personalAllowance" -> "132891"))
      lazy val view = personalAllowance(form)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}

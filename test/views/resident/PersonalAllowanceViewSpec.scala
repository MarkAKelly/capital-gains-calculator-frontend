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

import assets.MessageLookup.{personalAllowance => messages}
import assets.{MessageLookup => commonMessages}
import controllers.helpers.FakeRequestHelper
import forms.resident.income.PersonalAllowanceForm._
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{resident => views}

class PersonalAllowanceViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Personal Allowance view" when {

    "supplied with a 2015/16 tax year" should {

      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val view = views.personalAllowance(personalAllowanceForm(), taxYearModel, BigDecimal(10600))(fakeRequest)
      lazy val doc = Jsoup.parse(view.body)

      "have a charset of UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"have a title ${messages.title("2015/16")}" in {
        doc.title() shouldBe messages.title("2015/16")
      }

      "have a back button that" should {
        lazy val backLink = doc.select("a#back-link")
        "have the correct back link text" in {
          backLink.text shouldBe commonMessages.calcBaseBack
        }

        "have the back-link class" in {
          backLink.hasClass("back-link") shouldBe true
        }

        "have a link to Current Income" in {
          backLink.attr("href") shouldBe controllers.resident.properties.routes.IncomeController.currentIncome().toString
        }
      }

      "have a H1 tag that" should {
        lazy val h1Tag = doc.select("H1")

        s"have the page heading '${messages.title("2015/16")}'" in {
          h1Tag.text shouldBe messages.title("2015/16")
        }

        "have the heading-large class" in {
          h1Tag.hasClass("heading-large") shouldBe true
        }
      }

      "have a form" which {
        lazy val form = doc.getElementsByTag("form")

        s"has the action '${controllers.resident.properties.routes.IncomeController.submitPersonalAllowance().toString}'" in {
          form.attr("action") shouldBe controllers.resident.properties.routes.IncomeController.submitPersonalAllowance().toString
        }

        "has the method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"have a legend for an input with text ${messages.question("2015/16")}" in {
          doc.body.getElementsByClass("heading-large").text() shouldEqual messages.question("2015/16")
        }


        "has help text that" should {
          s"have the text ${messages.help("10,600")}" in {
            doc.body().select("div.form-group span.form-hint").text() shouldBe messages.help("10,600")
          }
        }


      }

      s"the Personal Allowance Help link ${messages.helpLinkOne} should " +
        "have the address Some(https://www.gov.uk/income-tax-rates/current-rates-and-allowances)" in {
        doc.select("a#personalAllowanceLink").attr("href") shouldEqual "https://www.gov.uk/income-tax-rates/current-rates-and-allowances"
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
        "has a step value of '1'" in {
          input.attr("step") shouldBe "1"
        }
        s"has placeholder 'eg. 25000.00'" in {
          input.attr("placeholder") shouldBe "eg. 25000.00"
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


      "Personal Allowance view with stored values" should {
        lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
        lazy val form = personalAllowanceForm().bind(Map(("amount", "1000")))
        lazy val view = views.personalAllowance(form, taxYearModel, BigDecimal(10600))(fakeRequest)
        lazy val doc = Jsoup.parse(view.body)

        "have the value of 1000 auto-filled in the input" in {
          lazy val input = doc.body.getElementsByTag("input")
          input.`val` shouldBe "1000"
        }
      }
    }

    "supplied with a 2016/17 tax year" should {

      lazy val taxYearModel = TaxYearModel("2016/17", true, "2016/17")
      lazy val view = views.personalAllowance(personalAllowanceForm(), taxYearModel, BigDecimal(11000))(fakeRequest)
      lazy val doc = Jsoup.parse(view.body)
      lazy val h1Tag = doc.select("H1")

      s"have a title ${messages.inYearTitle}" in {
        doc.title() shouldBe messages.inYearTitle
      }

      s"have the page heading '${messages.inYearTitle}'" in {
        h1Tag.text shouldBe messages.inYearTitle
      }

      s"have a legend for an input with text ${messages.inYearQuestion}" in {
        doc.body.getElementsByClass("heading-large").text() shouldEqual messages.inYearQuestion
      }

      "has help text that" should {
        s"have the text ${messages.help("11,000")}" in {
          doc.body().select("div.form-group span.form-hint").text() shouldBe messages.help("11,000")
        }
      }
    }

    "supplied with a 2017/18 tax year" should {

      lazy val taxYearModel = TaxYearModel("2017/18", false, "2016/17")
      lazy val view = views.personalAllowance(personalAllowanceForm(), taxYearModel, BigDecimal(11000))(fakeRequest)
      lazy val doc = Jsoup.parse(view.body)
      lazy val h1Tag = doc.select("H1")

      s"have a title ${messages.title("2017/18")}" in {
        doc.title() shouldBe messages.title("2017/18")
      }

      s"have the page heading '${messages.title("2017/18")}'" in {
        h1Tag.text shouldBe messages.title("2017/18")
      }

      s"have a legend for an input with text ${messages.question("2017/18")}" in {
        doc.body.getElementsByClass("heading-large").text() shouldEqual messages.question("2017/18")
      }

      "has help text that" should {
        s"have the text ${messages.help("11,000")}" in {
          doc.body().select("div.form-group span.form-hint").text() shouldBe messages.help("11,000")
        }
      }
    }

    "Personal Allowance View with form with errors" which {

      "is due to mandatory field error" should {

        lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
        val form = personalAllowanceForm().bind(Map("amount" -> ""))
        lazy val view = views.personalAllowance(form, taxYearModel, BigDecimal(11000))(fakeRequest)
        lazy val doc = Jsoup.parse(view.body)

        "display an error summary message for the amount" in {
          doc.body.select("#amount-error-summary").size shouldBe 1
        }

        "display an error message for the input" in {
          doc.body.select(".form-group .error-notification").size shouldBe 1
        }
      }
    }
  }
}

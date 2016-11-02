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
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class PersonalAllowanceViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  "The Personal Allowance View" should {

    "return some HTML" which {

      s"has the title ${messages.question}" in {
        document.title shouldEqual messages.question
      }

      s"has the heading ${commonMessages.pageHeading}" in {
        document.body.getElementsByTag("h1").text shouldEqual commonMessages.pageHeading
      }

      s"has a 'Back' link to ${routes.CurrentIncomeController.currentIncome()}" in {
        document.body.getElementById("back-link").text shouldEqual commonMessages.back
        document.body.getElementById("back-link").attr("href") shouldEqual routes.CurrentIncomeController.currentIncome().toString()
      }

      s"has the question '${messages.question}' as the label of the input" in {
        document.body.getElementsByTag("label").text should include(messages.question)
      }

      "display an input box for the Personal Allowance" in {
        document.body.getElementById("personalAllowance").tagName() shouldEqual "input"
      }

      "has no value auto-filled into the input box" in {
        document.getElementById("personalAllowance").attr("value") shouldBe empty
      }

      "display a 'Continue' button " in {
        document.body.getElementById("continue-button").text shouldEqual commonMessages.continue
      }

      "should contain a Read more sidebar with a link to personal allowances and taxation abroad" in {
        document.select("aside h2").text shouldBe commonMessages.readMore
        document.select("aside a").first().attr("href") shouldBe "https://www.gov.uk/income-tax-rates/current-rates-and-allowances"
        document.select("aside a").first.text shouldBe s"${messages.linkOne} ${commonMessages.externalLink}"
        document.select("aside a").last().attr("href") shouldBe "https://www.gov.uk/tax-uk-income-live-abroad/personal-allowance"
        document.select("aside a").last.text shouldBe s"${messages.linkTwo} ${commonMessages.externalLink}"
      }
    }
  }
}

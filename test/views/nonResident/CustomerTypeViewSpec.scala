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

import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.customerType
import forms.nonresident.CustomerTypeForm._
import org.jsoup.Jsoup
import assets.MessageLookup.NonResident.{CustomerType => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import controllers.helpers.FakeRequestHelper

class CustomerTypeViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  "The Customer Type View" should {

    lazy val view = customerType(customerTypeForm)(fakeRequest)
    lazy val document = Jsoup.parse(view.body)

    "return some HTML that" which {

      s"have the title ${messages.question}" in {
        document.title shouldEqual messages.question
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "heading-large"
        }

        s"has the text '${commonMessages.pageHeading}'" in {
          heading.text shouldBe commonMessages.pageHeading
        }
      }

      s"have a home link to '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
        document.select("#homeNavHref").attr("href") shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.nonresident.routes.CustomerTypeController.submitCustomerType().url}'" in {
          form.attr("action") shouldBe controllers.nonresident.routes.CustomerTypeController.submitCustomerType().url
        }
      }

      s"have the question ${messages.question} as the legend of the input" in {
        document.body.getElementsByTag("legend").text shouldEqual messages.question
      }

      s"display a radio button with the option ${messages.individual}" in {
        document.body.getElementById("customerType-individual").parent.text shouldEqual messages.individual
      }

      "have the radio option `individual` not selected by default" in {
        document.body.getElementById("customerType-individual").parent.classNames().contains("selected") shouldBe false
      }

      s"display a radio button with the option ${messages.trustee}" in {
        document.body.getElementById("customerType-trustee").parent.text shouldEqual messages.trustee
      }

      "have the radio option `trustee` not selected by default" in {
        document.body.getElementById("customerType-trustee").parent.classNames().contains("selected") shouldBe false
      }

      s"display a radio button with the option ${messages.personalRep}" in {
        document.body.getElementById("customerType-personalrep").parent.text shouldEqual messages.personalRep
      }

      "have the radio option `personalrep` not selected by default" in {
        document.body.getElementById("customerType-personalrep").parent.classNames().contains("selected") shouldBe false
      }

      "display a 'Continue' button " in {
        document.body.getElementById("continue-button").text shouldEqual commonMessages.continue
      }
    }
  }
}

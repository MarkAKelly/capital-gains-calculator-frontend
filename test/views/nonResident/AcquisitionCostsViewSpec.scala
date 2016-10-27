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

import assets.MessageLookup
import controllers.helpers.FakeRequestHelper
import forms.nonresident.AcquisitionCostsForm._
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.acquisitionCosts
import assets.MessageLookup.{NonResident => messages}

class AcquisitionCostsViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  "Acquisition costs view" when {

    "supplied with no errors" should {
      lazy val view = acquisitionCosts(acquisitionCostsForm)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.AcquisitionCosts.question}'" in {
        document.title() shouldBe messages.AcquisitionCosts.question
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has the text" in {
          backLink.text shouldBe MessageLookup.NonResident.back
        }

        s"has a route to 'disposal-costs'" in {
          backLink.attr("href") shouldBe controllers.nonresident.routes.DisposalValueController.disposalValue().url
        }
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "heading-large"
        }

        s"has the text '${messages.pageHeading}'" in {
          heading.text shouldBe messages.pageHeading
        }
      }

      s"have the question '${messages.AcquisitionCosts.question}'" in {
        document.body.select("label span").first().text shouldBe messages.AcquisitionCosts.question
      }

      s"have the help text '${messages.AcquisitionCosts.helpText}'" in {
        document.body.select("label span.form-hint").text() shouldBe messages.AcquisitionCosts.helpText
      }

      "have an input with the id 'acquisitionCosts" in {
        document.body().select("input").attr("id") shouldBe "acquisitionCosts"
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.nonresident.routes.AcquisitionCostsController.submitAcquisitionCosts().url}'" in {
          form.attr("action") shouldBe controllers.nonresident.routes.AcquisitionCostsController.submitAcquisitionCosts().url
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

    "supplied with errors" should {
      lazy val form = acquisitionCostsForm.bind(Map("acquisitionCosts" -> "a"))
      lazy val view = acquisitionCosts(form)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}

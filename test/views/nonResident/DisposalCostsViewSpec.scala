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
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import forms.nonresident.DisposalCostsForm._
import org.jsoup.Jsoup
import views.html.calculation.nonresident.disposalCosts

class DisposalCostsViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Disposal Costs view" when {

    "supplied with no errors" should {
      lazy val view = disposalCosts(disposalCostsForm(BigDecimal(10000)))(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.DisposalCosts.question}'" in {
        document.title shouldBe messages.DisposalCosts.question
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "back-link"
        }

        "has the text" in {
          backLink.text shouldBe messages.back
        }

        s"has a route to 'acquisition-costs'" in {
          backLink.attr("href") shouldBe controllers.nonresident.routes.AcquisitionCostsController.acquisitionCosts().url
        }
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a POST method" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.nonresident.routes.DisposalCostsController.submitDisposalCosts().url}'" in {
          form.attr("action") shouldBe controllers.nonresident.routes.DisposalCostsController.submitDisposalCosts().url
        }
      }

      s"have the question ${messages.DisposalCosts.question}" in {
        document.body.select("label span").first.text shouldBe messages.DisposalCosts.question
      }

      "have an input with the id 'disposalCosts" in {
        document.body.select("input").attr("id") shouldBe "disposalCosts"
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
      lazy val form = disposalCostsForm().bind(Map("disposalCosts" -> "a"))
      lazy val view = disposalCosts(form)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}

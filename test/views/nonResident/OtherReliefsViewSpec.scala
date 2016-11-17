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
import forms.nonresident.OtherReliefsForm._
import models.nonresident.CalculationResultModel
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.otherReliefs

class OtherReliefsViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  "The Other Reliefs Flat view" when {

    "not supplied with a pre-existing stored value and a taxable gain" should {
      lazy val view = otherReliefs(otherReliefsForm)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of ${messages.OtherReliefs.question}" in {
        document.title() shouldBe messages.OtherReliefs.question
      }

      "have a back link" which {
        lazy val backLink = document.select("#back-link")

        "should have the text" in {
          backLink.text shouldEqual messages.back
        }

        "should have a class of back-link" in {
          backLink.attr("class") shouldBe "back-link"
        }

        s"should have a route to 'improvements'" in {
          backLink.attr("href") shouldEqual
            controllers.nonresident.routes.ImprovementsController.improvements().url
        }
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "heading-xlarge"
        }

        s"has the text '${messages.OtherReliefs.question}'" in {
          heading.text shouldBe messages.OtherReliefs.question
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

        s"has an action of '${controllers.nonresident.routes.OtherReliefsController.otherReliefs().url}'" in {
          form.attr("action") shouldBe controllers.nonresident.routes.OtherReliefsController.otherReliefs().url
        }
      }

      s"have the question '${messages.OtherReliefs.question}'" in {
        document.body.select("legend").first().text shouldBe messages.OtherReliefs.question
      }

      "have an input using the id otherReliefs" in {
        document.body().select("input[type=number]").attr("id") should include("otherReliefs")
      }

      "have the correct help text" in {
        document.body().select("form .form-hint") shouldBe s"${messages.OtherReliefs.help}\n${messages.OtherReliefs.helpTwo}"
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the class 'button'" in {
          button.attr("class") shouldBe "button"
        }

        "has the id 'continue-button'" in {
          button.attr("id") shouldBe "continue-button"
        }

        s"has the text ${messages.continue}" in {
          button.text() shouldBe messages.continue
        }
      }
    }

    "supplied with an invalid map" should {
      val map = Map("otherReliefs" -> "-1000")
      lazy val view = otherReliefs(otherReliefsForm.bind(map))(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}


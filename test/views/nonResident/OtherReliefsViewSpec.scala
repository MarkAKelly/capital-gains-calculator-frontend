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
      val model = CalculationResultModel(100, 1000, 100, 18, 0, None, None, None)
      lazy val view = otherReliefs(otherReliefsForm(false), model)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of ${messages.OtherReliefs.question}" in {
        document.title() shouldBe messages.OtherReliefs.question
      }

      "have a back link" which {
        lazy val backLink = document.select("#back-link")

        "should have the text" in {
          backLink.text shouldEqual messages.back
        }

        s"should have a route to 'allowable losses'" in {
          backLink.attr("href") shouldEqual
            controllers.nonresident.routes.AllowableLossesController.allowableLosses().url
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

      "have include 'isClaimingOtherReliefs' as part of the id on the option inputs" in {
        document.select("input").attr("id") should include ("isClaimingOtherReliefs")
      }

      "have an input using the id otherReliefs" in {
        document.body().select("input[type=number]").attr("id") should include ("otherReliefs")
      }

      "have additional content" which {
        lazy val content = document.select("form > div")

        "has a list of class list" in {
          content.select("ul").attr("class") shouldBe "list"
        }

        "has a list entry with the total gain message and value" in {
          content.select("li#totalGain").text() shouldBe s"${messages.OtherReliefs.totalGain} £1,000"
        }

        "has a list entry with the taxable gain message and value" in {
          content.select("li#taxableGain").text() shouldBe s"${messages.OtherReliefs.taxableGain} £100"
        }
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

    "supplied with a pre-existing stored value and a negative taxable gain" should {
      val model = CalculationResultModel(100, 1000, -100, 18, 0, None, None, None)
      val map = Map("otherReliefs" -> "1000")
      lazy val view = otherReliefs(otherReliefsForm(false).bind(map), model)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "has a list entry with the loss carried forward message and value" in {
        document.select("li#taxableGain").text() shouldBe s"${messages.OtherReliefs.lossCarriedForward} £100"
      }
    }

    "supplied with an invalid map" should {
      val model = CalculationResultModel(100, 1000, -100, 18, 0, None, None, None)
      val map = Map("otherReliefs" -> "-1000")
      lazy val view = otherReliefs(otherReliefsForm(false).bind(map), model)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}

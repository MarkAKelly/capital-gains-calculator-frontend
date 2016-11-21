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
import forms.nonresident.MarketValueWhenSoldForm._
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.marketValueSold

/**
  * Created by emma on 17/11/16.
  */
class MarketValueWhenSoldViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{
  "The market value when gave away page" should {

    lazy val view = marketValueSold(marketValueForm)(fakeRequestWithSession)
    lazy val document = Jsoup.parse(view.body)

    "supplied with no errors" should {
      s"have a title of" in {
        document.title() shouldBe MessageLookup.NonResident.MarketValue.disposalSoldQuestion
      }

      s"have a header" which {
        lazy val header = document.select("h1")
        s"has the text '${MessageLookup.NonResident.MarketValue.disposalSoldQuestion}'" in {
          header.text() shouldBe MessageLookup.NonResident.MarketValue.disposalSoldQuestion
        }

        s"has the class 'head-xlarge'" in {
          header.attr("class") shouldBe "heading-xlarge"
        }
      }

      s"have a paragraph" which {
        lazy val helpText = document.select("p.form-hint")
        s"has the help text'${MessageLookup.NonResident.MarketValue.disposalHelpText}'" in {
          helpText.html() shouldBe MessageLookup.NonResident.MarketValue.disposalHelpText +
            " <br> " + MessageLookup.NonResident.MarketValue.disposalHelpTextAdditional
        }
        s"has the class 'form-hint'" in {
          helpText.attr("class") shouldBe "form-hint"
        }
      }


      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.nonresident.routes.MarketValueWhenSoldOrGaveAwayController.submitMarketValueWhenSold()}'" in {
          form.attr("action") shouldBe controllers.nonresident.routes.MarketValueWhenSoldOrGaveAwayController.submitMarketValueWhenSold().url
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

    "supplied with a form with errors" should {
      lazy val form = marketValueForm.bind(Map("disposalValue" -> "testData"))
      lazy val view = marketValueSold(form)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }
}

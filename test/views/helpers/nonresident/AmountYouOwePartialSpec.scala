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

package views.helpers.nonresident

import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.UnitSpec
import views.html.helpers.nonresident.amountYouOweRow

class AmountYouOwePartialSpec extends UnitSpec {

  "Creating questionAnswerRow" when {

    "passing in a String answer" should {
      lazy val result = amountYouOweRow(BigDecimal(1000), 2)
      lazy val doc = Jsoup.parse(result.body)

      "have a div for the question with an id of 'id-question' which" which {

        "has a class of 'grid-layout__column grid-layout__column--1-2'" in {
          doc.select("div#amount-you-owe-question").attr("class") shouldBe "grid-layout__column grid-layout__column--1-2"
        }

        "has a span with a class of 'heading-large'" in {
          doc.select("div#amount-you-owe-question span").attr("class") shouldBe "heading-large"
        }

        "has the text 'Amount you owe'" in {
          doc.select("div#amount-you-owe-question span").text shouldBe "Amount you owe"
        }
      }

      "have a div for the answer with an id of 'id-answer'" which {

        "has a class of 'grid-layout__column grid-layout__column--1-2'" in {
          doc.select("div#amount-you-owe-value").attr("class") shouldBe "grid-layout__column grid-layout__column--1-2"
        }

        "has a change link with a class of 'heading-large' and 'summary-answer" in {
          doc.select("div#amount-you-owe-value span").attr("class") shouldBe "heading-large"
        }

        "has the text 'answer'" in {
          doc.select("div#amount-you-owe-value span").text() shouldBe "£1,000.00"
        }
      }
    }
  }
}


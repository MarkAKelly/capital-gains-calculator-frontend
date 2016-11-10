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

import controllers.helpers.FakeRequestHelper
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{NonResident => messages}
import views.html.calculation.nonresident.boughtForLess
import forms.nonresident.BoughtForLessForm._
import org.jsoup.Jsoup
import org.jsoup.select.Elements

class BoughtForLessViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  def assertHTML(elements: Elements)(test: Elements => Unit): Unit = {
    if(elements.isEmpty) cancel("element not found")
    else test(elements)
  }

  "Bought for less view" when {

    "supplied with no errors" should {
      lazy val view = boughtForLess(boughtForLessForm)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of ${messages.BoughtForLess.question}" in {
        document.title() shouldBe messages.BoughtForLess.question
      }

      "have a back link" which {
        lazy val backLink = document.select("#back-link")

        "has only a single back link" in {
          backLink.size() shouldBe 1
        }

        "has a class of back-link" in {
          assertHTML(backLink)(_.attr("class") shouldBe "back-link")
        }

        "has a message of back-link" in {
          assertHTML(backLink)(_.text() shouldBe messages.back)
        }

        "has an href to the how became owner page" in {
          assertHTML(backLink)(_.attr("href") shouldBe controllers.nonresident.routes.HowBecameOwnerController.howBecameOwner().url)
        }
      }
    }
  }

}

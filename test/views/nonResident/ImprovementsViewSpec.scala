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
import org.jsoup.Jsoup
import views.html.calculation.nonresident.improvements
import forms.nonresident.ImprovementsForm._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class ImprovementsViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Passing no errors into the Improvements View" should {

    lazy val view = improvements(improvementsForm(true), true, "back-link")(fakeRequest)
    lazy val document = Jsoup.parse(view.body)

    "return some HTML that" should {

      s"has the question of ${messages.Improvements.question}" in {
        document.title shouldBe messages.Improvements.question
      }

      s"has the heading of ${messages.pageHeading}" in {
        document.body().getElementsByTag("h1").text shouldBe messages.pageHeading
      }

      "have a back link" which {

        lazy val backLink = document.body().select("#back-link")

        s"has the text ${messages.back}" in {
          backLink.text shouldEqual messages.back
        }

        "has a class of 'back-link'" in {
          backLink.attr("class") shouldBe "back-link"
        }

        s"has a route to 'back-link'" in {
          backLink.attr("href") shouldBe "back-link"
        }
      }

      "have that content" which {
        s"display the correct wording for radio option ${messages.yes}" in {
          document.body.getElementById("isClaimingImprovements-yes").parent.text shouldEqual messages.yes
        }

        s"display the correct wording for radio option ${messages.no}" in {
          document.body.getElementById("isClaimingImprovements-no").parent.text shouldEqual messages.no
        }

        "contain a hidden component with an input box" in {
          document.body.getElementById("hidden").html should include("input")
        }
      }
    }
  }
}

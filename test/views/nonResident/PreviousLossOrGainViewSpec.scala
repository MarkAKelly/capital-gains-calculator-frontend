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


import assets.MessageLookup.NonResident.{PreviousLossOrGain => messages}
import controllers.helpers.FakeRequestHelper
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.previousLossOrGain
import forms.nonresident.PreviousLossOrGainForm._
import org.jsoup.Jsoup

class PreviousLossOrGainViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  "The PreviousLossOrGain view" should {

    lazy val view = previousLossOrGain(previousLossOrGainForm)(fakeRequest)
    lazy val document = Jsoup.parse(view.body)

    "return some HTML" which {
      s"has the title ${messages.question}" in {
        document.title shouldEqual messages.question
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "heading-large"
        }
      }
    }
  }

}

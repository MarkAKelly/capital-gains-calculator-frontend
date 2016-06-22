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

package views.resident

import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}
import forms.resident.DisposalDateForm._
import assets.MessageLookup.{disposalDate => messages}
import assets.MessageLookup._

class DisposalDateViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Disposal Date view" should {

    lazy val view = views.html.calculation.resident.disposalDate(disposalDateForm)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    "have the title 'When did you sign the contract that made someone else the owner?'" in {
      doc.title() shouldBe messages.title
    }

    "have the heading question 'When did you sign the contract that made someone else the owner?'" in {
      doc.body.getElementsByTag("h1").text should include(messages.question)
    }

    "have the helptext 'For example, 4 9 2016'" in {
      doc.body.getElementsByClass("form-hint").text should include(messages.helpText)
    }

    "have an input box for day" in {
      doc.body.getElementById("disposalDateDay").parent.text shouldBe messages.day
    }

    "have an input box for month" in {
      doc.body.getElementById("disposalDateMonth").parent.text shouldBe messages.month
    }

    "have an input box for year" in {
      doc.body.getElementById("disposalDateYear").parent.text shouldBe messages.year
    }

    "have a button with the text 'Continue'" in {
      doc.body.getElementById("continue-button").text shouldBe calcBaseContinue
    }
  }
}

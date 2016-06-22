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

import org.jsoup.Jsoup
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}
import forms.resident.DisposalDateForm._

class DisposalDateViewSpec extends UnitSpec with WithFakeApplication {

  "Disposal Date view" should {

    val fakeRequest = FakeRequest("GET", "")

    "have charset UTF-8" in {
      val view = views.html.calculation.resident.disposalDate(disposalDateForm)(fakeRequest)
      val doc = Jsoup.parse(view.body)
      doc.charset().toString shouldBe "UTF-8"
    }

    "have the title 'When did you sign the contract that made someone else the owner?'" in {
      val view = views.html.calculation.resident.disposalDate(disposalDateForm)(fakeRequest)
      val doc = Jsoup.parse(view.body)
      doc.title() shouldBe "When did you sign the contract that made someone else the owner?"
    }

    "have the heading question 'When did you sign the contract that made someone else the owner?'" in {
      val view = views.html.calculation.resident.disposalDate(disposalDateForm)(fakeRequest)
      val doc = Jsoup.parse(view.body)
      doc.body.getElementsByTag("h1").text should include("When did you sign the contract that made someone else the owner?")
    }

    "have the helptext 'For example, 4 9 2016'" in {
      val view = views.html.calculation.resident.disposalDate(disposalDateForm)(fakeRequest)
      val doc = Jsoup.parse(view.body)
      doc.body.getElementsByClass("form-hint").text should include("For example, 4 9 2016")
    }
  }
}

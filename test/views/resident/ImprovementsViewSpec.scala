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
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{improvementsView => messages}
import forms.resident.ImprovementsForm.improvementsForm

class ImprovementsViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Improvements view" should {

    lazy val view = views.html.calculation.resident.improvements(improvementsForm)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title of ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    "have a H1 tag that" should {

      lazy val heading = doc.select("H1")

      s"have the page heading '${messages.title}'" in {
        heading.text shouldBe messages.title
      }

      "have the visuallyhidden class" in {
        heading.hasClass("visuallyhidden") shouldBe true
      }
    }

    "have the correct note" in {
      val note = doc.select(".panel.panel-border-wide>p")
      note.text shouldBe messages.note
    }

    "have the correct label" in {
      val label = doc.select("label")
      label.text should startWith(messages.label)
    }

    "have the correct hint" in {
      val hint = doc.select("label .form-hint")
      hint.text shouldBe messages.hint
    }
  }
}

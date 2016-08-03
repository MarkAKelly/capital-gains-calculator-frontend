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

package views.resident.properties.gain

import assets.MessageLookup.{improvementsView => messages}
import controllers.helpers.FakeRequestHelper
import forms.resident.properties.ImprovementsForm._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{gain => views}

class ImprovementsViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Improvements view" should {

    lazy val view = views.improvements(improvementsForm)(fakeRequest)
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

      "have the heading-large class" in {
        heading.hasClass("heading-large") shouldBe true
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

    "have a hidden label" in {
      val label = doc.select("label > div > span")
      label.hasClass("visuallyhidden") shouldBe true
    }

    "have the correct hint" in {
      val hint = doc.select("label .form-hint")
      hint.text shouldBe messages.hint
    }

    "not display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 0
    }

    "not display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 0
    }
  }

  "Improvements View with form without errors" should {

    val form = improvementsForm.bind(Map("amount" -> "100"))
    lazy val view = views.improvements(form)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "display the value of the form" in {
      doc.body.select("#amount").attr("value") shouldEqual "100"
    }

    "display no error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 0
    }

    "display no error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 0
    }
  }

  "Improvements View with form with errors" should {

    val form = improvementsForm.bind(Map("amount" -> ""))
    lazy val view = views.improvements(form)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }

}

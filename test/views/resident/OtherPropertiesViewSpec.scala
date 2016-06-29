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
import assets.MessageLookup.{otherProperties => messages}
import assets.MessageLookup._
import forms.resident.OtherPropertiesForm._

class OtherPropertiesViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Other Properties view" should {

    lazy val view = views.html.calculation.resident.otherProperties(otherPropertiesForm)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title of ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    "have a back button that" should {

      lazy val backLink = doc.select("a#back-link")

      "have the correct back link text" in {
        backLink.text shouldBe calcBaseBack
      }

      "have the correct back link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "have the correct back link" in {
        //merge other reliefs first
      }
    }

    "have a h1 tag that" should {

      lazy val h1Tag = doc.select("h1")

      s"have the page heading '${messages.pageHeading}'" in {
        h1Tag.hasClass("visuallyhidden") shouldBe true
      }
    }

    "have a form" which {

      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.resident.routes.DeductionsController.submitOtherProperties().toString}'" in {
        form.attr("action") shouldBe controllers.resident.routes.DeductionsController.submitOtherProperties().toString()
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }
    }
  }
}

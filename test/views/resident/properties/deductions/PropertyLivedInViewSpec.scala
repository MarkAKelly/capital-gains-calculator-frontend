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

package views.resident.properties.deductions

import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{deductions => views}
import assets.MessageLookup.{propertyLivedIn => messages}
import assets.{MessageLookup => commonMessages}
import forms.resident.properties.PropertyLivedInForm._

class PropertyLivedInViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Property lived in view with an empty form" should {

    lazy val view = views.propertyLivedIn(propertyLivedInForm, "home-link", Some("back-link"))(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    "have a H1 tag that" should {

      lazy val h1Tag = doc.select("h1")

      s"have the page heading '${messages.title}'" in {
        h1Tag.text shouldBe messages.title
      }

      "have the heading-large class" in {
        h1Tag.hasClass("heading-large") shouldBe true
      }
    }

    s"have the home link to 'home'" in {
      doc.select("#homeNavHref").attr("href") shouldEqual "home-link"
    }

    "have a back button" which {

      lazy val backLink = doc.select("a#back-link")

      "has the correct back link text" in {
        backLink.text shouldBe commonMessages.calcBaseBack
      }

      "has the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "has a back link to 'back'" in {
        backLink.attr("href") shouldBe "back-link"
      }
    }

//    "render a form tag with a submit action" in {
//      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/properties/property-lived-in"
//    }
  }
}

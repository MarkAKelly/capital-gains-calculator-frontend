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

package views.resident.properties

import assets.{MessageLookup => commonMessages}
import assets.MessageLookup.{introductionView => messages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.{properties => views}
import controllers.resident.properties.routes.{GainController => routes}

class IntroductionViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Introduction view" should {

    lazy val view = views.introduction()(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    def scopedSelector(selector: String): String = s"article.content__body $selector"

    val headingSelector = scopedSelector("h1")
    val subheadingSelector = scopedSelector("h2")
    val helpTextSelector = scopedSelector("p:nth-of-type(1)")
    val entitlementLinkSelector = scopedSelector("a:nth-of-type(1)")
    val continueLinkSelector = scopedSelector("a:nth-of-type(2)")

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    "have the correct title" in {
      doc.select(headingSelector).text shouldBe messages.title
    }

    "have the correct sub-heading" in {
      doc.select(subheadingSelector).text.trim shouldBe messages.subheading
    }

    "have the correct paragraph text" in {
      doc.select(helpTextSelector).text.trim shouldBe messages.paragraph
    }

    "have the correct hyperlink text" in {
      doc.select(entitlementLinkSelector).text.trim shouldBe messages.entitledLinkText
    }

    "have the correct URL referenced too by the hyperlink" in {
      doc.select(entitlementLinkSelector).attr("href") shouldBe "https://www.gov.uk/tax-relief-selling-home"
    }

    "have the correct continuation instructions" in {
      doc.select("article.content__body p:nth-of-type(2)").text.trim shouldBe messages.continuationInstructions
    }

    "have the correct hyperlink text for Continue" in {
      doc.select(continueLinkSelector).text.trim shouldBe commonMessages.calcBaseContinue
    }

    "have the correct URL reference too for Continue" in {
      doc.select(continueLinkSelector).attr("href") shouldBe routes.disposalDate.toString
    }

  }
}
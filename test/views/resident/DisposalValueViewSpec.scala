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
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup
import play.api.test.FakeRequest

class DisposalValueViewSpec extends UnitSpec with WithFakeApplication {

  "Disposal Value View" should {

    val fakeRequest = FakeRequest("GET", "")

    "have charset UTF-8" in {
      val view = views.html.calculation.resident.disposalValue()(fakeRequest)
      val doc = Jsoup.parse(view.body)
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have the title of the page ${MessageLookup.disposalValueTitle}" in {
      val view = views.html.calculation.resident.disposalValue()(fakeRequest)
      val doc = Jsoup.parse(view.body)
      doc.title shouldEqual MessageLookup.disposalValueTitle
    }

    s"have the question of the page ${MessageLookup.disposalValueQuestion}" in {
      val view = views.html.calculation.resident.disposalValue()(fakeRequest)
      val doc = Jsoup.parse(view.body)
      doc.select("h1.visuallyhidden").text() shouldEqual MessageLookup.disposalValueQuestion
    }

    s"have bullet point list title of ${MessageLookup.disposalValueBulletListTitle}" in {
      val view = views.html.calculation.resident.disposalValue()(fakeRequest)
      val doc = Jsoup.parse(view.body)
      doc.select("div.indent p#bullet-list-title").text() shouldEqual MessageLookup.disposalValueBulletListTitle
    }

    s"have first bullet point of ${MessageLookup.disposalValueBulletListOne}" in {
      val view = views.html.calculation.resident.disposalValue()(fakeRequest)
      val doc = Jsoup.parse(view.body)
      doc.select("div.indent li#bullet-list-one").text() shouldEqual MessageLookup.disposalValueBulletListOne
    }

    s"have second bullet point of ${MessageLookup.disposalValueBulletListTwo} with link text ${MessageLookup.disposalValueBulletListTwoLink}" in {
      val view = views.html.calculation.resident.disposalValue()(fakeRequest)
      val doc = Jsoup.parse(view.body)
      doc.select("div.indent li#bullet-list-two").text() shouldEqual MessageLookup.disposalValueBulletListTwo +
      " " + MessageLookup.disposalValueBulletListTwoLink + " " + MessageLookup.calcBaseExternalLink
    }

    s"have the second bullet point link ${MessageLookup.disposalValueBulletListTwoLink} with a visually hidden content span" in {
      val view = views.html.calculation.resident.disposalValue()(fakeRequest)
      val doc = Jsoup.parse(view.body)
      doc.select("span.visuallyhidden").text() shouldEqual MessageLookup.calcBaseExternalLink
    }

    s"have third bullet point of ${MessageLookup.disposalValueBulletListThree}" in {
      val view = views.html.calculation.resident.disposalValue()(fakeRequest)
      val doc = Jsoup.parse(view.body)
      doc.select("div.indent li#bullet-list-three").text() shouldEqual MessageLookup.disposalValueBulletListThree
    }

    s"have an input field with id amount and the label displays the text of ${MessageLookup.disposalValueQuestion}" in {
      val view = views.html.calculation.resident.disposalValue()(fakeRequest)
      val doc = Jsoup.parse(view.body)
      doc.select("span.heading-large").text() shouldEqual MessageLookup.disposalValueQuestion
      doc.body.getElementById("amount").tagName() shouldEqual "input"
    }

    "have continue button " in {
      val view = views.html.calculation.resident.disposalValue()(fakeRequest)
      val doc = Jsoup.parse(view.body)
      doc.body.getElementById("continue-button").text shouldEqual MessageLookup.calcBaseContinue
    }
  }

}

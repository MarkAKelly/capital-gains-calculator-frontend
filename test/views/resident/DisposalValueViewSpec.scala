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
import forms.resident.DisposalValueForm
import play.api.test.FakeRequest
import controllers.helpers.FakeRequestHelper

class DisposalValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  case class fakePOSTRequest(value: String) {
    lazy val fakePOSTRequest = fakeRequestToPOSTWithSession(("amount", value))
    lazy val fakePOSTForm = DisposalValueForm.disposalValueForm.bind(Map(("amount", value)))
    lazy val fakePOSTView = views.html.calculation.resident.disposalValue(fakePOSTForm)(fakePOSTRequest)
    lazy val fakePOSTDoc = Jsoup.parse(fakePOSTView.body)
  }

  "Disposal Value View" should {

    val fakeRequest = FakeRequest("GET", "")
    val fakeForm = DisposalValueForm.disposalValueForm

    lazy val view = views.html.calculation.resident.disposalValue(fakeForm)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have the title of the page ${MessageLookup.disposalValue.Title}" in {
      doc.title shouldEqual MessageLookup.disposalValue.Title
    }

    s"have a back link to the Disposal Date Page with text ${MessageLookup.calcBaseBack}" in {
      doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/disposal-date"
    }

    s"have the question of the page ${MessageLookup.disposalValue.Question}" in {
      doc.select("h1.visuallyhidden").text() shouldEqual MessageLookup.disposalValue.Question
    }

    s"have bullet point list title of ${MessageLookup.disposalValue.BulletListTitle}" in {
      doc.select("div.indent p#bullet-list-title").text() shouldEqual MessageLookup.disposalValue.BulletListTitle
    }

    s"have first bullet point of ${MessageLookup.disposalValue.BulletListOne}" in {
      doc.select("div.indent li#bullet-list-one").text() shouldEqual MessageLookup.disposalValue.BulletListOne
    }

    s"have second bullet point of ${MessageLookup.disposalValue.BulletListTwo} with link text ${MessageLookup.disposalValue.BulletListTwoLink}" in {
      doc.select("div.indent li#bullet-list-two").text() shouldEqual MessageLookup.disposalValue.BulletListTwo +
        " " + MessageLookup.disposalValue.BulletListTwoLink + " " + MessageLookup.calcBaseExternalLink
    }

    s"the second bullet point link ${MessageLookup.disposalValue.BulletListTwoLink} should have a visually hidden content span" in {
      doc.select("span.visuallyhidden").text() shouldEqual MessageLookup.calcBaseExternalLink
    }

    s"the second bullet point link ${MessageLookup.disposalValue.BulletListTwoLink} should " +
      "have the address Some(https://www.gov.uk/capital-gains-tax/losses)" in {
      doc.select("a#lossesLink").attr("href") shouldEqual "https://www.gov.uk/capital-gains-tax/losses"
    }

    s"have third bullet point of ${MessageLookup.disposalValue.BulletListThree}" in {
      doc.select("div.indent li#bullet-list-three").text() shouldEqual MessageLookup.disposalValue.BulletListThree
    }

    "render a form tag with a submit action" in {
      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/disposal-value"
    }

    s"have an input field with id amount and the label displays the text of ${MessageLookup.disposalValue.Question}" in {
      doc.select("span.heading-large").text() shouldEqual MessageLookup.disposalValue.Question
      doc.body.getElementById("amount").tagName() shouldEqual "input"
    }

    "have continue button " in {
      doc.body.getElementById("continue-button").text shouldEqual MessageLookup.calcBaseContinue
    }

    s"render a view with an error message ${MessageLookup.undefinedMessage} in both a summary and a span " +
      "when supplied with an empty form" in {
      val fakePOST = fakePOSTRequest("")
      fakePOST.fakePOSTDoc.select("span.error-notification").text shouldEqual MessageLookup.undefinedMessage
      fakePOST.fakePOSTDoc.select("a#amount-error-summary").text shouldEqual MessageLookup.undefinedMessage
    }

    s"render a view with an error message ${MessageLookup.undefinedMessage} in both a summary and a span " +
      "when a number that has more than 2 decimal places is supplied" in {
      val fakePOST = fakePOSTRequest("100.0000")
      fakePOST.fakePOSTDoc.select("span.error-notification").text shouldEqual MessageLookup.undefinedMessage
      fakePOST.fakePOSTDoc.select("a#amount-error-summary").text shouldEqual MessageLookup.undefinedMessage
    }

    s"render a view with an error message ${MessageLookup.undefinedMessage} in both a summary and a span " +
      "when a number that is greater than the maximum value is supplied" in {
      val fakePOST = fakePOSTRequest("11000000000")
      fakePOST.fakePOSTDoc.select("span.error-notification").text shouldEqual MessageLookup.undefinedMessage
      fakePOST.fakePOSTDoc.select("a#amount-error-summary").text shouldEqual MessageLookup.undefinedMessage
    }

    s"render a view with an error message ${MessageLookup.undefinedMessage} in both a summary and a span " +
      "when a number that is not a positive value is supplied" in {
      val fakePOST = fakePOSTRequest("-1000")
      fakePOST.fakePOSTDoc.select("span.error-notification").text shouldEqual MessageLookup.undefinedMessage
      fakePOST.fakePOSTDoc.select("a#amount-error-summary").text shouldEqual MessageLookup.undefinedMessage
    }
  }
}

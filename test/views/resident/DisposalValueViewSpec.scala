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
import models.resident.DisposalValueModel

class DisposalValueViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Disposal Value View" should {

    val fakeRequest = FakeRequest("GET", "")
    val fakeForm = DisposalValueForm.disposalValueForm

    lazy val view = views.html.calculation.resident.disposalValue(fakeForm)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    lazy val fakePOSTRequest = fakeRequestToPOSTWithSession(("amount", ""))
    lazy val fakePOSTForm = DisposalValueForm.disposalValueForm.bind(Map(("amount", "")))
    lazy val fakePOSTView = views.html.calculation.resident.disposalValue(fakePOSTForm)(fakePOSTRequest)
    lazy val fakePOSTDoc = Jsoup.parse(fakePOSTView.body)

    "have charset UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have the title of the page ${MessageLookup.disposalValueTitle}" in {
      doc.title shouldEqual MessageLookup.disposalValueTitle
    }

    s"have the question of the page ${MessageLookup.disposalValueQuestion}" in {
      doc.select("h1.visuallyhidden").text() shouldEqual MessageLookup.disposalValueQuestion
    }

    s"have bullet point list title of ${MessageLookup.disposalValueBulletListTitle}" in {
      doc.select("div.indent p#bullet-list-title").text() shouldEqual MessageLookup.disposalValueBulletListTitle
    }

    s"have first bullet point of ${MessageLookup.disposalValueBulletListOne}" in {
      doc.select("div.indent li#bullet-list-one").text() shouldEqual MessageLookup.disposalValueBulletListOne
    }

    s"have second bullet point of ${MessageLookup.disposalValueBulletListTwo} with link text ${MessageLookup.disposalValueBulletListTwoLink}" in {
      doc.select("div.indent li#bullet-list-two").text() shouldEqual MessageLookup.disposalValueBulletListTwo +
      " " + MessageLookup.disposalValueBulletListTwoLink + " " + MessageLookup.calcBaseExternalLink
    }

    s"have the second bullet point link ${MessageLookup.disposalValueBulletListTwoLink} with a visually hidden content span" in {
      doc.select("span.visuallyhidden").text() shouldEqual MessageLookup.calcBaseExternalLink
    }

    s"have third bullet point of ${MessageLookup.disposalValueBulletListThree}" in {
      doc.select("div.indent li#bullet-list-three").text() shouldEqual MessageLookup.disposalValueBulletListThree
    }

    "render a form tag with id of with a submit action" in {
      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/disposal-value"
    }

    s"have an input field with id amount and the label displays the text of ${MessageLookup.disposalValueQuestion}" in {
      doc.select("span.heading-large").text() shouldEqual MessageLookup.disposalValueQuestion
      doc.body.getElementById("amount").tagName() shouldEqual "input"
    }

    "have continue button " in {
      doc.body.getElementById("continue-button").text shouldEqual MessageLookup.calcBaseContinue
    }

    s"render a view with an error message ${MessageLookup.undefinedMessage} when supplied with an invalid form" in {
      lazy val fakePOSTRequest = fakeRequestToPOSTWithSession(("amount", ""))
      lazy val fakePOSTForm = DisposalValueForm.disposalValueForm.bind(Map(("amount", "")))
      lazy val fakePOSTView = views.html.calculation.resident.disposalValue(fakePOSTForm)(fakePOSTRequest)
      lazy val fakePOSTDoc = Jsoup.parse(fakePOSTView.body)
      fakePOSTDoc.select("span.error-notification").text shouldEqual MessageLookup.undefinedMessage
      fakePOSTDoc.select("a#amount-error-summary").text shouldEqual MessageLookup.undefinedMessage
    }

    s"render a view with an error message ${MessageLookup.undefinedMessage} when a number that has more than 2 decimal places is supplied" in {
      lazy val fakePOSTRequest = fakeRequestToPOSTWithSession(("amount", "100.0000"))
      lazy val fakePOSTForm = DisposalValueForm.disposalValueForm.bind(Map(("amount", "100.0000")))
      lazy val fakePOSTView = views.html.calculation.resident.disposalValue(fakePOSTForm)(fakePOSTRequest)
      lazy val fakePOSTDoc = Jsoup.parse(fakePOSTView.body)
      fakePOSTDoc.select("span.error-notification").text shouldEqual MessageLookup.undefinedMessage
      fakePOSTDoc.select("a#amount-error-summary").text shouldEqual MessageLookup.undefinedMessage
    }

    s"render a view with an error message ${MessageLookup.undefinedMessage} when a number that is greater than the maximum value is supplied" in {
      lazy val fakePOSTRequest = fakeRequestToPOSTWithSession(("amount", "11000000000"))
      lazy val fakePOSTForm = DisposalValueForm.disposalValueForm.bind(Map(("amount", "11000000000")))
      lazy val fakePOSTView = views.html.calculation.resident.disposalValue(fakePOSTForm)(fakePOSTRequest)
      lazy val fakePOSTDoc = Jsoup.parse(fakePOSTView.body)
      fakePOSTDoc.select("span.error-notification").text shouldEqual MessageLookup.undefinedMessage
      fakePOSTDoc.select("a#amount-error-summary").text shouldEqual MessageLookup.undefinedMessage
    }

    s"render a view with an error message ${MessageLookup.undefinedMessage} when a number that is not a positive value is supplied" in {
      lazy val fakePOSTRequest = fakeRequestToPOSTWithSession(("amount", "-11000"))
      lazy val fakePOSTForm = DisposalValueForm.disposalValueForm.bind(Map(("amount", "-11000")))
      lazy val fakePOSTView = views.html.calculation.resident.disposalValue(fakePOSTForm)(fakePOSTRequest)
      lazy val fakePOSTDoc = Jsoup.parse(fakePOSTView.body)
      fakePOSTDoc.select("span.error-notification").text shouldEqual MessageLookup.undefinedMessage
      fakePOSTDoc.select("a#amount-error-summary").text shouldEqual MessageLookup.undefinedMessage
    }
  }
}

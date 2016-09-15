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

import assets.MessageLookup
import assets.MessageLookup.{propertiesWorthWhenGaveAway => messages}
import controllers.helpers.FakeRequestHelper
import forms.resident.properties.WorthWhenGaveAwayForm._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{gain => views}
import controllers.resident.properties.routes

class WorthWhenGaveAwayViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  case class FakePOST(value: String) {
    lazy val request = fakeRequestToPOSTWithSession(("amount", value))
    lazy val form = worthWhenGaveAwayForm.bind(Map(("amount", value)))
    lazy val backLink = Some(controllers.resident.properties.routes.GainController.whoDidYouGiveItTo().toString())
    lazy val view = views.worthWhenGaveAway(worthWhenGaveAwayForm, backLink, "home-link", routes.GainController.submitWorthWhenGaveAway())(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)
  }

  "Worth when gave away View" should {

    lazy val backLink = Some(controllers.resident.properties.routes.GainController.whoDidYouGiveItTo().toString())
    lazy val view = views.worthWhenGaveAway(worthWhenGaveAwayForm, backLink, "home-link", routes.GainController.submitWorthWhenGaveAway())(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have the title of the page ${messages.title}" in {
      doc.title shouldEqual messages.title
    }

    s"have a back link to the Who did you give it to Page with text ${MessageLookup.calcBaseBack}" in {
      doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/who-did-you-give-it-to"
    }

    s"have the question of the page ${messages.question}" in {
      doc.select("h1").text shouldEqual messages.question
    }

    }



  "Worth When Gave Away View with form without errors" should {

    val form = worthWhenGaveAwayForm.bind(Map("amount" -> "100"))
    lazy val backLink = Some(controllers.resident.properties.routes.GainController.whoDidYouGiveItTo().toString())
    lazy val view = views.worthWhenGaveAway(form, backLink, "home-link", routes.GainController.submitWorthWhenGaveAway())(fakeRequest)
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

  "Worth When Gave Away View with form with errors" should {

    val form = worthWhenGaveAwayForm.bind(Map("amount" -> ""))
    lazy val backLink = Some(controllers.resident.properties.routes.GainController.whoDidYouGiveItTo().toString())
    lazy val view = views.worthWhenGaveAway(form, backLink, "home-link", routes.GainController.submitWorthWhenGaveAway())(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }
}

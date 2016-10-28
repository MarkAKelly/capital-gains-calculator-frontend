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

package views.nonResident

import common.TestModels
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.routes
import org.jsoup.Jsoup
import play.api.i18n.Messages
import forms.nonresident.CalculationElectionForm._
import views.html.calculation.{nonresident => views}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.NonResident.{CalculationElection => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import models.nonresident.{CalculationElectionModel, CalculationResultModel, SummaryModel}
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.SessionKeys

import scala.concurrent.Future



class CalculationElectionViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "The Calculation Election View" should {

    lazy val form = calculationElectionForm
    lazy val summaryModel = TestModels.sumModelFlat
    lazy val seq: Seq[(String, String, String, Option[String], String, Option[BigDecimal])] =
      Seq(("flat", "2000", Messages("calc.calculationElection.message.flat"), None,
      routes.OtherReliefsController.otherReliefs().toString(), Some(BigDecimal(500.0))))
    lazy val view = views.calculationElection(form, summaryModel, seq)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    "have a h1 tag that" should {

      s"have the question of ${messages.heading}" in {
        doc.select("h1").text() shouldBe messages.heading
      }

      "have the heading-xlarge class" in {
        doc.select("h1").hasClass("heading-xlarge") shouldBe true
      }
    }

    "have the home link to 'home'" in {
      doc.select("#homeNavHref").attr("href") shouldBe "/calculate-your-capital-gains/non-resident/"
    }

    "have a back button" which {

      "has the correct back link text" in {
        doc.select("a#back-link").text shouldBe commonMessages.back
      }

      "has the back-link class" in {
        doc.select("a#back-link").hasClass("back-link") shouldBe true
      }

      "has a back link to 'back'" in {
        doc.select("a#back-link").attr("href") shouldBe "/calculate-your-capital-gains/non-resident/allowable-losses"
      }
    }

    "render a form tag" which {

      lazy val form = doc.select("form")

      "has a submit action" in {
        form.attr("action") shouldEqual "/calculate-your-capital-gains/non-resident/calculation-election"
      }

      "with method type POST" in {
        form.attr("method") shouldBe "POST"
      }
    }

    "have a read more sidebar" which {

      s"contains the text ${commonMessages.readMore}" in {
        doc.select("aside h2").text shouldBe commonMessages.readMore
      }

      s"contains the link ${commonMessages.externalLink}" in {
        doc.select("aside a").text shouldBe s"${messages.linkOne} ${commonMessages.externalLink}"
      }
    }

    "contains a h2 heading" which {

      "has the class of heading-small" in {
        doc.select("h2").hasClass("heading-small") shouldBe true
      }

      s"contains the text ${messages.moreInformation}" in {
        doc.body().getElementsByTag("h2").text should include (messages.moreInformation)
      }
    }

    "have the text in paragraphs" which {

      s"contains the text ${messages.moreInfoFirstP}" in {
        doc.body().getElementsByTag("p").text should include (messages.moreInfoFirstP)
      }

      s"contains the text ${messages.moreInfoSecondP}" in {
        doc.body().getElementsByTag("p").text should include (messages.moreInfoSecondP)
      }

      s"contains the text ${messages.moreInfoThirdP}" in {
        doc.body().getElementsByTag("p").text should include (messages.moreInfoThirdP)
      }
    }

    "display a 'Continue' button " in {
      doc.body.getElementById("continue-button").text shouldEqual commonMessages.continue
    }

    s"display a concertina information box with '${messages.whyMore} " +
      s"${Messages("calc.calculationElection.whyMoreDetails.two")}' as the content" in {
      doc.select("summary span.summary").text shouldEqual messages.whyMore
      doc.select("div#details-content-0 p").text should include (messages.whyMoreDetailsOne)
      doc.select("div#details-content-0 p").text should include (messages.whyMoreDetailsTwo)
    }

    "have no pre-selected option" in {
      doc.body.getElementById("calculationElection-flat").parent.classNames().contains("selected") shouldBe false
    }

    "supplied with errors" should {
      lazy val form = calculationElectionForm.bind(Map("calculationElection" -> "a"))
      lazy val view = views.calculationElection(form, summaryModel, seq)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      "have an error summary" in {
        document.select("#error-summary-display").size() shouldBe 1
      }
    }
  }

}

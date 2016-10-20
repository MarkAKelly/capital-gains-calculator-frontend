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

package controllers.CalculationControllerTests

import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{DisposalValue => messages}

import common.Constants
import connectors.CalculatorConnector
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup._
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future
import controllers.nonresident.{DisposalValueController, routes}
import models.nonresident.DisposalValueModel
import play.api.mvc.Result
import uk.gov.hmrc.play.views.helpers.MoneyPounds

class DisposalValueSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[DisposalValueModel],postData: Option[DisposalValueModel]): DisposalValueController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[DisposalValueModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    lazy val data = CacheMap("form-id", Map("data" -> Json.toJson(postData.getOrElse(DisposalValueModel(0)))))
    when(mockCalcConnector.saveFormData[DisposalValueModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))

    new DisposalValueController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  //GET Tests
  "In CalculationController calling the .disposalValue action " when {

    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/non-resident/disposal-value").withSession(SessionKeys.sessionId -> "12345")

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None, None)
      lazy val result = target.disposalValue(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        s"have the title ${messages.question}" in {
          document.title shouldEqual messages.question
        }

        s"have the heading ${commonMessages.pageHeading}" in {
          document.body.getElementsByTag("h1").text shouldEqual commonMessages.pageHeading
        }

        s"have a 'Back' link to ${routes.DisposalDateController.disposalDate()}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
          document.body.getElementById("back-link").attr("href") shouldEqual routes.DisposalDateController.disposalDate().toString()
        }

        s"have the question ${messages.question} as the legend of the input" in {
          document.body.getElementsByTag("label").text should include (messages.question)
        }

        "have the bullet list content title and content" in {
          document.select("p#bullet-list-title").text shouldEqual messages.bulletIntro
        }

        "Have the bullet content" in {
          document.select("ul li").text should include(messages.bulletOne)
          document.select("ul li").text should include(messages.bulletTwo)
          document.select("ul li").text should include(messages.bulletThree)
        }

        "have a link with a hidden external link field" in {
          document.select("ul li a#lossesLink").text should include(messages.bulletTwoLink)
          document.select("span#opensInANewWindow").text shouldEqual commonMessages.externalLink
        }

        "display an input box for the Annual Exempt Amount" in {
          document.body.getElementById("disposalValue").tagName() shouldEqual "input"
        }

        "display a 'Continue' button " in {
          document.body.getElementById("continue-button").text shouldEqual commonMessages.continue
        }
      }
    }


    "supplied with a pre-existing stored model" should {

      val target = setupTarget(Some(DisposalValueModel(1000)), None)
      lazy val result = target.disposalValue(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "have the value 1000 auto-filled into the input box" in {
          document.getElementById("disposalValue").attr("value") shouldEqual "1000"
        }
      }
    }
  }

  "In CalculationController calling the .submitDisposalValue action" when {

    def buildRequest(body: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST",
      "/calculate-your-capital-gains/non-resident/disposal-value")
      .withSession(SessionKeys.sessionId -> "12345")
      .withFormUrlEncodedBody(body: _*)

    def executeTargetWithMockData(data: String): Future[Result] = {
      lazy val fakeRequest = buildRequest(("disposalValue", data))
      val mockData = data match {
        case "" => None
        case _ => Some(DisposalValueModel(BigDecimal(data)))
      }
      val target = setupTarget(None, mockData)
      target.submitDisposalValue(fakeRequest)
    }

    "submitting a valid form" should {

      lazy val result = executeTargetWithMockData("1000")

      "return a 303" in {
        status(result) shouldBe 303
      }
    }

    "submitting an invalid form with no value" should {

      lazy val result = executeTargetWithMockData("")

      "return a 400" in {
        status(result) shouldBe 400
      }
    }

    "submitting an invalid form with a negative value" should {

      lazy val result = executeTargetWithMockData("-1000")

      "return a 400" in {
        status(result) shouldBe 400
      }
    }

    "submitting an invalid form with value 1.111" should {

      lazy val result = executeTargetWithMockData("1.111")
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"fail with message ${messages.errorDecimalPlaces}" in {
        document.getElementsByClass("error-notification").text should include (messages.errorDecimalPlaces)
      }
    }

    "submitting a value which exceeds the maximum numeric" should {

      lazy val result = executeTargetWithMockData((Constants.maxNumeric + 0.01).toString)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"fail with message ${commonMessages.maximumLimit(MoneyPounds(Constants.maxNumeric, 0).quantity)}" in {
        document.getElementsByClass("error-notification").text should
          include (commonMessages.maximumLimit(MoneyPounds(Constants.maxNumeric, 0).quantity))
      }
    }
  }
}

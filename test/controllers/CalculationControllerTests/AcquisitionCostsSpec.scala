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

import assets.MessageLookup
import common.Constants
import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import controllers.nonresident.{AcquisitionCostsController, CalculationController, routes}
import models.nonresident.AcquisitionCostsModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.play.views.helpers.MoneyPounds

import scala.concurrent.Future

class AcquisitionCostsSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  implicit val hc = new HeaderCarrier()
  def setupTarget(getData: Option[AcquisitionCostsModel], postData: Option[AcquisitionCostsModel]): AcquisitionCostsController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockCalcElectionConstructor = mock[CalculationElectionConstructor]

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionCostsModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    lazy val data = CacheMap("form-id", Map("data" -> Json.toJson(postData.getOrElse(AcquisitionCostsModel(0)))))
    when(mockCalcConnector.saveFormData[AcquisitionCostsModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))

    new AcquisitionCostsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val calcElectionConstructor: CalculationElectionConstructor = mockCalcElectionConstructor
    }
  }

  "In CalculationController calling the .acquisitionCosts action " should {

    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/non-resident/acquisition-costs").withSession(SessionKeys.sessionId -> "12345")

    "not supplied with a pre-existing stored model" should {
      val target = setupTarget(None, None)
      lazy val result = target.acquisitionCosts(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "have the title 'How much did you pay in costs when you became the property owner'" in {
          document.getElementsByTag("title").text shouldEqual Messages("calc.acquisitionCosts.question")
        }

        s"have a 'Back' link to ${routes.CalculationController.disposalValue}" in {
          document.body.getElementById("back-link").text shouldEqual Messages("calc.base.back")
          document.body.getElementById("back-link").attr("href") shouldEqual routes.CalculationController.disposalValue.toString()
        }

        "have the page heading 'Calculate your tax (non-residents)'" in {
          document.getElementsByTag("h1").text shouldEqual Messages("calc.base.pageHeading")
        }

        "have a monetary field that" should {

          "have the title 'How much did you pay in costs when you became the property owner?'" in {
            document.select("label[for=acquisitionCosts]").text should include (Messages("calc.acquisitionCosts.question"))
          }

          "have the help text 'Costs include agent fees, legal fees and surveys'" in {
            document.select("span.form-hint").text shouldEqual Messages("calc.acquisitionCosts.helpText")
          }

          "have an input box for the acquisition costs" in {
            document.getElementById("acquisitionCosts").tagName shouldBe "input"
          }
        }

        "have a continue button that" should {

          "be a button element" in {
            document.getElementById("continue-button").tagName shouldBe "button"
          }

          "have the text 'Continue'" in {
            document.getElementById("continue-button").text shouldEqual Messages("calc.base.continue")
          }
        }
      }
    }

    "supplied with a pre-existing stored model" should {
      val testAcquisitionCostsModel = new AcquisitionCostsModel(1000)
      val target = setupTarget(Some(testAcquisitionCostsModel), None)
      lazy val result = target.acquisitionCosts(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some HTML that" should {
        "have the value 1000 auto-filled into the input box" in {
            document.getElementById("acquisitionCosts").attr("value") shouldEqual "1000"
        }
      }
    }
  }

  "In CalculationController calling the .submitAcquisitionCosts action" when {
    def buildRequest(body: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST",
      "/calculate-your-capital-gains/non-resident/acquisition-date")
      .withSession(SessionKeys.sessionId -> "12345")
      .withFormUrlEncodedBody(body: _*)

    def executeTargetWithMockData
    (
      acquisitionCosts: String = ""
    ): Future[Result] = {
      lazy val fakeRequest = buildRequest(
        ("acquisitionCosts", acquisitionCosts))

      val numeric = "(0-9*)".r
      val mockData = acquisitionCosts match {
        case numeric(money) => new AcquisitionCostsModel(BigDecimal(money))
        case _ => new AcquisitionCostsModel(0)
      }
      val target = setupTarget(None, Some(mockData))
      target.submitAcquisitionCosts(fakeRequest)
    }

    "submitting a valid form" should {

      "with value 1000" should {
        lazy val result = executeTargetWithMockData("1000")

        "return a 303" in {
          status(result) shouldBe 303
        }

        s"redirect to ${routes.DisposalCostsController.disposalCosts()}" in {
          redirectLocation(result) shouldBe Some(s"${routes.DisposalCostsController.disposalCosts()}")
        }
      }

      "with no value" should {
        lazy val result = executeTargetWithMockData("")
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 400" in {
          status(result) shouldBe 400
        }

        "fail with message 'Enter a number without commas, for example 10000.00'" in {
          document.getElementsByClass("error-notification").text should include (MessageLookup.errorMessages.numericPlayErrorOverride)
        }
      }
    }

    "submitting an invalid form" should {
      val testModel = new AcquisitionCostsModel(0)

      "with value -1" should {
        lazy val result = executeTargetWithMockData("-1")
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 400" in {
          status(result) shouldBe 400
        }

        s"fail with message ${Messages("calc.acquisitionCosts.errorNegative")}" in {
          document.getElementsByClass("error-notification").text should include (Messages("calc.acquisitionCosts.errorNegative"))
        }

        "display a visible Error Summary field" in {
          document.getElementById("error-summary-display").hasClass("error-summary--show")
        }

        "link to the invalid input box in Error Summary" in {
          document.getElementById("acquisitionCosts-error-summary").attr("href") should include ("#acquisitionCosts")
        }
      }

      "with value 1.111" should {
        lazy val result = executeTargetWithMockData("1.111")
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 400" in {
          status(result) shouldBe 400
        }

        s"fail with message ${Messages("calc.acquisitionCosts.errorDecimalPlaces")}" in {
          document.getElementsByClass("error-notification").text should include(Messages("calc.acquisitionCosts.errorDecimalPlaces"))
        }

        "display a visible Error Summary field" in {
          document.getElementById("error-summary-display").hasClass("error-summary--show")
        }

        "link to the invalid input box in Error Summary" in {
          document.getElementById("acquisitionCosts-error-summary").attr("href") should include ("#acquisitionCosts")
        }
      }

      "submitting a value which exceeds the maximum numeric" should {
        lazy val result = executeTargetWithMockData((Constants.maxNumeric + 0.01).toString)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 400" in {
          status(result) shouldBe 400
        }

        s"fail with message ${Messages("calc.common.error.maxNumericExceeded")}" in {
          document.getElementsByClass("error-notification").text should
            include (Messages("calc.common.error.maxNumericExceeded") + MoneyPounds(Constants.maxNumeric, 0).quantity +
              " " + Messages("calc.common.error.maxNumericExceeded.OrLess"))
        }

        "display a visible Error Summary field" in {
          document.getElementById("error-summary-display").hasClass("error-summary--show")
        }

        "link to the invalid input box in Error Summary" in {
          document.getElementById("acquisitionCosts-error-summary").attr("href") should include ("#acquisitionCosts")
        }
      }
    }
  }

}

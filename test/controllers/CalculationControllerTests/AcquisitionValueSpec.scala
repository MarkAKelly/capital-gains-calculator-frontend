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

import common.{Constants, KeystoreKeys}
import connectors.CalculatorConnector
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap
import constructors.nonresident.CalculationElectionConstructor
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup._
import org.scalatest.mock.MockitoSugar
import assets.MessageLookup
import assets.MessageLookup.NonResident.{AcquisitionValue => messages, Common}
import scala.concurrent.Future
import controllers.nonresident.{AcquisitionValueController, routes}
import models.nonresident.{AcquisitionDateModel, AcquisitionValueModel}
import play.api.mvc.Result
import uk.gov.hmrc.play.views.helpers.MoneyPounds

class AcquisitionValueSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget(
                   getData: Option[AcquisitionValueModel],
                   postData: Option[AcquisitionValueModel],
                   acquisitionDateModel: Option[AcquisitionDateModel] = None): AcquisitionValueController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockCalcElectionConstructor = mock[CalculationElectionConstructor]

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionValueModel](Matchers.eq(KeystoreKeys.acquisitionValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(acquisitionDateModel))

    lazy val data = CacheMap("form-id", Map("data" -> Json.toJson(postData.getOrElse(AcquisitionValueModel(0)))))
    when(mockCalcConnector.saveFormData[AcquisitionValueModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))

    new AcquisitionValueController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val calcElectionConstructor: CalculationElectionConstructor = mockCalcElectionConstructor
    }
  }

  // GET Tests
  "Calling the CalculationController.acquisitionValue" when {

    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/non-resident/acquisition-value").withSession(SessionKeys.sessionId -> "12345")

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None, None)
      lazy val result = target.acquisitionValue(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "have the title 'How much did you pay for the property?'" in {
          document.title shouldEqual messages.question
        }

        "have the heading Calculate your tax (non-residents) " in {
          document.body.getElementsByTag("h1").text shouldEqual Common.pageHeading
        }

        s"have a 'Back' link to ${routes.AcquisitionDateController.acquisitionDate()}" in {
          document.body.getElementById("back-link").text shouldEqual MessageLookup.calcBaseBack
          document.body.getElementById("back-link").attr("href") shouldEqual routes.AcquisitionDateController.acquisitionDate().toString()
        }

        "have the question 'How much did you pay for the property?'" in {
          document.body.getElementsByTag("label").text should include (messages.question)
        }

        "have the bullet list content title and content" in {
          document.select("p#bullet-list-title").text shouldEqual messages.bulletTitle
        }

        "Have the bullet content" in {
          document.select("ul li").text should include(messages.bulletOne)
          document.select("ul li").text should include(messages.bulletTwo)
          document.select("ul li").text should include(messages.bulletThree)
          document.select("ul li").text should include(messages.bulletFour)
          document.select("ul li").text should include(messages.bulletFive)
        }
        "have a link with a hidden external link field" in {
          document.select("ul li a#lossesLink").text should include(messages.bulletLink)
          document.select("span#opensInANewTab").text shouldEqual MessageLookup.calcBaseExternalLink
        }
        "display an input box for the Acquisition Value" in {
          document.body.getElementById("acquisitionValue").tagName shouldEqual "input"
        }
        "have no value auto-filled into the input box" in {
          document.getElementById("acquisitionValue").attr("value") shouldEqual ""
        }
        "display a 'Continue' button " in {
          document.body.getElementById("continue-button").text shouldEqual MessageLookup.calcBaseContinue
        }
      }
    }

    "supplied with a pre-existing stored model" should {

      val target = setupTarget(Some(AcquisitionValueModel(1000)), None)
      lazy val result = target.acquisitionValue(fakeRequest)
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
          document.getElementById("acquisitionValue").attr("value") shouldEqual "1000"
        }
      }
    }
  }

  // POST Tests
  "In CalculationController calling the .submitAcquisitionValue action" when {

    def buildRequest(body: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST",
      "/calculate-your-capital-gains/non-resident/acquisition-value")
      .withSession(SessionKeys.sessionId -> "12345")
      .withFormUrlEncodedBody(body: _*)

    def executeTargetWithMockData(data: String, mockDate: Option[AcquisitionDateModel]): Future[Result] = {
      lazy val fakeRequest = buildRequest(("acquisitionValue", data))
      val mockData = data match {
        case "" => None
        case _ => Some(AcquisitionValueModel(BigDecimal(data)))
      }
      val target = setupTarget(None, mockData, mockDate)
      target.submitAcquisitionValue(fakeRequest)
    }

    s"return a 303 to ${routes.ImprovementsController.improvements()}" in {

      val acquisitionDateModelYesAfterStartDate = new AcquisitionDateModel("Yes", Some(10), Some(10), Some(2017))
      lazy val result = executeTargetWithMockData("1000", Some(acquisitionDateModelYesAfterStartDate))

      status(result) shouldBe 303
      redirectLocation(result) shouldBe Some(s"${routes.ImprovementsController.improvements()}")
    }

    "submitting a valid form with a date before 5 5 2015" should {

      val date = new AcquisitionDateModel("Yes", Some(10), Some(10), Some(2010))
      lazy val result = executeTargetWithMockData("1000", Some(date))

      s"return a 303 to ${routes.RebasedValueController.rebasedValue()}" in {
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(s"${routes.RebasedValueController.rebasedValue()}")
      }
    }

    "submitting a valid form with No date supplied" should {

      val noDate = new AcquisitionDateModel("No", None, None, None)
      lazy val result = executeTargetWithMockData("1000", Some(noDate))

      s"return a 303 to ${routes.RebasedValueController.rebasedValue()}" in {
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(s"${routes.RebasedValueController.rebasedValue()}")
      }
    }

    "submitting an invalid form with no value" should {

      lazy val result = executeTargetWithMockData("", None)

      "return a 400" in {
        status(result) shouldBe 400
      }
    }

    "submitting an invalid form with a negative value" should {

      lazy val result = executeTargetWithMockData("-1000", None)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"fail with message ${messages.errorNegative}" in {
        document.getElementsByClass("error-notification").text should include (messages.errorNegative)
      }
    }

    "submitting an invalid form with value 1.111" should {

      lazy val result = executeTargetWithMockData("1.111", None)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"fail with message ${messages.errorDecimalPlaces}" in {
        document.getElementsByClass("error-notification").text should include (messages.errorDecimalPlaces)
      }
    }

    "submitting a value which exceeds the maximum numeric" should {

      lazy val result = executeTargetWithMockData((Constants.maxNumeric + 0.01).toString(), None)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"fail with message ${messages.errorMaximum(MoneyPounds(Constants.maxNumeric, 0).quantity)}" in {
        document.getElementsByClass("error-notification").text should
          include (messages.errorMaximum(MoneyPounds(Constants.maxNumeric, 0).quantity))
      }
    }
  }
}

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
import assets.MessageLookup.NonResident.{DisposalDate => messages}

import common.KeystoreKeys
import connectors.CalculatorConnector
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup._
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future
import controllers.nonresident.{DisposalDateController, routes}
import models.nonresident.{AcquisitionDateModel, DisposalDateModel}
import play.api.mvc.Result

class DisposalDateSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[DisposalDateModel],
                  postData: Option[DisposalDateModel],
                  acquisitionData: Option[AcquisitionDateModel] = None
                 ): DisposalDateController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](Matchers.eq(KeystoreKeys.disposalDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(acquisitionData))

    lazy val data = CacheMap("form-id", Map("data" -> Json.toJson(postData.getOrElse(DisposalDateModel(1, 1, 1)))))
    when(mockCalcConnector.saveFormData[DisposalDateModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))

    new DisposalDateController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  // GET Tests
  "Calling the CalculationController.disposalDate" when {

    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/non-resident/disposal-date").withSession(SessionKeys.sessionId -> "12345")

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None, None)
      lazy val result = target.disposalDate(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }
    }

    "supplied with a model already filled with data" should {

      val target = setupTarget(Some(DisposalDateModel(10, 12, 2016)), None)
      lazy val result = target.disposalDate(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some HTML that" should {
        "contain some text and use the character set utf-8" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "be pre-populated with the date 10, 12, 2016" in {
          document.body.getElementById("disposalDateDay").attr("value") shouldEqual "10"
          document.body.getElementById("disposalDateMonth").attr("value") shouldEqual "12"
          document.body.getElementById("disposalDateYear").attr("value") shouldEqual "2016"
        }
      }
    }
  }

  // POST Tests
  "In CalculationController calling the .submitDisposalDate action" when {

    val numeric = "([0-9]+)".r

    def getIntOrDefault(input: String): Int = input match {
      case numeric(number) => number.toInt
      case _ => 0
    }

    def buildRequest(body: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST",
      "/calculate-your-capital-gains/non-resident/disposal-date")
      .withSession(SessionKeys.sessionId -> "12345")
      .withFormUrlEncodedBody(body: _*)

    def executeTargetWithMockData(day: String, month: String, year: String, acquisitionDateData: Option[AcquisitionDateModel] = None): Future[Result] = {
      lazy val fakeRequest = buildRequest(("disposalDateDay", day), ("disposalDateMonth", month), ("disposalDateYear", year))
      val mockData = new DisposalDateModel(getIntOrDefault(day), getIntOrDefault(month), getIntOrDefault(year))
      val target = setupTarget(None, Some(mockData), acquisitionDateData)
      target.submitDisposalDate(fakeRequest)
    }

    "submitting a valid date 31/01/2016" should {

      lazy val result = executeTargetWithMockData("31", "1", "2016")

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.DisposalValueController.disposalValue()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.DisposalValueController.disposalValue()}")
      }
    }

    "submitting a valid leap year date 29/02/2016" should {

      lazy val result = executeTargetWithMockData("29", "2", "2016")

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.DisposalValueController.disposalValue()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.DisposalValueController.disposalValue()}")
      }
    }

    "submitting a disposal date of 22/02/1990 which is before acquisition date 22/01/2000" should {

      lazy val result = executeTargetWithMockData("22", "2", "1990", Some(AcquisitionDateModel("Yes",Some(22),Some(1),Some(2000))))
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"should error with message '${messages.errorDateAfter}'" in {
        document.select(".error-notification").text should include (messages.errorDateAfter)
      }
    }

    "submitting an invalid leap year date 29/02/2017" should {

      lazy val result = executeTargetWithMockData("29", "2", "2017")
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"should error with message '${commonMessages.errorInvalidDate}'" in {
        document.select(".error-notification").text should include (commonMessages.errorInvalidDate)
      }
    }

    "submitting a day less than 1" should {

      lazy val result = executeTargetWithMockData("0", "2", "2017")
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"should error with message '${commonMessages.errorInvalidDate}'" in {
        document.select(".error-notification").text should include (commonMessages.errorInvalidDate)
      }
    }

    "submitting a day greater than 31" should {

      lazy val result = executeTargetWithMockData("32", "2", "2017")
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"should error with message '${commonMessages.errorInvalidDate}'" in {
        document.select(".error-notification").text should include (commonMessages.errorInvalidDate)
      }
    }

    "submitting a month greater than 12" should {

      lazy val result = executeTargetWithMockData("31", "13", "2017")
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"should error with message '${commonMessages.errorInvalidDate}'" in {
        document.select(".error-notification").text should include (commonMessages.errorInvalidDate)
      }
    }

    "submitting a month less than 1" should {

      lazy val result = executeTargetWithMockData("31", "0", "2017")
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"should error with message '${commonMessages.errorInvalidDate}'" in {
        document.select(".error-notification").text should include (commonMessages.errorInvalidDate)
      }
    }

    "submitting a day with no value" should {

      lazy val result = executeTargetWithMockData("", "12", "2017")
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"should error with message ${commonMessages.errorInvalidDate}" in {
        document.select(".error-notification").text should include (commonMessages.errorInvalidDate)
      }
    }

    "submitting a month with no value" should {

      lazy val result = executeTargetWithMockData("31", "", "2017")
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"should error with message ${commonMessages.errorInvalidDate}" in {
        document.select(".error-notification").text should include (commonMessages.errorInvalidDate)
      }
    }

    "submitting a year with no value" should {

      lazy val result = executeTargetWithMockData("31", "12", "")
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"should error with message ${commonMessages.errorInvalidDate}" in {
        document.select(".error-notification").text should include (commonMessages.errorInvalidDate)
      }
    }
  }
}

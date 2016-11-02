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
import assets.MessageLookup.NonResident.{PersonalAllowance => messages}
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
import controllers.nonresident.{PersonalAllowanceController, routes}
import models.nonresident.PersonalAllowanceModel
import play.api.mvc.Result

class PersonalAllowanceSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[PersonalAllowanceModel], postData: Option[PersonalAllowanceModel]): PersonalAllowanceController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[PersonalAllowanceModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.getPA(Matchers.anyInt(), Matchers.anyBoolean())(Matchers.any()))
      .thenReturn(Some(BigDecimal(11000)))

    lazy val data = CacheMap("form-id", Map("data" -> Json.toJson(postData.getOrElse(PersonalAllowanceModel(0)))))
    when(mockCalcConnector.saveFormData[PersonalAllowanceModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))

    new PersonalAllowanceController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
     
    }
  }

  // GET Tests
  "Calling the PersonalAllowanceController.customerType" when {

    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/non-resident/personal-allowance").withSession(SessionKeys.sessionId -> "12345")

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None, None)
      lazy val result = target.personalAllowance(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the title ${messages.question}" in {
        document.title shouldEqual messages.question
      }
    }
  }

  // POST Tests
  "In PersonalAllowanceController calling the .submitPersonalAllowance action" when {

    def buildRequest(body: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST",
      "/calculate-your-capital-gains/non-resident/personal-allowance")
      .withSession(SessionKeys.sessionId -> "12345")
      .withFormUrlEncodedBody(body: _*)

    def executeTargetWithMockData(data: String): Future[Result] = {
      lazy val fakeRequest = buildRequest(("personalAllowance", data))
      val mockData = data match {
        case "" => None
        case _ => Some(PersonalAllowanceModel(BigDecimal(data)))
      }
      val target = setupTarget(None, mockData)
      target.submitPersonalAllowance(fakeRequest)
    }

    "submitting a valid form with '1000'" should {

      lazy val result = executeTargetWithMockData("1000")

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.OtherPropertiesController.otherProperties()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherPropertiesController.otherProperties()}")
      }


      "submitting an invalid form with no value" should {

        lazy val result = executeTargetWithMockData("")

        "return a 400" in {
          status(result) shouldBe 400
        }
      }

      "submitting an invalid form with a negative value of -342" should {

        lazy val result = executeTargetWithMockData("-342")

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
          document.getElementsByClass("error-notification").text should include(messages.errorDecimalPlaces)
        }
      }

      "submitting a form which exceeds the maximum PA amount" should {

        lazy val result = executeTargetWithMockData("11001")
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 400" in {
          status(result) shouldBe 400
        }

        s"fail with message ${messages.errorMaxLimit}" in {
          document.getElementsByClass("error-notification").text should include(messages.errorMaxLimit)
        }
      }

      "submitting a form which exceeds the maximum PA amount and has fractional pounds" should {

        lazy val result = executeTargetWithMockData("12100.01")
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 400" in {
          status(result) shouldBe 400
        }

        s"fail with message ${messages.errorMaxLimit}" in {
          document.getElementsByClass("error-notification").text should include(messages.errorMaxLimit)
          document.getElementsByClass("error-notification").text should include(messages.errorDecimalPlaces)
        }
      }
    }
  }
}

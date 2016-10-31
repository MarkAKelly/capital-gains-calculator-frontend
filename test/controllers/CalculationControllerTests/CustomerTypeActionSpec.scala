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
import assets.MessageLookup.NonResident.{CustomerType => messages}

import common.nonresident.CustomerTypeKeys
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

import scala.concurrent.Future
import controllers.nonresident.CustomerTypeController
import models.nonresident.CustomerTypeModel
import play.api.mvc.Result

class CustomerTypeActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[CustomerTypeModel], postData: Option[CustomerTypeModel]): CustomerTypeController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockCalcElectionConstructor = mock[CalculationElectionConstructor]

    when(mockCalcConnector.fetchAndGetFormData[CustomerTypeModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    lazy val data = CacheMap("form-id", Map("data" -> Json.toJson(postData.getOrElse(CustomerTypeModel("")))))
    when(mockCalcConnector.saveFormData[CustomerTypeModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))

    new CustomerTypeController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val calcElectionConstructor: CalculationElectionConstructor = mockCalcElectionConstructor
    }
  }

  // GET Tests
  "Calling the CalculationController.customerType" when {

    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/non-resident/customer-type").withSession(SessionKeys.sessionId -> "12345")

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None, None)
      lazy val result = target.customerType(fakeRequest)

      "return a 200" in {
        status(result) shouldBe 200
      }
    }

    "supplied with a pre-existing stored model" should {

      val target = setupTarget(Some(CustomerTypeModel(CustomerTypeKeys.individual)), None)
      lazy val result = target.customerType(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "have the radio option `individual` selected by default" in {
          document.body.getElementById("customerType-individual").parent.classNames().contains("selected") shouldBe true
        }
      }
    }
  }

  // POST Tests
  "In CalculationController calling the .submitCustomerType action" when {

    def buildRequest(body: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST",
      "/calculate-your-capital-gains/non-resident/customer-type")
      .withSession(SessionKeys.sessionId -> "12345")
      .withFormUrlEncodedBody(body: _*)

    def executeTargetWithMockData(data: String): Future[Result] = {
      lazy val fakeRequest = buildRequest(("customerType", data))
      val mockData = new CustomerTypeModel(data)
      val target = setupTarget(None, Some(mockData))
      target.submitCustomerType(fakeRequest)
    }

    "submitting a valid form with 'individual'" should {

      lazy val result = executeTargetWithMockData(CustomerTypeKeys.individual)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the current income page" in {
        redirectLocation(result) shouldEqual Some("/calculate-your-capital-gains/non-resident/current-income")
      }
    }

    "submitting a valid form with 'trustee'" should {

      lazy val result = executeTargetWithMockData(CustomerTypeKeys.trustee)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the current income page" in {
        redirectLocation(result) shouldEqual Some("/calculate-your-capital-gains/non-resident/disabled-trustee")
      }
    }

    "submitting a valid form with 'personalRep'" should {

      lazy val result = executeTargetWithMockData(CustomerTypeKeys.personalRep)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the current income page" in {
        redirectLocation(result) shouldEqual Some("/calculate-your-capital-gains/non-resident/other-properties")
      }
    }

    "submitting an invalid form with no content" should {

      lazy val result = executeTargetWithMockData("")
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "raise an error on the page" in {
        document.body.select("#customerType-error-summary").size shouldBe 1
      }

      "render the customer type page" in {
        document.title shouldEqual messages.question
      }
    }

    "submitting an invalid form with incorrect content" should {

      lazy val result = executeTargetWithMockData("invalid-user")
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "raise an error on the page" in {
        document.body.select("#customerType-error-summary").size shouldBe 1
      }

      "render the customer type page" in {
        document.title shouldEqual messages.question
      }
    }
  }
}
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

package controllers.resident.properties.GainControllerSpec

import controllers.resident.properties.GainController
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{DisposalDate => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import config.AppConfig
import connectors.CalculatorConnector
import models.resident.{DisposalDateModel, TaxYearModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class DisposalDateActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[DisposalDateModel]): GainController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](Matchers.eq(keystoreKeys.disposalDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[DisposalDateModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new GainController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      val config: AppConfig = mock[AppConfig]
    }
  }

  case class FakePOSTRequest (dateResponse: TaxYearModel, inputOne: (String, String), inputTwo: (String, String), inputThree: (String, String)) {

    def setupTarget(): GainController = {

      val mockCalcConnector = mock[CalculatorConnector]

      when(mockCalcConnector.getTaxYear(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(Some(dateResponse)))

      when(mockCalcConnector.saveFormData[DisposalDateModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(mock[CacheMap]))

      new GainController {
        override val calcConnector: CalculatorConnector = mockCalcConnector
        val config: AppConfig = mock[AppConfig]
      }
    }

    val target = setupTarget()
    val result = target.submitDisposalDate(fakeRequestToPOSTWithSession(inputOne, inputTwo, inputThree))
    val doc = Jsoup.parse(bodyOf(result))
  }

  "Calling .disposalDate from the GainCalculationController" should {

    "when there is no keystore data" should {

      lazy val target = setupTarget(None)
      lazy val result = target.disposalDate(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a page with the title ${messages.title}" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }

    "when there is keystore data" should {

      lazy val target = setupTarget(Some(DisposalDateModel(10, 10, 2016)))
      lazy val result = target.disposalDate(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a page with the title ${messages.title}" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }
  }

  "Calling .disposalDate from the GainCalculationController with no session" should {
    lazy val target = setupTarget(None)
    lazy val result = target.disposalDate(fakeRequest)

    "return a status of 200" in {
      status(result) shouldBe 200
    }
  }

  "Calling .submitDisposalDate from the GainCalculationController" should {

    "when there is a valid form" should {

      lazy val dateResponse = TaxYearModel("2016/17", true, "2016/17")
      lazy val request = FakePOSTRequest(dateResponse, ("disposalDateDay", "28"), ("disposalDateMonth", "4"), ("disposalDateYear", "2016"))

      "return a status of 303" in {
        status(request.result) shouldBe 303
      }

      "redirect to the Sell Or Give Away page" in {
        redirectLocation(request.result) shouldBe Some("/calculate-your-capital-gains/resident/properties/sell-or-give-away")
      }
    }

    "when there is an invalid form" should {

      lazy val dateResponse = TaxYearModel("2016/17", true, "2016/17")
      lazy val request = FakePOSTRequest(dateResponse, ("disposalDateDay", "32"), ("disposalDateMonth", "4"), ("disposalDateYear", "2016"))

      "return a status of 400 with an invalid POST" in {
        status(request.result) shouldBe 400
      }

      "return a page with the title ''When did you sign the contract that made someone else the owner?'" in {
        Jsoup.parse(bodyOf(request.result)).title shouldBe messages.title
      }
    }

    "when there is a date that is greater than any specified tax year" should {

      lazy val dateResponse = TaxYearModel("2019/20", false, "2016/17")
      lazy val request = FakePOSTRequest(dateResponse, ("disposalDateDay", "30"), ("disposalDateMonth", "4"), ("disposalDateYear", "2019"))

      "return a status of 303" in {
        status(request.result) shouldBe 303
      }

      "redirect to the outside know years page" in {
        redirectLocation(request.result) shouldBe Some("/calculate-your-capital-gains/resident/properties/outside-tax-years")
      }
    }
    "when there is a date that is less than any specified tax year" should {

      lazy val dateResponse = TaxYearModel("2013/14", false, "2015/16")
      lazy val request = FakePOSTRequest(dateResponse, ("disposalDateDay", "12"), ("disposalDateMonth", "4"), ("disposalDateYear", "2013"))

      "return a status of 303" in {
        status(request.result) shouldBe 303
      }

      "redirect to the outside know years page" in {
        redirectLocation(request.result) shouldBe Some("/calculate-your-capital-gains/resident/properties/outside-tax-years")
      }
    }
  }
}

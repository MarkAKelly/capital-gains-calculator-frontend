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

package controllers.resident.shares.GainControllerSpec

import assets.MessageLookup.Resident.Shares.{worthOn => messages}
import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.shares.{GainController, routes}
import models.resident.shares.gain.WorthOnModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class WorthOnActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[WorthOnModel]): GainController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[WorthOnModel](Matchers.eq(keystoreKeys.worthOn))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[WorthOnModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new GainController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .worthOnMarchEightyTwo from the GainCalculationController" when {
    "there is no keystore data" should {
      lazy val target = setupTarget(None)
      lazy val result = target.worthOnMarchEightyTwo(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }
    }

    "there is keystore data" should {
      lazy val target = setupTarget(Some(WorthOnModel(100)))
      lazy val result = target.worthOnMarchEightyTwo(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }
    }
  }

  "Calling .worthOnMarchEightyTwo from the GainCalculationController" should {

    lazy val target = setupTarget(None)
    lazy val result = target.worthOnMarchEightyTwo(fakeRequestWithSession)

    "return a status of 200" in {
      status(result) shouldBe 200
    }

    s"return some html with title of ${messages.question}" in {
      contentType(result) shouldBe Some("text/html")
      Jsoup.parse(bodyOf(result)).select("h1").text shouldEqual messages.question
    }
  }

  "Calling .worthOnMarchEightyTwo from the GainCalculationController with no session" should {

    lazy val target = setupTarget(None)
    lazy val result = target.worthOnMarchEightyTwo(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }
  }

  "Calling .submitWorthOnMarchEightyTwo with a valid request" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("amount", "100"))
    lazy val result = target.submitWorthOnMarchEightyTwo(request)

    "return a status of 303" in {
      status(result) shouldEqual 303
    }

    "re-direct to the acquisition Costs page when supplied with a valid form" in {
      redirectLocation(result) shouldBe Some(routes.GainController.acquisitionCosts().url)
    }
  }

  "Calling .submitWorthOnMarchEightyTwo with an invalid request" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
    lazy val result = target.submitWorthOnMarchEightyTwo(request)

    "render with a status of 400" in {
      status(result) shouldEqual 400
    }

    "render the worth on view" in {
      Jsoup.parse(bodyOf(result)).title() shouldEqual messages.question
    }
  }
}

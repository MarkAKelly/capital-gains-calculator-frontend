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

import assets.MessageLookup
import controllers.helpers.FakeRequestHelper
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import connectors.CalculatorConnector
import org.mockito.Matchers
import config.AppConfig
import controllers.resident.shares.GainController
import models.resident.WorthWhenSoldForLessModel
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.jsoup.Jsoup
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future


class WorthWhenSoldForLessActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {


  def setupTarget(getData: Option[WorthWhenSoldForLessModel]): GainController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[WorthWhenSoldForLessModel](Matchers.eq(keystoreKeys.worthWhenSoldForLess))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[WorthWhenSoldForLessModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new GainController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      val config: AppConfig = mock[AppConfig]
    }
  }

//  "Calling .worthWhenSoldForLess from the GainCalculationController" when {
//    "there is no keystore data" should {
//      lazy val target = setupTarget(None)
//      lazy val result = target.worthWhenSoldForLess(fakeRequestWithSession)
//
//      "return a status of 200" in {
//        status(result) shouldEqual 200
//      }
//
//      s"return some html with title of ${MessageLookup.Resident.Shares.WorthWhenSoldForLess.question}" in {
//        Jsoup.parse(bodyOf(result)).select("h1").text shouldEqual MessageLookup.Resident.Shares.WorthWhenSoldForLess.question
//      }
//    }
//
//    "there is keystore data" should {
//      lazy val target = setupTarget(Some(WorthWhenSoldForLessModel(100)))
//      lazy val result = target.worthWhenSoldForLess(fakeRequestWithSession)
//
//      "return a status of 200" in {
//        status(result) shouldEqual 200
//      }
//
//      s"return some html with title of ${MessageLookup.Resident.Shares.WorthWhenSoldForLess.question}" in {
//        Jsoup.parse(bodyOf(result)).select("h1").text shouldEqual MessageLookup.Resident.Shares.WorthWhenSoldForLess.question
//      }
//    }
//  }
//
//  "Calling .worthWhenSoldForLess from the GainCalculationController with no session" should {
//
//    lazy val target = setupTarget(None)
//    lazy val result = target.worthWhenSoldForLess(fakeRequest)
//
//    "return a status of 303" in {
//      status(result) shouldBe 303
//    }
//
//    "return you to the session timeout page" in {
//      redirectLocation(result).get should include ("/calculate-your-capital-gains/session-timeout")
//    }
//  }
//
//  "Calling .submitWorthWhenSoldForLess from the GainController" should {
//    lazy val target = setupTarget(None)
//    lazy val request = fakeRequestToPOSTWithSession(("amount", "100"))
//    lazy val result = target.submitWorthWhenSoldForLess(request)
//
//    "when supplied with a valid form" which {
//
//      "redirect" in {
//        status(result) shouldEqual 303
//      }
//
//      "to the disposal costs page" in {
//        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/disposal-costs")
//      }
//    }
//  }
//
//  "Calling .submitWorthWhenSoldForLess from the GainController" should {
//    lazy val target = setupTarget(None)
//    lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
//    lazy val result = target.submitWorthWhenSoldForLess(request)
//
//    "when supplied with an invalid form" which {
//
//      "error" in {
//        status(result) shouldEqual 400
//      }
//
//      "stay on the shares Worth When Sold page" in {
//        Jsoup.parse(bodyOf(result)).title() shouldEqual MessageLookup.Resident.Shares.WorthWhenSoldForLess.question
//      }
//    }
//  }
}

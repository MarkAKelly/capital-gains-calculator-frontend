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

import assets.MessageLookup.{ownerBeforeAprilNineteenEightyTwo => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keyStoreKeys}
import config.{AppConfig, ApplicationConfig}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.properties.GainController
import models.resident.properties.gain.OwnerBeforeAprilModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

/**
  * Created by david on 15/09/16.
  */
class OwnerBeforeAprilActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[OwnerBeforeAprilModel]): GainController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[OwnerBeforeAprilModel](Matchers.eq(
      keyStoreKeys.ownerBeforeAprilNineteenEightyTwo))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[OwnerBeforeAprilModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new GainController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val config: AppConfig = ApplicationConfig
    }
  }

  "Calling .ownerBeforeAprilNineteenEightyTwo from the resident GainController" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = target.ownerBeforeAprilNineteenEightyTwo(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(OwnerBeforeAprilModel(true)))
      lazy val result = target.ownerBeforeAprilNineteenEightyTwo(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

    }

    "request has an invalid session" should {

      lazy val target = setupTarget(None)
      lazy val result = target.ownerBeforeAprilNineteenEightyTwo(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/session-timeout")
      }
    }
  }

  "Calling .submitOwnerBeforeAprilNineteenEightyTwo from the resident GainCalculator" when {

    //Un-comment these when the pages and routes are actually added.

//    "a valid form with the answer 'Yes' is submitted" should {
//
//      lazy val target = setupTarget(None)
//      lazy val request = fakeRequestToPOSTWithSession(("ownerBeforeAprilNineteenEightyTwo", "Yes"))
//      lazy val result = target.submitOwnerBeforeAprilNineteenEightyTwo(request)
//
//      "return a status of 303" in {
//        status(result) shouldBe 303
//      }
//
//      "redirect to the worth when sold page" in {
//        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/worth-on-march-thirty-first")
//      }
//    }
//
//    "a valid form with the answer 'No' is submitted" should {
//
//      lazy val target = setupTarget(None)
//      lazy val request = fakeRequestToPOSTWithSession(("ownerBeforeAprilNineteenEightyTwo", "No"))
//      lazy val result = target.submitOwnerBeforeAprilNineteenEightyTwo(request)
//
//      "return a status of 303" in {
//        status(result) shouldBe 303
//      }
//
//      "redirect to the disposal value page" in {
//        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/how-became-owner")
//      }
//    }

    "an invalid form with the answer '' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("ownerBeforeAprilNineteenEightyTwo", ""))
      lazy val result = target.submitOwnerBeforeAprilNineteenEightyTwo(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "render the Sell For Less page" in {
        doc.title() shouldEqual messages.title
      }
    }
  }
}

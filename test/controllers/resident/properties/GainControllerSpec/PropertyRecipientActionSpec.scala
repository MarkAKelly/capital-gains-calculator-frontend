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

import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.properties.GainController
import models.resident.properties.gain.PropertyRecipientModel
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future
import org.mockito.Mockito._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import config.AppConfig
import uk.gov.hmrc.http.cache.client.CacheMap
import assets.MessageLookup
import org.jsoup.Jsoup
import play.api.test.Helpers._


class PropertyRecipientActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar{

  def setupTarget(getData: Option[PropertyRecipientModel]) : GainController = {
    val mockCalcConnector = mock[CalculatorConnector]
    when(mockCalcConnector.fetchAndGetFormData[PropertyRecipientModel](Matchers.eq(keystoreKeys.propertyRecipient))(Matchers.any(), Matchers.any())).
      thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[PropertyRecipientModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(mock[CacheMap]))

    new GainController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      val config: AppConfig = mock[AppConfig]
    }
  }

  "Calling .whoDidYouGiveItTo from the GainsController" when {
    "there is no keystore data" should {
      lazy val target = setupTarget(None)
      lazy val result = target.whoDidYouGiveItTo(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }

      s"return some html with title of ${MessageLookup.whoDidYouGiveItTo.title}" in {
        Jsoup.parse(bodyOf(result)).select("h1").text shouldEqual MessageLookup.whoDidYouGiveItTo.title
      }
    }

    "there is keystore data" should {
      lazy val target = setupTarget(Some(PropertyRecipientModel("Charity")))
      lazy val result = target.whoDidYouGiveItTo(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }

      s"return some html with title of ${MessageLookup.whoDidYouGiveItTo.title}" in {
        Jsoup.parse(bodyOf(result)).select("h1").text shouldEqual MessageLookup.whoDidYouGiveItTo.title
      }
    }
  }

  "Calling .whoDidYouGiveItTo from the GainsController with no session" should{
    lazy val target = setupTarget(None)
    lazy val result = target.whoDidYouGiveItTo(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "to the session timeout page" in {
      redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/session-timeout?restartUrl=%2Fcalculate-your-capital-gains" +
        "%2Fresident%2Fproperties%2F&homeLink=%2Fcalculate-your-capital-gains%2Fresident%2Fproperties%2F")
    }

  }

  "Calling .submitWhoDidYouGiveItTo from the GainController with a Charity value" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("propertyRecipient","Charity"))
    lazy val result = target.submitWhoDidYouGiveItTo(request)

    "when supplied with a valid form" which {
      "redirects" in {
        status(result) shouldEqual 303
      }

      "to the You Have No Tax To Pay page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/you-have-no-tax-to-pay")
      }
    }
  }

  "Calling .submitWhoDidYouGiveItTo from the GainController with a Spouse value" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("propertyRecipient", "Spouse"))
    lazy val result = target.submitWhoDidYouGiveItTo(request)

    "when supplied with a valid form" which {
      "redirects" in {
        status(result) shouldEqual 303
      }

      "to the You Have No Tax To Pay page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/you-have-no-tax-to-pay")
      }
    }
  }

  "Calling .submitWhoDidYouGiveItTo from the GainController with a Someone Else value" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("propertyRecipient", "Other"))
    lazy val result = target.submitWhoDidYouGiveItTo(request)

    "when supplied with a valid form" which{
      "redirect" in {
        status(result) shouldEqual 303
      }

      "to the page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/worth-when-gave-away")
      }
    }
  }

  "Calling .submitWhoDidYouGiveIt to from the GainController with an invalid value/bad request" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("propertyRecipient", "blah"))
    lazy val result = target.submitWhoDidYouGiveItTo(request)

    "when supplied with an invalid form" which {
      "will generate a 400 error" in {
        status(result) shouldEqual 400
      }
      s"and lead to the current page reloading and return some HTML with title of ${MessageLookup.whoDidYouGiveItTo.title} " in {
        Jsoup.parse(bodyOf(result)).select("h1").text shouldEqual MessageLookup.whoDidYouGiveItTo.title

      }
    }
  }

}

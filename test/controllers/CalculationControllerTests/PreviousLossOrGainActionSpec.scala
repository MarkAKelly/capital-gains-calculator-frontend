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

import connectors.CalculatorConnector
import controllers.nonresident.PreviousLossOrGainController
import models.nonresident.PreviousLossOrGainModel
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import config.AppConfig
import controllers.helpers.FakeRequestHelper
import org.mockito.Mockito
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Mockito._
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import common.KeystoreKeys.{NonResidentKeys => keystoreKeys}
import assets.MessageLookup.NonResident.{PreviousLossOrGain => messages}
import org.jsoup.Jsoup

import scala.concurrent.Future

class PreviousLossOrGainActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {
  def setupTarget(getData: Option[PreviousLossOrGainModel]): PreviousLossOrGainController = {
    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[PreviousLossOrGainModel](Matchers.eq(keystoreKeys.previousLossOrGain))(Matchers.any(), Matchers.any())).
      thenReturn(Future.successful(getData))

    new PreviousLossOrGainController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      val config: AppConfig = mock[AppConfig]
    }
  }

  "Calling .previousLossOrGain from the PreviousLossOrGainController" when {
    "there is no keystore data" should {
      lazy val target = setupTarget(None)
      lazy val result = target.previousLossOrGain(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }

      s"return some HTML with title of ${messages.question}" in {
        Jsoup.parse(bodyOf(result)).select("h1").text shouldEqual messages.question
      }
    }

    "there is keystore data" should {
      lazy val target = setupTarget(Some(PreviousLossOrGainModel("Loss")))
      lazy val result = target.previousLossOrGain(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }

      s"return some html with title of ${messages.question}" in {
        Jsoup.parse(bodyOf(result)).select("h1").text shouldEqual messages.question
      }
    }

    "there is no valid session" should {
      lazy val target = setupTarget(None)
      lazy val result = target.previousLossOrGain(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/session-timeout")
      }
    }
  }
}

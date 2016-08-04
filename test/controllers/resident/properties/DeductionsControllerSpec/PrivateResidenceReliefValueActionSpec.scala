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

package controllers.resident.properties.DeductionsControllerSpec

import assets.MessageLookup.{privateResidenceReliefValue => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import common.Dates._
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.properties.DeductionsController
import models.resident.properties.{YourAnswersSummaryModel, PrivateResidenceReliefValueModel}
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class PrivateResidenceReliefValueActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  val summaryModel = YourAnswersSummaryModel(constructDate(10, 10, 2016), 100000, 10000, 50000, 1000, 2500)

  def setupTarget(getData: Option[PrivateResidenceReliefValueModel], summary: YourAnswersSummaryModel, totalGain: BigDecimal): DeductionsController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefValueModel](Matchers.eq(keystoreKeys.prrValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[PrivateResidenceReliefValueModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    when(mockCalcConnector.getPropertyGainAnswers(Matchers.any()))
      .thenReturn(summary)

    when(mockCalcConnector.calculateRttPropertyGrossGain(Matchers.any())(Matchers.any()))
      .thenReturn(totalGain)

    new DeductionsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .privateResidenceReliefValue from the resident DeductionsController" when {

    "there is no keystore data" should {

      lazy val target = setupTarget(None, summaryModel, BigDecimal(10000))
      lazy val result = target.privateResidenceReliefValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Private Residence Relief Value view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.question
      }
    }

    "there is some keystore data" should {

      lazy val target = setupTarget(Some(PrivateResidenceReliefValueModel(1000)), summaryModel, BigDecimal(10000))
      lazy val result = target.privateResidenceReliefValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Private Residence Relief Value view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.question
      }
    }
  }

  "request has an invalid session" should {

    lazy val target = setupTarget(None, summaryModel, BigDecimal(10000))
    lazy val result = target.privateResidenceReliefValue(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout page" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/session-timeout")
    }
  }
}

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

package controllers.resident.properties.DeductionsControllerTests

import assets.MessageLookup.{reliefs => messages}
import common.Dates._
import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.properties.DeductionsController
import models.resident.{ReliefsModel, YourAnswersSummaryModel}
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class ReliefsActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  val summaryModel = YourAnswersSummaryModel(constructDate(10, 10, 2016), 100000, 10000, 50000, 1000, 2500)

  def setupTarget(getData: Option[ReliefsModel], summary: YourAnswersSummaryModel, totalGain: BigDecimal): DeductionsController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[ReliefsModel](Matchers.eq(KeystoreKeys.ResidentKeys.reliefs))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[ReliefsModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    when(mockCalcConnector.getPropertyGainAnswers(Matchers.any()))
      .thenReturn(summary)

    when(mockCalcConnector.calculateRttPropertyGrossGain(Matchers.any())(Matchers.any()))
      .thenReturn(totalGain)

    new DeductionsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .reliefs from the resident DeductionsController" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None, summaryModel, BigDecimal(10000))
      lazy val result = target.reliefs(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(ReliefsModel(true)), summaryModel, BigDecimal(10000))
      lazy val result = target.reliefs(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }

    "request has an invalid session" should {

      lazy val target = setupTarget(None, summaryModel, BigDecimal(10000))
      lazy val result = target.reliefs(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/session-timeout")
      }
    }
  }

  "Calling .submitReliefs from the DeductionsController" when {

    "a valid form 'Yes' is submitted" should {
      lazy val target = setupTarget(None, summaryModel, BigDecimal(10000))
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", "Yes"))
      lazy val result = target.submitReliefs(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the reliefs entry page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/reliefs-value")
      }
    }

    "a valid form 'No' is submitted" should {
      lazy val target = setupTarget(None, summaryModel, BigDecimal(10000))
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", "No"))
      lazy val result = target.submitReliefs(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the other properties page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/other-properties")
      }
    }

    "an invalid form is submitted" should {
      lazy val target = setupTarget(None, summaryModel, BigDecimal(10000))
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", ""))
      lazy val result = target.submitReliefs(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the reliefs page" in {
        doc.title() shouldEqual messages.title
      }
    }
  }
}

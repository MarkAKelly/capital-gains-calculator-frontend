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

import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import common.resident.{PrivateResidenceReliefKeys => prrKeys}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.properties.DeductionsController
import models.resident.properties.PrivateResidenceReliefModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Mockito._
import play.api.test.Helpers._
import assets.MessageLookup.{privateResidenceRelief => messages}
import uk.gov.hmrc.http.cache.client.CacheMap
import config.AppConfig

import scala.concurrent.Future

class PrivateResidenceReliefSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[PrivateResidenceReliefModel]): DeductionsController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](Matchers.eq(keystoreKeys.privateResidenceRelief))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[PrivateResidenceReliefModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))


    new DeductionsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      val config = mock[AppConfig]
    }
  }

  "Calling .privateResidenceRelief from the resident DeductionsController" when {
    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = target.privateResidenceRelief(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(PrivateResidenceReliefModel(prrKeys.full)))
      lazy val result = target.privateResidenceRelief(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }

    "request has an invalid session" should {

      lazy val target = setupTarget(None)
      lazy val result = target.privateResidenceRelief(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/session-timeout")
      }
    }
  }

  "Calling .submitPrivateResidenceRelief from the resident DeductionsController" when {

    "a valid form with 'Full' prr is submitted" should {
      lazy val target = setupTarget(Some(PrivateResidenceReliefModel("Full")))
      lazy val request = fakeRequestToPOSTWithSession(("prrClaiming", "Full"))
      lazy val result = target.submitPrivateResidenceRelief(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the other-properties page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/other-properties")
      }
    }

    "a valid form with 'Part' prr is submitted" should {
      lazy val target = setupTarget(Some(PrivateResidenceReliefModel("Part")))
      lazy val request = fakeRequestToPOSTWithSession(("prrClaiming", "Part"))
      lazy val result = target.submitPrivateResidenceRelief(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the private-residence-relief-value page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/private-residence-relief-value")
      }
    }

    "a valid form with 'None' prr is submitted" should {
      lazy val target = setupTarget(Some(PrivateResidenceReliefModel("None")))
      lazy val request = fakeRequestToPOSTWithSession(("prrClaiming", "None"))
      lazy val result = target.submitPrivateResidenceRelief(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the other-properties page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/reliefs")
      }
    }

    "a invalid form with '' prr is submitted" should {
      lazy val target = setupTarget(Some(PrivateResidenceReliefModel("")))
      lazy val request = fakeRequestToPOSTWithSession(("prrClaiming", ""))
      lazy val result = target.submitPrivateResidenceRelief(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the Private Residence Relief page" in {
        doc.title() shouldEqual messages.title
      }
    }
  }
}

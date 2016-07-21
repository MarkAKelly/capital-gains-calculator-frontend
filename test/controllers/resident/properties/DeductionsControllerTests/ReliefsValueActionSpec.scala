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

import assets.MessageLookup.{reliefsValue => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.properties.DeductionsController
import models.resident.properties.ReliefsValueModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class ReliefsValueActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[ReliefsValueModel]): DeductionsController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[ReliefsValueModel](Matchers.eq(keystoreKeys.reliefsValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[ReliefsValueModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new DeductionsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .reliefsValue from the resident DeductionsController" when {

    "there is no keystore data" should {

      lazy val target = setupTarget(None)
      lazy val result = target.reliefsValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the reliefs value view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }

    "there is some keystore data" should {

      lazy val target = setupTarget(Some(ReliefsValueModel(1000)))
      lazy val result = target.reliefsValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Reliefs Value view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }
  }

  "request has an invalid session" should {

    lazy val target = setupTarget(None)
    lazy val result = target.reliefsValue(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout page" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/session-timeout")
    }
  }

  "Calling .submitReliefsValue from the GainCalculationController" when {

    "a valid form is submitted" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
      lazy val result = target.submitReliefsValue(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the improvements page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/other-properties")
      }
    }

    "an invalid form is submitted" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
      lazy val result = target.submitReliefsValue(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the Reliefs Value page" in {
        doc.title() shouldEqual messages.title
      }
    }
  }
}

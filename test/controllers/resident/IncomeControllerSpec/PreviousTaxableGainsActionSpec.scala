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

package controllers.resident.IncomeControllerSpec

import controllers.helpers.FakeRequestHelper
import controllers.resident.IncomeController
import org.jsoup.Jsoup
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{previousTaxableGains => messages}
import common.KeystoreKeys.{ResidentKeys => keystore}
import connectors.CalculatorConnector
import models.resident.income.PreviousTaxableGainsModel
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future

class PreviousTaxableGainsActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[PreviousTaxableGainsModel]): IncomeController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[PreviousTaxableGainsModel](Matchers.eq(keystore.previousTaxableGains))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    new IncomeController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .previousTaxableGains from the IncomeController" when {

    "there is no keystore data" should {

      lazy val target = setupTarget(None)
      lazy val result = target.previousTaxableGains(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the previous taxable gains view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }

    "there is some keystore data" should {

      lazy val target = setupTarget(Some(PreviousTaxableGainsModel(1000)))
      lazy val result = target.previousTaxableGains(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Improvements view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }
  }

  "request has an invalid session" should {

    lazy val target = setupTarget(None)
    lazy val result = target.previousTaxableGains(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout page" in {
      redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/session-timeout")
    }
  }

  "Calling .submitPreviousTaxableGains from the IncomeController" when {

    "an invalid form is submitted" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
      lazy val result = target.submitPreviousTaxableGains(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the previous taxable gains page" in {
        doc.title() shouldEqual messages.title
      }
    }
  }
}

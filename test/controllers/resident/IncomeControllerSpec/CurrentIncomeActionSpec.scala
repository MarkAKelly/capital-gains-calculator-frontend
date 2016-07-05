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

import assets.MessageLookup.{currentIncome => messages}
import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.IncomeController
import models.resident._
import models.resident.income._
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class CurrentIncomeActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  case class FakeGETRequest(storedData: Option[CurrentIncomeModel], otherProperties: Boolean = true,
                            lossesBroughtForward: Boolean = true, annualExemptAmount: BigDecimal = 0) {
    def setupTarget(): IncomeController = {

      val mockCalcConnector = mock[CalculatorConnector]

      when(mockCalcConnector.fetchAndGetFormData[CurrentIncomeModel](Matchers.eq(KeystoreKeys.ResidentKeys.currentIncome))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(storedData))

      when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](Matchers.eq(KeystoreKeys.ResidentKeys.otherProperties))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(OtherPropertiesModel(otherProperties))))

      when(mockCalcConnector.fetchAndGetFormData[LossesBroughtForwardModel](Matchers.eq(KeystoreKeys.ResidentKeys.lossesBroughtForward))(Matchers.any(),
        Matchers.any()))
        .thenReturn(Future.successful(Some(LossesBroughtForwardModel(lossesBroughtForward))))

      when(mockCalcConnector.fetchAndGetFormData[AnnualExemptAmountModel](Matchers.eq(KeystoreKeys.ResidentKeys.annualExemptAmount))(Matchers.any(),
        Matchers.any()))
        .thenReturn(Future.successful(Some(AnnualExemptAmountModel(annualExemptAmount))))

      new IncomeController {
        override val calcConnector: CalculatorConnector = mockCalcConnector
      }
    }

    val target = setupTarget()
    val result = target.currentIncome(fakeRequestWithSession)
    val doc = Jsoup.parse(bodyOf(result))
  }

  case class FakePOSTRequest(input: (String, String)*) {
    lazy val target = fakeRequestToPOSTWithSession(input: _*)
    lazy val result = IncomeController.submitCurrentIncome(target)
    lazy val doc = Jsoup.parse(bodyOf(result))
  }

  "Calling .currentIncome from the IncomeController with a session" when {

    "supplied with no pre-existing stored data" should {

      lazy val request = FakeGETRequest(None)

      "return a status of 200" in {
        status(request.result) shouldBe 200
      }

      "return some html" in {
        contentType(request.result) shouldBe Some("text/html")
      }

      "display the Current Income view" in {
        Jsoup.parse(bodyOf(request.result)).title shouldBe messages.title
      }
    }
    "supplied with pre-existing stored data" should {

      lazy val request = FakeGETRequest(Some(CurrentIncomeModel(40000)))

      "return a status of 200" in {
        status(request.result) shouldBe 200
      }

      "have the amount 40000 pre-populated into the input field" in {
        request.doc.getElementById("amount").attr("value") shouldBe "40000"
      }
    }

    "other properties have been selected and 0 has been entered into the annual exempt amount" should {

      lazy val request = FakeGETRequest(None)

      "return a 200" in {
        status(request.result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/previous-taxable-gains" in {
        request.doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/previous-taxable-gains"
      }
    }

    "other properties have been selected and non-zero has been entered into the annual exempt amount" should {

      lazy val request = FakeGETRequest(None, annualExemptAmount = 10)

      "return a 200" in {
        status(request.result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/annual-exempt-amount" in {
        request.doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/annual-exempt-amount"
      }
    }

    "other properties has not been selected and neither has brought forward losses" should {

      lazy val request = FakeGETRequest(None, otherProperties = false, lossesBroughtForward = false)

      "return a 200" in {
        status(request.result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/losses-brought-forward" in {
        request.doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/losses-brought-forward"
      }
    }

    "other properties has not been selected and brought forward losses has been selected" should {

      lazy val request = FakeGETRequest(None, otherProperties = false)

      "return a 200" in {
        status(request.result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/losses-brought-forward-value" in {
        request.doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/losses-brought-forward-value"
      }
    }
  }

  "Calling .currentIncome from the IncomeController with no session" should {

    lazy val result = IncomeController.currentIncome(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout view" in {
      redirectLocation(result).get shouldBe "/calculate-your-capital-gains/non-resident/session-timeout"
    }
  }

  "calling .submitCurrentIncome from the IncomeController" when {

    "given a valid form should" should {

      lazy val request = FakePOSTRequest(("amount", "100"))

      "return a status of 303" in {
        status(request.result) shouldBe 303
      }

      s"redirect to '${controllers.resident.routes.IncomeController.personalAllowance().toString}'" in {
        redirectLocation(request.result).get shouldBe controllers.resident.routes.IncomeController.personalAllowance().toString
      }
    }

    "given an invalid form" should {

      lazy val request = FakePOSTRequest(("amount", "-100"))

      "return a status of 400" in {
        status(request.result) shouldBe 400
      }
    }
  }
}

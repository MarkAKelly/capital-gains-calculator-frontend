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

import assets.MessageLookup.NonResident.{HowMuchGain => messages}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.HowMuchGainController
import models.nonresident.HowMuchGainModel
import org.jsoup._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class HowMuchGainActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[HowMuchGainModel]): HowMuchGainController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[HowMuchGainModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    new HowMuchGainController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "HowMuchGainController" should {
    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      HowMuchGainController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }

  //GET Tests
  "In CalculationController calling the .howMuchGain action" when {

    "not supplied with a pre-existing stored model" should {
      val target = setupTarget(None)
      lazy val result = target.howMuchGain(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the How Much Gain page" in {
        document.title shouldBe messages.question
      }
    }

    "supplied with a pre-existing stored model" should {
      val target = setupTarget(Some(HowMuchGainModel(1000)))
      lazy val result = target.howMuchGain(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the How Much Gain page" in {
        document.title shouldBe messages.question
      }
    }

    "without a valid session" should {
      val target = setupTarget(None)
      lazy val result = target.howMuchGain(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/session-timeout")
      }
    }
  }

  "In CalculationController calling the .submitHowMuchGain action" when {

    "submitting a valid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("howMuchGain", "1000"))
      lazy val result = target.submitHowMuchGain(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Brought Forward Losses page" in {
        redirectLocation(result) shouldBe Some(controllers.nonresident.routes.BroughtForwardLossesController.broughtForwardLosses().url)
      }
    }

    "a valid form is submitted with a zero value" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("howMuchGain", "0"))
      lazy val result = target.submitHowMuchGain(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Annual Exempt Amount page" in {
        redirectLocation(result) shouldBe Some(controllers.nonresident.routes.AnnualExemptAmountController.annualExemptAmount().url)
      }
    }

    "submitting an invalid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("howMuchGain", "-100"))
      lazy val result = target.submitHowMuchGain(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "load the How Much Gain page" in {
        document.title shouldBe messages.question
      }
    }

    "an invalid session is submitted" should {
      val target = setupTarget(None)
      lazy val result = target.howMuchGain(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/session-timeout")
      }
    }
  }
}

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

import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.BroughtForwardLossesController
import models.nonresident.BroughtForwardLossesModel
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Matchers
import org.mockito.Mockito._
import assets.MessageLookup.{NonResident => messages}
import play.api.test.Helpers._

class BroughtForwardLossesActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  def setupTarget(getData: Option[BroughtForwardLossesModel]): BroughtForwardLossesController = {
    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[BroughtForwardLossesModel](Matchers.eq(KeystoreKeys.broughtForwardLosses))(Matchers.any(), Matchers.any()))
      .thenReturn(getData)

    new BroughtForwardLossesController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .broughtForwardLosses" when {

    "provided with no previous data" should {
      val target = setupTarget(None)
      lazy val result = target.broughtForwardLosses(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the Brought Forward Losses page" in {
        document.title() shouldBe messages.BroughtForwardLosses.question
      }
    }

    "provided with previous data" should {
      val target = setupTarget(Some(BroughtForwardLossesModel(false, None)))
      lazy val result = target.broughtForwardLosses(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the Brought Forward Losses page" in {
        document.title() shouldBe messages.BroughtForwardLosses.question
      }
    }

    "provided with no valid session" should {
      val target = setupTarget(None)
      lazy val result = target.broughtForwardLosses(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/session-timeout")
      }
    }
  }

  "Calling .submitBroughtForwardLosses" when {

    "provided with a valid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", "No"), ("broughtForwardLoss", ""))
      lazy val result = target.submitBroughtForwardLosses(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Check Your Answers page" in {
        redirectLocation(result) shouldBe Some(controllers.nonresident.routes.CheckYourAnswersController.checkYourAnswers().url)
      }
    }

    "provided with an invalid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", "Yes"), ("broughtForwardLoss", ""))
      lazy val result = target.submitBroughtForwardLosses(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "load the Brought Forward Losses page" in {
        document.title() shouldBe messages.BroughtForwardLosses.question
      }
    }

    "provided with an invalid session" should {
      val target = setupTarget(None)
      lazy val result = target.submitBroughtForwardLosses(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/session-timeout")
      }
    }
  }
}

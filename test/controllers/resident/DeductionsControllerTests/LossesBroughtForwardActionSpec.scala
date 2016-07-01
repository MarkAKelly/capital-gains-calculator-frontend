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

package controllers.resident.DeductionsControllerTests

import assets.MessageLookup.{lossesBroughtForward => messages}
import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.DeductionsController
import models.resident._
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import scala.concurrent.Future

class LossesBroughtForwardActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(lossesBroughtForwardData: Option[LossesBroughtForwardModel],
                   otherPropertiesData: Option[OtherPropertiesModel],
                  allowableLossesData: Option[AllowableLossesModel] ): DeductionsController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[LossesBroughtForwardModel](Matchers.eq(KeystoreKeys.ResidentKeys.lossesBroughtForward))
      (Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(lossesBroughtForwardData))

    when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](Matchers.eq(KeystoreKeys.ResidentKeys.otherProperties))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(otherPropertiesData))

    when(mockCalcConnector.fetchAndGetFormData[AllowableLossesModel](Matchers.eq(KeystoreKeys.ResidentKeys.allowableLosses))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(allowableLossesData))

    new DeductionsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .lossesBroughtForward from the resident DeductionsController" when {

    "request has a valid session and no keystore value" should {

    lazy val target = setupTarget(None, Some(OtherPropertiesModel(false)), None)
    lazy val result = target.lossesBroughtForward(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(bodyOf(result))

    "return a status of 200" in {
      status(result) shouldBe 200
    }

    s"return some html with " in {
      contentType(result) shouldBe Some("text/html")
    }

    s"return a title of ${messages.title}" in {
      doc.title shouldEqual messages.title
    }
  }

    "request has no session" should {

      lazy val target = setupTarget(None, None, None)
      lazy val result = target.lossesBroughtForward(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }
    }

    "no other properties have been selected" should {

      lazy val target = setupTarget(None, Some(OtherPropertiesModel(false)), None)
      lazy val result = target.lossesBroughtForward(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/other-properties" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/other-properties"
      }
    }

    "other properties have been selected" should {

      lazy val target = setupTarget(None, Some(OtherPropertiesModel(true)), None)
      lazy val result = target.lossesBroughtForward(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/allowable-losses" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/allowable-losses"
      }
    }

    "other properties have been selected but no allowable losses" should {

      lazy val target = setupTarget(None, Some(OtherPropertiesModel(true)), Some(AllowableLossesModel(false)))
      lazy val result = target.lossesBroughtForward(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/allowable-losses" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/allowable-losses"
      }
    }

    "other properties have been selected and allowable losses are claimed" should {

      lazy val target = setupTarget(None, Some(OtherPropertiesModel(true)), Some(AllowableLossesModel(true)))
      lazy val result = target.lossesBroughtForward(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link with the address /calculate-your-capital-gains/resident/allowable-losses-value" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/allowable-losses-value"
      }
    }
  }

  "Calling .submitLossesBroughtForward from the DeductionsController" when {
    "a valid form 'No' and no other properties are claimed" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(false)), Some(OtherPropertiesModel(false)), None)
      lazy val request = fakeRequestToPOSTWithSession(("option", "No"))
      lazy val result = target.submitLossesBroughtForward(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/summary")
      }
    }

    "a valid form 'No' is and other properties are claimed" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(false)), Some(OtherPropertiesModel(true)), None)
      lazy val request = fakeRequestToPOSTWithSession(("option", "No"))
      lazy val result = target.submitLossesBroughtForward(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the annual exempt amount page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/annual-exempt-amount")
      }
    }

    "a valid form 'Yes' is and other properties are not claimed" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(true)), Some(OtherPropertiesModel(false)), None)
      lazy val request = fakeRequestToPOSTWithSession(("option", "Yes"))
      lazy val result = target.submitLossesBroughtForward(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the losses brought forward value page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/losses-brought-forward-value")
      }
    }

    "a valid form 'Yes' is and other properties are claimed" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(true)), Some(OtherPropertiesModel(true)), None)
      lazy val request = fakeRequestToPOSTWithSession(("option", "Yes"))
      lazy val result = target.submitLossesBroughtForward(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the losses brought forward value page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/losses-brought-forward-value")
      }
    }

    "an invalid form is submitted" should {

      lazy val target = setupTarget(Some(LossesBroughtForwardModel(true)), Some(OtherPropertiesModel(false)), None)
      lazy val request = fakeRequestToPOSTWithSession(("option", ""))
      lazy val result = target.submitLossesBroughtForward(request)

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the brought forward losses page" in {
        Jsoup.parse(bodyOf(result)).title() shouldEqual messages.title
      }
    }
  }
}

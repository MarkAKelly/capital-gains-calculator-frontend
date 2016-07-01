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

import assets.MessageLookup.{allowableLosses => messages}
import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.DeductionsController
import models.resident.AllowableLossesModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class AllowableLossesActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[AllowableLossesModel]): DeductionsController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[AllowableLossesModel](Matchers.eq(KeystoreKeys.ResidentKeys.allowableLosses))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    new DeductionsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .allowableLosses from the resident DeductionsController" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = target.allowableLosses(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title of ${messages.title}" in {
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(AllowableLossesModel(true)))
      lazy val result = target.allowableLosses(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title of ${messages.title}" in {
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }

    "request has an invalid session" should {

      lazy val target = setupTarget(None)
      lazy val result = target.allowableLosses(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .submitAllowableLosses from the DeductionsController" when {

    "a valid form 'Yes' is submitted" should {
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", "Yes"))
      lazy val result = DeductionsController.submitAllowableLosses(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the allowable losses value page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/allowable-losses-value")
      }
    }

    "a valid form 'No' is submitted" should {
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", "No"))
      lazy val result = DeductionsController.submitAllowableLosses(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the losses brought forward page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/losses-brought-forward")
      }
    }

    "an invalid form is submitted" should {
      lazy val request = fakeRequestToPOSTWithSession(("isClaiming", ""))
      lazy val result = DeductionsController.submitAllowableLosses(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the allowable losses" in {
        doc.title() shouldEqual messages.title
      }
    }
  }
}

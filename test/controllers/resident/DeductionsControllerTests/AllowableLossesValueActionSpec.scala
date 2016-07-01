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

import controllers.helpers.FakeRequestHelper
import controllers.resident.DeductionsController
import org.jsoup.Jsoup
import play.api.test.Helpers._
import assets.MessageLookup.{allowableLossesValue => messages}
import common.KeystoreKeys
import connectors.CalculatorConnector
import models.resident.AllowableLossesValueModel
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class AllowableLossesValueActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[AllowableLossesValueModel]): DeductionsController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[AllowableLossesValueModel]
      (Matchers.eq(KeystoreKeys.ResidentKeys.allowableLossesValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    new DeductionsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .allowableLossesValue from the DeductionsController" should {

    "there is no keystore data" should {

      lazy val target = setupTarget(None)
      lazy val result = target.allowableLossesValue(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"have a title of ${messages.title}" in {
        doc.title() shouldBe messages.title
      }
    }

    "there is some keystore data" should {

      lazy val target = setupTarget(Some(AllowableLossesValueModel(1000)))
      lazy val result = target.allowableLossesValue(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Reliefs Value view" in {
        doc.title shouldBe messages.title
      }

      "have 1000 pre-populated in the amount input field" in {
        doc.select("input#amount").attr("value") shouldBe "1000"
      }
    }

    "request has an invalid session" should {

      lazy val result = DeductionsController.allowableLossesValue(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .submitAllowableLossesValue from the DeductionsController" when {

    "a valid form is submitted" should {
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
      lazy val result = DeductionsController.submitAllowableLossesValue(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the brought forward losses page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/losses-brought-forward")
      }
    }

    "an invalid form is submitted" should {
      lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
      lazy val result = DeductionsController.submitAllowableLossesValue(request)
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

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

import assets.MessageLookup.{otherProperties => messages}
import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.DeductionsController
import models.resident.OtherPropertiesModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import scala.concurrent.Future

class OtherPropertiesActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[OtherPropertiesModel]): DeductionsController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](Matchers.eq(KeystoreKeys.ResidentKeys.otherProperties))(Matchers.any(), Matchers.any()))
    .thenReturn(Future.successful(getData))

    new DeductionsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .otherProperties from the DeductionsController" when {
    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = DeductionsController.otherProperties(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"have a title of ${messages.title}" in {
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }

    "request has no session" should {

      lazy val target = setupTarget(None)
      lazy val result = target.otherProperties(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }
    }
  }

  "Calling .submitOtherProperties from the DeductionsController" when {
    "a valid form 'Yes' is submitted" should {

      lazy val request = fakeRequestToPOSTWithSession(("hasOtherProperties", "Yes"))
      lazy val result = DeductionsController.submitOtherProperties(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the allowable losses page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/allowable-losses")
      }
    }

    "a valid form 'No' is submitted" should {

      lazy val request = fakeRequestToPOSTWithSession(("hasOtherProperties", "No"))
      lazy val result = DeductionsController.submitOtherProperties(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the allowable losses page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/losses-brought-forward")
      }
    }

    "an invalid form is submitted" should {

      lazy val request = fakeRequestToPOSTWithSession(("hasOtherProperties", ""))
      lazy val result = DeductionsController.submitOtherProperties(request)

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the other properties page" in {
        Jsoup.parse(bodyOf(result)).title() shouldEqual messages.title
      }
    }
  }
}

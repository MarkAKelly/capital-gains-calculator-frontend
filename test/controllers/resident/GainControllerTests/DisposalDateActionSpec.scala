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

package controllers.resident.GainControllerTests

import controllers.resident.GainController
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{disposalDate => messages}
import common.KeystoreKeys
import connectors.CalculatorConnector
import models.resident.DisposalDateModel
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future

class DisposalDateActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar{

  case class FakeGETRequest (storedData: Option[DisposalDateModel]) {
    def setupTarget(getData: Option[DisposalDateModel]): GainController = {

      val mockCalcConnector = mock[CalculatorConnector]

      when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](Matchers.eq(KeystoreKeys.ResidentKeys.disposalDate))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(getData))

      new GainController {
        override val calcConnector: CalculatorConnector = mockCalcConnector
      }
    }
    val target = setupTarget(storedData)
    val result = target.disposalDate(fakeRequestWithSession)
    val doc = Jsoup.parse(bodyOf(result))
  }

  case class FakePOSTRequest (input: (String, String)*) {
    lazy val target = fakeRequestToPOSTWithSession(input: _*)
    lazy val result = GainController.submitDisposalDate(target)
    lazy val doc = Jsoup.parse(bodyOf(result))
  }

  "Calling .disposalDate from the GainCalculationController" should {

    "when there is no keystore data" should {

      lazy val request = FakeGETRequest(None)

      "return a status of 200" in {
        status(request.result) shouldBe 200
      }

      "return some html" in {
        contentType(request.result) shouldBe Some("text/html")
      }

      s"return a page with the title ${messages.title}" in {
        request.doc.title shouldBe messages.title
      }
    }

    "when there is keystore data" should {

      lazy val request = FakeGETRequest(Some(DisposalDateModel(10, 10, 2016)))

      "return a status of 200" in {
        status(request.result) shouldBe 200
      }

      "return some html" in {
        contentType(request.result) shouldBe Some("text/html")
      }

      s"return a page with the title ${messages.title}" in {
        request.doc.title shouldBe messages.title
      }
    }
  }

  "Calling .disposalDate from the GainCalculationController with no session" should {

    lazy val result = GainController.disposalDate(fakeRequest)

    "return a status of 200" in {
      status(result) shouldBe 200
    }
  }

  "Calling .submitDisposalDate from the GainCalculationController" should {

    "when there is a valid form" should {

      lazy val request = FakePOSTRequest(("disposalDateDay", "30"), ("disposalDateMonth", "4"), ("disposalDateYear", "2016"))

      "return a status of 303" in {
        status(request.result) shouldBe 303
      }

      "redirect to the Disposal Value page" in {
        redirectLocation(request.result) shouldBe Some("/calculate-your-capital-gains/resident/disposal-value")
      }
    }

    "when there is an invalid form" should {

      lazy val request = FakePOSTRequest(("disposalDateDay", "32"), ("disposalDateMonth", "4"), ("disposalDateYear", "2016"))

      "return a status of 400 with an invalid POST" in {
        status(request.result) shouldBe 400
      }

      "return a page with the title ''When did you sign the contract that made someone else the owner?'" in {
        request.doc.title() shouldBe messages.title
      }
    }
  }
}

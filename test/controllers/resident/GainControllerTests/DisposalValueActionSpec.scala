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

import assets.MessageLookup
import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.resident.GainController
import controllers.helpers.FakeRequestHelper
import models.resident.DisposalValueModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

import scala.concurrent.Future

class DisposalValueActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData : Option[DisposalValueModel]): GainController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[DisposalValueModel](Matchers.eq(KeystoreKeys.ResidentKeys.disposalValue))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(getData))

    new GainController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .disposalValue from the GainCalculationController" when {
    "there is no keystore data" should {
      lazy val target = setupTarget(None)
      lazy val result = target.disposalValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }
    }

    "there is keystore data" should {
      lazy val target = setupTarget(Some(DisposalValueModel(100)))
      lazy val result = target.disposalValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }
    }
  }

  "Calling .disposalValue from the GainCalculationController" should {

    lazy val result = GainController.disposalValue(fakeRequestWithSession)

    "return a status of 200" in {
      status(result) shouldBe 200
    }

    s"return some html with title of ${MessageLookup.disposalValueQuestion}" in {
      contentType(result) shouldBe Some("text/html")
      Jsoup.parse(bodyOf(result)).select("h1").text shouldEqual MessageLookup.disposalValueQuestion
    }
  }

  "Calling .disposalValue from the GainCalculationController with no session" should {

    lazy val result = GainController.disposalValue(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }
  }


  "Calling .submitDisposalValue from the GainController" should {

    lazy val request = fakeRequestToPOSTWithSession(("amount", "100"))
    lazy val result = GainController.submitDisposalValue(request)

    "re-direct to the disposal Costs page when supplied with a valid form" in {
      status(result) shouldEqual 303
      redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/disposal-costs")
    }
  }

  "Calling .submitDisposalValue from the GainController" should {
    lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
    lazy val result = GainController.submitDisposalValue(request)

    "render the disposal value page when supplied with an invalid form" in {
      status(result) shouldEqual 400
      Jsoup.parse(bodyOf(result)).title() shouldEqual MessageLookup.disposalValueTitle
    }
  }
}

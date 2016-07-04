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
import models.resident.{LossesBroughtForwardModel, OtherPropertiesModel}
import models.resident.income.PreviousTaxableGainsModel
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future

class PreviousTaxableGainsActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[PreviousTaxableGainsModel], otherProperties: Boolean = true, lossesBroughtForward: Boolean = true): IncomeController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[LossesBroughtForwardModel](Matchers.eq(keystore.lossesBroughtForward))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(LossesBroughtForwardModel(lossesBroughtForward))))

    when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](Matchers.eq(keystore.otherProperties))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(OtherPropertiesModel(otherProperties))))

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

    "other properties have been specified" should {
      "return a back link to the AEA page" in {
        val target = setupTarget(Some(PreviousTaxableGainsModel(1000)), otherProperties = true)
        val result = target.previousTaxableGains(fakeRequestWithSession)
        val doc = Jsoup.parse(bodyOf(result))

        val link = doc.select("#back-link")
        link.attr("href") shouldBe controllers.resident.routes.DeductionsController.annualExemptAmount().toString
      }
    }

    "no other properties AND brought forward losses specified" should {
      "return a back link to the brought forward input page" in {
        val target = setupTarget(Some(PreviousTaxableGainsModel(1000)), otherProperties = false, lossesBroughtForward = true)
        val result = target.previousTaxableGains(fakeRequestWithSession)
        val doc = Jsoup.parse(bodyOf(result))

        val link = doc.select("#back-link")
        link.attr("href") shouldBe controllers.resident.routes.DeductionsController.lossesBroughtForwardValue().toString
      }
    }

    "no other properties AND brought forward losses NOT specified" should {
      "return a back link to the brought forward choice page" in {
        val target = setupTarget(Some(PreviousTaxableGainsModel(1000)), otherProperties = false, lossesBroughtForward = false)
        val result = target.previousTaxableGains(fakeRequestWithSession)
        val doc = Jsoup.parse(bodyOf(result))

        val link = doc.select("#back-link")
        link.attr("href") shouldBe controllers.resident.routes.DeductionsController.lossesBroughtForward().toString
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

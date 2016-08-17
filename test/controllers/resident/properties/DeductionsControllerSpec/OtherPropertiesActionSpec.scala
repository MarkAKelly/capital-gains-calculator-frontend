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

package controllers.resident.properties.DeductionsControllerSpec

import assets.MessageLookup.{otherProperties => messages}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import common.resident.PrivateResidenceReliefKeys
import config.AppConfig
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.properties.DeductionsController
import models.resident.properties.{PrivateResidenceReliefModel, ReliefsModel}
import models.resident.{DisposalDateModel, OtherPropertiesModel, TaxYearModel}
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class OtherPropertiesActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[OtherPropertiesModel],
                  reliefsData: Option[ReliefsModel],
                  disposalDate: Option[DisposalDateModel],
                  taxYear: Option[TaxYearModel],
                  prrModel: Option[PrivateResidenceReliefModel],
                  prrEnabled: Boolean = true): DeductionsController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockAppConfig = mock[AppConfig]

    when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](Matchers.eq(keystoreKeys.otherProperties))(Matchers.any(), Matchers.any()))
    .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[ReliefsModel](Matchers.eq(keystoreKeys.reliefs))(Matchers.any(), Matchers.any()))
    .thenReturn(Future.successful(reliefsData))

    when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](Matchers.eq(keystoreKeys.disposalDate))(Matchers.any(), Matchers.any()))
      .thenReturn(disposalDate)

    when(mockCalcConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(taxYear)

    when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](Matchers.eq(keystoreKeys.privateResidenceRelief))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(prrModel))

    when(mockAppConfig.featureRTTPRREnabled)
      .thenReturn(prrEnabled)

    new DeductionsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      val config = mockAppConfig
    }
  }

  "Calling .otherProperties from the DeductionsController" when {
    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None,
        None,
        Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")),
        Some(PrivateResidenceReliefModel(PrivateResidenceReliefKeys.full)))
      lazy val result = target.otherProperties(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"have a title of ${messages.title("2015/16")}" in {
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title("2015/16")
      }

      "have a back link to prr page value page" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/private-residence-relief"
      }
    }

    "request has no session" should {

      lazy val target = setupTarget(None,
        None,
        Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")),
        None)
      lazy val result = target.otherProperties(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }
    }

    "reliefs model is populated with 'Yes'" should {

      lazy val target = setupTarget(None,
        Some(ReliefsModel(true)),
        Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")),
        None)
      lazy val result = target.otherProperties(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link to reliefs value page" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/reliefs-value"
      }
    }

    "private residence relief is disabled" should {

      lazy val target = setupTarget(None,
        Some(ReliefsModel(true)),
        Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")),
        Some(PrivateResidenceReliefModel(PrivateResidenceReliefKeys.full)),
        prrEnabled = false)
      lazy val result = target.otherProperties(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link to reliefs value page" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/reliefs-value"
      }
    }

    "reliefs model is populated with 'No'" should {

      lazy val target = setupTarget(None,
        Some(ReliefsModel(false)),
        Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")),
        None)
      lazy val result = target.otherProperties(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link to reliefs page" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/reliefs"
      }
    }
  }

  "Calling .submitOtherProperties from the DeductionsController" when {
    "a valid form 'Yes' is submitted" should {

      lazy val target = setupTarget(None,
        Some(ReliefsModel(true)),
        Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")),
        None)
      lazy val request = fakeRequestToPOSTWithSession(("hasOtherProperties", "Yes"))
      lazy val result = target.submitOtherProperties(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the allowable losses page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/allowable-losses")
      }
    }

    "a valid form 'No' is submitted" should {

      lazy val target = setupTarget(None, Some(ReliefsModel(true)),
        Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")),
        None)
      lazy val request = fakeRequestToPOSTWithSession(("hasOtherProperties", "No"))
      lazy val result = target.submitOtherProperties(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the allowable losses page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/properties/losses-brought-forward")
      }
    }

    "an invalid form is submitted" should {

      lazy val target = setupTarget(None,
        Some(ReliefsModel(true)),
        Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")),
        None)
      lazy val request = fakeRequestToPOSTWithSession(("hasOtherProperties", ""))
      lazy val result = target.submitOtherProperties(request)

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the other properties page" in {
        Jsoup.parse(bodyOf(result)).title() shouldEqual messages.title("2015/16")
      }
    }
  }
}

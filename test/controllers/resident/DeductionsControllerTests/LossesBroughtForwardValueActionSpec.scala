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

import assets.MessageLookup.{lossesBroughtForwardValue => messages}
import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.DeductionsController
import models.resident._
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.collection.immutable.Range
import scala.concurrent.Future

class LossesBroughtForwardValueActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar{

  "Calling .lossesBroughtForwardValue from the resident DeductionsController" when {

    def setGetTarget(getData: Option[LossesBroughtForwardValueModel]): DeductionsController = {

      val mockCalcConnector = mock[CalculatorConnector]

      when(mockCalcConnector.fetchAndGetFormData[LossesBroughtForwardValueModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
        .thenReturn(getData)

      new DeductionsController {
        override val calcConnector = mockCalcConnector
      }
    }

    "request has a valid session with no keystore data" should {
      lazy val target = setGetTarget(None)
      lazy val result = target.lossesBroughtForwardValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with " in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title of ${messages.title}" in {
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }

    "request has a valid session with some keystore data" should {
      lazy val target = setGetTarget(Some(LossesBroughtForwardValueModel(BigDecimal(1000))))
      lazy val result = target.lossesBroughtForwardValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with " in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title of ${messages.title}" in {
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }

    "request has an invalid session" should {
      lazy val result = DeductionsController.lossesBroughtForwardValue(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/session-timeout")
      }
    }
  }

  "Calling .submitLossesBroughtForwardValue from the resident DeductionsController" when {

    val gainModel = mock[YourAnswersSummaryModel]
    val summaryModel = mock[ChargeableGainAnswers]

    def setPostTarget(otherPropertiesModel: Option[OtherPropertiesModel],
                      gainAnswers: YourAnswersSummaryModel,
                      chargeableGainAnswers: ChargeableGainAnswers,
                      chargeableGain: ChargeableGainResultModel,
                      allowableLossesModel: Option[AllowableLossesModel] = None,
                      allowableLossesValueModel: Option[AllowableLossesValueModel] = None): DeductionsController = {

      val mockCalcConnector = mock[CalculatorConnector]

      when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](Matchers.eq(KeystoreKeys.ResidentKeys.otherProperties))(Matchers.any(), Matchers.any()))
        .thenReturn(otherPropertiesModel)

      when(mockCalcConnector.fetchAndGetFormData[AllowableLossesModel](Matchers.eq(KeystoreKeys.ResidentKeys.allowableLosses))(Matchers.any(), Matchers.any()))
        .thenReturn(allowableLossesModel)

      when(mockCalcConnector.fetchAndGetFormData[AllowableLossesValueModel](Matchers.eq(KeystoreKeys.ResidentKeys.allowableLossesValue))(Matchers.any(), Matchers.any()))
        .thenReturn(allowableLossesValueModel)

      when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](Matchers.eq(KeystoreKeys.ResidentKeys.otherProperties))(Matchers.any(), Matchers.any()))
        .thenReturn(otherPropertiesModel)

      when(mockCalcConnector.getYourAnswers(Matchers.any()))
        .thenReturn(Future.successful(gainAnswers))

      when(mockCalcConnector.getChargeableGainAnswers(Matchers.any()))
        .thenReturn(Future.successful(chargeableGainAnswers))

      when(mockCalcConnector.calculateRttChargeableGain(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(Some(chargeableGain)))

      new DeductionsController {
        override val calcConnector = mockCalcConnector
      }
    }

    "given a valid form" when {

      "the user has disposed of other properties with non-zero allowable losses" should {
        lazy val target = setPostTarget(Some(OtherPropertiesModel(true)), gainModel, summaryModel, ChargeableGainResultModel(0, 0, 0, 0), Some(AllowableLossesModel(true)), Some(AllowableLossesValueModel(BigDecimal(1000))))
        lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
        lazy val result = target.submitLossesBroughtForwardValue(request)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        s"redirect to '${controllers.resident.routes.DeductionsController.annualExemptAmount().toString}'" in {
          redirectLocation(result).get shouldBe controllers.resident.routes.DeductionsController.annualExemptAmount().toString
        }
      }

      "the user has disposed of other properties with zero allowable losses" should {
        lazy val target = setPostTarget(Some(OtherPropertiesModel(true)), gainModel, summaryModel, ChargeableGainResultModel(2000, 2000, 2000, 2000), Some(AllowableLossesModel(true)), Some(AllowableLossesValueModel(BigDecimal(0))))
        lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
        lazy val result = target.submitLossesBroughtForwardValue(request)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        s"redirect to '${controllers.resident.routes.DeductionsController.annualExemptAmount().toString}'" in {
          redirectLocation(result).get shouldBe controllers.resident.routes.IncomeController.currentIncome().toString
        }
      }

      "the user has disposed of other properties with no allowable losses" should {
        lazy val target = setPostTarget(Some(OtherPropertiesModel(true)), gainModel, summaryModel, ChargeableGainResultModel(0, 0, 0, 0), Some(AllowableLossesModel(false)), None)
        lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
        lazy val result = target.submitLossesBroughtForwardValue(request)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        s"redirect to '${controllers.resident.routes.DeductionsController.annualExemptAmount().toString}'" in {
          redirectLocation(result).get shouldBe controllers.resident.routes.DeductionsController.annualExemptAmount().toString
        }
      }

      "the user has not disposed of other properties and has zero chargeable gain" should {
        lazy val target = setPostTarget(Some(OtherPropertiesModel(false)), gainModel, summaryModel, ChargeableGainResultModel(2000, 0, 0, 2000))
        lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
        lazy val result = target.submitLossesBroughtForwardValue(request)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        s"redirect to '${controllers.resident.routes.SummaryController.summary().toString}'" in {
          redirectLocation(result).get shouldBe controllers.resident.routes.SummaryController.summary().toString
        }
      }

      "the user has not disposed of other properties and has negative chargeable gain" should {
        lazy val target = setPostTarget(Some(OtherPropertiesModel(false)), gainModel, summaryModel, ChargeableGainResultModel(2000, -1000, 0, 3000))
        lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
        lazy val result = target.submitLossesBroughtForwardValue(request)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        s"redirect to '${controllers.resident.routes.SummaryController.summary().toString}'" in {
          redirectLocation(result).get shouldBe controllers.resident.routes.SummaryController.summary().toString
        }
      }

      "the user has not disposed of other properties and has positive chargeable gain of £1,000" should {
        lazy val target = setPostTarget(Some(OtherPropertiesModel(false)), gainModel, summaryModel, ChargeableGainResultModel(1000, 1000, 0, 0))
        lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
        lazy val result = target.submitLossesBroughtForwardValue(request)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        s"redirect to '${controllers.resident.routes.SummaryController.summary().toString}'" in {
          redirectLocation(result).get shouldBe controllers.resident.routes.IncomeController.currentIncome().toString
        }
      }
    }

    "given an invalid form" should {
      lazy val target = setPostTarget(Some(OtherPropertiesModel(false)), gainModel, summaryModel, ChargeableGainResultModel(1000, 1000, 0, 0))
      lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
      lazy val result = target.submitLossesBroughtForwardValue(request)

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      s"return a title of ${messages.title}" in {
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }
  }
}

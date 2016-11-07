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

import assets.MessageLookup.NonResident.{OtherReliefs => messages}
import common.{KeystoreKeys, TestModels}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.OtherReliefsFlatController
import models.nonresident._
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class OtherReliefsFlatActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(
                   getData: Option[OtherReliefsModel],
                   summary: SummaryModel,
                   result: CalculationResultModel,
                   acquisitionDateData: Option[AcquisitionDateModel],
                   rebasedValueData: Option[RebasedValueModel]
                 ): OtherReliefsFlatController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[RebasedValueModel](Matchers.eq(KeystoreKeys.rebasedValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(rebasedValueData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(acquisitionDateData))

    when(mockCalcConnector.createSummary(Matchers.any()))
      .thenReturn(Future.successful(summary))

    when(mockCalcConnector.calculateFlat(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(result)))

    new OtherReliefsFlatController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling the .otherReliefsFlat action " when {

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(
        None,
        TestModels.summaryIndividualFlatWithoutAEA,
        TestModels.calcModelTwoRates,
        Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
        None
      )
      lazy val result = target.otherReliefsFlat(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200 with a valid calculation result" in {
        status(result) shouldBe 200
      }

      "load the otherReliefs flat page" in {
        document.title() shouldBe messages.inputQuestion
      }
    }

    "supplied with a pre-existing stored model" should {
      val testOtherReliefsModel = OtherReliefsModel(None, Some(5000))
      val target = setupTarget(
        Some(testOtherReliefsModel),
        TestModels.summaryIndividualFlatWithoutAEA,
        TestModels.calcModelLoss,
        Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
        None
      )
      lazy val result = target.otherReliefsFlat(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the otherReliefs flat page" in {
        document.title() shouldBe messages.inputQuestion
      }
    }

    "supplied with an invalid session" should {
      val target = setupTarget(
        None,
        TestModels.summaryIndividualFlatWithoutAEA,
        TestModels.calcModelTwoRates,
        Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
        None
      )
      lazy val result = target.otherReliefsFlat(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/session-timeout")
      }
    }
  }

  "Calling the .submitOtherReliefsFlat action" when {

    "submitting a valid form" should {
      val target = setupTarget(
        None,
        TestModels.summaryIndividualFlatWithoutAEA,
        TestModels.calcModelLoss,
        Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
        None
      )
      lazy val request = fakeRequestToPOSTWithSession(("isClaimingOtherReliefs", "Yes"), ("otherReliefs", "1000"))
      lazy val result = target.submitOtherReliefsFlat(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the calculation election page" in {
        redirectLocation(result) shouldBe Some(controllers.nonresident.routes.CalculationElectionController.calculationElection().url)
      }
    }

    "submitting an invalid form" should {
      val target = setupTarget(
        None,
        TestModels.summaryIndividualFlatWithoutAEA,
        TestModels.calcModelLoss,
        Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
        None
      )
      lazy val request = fakeRequestToPOSTWithSession(("isClaimingOtherReliefs", "Yes"), ("otherReliefs", "-1000"))
      lazy val result = target.submitOtherReliefsFlat(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the other reliefs flat page" in {
        document.title() shouldBe messages.inputQuestion
      }
    }
  }
}

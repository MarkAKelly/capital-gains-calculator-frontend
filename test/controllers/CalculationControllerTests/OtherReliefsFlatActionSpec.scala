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
import constructors.nonresident.AnswersConstructor
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.OtherReliefsFlatController
import models.nonresident._
import models.resident.TaxYearModel
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
                   gainAnswers: TotalGainAnswersModel,
                   calculationResultsModel: CalculationResultsWithTaxOwedModel,
                   personalDetailsModel: TotalPersonalDetailsCalculationModel,
                   totalGainResultModel: TotalGainResultsModel = TotalGainResultsModel(200, Some(100), None),
                   calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel] = None
                 ): OtherReliefsFlatController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockAnswersConstructor = mock[AnswersConstructor]

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](Matchers.eq(KeystoreKeys.otherReliefsFlat))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](Matchers.eq(KeystoreKeys.privateResidenceRelief))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(PrivateResidenceReliefModel("No", None))))

    when(mockAnswersConstructor.getNRTotalGainAnswers(Matchers.any()))
      .thenReturn(Future.successful(gainAnswers))

    when(mockCalcConnector.calculateTotalGain(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainResultModel)))

    when(mockAnswersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(Matchers.any()))
      .thenReturn(Future.successful(Some(personalDetailsModel)))

    when(mockCalcConnector.calculateTaxableGainAfterPRR(Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(calculationResultsWithPRRModel)

    when(mockCalcConnector.getFullAEA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11000))))

    when(mockCalcConnector.getPartialAEA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(5500))))

    when(mockCalcConnector.calculateNRCGTTotalTax(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(TestModels.calculationResultsModelWithRebased)))

    when(mockCalcConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(TaxYearModel("2015/16", isValidYear = true, "2015/16"))))

    new OtherReliefsFlatController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val answersConstructor: AnswersConstructor = mockAnswersConstructor
    }
  }

  "OtherReliefsFlatController" should {
    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      OtherReliefsFlatController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }

  "Calling the .otherReliefsFlat action " when {

    "not supplied with a pre-existing stored model and a chargeable gain of £100 and total gain of £200" should {

      val target = setupTarget(
        None,
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModelIndividual)
      lazy val result = target.otherReliefsFlat(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200 with a valid calculation result" in {
        status(result) shouldBe 200
      }

      "load the otherReliefs flat page" in {
        document.title() shouldBe messages.question
      }

      s"have a total gain message with text '${messages.totalGain}' £200" in {
        document.getElementById("totalGain").text() shouldBe s"${messages.totalGain} £200"
      }

      s"have a taxable gain message with text '${messages.taxableGain}' £100" in {
        document.getElementById("taxableGain").text() shouldBe s"${messages.taxableGain} £100"
      }
    }

    "supplied with a pre-existing stored model" should {
      val testOtherReliefsModel = OtherReliefsModel(5000)
      val target = setupTarget(
        Some(testOtherReliefsModel),
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModelIndividual)
      lazy val result = target.otherReliefsFlat(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the otherReliefs flat page" in {
        document.title() shouldBe messages.question
      }
    }

    "supplied with an invalid session" should {
      val target = setupTarget(
        None,
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModelIndividual)
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
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModelIndividual)
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
        TestModels.totalGainAnswersModelWithRebasedTA,
        TestModels.calculationResultsModelWithRebased,
        TestModels.personalDetailsCalculationModelIndividual)
      lazy val request = fakeRequestToPOSTWithSession(("isClaimingOtherReliefs", "Yes"), ("otherReliefs", "-1000"))
      lazy val result = target.submitOtherReliefsFlat(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the other reliefs flat page" in {
        document.title() shouldBe messages.question
      }
    }
  }
}

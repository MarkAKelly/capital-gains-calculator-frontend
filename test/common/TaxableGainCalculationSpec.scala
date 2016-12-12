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

package common

import assets.MessageLookup.NonResident.{OtherReliefs => messages}
import connectors.CalculatorConnector
import constructors.nonresident.AnswersConstructor
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.OtherReliefsController
import models.nonresident._
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class TaxableGainCalculationSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[OtherReliefsModel],
                  gainResult: CalculationResultModel,
                  chargeableGainResult: Option[CalculationResultsWithTaxOwedModel] = None,
                  finalSummaryModel: TotalPersonalDetailsCalculationModel,
                  totalGainResult: Option[TotalGainResultsModel] = Some(TotalGainResultsModel(200, None, None)),
                  calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel] = None
                 ): OtherReliefsController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockAnswersConstructor = mock[AnswersConstructor]

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](Matchers.eq(KeystoreKeys.otherReliefsFlat))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](Matchers.eq(KeystoreKeys.privateResidenceRelief))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(PrivateResidenceReliefModel("No", None))))

    when(mockCalcConnector.calculateFlat(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(gainResult)))

    when(mockAnswersConstructor.getNRTotalGainAnswers(Matchers.any()))
      .thenReturn(Future.successful(Future.successful(TestModels.businessScenarioFiveModel)))

    when(mockCalcConnector.calculateTotalGain(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Future.successful(totalGainResult)))

    when(mockAnswersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(Matchers.any()))
      .thenReturn(Future.successful(Some(finalSummaryModel)))

    when(mockCalcConnector.calculateTaxableGainAfterPRR(Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(calculationResultsWithPRRModel)

    when(mockCalcConnector.getFullAEA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11000))))

    when(mockCalcConnector.getPartialAEA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(5500))))

    when(mockCalcConnector.calculateNRCGTTotalTax(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(chargeableGainResult))

    when(mockCalcConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(TaxYearModel("2015/16", true, "2015/16"))))

    new OtherReliefsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val answersConstructor: AnswersConstructor = mockAnswersConstructor
    }
  }

  "When the otherReliefs controller is supplied with a chargeable gain of £100 and total gain of £200" should {

    val finalAnswersModel = TotalPersonalDetailsCalculationModel(
      CustomerTypeModel("individual"),
      Some(CurrentIncomeModel(20000)),
      Some(PersonalAllowanceModel(0)),
      None,
      OtherPropertiesModel("Yes"),
      Some(PreviousLossOrGainModel("Neither")),
      None,
      None,
      Some(AnnualExemptAmountModel(0)),
      BroughtForwardLossesModel(false, None)
    )

    val chargeableGainResultModel = CalculationResultsWithTaxOwedModel(
      TotalTaxOwedModel(100, 100, 20, None, None, 200, 100, None, None, None, None, 0, None),
      None,
      None
    )

    val calcResultModel = CalculationResultModel(100, 100, 100, 20, 0, None, None, None)


    "supplied with a chargeable gain of £100 and total gain of £200" when {
      val target = setupTarget(None,
        calcResultModel,
        Some(chargeableGainResultModel),
        finalAnswersModel)
      lazy val result = target.otherReliefs(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"show help text with text '${messages.additionalHelp(200, 100)}'" in {
        document.body().select("#otherReliefHelpTwo").select("p").text() shouldBe messages.additionalHelp(200, 100)
      }
    }
  }
}

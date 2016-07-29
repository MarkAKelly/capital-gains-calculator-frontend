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

package controllers.resident.properties.ReportControllerSpec

import common.Dates
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.properties.{ReportController, SummaryController}
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel}
import models.resident.{TaxYearModel, _}
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.mvc.RequestHeader
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{summary => messages}
import models.resident.properties.{ChargeableGainAnswers, ReliefsModel, YourAnswersSummaryModel}

import scala.concurrent.Future

class FinalSummaryActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget
  (
    yourAnswersSummaryModel: YourAnswersSummaryModel,
    grossGain: BigDecimal,
    chargeableGainAnswers: ChargeableGainAnswers,
    chargeableGainResultModel: Option[ChargeableGainResultModel] = None,
    incomeAnswers: IncomeAnswersModel,
    totalGainAndTaxOwedModel: Option[TotalGainAndTaxOwedModel] = None,
    taxYearModel: Option[TaxYearModel]
  ): ReportController = {

    lazy val mockCalculatorConnector = mock[CalculatorConnector]

    when(mockCalculatorConnector.getPropertyGainAnswers(Matchers.any()))
      .thenReturn(Future.successful(yourAnswersSummaryModel))

    when(mockCalculatorConnector.calculateRttPropertyGrossGain(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(grossGain))

    when(mockCalculatorConnector.getPropertyDeductionAnswers(Matchers.any()))
      .thenReturn(Future.successful(chargeableGainAnswers))

    when(mockCalculatorConnector.calculateRttPropertyChargeableGain(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(chargeableGainResultModel)

    when(mockCalculatorConnector.getPropertyIncomeAnswers(Matchers.any()))
      .thenReturn(Future.successful(incomeAnswers))

    when(mockCalculatorConnector.calculateRttPropertyTotalGainAndTax(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(totalGainAndTaxOwedModel))

    when(mockCalculatorConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    when(mockCalculatorConnector.getFullAEA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11100))))

    new ReportController {
      override val calcConnector: CalculatorConnector = mockCalculatorConnector
      override def host(implicit request: RequestHeader): String = "http://localhost:9977/"
    }
  }

  "Calling .finalSummaryReport from the ReportController" when {

    "a positive taxable gain is returned" should {
      lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
        30000,
        0,
        10000,
        0,
        0)
      lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(ReliefsModel(false)), None, Some(OtherPropertiesModel(false)),
        Some(AllowableLossesModel(false)), None, Some(LossesBroughtForwardModel(false)), None, None)
      lazy val chargeableGainResultModel = ChargeableGainResultModel(20000, 20000, 11100, 0, 11100)
      lazy val incomeAnswersModel = IncomeAnswersModel(None, Some(CurrentIncomeModel(20000)), Some(PersonalAllowanceModel(10000)))
      lazy val totalGainAndTaxOwedModel = TotalGainAndTaxOwedModel(20000, 20000, 11100, 11100, 3600, 20000, 18, None, None)
      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        10000,
        chargeableGainAnswers,
        Some(chargeableGainResultModel),
        incomeAnswersModel,
        Some(totalGainAndTaxOwedModel),
        taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
      )
      lazy val result = target.finalSummaryReport(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return a pdf" in {
        contentType(result) shouldBe Some("application/pdf")
      }

      "return a pdf as an attachment" in {
        header("Content-Disposition", result).get should include("attachment")
      }

      "return the pdf with a filename of 'Summary'" in {
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf"""")
      }
    }

    "a positive taxable gain is returned with an invalid tax year and two tax rates" should {
      lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
        30000,
        0,
        10000,
        0,
        0)
      lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(ReliefsModel(false)), None, Some(OtherPropertiesModel(false)),
        Some(AllowableLossesModel(false)), None, Some(LossesBroughtForwardModel(false)), None, None)
      lazy val chargeableGainResultModel = ChargeableGainResultModel(20000, 20000, 11100, 0, 11100)
      lazy val incomeAnswersModel = IncomeAnswersModel(None, Some(CurrentIncomeModel(20000)), Some(PersonalAllowanceModel(10000)))
      lazy val totalGainAndTaxOwedModel = TotalGainAndTaxOwedModel(20000, 20000, 11100, 11100, 3600, 20000, 18, Some(5000), Some(28))
      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        10000,
        chargeableGainAnswers,
        Some(chargeableGainResultModel),
        incomeAnswersModel,
        Some(totalGainAndTaxOwedModel),
        taxYearModel = Some(TaxYearModel("2013/2014", false, "2015/16"))
      )
      lazy val result = target.finalSummaryReport(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return a pdf" in {
        contentType(result) shouldBe Some("application/pdf")
      }

      "return a pdf as an attachment" in {
        header("Content-Disposition", result).get should include("attachment")
      }

      "return the pdf with a filename of 'Summary'" in {
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf"""")
      }
    }
  }
}


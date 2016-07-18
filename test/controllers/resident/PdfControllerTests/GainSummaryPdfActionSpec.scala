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

package controllers.resident.PdfControllerTests

import common.Dates
import assets.MessageLookup.{summary => messages}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.{PdfController, routes}
import models.resident.{TaxYearModel, _}
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class GainSummaryPdfActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar{

  def setupTarget
  (
    yourAnswersSummaryModel: YourAnswersSummaryModel,
    grossGain: BigDecimal,
    chargeableGainAnswers: ChargeableGainAnswers,
    chargeableGainResultModel: Option[ChargeableGainResultModel] = None,
    incomeAnswers: IncomeAnswersModel,
    totalGainAndTaxOwedModel: Option[TotalGainAndTaxOwedModel] = None,
    taxYearModel: Option[TaxYearModel]
  ): PdfController = {

    lazy val mockCalculatorConnector = mock[CalculatorConnector]

    when(mockCalculatorConnector.getYourAnswers(Matchers.any()))
      .thenReturn(Future.successful(yourAnswersSummaryModel))

    when(mockCalculatorConnector.calculateRttGrossGain(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(grossGain))

    when(mockCalculatorConnector.getChargeableGainAnswers(Matchers.any()))
      .thenReturn(Future.successful(chargeableGainAnswers))

    when(mockCalculatorConnector.calculateRttChargeableGain(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(chargeableGainResultModel)

    when(mockCalculatorConnector.getIncomeAnswers(Matchers.any()))
      .thenReturn(Future.successful(incomeAnswers))

    when(mockCalculatorConnector.calculateRttTotalGainAndTax(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(totalGainAndTaxOwedModel))

    when(mockCalculatorConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    new PdfController {
      override val calcConnector: CalculatorConnector = mockCalculatorConnector
    }
  }

  "Calling .summary from the SummaryController" when {

    "a negative total gain is returned" should {
      lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
        3000,
        10,
        5000,
        5,
        0)
      lazy val chargeableGainAnswers = ChargeableGainAnswers(None, None, None, None, None, None, None, None)
      lazy val incomeAnswersModel = IncomeAnswersModel(None, None, None)
      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        -6000,
        chargeableGainAnswers, None, incomeAnswersModel,
        taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
      )
      lazy val result = target.gainSummaryReport(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

//      "return some pdf" in {
//        contentType(result) shouldBe Some("text/pdf")
//      }
    }
  }
}

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
import controllers.resident.properties.ReportController
import models.resident._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.mvc.RequestHeader
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{SummaryPage => messages}
import models.resident.properties._

import scala.concurrent.Future

class DeductionsSummaryActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget
  (
    yourAnswersSummaryModel: YourAnswersSummaryModel,
    grossGain: BigDecimal,
    chargeableGainAnswers: ChargeableGainAnswers,
    chargeableGainResultModel: Option[ChargeableGainResultModel] = None,
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

    when(mockCalculatorConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    when(mockCalculatorConnector.getFullAEA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11100))))

    new ReportController {
      override val calcConnector: CalculatorConnector = mockCalculatorConnector
      override def host(implicit request: RequestHeader): String = "http://localhost:9977/"
    }
  }

  "Calling .deductionReport from the ReportController" when {

    "a 0 gain is returned" should {
      lazy val gainAnswers = YourAnswersSummaryModel(
        disposalDate = Dates.constructDate(10, 10, 2018),
        disposalValue = Some(200000),
        worthWhenSoldForLess = None,
        whoDidYouGiveItTo = Some("Other"),
        worthWhenGaveAway = Some(10000),
        disposalCosts = 10000,
        acquisitionValue = Some(100000),
        worthWhenInherited = None,
        worthWhenGifted = None,
        worthWhenBoughtForLess = None,
        acquisitionCosts = 10000,
        improvements = 30000,
        givenAway = true,
        sellForLess = Some(false),
        ownerBeforeLegislationStart = true,
        valueBeforeLegislationStart = Some(5000),
        howBecameOwner = Some("Bought"),
        boughtForLessThanWorth = Some(false)
      )

      lazy val deductionAnswers = ChargeableGainAnswers(
        Some(OtherPropertiesModel(true)),
        Some(AllowableLossesModel(true)),
        Some(AllowableLossesValueModel(10000)),
        Some(LossesBroughtForwardModel(true)),
        Some(LossesBroughtForwardValueModel(10000)),
        Some(AnnualExemptAmountModel(1000)),
        Some(PropertyLivedInModel(false)),
        None,
        None,
        None,
        None
      )
      lazy val results = ChargeableGainResultModel(BigDecimal(50000),
        BigDecimal(-11000),
        BigDecimal(0),
        BigDecimal(11000),
        BigDecimal(71000),
        BigDecimal(0),
        BigDecimal(0),
        Some(BigDecimal(0)),
        Some(BigDecimal(0)),
        0,
        0
      )

      lazy val target = setupTarget(
        gainAnswers,
        0,
        deductionAnswers,
        Some(results),
        taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
      )
      lazy val result = target.deductionsReport(fakeRequestWithSession)

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

    "a carried forward loss is returned with an invalid tax year" should {
      lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(
        disposalDate = Dates.constructDate(12, 1, 2016),
        disposalValue = Some(30000),
        worthWhenSoldForLess = None,
        whoDidYouGiveItTo = None,
        worthWhenGaveAway = None,
        disposalCosts = 0,
        acquisitionValue = Some(10000),
        worthWhenInherited = None,
        worthWhenGifted = None,
        worthWhenBoughtForLess = None,
        acquisitionCosts = 0,
        improvements = 0,
        givenAway = false,
        sellForLess = Some(false),
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart =  None,
        howBecameOwner = Some("Bought"),
        boughtForLessThanWorth = Some(false)
      )

      lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(OtherPropertiesModel(false)),
        Some(AllowableLossesModel(false)), None, Some(LossesBroughtForwardModel(false)), None, None,
        Some(PropertyLivedInModel(false)), None, None, None, None)
      lazy val chargeableGainResultModel = ChargeableGainResultModel(20000, 20000, 11100, 0, 11100,
        BigDecimal(0), BigDecimal(0), Some(BigDecimal(0)), Some(BigDecimal(0)), 0, 0)
      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        -10000,
        chargeableGainAnswers,
        Some(chargeableGainResultModel),
        taxYearModel = Some(TaxYearModel("2013/2014", false, "2015/16"))
      )
      lazy val result = target.deductionsReport(fakeRequestWithSession)

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

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

package controllers.resident.shares

import assets.MessageLookup.{SummaryPage => messages}
import common.Dates
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import models.resident._
import models.resident.shares._
import models.resident.income._
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future


class SummaryActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget
  (
    gainAnswersModel: GainAnswersModel,
    grossGain: BigDecimal,
    chargeableGainAnswers: DeductionGainAnswersModel,
    chargeableGainResultModel: Option[ChargeableGainResultModel] = None,
    taxYearModel: Option[TaxYearModel],
    incomeAnswers: IncomeAnswersModel,
    totalGainAndTaxOwedModel: Option[TotalGainAndTaxOwedModel] = None
    ): SummaryController = {

    lazy val mockCalculatorConnector = mock[CalculatorConnector]

    when(mockCalculatorConnector.getShareGainAnswers(Matchers.any()))
      .thenReturn(Future.successful(gainAnswersModel))

    when(mockCalculatorConnector.calculateRttShareGrossGain(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(grossGain))

    when(mockCalculatorConnector.getShareDeductionAnswers(Matchers.any()))
      .thenReturn(Future.successful(chargeableGainAnswers))

    when(mockCalculatorConnector.calculateRttShareChargeableGain(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(chargeableGainResultModel)

    when(mockCalculatorConnector.getShareIncomeAnswers(Matchers.any()))
      .thenReturn(Future.successful(incomeAnswers))

    when(mockCalculatorConnector.calculateRttShareTotalGainAndTax(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(totalGainAndTaxOwedModel))

    when(mockCalculatorConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    when(mockCalculatorConnector.getFullAEA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11100))))

    new SummaryController {
      override val calculatorConnector: CalculatorConnector = mockCalculatorConnector
    }
  }

  "Calling .summary from the SummaryController for Shares" when {

    "a negative gross gain is returned" should {
      lazy val gainAnswers = GainAnswersModel(
        disposalDate = Dates.constructDate(12, 1, 2016),
        soldForLessThanWorth = false,
        disposalValue = Some(3000),
        worthWhenSoldForLess = None,
        disposalCosts = 10,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false),
        worthWhenInherited = None,
        acquisitionValue = Some(5000),
        acquisitionCosts = 5
      )
      lazy val chargeableGainAnswers = DeductionGainAnswersModel(None, None, None, None, None, None)
      lazy val incomeAnswersModel = IncomeAnswersModel(None, None, None)
      lazy val target = setupTarget(
        gainAnswers,
        -6000,
        chargeableGainAnswers,
        None,
        taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16")),
        incomeAnswersModel
      )
      lazy val result = target.summary()(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }
    }

    "a zero taxable gain is returned with no other disposals of or brought forward losses" should {
      lazy val gainAnswers = GainAnswersModel(
        disposalDate = Dates.constructDate(12, 1, 2016),
        soldForLessThanWorth = false,
        disposalValue = Some(13000),
        worthWhenSoldForLess = None,
        disposalCosts = 500,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false),
        worthWhenInherited = None,
        acquisitionValue = Some(5000),
        acquisitionCosts = 500
      )
      lazy val chargeableGainAnswers = DeductionGainAnswersModel(Some(OtherPropertiesModel(false)),
        None, None, Some(LossesBroughtForwardModel(false)), None, None)
      lazy val chargeableGainResultModel = ChargeableGainResultModel(7000, 0, 11100, 0, 0, BigDecimal(0), BigDecimal(0), None, None, 0, 0)
      lazy val incomeAnswersModel = IncomeAnswersModel(None, None, None)
      lazy val target = setupTarget(
        gainAnswers,
        7000,
        chargeableGainAnswers,
        Some(chargeableGainResultModel),
        taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16")),
        incomeAnswersModel
      )
      lazy val result = target.summary()(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      s"has a back link to '${routes.DeductionsController.lossesBroughtForward().toString()}'" in {
        doc.getElementById("back-link").attr("href") shouldBe routes.DeductionsController.lossesBroughtForward().toString
      }
    }

    "a negative taxable gain is returned with no other disposals of but with brought forward losses" should {
      lazy val gainAnswers = GainAnswersModel(
        disposalDate = Dates.constructDate(12, 1, 2016),
        soldForLessThanWorth = false,
        disposalValue = Some(13000),
        worthWhenSoldForLess = None,
        disposalCosts = 500,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false),
        worthWhenInherited = None,
        acquisitionValue = Some(5000),
        acquisitionCosts = 500
      )
      lazy val chargeableGainAnswers = DeductionGainAnswersModel(Some(OtherPropertiesModel(false)), None, None,
        Some(LossesBroughtForwardModel(true)), Some(LossesBroughtForwardValueModel(1000)), None)
      lazy val chargeableGainResultModel = ChargeableGainResultModel(7000, -1000, 11100, 5100, 1000, BigDecimal(0), BigDecimal(0), None, None, 0, 0)
      lazy val incomeAnswersModel = IncomeAnswersModel(None, None, None)
      lazy val target = setupTarget(
        gainAnswers,
        7000,
        chargeableGainAnswers,
        Some(chargeableGainResultModel),
        taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16")),
        incomeAnswersModel
      )
      lazy val result = target.summary()(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      s"has a link to '${routes.DeductionsController.lossesBroughtForwardValue().toString()}'" in {
        doc.getElementById("back-link").attr("href") shouldBe routes.DeductionsController.lossesBroughtForwardValue().toString
      }
    }

    "a negative taxable gain is returned with other disposals" should {
      lazy val gainAnswers = GainAnswersModel(
        disposalDate = Dates.constructDate(12, 1, 2016),
        soldForLessThanWorth = false,
        disposalValue = Some(15000),
        worthWhenSoldForLess = None,
        disposalCosts = 1000,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false),
        worthWhenInherited = None,
        acquisitionValue = Some(3000),
        acquisitionCosts = 2000
      )
      lazy val chargeableGainAnswers = DeductionGainAnswersModel(Some(OtherPropertiesModel(true)), Some(AllowableLossesModel(true)),
        Some(AllowableLossesValueModel(BigDecimal(1000))), Some(LossesBroughtForwardModel(false)), None, Some(AnnualExemptAmountModel(10000)))
      lazy val chargeableGainResultModel = ChargeableGainResultModel(10000, -1100, 11100, 0, 11100, BigDecimal(0), BigDecimal(0), None, None, 0, 0)
      lazy val incomeAnswersModel = IncomeAnswersModel(None, Some(CurrentIncomeModel(20000)), Some(PersonalAllowanceModel(10000)))
      lazy val totalGainAndTaxOwedModel = TotalGainAndTaxOwedModel(20000, 20000, 11100, 11100, 3600, 20000, 18, None, None, None, None, 0, 0)
      lazy val target = setupTarget(
        gainAnswers,
        10000,
        chargeableGainAnswers,
        Some(chargeableGainResultModel),
        taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16")),
        incomeAnswersModel,
        Some(totalGainAndTaxOwedModel)
      )
      lazy val result = target.summary()(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      s"has a link to '${routes.DeductionsController.lossesBroughtForward().toString()}'" in {
        doc.getElementById("back-link").attr("href") shouldBe routes.DeductionsController.lossesBroughtForward().toString
      }
    }

    "a negative taxable gain is returned with other properties disposed of but an allowable loss of 0" should {
      lazy val gainAnswers = GainAnswersModel(
        disposalDate = Dates.constructDate(12, 1, 2016),
        soldForLessThanWorth = false,
        disposalValue = Some(15000),
        worthWhenSoldForLess = None,
        disposalCosts = 1000,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false),
        worthWhenInherited = None,
        acquisitionValue = Some(3000),
        acquisitionCosts = 2000
      )
      lazy val chargeableGainAnswers = DeductionGainAnswersModel(Some(OtherPropertiesModel(true)), Some(AllowableLossesModel(true)),
        Some(AllowableLossesValueModel(BigDecimal(0))), Some(LossesBroughtForwardModel(false)), None, Some(AnnualExemptAmountModel(10000)))
      lazy val chargeableGainResultModel = ChargeableGainResultModel(10000, -1100, 11100, 0, 11100, BigDecimal(0), BigDecimal(0), None, None, 0, 0)
      lazy val incomeAnswersModel = IncomeAnswersModel(None, None, None)
      lazy val target = setupTarget(
        gainAnswers,
        10000,
        chargeableGainAnswers,
        Some(chargeableGainResultModel),
        taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16")),
        incomeAnswersModel
      )
      lazy val result = target.summary()(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      s"has a link to '${routes.DeductionsController.annualExemptAmount().toString()}'" in {
        doc.getElementById("back-link").attr("href") shouldBe routes.DeductionsController.annualExemptAmount().toString
      }
    }
  }

  "Calling .summary from the SummaryController with no session" should {

    lazy val result = SummaryController.summary(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout view" in {
      redirectLocation(result).get should include("/calculate-your-capital-gains/resident/shares/session-timeout")
    }
  }
}
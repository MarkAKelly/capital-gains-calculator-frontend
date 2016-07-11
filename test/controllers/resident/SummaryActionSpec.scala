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

package controllers.resident

import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.test.Helpers._
import assets.MessageLookup.{summary => messages}
import common.Dates
import connectors.CalculatorConnector
import models.resident._
import models.resident.income._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future


class SummaryActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget
  (
    yourAnswersSummaryModel: YourAnswersSummaryModel,
    grossGain: BigDecimal,
    chargeableGainAnswers: ChargeableGainAnswers,
    chargeableGainResultModel: Option[ChargeableGainResultModel] = None,
    incomeAnswers: IncomeAnswersModel,
    totalGainAndTaxOwedModel: Option[TotalGainAndTaxOwedModel] = None
  ): SummaryController = {

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

    new SummaryController {
      override val calculatorConnector: CalculatorConnector = mockCalculatorConnector
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
        chargeableGainAnswers, None,  incomeAnswersModel
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

      s"has a link to '${routes.GainController.improvements().toString()}'" in {
        doc.getElementById("back-link").attr("href") shouldBe routes.GainController.improvements().toString
      }

    }

    "a negative taxable gain is returned with no other properties disposed of or brought forward losses" should {
      lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
        3000,
        10,
        5000,
        5,
        0)
      lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(ReliefsModel(false)), None, Some(OtherPropertiesModel(false)),
        None, None, Some(LossesBroughtForwardModel(false)), None, None)
      lazy val chargeableGainResultModel = ChargeableGainResultModel(10000, -1100, 11100, 11100)
      lazy val incomeAnswersModel = IncomeAnswersModel(None, None, None)
      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        10000,
        chargeableGainAnswers,
        Some(chargeableGainResultModel),
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

      s"has a link to '${routes.DeductionsController.lossesBroughtForward().toString()}'" in {
        doc.getElementById("back-link").attr("href") shouldBe routes.DeductionsController.lossesBroughtForward().toString
      }
    }

    "a negative taxable gain is returned with no other properties disposed of but with brought forward losses" should {
      lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
        3000,
        10,
        5000,
        5,
        0)
      lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(ReliefsModel(false)), None, Some(OtherPropertiesModel(false)),
        None, None, Some(LossesBroughtForwardModel(true)), Some(LossesBroughtForwardValueModel(1000)), None)
      lazy val chargeableGainResultModel = ChargeableGainResultModel(10000, -1100, 11100, 11100)
      lazy val incomeAnswersModel = IncomeAnswersModel(None, None, None)
      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        10000,
        chargeableGainAnswers,
        Some(chargeableGainResultModel),
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

    "a negative taxable gain is returned with other properties disposed of" should {
      lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
        3000,
        10,
        5000,
        5,
        0)
      lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(ReliefsModel(false)), None, Some(OtherPropertiesModel(true)),
        Some(AllowableLossesModel(true)), Some(AllowableLossesValueModel(BigDecimal(1000))), Some(LossesBroughtForwardModel(false)), None, Some(AnnualExemptAmountModel(10000)))
      lazy val chargeableGainResultModel = ChargeableGainResultModel(10000, -1100, 11100, 11100)
      lazy val incomeAnswersModel = IncomeAnswersModel(None, None, None)
      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        10000,
        chargeableGainAnswers,
        Some(chargeableGainResultModel),
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

      s"has a link to '${routes.DeductionsController.lossesBroughtForward().toString()}'" in {
        doc.getElementById("back-link").attr("href") shouldBe routes.DeductionsController.lossesBroughtForward().toString
      }
    }

    "a negative taxable gain is returned with other properties disposed of but an allowable loss of 0" should {
      lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
        3000,
        10,
        5000,
        5,
        0)
      lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(ReliefsModel(false)), None, Some(OtherPropertiesModel(true)),
        Some(AllowableLossesModel(true)), Some(AllowableLossesValueModel(BigDecimal(0))), Some(LossesBroughtForwardModel(false)), None, Some(AnnualExemptAmountModel(10000)))
      lazy val chargeableGainResultModel = ChargeableGainResultModel(10000, -1100, 11100, 11100)
      lazy val incomeAnswersModel = IncomeAnswersModel(None, None, None)
      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        10000,
        chargeableGainAnswers,
        Some(chargeableGainResultModel),
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

    "a positive taxable gain is returned with no other" should {
      lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
        30000,
        0,
        10000,
        0,
        0)
      lazy val chargeableGainAnswers = ChargeableGainAnswers(Some(ReliefsModel(false)), None, Some(OtherPropertiesModel(false)),
        Some(AllowableLossesModel(false)), None, Some(LossesBroughtForwardModel(false)), None, None)
      lazy val chargeableGainResultModel = ChargeableGainResultModel(20000, 20000, 11100, 11100)
      lazy val incomeAnswersModel = IncomeAnswersModel(None, Some(CurrentIncomeModel(20000)), Some(PersonalAllowanceModel(10000)))
      lazy val totalGainAndTaxOwedModel = TotalGainAndTaxOwedModel(20000, 20000, 11100, 11100, 3600, 20000, 18, None, None)
      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        10000,
        chargeableGainAnswers,
        Some(chargeableGainResultModel),
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

      s"has a link to '${routes.IncomeController.personalAllowance().toString()}'" in {
        doc.getElementById("back-link").attr("href") shouldBe routes.IncomeController.personalAllowance().toString
      }
    }
  }



  "Calling .summary from the SummaryController with no session" should {

    lazy val result = SummaryController.summary(fakeRequest)
    lazy val doc = Jsoup.parse(bodyOf(result))

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout view" in {
      redirectLocation(result).get shouldBe "/calculate-your-capital-gains/non-resident/session-timeout"
    }
  }
}

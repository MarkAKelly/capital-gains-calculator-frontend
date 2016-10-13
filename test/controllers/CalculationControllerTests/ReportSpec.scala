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

import common.TestModels._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import play.api.mvc.RequestHeader
import models.resident.TaxYearModel
import connectors.CalculatorConnector
import org.scalatest.mock.MockitoSugar
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.ReportController
import assets.MessageLookup.{summaryPage => messages}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import models.nonresident.{CalculationResultModel, SummaryModel}

import scala.concurrent.Future

class ReportSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget
  (
    summaryModel: SummaryModel,
    calculationResultsModel: Option[CalculationResultModel],
    taxYearModel: Option[TaxYearModel]
  ): ReportController = {

    lazy val mockCalculatorConnector = mock[CalculatorConnector]

    when(mockCalculatorConnector.createSummary(Matchers.any()))
      .thenReturn(Future.successful(summaryModel))

    when(mockCalculatorConnector.calculateFlat(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(calculationResultsModel))

    when(mockCalculatorConnector.calculateTA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(calculationResultsModel))

    when(mockCalculatorConnector.calculateRebased(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(calculationResultsModel))

    when(mockCalculatorConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    new ReportController {
      override val calcConnector: CalculatorConnector = mockCalculatorConnector
      override def host(implicit request: RequestHeader): String = "http://localhost:9977"
    }
  }

  "ReportController" should {
    "use the correct calculator connector" in {
      ReportController.calcConnector shouldBe CalculatorConnector
    }
  }


  "Calling .summaryReport from the ReportController" when {

    "a 0 gain is returned by flat" should {

      lazy val taxYear = TaxYearModel("2016-12-12", true, "16")

      lazy val target = setupTarget(
        sumModelFlat,
        Some(calcModelZeroTotal),
        Some(taxYear)
      )

      lazy val result = target.summaryReport(fakeRequestWithSession)

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

    "a 0 gain is returned by rebased" should {

      lazy val taxYear = TaxYearModel("2016-12-12", true, "16")

      lazy val target = setupTarget(
        sumModelRebased,
        Some(calcModelZeroTotal),
        Some(taxYear)
      )

      lazy val result = target.summaryReport(fakeRequestWithSession)

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

    "a 0 gain is returned by time apportioned" should {

      lazy val taxYear = TaxYearModel("2016-12-12", true, "16")

      lazy val target = setupTarget(
        sumModelTA,
        Some(calcModelZeroTotal),
        Some(taxYear)
      )

      lazy val result = target.summaryReport(fakeRequestWithSession)

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

    "a loss is returned by flat " should {

      lazy val taxYear = TaxYearModel("2016-12-12", true, "16")

      lazy val target = setupTarget(
        sumModelFlat,
        Some(calcModelLoss),
        Some(taxYear)
      )

      lazy val result = target.summaryReport(fakeRequestWithSession)

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

    "a loss is returned by rebased " should {

      lazy val taxYear = TaxYearModel("2016-12-12", true, "16")

      lazy val target = setupTarget(
        sumModelRebased,
        Some(calcModelLoss),
        Some(taxYear)
      )

      lazy val result = target.summaryReport(fakeRequestWithSession)

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

    "a loss is returned by apportioned" should {

      lazy val taxYear = TaxYearModel("2016-12-12", true, "16")

      lazy val target = setupTarget(
        sumModelTA,
        Some(calcModelLoss),
        Some(taxYear)
      )

      lazy val result = target.summaryReport(fakeRequestWithSession)

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

    "a gain is returned by flat" should {

      lazy val taxYear = TaxYearModel("2016-12-12", true, "16")

      lazy val target = setupTarget(
        sumModelFlat,
        Some(calcModelTwoRates),
        Some(taxYear)
      )

      lazy val result = target.summaryReport(fakeRequestWithSession)

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

    "a gain is returned by rebased" should {

      lazy val taxYear = TaxYearModel("2016-12-12", true, "16")

      lazy val target = setupTarget(
        sumModelRebased,
        Some(calcModelTwoRates),
        Some(taxYear)
      )

      lazy val result = target.summaryReport(fakeRequestWithSession)

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

    "a gain is returned by apportioned" should {

      lazy val taxYear = TaxYearModel("2016-12-12", true, "16")

      lazy val target = setupTarget(
        sumModelTA,
        Some(calcModelTwoRates),
        Some(taxYear)
      )

      lazy val result = target.summaryReport(fakeRequestWithSession)

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

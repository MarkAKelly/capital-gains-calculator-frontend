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
import assets.MessageLookup.{summaryPage => messages}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.properties.ReportController
import models.resident.properties.YourAnswersSummaryModel
import models.resident.TaxYearModel
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.test.Helpers._

import scala.concurrent.Future

class GainSummaryActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget
  (
    yourAnswersSummaryModel: YourAnswersSummaryModel,
    grossGain: BigDecimal,
    taxYearModel: Option[TaxYearModel]
  ): ReportController = {

    lazy val mockCalculatorConnector = mock[CalculatorConnector]

    when(mockCalculatorConnector.getPropertyGainAnswers(Matchers.any()))
      .thenReturn(Future.successful(yourAnswersSummaryModel))

    when(mockCalculatorConnector.calculateRttPropertyGrossGain(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(grossGain))

    when(mockCalculatorConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    new ReportController{
      override val calcConnector: CalculatorConnector = mockCalculatorConnector
      override def host(implicit request: RequestHeader): String  = "http://localhost:9977/"
    }
  }

  "Calling .gainSummaryReport from the ReportController" when {

    "a negative total gain is returned" should {
      lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
        Some(3000),
        None,
        10,
        5000,
        5,
        0,
        true,
        Some(false),
        true,
        Some(BigDecimal(5000)),
        Some("Bought"),
        Some(false))

      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        -6000,
        taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
      )
      lazy val result = target.gainSummaryReport(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return a pdf" in {
        contentType(result) shouldBe Some("application/pdf")
      }

      "should return the pdf as an attachment" in {
        header("Content-Disposition", result).get should include("attachment")
      }

      "should have a filename of 'Summary.pdf'" in {
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf"""")
      }
    }

    "a zero total gain is returned with an invalid tax year" should {
      lazy val yourAnswersSummaryModel = YourAnswersSummaryModel(Dates.constructDate(12, 1, 2016),
        Some(3000),
        Some(500),
        10,
        5000,
        5,
        0,
        false,
        Some(true),
        false,
        None,
        Some("Bought"),
        Some(false))

      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        -6000,
        taxYearModel = Some(TaxYearModel("2013/2014", false, "2015/16"))
      )
      lazy val result = target.gainSummaryReport(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return a pdf" in {
        contentType(result) shouldBe Some("application/pdf")
      }

      "should return the pdf as an attachment" in {
        header("Content-Disposition", result).get should include("attachment")
      }

      "should have a filename of 'Summary'" in {
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf"""")
      }
    }
  }
}

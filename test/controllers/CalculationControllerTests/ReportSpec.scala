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
import assets.MessageLookup.{SummaryPage => messages}
import constructors.nonresident.AnswersConstructor
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import models.nonresident._
import common.nonresident.CalculationType

import scala.concurrent.Future

class ReportSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget
  (
    totalGainAnswersModel: TotalGainAnswersModel,
    totalGainResultsModel: Option[TotalGainResultsModel],
    taxYearModel: Option[TaxYearModel],
    calculationElectionModel: CalculationElectionModel
  ): ReportController = {

    lazy val mockCalculatorConnector = mock[CalculatorConnector]
    lazy val mockAnswersConstructor = mock[AnswersConstructor]

    when(mockAnswersConstructor.getNRTotalGainAnswers(Matchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel))

    when(mockCalculatorConnector.fetchAndGetFormData[CalculationElectionModel](Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Some(calculationElectionModel))

    when(mockCalculatorConnector.calculateTotalGain(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(totalGainResultsModel))

    when(mockCalculatorConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    new ReportController {
      override val calcConnector: CalculatorConnector = mockCalculatorConnector
      override val answersConstructor = mockAnswersConstructor
      override def host(implicit request: RequestHeader): String = "http://localhost:9977"
    }
  }

  val model = TotalGainAnswersModel(DisposalDateModel(5, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(1000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(2000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel("Yes", Some(4), Some(10), Some(2013)),
    Some(RebasedValueModel(Some(3000))),
    Some(RebasedCostsModel("Yes", Some(300))),
    ImprovementsModel("Yes", Some(10), Some(20)),
    Some(OtherReliefsModel(30)))

  "ReportController" should {
    "use the correct calculator connector" in {
      ReportController.calcConnector shouldBe CalculatorConnector
    }

    "use the correct answers constructor" in {
      ReportController.answersConstructor shouldBe AnswersConstructor
    }
  }


  "Calling .summaryReport from the ReportController" when {

    "the calculation chosen is flat" should {

      lazy val taxYear = TaxYearModel("2016-12-12", true, "16")

      lazy val target = setupTarget(
        model,
        Some(TotalGainResultsModel(1000, None, None)),
        Some(taxYear),
        CalculationElectionModel(CalculationType.flat)
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

    "the calculation chosen is rebased" should {

      lazy val taxYear = TaxYearModel("2016-12-12", true, "16")

      lazy val target = setupTarget(
        model,
        Some(TotalGainResultsModel(1000, Some(2000), None)),
        Some(taxYear),
        CalculationElectionModel(CalculationType.rebased)
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

    "the calculation chosen is time apportioned" should {

      lazy val taxYear = TaxYearModel("2016-12-12", true, "16")

      lazy val target = setupTarget(
        model,
        Some(TotalGainResultsModel(1000, None, Some(3000))),
        Some(taxYear),
        CalculationElectionModel(CalculationType.timeApportioned)
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

    "supplied without a session" should {
      lazy val taxYear = TaxYearModel("2016-12-12", true, "16")

      lazy val target = setupTarget(
        model,
        Some(TotalGainResultsModel(1000, None, None)),
        Some(taxYear),
        CalculationElectionModel(CalculationType.flat)
      )

      lazy val result = target.summaryReport(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/session-timeout")
      }
    }
  }
}

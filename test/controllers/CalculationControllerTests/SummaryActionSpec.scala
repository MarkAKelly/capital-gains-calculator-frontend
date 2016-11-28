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

import assets.MessageLookup.NonResident.{Summary => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import common.DefaultRoutes._
import common.nonresident.CalculationType
import common.{KeystoreKeys, TestModels}
import connectors.CalculatorConnector
import constructors.nonresident.AnswersConstructor
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.SummaryController
import models.nonresident._
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class SummaryActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(summary: TotalGainAnswersModel,
                  result: TotalGainResultsModel,
                  calculationElectionModel: CalculationElectionModel
                 ): SummaryController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockAnswersConstructor = mock[AnswersConstructor]

    when(mockAnswersConstructor.getNRTotalGainAnswers(Matchers.any()))
      .thenReturn(Future.successful(summary))

    when(mockCalcConnector.fetchAndGetFormData[CalculationElectionModel](Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Some(calculationElectionModel))

    when(mockCalcConnector.calculateTotalGain(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(result)))

    new SummaryController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val answersConstructor: AnswersConstructor = mockAnswersConstructor
    }
  }

  val answerModel = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel("No", None, None, None),
    None,
    None,
    ImprovementsModel("No", None, None),
    None
  )

  "SummaryController" should {
    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      SummaryController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }

  "Calling the .summary action" when {

    "provided with a valid session and three potential calculations" should {
      val target = setupTarget(
        answerModel,
        TotalGainResultsModel(1000, Some(2000), Some(3000)),
        CalculationElectionModel(CalculationType.flat)
      )
      lazy val result = target.summary()(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the summary page" in {
        document.title() shouldBe messages.title
      }

      "has a back-link to the calculation election page" in {
        document.select("#back-link").attr("href") shouldEqual controllers.nonresident.routes.CalculationElectionController.calculationElection().url
      }
    }

    "provided with a valid session and only one (flat) calculation" should {
      val target = setupTarget(
        answerModel,
        TotalGainResultsModel(1000, None, None),
        CalculationElectionModel(CalculationType.flat)
      )
      lazy val result = target.summary()(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the summary page" in {
        document.title() shouldBe messages.title
      }

      "has a back-link to the check your answers page" in {
        document.select("#back-link").attr("href") shouldEqual controllers.nonresident.routes.CheckYourAnswersController.checkYourAnswers().url
      }
    }

    "provided with an invalid session" should {
      val target = setupTarget(
        answerModel,
        TotalGainResultsModel(1000, Some(2000), Some(3000)),
        CalculationElectionModel(CalculationType.flat)
      )
      lazy val result = target.summary()(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/session-timeout")
      }
    }
  }

  "calling the .restart action" should {
    val target = setupTarget(
      answerModel,
      TotalGainResultsModel(1000, Some(2000), Some(3000)),
      CalculationElectionModel(CalculationType.flat)
    )
    lazy val result = target.restart()(fakeRequestWithSession)

    "return a 303" in {
      status(result) shouldBe 303
    }

    "redirect to the start page" in {
      redirectLocation(result).get shouldBe controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }
}

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

import assets.MessageLookup.NonResident.{OtherReliefs => messages}
import common.TestModels
import connectors.CalculatorConnector
import constructors.nonresident.AnswersConstructor
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.OtherReliefsController
import models.nonresident._
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class OtherReliefsActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[OtherReliefsModel], summary: SummaryModel, result: CalculationResultModel): OtherReliefsController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockAnswersConstructor = mock[AnswersConstructor]
    val totalGainResult = TotalGainResultsModel(0, None, None)

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.createSummary(Matchers.any()))
      .thenReturn(Future.successful(summary))

    when(mockCalcConnector.calculateFlat(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(result)))

    when(mockAnswersConstructor.getNRTotalGainAnswers(Matchers.any()))
      .thenReturn(Future.successful(Future.successful(TestModels.businessScenarioFiveModel)))

    when(mockCalcConnector.calculateTotalGain(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainResult)))

    new OtherReliefsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val answersConstructor: AnswersConstructor = mockAnswersConstructor
    }
  }

  "OtherReliefsController" should {
    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      OtherReliefsController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }

  "Calling the .otherReliefs action " when {

    "not supplied with a pre-existing stored model" should {
      val target = setupTarget(None, TestModels.summaryIndividualFlatWithoutAEA, TestModels.calcModelTwoRates)
      lazy val result = target.otherReliefs(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the other reliefs page" in {
        document.title() shouldBe messages.question
      }
    }

    "supplied with a pre-existing stored model" should {
      val testOtherReliefsModel = OtherReliefsModel(5000)
      val target = setupTarget(Some(testOtherReliefsModel), TestModels.summaryIndividualFlatWithoutAEA, TestModels.calcModelLoss)
      lazy val result = target.otherReliefs(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the other reliefs page" in {
        document.title() shouldBe messages.question
      }
    }

    "supplied without a valid session" should {
      val target = setupTarget(None, TestModels.summaryIndividualFlatWithoutAEA, TestModels.calcModelTwoRates)
      lazy val result = target.otherReliefs(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/session-timeout")
      }
    }
  }

  "Calling the .submitOtherReliefs action" when {

    "submitting a valid form" should {
      val target = setupTarget(None, TestModels.summaryIndividualFlatWithoutAEA, TestModels.calcModelTwoRates)
      lazy val request = fakeRequestToPOSTWithSession("otherReliefs" -> "1000")
      lazy val result = target.submitOtherReliefs(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary page" in {
        redirectLocation(result).get shouldBe controllers.nonresident.routes.SummaryController.summary().url
      }
    }

    "submitting an invalid form" should {
      val target = setupTarget(None, TestModels.summaryIndividualFlatWithoutAEA, TestModels.calcModelTwoRates)
      lazy val request = fakeRequestToPOSTWithSession("otherReliefs" -> "")
      lazy val result = target.submitOtherReliefs(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the other reliefs page" in {
        document.title() shouldBe messages.question
      }
    }
  }
}
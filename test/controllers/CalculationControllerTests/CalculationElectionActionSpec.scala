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

import common.{KeystoreKeys, TestModels}
import connectors.CalculatorConnector
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap
import constructors.nonresident.{AnswersConstructor, CalculationElectionConstructor}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup._
import org.scalatest.mock.MockitoSugar
import common.nonresident.CalculationType
import assets.MessageLookup.NonResident.{CalculationElection => messages}
import controllers.helpers.FakeRequestHelper

import scala.concurrent.Future
import controllers.nonresident.{CalculationElectionController, routes}
import models.nonresident._
import play.api.mvc.Result

class CalculationElectionActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[CalculationElectionModel],
                  postData: Option[CalculationElectionModel],
                  totalGainResultsModel: Option[TotalGainResultsModel],
                  contentElements: Seq[(String, String, String, Option[String])]
                 ): CalculationElectionController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockCalcElectionConstructor = mock[CalculationElectionConstructor]
    val mockCalcAnswersConstructor = mock[AnswersConstructor]

    when(mockCalcConnector.fetchAndGetFormData[CalculationElectionModel](Matchers.eq(KeystoreKeys.calculationElection))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcAnswersConstructor.getNRTotalGainAnswers(Matchers.any()))
      .thenReturn(Future.successful(TestModels.businessScenarioFiveModel))

    when(mockCalcConnector.calculateTotalGain(Matchers.any())(Matchers.any()))
      .thenReturn(totalGainResultsModel)

    when(mockCalcElectionConstructor.generateElection(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(contentElements))

    new CalculationElectionController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val calcElectionConstructor: CalculationElectionConstructor = mockCalcElectionConstructor
      override val calcAnswersConstructor: AnswersConstructor = mockCalcAnswersConstructor
    }
  }

  "CalculationElectionController" should {
    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      CalculationElectionController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }

  // GET Tests
  "In CalculationController calling the .calculationElection action" when {

    lazy val seq = Seq(("flat", "300", "A question", Some("Another bit of a question")))

    "supplied with no pre-existing session" should {

      lazy val target = setupTarget(
        None,
        None,
        Some(TotalGainResultsModel(0, Some(0), Some(0))),
        seq
      )
      lazy val result = target.calculationElection(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/session-timeout")
      }
    }

    "supplied with no pre-existing data" should {

      lazy val target = setupTarget(
        None,
        None,
        Some(TotalGainResultsModel(0, Some(0), Some(0))),
        seq
      )
      lazy val result = target.calculationElection(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "be on the calculation election page" in {
        document.title() shouldEqual messages.question
      }
    }

    "supplied with a pre-existing model" which {
      lazy val target = setupTarget(
        Some(CalculationElectionModel("flat")),
        None,
        Some(TotalGainResultsModel(0, Some(0), Some(0))),
        seq
      )
      lazy val result = target.calculationElection(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "be on the calculation election page" in {
        document.title() shouldEqual messages.question
      }
    }
  }

  "In CalculationController calling the .submitCalculationElection action" when {

    lazy val seq = Seq(("flat", "300", "A question", Some("Another bit of a question")))

    "submitting a valid calculation election" should {

      lazy val request = fakeRequestToPOSTWithSession(("calculationElection", "flat"))
      lazy val target = setupTarget(
        None,
        None,
        Some(TotalGainResultsModel(0, Some(0), Some(0))),
        seq
      )
      lazy val result = target.submitCalculationElection(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary page" in {
        redirectLocation(result) shouldBe Some(s"${routes.SummaryController.summary()}")
      }
    }

    "submitting an invalid calculation election" should {

      lazy val request = fakeRequestToPOSTWithSession(("calculationElection", "fehuifoh"))
      lazy val target = setupTarget(
        None,
        None,
        Some(TotalGainResultsModel(0, Some(0), Some(0))),
        seq
      )
      lazy val result = target.submitCalculationElection(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the calculation election page" in {
        document.title shouldEqual messages.question
      }
    }
  }

  "CalculationElectionController" should {
    "use the correct keystore connector" in {
      CalculationElectionController.calcConnector shouldBe CalculatorConnector
    }
  }
}

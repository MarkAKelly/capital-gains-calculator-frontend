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
import constructors.nonresident.CalculationElectionConstructor
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
import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{CalculationElection => messages}

import scala.concurrent.Future
import controllers.nonresident.{CalculationElectionController, routes}
import models.nonresident.{CalculationElectionModel, CalculationResultModel, OtherReliefsModel, SummaryModel}
import play.api.mvc.Result

class CalculationElectionActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[CalculationElectionModel],
                  postData: Option[CalculationElectionModel],
                  summaryData: SummaryModel,
                  calc: Option[CalculationResultModel] = None,
                  otherReliefsFlat: Option[OtherReliefsModel] = None,
                  otherReliefsTA: Option[OtherReliefsModel] = None,
                  otherReliefsRebased: Option[OtherReliefsModel] = None
                 ): CalculationElectionController = {
    val mockCalcConnector = mock[CalculatorConnector]
    val mockCalcElectionConstructor = mock[CalculationElectionConstructor]

    when(mockCalcConnector.createSummary(Matchers.any()))
      .thenReturn(summaryData)

    when(mockCalcElectionConstructor.generateElection(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(),
      Matchers.any()))
      .thenReturn(Seq(
        (s"${CalculationType.flat}", "8000.00", "flat calculation", None),
        (s"${CalculationType.timeApportioned}", "8000.00", "time apportioned calculation",
          Some(messages.taxStartDate)),
        (s"${CalculationType.rebased}", "10000.00", "rebased calculation",
          Some(messages.taxStartDate))
      ))
    when(mockCalcConnector.calculateFlat(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(calc))
    when(mockCalcConnector.calculateTA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(calc))
    when(mockCalcConnector.calculateRebased(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(calc))
    when(mockCalcConnector.fetchAndGetFormData[CalculationElectionModel](Matchers.eq(KeystoreKeys.calculationElection))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    lazy val data = CacheMap("form-id", Map("data" -> Json.toJson(postData.getOrElse(CalculationElectionModel("")))))
    when(mockCalcConnector.saveFormData[CalculationElectionModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))

    new CalculationElectionController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val calcElectionConstructor: CalculationElectionConstructor = mockCalcElectionConstructor
    }
  }

  "CalculationElectionController" should {
    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      CalculationElectionController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }

  // GET Tests
  "In CalculationController calling the .calculationElection action" when {

    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/non-resident/calculation-election").withSession(SessionKeys.sessionId -> "12345")

    "supplied with no pre-existing data" should {

      val target = setupTarget(None, None, TestModels.summaryTrusteeTAWithoutAEA)
      lazy val result = target.calculationElection(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "be on the calculation election page" in {
        document.title() shouldEqual messages.heading
      }
    }

    "supplied with no pre-existing data and no acquisition date" should {

      val target = setupTarget(None, None, TestModels.summaryIndividualFlatWithAEA)
      lazy val result = target.calculationElection(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "be on the calculation election page" in {
        document.title() shouldEqual messages.heading
      }
    }

    "supplied with pre-existing data and a value for flat, time and rebased reliefs" should {

      val target = setupTarget(
        Some(CalculationElectionModel("flat")),
        None,
        TestModels.summaryTrusteeTAWithoutAEA,
        None,
        Some(OtherReliefsModel(None, Some(500))),
        Some(OtherReliefsModel(None, Some(600))),
        Some(OtherReliefsModel(None, Some(700)))
      )
      lazy val result = target.calculationElection(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "be on the calculation election page" in {
        document.title() shouldEqual messages.heading
      }
    }

    "supplied with pre-existing data and no values for flat, time and rebased reliefs" should {

      val target = setupTarget(
        Some(CalculationElectionModel("flat")),
        None,
        TestModels.summaryTrusteeTAWithoutAEA
      )
      lazy val result = target.calculationElection(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "be on the calculation election page" in {
        document.title() shouldEqual messages.heading
      }
    }
  }

  "In CalculationController calling the .submitCalculationElection action" when {

    def buildRequest(body: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST",
      "/calculate-your-capital-gains/non-resident/calculation-election")
      .withSession(SessionKeys.sessionId -> "12345")
      .withFormUrlEncodedBody(body: _*)

    def executeTargetWithMockData
    (
      data: String,
      calc: Option[CalculationResultModel],
      summary: SummaryModel,
      action: String
    ): Future[Result] = {
      lazy val fakeRequest = buildRequest(("calculationElection", data), ("action",action))
      val mockData = new CalculationElectionModel(data)
      val target = setupTarget(None, Some(mockData), summary, calc)
      target.submitCalculationElection(fakeRequest)
    }

    "submitting form via Other Reliefs Flat button" should {

      lazy val result = executeTargetWithMockData(CalculationType.flat, Some(TestModels.calcModelOneRate),
        TestModels.summaryTrusteeTAWithoutAEA, CalculationType.flat)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the other reliefs page" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherReliefsFlatController.otherReliefsFlat()}")
      }
    }

    "submitting form via Other Reliefs Time Apportioned button" should {

      lazy val result = executeTargetWithMockData(CalculationType.flat, Some(TestModels.calcModelOneRate),
        TestModels.summaryTrusteeTAWithoutAEA, CalculationType.timeApportioned)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Other Reliefs Time Apportioned page" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherReliefsTAController.otherReliefsTA()}")
      }
    }

    "submitting form via Other Reliefs Rebased button" should {

      lazy val result = executeTargetWithMockData(CalculationType.flat, Some(TestModels.calcModelOneRate),
        TestModels.summaryTrusteeTAWithoutAEA, CalculationType.rebased)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Other Reliefs Rebased page" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherReliefsRebasedController.otherReliefsRebased()}")
      }
    }

    "submitting a valid form with 'flat' selected" should {

      lazy val result = executeTargetWithMockData(CalculationType.flat, Some(TestModels.calcModelOneRate), TestModels.summaryTrusteeTAWithoutAEA, "continue")

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary page" in {
        redirectLocation(result) shouldBe Some(s"${routes.SummaryController.summary()}")
      }
    }

    "submitting a valid form with 'time' selected" should {

      lazy val result = executeTargetWithMockData(CalculationType.timeApportioned, Some(TestModels.calcModelOneRate),
        TestModels.summaryIndividualAcqDateAfter, "continue")

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary page" in {
        redirectLocation(result) shouldBe Some(s"${routes.SummaryController.summary()}")
      }
    }

    "submitting a valid form with 'rebased' selected" should {

      lazy val result = executeTargetWithMockData(CalculationType.rebased, Some(TestModels.calcModelOneRate),
        TestModels.summaryIndividualFlatWithAEA, "continue")

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary page" in {
          redirectLocation(result) shouldBe Some(s"${routes.SummaryController.summary()}")
      }
    }

    "submitting a form with no data" should  {

      lazy val result = executeTargetWithMockData("", Some(TestModels.calcModelOneRate), TestModels.summaryIndividualImprovementsWithRebasedModel, "continue")
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "should return to the the calculation election page" in {
        document.title shouldBe messages.question
      }


    }

    "submitting a form with completely unrelated 'ew1234qwer'" should  {

      lazy val result = executeTargetWithMockData("ew1234qwer", Some(TestModels.calcModelOneRate),
        TestModels.summaryIndividualImprovementsNoRebasedModel, "continue")
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "should return to the the calculation election page" in {
        document.title shouldBe messages.question
      }
    }
  }

  "CalculationElectionController" should {
    "use the correct keystore connector" in {
      CalculationElectionController.calcConnector shouldBe CalculatorConnector
    }
  }
}

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
import common.{KeystoreKeys, TestModels}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.SummaryController
import models.nonresident.{AcquisitionDateModel, CalculationResultModel, RebasedValueModel, SummaryModel}
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

  def setupTarget(
                   summary: SummaryModel,
                   result: CalculationResultModel,
                   acquisitionDateData: Option[AcquisitionDateModel],
                   rebasedValueData: Option[RebasedValueModel]
                 ): SummaryController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[RebasedValueModel](Matchers.eq(KeystoreKeys.rebasedValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(rebasedValueData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(acquisitionDateData))

    when(mockCalcConnector.createSummary(Matchers.any()))
      .thenReturn(Future.successful(summary))

    when(mockCalcConnector.calculateFlat(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(result)))

    when(mockCalcConnector.calculateTA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(result)))

    when(mockCalcConnector.calculateRebased(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(result)))

    new SummaryController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling the .summaryBackUrl" when {

    "provided with an acquisition date" should {

      "return a route to other reliefs when date is after start date" in {
        val acquisitionDate = AcquisitionDateModel("Yes", Some(10), Some(5), Some(2017))
        val target = setupTarget(TestModels.sumModelTA, TestModels.calcModelOneRate, Some(acquisitionDate), None)
        val result = target.summaryBackUrl

        await(result) shouldBe controllers.nonresident.routes.OtherReliefsController.otherReliefs().url
      }

      "return a route to the calculation election page when date is before start date" in {
        val acquisitionDate = AcquisitionDateModel("Yes", Some(10), Some(5), Some(2013))
        val target = setupTarget(TestModels.sumModelTA, TestModels.calcModelOneRate, Some(acquisitionDate), None)
        val result = target.summaryBackUrl

        await(result) shouldBe controllers.nonresident.routes.CalculationElectionController.calculationElection().url
      }
    }

    "provided with no acquisition date" should {
      val acquisitionDate = AcquisitionDateModel("No", None, None, None)

      "return a route to the calculation election page when a rebased value is provided" in {
        val rebasedValue = RebasedValueModel("Yes", Some(100))
        val target = setupTarget(TestModels.sumModelTA, TestModels.calcModelOneRate, Some(acquisitionDate), Some(rebasedValue))
        val result = target.summaryBackUrl

        await(result) shouldBe controllers.nonresident.routes.CalculationElectionController.calculationElection().url
      }

      "return a route with to the other reliefs page when no rebased value is provided" in {
        val rebasedValue = RebasedValueModel("No", None)
        val target = setupTarget(TestModels.sumModelTA, TestModels.calcModelOneRate, Some(acquisitionDate), Some(rebasedValue))
        val result = target.summaryBackUrl

        await(result) shouldBe controllers.nonresident.routes.OtherReliefsController.otherReliefs().url
      }

      "return a missing route when no rebased value is found" in {
        val target = setupTarget(TestModels.sumModelTA, TestModels.calcModelOneRate, Some(acquisitionDate), None)
        val result = target.summaryBackUrl

        await(result) shouldBe missingDataRoute
      }
    }

    "no acquisition date is found" should {
      val target = setupTarget(TestModels.sumModelTA, TestModels.calcModelOneRate, None, None)
      val result = target.summaryBackUrl

      await(result) shouldBe missingDataRoute
    }
  }

  "Calling the .summary action" when {

    "provided with a valid session" should {
      val target = setupTarget(
        TestModels.summaryIndividualFlatWithAEA,
        TestModels.calcModelTwoRates,
        Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
        None
      )
      lazy val result = target.summary()(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the summary page" in {
        document.title() shouldBe messages.title
      }
    }

    "provided with an invalid session" should {
      val target = setupTarget(
        TestModels.summaryIndividualFlatWithAEA,
        TestModels.calcModelTwoRates,
        Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
        None
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
      TestModels.summaryIndividualFlatWithAEA,
      TestModels.calcModelTwoRates,
      Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
      None
    )
    lazy val result = target.restart()(fakeRequestWithSession)

    "return a 303" in {
      status(result) shouldBe 303
    }

    "redirect to the start page" in {
      redirectLocation(result).get shouldBe controllers.nonresident.routes.CustomerTypeController.customerType().url
    }
  }
}
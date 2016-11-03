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

import common.DefaultRoutes._
import common.{KeystoreKeys, TestModels}
import connectors.CalculatorConnector
import controllers.nonresident.{SummaryController, routes}
import models.nonresident.{AcquisitionDateModel, CalculationResultModel, RebasedValueModel, SummaryModel}
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{Summary => messages}

import scala.concurrent.Future

class SummarySpec extends UnitSpec with WithFakeApplication with MockitoSugar {

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

  "In CalculationController calling the .summary action" when {
    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/non-resident/summary").withSession(SessionKeys.sessionId -> "12345")

    "Testing the back links for all user types" when {

      "Acquisition Date is > 5 April 2015" should {
        val target = setupTarget(
          TestModels.summaryIndividualFlatWithAEA,
          TestModels.calcModelTwoRates,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to ${routes.OtherReliefsController.otherReliefs().url}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
          document.body.getElementById("back-link").attr("href") shouldEqual routes.OtherReliefsController.otherReliefs().url
        }
      }

      "Acquisition Date is not supplied and no rebased value has been supplied" should {
        val target = setupTarget(
          TestModels.summaryIndividualFlatWithAEA,
          TestModels.calcModelTwoRates,
          Some(AcquisitionDateModel("No", None, None, None)),
          Some(RebasedValueModel("No", None))
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to ${routes.OtherReliefsController.otherReliefs().url}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
          document.body.getElementById("back-link").attr("href") shouldEqual routes.OtherReliefsController.otherReliefs().url
        }
      }

      "Acquisition Date is not supplied and rebased value is supplied" should {
        val target = setupTarget(
          TestModels.summaryIndividualFlatWithAEA,
          TestModels.calcModelTwoRates,
          Some(AcquisitionDateModel("No", None, None, None)),
          Some(RebasedValueModel("Yes", Some(500)))
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to ${routes.CalculationElectionController.calculationElection().url}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
          document.body.getElementById("back-link").attr("href") shouldEqual routes.CalculationElectionController.calculationElection().url
        }
      }

      "Acquisition Date <= 5 April 2015" should {
        val target = setupTarget(
          TestModels.summaryIndividualFlatWithAEA,
          TestModels.calcModelTwoRates,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2014))),
          Some(RebasedValueModel("Yes", Some(500)))
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to ${routes.CalculationElectionController.calculationElection().url}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
          document.body.getElementById("back-link").attr("href") shouldEqual routes.CalculationElectionController.calculationElection().url
        }
      }

      "Acquisition Date Model is not supplied" should {
        val target = setupTarget(
          TestModels.summaryIndividualFlatWithAEA,
          TestModels.calcModelTwoRates,
          None,
          Some(RebasedValueModel("Yes", Some(500)))
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to $missingDataRoute " in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
          document.body.getElementById("back-link").attr("href") shouldEqual missingDataRoute
        }
      }

      "Acquisition Date Model is supplied with no date but Rebased Value Model is not" should {
        val target = setupTarget(
          TestModels.summaryIndividualFlatWithAEA,
          TestModels.calcModelTwoRates,
          Some(AcquisitionDateModel("No", None, None, None)),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to $missingDataRoute " in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
          document.body.getElementById("back-link").attr("href") shouldEqual missingDataRoute
        }
      }
    }
  }

  "calling the .restart action" should {
    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/restart").withSession(SessionKeys.sessionId -> "12345")
    val target = setupTarget(
      TestModels.summaryIndividualFlatWithAEA,
      TestModels.calcModelTwoRates,
      Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
      None
    )
    lazy val result = target.restart()(fakeRequest)

    "return a 303" in {
      status(result) shouldBe 303
    }
  }

}

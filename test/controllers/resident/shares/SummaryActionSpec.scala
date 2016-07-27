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

package controllers.resident.shares

import assets.MessageLookup.{summary => messages}
import common.Dates
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import models.resident._
import models.resident.shares.GainAnswersModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future


class SummaryActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget
  (
    gainAnswersModel: GainAnswersModel,
    grossGain: BigDecimal,
    taxYearModel: Option[TaxYearModel]
    ): SummaryController = {

    lazy val mockCalculatorConnector = mock[CalculatorConnector]

    when(mockCalculatorConnector.getShareGainAnswers(Matchers.any()))
      .thenReturn(Future.successful(gainAnswersModel))

    when(mockCalculatorConnector.calculateRttShareGrossGain(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(grossGain))

    when(mockCalculatorConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    new SummaryController {
      override val calculatorConnector: CalculatorConnector = mockCalculatorConnector
    }
  }

  "Calling .summary from the SummaryController" when {

    "a negative gross gain is returned" should {
      lazy val yourAnswersSummaryModel = GainAnswersModel(Dates.constructDate(12, 1, 2016),
        3000,
        10,
        5000,
        5)
      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        -6000,
        taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
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
    }
  }

  "Calling .summary from the SummaryController with no session" should {

    lazy val result = SummaryController.summary(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout view" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/session-timeout")
    }
  }
}
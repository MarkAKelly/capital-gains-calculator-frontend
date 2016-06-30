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

package controllers.resident

import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.test.Helpers._
import assets.MessageLookup.{summary => messages}
import common.KeystoreKeys.ResidentKeys
import connectors.CalculatorConnector
import models.resident._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future


class SummaryActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget
  (
    disposalDateData: Option[DisposalDateModel],
    disposalValueData: Option[DisposalValueModel],
    disposalCostsData: Option[DisposalCostsModel],
    acquisitionValueData: Option[AcquisitionValueModel],
    acquisitionCostsData: Option[AcquisitionCostsModel],
    grossGain: BigDecimal
  ): SummaryController = {

    val mockCalculatorConnector = mock[CalculatorConnector]

    when(mockCalculatorConnector.fetchAndGetFormData[DisposalDateModel](Matchers.eq(ResidentKeys.disposalDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(disposalDateData))

    when(mockCalculatorConnector.fetchAndGetFormData[DisposalValueModel](Matchers.eq(ResidentKeys.disposalValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(disposalValueData))

    when(mockCalculatorConnector.fetchAndGetFormData[DisposalCostsModel](Matchers.eq(ResidentKeys.disposalCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(disposalCostsData))

    when(mockCalculatorConnector.fetchAndGetFormData[AcquisitionValueModel](Matchers.eq(ResidentKeys.acquisitionValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(acquisitionValueData))

    when(mockCalculatorConnector.fetchAndGetFormData[AcquisitionCostsModel](Matchers.eq(ResidentKeys.acquisitionCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(acquisitionCostsData))

    when(mockCalculatorConnector.calculateRttGrossGain(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(grossGain))

    new SummaryController {
      override val calculatorConnector: CalculatorConnector = mockCalculatorConnector
    }
  }

  "Calling .summary from the SummaryController" should {

    val target = setupTarget(
      Some(DisposalDateModel(12,1,2016)),
      Some(DisposalValueModel(3000)),
      Some(DisposalCostsModel(10)),
      Some(AcquisitionValueModel(5000)),
      Some(AcquisitionCostsModel(5)),
      -6000
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

  "Calling .summary from the SummaryController with no session" should {

    lazy val result = SummaryController.summary(fakeRequest)
    lazy val doc = Jsoup.parse(bodyOf(result))

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout view" in {
      redirectLocation(result).get shouldBe "/calculate-your-capital-gains/non-resident/session-timeout"
    }
  }
}

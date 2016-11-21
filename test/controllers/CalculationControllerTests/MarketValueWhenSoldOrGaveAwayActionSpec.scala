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

import assets.MessageLookup.NonResident.{MarketValue => marketValueMessages}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.{DisposalValueController, MarketValueWhenSoldOrGaveAwayController}
import models.nonresident.DisposalValueModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

/**
  * Created by emma on 21/11/16.
  */
class MarketValueWhenSoldOrGaveAwayActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[DisposalValueModel]): MarketValueWhenSoldOrGaveAwayController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[DisposalValueModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    new MarketValueWhenSoldOrGaveAwayController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "MarketValueController" should {
    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      DisposalValueController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }

  "The marketValueWhenGaveAway action" when {
    "not supplied with a pre-existing stored model" should {
      val target = setupTarget(None)
      lazy val result = target.marketValueWhenGaveAway(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the market value page" in {
        document.title shouldBe marketValueMessages.disposalGaveAwayQuestion
      }
    }

    "supplied with a pre-existing stored model" should {
      val target = setupTarget(Some(DisposalValueModel(1000)))
      lazy val result = target.marketValueWhenGaveAway(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the market value page" in {
        document.title shouldBe marketValueMessages.disposalGaveAwayQuestion
      }
    }

    "without a valid session" should {
      val target = setupTarget(None)
      lazy val result = target.marketValueWhenGaveAway(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/session-timeout")
      }
    }
  }

  "The marketValueWhenSold action" when {
    "not supplied with a pre-existing stored model" should {
      val target = setupTarget(None)
      lazy val result = target.marketValueWhenSold(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the market value page" in {
        document.title shouldBe marketValueMessages.disposalSoldQuestion
      }
    }

    "supplied with a pre-existing stored model" should {
      val target = setupTarget(Some(DisposalValueModel(1000)))
      lazy val result = target.marketValueWhenSold(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the market value page" in {
        document.title shouldBe marketValueMessages.disposalSoldQuestion
      }
    }

    "without a valid session" should {
      val target = setupTarget(None)
      lazy val result = target.marketValueWhenSold(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/session-timeout")
      }
    }
  }

  "The submitMarketValueWhenSold action" when {
    "submitting a valid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("disposalValue", "1000"))
      lazy val result = target.submitMarketValueWhenSold(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the AcquisitionCosts page" in {
        redirectLocation(result) shouldBe Some(controllers.nonresident.routes.AcquisitionCostsController.acquisitionCosts().url)
      }
    }
  }

  "The submitMarketValueWhenGaveAway action" when {
    "submitting a valid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("disposalValue", "1000"))
      lazy val result = target.submitMarketValueWhenGaveAway(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the AcquisitionCosts page" in {
        redirectLocation(result) shouldBe Some(controllers.nonresident.routes.AcquisitionCostsController.acquisitionCosts().url)
      }
    }
  }
}


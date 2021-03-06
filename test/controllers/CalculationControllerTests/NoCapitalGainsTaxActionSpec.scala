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

import assets.MessageLookup.NonResident.{NoCapitalGainsTax => messages}
import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.NoCapitalGainsTaxController
import models.nonresident.DisposalDateModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class NoCapitalGainsTaxActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[DisposalDateModel]): NoCapitalGainsTaxController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](Matchers.eq(KeystoreKeys.disposalDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    new NoCapitalGainsTaxController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "NoCapitalGainsTaxController" should {
    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      NoCapitalGainsTaxController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }

  //GET Tests
  "In CalculationController calling the .noCapitalGainsTax action " when {

    "called with a valid session" should {
      val target = setupTarget(Some(DisposalDateModel(1, 1, 2015)))
      lazy val result = target.noCapitalGainsTax(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the No Capital Gains Tax page" in {
        document.title() shouldBe messages.title
      }
    }

    "called with an invalid session" should {
      val target = setupTarget(Some(DisposalDateModel(2, 4, 2013)))
      lazy val result = target.noCapitalGainsTax(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/session-timeout")
      }
    }
  }
}


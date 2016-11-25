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

import assets.MessageLookup.NonResident.{DisposalCosts => messages}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.{DisposalCostsController, routes}
import models.nonresident.DisposalCostsModel
import org.jsoup._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class DisposalCostsActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[DisposalCostsModel]): DisposalCostsController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[DisposalCostsModel](Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    new DisposalCostsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "DisposalCostsController" should {
    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      DisposalCostsController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }

  //GET Tests
  "In CalculationController calling the .disposalCosts action " should {

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None)
      lazy val result = target.disposalCosts(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the title ${messages.question}" in {
        document.getElementsByTag("title").text shouldBe messages.question
      }
    }

    "supplied with a pre-existing stored model" should {

      val target = setupTarget(Some(DisposalCostsModel(1000)))
      lazy val result = target.disposalCosts(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have the title ${messages.question}" in {
        document.getElementsByTag("title").text shouldBe messages.question
      }
    }

    "supplied with an invalid session" should {
      val target = setupTarget(Some(DisposalCostsModel(1000)))
      lazy val result = target.disposalCosts(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/session-timeout")
      }
    }
  }

  //POST Tests
  "In CalculationController calling the .submitDisposalCosts action" when {

    "submitting a valid form with 1000" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("disposalCosts", "1000"))
      lazy val result = target.submitDisposalCosts(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.AcquisitionDateController.acquisitionDate()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.AcquisitionDateController.acquisitionDate()}")
      }
    }

    "submitting an invalid form with no value" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("disposalCosts", ""))
      lazy val result = target.submitDisposalCosts(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the disposal costs page" in {
        document.title shouldEqual messages.question
      }
    }
  }
}

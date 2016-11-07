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

import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import controllers.nonresident.AcquisitionCostsController
import models.nonresident.AcquisitionCostsModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.NonResident.{AcquisitionCosts => messages}
import controllers.helpers.FakeRequestHelper

import scala.concurrent.Future

class AcquisitionCostsSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()
  def setupTarget(getData: Option[AcquisitionCostsModel]): AcquisitionCostsController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockCalcElectionConstructor = mock[CalculationElectionConstructor]

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionCostsModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    new AcquisitionCostsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val calcElectionConstructor: CalculationElectionConstructor = mockCalcElectionConstructor
    }
  }

  "In CalculationController calling the .acquisitionCosts action " should {

    "not supplied with a pre-existing stored model" should {
      val target = setupTarget(None)
      lazy val result = target.acquisitionCosts(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the acquisitionCosts page" in {
        document.title shouldBe messages.question
      }
    }

    "supplied with a pre-existing stored model" should {
      val testAcquisitionCostsModel = new AcquisitionCostsModel(1000)
      val target = setupTarget(Some(testAcquisitionCostsModel))
      lazy val result = target.acquisitionCosts(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the acquisitionCosts page" in {
        document.title shouldBe messages.question
      }
    }

    "without a valid session" should {
      val target = setupTarget(None)
      lazy val result = target.acquisitionCosts(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/session-timeout")
      }
    }
  }

  "Calling the .submitAcquisitionCosts action" when {

    "supplied with a valid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionCosts", "1000"))
      lazy val result = target.submitAcquisitionCosts(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to '${controllers.nonresident.routes.DisposalCostsController.disposalCosts().url}'" in {
        redirectLocation(result).get shouldBe controllers.nonresident.routes.DisposalCostsController.disposalCosts().url
      }
    }

    "supplied with an invalid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionCosts", "a"))
      lazy val result = target.submitAcquisitionCosts(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the acquisition costs page" in {
        document.title shouldBe messages.question
      }
    }
  }
}

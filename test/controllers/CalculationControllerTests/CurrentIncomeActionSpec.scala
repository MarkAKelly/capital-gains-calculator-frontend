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

import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{AcquisitionCosts, CurrentIncome => messages}
import common.Constants
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.{CurrentIncomeController, routes}
import models.nonresident.CurrentIncomeModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.play.views.helpers.MoneyPounds

import scala.concurrent.Future

class CurrentIncomeActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()
  def setupTarget(getData: Option[CurrentIncomeModel]): CurrentIncomeController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[CurrentIncomeModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[CurrentIncomeModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(mock[CacheMap])

    new CurrentIncomeController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "CurrentIncomeController" should {
    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      CurrentIncomeController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }

  //GET Tests
  "Calling the .currentIncome action " when {

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None)
      lazy val result = target.currentIncome(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the current income page" in {
        document.title shouldBe messages.question
      }
    }

    "supplied with a pre-existing stored model" should {

      val target = setupTarget(Some(CurrentIncomeModel(1000)))
      lazy val result = target.currentIncome(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "load the current income page" in {
        document.title shouldBe messages.question
      }
    }

    "without a valid session" should {
      val target = setupTarget(None)
      lazy val result = target.currentIncome(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/session-timeout")
      }
    }
  }

  //POST Tests
  "In CalculationController calling the .submitCurrentIncome action " when {

    "submitting a valid form" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("currentIncome", "1000"))
      lazy val result = target.submitCurrentIncome(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.PersonalAllowanceController.personalAllowance()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.PersonalAllowanceController.personalAllowance()}")
      }
    }

    "submitting a valid form with a £0 amount" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("currentIncome", "0"))
      lazy val result = target.submitCurrentIncome(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.OtherPropertiesController.otherProperties()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherPropertiesController.otherProperties()}")
      }
    }

    "submitting an invalid form with negative data" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("currentIncome", "-10"))
      lazy val result = target.submitCurrentIncome(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the current income page" in {
        document.title shouldBe messages.question
      }
    }
  }
}

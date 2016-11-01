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

import common.{Constants, KeystoreKeys}
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
import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{AcquisitionValue => messages}
import config.AppConfig
import controllers.helpers.FakeRequestHelper

import scala.concurrent.Future
import controllers.nonresident.{AcquisitionValueController, routes}
import models.nonresident.{AcquisitionDateModel, AcquisitionValueModel}
import play.api.mvc.Result
import uk.gov.hmrc.play.views.helpers.MoneyPounds

class AcquisitionValueActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(
                   getData: Option[AcquisitionValueModel],
                   acquisitionDateModel: Option[AcquisitionDateModel] = None): AcquisitionValueController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockCalcElectionConstructor = mock[CalculationElectionConstructor]

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionValueModel](Matchers.eq(KeystoreKeys.acquisitionValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(acquisitionDateModel))

    new AcquisitionValueController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val calcElectionConstructor: CalculationElectionConstructor = mockCalcElectionConstructor
    }
  }

  // GET Tests
  "Calling the CalculationController.acquisitionValue" when {

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None, None)
      lazy val result = target.acquisitionValue(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }
    }

    "supplied with a pre-existing stored model" should {

      val target = setupTarget(Some(AcquisitionValueModel(1000)), None)
      lazy val result = target.acquisitionValue(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }
    }
  }

  // POST Tests
  "In CalculationController calling the .submitAcquisitionValue action" when {

    "submit a valid form with a acquisition date after tax start" should {
      val acquisitionDateModelYesAfterStartDate = new AcquisitionDateModel("Yes", Some(10), Some(10), Some(2017))
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionValue", "1000"))
      lazy val target = setupTarget(None, Some(acquisitionDateModelYesAfterStartDate))
      lazy val result = target.submitAcquisitionValue(request)

      s"return a 303 to ${routes.ImprovementsController.improvements()}" in {
        status(result) shouldBe 303
      }

      "redirect to the improvements page" in {
        redirectLocation(result) shouldBe Some(s"${routes.ImprovementsController.improvements()}")
      }
    }

    "submitting a valid form with a date before 5 5 2015" should {

      val date = new AcquisitionDateModel("Yes", Some(10), Some(10), Some(2010))
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionValue", "1000"))
      lazy val target = setupTarget(None, Some(date))
      lazy val result = target.submitAcquisitionValue(request)

      "return a 303" in {
        status(result) shouldBe 303
      }
      "redirect to the rebased value page" in {
        redirectLocation(result) shouldBe Some(s"${routes.RebasedValueController.rebasedValue()}")
      }
    }

    "submitting a valid form with No date supplied" should {

      val noDate = new AcquisitionDateModel("No", None, None, None)
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionValue", "1000"))
      lazy val target = setupTarget(None, Some(noDate))
      lazy val result = target.submitAcquisitionValue(request)

      s"return a 303 to ${routes.RebasedValueController.rebasedValue()}" in {
        status(result) shouldBe 303
      }

      "redirect to rebased value" in {
        redirectLocation(result) shouldBe Some(s"${routes.RebasedValueController.rebasedValue()}")
      }
    }

    "submitting an invalid form with a negative value" should {
      lazy val request = fakeRequestToPOSTWithSession(("acquisitionValue", "-1000"))
      lazy val target = setupTarget(None, None)
      lazy val result = target.submitAcquisitionValue(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"fail with message ${messages.errorNegative}" in {
        document.getElementsByClass("error-notification").text should include (messages.errorNegative)
      }
    }

    "submitting an invalid form with value 1.111" should {

      lazy val request = fakeRequestToPOSTWithSession(("acquisitionValue", "1.111"))
      lazy val target = setupTarget(None, None)
      lazy val result = target.submitAcquisitionValue(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"fail with message ${messages.errorDecimalPlaces}" in {
        document.getElementsByClass("error-notification").text should include (messages.errorDecimalPlaces)
      }
    }

    "submitting a value which exceeds the maximum numeric" should {

      lazy val request = fakeRequestToPOSTWithSession(("acquisitionValue", "1000000000.1"))
      lazy val target = setupTarget(None, None)
      lazy val result = target.submitAcquisitionValue(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"fail with message ${messages.errorMaximum(MoneyPounds(Constants.maxNumeric, 0).quantity)}" in {
        document.getElementsByClass("error-notification").text should
          include (messages.errorMaximum(MoneyPounds(Constants.maxNumeric, 0).quantity))
      }
    }
  }
}

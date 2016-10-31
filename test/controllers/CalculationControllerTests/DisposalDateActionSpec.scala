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

import assets.MessageLookup.NonResident.{DisposalDate => messages}
import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup._
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future
import controllers.nonresident.{DisposalDateController, routes}
import models.nonresident.{AcquisitionDateModel, DisposalDateModel}
import play.api.mvc.Result

class DisposalDateActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[DisposalDateModel],
                   acquisitionData: Option[AcquisitionDateModel] = None
                 ): DisposalDateController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(acquisitionData))

    new DisposalDateController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  // GET Tests
  "Calling the CalculationController.disposalDate" when {

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None)
      lazy val result = target.disposalDate(fakeRequestWithSession)

      "return a 200" in {
        status(result) shouldBe 200
      }
    }
  }

  // POST Tests
  "In CalculationController calling the .submitDisposalDate action" when {

    "submitting a valid date 31/01/2016" should {

      lazy val target = setupTarget(None, Some(AcquisitionDateModel("Yes", Some(1), Some(4), Some(2010))))
      lazy val request = fakeRequestToPOSTWithSession(("disposalDateDay", "31"), ("disposalDateMonth", "1"), ("disposalDateYear", "2016"))
      lazy val result = target.submitDisposalDate(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.DisposalValueController.disposalValue()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.DisposalValueController.disposalValue()}")
      }
    }

    "submitting a valid leap year date 29/02/2016" should {

      lazy val target = setupTarget(None, Some(AcquisitionDateModel("Yes", Some(1), Some(4), Some(2010))))
      lazy val request = fakeRequestToPOSTWithSession(("disposalDateDay", "29"), ("disposalDateMonth", "2"), ("disposalDateYear", "2016"))
      lazy val result = target.submitDisposalDate(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.DisposalValueController.disposalValue()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.DisposalValueController.disposalValue()}")
      }
    }

    "submitting a disposal date of 22/02/1990 which is before acquisition date 22/01/2000" should {

      lazy val target = setupTarget(None, Some(AcquisitionDateModel("Yes", Some(22), Some(1), Some(2000))))
      lazy val request = fakeRequestToPOSTWithSession(("disposalDateDay", "22"), ("disposalDateMonth", "2"), ("disposalDateYear", "1990"))
      lazy val result = target.submitDisposalDate(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "should return to the disposal date page" in {
        document.title shouldEqual messages.question
      }
    }
  }
}

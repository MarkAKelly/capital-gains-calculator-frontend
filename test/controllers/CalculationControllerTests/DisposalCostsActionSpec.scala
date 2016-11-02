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
import assets.MessageLookup.{NonResident => commonMessages}
import common.{Constants, KeystoreKeys}
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
import controllers.nonresident.{DisposalCostsController, routes}
import models.nonresident.{AcquisitionDateModel, DisposalCostsModel, RebasedValueModel}
import play.api.mvc.Result
import uk.gov.hmrc.play.views.helpers.MoneyPounds

class DisposalCostsActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[DisposalCostsModel],
                  acquisitionDate: Option[AcquisitionDateModel],
                  rebasedData: Option[RebasedValueModel] = None): DisposalCostsController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[DisposalCostsModel](Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(acquisitionDate))

    when(mockCalcConnector.fetchAndGetFormData[RebasedValueModel](Matchers.eq(KeystoreKeys.rebasedValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(rebasedData))

    new DisposalCostsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  //GET Tests
  "In CalculationController calling the .disposalCosts action " should {

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None, None, None)
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

      val target = setupTarget(Some(DisposalCostsModel(1000)), None, None)
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
      val target = setupTarget(Some(DisposalCostsModel(1000)), None, None)
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

    "submitting a valid form when any acquisition date has been supplied but no property was revalued" should {
      val target = setupTarget(None, Some(AcquisitionDateModel("Yes", Some(12), Some(3), Some(2016))))
      lazy val request = fakeRequestToPOSTWithSession(("disposalCosts", "1000"))
      lazy val result = target.submitDisposalCosts(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.PrivateResidenceReliefController.privateResidenceRelief()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.PrivateResidenceReliefController.privateResidenceRelief()}")
      }
    }

    "submitting a valid form when no acquisition date has been supplied but a property was revalued" should {
      val target = setupTarget(None, Some(AcquisitionDateModel("No", None, None, None)), Some(RebasedValueModel("Yes", Some(BigDecimal(1000)))))
      lazy val request = fakeRequestToPOSTWithSession(("disposalCosts", "1000"))
      lazy val result = target.submitDisposalCosts(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.PrivateResidenceReliefController.privateResidenceRelief()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.PrivateResidenceReliefController.privateResidenceRelief()}")
      }
    }

    "submitting a valid form when no acquisition date has been supplied and no property was revalued" should {
      val target = setupTarget(None, Some(AcquisitionDateModel("No", None, None, None)))
      lazy val request = fakeRequestToPOSTWithSession(("disposalCosts", "1000"))
      lazy val result = target.submitDisposalCosts(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.AllowableLossesController.allowableLosses()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.AllowableLossesController.allowableLosses()}")
      }
    }

    "submitting a valid form when an invalid Acquisition Date Model has been supplied and no property was revalued" should {
      val target = setupTarget(None, Some(AcquisitionDateModel("invalid", None, None, None)))
      lazy val request = fakeRequestToPOSTWithSession(("disposalCosts", "1000"))
      lazy val result = target.submitDisposalCosts(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.AllowableLossesController.allowableLosses()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.AllowableLossesController.allowableLosses()}")
      }
    }

    "submitting an invalid form with no value" should {
      val target = setupTarget(None, Some(AcquisitionDateModel("No", None, None, None)))
      lazy val request = fakeRequestToPOSTWithSession(("disposalCosts", ""))
      lazy val result = target.submitDisposalCosts(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"display the error message '${commonMessages.errorRealNumber}'" in {
        document.select("div label span.error-notification").text shouldEqual commonMessages.errorRealNumber
      }
    }
  }
}

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

package controllers.resident.DeductionsControllerTests
import assets.MessageLookup.{annualExemptAmount => messages}
import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.DeductionsController
import models.resident.AnnualExemptAmountModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class AnnualExemptAmountActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar{
  def setupTarget(getData: Option[AnnualExemptAmountModel]): DeductionsController = {
    val mockCalcConnector = mock[CalculatorConnector]
    when(mockCalcConnector.fetchAndGetFormData[AnnualExemptAmountModel](Matchers.eq(KeystoreKeys.ResidentKeys.annualExemptAmount))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))
    new DeductionsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }
  "Calling .annualExemptAmount from the DeductionsController" when {
    "there is no keystore data" should {
      lazy val target = setupTarget(None)
      lazy val result = target.annualExemptAmount(fakeRequestWithSession)
      "return a status of 200" in {
        status(result) shouldBe 200
      }
      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }
      "display the Annual Exempt Amount view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }
    "there is some keystore data" should {
      lazy val target = setupTarget(Some(AnnualExemptAmountModel(1000)))
      lazy val result = target.annualExemptAmount(fakeRequestWithSession)
      "return a status of 200" in {
        status(result) shouldBe 200
      }
      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }
      "display the Annual Exempt Amount view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }
  }
  "request has an invalid session" should {
    lazy val result = DeductionsController.annualExemptAmount(fakeRequest)
    "return a status of 303" in {
      status(result) shouldBe 303
    }
    "return you to the session timeout page" in {
      redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/session-timeout")
    }
  }
  "Calling .submitAnnualExemptAmount from the DeductionsController" when {
    "a valid form is submitted" should {
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
      lazy val result = DeductionsController.submitAnnualExemptAmount(request)
      "return a 303" in {
        status(result) shouldBe 303
      }
      "redirect to the summary page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/summary")
      }
    }
    "an invalid form is submitted" should {
      lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
      lazy val result = DeductionsController.submitAnnualExemptAmount(request)
      lazy val doc = Jsoup.parse(bodyOf(result))
      "return a 400" in {
        status(result) shouldBe 400
      }
      "render the annual exempt amount page" in {
        doc.title() shouldEqual messages.title
      }
    }
  }
}

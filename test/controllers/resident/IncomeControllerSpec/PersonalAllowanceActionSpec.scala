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

package controllers.resident.IncomeControllerSpec

import common.KeystoreKeys.{ResidentKeys => keystoreKeys}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.IncomeController
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import play.api.test.Helpers._
import assets.MessageLookup.{personalAllowance => messages}
import models.resident.{AnnualExemptAmountModel, ChargeableGainResultModel}
import models.resident.income.PersonalAllowanceModel
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class PersonalAllowanceActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {


  def setupTarget(getData: Option[PersonalAllowanceModel],
                  maxPersonalAllowance: Option[BigDecimal] = Some(BigDecimal(11100))): IncomeController = {
    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[PersonalAllowanceModel](Matchers.eq(keystoreKeys.personalAllowance))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

<<<<<<< HEAD
    when(mockCalcConnector.getPA(Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(maxPersonalAllowance))
=======
    when(mockCalcConnector.saveFormData[PersonalAllowanceModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))
>>>>>>> master

    new IncomeController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .personalAllowance from the IncomeController" when {

    "there is no keystore data" should {

      lazy val target = setupTarget(None)
      lazy val result = target.personalAllowance(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Perosnal Allowance view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }

    "there is some keystore data" should {

      lazy val target = setupTarget(Some(PersonalAllowanceModel(1000)))
      lazy val result = target.personalAllowance(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Personal Allowance view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }
  }

  "request has an invalid session" should {

    lazy val target = setupTarget(None)
    lazy val result = target.personalAllowance(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout page" in {
      redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/non-resident/session-timeout")
    }
  }

  "Calling .submitPersonalAllowance from the IncomeController" when {

    "a valid form is submitted" should {


      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
      lazy val target = setupTarget(Some(PersonalAllowanceModel(1000)))
      lazy val result = target.submitPersonalAllowance(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/summary")
      }
    }

    "an invalid form is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
      lazy val target = setupTarget(None)
      lazy val result = target.submitPersonalAllowance(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the personal allowance page" in {
        doc.title() shouldEqual messages.title
      }
    }
  }
}
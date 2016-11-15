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

import assets.MessageLookup.NonResident.{AnnualExemptAmount => messages}
import common.KeystoreKeys
import common.nonresident.CustomerTypeKeys
import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import controllers.helpers.FakeRequestHelper
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup._
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future
import controllers.nonresident.AnnualExemptAmountController
import models.nonresident.{AnnualExemptAmountModel, CustomerTypeModel, DisabledTrusteeModel}

class AnnualExemptAmountSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(
                   getData: Option[AnnualExemptAmountModel],
                   customerType: String = CustomerTypeKeys.individual,
                   disabledTrustee: String = ""
                 ): AnnualExemptAmountController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockCalcElectionConstructor = mock[CalculationElectionConstructor]

    when(mockCalcConnector.fetchAndGetFormData[DisabledTrusteeModel](Matchers.eq(KeystoreKeys.disabledTrustee))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(DisabledTrusteeModel(disabledTrustee))))

    when(mockCalcConnector.fetchAndGetFormData[CustomerTypeModel](Matchers.eq(KeystoreKeys.customerType))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(CustomerTypeModel(customerType))))

    when(mockCalcConnector.fetchAndGetFormData[AnnualExemptAmountModel](Matchers.eq(KeystoreKeys.annualExemptAmount))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.getFullAEA(Matchers.anyInt())(Matchers.any()))
      .thenReturn(Some(BigDecimal(11100)))

    when(mockCalcConnector.getPartialAEA(Matchers.anyInt())(Matchers.any()))
      .thenReturn(Some(BigDecimal(5550)))

    new AnnualExemptAmountController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val calcElectionConstructor: CalculationElectionConstructor = mockCalcElectionConstructor
    }
  }

  "AnnualExemptAmountController" should {
    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      AnnualExemptAmountController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }
  }

  // GET Tests
  "Calling the .annualExemptAmount action" when {

    "not supplied with a pre-existing stored model" should {
      val target = setupTarget(None)
      lazy val result = target.annualExemptAmount(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have the title 'How much of your Capital Gains Tax allowance have you got left?'" in {
        document.title shouldEqual messages.question
      }
    }

    "supplied with a pre-existing stored model" should {
      val target = setupTarget(Some(AnnualExemptAmountModel(1000)))
      lazy val result = target.annualExemptAmount(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have the title 'How much of your Capital Gains Tax allowance have you got left?'" in {
        document.title shouldEqual messages.question
      }
    }

    "not supplied with a valid session" should {
      val target = setupTarget(None)
      lazy val result = target.annualExemptAmount(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/session-timeout")
      }
    }
  }

  // POST Tests
  "Calling the .submitAnnualExemptAmount action" when {

    "submitting a valid form for a non-trustee" should {
      val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("annualExemptAmount", "1000"))
      lazy val result = target.submitAnnualExemptAmount(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "should redirect to the Acquisition Date page" in {
        redirectLocation(result) shouldBe Some(controllers.nonresident.routes.AcquisitionDateController.acquisitionDate().url)
      }
    }

    "submitting a valid form for a non-vulnerable trustee" should {
      val target = setupTarget(None, CustomerTypeKeys.trustee, "No")
      lazy val request = fakeRequestToPOSTWithSession(("annualExemptAmount", "1000"))
      lazy val result = target.submitAnnualExemptAmount(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "should redirect to the Acquisition Date page" in {
        redirectLocation(result) shouldBe Some(controllers.nonresident.routes.AcquisitionDateController.acquisitionDate().url)
      }
    }

    "submitting a valid form for a vulnerable trustee" should {
      val target = setupTarget(None, CustomerTypeKeys.trustee, "Yes")
      lazy val request = fakeRequestToPOSTWithSession(("annualExemptAmount", "1000"))
      lazy val result = target.submitAnnualExemptAmount(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "should redirect to the Acquisition Date page" in {
        redirectLocation(result) shouldBe Some(controllers.nonresident.routes.AcquisitionDateController.acquisitionDate().url)
      }
    }

    "submitting an invalid form" should {
      val target = setupTarget(None, CustomerTypeKeys.trustee, "Yes")
      lazy val request = fakeRequestToPOSTWithSession(("annualExemptAmount", "1000000"))
      lazy val result = target.submitAnnualExemptAmount(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the Annual Exempt Amount page" in {
        document.title shouldBe messages.question
      }
    }
  }
}

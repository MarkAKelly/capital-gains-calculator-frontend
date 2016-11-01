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

import assets.MessageLookup.NonResident.{OtherProperties => messages}
import common.DefaultRoutes._
import common.nonresident.CustomerTypeKeys
import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup._
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future
import controllers.nonresident.{OtherPropertiesController, routes}
import models.nonresident.{CurrentIncomeModel, CustomerTypeModel, OtherPropertiesModel}

class OtherPropertiesActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[OtherPropertiesModel],
                  customerTypeData: Option[CustomerTypeModel],
                  currentIncomeData: Option[CurrentIncomeModel] = None): OtherPropertiesController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[CustomerTypeModel](Matchers.eq(KeystoreKeys.customerType))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(customerTypeData))

    when(mockCalcConnector.fetchAndGetFormData[CurrentIncomeModel](Matchers.eq(KeystoreKeys.currentIncome))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(currentIncomeData))

    new OtherPropertiesController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  // GET Tests
  "Calling the CalculationController.otherProperties" when {

    "no session is active" should {
      lazy val target = setupTarget(None, None)
      lazy val result = target.otherProperties(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${controllers.TimeoutController.timeout("restart", "home")}" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/session-timeout")
      }
    }

    "not supplied with a pre-existing stored model" should {

      "for a customer type of Individual" should {

        val target = setupTarget(None, Some(CustomerTypeModel(CustomerTypeKeys.individual)))
        lazy val result = target.otherProperties(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "display the other properties page" in {
          document.title shouldEqual messages.question
        }

        s"has a 'Back' link to ${routes.PersonalAllowanceController.personalAllowance().url}" in {
          document.body.getElementById("back-link").attr("href") shouldEqual routes.PersonalAllowanceController.personalAllowance().url
        }
      }

      "for a Customer Type of Individual with no Current Income" should {

        val target = setupTarget(None, Some(CustomerTypeModel(CustomerTypeKeys.individual)), Some(CurrentIncomeModel(0)))
        lazy val result = target.otherProperties(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "display the other properties page" in {
          document.title shouldEqual messages.question
        }

        s"have a 'Back' link to ${routes.CurrentIncomeController.currentIncome().url}" in {
          document.body.getElementById("back-link").attr("href") shouldEqual routes.CurrentIncomeController.currentIncome().url
        }
      }

      "for a Customer Type of Trustee" should {

        val target = setupTarget(None, Some(CustomerTypeModel(CustomerTypeKeys.trustee)))
        lazy val result = target.otherProperties(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "display the other properties page" in {
          document.title shouldEqual messages.question
        }

        s"have a 'Back' link to ${routes.DisabledTrusteeController.disabledTrustee().url}" in {
          document.body.getElementById("back-link").attr("href") shouldEqual routes.DisabledTrusteeController.disabledTrustee().url
        }
      }

      "for a Customer Type of Personal Rep" should {

        val target = setupTarget(None, Some(CustomerTypeModel(CustomerTypeKeys.personalRep)))
        lazy val result = target.otherProperties(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "display the other properties page" in {
          document.title shouldEqual messages.question
        }

        s"have a 'Back' link to ${routes.CustomerTypeController.customerType().url}" in {
          document.body.getElementById("back-link").attr("href") shouldEqual routes.CustomerTypeController.customerType().url
        }
      }

      "if no customer type model exists" should {
        val target = setupTarget(None, None, None)
        lazy val result = target.otherProperties(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "display the other properties page" in {
          document.title shouldEqual messages.question
        }

        s"have a 'Back' link to $missingDataRoute " in {
          document.body.getElementById("back-link").attr("href") shouldEqual missingDataRoute
        }
      }
    }
  }

  // POST Tests
  "In CalculationController calling the .submitOtherProperties action" when {

    "for an individual" should {

      "submitting a valid form with 'Yes' and a non-zero amount" should {

        lazy val target = setupTarget(None, Some(CustomerTypeModel(CustomerTypeKeys.individual)))
        lazy val request = fakeRequestToPOSTWithSession(("otherProperties", "Yes"), ("otherPropertiesAmt", "2100"))
        lazy val result = target.submitOtherProperties(request)

        "return a 303" in {
          status(result) shouldBe 303
        }

        "should redirect to the acquisitionDate page" in {
          redirectLocation(result) shouldBe Some(s"${routes.AcquisitionDateController.acquisitionDate()}")
        }
      }

      "submitting a valid form with 'Yes' and a nil amount" should {

        lazy val target = setupTarget(None, Some(CustomerTypeModel(CustomerTypeKeys.individual)))
        lazy val request = fakeRequestToPOSTWithSession(("otherProperties", "Yes"), ("otherPropertiesAmt", "0"))
        lazy val result = target.submitOtherProperties(request)

        "return a 303" in {
          status(result) shouldBe 303
        }

        "should redirect to the annualExemptAmountPage page" in {
          redirectLocation(result) shouldBe Some(s"${routes.AnnualExemptAmountController.annualExemptAmount()}")
        }
      }

      "submitting a valid form with 'No'" should {

        lazy val target = setupTarget(None, Some(CustomerTypeModel(CustomerTypeKeys.individual)))
        lazy val request = fakeRequestToPOSTWithSession(("otherProperties", "No"), ("otherPropertiesAmt", ""))
        lazy val result = target.submitOtherProperties(request)

        "return a 303" in {
          status(result) shouldBe 303
        }

        "should redirect to the acquisitionDate page" in {
          redirectLocation(result) shouldBe Some(s"${routes.AcquisitionDateController.acquisitionDate()}")
        }
      }

      "submitting an form with no data" should {

        lazy val target = setupTarget(None, Some(CustomerTypeModel(CustomerTypeKeys.individual)))
        lazy val request = fakeRequestToPOSTWithSession(("otherProperties", ""), ("otherPropertiesAmt", ""))
        lazy val result = target.submitOtherProperties(request)

        "return a 400" in {
          status(result) shouldBe 400
        }

        "return to the other properties page" in {
          Jsoup.parse(bodyOf(result)).title shouldEqual messages.question
        }
      }
    }

    "for a trustee" should {

      "submitting a valid form with 'Yes' selected" should {

        lazy val target = setupTarget(None, Some(CustomerTypeModel(CustomerTypeKeys.trustee)))
        lazy val request = fakeRequestToPOSTWithSession(("otherProperties", "Yes"), ("otherPropertiesAmt", ""))
        lazy val result = target.submitOtherProperties(request)

        "return a 303" in {
          status(result) shouldBe 303
        }

        "should redirect to the annual exempt amount page" in {
          redirectLocation(result) shouldBe Some(s"${routes.AnnualExemptAmountController.annualExemptAmount()}")
        }
      }

      "submitting a valid form with 'No' selected" should {

        lazy val target = setupTarget(None, Some(CustomerTypeModel(CustomerTypeKeys.trustee)))
        lazy val request = fakeRequestToPOSTWithSession(("otherProperties", "No"), ("otherPropertiesAmt", ""))
        lazy val result = target.submitOtherProperties(request)

        "return a 303" in {
          status(result) shouldBe 303
        }

        "should redirect to the acquisitionDate page" in {
          redirectLocation(result) shouldBe Some(s"${routes.AcquisitionDateController.acquisitionDate()}")
        }
      }
    }

    "for a personal rep" should {

      "submitting a valid form with 'Yes' selected" should {

        lazy val target = setupTarget(None, Some(CustomerTypeModel(CustomerTypeKeys.personalRep)))
        lazy val request = fakeRequestToPOSTWithSession(("otherProperties", "Yes"), ("otherPropertiesAmt", ""))
        lazy val result = target.submitOtherProperties(request)

        "return a 303" in {
          status(result) shouldBe 303
        }

        "should redirect to the annual exempt amount page" in {
          redirectLocation(result) shouldBe Some(s"${routes.AnnualExemptAmountController.annualExemptAmount()}")
        }
      }

      "submitting a valid form with 'No' selected" should {

        lazy val target = setupTarget(None, Some(CustomerTypeModel(CustomerTypeKeys.trustee)))
        lazy val request = fakeRequestToPOSTWithSession(("otherProperties", "No"), ("otherPropertiesAmt", ""))
        lazy val result = target.submitOtherProperties(request)

        "return a 303" in {
          status(result) shouldBe 303
        }

        "should redirect to the acquisitionDate page" in {
          redirectLocation(result) shouldBe Some(s"${routes.AcquisitionDateController.acquisitionDate()}")
        }
      }
    }
  }
}

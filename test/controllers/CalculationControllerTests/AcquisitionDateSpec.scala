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

import common.DefaultRoutes._
import common.KeystoreKeys
import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup._
import org.scalatest.mock.MockitoSugar
import assets.MessageLookup.NonResident.{AcquisitionDate => messages}
import controllers.helpers.FakeRequestHelper
import scala.concurrent.Future
import controllers.nonresident.{AcquisitionDateController, routes}
import models.nonresident.{AcquisitionDateModel, OtherPropertiesModel}

class AcquisitionDateSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[AcquisitionDateModel],
                  otherPropertiesData: Option[OtherPropertiesModel]
                 ): AcquisitionDateController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockCalcElectionConstructor = mock[CalculationElectionConstructor]

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
    .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](Matchers.eq(KeystoreKeys.otherProperties))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(otherPropertiesData))

    new AcquisitionDateController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val calcElectionConstructor: CalculationElectionConstructor = mockCalcElectionConstructor
    }
  }

  "Calling the .acquisitionDate action " should {

    "not supplied with a pre-existing model" should {

      "when Previous Taxable Gains is 'No'" should {

        val target = setupTarget(None, Some(OtherPropertiesModel("No", None)))
        lazy val result = target.acquisitionDate(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "return some HTML that" should {

          s"have load the acquisition date page ${messages.question}" in {
            document.title() shouldBe messages.question
          }

          s"have a 'Back' link to ${routes.OtherPropertiesController.otherProperties().url} " in {
            document.body.getElementById("back-link").attr("href") shouldEqual routes.OtherPropertiesController.otherProperties().url
          }
        }
      }

      "when Previous Taxable Gains is 'Yes' and amount is 0.00" should {

        val target = setupTarget(None, Some(OtherPropertiesModel("Yes", Some(0))))
        lazy val result = target.acquisitionDate(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to ${routes.AnnualExemptAmountController.annualExemptAmount().url} " in {
          document.body.getElementById("back-link").attr("href") shouldEqual routes.AnnualExemptAmountController.annualExemptAmount().url
        }
      }

      "when Previous Taxable Gains is 'Yes' and amount is > 0.00" should {

        val target = setupTarget(None, Some(OtherPropertiesModel("Yes", Some(0.01))))
        lazy val result = target.acquisitionDate(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to ${routes.OtherPropertiesController.otherProperties().url} " in {
          document.body.getElementById("back-link").attr("href") shouldEqual routes.OtherPropertiesController.otherProperties().url
        }
      }

      "when there is no Previous Taxable Gains model" should {

        val target = setupTarget(None, None)
        lazy val result = target.acquisitionDate(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to $missingDataRoute " in {
          document.body.getElementById("back-link").attr("href") shouldEqual missingDataRoute
        }
      }
    }

    "supplied with a model already filled with data" should {

      val testAcquisitionDateModel = new AcquisitionDateModel("Yes", Some(10), Some(12), Some(2016))
      val target = setupTarget(Some(testAcquisitionDateModel), Some(OtherPropertiesModel("No",None)))
      lazy val result = target.acquisitionDate(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      s"have load the acquisition date page ${messages.question}" in {
        document.title() shouldBe messages.question
      }
    }

    "no valid session is present" should {
      val target = setupTarget(None, Some(OtherPropertiesModel("No", None)))
      lazy val result = target.acquisitionDate(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/session-timeout")
      }
    }
  }

  "Calling the submitAcquisitionDate action" when {

    "supplied with a valid model" should {
      val target = setupTarget(None, Some(OtherPropertiesModel("No", None)))
      lazy val request = fakeRequestToPOSTWithSession(("hasAcquisitionDate", "No"))
      lazy val result = target.submitAcquisitionDate(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${controllers.nonresident.routes.AcquisitionValueController.acquisitionValue().url}" in {
        redirectLocation(result).get shouldBe controllers.nonresident.routes.AcquisitionValueController.acquisitionValue().url
      }
    }

    "supplied with an invalid model" should {
      val target = setupTarget(None, Some(OtherPropertiesModel("No", None)))
      lazy val request = fakeRequestToPOSTWithSession(("hasAcquisitionDate", "Yes"))
      lazy val result = target.submitAcquisitionDate(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the Acquisition Date page" in {
        document.title shouldBe messages.question
      }
    }
  }
}

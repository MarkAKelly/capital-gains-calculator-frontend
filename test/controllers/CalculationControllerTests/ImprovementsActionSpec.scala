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
import org.mockito.Matchers
import org.mockito.Mockito._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import assets.MessageLookup.NonResident.{Improvements => messages}
import assets.MessageLookup.{NonResident => commonMessages}
import controllers.helpers.FakeRequestHelper

import scala.concurrent.Future
import controllers.nonresident.{ImprovementsController, routes}
import models.nonresident.{AcquisitionDateModel, ImprovementsModel, RebasedValueModel}

class ImprovementsActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[ImprovementsModel],
                  acquisitionDateData: Option[AcquisitionDateModel],
                  rebasedValueData: Option[RebasedValueModel] = None
                 ): ImprovementsController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[ImprovementsModel](Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[RebasedValueModel](Matchers.eq(KeystoreKeys.rebasedValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(rebasedValueData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(acquisitionDateData))

    new ImprovementsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "In CalculationController calling the .improvements action " when {

    "not supplied with a pre-existing stored model" should {

      "when Acquisition Date is supplied and > 5 April 2016" should {

        val target = setupTarget(None, Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))))
        lazy val result = target.improvements(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "return some HTML that" should {

          s"have the title ${messages.question}" in {
            document.title shouldEqual messages.question
          }
        }
      }

      "when Acquisition Date is supplied and <= 5 April 2016" +
        "and a rebased value is supplied" should {

        val target = setupTarget(
          None,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2014))),
          Some(RebasedValueModel("Yes", Some(500)))
        )
        lazy val result = target.improvements(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a back link that contains ${commonMessages.back}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
        }

        s"have a 'Back' link to ${routes.RebasedCostsController.rebasedCosts().url} " in {
          document.body.getElementById("back-link").attr("href") shouldEqual routes.RebasedCostsController.rebasedCosts().url
        }
      }


      "when Acquisition Date is supplied and <= 5 April 2016" +
        "and no rebased value is supplied" should {

        val target = setupTarget(
          None,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2014))),
          Some(RebasedValueModel("No", None))
        )
        lazy val result = target.improvements(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a back link that contains ${commonMessages.back}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
        }

        s"have a 'Back' link to ${routes.RebasedValueController.rebasedValue().url} " in {
          document.body.getElementById("back-link").attr("href") shouldEqual routes.RebasedValueController.rebasedValue().url
        }
      }

      "when no Acquisition Date Model is supplied" should {

        val target = setupTarget(
          None,
          None,
          None
        )
        lazy val result = target.improvements(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a back link that contains ${commonMessages.back}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
        }

        s"have a 'Back' link to $missingDataRoute " in {
          document.body.getElementById("back-link").attr("href") shouldEqual missingDataRoute
        }
      }

      "when Acquisition Date <= 5 April and no rebased model is supplied" should {

        val target = setupTarget(
          None,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2014))),
          None
        )
        lazy val result = target.improvements(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a back link that contains ${commonMessages.back}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
        }

        s"have a 'Back' link to $missingDataRoute " in {
          document.body.getElementById("back-link").attr("href") shouldEqual missingDataRoute
        }
      }
    }
  }


  "In CalculationController calling the .submitImprovements action " when {

    "submitting a valid form with 'Yes' and a value of 12045 for both fields" should {

      val target = setupTarget(None, None)
      lazy val request = fakeRequestToPOSTWithSession("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "12045", "improvementsAmtAfter" -> "12045")
      lazy val result = target.submitImprovements(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.DisposalDateController.disposalDate()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.DisposalDateController.disposalDate()}")
      }
    }

    "submitting a valid form with 'Yes' and a value of 12045 for improvementsAmt and a None for improvementsAmtAfter" should {

      val target = setupTarget(None, None)
      lazy val request = fakeRequestToPOSTWithSession("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "12045", "improvementsAmtAfter" -> "")
      lazy val result = target.submitImprovements(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.DisposalDateController.disposalDate()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.DisposalDateController.disposalDate()}")
      }
    }

    "submitting a valid form with 'Yes' and a value of 12045 for improvementsAmtAfter and a None for improvementsAmt" should {

      // set up scenario so that both improvements values are displayed
      val acquisitionData = AcquisitionDateModel("No", None, None, None)
      val rebasedData = RebasedValueModel("Yes", Some(1))

      val target = setupTarget(None, Some(acquisitionData), Some(rebasedData))
      lazy val request = fakeRequestToPOSTWithSession("isClaimingImprovements" -> "Yes", "improvementsAmt" -> "", "improvementsAmtAfter" -> "12045")
      lazy val result = target.submitImprovements(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.DisposalDateController.disposalDate()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.DisposalDateController.disposalDate()}")
      }
    }


    "submitting a valid form with 'No' and no value" should {

      val target = setupTarget(None, None)
      lazy val request = fakeRequestToPOSTWithSession("isClaimingImprovements" -> "No", "improvementsAmt" -> "")
      lazy val result = target.submitImprovements(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.DisposalDateController.disposalDate()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.DisposalDateController.disposalDate()}")
      }
    }

    "submitting an invalid form with 'testData123' and a value of 'fhu39awd8'" should {

      val target = setupTarget(None, None)
      lazy val request = fakeRequestToPOSTWithSession("isClaimingImprovements" -> "testData123", "improvementsAmt" -> "fhu39awd8")
      lazy val result = target.submitImprovements(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "return to the improvements page" in {
        document.title shouldBe messages.question
      }
    }
  }
}
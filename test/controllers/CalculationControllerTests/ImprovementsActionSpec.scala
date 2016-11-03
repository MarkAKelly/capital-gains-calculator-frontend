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

        s"have a 'Back' link to ${routes.RebasedCostsController.rebasedCosts().url} " in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
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

        s"have a 'Back' link to ${routes.RebasedValueController.rebasedValue().url} " in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
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

        s"have a 'Back' link to $missingDataRoute " in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
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

        s"have a 'Back' link to $missingDataRoute " in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
          document.body.getElementById("back-link").attr("href") shouldEqual missingDataRoute
        }
      }
    }

    "supplied with a pre-existing model with 'Yes' checked and value already entered" should {

      val target = setupTarget(
        Some(ImprovementsModel("Yes", Some(10000))),
        Some(AcquisitionDateModel("Yes", Some(1), Some(1),Some(2017)))
      )

      lazy val result = target.improvements(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "be pre populated with Yes box selected and a value of 10000 entered" in {
          document.getElementById("isClaimingImprovements-yes").attr("checked") shouldEqual "checked"
          document.getElementById("improvementsAmt").attr("value") shouldEqual "10000"
        }
      }
    }

    "supplied with a pre-existing model with 'No' checked and value already entered" should {

      val target = setupTarget(
        Some(ImprovementsModel("No", Some(0))),
        Some(AcquisitionDateModel("Yes", Some(1), Some(1),Some(2017)))
      )

      lazy val result = target.improvements(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "be pre populated with No box selected and a value of 0" in {
          document.getElementById("isClaimingImprovements-no").attr("checked") shouldEqual "checked"
          document.getElementById("improvementsAmt").attr("value") shouldEqual "0"
        }
      }
    }

    "not supplied with a pre-existing stored model but with an acquisition date and a rebased value" should {

      val target = setupTarget(
        None,
        Some(AcquisitionDateModel("Yes", Some(1), Some(1),Some(2000))),
        Some(RebasedValueModel("Yes", Some(1000)))
      )

      lazy val result = target.improvements(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "contain a two hidden input boxes for improvements" in {
        document.body.getElementById("hidden").getElementsByTag("input").first().id() shouldBe "improvementsAmt"
        document.body.getElementById("hidden").getElementsByTag("input").last().id() shouldBe "improvementsAmtAfter"
      }
    }

    "not supplied with a pre-existing stored model and no acquisition date but with a rebased value" should {

      val target = setupTarget(
        None,
        Some(AcquisitionDateModel("No", Some(1), Some(1),Some(2000))),
        Some(RebasedValueModel("Yes", Some(1000)))
      )

      lazy val result = target.improvements(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "contain a two hidden input boxes for improvements" in {
        document.body.getElementById("hidden").getElementsByTag("input").first().id() shouldBe "improvementsAmt"
        document.body.getElementById("hidden").getElementsByTag("input").last().id() shouldBe "improvementsAmtAfter"
      }
    }

    "not supplied with a pre-existing stored model and with no rebased value model" should {

      val target = setupTarget(
        None,
        Some(AcquisitionDateModel("Yes", Some(1), Some(1),Some(2017)))
      )

      lazy val result = target.improvements(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "contain a two hidden input boxes for improvements" in {
        document.body.getElementById("hidden").html should include("input")
        document.body.getElementById("hidden").getElementsByTag("input").first().id() shouldBe "improvementsAmt"
        document.body.getElementById("hidden").getElementsByTag("input").last().id() shouldBe "improvementsAmt"
      }
    }
  }

  "In CalculationController calling the .submitImprovements action " when {

    "submitting a valid form with 'Yes' and a value of 12045" should {

      val target = setupTarget(None, None)
      lazy val request = fakeRequestToPOSTWithSession("improvements" -> "Yes", "improvementsAmt" -> "12045", "improvementsAmtAfter" -> "12045")
      lazy val result = target.submitImprovements(request)

      "return a 303" in {
        status(result) shouldBe 303
      }
    }

    "submitting a valid form with 'No' and no value" should {

      val target = setupTarget(None, None)
      lazy val request = fakeRequestToPOSTWithSession("improvements" -> "No", "improvementsAmt" -> "")
      lazy val result = target.submitImprovements(request)

      "return a 303" in {
        status(result) shouldBe 303
      }
    }

    "submitting an invalid form with 'testData123' and a value of 'fhu39awd8'" should {

      val target = setupTarget(None, None)
      lazy val request = fakeRequestToPOSTWithSession("improvements" -> "testData123", "improvementsAmt" -> "fhu39awd8")
      lazy val result = target.submitImprovements(request)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"return HTML that displays the error message ${commonMessages.errorRealNumber}" in {
        document.select("div#hidden span.error-notification").text shouldEqual commonMessages.errorRealNumber
      }
    }
  }
}

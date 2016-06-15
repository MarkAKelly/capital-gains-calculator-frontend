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
import common.{Constants, KeystoreKeys, TestModels}
import connectors.CalculatorConnector
import constructors.CalculationElectionConstructor
import controllers.{CalculationController, routes}
import models._
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class OtherReliefsSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  implicit val hc = new HeaderCarrier()
  def setupTarget(
                   getData: Option[OtherReliefsModel],
                   postData: Option[OtherReliefsModel],
                   summary: SummaryModel,
                   result: CalculationResultModel,
                   acquisitionDateData: Option[AcquisitionDateModel],
                   rebasedValueData: Option[RebasedValueModel]
                 ): CalculationController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockCalcElectionConstructor = mock[CalculationElectionConstructor]

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[RebasedValueModel](Matchers.eq(KeystoreKeys.rebasedValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(rebasedValueData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(acquisitionDateData))

    when(mockCalcConnector.createSummary(Matchers.any()))
      .thenReturn(Future.successful(summary))

    when(mockCalcConnector.calculateFlat(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(result)))

    lazy val data = CacheMap("form-id", Map("data" -> Json.toJson(postData.getOrElse(OtherReliefsModel(Some("Yes"), Some(1000))))))
    when(mockCalcConnector.saveFormData[OtherReliefsModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))

    new CalculationController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val calcElectionConstructor: CalculationElectionConstructor = mockCalcElectionConstructor
    }
  }

  "In CalculationController calling the .otherReliefs action " when {
    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/other-reliefs").withSession(SessionKeys.sessionId -> "12345")

    "not supplied with a pre-existing stored model" should {

      "when Acquisition Date > 5 April 2015" should {

        val target = setupTarget(
          None,
          None,
          TestModels.summaryIndividualFlatWithoutAEA,
          TestModels.calcModelTwoRates,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
          None
        )
        lazy val result = target.otherReliefs(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200 with a valid calculation result" in {
          status(result) shouldBe 200
        }

        "return some HTML that" should {

          "contain some text and use the character set utf-8" in {
            contentType(result) shouldBe Some("text/html")
            charset(result) shouldBe Some("utf-8")
          }

          "have the title 'Do you want to add other tax relief?'" in {
            document.title shouldEqual Messages("calc.otherReliefs.questionTwo")
          }

          "have the heading Calculate your tax (non-residents) " in {
            document.body.getElementsByTag("h1").text shouldEqual Messages("calc.base.pageHeading")
          }

          s"have a 'Back' link to ${routes.CalculationController.allowableLosses().url}" in {
            document.body.getElementById("back-link").text shouldEqual Messages("calc.base.back")
            document.body.getElementById("back-link").attr("href") shouldEqual routes.CalculationController.allowableLosses().url
          }

          "have a yes no helper with hidden content and question 'Do you want to add other tax relief?'" in {
            document.body.getElementById("isClaimingOtherReliefs-yes").parent.text shouldBe Messages("calc.base.yes")
            document.body.getElementById("isClaimingOtherReliefs-no").parent.text shouldBe Messages("calc.base.no")
            document.body.getElementsByTag("legend").text shouldBe Messages("calc.otherReliefs.questionTwo")
          }

          "have the help text 'For example, lettings relief'" in {
            document.body.getElementsByClass("form-hint").text should include(Messages("calc.otherReliefs.help"))
          }

          "have a value for your gain" in {
            document.getElementById("totalGain").text() shouldBe "Total gain £40,000"
          }

          "display an input box for the Other Tax Reliefs with question 'How much extra tax relief are you claiming?" in {
            document.body.getElementById("otherReliefs").tagName() shouldEqual "input"
            document.select("label[for=otherReliefs]").text should include(Messages("calc.otherReliefs.question"))
          }

          "display a 'Continue' button " in {
            document.body.getElementById("continue-button").text shouldEqual Messages("calc.base.continue")
          }

          "include helptext for 'Total gain'" in {
            document.body.getElementById("totalGain").text should include(Messages("calc.otherReliefs.totalGain"))
          }

          "include helptext for 'Taxable gain'" in {
            document.body.getElementById("taxableGain").text should include(Messages("calc.otherReliefs.taxableGain"))
          }
        }
      }
    }

    "supplied with a pre-existing stored model and a loss" should {
      val testOtherReliefsModel = OtherReliefsModel(Some("Yes"), Some(5000))
      val target = setupTarget(
        Some(testOtherReliefsModel),
        None,
        TestModels.summaryIndividualFlatWithoutAEA,
        TestModels.calcModelLoss,
        Some(AcquisitionDateModel("Yes",Some(1),Some(1),Some(2017))),
        None
      )
      lazy val result = target.otherReliefs(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200 with a valid calculation call" in {
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "have the value 5000 auto-filled into the input box" in {
          document.getElementById("otherReliefs").attr("value") shouldEqual "5000"
        }

        "have a value for your loss" in {
          document.getElementById("totalGain").text() shouldBe "Total loss £10,000"
        }
      }
    }
  }

  "In CalculationController calling the .submitOtherReliefs action" when {
    def buildRequest(body: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST", "/calculate-your-capital-gains/other-reliefs")
      .withSession(SessionKeys.sessionId -> "12345")
      .withFormUrlEncodedBody(body: _*)

    def executeTargetWithMockData(selection: String, amount: String, summary: SummaryModel): Future[Result] = {
      lazy val fakeRequest = buildRequest(("isClaimingOtherReliefs", selection), ("otherReliefs", amount))
      val numeric = "(0-9*)".r
      val mockData = (selection, amount) match {
        case ("", "") => OtherReliefsModel(None, None)
        case ("Yes", numeric(money)) => OtherReliefsModel(Some("Yes"), Some(BigDecimal(money)))
        case _ => OtherReliefsModel(Some("No"), None)
      }
      val target = setupTarget(
        None,
        Some(mockData),
        summary,
        TestModels.calcModelOneRate,
        Some(AcquisitionDateModel("Yes",Some(1),Some(1),Some(2017))),
        None
      )
      target.submitOtherReliefs(fakeRequest)
    }

    "submitting a valid form with and an amount of 1000" should {

      "return a 303 with no Acquisition date" in {
        lazy val result = executeTargetWithMockData("Yes", "1000", TestModels.summaryIndividualFlatWithoutAEA)
        status(result) shouldBe 303
      }

      "return a 303 with an Acquisition date after the start date" in {
        lazy val result = executeTargetWithMockData("Yes", "1000", TestModels.summaryIndividualAcqDateAfter)
        status(result) shouldBe 303
      }
    }

    "submitting a valid form with and an amount with two decimal places" should {
      lazy val result = executeTargetWithMockData("Yes", "1000.11", TestModels.summaryIndividualFlatWithoutAEA)

      "return a 303" in {
        status(result) shouldBe 303
      }
    }

    "submitting an invalid form with no radio button selected" should {
      lazy val result = executeTargetWithMockData("", "", TestModels.summaryIndividualFlatWithoutAEA)

      "return a 400" in {
        status(result) shouldBe 400
      }
    }

    "submitting an invalid form with 'Yes' selected and no value supplied" should {
      lazy val result = executeTargetWithMockData("Yes", "0", TestModels.summaryIndividualFlatWithoutAEA)

      "return a 400" in {
        status(result) shouldBe 303
      }
    }

    "submitting an invalid form with 'Yes' and an amount with three decimal places" should {
      lazy val result = executeTargetWithMockData("Yes", "0.111", TestModels.summaryIndividualFlatWithoutAEA)

      "return a 400" in {
        status(result) shouldBe 400
      }
    }

    "submitting an invalid form with 'Yes' and a negative value" should {
      lazy val result = executeTargetWithMockData("Yes", "-1000", TestModels.summaryIndividualFlatWithoutAEA)

      "return a 400" in {
        status(result) shouldBe 400
      }
    }

    "submitting an invalid form with 'Yes' and a value of shdgsaf" should {
      lazy val result = executeTargetWithMockData("Yes", "shdgsaf", TestModels.summaryIndividualFlatWithoutAEA)

      "return a 400" in {
        status(result) shouldBe 400
      }
    }

    "submitting a value with 'Yes' and value exceeds the maximum numeric" should {

      lazy val result = executeTargetWithMockData("Yes", (Constants.maxNumeric + 0.01).toString, TestModels.summaryIndividualFlatWithoutAEA)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"fail with message ${Messages("calc.common.error.maxNumericExceeded")}" in {
        document.getElementsByClass("error-notification").text should
          include (Messages("calc.common.error.maxNumericExceeded") + Constants.maxNumeric + " " + Messages("calc.common.error.maxNumericExceeded.OrLess"))
      }
    }
  }
}
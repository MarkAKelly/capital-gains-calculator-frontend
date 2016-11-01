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

import common.{Constants, TestModels}
import connectors.CalculatorConnector
import controllers.nonresident.{OtherReliefsTAController, routes}
import models.nonresident.{CalculationResultModel, OtherReliefsModel, SummaryModel}
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.play.views.helpers.MoneyPounds
import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{OtherReliefs => messages}
import scala.concurrent.Future

class OtherReliefsTASpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget(
                   getData: Option[OtherReliefsModel],
                   postData: Option[OtherReliefsModel],
                   summary: SummaryModel,
                   result: CalculationResultModel
                 ): OtherReliefsTAController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.createSummary(Matchers.any()))
      .thenReturn(Future.successful(summary))

    when(mockCalcConnector.calculateTA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(result)))

    lazy val data = CacheMap("form-id", Map("data" -> Json.toJson(postData.getOrElse(OtherReliefsModel(None, Some(1000))))))
    when(mockCalcConnector.saveFormData[OtherReliefsModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))

    new OtherReliefsTAController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "In CalculationController calling the .otherReliefsTA action " should {
    lazy val fakeRequest = FakeRequest("GET",
      "/calculate-your-capital-gains/non-resident/other-reliefs-time-apportioned").withSession(SessionKeys.sessionId -> "12345")
    val target = setupTarget(None, None, TestModels.summaryTrusteeTAWithAEA, TestModels.calcModelTwoRates)
    lazy val result = target.otherReliefsTA(fakeRequest)
    lazy val document = Jsoup.parse(bodyOf(result))

    "return a 200" in {
      status(result) shouldBe 200
    }

    "return some HTML that" should {

      "contain some text and use the character set utf-8" in {
        contentType(result) shouldBe Some("text/html")
        charset(result) shouldBe Some("utf-8")
      }

      s"have the title '${messages.inputQuestion}'" in {
        document.title shouldEqual messages.inputQuestion
      }

      s"have the heading ${commonMessages.pageHeading} " in {
        document.body.getElementsByTag("h1").text shouldEqual commonMessages.pageHeading
      }

      s"have a 'Back' link to ${routes.CalculationElectionController.calculationElection()}" in {
        document.body.getElementById("back-link").text shouldEqual commonMessages.back
        document.body.getElementById("back-link").attr("href") shouldEqual routes.CalculationElectionController.calculationElection().toString()
      }

      s"have the question '${messages.inputQuestion}' as the legend of the input" in {
        document.body.getElementsByTag("label").text should include(messages.inputQuestion)
      }

      s"have the help text '${messages.help}'" in {
        document.body.getElementsByClass("form-hint").text should include(messages.help)
      }

      "have a value for your gain" in {
        document.getElementById("totalGain").text() shouldBe s"${messages.totalGain} £40,000"
      }

      "display an input box for the Other Tax Reliefs" in {
        document.body.getElementById("otherReliefs").tagName() shouldEqual "input"
      }

      s"display an '${messages.addRelief}' button " in {
        document.body.getElementById("add-relief-button").text shouldEqual messages.addRelief
      }

      s"include helptext for '${messages.totalGain}'" in {
        document.body.getElementById("totalGain").text should include(messages.totalGain)
      }

      s"include helptext for '${messages.taxableGain}'" in {
        document.body.getElementById("taxableGain").text should include(messages.taxableGain)
      }
    }

    "when not supplied with any previous value" should {
      lazy val fakeRequest = FakeRequest("GET",
        "/calculate-your-capital-gains/non-resident/other-reliefs-time-apportioned").withSession(SessionKeys.sessionId -> "12345")
      val target = setupTarget(None, None, TestModels.summaryTrusteeTAWithAEA, TestModels.calcModelTwoRates)
      lazy val result = target.otherReliefsTA(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "contain no pre-filled data" in {
        document.body.getElementById("otherReliefs").attr("value") shouldBe ""
      }
    }

    "when supplied with a previous value" should {
      lazy val fakeRequest = FakeRequest("GET",
        "/calculate-your-capital-gains/non-resident/other-reliefs-time-apportioned").withSession(SessionKeys.sessionId -> "12345")
      val testModel = OtherReliefsModel(None, Some(1000))
      val target = setupTarget(Some(testModel), None, TestModels.summaryTrusteeTAWithAEA, TestModels.calcModelTwoRates)
      lazy val result = target.otherReliefsTA(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "contain the pre-supplied data" in {
        document.body.getElementById("otherReliefs").attr("value") shouldBe "1000"
      }
    }
  }

  "In CalculationController calling the .submitOtherReliefsTA action" when {
    def buildRequest(body: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST",
      "/calculate-your-capital-gains/non-resident/other-reliefs-time-apportioned")
      .withSession(SessionKeys.sessionId -> "12345")
      .withFormUrlEncodedBody(body: _*)

    def executeTargetWithMockData(amount: String, summary: SummaryModel): Future[Result] = {
      lazy val fakeRequest = buildRequest(("otherReliefs", amount))
      val numeric = "(-?\\d*.\\d*)".r
      val mockData = amount match {
        case numeric(money) => OtherReliefsModel(None, Some(BigDecimal(money)))
        case _ => OtherReliefsModel(None, None)
      }
      val target = setupTarget(None, Some(mockData), summary, TestModels.calcModelOneRate)
      target.submitOtherReliefsTA(fakeRequest)
    }

    "submitting a valid form with and an amount of 1000" should {
      lazy val result = executeTargetWithMockData("1000", TestModels.summaryTrusteeTAWithAEA)

      "return a 303" in {
        status(result) shouldBe 303
      }
    }

    "submitting a valid form with and an amount with two decimal places" should {
      lazy val result = executeTargetWithMockData("1000.11", TestModels.summaryTrusteeTAWithAEA)

      "return a 303" in {
        status(result) shouldBe 303
      }
    }

    "submitting an valid form with no value" should {
      lazy val result = executeTargetWithMockData("", TestModels.summaryTrusteeTAWithAEA)

      "return a 303" in {

      }
    }
  }
}

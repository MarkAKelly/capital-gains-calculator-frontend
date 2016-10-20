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

import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{CurrentIncome => messages}

import common.Constants
import connectors.CalculatorConnector
import controllers.nonresident.{CurrentIncomeController, routes}
import models.nonresident.CurrentIncomeModel
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
import uk.gov.hmrc.play.views.helpers.MoneyPounds

import scala.concurrent.Future

class CurrentIncomeSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  implicit val hc = new HeaderCarrier()
  def setupTarget(getData: Option[CurrentIncomeModel], postData: Option[CurrentIncomeModel]): CurrentIncomeController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[CurrentIncomeModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    lazy val data = CacheMap("form-id", Map("data" -> Json.toJson(postData.getOrElse(CurrentIncomeModel(0)))))
    when(mockCalcConnector.saveFormData[CurrentIncomeModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))

    new CurrentIncomeController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }


  //GET Tests
  "In CalculationController calling the .currentIncome action " when {

    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/non-resident/current-income").withSession(SessionKeys.sessionId -> "12345")

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None, None)
      lazy val result = target.currentIncome(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        s"have the title ${messages.question}" in {
          document.title shouldEqual messages.question
        }

        s"have the heading ${commonMessages.pageHeading}" in {
          document.body.getElementsByTag("h1").text shouldEqual commonMessages.pageHeading
        }

        s"have a 'Back' link to ${routes.CustomerTypeController.customerType()}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
          document.body.getElementById("back-link").attr("href") shouldEqual routes.CustomerTypeController.customerType().toString()
        }

        s"have the question '${messages.question}' as the label of the input" in {
          document.body.getElementsByTag("label").text.contains(messages.question) shouldBe true
        }

        s"have the help text '${messages.helpText}' as the form-hint of the input" in {
          document.body.getElementsByClass("form-hint").text shouldEqual messages.helpText
        }

        "display an input box for the Current Income Amount" in {
          document.body.getElementById("currentIncome").tagName() shouldEqual "input"
        }

        "have no value auto-filled into the input box" in {
          document.getElementById("currentIncome").attr("value") shouldBe empty
        }

        "display a 'Continue' button " in {
          document.body.getElementById("continue-button").text shouldEqual commonMessages.continue
        }

        "should contain a Read more sidebar with a link to CGT allowances" in {
          document.select("aside h2").text shouldBe commonMessages.readMore
          document.select("aside a").first.text shouldBe s"${messages.linkOne} ${commonMessages.externalLink}"
          document.select("aside a").last.text shouldBe s"${messages.linkTwo} ${commonMessages.externalLink}"
        }
      }
    }

    "supplied with a pre-existing stored model" should {

      val target = setupTarget(Some(CurrentIncomeModel(1000)), None)
      lazy val result = target.currentIncome(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "have some value auto-filled into the input box" in {

          document.getElementById("currentIncome").attr("value") shouldBe "1000"
        }
      }
    }
  }

  "In CalculationController calling the .currentIncome action " when {

    "called with no active session or valid session Id" should {

      lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/non-resident/current-income")
      s"redirect to ${controllers.routes.TimeoutController.timeout("", "")}" in {

        val target = setupTarget(None, None)
        lazy val result = target.currentIncome(fakeRequest)

        redirectLocation(result).get should include ("/calculate-your-capital-gains/session-timeout")
      }
    }
  }


  //POST Tests
  "In CalculationController calling the .submitCurrentIncome action " when {

    def buildRequest(body: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST",
      "/calculate-your-capital-gains/non-resident/current-income")
      .withSession(SessionKeys.sessionId -> "12345")
      .withFormUrlEncodedBody(body: _*)

    def executeTargetWithMockData(amount: String): Future[Result] = {

      lazy val fakeRequest = buildRequest(("currentIncome", amount))

      val numeric = "(0-9*)".r
      val mockData = amount match {
        case numeric(money) => new CurrentIncomeModel(BigDecimal(money))
        case _ => new CurrentIncomeModel(0)
      }

      val target = setupTarget(None, Some(mockData))
      target.submitCurrentIncome(fakeRequest)
    }

    "submitting a valid form" should {

      lazy val result = executeTargetWithMockData("1000")

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.PersonalAllowanceController.personalAllowance()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.PersonalAllowanceController.personalAllowance()}")
      }
    }

    "submitting a valid form with a £0 amount" should {

      lazy val result = executeTargetWithMockData("0")

      "return a 303" in {
        status(result) shouldBe 303
      }

      s"redirect to ${routes.OtherPropertiesController.otherProperties()}" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherPropertiesController.otherProperties()}")
      }
    }

    "submitting an invalid form with no data" should {

      lazy val result = executeTargetWithMockData("")

      "return a 400" in {
        status(result) shouldBe 400
      }

      //############################################################################
      //Need to test the message that is returned and that only on error is rendered
      //############################################################################
    }

    "submitting an invalid form with a negative value" should {

      lazy val result = executeTargetWithMockData("-1000")

      "return a 400" in {
        status(result) shouldBe 400
      }

      //############################################################################
      //Need to test the message that is returned and that only on error is rendered
      //############################################################################
    }

    "submitting an invalid form with value 1.111" should {

      lazy val result = executeTargetWithMockData("1.111")
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      s"fail with message ${messages.errorDecimalPlace}" in {
        //With the changes to the forms this should definitley be a shouldBe or shouldEqual not include..
        document.getElementsByClass("error-notification").text should include (messages.errorDecimalPlace)
      }
    }

    "submitting a value which exceeds the maximum numeric" should {

      lazy val result = executeTargetWithMockData((Constants.maxNumeric + 0.01).toString)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }


      s"fail with message ${commonMessages.maximumLimit(MoneyPounds(Constants.maxNumeric, 0).quantity)}" in {
        document.getElementsByClass("error-notification").text should
          include (commonMessages.maximumLimit(MoneyPounds(Constants.maxNumeric, 0).quantity))
      }
    }
  }
}

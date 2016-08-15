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
import common.nonresident.CustomerTypeKeys
import common.{Constants, KeystoreKeys}
import connectors.CalculatorConnector
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap
import constructors.nonresident.CalculationElectionConstructor
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup._
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future
import controllers.nonresident.{OtherPropertiesController, routes}
import models.nonresident.{CurrentIncomeModel, CustomerTypeModel, OtherPropertiesModel}
import play.api.mvc.Result
import uk.gov.hmrc.play.views.helpers.MoneyPounds

class OtherPropertiesSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[OtherPropertiesModel],
                  postData: Option[OtherPropertiesModel],
                  customerTypeData: Option[CustomerTypeModel],
                  currentIncomeData: Option[CurrentIncomeModel] = None): OtherPropertiesController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockCalcElectionConstructor = mock[CalculationElectionConstructor]

    when(mockCalcConnector.fetchAndGetFormData[OtherPropertiesModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[CustomerTypeModel](Matchers.eq(KeystoreKeys.customerType))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(customerTypeData))

    when(mockCalcConnector.fetchAndGetFormData[CurrentIncomeModel](Matchers.eq(KeystoreKeys.currentIncome))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(currentIncomeData))

    lazy val data = CacheMap("form-id", Map("data" -> Json.toJson(postData.getOrElse(OtherPropertiesModel("", Some(0))))))
    when(mockCalcConnector.saveFormData[OtherPropertiesModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))

    new OtherPropertiesController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  // GET Tests
  "Calling the CalculationController.otherProperties" when {

    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/non-resident/other-properties").withSession(SessionKeys.sessionId -> "12345")

    "not supplied with a pre-existing stored model" should {

      "for a customer type of Individual" should {

        val target = setupTarget(None, None, Some(CustomerTypeModel(CustomerTypeKeys.individual)))
        lazy val result = target.otherProperties(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "return some HTML that" should {

          "contain some text and use the character set utf-8" in {
            contentType(result) shouldBe Some("text/html")
            charset(result) shouldBe Some("utf-8")
          }

          "have the title 'Did you sell or give away any other properties in that tax year?'" in {
            document.title shouldEqual Messages("calc.otherProperties.question")
          }

          "have the heading Calculate your tax (non-residents) " in {
            document.body.getElementsByTag("h1").text shouldEqual Messages("calc.base.pageHeading")
          }

          s"have a 'Back' link to ${routes.PersonalAllowanceController.personalAllowance().url}" in {
            document.body.getElementById("back-link").text shouldEqual Messages("calc.base.back")
            document.body.getElementById("back-link").attr("href") shouldEqual routes.PersonalAllowanceController.personalAllowance().url
          }

          s"have the question '${Messages("calc.otherProperties.question")}' as the legend of the input" in {
            document.body.getElementsByTag("legend").text should include (Messages("calc.otherProperties.question"))
          }

          "include a read more section that" should {

            s"include a link to https://www.gov.uk/capital-gains-tax with text '${Messages("calc.otherProperties.link.one")}'" in {
              document.body.getElementById("helpLink1").text shouldEqual s"${Messages("calc.otherProperties.link.one")} ${Messages("calc.base.externalLink")}"
              document.body.getElementById("helpLink1").attr("href") shouldEqual "https://www.gov.uk/capital-gains-tax"
            }

            s"include a link to https://www.gov.uk/income-tax-rates/previous-tax-years with text '${Messages("calc.otherProperties.link.two")}'" in {
              document.body.getElementById("helpLink2").text shouldEqual s"${Messages("calc.otherProperties.link.two")} ${Messages("calc.base.externalLink")}"
              document.body.getElementById("helpLink2").attr("href") shouldEqual "https://www.gov.uk/income-tax-rates/previous-tax-years"
            }

          }

          "display a radio button with the option `Yes`" in {
            document.body.getElementById("otherProperties-yes").parent.text shouldEqual Messages("calc.base.yes")
          }

          "display a radio button with the option `No`" in {
            document.body.getElementById("otherProperties-no").parent.text shouldEqual Messages("calc.base.no")
          }

          "have a hidden monetary input with question 'What was your taxable gain?'" in {
            document.body.getElementById("otherPropertiesAmt").tagName shouldEqual "input"
            document.select("label[for=otherPropertiesAmt]").text should include(Messages("calc.otherProperties.questionTwo"))
          }

          "display a 'Continue' button " in {
            document.body.getElementById("continue-button").text shouldEqual Messages("calc.base.continue")
          }
        }
      }

      "for a Customer Type of Individual with no Current Income" should {

        val target = setupTarget(None, None, Some(CustomerTypeModel(CustomerTypeKeys.individual)), Some(CurrentIncomeModel(0)))
        lazy val result = target.otherProperties(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to ${routes.CurrentIncomeController.currentIncome().url}" in {
          document.body.getElementById("back-link").text shouldEqual Messages("calc.base.back")
          document.body.getElementById("back-link").attr("href") shouldEqual routes.CurrentIncomeController.currentIncome().url
        }
      }

      "for a Customer Type of Trustee" should {

        val target = setupTarget(None, None, Some(CustomerTypeModel(CustomerTypeKeys.trustee)))
        lazy val result = target.otherProperties(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to ${routes.DisabledTrusteeController.disabledTrustee().url}" in {
          document.body.getElementById("back-link").text shouldEqual Messages("calc.base.back")
          document.body.getElementById("back-link").attr("href") shouldEqual routes.DisabledTrusteeController.disabledTrustee().url
        }
      }

      "for a Customer Type of Personal Rep" should {

        val target = setupTarget(None, None, Some(CustomerTypeModel(CustomerTypeKeys.personalRep)))
        lazy val result = target.otherProperties(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to ${routes.CustomerTypeController.customerType().url}" in {
          document.body.getElementById("back-link").text shouldEqual Messages("calc.base.back")
          document.body.getElementById("back-link").attr("href") shouldEqual routes.CustomerTypeController.customerType().url
        }
      }

      "if no customer type model exists" should {
        val target = setupTarget(None, None, None)
        lazy val result = target.otherProperties(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to $missingDataRoute " in {
          document.body.getElementById("back-link").text shouldEqual Messages("calc.base.back")
          document.body.getElementById("back-link").attr("href") shouldEqual missingDataRoute
        }
      }
    }

    "supplied with a model that already contains data" should {

      "for an individual" should {

        val target = setupTarget(Some(OtherPropertiesModel("Yes", Some(2100))), None, Some(CustomerTypeModel(CustomerTypeKeys.individual)))
        lazy val result = target.otherProperties(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "return some HTML that" should {
          "contain some text and use the character set utf-8" in {
            contentType(result) shouldBe Some("text/html")
            charset(result) shouldBe Some("utf-8")
          }

          "have the radio option `Yes` selected by default" in {
            document.body.getElementById("otherProperties-yes").parent.classNames().contains("selected") shouldBe true
          }

          "have the value 2100 auto filled" in {
            document.body().getElementById("otherPropertiesAmt").attr("value") shouldBe "2100"
          }
        }
      }

      "for a trustee" should {

        val target = setupTarget(Some(OtherPropertiesModel("Yes", None)), None, Some(CustomerTypeModel(CustomerTypeKeys.trustee)))
        lazy val result = target.otherProperties(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "return some HTML that" should {
          "contain some text and use the character set utf-8" in {
            contentType(result) shouldBe Some("text/html")
            charset(result) shouldBe Some("utf-8")
          }

          "have the radio option `Yes` selected by default" in {
            document.body.getElementById("otherProperties-yes").parent.classNames().contains("selected") shouldBe true
          }
        }

        "for a personal rep" should {

          val target = setupTarget(Some(OtherPropertiesModel("Yes", None)), None, Some(CustomerTypeModel(CustomerTypeKeys.personalRep)))
          lazy val result = target.otherProperties(fakeRequest)
          lazy val document = Jsoup.parse(bodyOf(result))

          "return a 200" in {
            status(result) shouldBe 200
          }

          "return some HTML that" should {
            "contain some text and use the character set utf-8" in {
              contentType(result) shouldBe Some("text/html")
              charset(result) shouldBe Some("utf-8")
            }

            "have the radio option `Yes` selected by default" in {
              document.body.getElementById("otherProperties-yes").parent.classNames().contains("selected") shouldBe true
            }
          }
        }
      }
    }
  }

  // POST Tests
  "In CalculationController calling the .submitOtherProperties action" when {

    def buildRequest(body: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST",
      "/calculate-your-capital-gains/non-resident/other-properties")
      .withSession(SessionKeys.sessionId -> "12345")
      .withFormUrlEncodedBody(body: _*)

    "for an individual" should {

      def executeTargetWithMockData(selection: String, amount: String): Future[Result] = {
        lazy val fakeRequest = buildRequest(("otherProperties", selection), ("otherPropertiesAmt", amount))
        val mockData = amount match {
          case "" => OtherPropertiesModel(selection, None)
          case _ => OtherPropertiesModel(selection, Some(BigDecimal(amount)))
        }
        val target = setupTarget(None, Some(mockData), Some(CustomerTypeModel(CustomerTypeKeys.individual)))
        target.submitOtherProperties(fakeRequest)
      }

      "submitting a valid form with 'Yes' and a non-zero amount" should {

        lazy val result = executeTargetWithMockData("Yes", "2100")

        "return a 303" in {
          status(result) shouldBe 303
        }

        "should redirect to the acquisitionDate page" in {
          redirectLocation(result) shouldBe Some(s"${routes.AcquisitionDateController.acquisitionDate()}")
        }
      }

      "submitting a valid form with 'Yes' and a nil amount" should {

        lazy val result = executeTargetWithMockData("Yes", "0")

        "return a 303" in {
          status(result) shouldBe 303
        }

        "should redirect to the annualExemptAmountPage page" in {
          redirectLocation(result) shouldBe Some(s"${routes.AnnualExemptAmountController.annualExemptAmount()}")
        }
      }

      "submitting a valid form with 'No'" should {

        lazy val result = executeTargetWithMockData("No", "")

        "return a 303" in {
          status(result) shouldBe 303
        }

        "should redirect to the acquisitionDate page" in {
          redirectLocation(result) shouldBe Some(s"${routes.AcquisitionDateController.acquisitionDate()}")
        }
      }

      "submitting an form with no data" should {

        lazy val result = executeTargetWithMockData("", "")

        "return a 400" in {
          status(result) shouldBe 400
        }
      }

      "submitting an invalid form with 'Yes' selection and a no amount" should {

        lazy val result = executeTargetWithMockData("Yes", "")

        "return a 400" in {
          status(result) shouldBe 400
        }
      }

      "submitting an invalid form with 'Yes' selection and an amount with three decimal places" should {

        lazy val result = executeTargetWithMockData("Yes", "1000.111")
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 400" in {
          status(result) shouldBe 400
        }

        s"fail with message ${Messages("calc.otherProperties.errorDecimalPlaces")}" in {
          document.getElementsByClass("error-notification").text should include(Messages("calc.otherProperties.errorDecimalPlaces"))
        }
      }

      "submitting an invalid form with 'Yes' selection and a negative amount" should {

        lazy val result = executeTargetWithMockData("Yes", "-1000")
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 400" in {
          status(result) shouldBe 400
        }

        s"fail with message ${Messages("calc.otherProperties.errorNegative")}" in {
          document.getElementsByClass("error-notification").text should include(Messages("calc.otherProperties.errorNegative"))
        }
      }

      "submitting a value which exceeds the maximum numeric" should {

        lazy val result = executeTargetWithMockData("Yes", (Constants.maxNumeric + 0.01).toString)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 400" in {
          status(result) shouldBe 400
        }

        s"fail with message ${Messages("calc.common.error.maxNumericExceeded")}" in {
          document.getElementsByClass("error-notification").text should
            include(Messages("calc.common.error.maxNumericExceeded") + MoneyPounds(Constants.maxNumeric, 0).quantity +
              " " + Messages("calc.common.error.maxNumericExceeded.OrLess"))
        }
      }
    }

    "for a trustee" should {
      def executeTargetWithMockData(selection: String, amount: String): Future[Result] = {
        lazy val fakeRequest = buildRequest(("otherProperties", selection), ("otherPropertiesAmt", amount))
        val mockData = amount match {
          case "" => OtherPropertiesModel(selection, None)
          case _ => OtherPropertiesModel(selection, Some(BigDecimal(amount)))
        }
        val target = setupTarget(None, Some(mockData), Some(CustomerTypeModel(CustomerTypeKeys.trustee)))
        target.submitOtherProperties(fakeRequest)
      }

      "submitting a valid form with 'Yes' selected" should {

        lazy val result = executeTargetWithMockData("Yes", "")

        "return a 303" in {
          status(result) shouldBe 303
        }

        "should redirect to the annual exempt amount page" in {
          redirectLocation(result) shouldBe Some(s"${routes.AnnualExemptAmountController.annualExemptAmount()}")
        }
      }

      "submitting a valid form with 'No' selected" should {

        lazy val result = executeTargetWithMockData("No", "")

        "return a 303" in {
          status(result) shouldBe 303
        }

        "should redirect to the acquisitionDate page" in {
          redirectLocation(result) shouldBe Some(s"${routes.AcquisitionDateController.acquisitionDate()}")
        }
      }
    }

    "for a personal rep" should {
      def executeTargetWithMockData(selection: String, amount: String): Future[Result] = {
        lazy val fakeRequest = buildRequest(("otherProperties", selection), ("otherPropertiesAmt", amount))
        val mockData = amount match {
          case "" => OtherPropertiesModel(selection, None)
          case _ => OtherPropertiesModel(selection, Some(BigDecimal(amount)))
        }
        val target = setupTarget(None, Some(mockData), Some(CustomerTypeModel(CustomerTypeKeys.personalRep)))
        target.submitOtherProperties(fakeRequest)
      }

      "submitting a valid form with 'Yes' selected" should {

        lazy val result = executeTargetWithMockData("Yes", "")

        "return a 303" in {
          status(result) shouldBe 303
        }

        "should redirect to the annual exempt amount page" in {
          redirectLocation(result) shouldBe Some(s"${routes.AnnualExemptAmountController.annualExemptAmount()}")
        }
      }

      "submitting a valid form with 'No' selected" should {

        lazy val result = executeTargetWithMockData("No", "")

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

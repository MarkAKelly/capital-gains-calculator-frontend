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

///*
// * Copyright 2016 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package controllers.CalculationControllerTests
//
//import common.{Constants, KeystoreKeys}
//import connectors.CalculatorConnector
//import play.api.libs.json.Json
//import uk.gov.hmrc.http.cache.client.CacheMap
//import org.mockito.Matchers
//import org.mockito.Mockito._
//import play.api.mvc.AnyContentAsFormUrlEncoded
//import play.api.test.FakeRequest
//import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
//import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
//import org.jsoup._
//import org.scalatest.mock.MockitoSugar
//import assets.MessageLookup.{NonResident => commonMessages}
//import assets.MessageLookup.NonResident.{RebasedValue => messages}
//import scala.concurrent.Future
//import controllers.nonresident.{RebasedValueController, routes}
//import models.nonresident.{AcquisitionDateModel, RebasedValueModel}
//import play.api.mvc.Result
//import uk.gov.hmrc.play.views.helpers.MoneyPounds
//import assets.MessageLookup.NonResident._
//import controllers.helpers.FakeRequestHelper
//
//class RebasedValueSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {
//
//  implicit val hc = new HeaderCarrier()
//
//  def setupTarget(getData: Option[RebasedValueModel],
//                  postData: Option[RebasedValueModel],
//                  acquisitionDateModel: Option[AcquisitionDateModel]
//                 ): RebasedValueController = {
//
//    val mockCalcConnector = mock[CalculatorConnector]
//
//    when(mockCalcConnector.fetchAndGetFormData[RebasedValueModel](Matchers.eq(KeystoreKeys.rebasedValue))(Matchers.any(), Matchers.any()))
//      .thenReturn(Future.successful(getData))
//
//    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
//      .thenReturn(Future.successful(acquisitionDateModel))
//
//    lazy val data = CacheMap("form-id", Map("data" -> Json.toJson(postData.getOrElse(RebasedValueModel("", None)))))
//    when(mockCalcConnector.saveFormData[RebasedValueModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
//      .thenReturn(Future.successful(data))
//
//    new RebasedValueController {
//      override val calcConnector: CalculatorConnector = mockCalcConnector
//    }
//  }
//
//  //GET tests
//  "In CalculationController calling the .rebasedValue action " when {
//
//    "not supplied with a pre-existing stored model and an acquisition date before 6/4/2015" should {
//
//      val target = setupTarget(None, None, Some(AcquisitionDateModel("Yes", Some(5), Some(4), Some(2015))))
//      lazy val result = target.rebasedValue(fakeRequest)
//      lazy val document = Jsoup.parse(bodyOf(result))
//
//      s"route to the mandatory rebased value view with the question ${messages.question}" in {
//        document.title shouldEqual messages.inputQuestionMandatory
//      }
//    }
//
//    "supplied with a pre-existing model with 'Yes' checked and value already entered" should {
//
//      val target = setupTarget(Some(RebasedValueModel("Yes", Some(10000))), None, Some(AcquisitionDateModel("No", None, None, None)))
//      lazy val result = target.rebasedValue(fakeRequest)
//      lazy val document = Jsoup.parse(bodyOf(result))
//
//      "return a 200" in {
//        status(result) shouldBe 200
//      }
//
//      "return some HTML that" should {
//
//        "be pre populated with Yes box selected and a value of 10000 entered" in {
//          document.getElementById("hasRebasedValue-yes").attr("checked") shouldEqual "checked"
//          document.getElementById("rebasedValueAmt").attr("value") shouldEqual "10000"
//        }
//      }
//    }
//
//    "supplied with a pre-existing model with 'No' checked and value not entered" should {
//
//      val target = setupTarget(Some(RebasedValueModel("No", Some(0))), None, Some(AcquisitionDateModel("No", None, None, None)))
//      lazy val result = target.rebasedValue(fakeRequest)
//      lazy val document = Jsoup.parse(bodyOf(result))
//
//      "return a 200" in {
//        status(result) shouldBe 200
//      }
//
//      "return some HTML that" should {
//
//        "be pre populated with No box selected and a value of 0" in {
//          document.getElementById("hasRebasedValue-no").attr("checked") shouldEqual "checked"
//          document.getElementById("rebasedValueAmt").attr("value") shouldEqual "0"
//        }
//      }
//    }
//  }
//
//  //POST Tests
//  "In CalculationController calling the .submitRebasedValue action with no acquisition date" when {
//
//    def buildRequest(body: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST",
//      "/calculate-your-capital-gains/non-resident/rebased-value")
//      .withSession(SessionKeys.sessionId -> "12345")
//      .withFormUrlEncodedBody(body: _*)
//
//    def executeTargetWithMockData(selection: String, amount: String): Future[Result] = {
//      lazy val fakeRequest = buildRequest(("hasRebasedValue", selection), ("rebasedValueAmt", amount))
//      val mockData = amount match {
//        case "" => RebasedValueModel(selection, None)
//        case "fhu39awd8" => RebasedValueModel(selection, None) // required for real number test ONLY
//        case _ => RebasedValueModel(selection, Some(BigDecimal(amount)))
//      }
//      val target = setupTarget(None, Some(mockData), Some(AcquisitionDateModel("No", None, None, None)))
//      target.submitRebasedValue(fakeRequest)
//    }
//
//    "with no acquisition date" should {
//
//      "submitting a valid form with 'Yes' and a value of 12045" should {
//
//        lazy val result = executeTargetWithMockData("Yes", "12045")
//
//        "return a 303" in {
//          status(result) shouldBe 303
//        }
//      }
//
//      "submitting a valid form with 'No' and no value" should {
//
//        lazy val result = executeTargetWithMockData("No", "")
//
//        "return a 303" in {
//          status(result) shouldBe 303
//        }
//      }
//
//      "submitting an invalid form with 'Yes' and a value of 'fhu39awd8'" should {
//
//        lazy val result = executeTargetWithMockData("Yes", "fhu39awd8")
//        lazy val document = Jsoup.parse(bodyOf(result))
//
//        "return a 400" in {
//          status(result) shouldBe 400
//        }
//
//        "return HTML that displays the error message " in {
//          document.select("div#hidden span.error-notification").text shouldEqual commonMessages.errorRealNumber
//        }
//      }
//
//      "submitting an invalid form with 'Yes' and a value of '-200'" should {
//
//        lazy val result = executeTargetWithMockData("Yes", "-200")
//        lazy val document = Jsoup.parse(bodyOf(result))
//
//        "return a 400" in {
//          status(result) shouldBe 400
//        }
//
//        "return HTML that displays the error message " in {
//          document.select("div#hidden span.error-notification").text shouldEqual messages.errorNegative
//        }
//      }
//
//      "submitting an invalid form with 'Yes' and an empty value" should {
//
//        lazy val result = executeTargetWithMockData("Yes", "")
//        lazy val document = Jsoup.parse(bodyOf(result))
//
//        "return a 400" in {
//          status(result) shouldBe 400
//        }
//
//        "return HTML that displays the error message " in {
//          document.select("div#hidden span.error-notification").text shouldEqual messages.errorNoValue
//        }
//      }
//
//      "submitting an invalid form with 'Yes' and a value of 1.111" should {
//
//        lazy val result = executeTargetWithMockData("Yes", "1.111")
//        lazy val document = Jsoup.parse(bodyOf(result))
//
//        "return a 400" in {
//          status(result) shouldBe 400
//        }
//
//        "return HTML that displays the error message " in {
//          document.select("div#hidden span.error-notification").text shouldEqual messages.errorDecimalPlaces
//        }
//      }
//
//      "submitting a value which exceeds the maximum numeric" should {
//
//        lazy val result = executeTargetWithMockData("Yes", (Constants.maxNumeric + 0.01).toString)
//        lazy val document = Jsoup.parse(bodyOf(result))
//
//        "return a 400" in {
//          status(result) shouldBe 400
//        }
//
//        s"fail with message ${messages.errorMaximum(MoneyPounds(Constants.maxNumeric, 0).quantity)}" in {
//          document.getElementsByClass("error-notification").text should
//            include(messages.errorMaximum(MoneyPounds(Constants.maxNumeric, 0).quantity))
//        }
//      }
//    }
//
//    "with an acquisition date before 6/4/2015 (mandatory rebased value view)" should {
//
//        lazy val target = setupTarget(None, None, Some(AcquisitionDateModel("Yes", Some(5), Some(4), Some(2015))))
//
//      "submitting a valid form" should {
//
//        lazy val request = fakeRequestToPOSTWithSession(("hasRebasedValue", "Yes"), ("rebasedValueAmt", "100"))
//        lazy val result = target.submitRebasedValue(request)
//
//        "return a 303" in {
//          status(result) shouldBe 303
//        }
//      }
//
//      "submitting an invalid form" should {
//
//        lazy val request = fakeRequestToPOSTWithSession(("hasRebasedValue", "Yes"), ("rebasedValueAmt", ""))
//        lazy val result = target.submitRebasedValue(request)
//
//        "return a 400" in {
//          status(result) shouldBe 400
//        }
//
//        "return a error message" in {
//          lazy val document = Jsoup.parse(bodyOf(result))
//          document.getElementById("rebasedValueAmt-error-summary").text shouldEqual RebasedValue.errorNoValue
//        }
//      }
//    }
//  }
//}

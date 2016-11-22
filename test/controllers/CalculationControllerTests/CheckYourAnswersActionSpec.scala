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

import constructors.nonresident.AnswersConstructor
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.CheckYourAnswersController
import models.nonresident._
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Mockito._
import assets.MessageLookup.{NonResident => messages}
import connectors.CalculatorConnector

import scala.concurrent.Future
import play.api.test.Helpers._

class CheckYourAnswersActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  def setupTarget(totalGainAnswersModel: TotalGainAnswersModel): CheckYourAnswersController = {

    val mockAnswersConstructor = mock[AnswersConstructor]
    val mockCalcConnector = mock[CalculatorConnector]

    when(mockAnswersConstructor.getNRTotalGainAnswers(Matchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel))

    new CheckYourAnswersController {
      override val answersConstructor: AnswersConstructor = mockAnswersConstructor
      override val calculatorConnector: CalculatorConnector = mockCalcConnector
    }
  }

  val model = TotalGainAnswersModel(DisposalDateModel(5, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(1000),
    DisposalCostsModel(100),
    Some(HowBecameOwnerModel("Gifted")),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(2000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel("Yes", Some(4), Some(10), Some(2013)),
    Some(RebasedValueModel(Some(3000))),
    Some(RebasedCostsModel("Yes", Some(300))),
    ImprovementsModel("Yes", Some(10), Some(20)),
    Some(OtherReliefsModel(30)))

  "Check Your Answers Controller" should {

    "have the correct AnswersConstructor" in {
      CheckYourAnswersController.answersConstructor shouldBe AnswersConstructor
    }
  }

  "Calling .checkYourAnswers" when {

    "provided with a valid session" should {
      val target = setupTarget(model)
      lazy val result = target.checkYourAnswers(fakeRequestWithSession)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the check your answers page" in {
        document.title() shouldBe messages.CheckYourAnswers.question
      }

      "have a back link to the improvements page" in {
        document.select("#back-link").attr("href") shouldBe controllers.nonresident.routes.ImprovementsController.improvements().url
      }
    }

    "provided with an invalid session" should {
      val target = setupTarget(model)
      lazy val result = target.checkYourAnswers(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/session-timeout")
      }
    }
  }

  "Calling .submitCheckYourAnswers" when {

    "provided with a valid session" should {
      val target = setupTarget(model)
      lazy val result = target.submitCheckYourAnswers(fakeRequestWithSession)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the summary page" in {
        redirectLocation(result).get shouldBe controllers.nonresident.routes.SummaryController.summary().url
      }
    }

    "provided with an invalid session" should {
      val target = setupTarget(model)
      lazy val result = target.submitCheckYourAnswers(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/session-timeout")
      }
    }
  }
}

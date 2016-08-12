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

import common.{KeystoreKeys, TestModels}
import connectors.CalculatorConnector
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap
import constructors.nonresident.CalculationElectionConstructor
import models._
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
import controllers.nonresident.{CalculationController, CalculationElectionController, routes}
import models.nonresident.{CalculationElectionModel, CalculationResultModel, OtherReliefsModel, SummaryModel}
import play.api.mvc.Result

class CalculationElectionSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[CalculationElectionModel],
                  postData: Option[CalculationElectionModel],
                  summaryData: SummaryModel,
                  calc: Option[CalculationResultModel] = None,
                  otherReliefsFlat: Option[OtherReliefsModel] = None,
                  otherReliefsTA: Option[OtherReliefsModel] = None,
                  otherReliefsRebased: Option[OtherReliefsModel] = None
                 ): CalculationElectionController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockCalcElectionConstructor = mock[CalculationElectionConstructor]

    when(mockCalcConnector.createSummary(Matchers.any()))
      .thenReturn(summaryData)

    val flatReliefs = otherReliefsFlat match {
      case Some(x) => x.otherReliefs
      case _ => None
    }
    val timeReliefs = otherReliefsTA match {
      case Some(x) => x.otherReliefs
      case _ => None
    }
    val rebasedReliefs = otherReliefsRebased match {
      case Some(x) => x.otherReliefs
      case _ => None
    }

    when(mockCalcElectionConstructor.generateElection(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Seq(
        ("flat", "8000.00", "flat calculation",
          None, routes.CalculationController.otherReliefs().toString(), flatReliefs),
        ("time", "8000.00", "time apportioned calculation",
          Some(Messages("calc.calculationElection.message.timeDate")), routes.OtherReliefsTAController.otherReliefsTA().toString(), timeReliefs),
        ("rebased", "10000.00", "time apportioned calculation",
          Some(Messages("calc.calculationElection.message.timeDate")), routes.OtherReliefsTAController.otherReliefsTA().toString(), rebasedReliefs)
      ))

    when(mockCalcConnector.calculateFlat(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(calc))
    when(mockCalcConnector.calculateTA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(calc))
    when(mockCalcConnector.calculateRebased(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(calc))

    when(mockCalcConnector.fetchAndGetFormData[CalculationElectionModel](Matchers.eq(KeystoreKeys.calculationElection))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](Matchers.eq(KeystoreKeys.otherReliefsFlat))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(otherReliefsFlat))

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](Matchers.eq(KeystoreKeys.otherReliefsTA))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(otherReliefsTA))

    when(mockCalcConnector.fetchAndGetFormData[OtherReliefsModel](Matchers.eq(KeystoreKeys.otherReliefsRebased))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(otherReliefsRebased))

    lazy val data = CacheMap("form-id", Map("data" -> Json.toJson(postData.getOrElse(CalculationElectionModel("")))))
    when(mockCalcConnector.saveFormData[CalculationElectionModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))

    new CalculationElectionController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val calcElectionConstructor: CalculationElectionConstructor = mockCalcElectionConstructor
    }
  }

  // GET Tests
  "In CalculationController calling the .calculationElection action" when {

    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/non-resident/calculation-election").withSession(SessionKeys.sessionId -> "12345")

    "supplied with no pre-existing data" should {

      val target = setupTarget(None, None, TestModels.summaryTrusteeTAWithoutAEA)
      lazy val result = target.calculationElection(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "contain some text and use the character set UTF-8" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        s"have the title '${Messages("calc.calculationElection.question")}'" in {
          document.title shouldEqual Messages("calc.calculationElection.question")
        }

        s"have the heading '${Messages("calc.base.pageHeading")}'" in {
          document.body.getElementsByTag("h1").text shouldEqual Messages("calc.calculationElection.pageHeading")
        }

        s"have the class 'heading-xlarge' on the H1 tag" in {
          document.body.getElementsByTag("h1").hasClass("heading-xlarge") shouldBe true
        }

        s"have a 'Read more' section that" should {

          "have a link to 'https://www.gov.uk/guidance/capital-gains-tax-for-non-residents-calculating-taxable-gain-or-loss'" +
            s"with text '${Messages("calc.calculationElection.link.one")}'" in {
              document.body.getElementById("helpLink1").text shouldEqual s"${Messages("calc.calculationElection.link.one")} ${Messages("calc.base.externalLink")}"
              document.body.getElementById("helpLink1").attr("href") shouldEqual "https://www.gov.uk/guidance/capital-gains-tax-for-non-residents-calculating-taxable-gain-or-loss"
          }
        }

        s"have a 'Back' link to ${routes.AllowableLossesController.allowableLosses}" in {
          document.body.getElementById("back-link").text shouldEqual Messages("calc.base.back")
          document.body.getElementById("back-link").attr("href") shouldEqual routes.AllowableLossesController.allowableLosses.toString()
        }

        s"have the paragraph '${Messages("calc.calculationElection.paragraph.one")}'" in {
          document.body.getElementsByTag("p").text should include (Messages("calc.calculationElection.paragraph.one"))
        }

        s"have a H2 sub-heading with text '${Messages("calc.calculationElection.h2")}'" in {
          document.body.getElementsByTag("h2").text should include (Messages("calc.calculationElection.h2"))
        }

        s"have the paragraph '${Messages("calc.calculationElection.paragraph.two")}'" in {
          document.body.getElementsByTag("p").text should include (Messages("calc.calculationElection.paragraph.two"))
        }

        s"have the paragraph '${Messages("calc.calculationElection.paragraph.three")}'" in {
          document.body.getElementsByTag("p").text should include (Messages("calc.calculationElection.paragraph.three"))
        }

        "have a calculationElectionHelper for the option of a flat calculation rendered on the page" in {
          document.body.getElementById("calculationElection-flat").attr("value") shouldEqual "flat"
          document.body.getElementById("flat-para").text shouldEqual ("Based on " + "flat calculation")
        }

        "display a 'Continue' button " in {
          document.body.getElementById("continue-button").text shouldEqual Messages("calc.base.continue")
        }

        s"display a concertina information box with '${Messages("calc.calculationElection.whyMoreDetails.one")} " +
          s"${Messages("calc.calculationElection.whyMoreDetails.two")}' as the content" in {
          document.select("summary span.summary").text shouldEqual Messages("calc.calculationElection.message.whyMore")
          document.select("div#details-content-0 p").text should include (Messages("calc.calculationElection.whyMoreDetails.one"))
          document.select("div#details-content-0 p").text should include ( Messages("calc.calculationElection.whyMoreDetails.one"))
        }

        "have no pre-selected option" in {
          document.body.getElementById("calculationElection-flat").parent.classNames().contains("selected") shouldBe false
        }
      }
    }

    "supplied with no pre-existing data and no acquisition date" should {

      val target = setupTarget(None, None, TestModels.summaryIndividualFlatWithAEA)
      lazy val result = target.calculationElection(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

    }

    "supplied with no pre-existing data and an acquisition date after tax start date" should {

      val target = setupTarget(None, None, TestModels.summaryIndividualRebasedAcqDateAfter)
      lazy val result = target.calculationElection(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

    }

    "supplied with no pre-existing data and an acquisition date before tax start date" should {

      val target = setupTarget(None, None, TestModels.summaryIndividualRebased)
      lazy val result = target.calculationElection(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

    }

    "supplied with no pre-existing data and a None rebased value" should {

      val target = setupTarget(None, None, TestModels.summaryIndividualImprovementsNoRebasedModel)
      lazy val result = target.calculationElection(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

    }

    "supplied with no pre-existing data and a rebased value with no acquisition date" should {

      val target = setupTarget(None, None, TestModels.summaryIndividualRebasedNoAcqDate)
      lazy val result = target.calculationElection(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

    }

    "supplied with pre-existing data and a value for flat, time and rebased reliefs" should {

      val target = setupTarget(
        Some(CalculationElectionModel("flat")),
        None,
        TestModels.summaryTrusteeTAWithoutAEA,
        None,
        Some(OtherReliefsModel(None, Some(500))),
        Some(OtherReliefsModel(None, Some(600))),
        Some(OtherReliefsModel(None, Some(700)))
      )
      lazy val result = target.calculationElection(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "have the stored value of flat calculation selected" in {
          document.body.getElementById("calculationElection-flat").parent.classNames().contains("selected") shouldBe true
        }

        "have the button text '£500.00' under flat calc details" in {
          document.body.select("#flat-button").text shouldEqual "£500.00"
        }

        "have the button text '£600.00' under time calc details" in {
          document.body.select("#time-button").text shouldEqual "£600.00"
        }

        s"have the button text '£500.00' under rebased calc details" in {
          document.body.select("#rebased-button").text shouldEqual "£700.00"
        }
      }
    }

    "supplied with pre-existing data and no values for flat, time and rebased reliefs" should {

      val target = setupTarget(
        Some(CalculationElectionModel("flat")),
        None,
        TestModels.summaryTrusteeTAWithoutAEA
      )
      lazy val result = target.calculationElection(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "have the stored value of flat calculation selected" in {
          document.body.getElementById("calculationElection-flat").parent.classNames().contains("selected") shouldBe true
        }

        s"have the button text '${Messages("calc.calculationElection.otherRelief")}' under flat calc details" in {
          document.body.select("#flat-button").text shouldEqual Messages("calc.calculationElection.otherRelief")
        }

        s"have the button text '${Messages("calc.calculationElection.otherRelief")}' under time calc details" in {
          document.body.select("#time-button").text shouldEqual Messages("calc.calculationElection.otherRelief")
        }

        s"have the button text '${Messages("calc.calculationElection.otherRelief")}' under rebased calc details" in {
          document.body.select("#rebased-button").text shouldEqual Messages("calc.calculationElection.otherRelief")
        }
      }
    }
  }

  "In CalculationController calling the .submitCalculationElection action" when {

    def buildRequest(body: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST",
      "/calculate-your-capital-gains/non-resident/calculation-election")
      .withSession(SessionKeys.sessionId -> "12345")
      .withFormUrlEncodedBody(body: _*)

    def executeTargetWithMockData
    (
      data: String,
      calc: Option[CalculationResultModel],
      summary: SummaryModel,
      action: String
    ): Future[Result] = {
      lazy val fakeRequest = buildRequest(("calculationElection", data), ("action",action))
      val mockData = new CalculationElectionModel(data)
      val target = setupTarget(None, Some(mockData), summary, calc)
      target.submitCalculationElection(fakeRequest)
    }

    "submitting form via Other Reliefs Flat button" should {

      lazy val result = executeTargetWithMockData("flat", Some(TestModels.calcModelOneRate), TestModels.summaryTrusteeTAWithoutAEA, "flat")

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the other reliefs page" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherReliefsFlatController.otherReliefsFlat()}")
      }
    }

    "submitting form via Other Reliefs Time Apportioned button" should {

      lazy val result = executeTargetWithMockData("flat", Some(TestModels.calcModelOneRate), TestModels.summaryTrusteeTAWithoutAEA, "time")

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Other Reliefs Time Apportioned page" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherReliefsTAController.otherReliefsTA()}")
      }
    }

    "submitting form via Other Reliefs Rebased button" should {

      lazy val result = executeTargetWithMockData("flat", Some(TestModels.calcModelOneRate), TestModels.summaryTrusteeTAWithoutAEA, "rebased")

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Other Reliefs Rebased page" in {
        redirectLocation(result) shouldBe Some(s"${routes.OtherReliefsRebasedController.otherReliefsRebased()}")
      }
    }

    "submitting a valid form with 'flat' selected" should {

      lazy val result = executeTargetWithMockData("flat", Some(TestModels.calcModelOneRate), TestModels.summaryTrusteeTAWithoutAEA, "continue")

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary page" in {
        redirectLocation(result) shouldBe Some(s"${routes.CalculationController.summary}")
      }
    }

    "submitting a valid form with 'time' selected" should {

      lazy val result = executeTargetWithMockData("time", Some(TestModels.calcModelOneRate), TestModels.summaryIndividualAcqDateAfter, "continue")

      "return a 303" in {
        status(result) shouldBe 303
      }
    }

    "submitting a valid form with 'rebased' selected" should {

      lazy val result = executeTargetWithMockData("rebased", Some(TestModels.calcModelOneRate), TestModels.summaryIndividualFlatWithAEA, "continue")

      "return a 303" in {
        status(result) shouldBe 303
      }
    }

    "submitting a form with no data" should  {

      lazy val result = executeTargetWithMockData("", Some(TestModels.calcModelOneRate), TestModels.summaryIndividualImprovementsWithRebasedModel, "continue")
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "display a visible Error Summary field" in {
        document.getElementById("error-summary-display").hasClass("error-summary--show")
      }

      "link to the invalid input box in Error Summary" in {
        document.getElementById("calculationElection-error-summary").attr("href") should include ("#calculationElection")
      }
    }

    "submitting a form with completely unrelated 'ew1234qwer'" should  {

      lazy val result = executeTargetWithMockData("ew1234qwer", Some(TestModels.calcModelOneRate), TestModels.summaryIndividualImprovementsNoRebasedModel, "continue")

      "return a 400" in {
        status(result) shouldBe 400
      }
    }

    "submitting an invalid form with an acquisition date after the tax start date" should {
      lazy val result = executeTargetWithMockData("", Some(TestModels.calcModelOneRate), TestModels.summaryIndividualAcqDateAfter, "continue")

      "return a 400" in {
        status(result) shouldBe 400
      }
    }

    "submitting an invalid form with no acquisition date" should {
      lazy val result = executeTargetWithMockData("", Some(TestModels.calcModelOneRate), TestModels.summaryIndividualFlatWithoutAEA, "continue")

      "return a 400" in {
        status(result) shouldBe 400
      }
    }

    "submitting an invalid form with none rebased value" should {
      lazy val result = executeTargetWithMockData("", Some(TestModels.calcModelOneRate), TestModels.summaryIndividualImprovementsNoRebasedModel, "continue")

      "return a 400" in {
        status(result) shouldBe 400
      }
    }

    "submitting an invalid form with a rebased value and no acquisition date" should {
      lazy val result = executeTargetWithMockData("", Some(TestModels.calcModelOneRate), TestModels.summaryIndividualRebasedNoAcqDateOrRebasedCosts, "continue")

      "return a 400" in {
        status(result) shouldBe 400
      }
    }

    "submitting an invalid form with a rebased value and an acquisition date after tax start" should {
      lazy val result = executeTargetWithMockData("", Some(TestModels.calcModelOneRate), TestModels.summaryIndividualRebasedAcqDateAfter, "continue")

      "return a 400" in {
        status(result) shouldBe 400
      }
    }
  }
}

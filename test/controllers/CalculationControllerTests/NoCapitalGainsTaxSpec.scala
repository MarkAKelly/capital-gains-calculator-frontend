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

import common.KeystoreKeys
import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import controllers.nonresident.CalculationController
import models.nonresident.DisposalDateModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class NoCapitalGainsTaxSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[DisposalDateModel]): CalculationController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockCalcElectionConstructor = mock[CalculationElectionConstructor]

    when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](Matchers.eq(KeystoreKeys.disposalDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    new CalculationController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val calcElectionConstructor: CalculationElectionConstructor = mockCalcElectionConstructor

    }
  }

  //GET Tests
  "In CalculationController calling the .noCapitalGainsTax action " should {

    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/non-resident/no-capital-gains-tax").withSession(SessionKeys.sessionId -> "12345")

    "when supplied with a model for the date 01 January 2015" should {

      val target = setupTarget(Some(DisposalDateModel(1, 1, 2015)))
      lazy val result = target.noCapitalGainsTax(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "have the title 'You have no tax to pay'" in {
          document.title shouldEqual Messages("nocgt.invaliddate.title")
        }

        "have the heading 'You have no tax to pay'" in {
          document.body.getElementsByTag("h1").text shouldEqual Messages("nocgt.invaliddate.title")
        }
        "Contain the content " +
          "'This is because Capital Gains Tax for non-residents only applies to properties which were sold or given away after 5 April 2015.'" in {
          document.body.select("article p").text should include("This is because Capital Gains Tax for non-residents " +
            "only applies to properties which were sold or given away after 5 April 2015.")
        }
        "Contain the content 'You've told us that you sold or gave away the property on'" in {
          document.body.select("article p").text should include("You've told us that you sold or gave away the property on")
        }

        "should contain a Read more sidebar with a link to CGT allowances" in {
          document.select("aside h2").text shouldBe Messages("calc.common.readMore")
          document.select("aside a").first.text shouldBe s"${Messages("nocgt.sidebar.linkone")} ${Messages("calc.base.externalLink")}"
        }

        "should contain a change link to the disposal date page" in {
          document.select("a#change-link").text shouldBe Messages("nocgt.content.change")
        }

        "should display a date of 1 January 2015" in {
          document.select("span.bold-small").text shouldEqual "1 January 2015"
        }
      }
    }

    "when supplied with a model for the date 13 Decemeber 2014" should {

      val target = setupTarget(Some(DisposalDateModel(13, 12, 2014)))
      lazy val result = target.noCapitalGainsTax(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "should display a date of 13 December 2014" in {
        document.select("span.bold-small").text shouldEqual "13 December 2014"
      }
    }
  }
}


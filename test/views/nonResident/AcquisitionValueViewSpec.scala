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

package views.nonResident

import assets.MessageLookup.NonResident
import assets.MessageLookup.NonResident.AcquisitionValue
import common.KeystoreKeys
import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import controllers.nonresident.{AcquisitionValueController, routes}
import models.nonresident.{AcquisitionDateModel, AcquisitionValueModel}
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.SessionKeys
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Mockito._
import play.api.test.Helpers._
import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{AcquisitionValue => messages}
import scala.concurrent.Future

/**
  * Created by emma on 31/10/16.
  */
class AcquisitionValueViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  def setupTarget(
                   getData: Option[AcquisitionValueModel],
                   postData: Option[AcquisitionValueModel],
                   acquisitionDateModel: Option[AcquisitionDateModel] = None): AcquisitionValueController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockCalcElectionConstructor = mock[CalculationElectionConstructor]

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionValueModel](Matchers.eq(KeystoreKeys.acquisitionValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(acquisitionDateModel))

    new AcquisitionValueController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val calcElectionConstructor: CalculationElectionConstructor = mockCalcElectionConstructor
    }
  }
  "return some HTML that" should {

    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/non-resident/acquisition-value").withSession(SessionKeys.sessionId -> "12345")
    lazy val target = setupTarget(None, None)
    lazy val result = target.acquisitionValue(fakeRequest)
    lazy val document = Jsoup.parse(bodyOf(result))

    "contain some text and use the character set utf-8" in {
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "have the title 'How much did you pay for the property?'" in {
      document.title shouldEqual messages.question
    }

    s"have the heading ${commonMessages.pageHeading}" in {
      document.body.getElementsByTag("h1").text shouldEqual commonMessages.pageHeading
    }

    s"have a 'Back' link to ${routes.AcquisitionDateController.acquisitionDate()}" in {
      document.body.getElementById("back-link").text shouldEqual commonMessages.back
      document.body.getElementById("back-link").attr("href") shouldEqual routes.AcquisitionDateController.acquisitionDate().toString()
    }

    "have the question 'How much did you pay for the property?'" in {
      document.body.getElementsByTag("label").text should include (messages.question)
    }

    "have the bullet list content title and content" in {
      document.select("p#bullet-list-title").text shouldEqual messages.bulletTitle
    }

    "Have the bullet content" in {
      document.select("ul li").text should include(messages.bulletOne)
      document.select("ul li").text should include(messages.bulletTwo)
      document.select("ul li").text should include(messages.bulletThree)
      document.select("ul li").text should include(messages.bulletFour)
      document.select("ul li").text should include(messages.bulletFive)
    }
    "have a link with a hidden external link field" in {
      document.select("ul li a#lossesLink").text should include(messages.bulletLink)
      document.select("span#opensInANewTab").text shouldEqual commonMessages.externalLink
    }
    "display an input box for the Acquisition Value" in {
      document.body.getElementById("acquisitionValue").tagName shouldEqual "input"
    }
    "have no value auto-filled into the input box" in {
      document.getElementById("acquisitionValue").attr("value") shouldEqual ""
    }
    "display a 'Continue' button " in {
      document.body.getElementById("continue-button").text shouldEqual commonMessages.continue
    }

  }
}
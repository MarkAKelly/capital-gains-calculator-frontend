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

import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.nonresident.WorthBeforeLegislationStartController
import models.nonresident.WorthBeforeLegislationStartModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.http.HeaderCarrier
import assets.MessageLookup.{NonResident => commonMessages}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class WorthBeforeLegislationStartActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  implicit val hc = new HeaderCarrier()

  def setUpTarget(getData: Option[WorthBeforeLegislationStartModel]): WorthBeforeLegislationStartController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[WorthBeforeLegislationStartModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    new WorthBeforeLegislationStartController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "WorthBeforeLegislationStartController" when{

    s"have a session timeout home link of '${controllers.nonresident.routes.DisposalDateController.disposalDate().url}'" in {
      WorthBeforeLegislationStartController.homeLink shouldEqual controllers.nonresident.routes.DisposalDateController.disposalDate().url
    }

    "calling .worthBeforeLegislationStart" should {

      "with no pre-existing model" should {
        val target = setUpTarget(None)
        lazy val result = target.worthBeforeLegislationStart(fakeRequestWithSession)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        s"have a title of ${commonMessages.WorthBeforeLegislationStart}" in {
          document.title shouldBe commonMessages.WorthBeforeLegislationStart
        }

      }
    }
  }

}

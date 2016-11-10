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
import controllers.helpers.FakeRequestHelper
import models.nonresident.SellOrGiveAwayModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class SoldOrGivenAwayActionSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  implicit val hc = new HeaderCarrier()

//  def setUpTarget(getData: Option[SellOrGiveAwayModel]): SellOrGiveAwayController = {
//
//    val mockCalcConnector = mock[CalculatorConnector]
//
//    when(mockCalcConnector.fetchAndGetFormData[SellOrGiveAwayModel](Matchers.eq(KeystoreKeys.sellOrGiveAway))(Matchers.any(), Matchers.any()))
//      .thenReturn(Future.successful(getData))
//
//    new SellOrGiveAwayController = {
//      override val calcConnector: CalculatorConnector = mockCalcConnector
//    }
//  }
//
//  //GET Tests
//
//  "Calling the SellOrGiveAway .sellOrGiveAway" when {
//
//    "not supplied with a pre-existing model" should {
//      val target = setUpTarget(None)
//      lazy val result = target.sellOrGiveAway(fakeRequestWithSession)
//      lazy val document = Jsoup.parse(bodyOf(result))
//
//      "return a 200 response" in {
//
//      }
//
//      s"have the title of ${SellOrGiveAway.title}" in {
//
//      }
//    }
//  }

}

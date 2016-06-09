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

package controllers

import java.util.UUID
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Action}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.SessionKeys
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class TimeoutControllerSpec extends UnitSpec with WithFakeApplication {

  class fakeRequestTo(url : String, controllerAction : Action[AnyContent]) {
    val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/" + url)
    val result = controllerAction(fakeRequest)
    val jsoupDoc = Jsoup.parse(bodyOf(result))
  }

//  class fakeRequestToWithSessionId(url : String, controllerAction : Action[AnyContent]) {
//    val sessionId = UUID.randomUUID.toString
//    val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/" + url).withSession(SessionKeys.sessionId -> s"session-$sessionId")
//    val result = controllerAction(fakeRequest)
//    val jsoupDoc = Jsoup.parse(bodyOf(result))
//  }

  "TimeoutController.timeout" should {

    "when called with no session" should {

      object timeoutTestDataItem extends fakeRequestTo("", TimeoutController.timeout)

      "return a 200" in {
        status(timeoutTestDataItem.result) shouldBe 200
      }

      "and have the title 'Hard coded title'" in {
        timeoutTestDataItem.jsoupDoc.title() shouldBe "Hard coded title"
      }
    }

//
//    "when called with a session" should {
//
//      object IntroductionWithSessionTestDataItem extends fakeRequestToWithSessionId("", StartController.start)
//
//      "return a 303" in {
//        status(IntroductionWithSessionTestDataItem.result) shouldBe 303
//      }
//
//    }
  }
}
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

import controllers.helpers.FakeRequestHelper
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class StartControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  "Calling the .start method" should {
    val result = StartController.start(fakeRequest)

    "return a 303" in {
      status(result) shouldBe 303
    }

    "redirect to the customer type page" in {
      redirectLocation(result) shouldBe Some(nonresident.routes.CustomerTypeController.customerType().url)
    }
  }
}
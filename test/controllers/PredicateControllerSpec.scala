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

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import controllers.predicates._
import play.api.test.FakeRequest
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.SessionKeys

import scala.concurrent.Future

class PredicateControllerSpec extends UnitSpec with WithFakeApplication with FrontendController with FeatureLock {

  object FeatureLockForTrue extends FeatureLock {
    override val featureEnabled = true
  }
  object FeatureLockForFalse extends FeatureLock {
    override val featureEnabled = false
  }

  val featureLockTestTrue = FeatureLockForTrue.async { implicit request =>
    Future.successful(Ok("Hello"))
  }

  val featureLockTestTrueNoTimeout = FeatureLockForTrue.asyncNoTimeout { implicit request =>
    Future.successful(Ok("Hello"))
  }

  val featureLockTestFalse = FeatureLockForFalse.async { implicit request =>
    Future.successful(Ok("Hello"))
  }

  val featureLockTestFalseNoTimeout = FeatureLockForFalse.asyncNoTimeout { implicit request =>
    Future.successful(Ok("Hello"))
  }

  class fakeRequestTo(url : String, controllerAction : Action[AnyContent]) {
    val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/" + url).withSession(SessionKeys.sessionId -> "12345")
    val result = controllerAction(fakeRequest)
  }

  "Calling FeatureLockTestController.featureLockTestTrue" should {

    object featureLockTestDataItem extends fakeRequestTo("", featureLockTestTrue)

    "return status ok (200)" in {
      status(featureLockTestDataItem.result) shouldBe 200
    }

  }

  "Calling FeatureLockTestController.featureLockTestTrueNoTimeout" should {

    object featureLockTestDataItem extends fakeRequestTo("", featureLockTestTrueNoTimeout)

    "return status ok (200)" in {
      status(featureLockTestDataItem.result) shouldBe 200
    }

  }

  "Calling FeatureLockTestController.featureLockTestFalse" should {

    object featureLockTestDataItem extends fakeRequestTo("", featureLockTestFalse)

    "return status NotFound (404)" in {
      status(featureLockTestDataItem.result) shouldBe 404
    }

  }

  "Calling FeatureLockTestController.featureLockTestFalseNoTimeout" should {

    object featureLockTestDataItem extends fakeRequestTo("", featureLockTestFalseNoTimeout)

    "return status NotFound (404)" in {
      status(featureLockTestDataItem.result) shouldBe 404
    }

  }
}
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

package controllers.features

import config.AppConfig
import controllers.routes
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.SessionKeys

import scala.concurrent.Future

trait FeatureLock extends FrontendController with AppConfig{

  private type PlayRequest = Request[AnyContent] => Result
  private type AsyncPlayRequest = Request[AnyContent] => Future[Result]

  class FeatureLockFor(condition: Boolean) {

    def async(action: AsyncPlayRequest): Action[AnyContent] = {
      Action.async { implicit request =>
        if (condition && request.session.get(SessionKeys.sessionId).isDefined) {
          action(request)
        }
        else {
          Future.successful(Redirect(routes.StartController.start()))
        }
      }
    }

    def apply(action: PlayRequest): Action[AnyContent] = async(request => Future.successful(action(request)))
  }

  object FeatureLockForRTT extends FeatureLockFor(RTTCondition)
  object FeatureLockForNRCGT extends FeatureLockFor(NRCGTCondition)

  lazy val RTTCondition = featureRTTEnabled
  val NRCGTCondition = true
}

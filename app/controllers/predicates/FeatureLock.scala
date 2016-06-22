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

package controllers.predicates

import config.ApplicationConfig
import controllers.routes
import play.api.mvc.{Action, Result, AnyContent, Request}

import scala.concurrent.Future

trait FeatureLock extends ValidActiveSession {

  private type PlayRequest = Request[AnyContent] => Result
  private type AsyncPlayRequest = Request[AnyContent] => Future[Result]

  class FeatureLockFor(condition: Boolean) {

    def async(action: AsyncPlayRequest): Action[AnyContent] = {
      ValidateSession.async { implicit request =>
        if (condition) {
          action(request)
        }
        else {
          Future.successful(NotFound)
        }
      }
    }

    def asyncNoTimeout(action: AsyncPlayRequest): Action[AnyContent] = {
      Action.async { implicit request =>
        if (condition) {
          action(request)
        }
        else {
          Future.successful(NotFound)
        }
      }
    }
  }

  object FeatureLockForRTT extends FeatureLockFor(RTTCondition)

  lazy val RTTCondition = ApplicationConfig.featureRTTEnabled
}

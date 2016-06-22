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

package controllers.resident

import java.util.UUID

import controllers.predicates.FeatureLock
import uk.gov.hmrc.play.http.SessionKeys
import scala.concurrent.Future
import views.html.calculation.{resident => views}

object GainController extends GainController

trait GainController extends FeatureLock {

  val disposalDate = FeatureLockForRTT.asyncNoTimeout { implicit request =>
    if (request.session.get(SessionKeys.sessionId).isEmpty) {
      val sessionId = UUID.randomUUID.toString
      Future.successful(Ok(views.disposalDate()).withSession(request.session + (SessionKeys.sessionId -> s"session-$sessionId")))
    }
    else {
      Future.successful(Ok(views.disposalDate()))
    }
  }

  val disposalValue = FeatureLockForRTT.async { implicit request =>
    Future.successful(Ok(views.disposalValue()))
  }

  val acquisitionValue = FeatureLockForRTT.async { implicit request =>
    Future.successful(Ok(""))
  }
  val disposalCosts = FeatureLockForRTT.async { implicit request =>
    Future.successful(Ok(views.disposalCosts()))
  }
}

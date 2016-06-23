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

import common.nonresident.KeystoreKeys
import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import play.api.mvc.Action
import uk.gov.hmrc.play.http.SessionKeys

import scala.concurrent.Future
import views.html.calculation.{resident => views}
import forms.resident.DisposalDateForm._
import models.resident.DisposalDateModel


object GainController extends GainController {
  val calcConnector = CalculatorConnector
}

trait GainController extends FeatureLock {

  val calcConnector: CalculatorConnector

  val disposalDate = FeatureLockForRTT.asyncNoTimeout { implicit request =>
    if (request.session.get(SessionKeys.sessionId).isEmpty) {
      val sessionId = UUID.randomUUID.toString
      Future.successful(Ok(views.disposalDate(disposalDateForm)).withSession(request.session + (SessionKeys.sessionId -> s"session-$sessionId")))
    }
    else {
      calcConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.ResidentKeys.disposalDate).map{
        case Some(data) => Ok(views.disposalDate(disposalDateForm.fill(data)))
        case None => Ok(views.disposalDate(disposalDateForm))
      }
    }
  }

  val submitDisposalDate = Action.async { implicit request =>
    disposalDateForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.disposalDate(errors))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.ResidentKeys.disposalDate, success)
        Future.successful(Redirect(routes.GainController.disposalValue()))}
    )
  }

  val disposalValue = FeatureLockForRTT.async { implicit request =>
    Future.successful(Ok(views.disposalValue()))
  }

  val acquisitionValue = FeatureLockForRTT.async { implicit request =>
    Future.successful(Ok(views.acquisitionValue()))
  }

  val acquisitionCosts = TODO
  
  val disposalCosts = FeatureLockForRTT.async { implicit request =>
    Future.successful(Ok(views.disposalCosts()))
  }
}

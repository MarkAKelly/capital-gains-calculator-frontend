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

import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import play.api.mvc.Action
import uk.gov.hmrc.play.http.SessionKeys

import scala.concurrent.Future
import views.html.calculation.{resident => views}
import forms.resident.DisposalValueForm._
import forms.resident.DisposalDateForm._
import forms.resident.DisposalCostsForm._
import forms.resident.AcquisitionValueForm._
import forms.resident.AcquisitionCostsForm._
import models.resident.{AcquisitionValueModel, DisposalDateModel, AcquisitionCostsModel, DisposalValueModel, DisposalCostsModel}


object GainController extends GainController {
  val calcConnector = CalculatorConnector
}

trait GainController extends FeatureLock {

  val calcConnector: CalculatorConnector

  //################# Disposal Date Actions ####################
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

  //################ Disposal Value Actions ######################
  val disposalValue = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisposalValueModel](KeystoreKeys.ResidentKeys.disposalValue).map {
      case Some(data) => Ok(views.disposalValue(disposalValueForm.fill(data)))
      case None => Ok(views.disposalValue(disposalValueForm))
    }
  }

  val submitDisposalValue = FeatureLockForRTT.async { implicit request =>
    disposalValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.disposalValue(errors))),
      success => {
        calcConnector.saveFormData[DisposalValueModel](KeystoreKeys.ResidentKeys.disposalValue, success)
        Future.successful(Redirect(routes.GainController.disposalCosts()))}
    )
  }

  //################# Disposal Costs Actions ########################
  val disposalCosts = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisposalCostsModel](KeystoreKeys.ResidentKeys.disposalCosts).map {
      case Some(data) => Ok(views.disposalCosts(disposalCostsForm.fill(data)))
      case None => Ok(views.disposalCosts(disposalCostsForm))
    }
  }

  val submitDisposalCosts = FeatureLockForRTT.async { implicit request =>
    disposalCostsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.disposalCosts(errors))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.ResidentKeys.disposalCosts, success)
        Future.successful(Redirect(routes.GainController.acquisitionValue()))}
    )
  }

  //################# Acquisition Value Actions ########################
  val acquisitionValue = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[AcquisitionValueModel](KeystoreKeys.ResidentKeys.acquisitionValue).map {
      case Some(data) => Ok(views.acquisitionValue(acquisitionValueForm.fill(data)))
      case None => Ok(views.acquisitionValue(acquisitionValueForm))
    }
  }

  val submitAcquisitionValue = FeatureLockForRTT.async { implicit request =>
    acquisitionValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.acquisitionValue(errors))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.ResidentKeys.acquisitionValue, success)
        Future.successful(Redirect(routes.GainController.acquisitionCosts()))}
    )
  }

  //################# Acquisition Costs Actions ########################
  val acquisitionCosts = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[AcquisitionCostsModel](KeystoreKeys.ResidentKeys.acquisitionCosts).map {
      case Some(data) => Ok(views.acquisitionCosts(acquisitionCostsForm.fill(data)))
      case None => Ok(views.acquisitionCosts(acquisitionCostsForm))
    }
  }

  val submitAcquisitionCosts = FeatureLockForRTT.async { implicit request =>
    acquisitionCostsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.acquisitionCosts(errors))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.ResidentKeys.acquisitionCosts, success)
        Future.successful(Redirect(routes.GainController.improvements()))}
    )
  }



  val improvements = Action.async { implicit request =>
    Future.successful(Ok(views.improvements()))
  }
}
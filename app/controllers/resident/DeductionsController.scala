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

import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import models.resident.{LossesBroughtForwardModel, OtherPropertiesModel, AllowableLossesModel, ReliefsModel, ReliefsValueModel}
import forms.resident.ReliefsValueForm._
import forms.resident.LossesBroughtForwardForm._
import play.api.mvc.{Action, Result}
import views.html.calculation.{resident => views}
import forms.resident.ReliefsForm._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future


object DeductionsController extends DeductionsController {
  val calcConnector = CalculatorConnector
}

trait DeductionsController extends FeatureLock {

  val calcConnector: CalculatorConnector

  //################# Reliefs Actions ########################
  val reliefs = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[ReliefsModel](KeystoreKeys.ResidentKeys.reliefs).map {
      case Some(data) => Ok(views.reliefs(reliefsForm.fill(data)))
      case None => Ok(views.reliefs(reliefsForm))
    }
  }

  val submitReliefs = Action.async {implicit request =>
    reliefsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.reliefs(errors))),
      success => {
        calcConnector.saveFormData[ReliefsModel](KeystoreKeys.ResidentKeys.reliefs, success)
        success match {
          case ReliefsModel("Yes") => Future.successful(Redirect(routes.DeductionsController.reliefsValue()))
          case _ => Future.successful(Redirect(routes.DeductionsController.otherProperties()))
        }
      }
    )
  }

  //################# Reliefs Value Input Actions ########################

  val reliefsValue = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[ReliefsValueModel](KeystoreKeys.ResidentKeys.reliefsValue).map {
      case Some(data) => Ok(views.reliefsValue(reliefsValueForm.fill(data)))
      case None => Ok(views.reliefsValue(reliefsValueForm))
    }
  }

  val submitReliefsValue = FeatureLockForRTT.async { implicit request =>
    reliefsValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.reliefsValue(errors))),
      success => {
        calcConnector.saveFormData[ReliefsValueModel](KeystoreKeys.ResidentKeys.reliefsValue, success)
        Future.successful(Redirect(routes.DeductionsController.otherProperties()))
      }
    )
  }

  //################# Other Properties Actions #########################
  val otherProperties = Action.async { implicit request =>
    Future.successful(Ok(views.otherProperties()))
  }

  //################# Allowable Losses Actions #########################
  val allowableLosses = Action.async { implicit request =>
    Future.successful(Ok(views.allowableLosses()))
  }

  //################# Allowable Losses Value Actions ############################
  val allowableLossesValue = Action.async { implicit request =>
    Future.successful(Ok(views.allowableLossesValue()))
  }

  //################# Brought Forward Losses Actions ############################

  def lossesBroughtForwardBackUrlOtherProps(implicit hc: HeaderCarrier): Boolean = {
    calcConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.ResidentKeys.otherProperties).map {
      case Some(OtherPropertiesModel(false)) => false
      case Some(OtherPropertiesModel(true)) => true
    }
  }

  def lossesBroughtForwardBackUrlAllowableLosses(implicit hc: HeaderCarrier): Boolean ={
    calcConnector.fetchAndGetFormData[AllowableLossesModel](KeystoreKeys.ResidentKeys.allowableLosses).map {
      case Some(AllowableLossesModel(false)) => false
      case Some(AllowableLossesModel(true)) => true
    }
  }

  def lossesBroughtForwardBackUrl(implicit hc: HeaderCarrier): Future[String] = {

    for {
      otherPropertiesCheck <- lossesBroughtForwardBackUrlOtherProps
      allowableLossesCheck <- lossesBroughtForwardBackUrlAllowableLosses
    } yield (otherPropertiesCheck, allowableLossesCheck)
        
    match {
      case (false, _) => routes.DeductionsController.otherProperties().url
      case (true, false) => routes.DeductionsController.allowableLosses().url
      case (true, true) => routes.DeductionsController.allowableLossesValue().url
    }
  }

  val lossesBroughtForward = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String): Future[Result] = {
      calcConnector.fetchAndGetFormData[LossesBroughtForwardModel](KeystoreKeys.ResidentKeys.lossesBroughtForward).map {
        case Some(data) => Ok(views.lossesBroughtForward(lossesBroughtForwardForm.fill(data)))
        case None => Ok(views.lossesBroughtForward(lossesBroughtForwardForm))
      }
    }
    for {
      backUrl <- lossesBroughtForwardBackUrl
      finalResult <- routeRequest(backUrl)
    } yield finalResult
  }


  //################# Brought Forward Losses Value Actions ##############################
  val lossesBroughtForwardValue = Action.async { implicit request =>
    Future.successful(Ok(views.lossesBroughtForwardValue()))
  }

  //################# Annual Exempt Amount Input Actions #############################

  //################# Second Summary Actions ###############################

}
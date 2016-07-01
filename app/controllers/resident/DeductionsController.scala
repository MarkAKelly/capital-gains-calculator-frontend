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
import models.resident._
import forms.resident.LossesBroughtForwardValueForm._
import forms.resident.OtherPropertiesForm._
import forms.resident.ReliefsForm._
import forms.resident.AllowableLossesForm._
import forms.resident.ReliefsValueForm._
import forms.resident.AllowableLossesValueForm._
import models.resident.AllowableLossesValueModel
import play.api.mvc.{Action, Result}
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation.{resident => views}
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

  val submitReliefs = FeatureLockForRTT.async { implicit request =>
    reliefsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.reliefs(errors))),
      success => {
        calcConnector.saveFormData[ReliefsModel](KeystoreKeys.ResidentKeys.reliefs, success)
        success match {
          case ReliefsModel(true) => Future.successful(Redirect(routes.DeductionsController.reliefsValue()))
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
  def otherPropertiesBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    calcConnector.fetchAndGetFormData[ReliefsModel](KeystoreKeys.ResidentKeys.reliefs).flatMap {
      case Some(ReliefsModel(true)) => Future.successful(routes.DeductionsController.reliefsValue().url)
      case _ => Future.successful(routes.DeductionsController.reliefs().url)
    }
  }

  val otherProperties = FeatureLockForRTT.async { implicit request =>

    def routeRequest(backUrl: String): Future[Result] = {
      calcConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.ResidentKeys.otherProperties).map {
        case Some(data) => Ok(views.otherProperties(otherPropertiesForm.fill(data), backUrl))
        case None => Ok(views.otherProperties(otherPropertiesForm, backUrl))
      }
    }

    for {
      backUrl <- otherPropertiesBackUrl
      finalResult <- routeRequest(backUrl)
    } yield finalResult
  }

  val submitOtherProperties = FeatureLockForRTT.async { implicit request =>

    def routeRequest(backUrl: String): Future[Result] = {
      otherPropertiesForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(views.otherProperties(errors, backUrl))),
        success => {
          calcConnector.saveFormData[OtherPropertiesModel](KeystoreKeys.ResidentKeys.otherProperties, success)
          success match {
            case OtherPropertiesModel(true) => Future.successful(Redirect(routes.DeductionsController.allowableLosses()))
            case _ => Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
          }
        }
      )
    }
    for {
      backUrl <- otherPropertiesBackUrl
      route <- routeRequest(backUrl)
    } yield route
  }

  //################# Allowable Losses Actions #########################
  val allowableLosses = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[AllowableLossesModel](KeystoreKeys.ResidentKeys.allowableLosses).map {
      case Some(data) => Ok(views.allowableLosses(allowableLossesForm.fill(data)))
      case None => Ok(views.allowableLosses(allowableLossesForm))
    }
  }

  val submitAllowableLosses = FeatureLockForRTT.async { implicit request =>
    allowableLossesForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.allowableLosses(errors))),
      success => {
        calcConnector.saveFormData[AllowableLossesModel](KeystoreKeys.ResidentKeys.allowableLosses, success)
        if (success.isClaiming) {
          Future.successful(Redirect(routes.DeductionsController.allowableLossesValue()))
        }
        else {
          Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
        }
      }
    )
  }

  //################# Allowable Losses Value Actions ############################
  val allowableLossesValue = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[AllowableLossesValueModel](KeystoreKeys.ResidentKeys.allowableLossesValue).map {
      case Some(data) => Ok(views.allowableLossesValue(allowableLossesValueForm.fill(data)))
      case None => Ok(views.allowableLossesValue(allowableLossesValueForm))
    }
  }

  val submitAllowableLossesValue = FeatureLockForRTT.async { implicit request =>
    allowableLossesValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.allowableLossesValue(errors))),
      success => {
        calcConnector.saveFormData[AllowableLossesValueModel](KeystoreKeys.ResidentKeys.allowableLossesValue, success)
        Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
      }
    )
  }

  //################# Brought Forward Losses Actions ############################
  val lossesBroughtForward = Action.async { implicit request =>
    Future.successful(Ok(views.lossesBroughtForward()))
  }

  //################# Brought Forward Losses Value Actions ##############################
  val lossesBroughtForwardValue = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[LossesBroughtForwardValueModel](KeystoreKeys.ResidentKeys.lossesBroughtForwardValue).map {
      case Some(data) => Ok(views.lossesBroughtForwardValue(lossesBroughtForwardValueForm.fill(data)))
      case None => Ok(views.lossesBroughtForwardValue(lossesBroughtForwardValueForm))
    }
  }

  val submitLossesBroughtForwardValue = FeatureLockForRTT.async { implicit request =>
    lossesBroughtForwardValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.lossesBroughtForwardValue(errors))),
      success => calcConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.ResidentKeys.otherProperties).flatMap {
        case Some(OtherPropertiesModel(true)) => Future.successful(Redirect(routes.DeductionsController.annualExemptAmount()))
        case _ => Future.successful(Redirect(routes.SummaryController.summary()))
      }
    )
  }

  //################# Annual Exempt Amount Input Actions #############################
  val annualExemptAmount = Action.async { implicit request =>
    Future.successful(Ok(views.annualExemptAmount()))

  }

  //################# Second Summary Actions ###############################

}
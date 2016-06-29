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

import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import play.api.mvc.Action
import views.html.calculation.{resident => views}
import forms.resident.ReliefsForm._
import models.resident.ReliefsModel

import scala.concurrent.Future


object DeductionsController extends DeductionsController {
  val calcConnector = CalculatorConnector
}

trait DeductionsController extends FeatureLock {

  //################# Reliefs Actions ########################
  val reliefs = FeatureLockForRTT.async { implicit request =>
    Future.successful(Ok(views.reliefs(reliefsForm)))
  }

  val submitReliefs = Action.async {implicit request =>
    reliefsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.reliefs(errors))),
      success => {
        success match {
          case ReliefsModel("Yes") => Future.successful(Redirect(routes.DeductionsController.reliefsValue()))
          case _ => Future.successful(Redirect(routes.DeductionsController.otherProperties()))
        }
      }
    )
  }

  //################# Reliefs Value Input Actions ########################
  val reliefsValue = Action.async { implicit request =>
    Future.successful(Ok(views.reliefsValue()))
  }

  //################# Other Properties Actions #########################
  val otherProperties = Action.async { implicit request =>
    Future.successful(Ok(views.otherProperties()))
  }

  //################# Allowable Losses Actions #########################

  //################# Allowable Losses Input Actions ############################

  //################# Brought Forward Losses Actions ############################
  val lossesBroughtForward = Action.async { implicit request =>
    Future.successful(Ok(views.lossesBroughtForward()))
  }

  //################# Brought Forward Losses Value Actions ##############################

  //################# Annual Exempt Amount Input Actions #############################

  //################# Second Summary Actions ###############################

}
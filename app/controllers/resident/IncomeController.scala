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
import views.html.calculation.resident.{income => views}
import forms.resident.PersonalAllowanceForm._
import play.api.mvc.{Action, Result}
import models.resident._

import scala.concurrent.Future

object IncomeController extends IncomeController {

  val calcConnector = CalculatorConnector

}

trait IncomeController extends FeatureLock {

  val calcConnector: CalculatorConnector

  //################################# Previous Taxable Gain Actions ##########################################
  val previousTaxableGains = FeatureLockForRTT.async { implicit request =>
    Future.successful(Ok(views.previousTaxableGains()))
  }

  //################################# Current Income Actions ##########################################
  val currentIncome = FeatureLockForRTT.async { implicit request =>
    Future.successful(Ok(views.currentIncome()))
  }

  //################################# Personal Allowance Actions ##########################################
  val personalAllowance = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[PersonalAllowanceModel](KeystoreKeys.ResidentKeys.personalAllowance).map {
      case Some(data) => Ok(views.personalAllowance(personalAllowanceForm.fill(data)))
      case None => Ok(views.personalAllowance(personalAllowanceForm))
    }
  }
  val submitPersonalAllowance = FeatureLockForRTT.async { implicit request =>
    personalAllowanceForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.personalAllowance(errors))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.ResidentKeys.personalAllowance, success)
        Future.successful(Redirect(routes.SummaryController.summary()))
      }
    )

  }
}

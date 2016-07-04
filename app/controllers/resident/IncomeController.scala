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
import forms.resident.CurrentIncomeForm._
import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import models.resident.CurrentIncomeModel
import views.html.calculation.resident.{income => views}

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
    calcConnector.fetchAndGetFormData[CurrentIncomeModel](KeystoreKeys.ResidentKeys.currentIncome).map {
      case Some(data) => Ok(views.currentIncome(currentIncomeForm.fill(data)))
      case None => Ok(views.currentIncome(currentIncomeForm))
    }
  }

  val submitCurrentIncome = FeatureLockForRTT.async { implicit request =>
    currentIncomeForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.currentIncome(errors))),
      success => {
        calcConnector.saveFormData[CurrentIncomeModel](KeystoreKeys.ResidentKeys.currentIncome, success)
        Future.successful(Redirect(routes.IncomeController.personalAllowance()))
      }
    )
  }

  //################################# Personal Allowance Actions ##########################################
  val personalAllowance = FeatureLockForRTT.async { implicit request =>
    Future.successful(Ok(views.personalAllowance()))
  }

}

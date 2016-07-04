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
import play.api.mvc.Result

import scala.concurrent.Future
import views.html.calculation.resident.{income => views}
import forms.resident.income.PreviousTaxableGainsForm._
import models.resident.income._

object IncomeController extends IncomeController {
  val calcConnector = CalculatorConnector
}

trait IncomeController extends FeatureLock {

  val calcConnector: CalculatorConnector

  val previousTaxableGains = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[PreviousTaxableGainsModel](KeystoreKeys.ResidentKeys.previousTaxableGains).map {
      case Some(data) => Ok(views.previousTaxableGains(previousTaxableGainsForm.fill(data)))
      case None => Ok(views.previousTaxableGains(previousTaxableGainsForm))
    }
  }

  val submitPreviousTaxableGains = FeatureLockForRTT.async { implicit request =>

    previousTaxableGainsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.previousTaxableGains(errors))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.ResidentKeys.previousTaxableGains, success)
        Future.successful(Redirect(routes.IncomeController.previousTaxableGains()))
      }
    )
  }
}

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

package controllers.nonresident

import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.nonresident.DisposalCostsForm._
import models.nonresident.{AcquisitionDateModel, DisposalCostsModel, RebasedValueModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.calculation

import scala.concurrent.Future

object DisposalCostsController extends DisposalCostsController {
  val calcConnector = CalculatorConnector
}

trait DisposalCostsController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url

  val disposalCosts = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisposalCostsModel](KeystoreKeys.disposalCosts).map {
      case Some(data) => Ok(calculation.nonresident.disposalCosts(disposalCostsForm.fill(data)))
      case None => Ok(calculation.nonresident.disposalCosts(disposalCostsForm))
    }
  }

  val submitDisposalCosts = ValidateSession.async { implicit request =>
    disposalCostsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(calculation.nonresident.disposalCosts(errors))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.disposalCosts, success)
        calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap {
          case Some(data) if data.hasAcquisitionDate == "Yes" =>
            Future.successful(Redirect(routes.PrivateResidenceReliefController.privateResidenceRelief()))
          case _ =>
            calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap {
              case Some(rebasedData) if rebasedData.hasRebasedValue == "Yes" =>
                Future.successful(Redirect(routes.PrivateResidenceReliefController.privateResidenceRelief()))
              case _ =>
                Future.successful(Redirect(routes.AllowableLossesController.allowableLosses()))

            }
        }
      }
    )
  }
}

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

import common.DefaultRoutes._
import common.{KeystoreKeys, TaxDates}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.nonresident.ImprovementsForm._
import models.nonresident.{AcquisitionDateModel, ImprovementsModel, RebasedValueModel}
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object ImprovementsController extends ImprovementsController {
  val calcConnector = CalculatorConnector
}

trait ImprovementsController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.CustomerTypeController.customerType().url

//This will need to be replace with backLink when the back routing logic is added back in
  private val tempBackLink = routes.AcquisitionCostsController.acquisitionCosts().url

  private def improvementsBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    def checkRebasedValue = {
      calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap {
        case Some(RebasedValueModel("Yes", data)) => Future.successful(routes.RebasedCostsController.rebasedCosts().url)
        case Some(RebasedValueModel("No", data)) => Future.successful(routes.RebasedValueController.rebasedValue().url)
        case _ => Future.successful(missingDataRoute)
      }
    }

    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap {
      case Some(AcquisitionDateModel("Yes", Some(day), Some(month), Some(year))) if TaxDates.dateAfterStart(day, month, year) =>
        Future.successful(routes.AcquisitionValueController.acquisitionValue().url)
      case None => Future.successful(missingDataRoute)
      case _ => checkRebasedValue
    }
  }

  private def fetchAcquisitionDate(implicit headerCarrier: HeaderCarrier): Future[Option[AcquisitionDateModel]] = {
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate)
  }

  private def fetchImprovements(implicit headerCarrier: HeaderCarrier): Future[Option[ImprovementsModel]] = {
    calcConnector.fetchAndGetFormData[ImprovementsModel](KeystoreKeys.improvements)
  }

  private def fetchRebasedValue(implicit headerCarrier: HeaderCarrier): Future[Option[RebasedValueModel]] = {
    calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue)
  }

  private def displayImprovementsSectionCheck(rebasedValueModel: Option[RebasedValueModel],
                                              acquisitionDateModel: Option[AcquisitionDateModel]): Future[Boolean] = {
    (rebasedValueModel, acquisitionDateModel) match {
      case (Some(value), Some(data)) if data.hasAcquisitionDate == "Yes" &&
        !TaxDates.dateAfterStart(data.day.get, data.month.get, data.year.get) &&
        value.hasRebasedValue == "Yes" =>
        Future.successful(true)
      case (Some(value), Some(data)) if data.hasAcquisitionDate == "No" &&
        value.hasRebasedValue == "Yes" =>
        Future.successful(true)
      case (_, _) =>
        Future.successful(false)
    }
  }

  val improvements = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String, improvementsModel: Option[ImprovementsModel], improvementsOptions: Boolean): Future[Result] = {
      improvementsModel match {
        case Some(data) =>
          Future.successful(Ok(calculation.nonresident.improvements(improvementsForm(improvementsOptions).fill(data),
            improvementsOptions, tempBackLink)))
        case None =>
          Future.successful(Ok(calculation.nonresident.improvements(improvementsForm(improvementsOptions),
            improvementsOptions, tempBackLink)))
      }
    }

    for {
      rebasedValue <- fetchRebasedValue(hc)
      acquisitionDate <- fetchAcquisitionDate(hc)
      improvements <- fetchImprovements(hc)
      improvementsOptions <- displayImprovementsSectionCheck(rebasedValue, acquisitionDate)
      backUrl <- improvementsBackUrl
      route <- routeRequest(tempBackLink, improvements, improvementsOptions)
    } yield route
  }

  val submitImprovements = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String, improvementsOptions: Boolean): Future[Result] = {
      improvementsForm(improvementsOptions).bindFromRequest.fold(
        errors => {
            Future.successful(BadRequest(calculation.nonresident.improvements(errors,
              improvementsOptions,
              tempBackLink)))
        },
        success => {
          calcConnector.saveFormData(KeystoreKeys.improvements, success)
          Future.successful(Redirect(routes.OtherReliefsController.otherReliefs()))
        }
      )
    }

    for {
      rebasedValue <- fetchRebasedValue(hc)
      acquisitionDate <- fetchAcquisitionDate(hc)
      improvementsOptions <- displayImprovementsSectionCheck(rebasedValue, acquisitionDate)
      backUrl <- improvementsBackUrl
      route <- routeRequest(tempBackLink, improvementsOptions)
    } yield route
  }
}
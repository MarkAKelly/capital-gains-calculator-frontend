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
import common.{Dates, KeystoreKeys, TaxDates}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.nonresident.OtherReliefsForm._
import models.nonresident._
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object OtherReliefsController extends OtherReliefsController {
  val calcConnector = CalculatorConnector
}

trait OtherReliefsController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.CustomerTypeController.customerType().url

  private def otherReliefsBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap {
      case (Some(AcquisitionDateModel("Yes", day, month, year))) if TaxDates.dateAfterStart(day.get, month.get, year.get) =>
        Future.successful(routes.AllowableLossesController.allowableLosses().url)
      case (Some(AcquisitionDateModel("Yes", day, month, year))) => Future.successful(routes.CalculationElectionController.calculationElection().url)
      case (Some(AcquisitionDateModel("No", _, _, _))) =>
        calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap {
          case Some(RebasedValueModel("Yes", _)) => Future.successful(routes.CalculationElectionController.calculationElection().url)
          case Some(RebasedValueModel("No", _)) => Future.successful(routes.AllowableLossesController.allowableLosses().url)
          case _ => Future.successful(missingDataRoute)
        }
      case _ => Future.successful(missingDataRoute)
    }
  }

  val otherReliefs = ValidateSession.async { implicit request =>

    def action(dataResult: Option[CalculationResultModel], backUrl: String) = {
      calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat).map {
        case Some(data) if data.otherReliefs.isDefined => Ok(calculation.nonresident.otherReliefs(otherReliefsForm(false).fill(data), dataResult.get))
        case Some(data) if data.otherReliefs.isEmpty => Ok(calculation.nonresident.otherReliefs(otherReliefsForm(true).fill(data), dataResult.get))
        case _ => Ok(calculation.nonresident.otherReliefs(otherReliefsForm(false), dataResult.get))
      }
    }

    for {
      construct <- calcConnector.createSummary(hc)
      calculation <- calcConnector.calculateFlat(construct)
      backUrl <- otherReliefsBackUrl
      finalResult <- action(calculation, backUrl)
    } yield finalResult
  }

  val submitOtherReliefs = ValidateSession.async { implicit request =>

    def errorAction(form: Form[OtherReliefsModel], construct: SummaryModel) = {
      for {
        calculation <- calcConnector.calculateFlat(construct)
        route <- errorRoute(form, calculation)
      } yield route
    }

    def errorRoute(form: Form[OtherReliefsModel], dataResult: Option[CalculationResultModel]) = {
      calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat).map {
        case Some(data) if data.otherReliefs.isDefined => BadRequest(calculation.nonresident.otherReliefs(form, dataResult.get))
        case _ => BadRequest(calculation.nonresident.otherReliefs(form, dataResult.get))
      }
    }

    def successAction(model: OtherReliefsModel, construct: SummaryModel) = {
      calcConnector.saveFormData(KeystoreKeys.otherReliefsFlat, model)
      (construct.acquisitionDateModel.hasAcquisitionDate, construct.rebasedValueModel.getOrElse(RebasedValueModel("No", None)).hasRebasedValue) match {
        case ("Yes", _) if TaxDates.dateAfterStart(construct.acquisitionDateModel.day.get,
          construct.acquisitionDateModel.month.get, construct.acquisitionDateModel.year.get) =>
          Future.successful(Redirect(routes.SummaryController.summary()))
        case ("No", "No") => Future.successful(Redirect(routes.SummaryController.summary()))
        case _ => Future.successful(Redirect(routes.CalculationElectionController.calculationElection()))
      }
    }

    def action(construct: SummaryModel) = otherReliefsForm(false).bindFromRequest.fold(
      errors => errorAction(errors, construct),
      success => successAction(success, construct))

    for {
      construct <- calcConnector.createSummary(hc)
      finalResult <- action(construct)
    } yield finalResult
  }
}

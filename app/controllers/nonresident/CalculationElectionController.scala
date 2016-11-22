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

import common.{KeystoreKeys, TaxDates}
import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import forms.nonresident.CalculationElectionForm._
import models.nonresident._
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object CalculationElectionController extends CalculationElectionController {
  val calcConnector = CalculatorConnector
  val calcElectionConstructor = CalculationElectionConstructor
}

trait CalculationElectionController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url
  val calcConnector: CalculatorConnector
  val calcElectionConstructor: CalculationElectionConstructor

  private def getOtherReliefsFlat(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] =
    calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat).map {
      case Some(data) => Some(data.otherReliefs)
      case _ => None
    }

  private def getOtherReliefsTA(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] =
    calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA).map {
      case Some(data) => Some(data.otherReliefs)
      case _ => None
    }

  private def getOtherReliefsRebased(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] =
    calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsRebased).map {
      case Some(data) => Some(data.otherReliefs)
      case _ => None
    }

  private def calcTimeCall(summary: SummaryModel)(implicit hc: HeaderCarrier): Future[Option[CalculationResultModel]] = {
    summary.acquisitionDateModel.hasAcquisitionDate match {
      case "Yes" if !TaxDates.dateAfterStart(summary.acquisitionDateModel.day.get,
        summary.acquisitionDateModel.month.get,
        summary.acquisitionDateModel.year.get) =>
        calcConnector.calculateTA(summary)
      case _ => Future(None)
    }
  }

  private def calcRebasedCall(summary: SummaryModel)(implicit hc: HeaderCarrier): Future[Option[CalculationResultModel]] = {
    (summary.rebasedValueModel.getOrElse(RebasedValueModel(None)).rebasedValueAmt.isDefined, summary.acquisitionDateModel.hasAcquisitionDate) match {
      case (true, "Yes") if !TaxDates.dateAfterStart(summary.acquisitionDateModel.day.get,
        summary.acquisitionDateModel.month.get,
        summary.acquisitionDateModel.year.get) =>
        calcConnector.calculateRebased(summary)
      case (true, "No") => calcConnector.calculateRebased(summary)
      case _ => Future(None)
    }
  }

  val calculationElection = ValidateSession.async { implicit request =>

    def action(construct: SummaryModel,
               content: Seq[(String, String, String, Option[String], String, Option[BigDecimal])]) =
      calcConnector.fetchAndGetFormData[CalculationElectionModel](KeystoreKeys.calculationElection).map {
        case Some(data) =>
          Ok(calculation.nonresident.calculationElection(
            calculationElectionForm.fill(data),
            construct,
            content)
          )
        case None =>
          Ok(calculation.nonresident.calculationElection(
            calculationElectionForm,
            construct,
            content)
          )
      }

    for {
      construct <- calcConnector.createSummary(hc)
      calcFlat <- calcConnector.calculateFlat(construct)
      calcTA <- calcTimeCall(construct)
      calcRebased <- calcRebasedCall(construct)
      otherReliefsFlat <- getOtherReliefsFlat
      otherReliefsTA <- getOtherReliefsTA
      otherReliefsRebased <- getOtherReliefsRebased
      finalResult <- action(
        construct,
        calcElectionConstructor.generateElection(construct, hc, calcFlat, calcTA, calcRebased, otherReliefsFlat, otherReliefsTA, otherReliefsRebased)
      )
    } yield finalResult
  }

  val submitCalculationElection = ValidateSession.async { implicit request =>

    def successAction(model: CalculationElectionModel) = {
      calcConnector.saveFormData(KeystoreKeys.calculationElection, model)
      request.body.asFormUrlEncoded.get("action").headOption match {
        case Some("flat") => Future.successful(Redirect(routes.OtherReliefsFlatController.otherReliefsFlat()))
        case Some("time") => Future.successful(Redirect(routes.OtherReliefsTAController.otherReliefsTA()))
        case Some("rebased") => Future.successful(Redirect(routes.OtherReliefsRebasedController.otherReliefsRebased()))
        case _ => Future.successful(Redirect(routes.SummaryController.summary()))
      }
    }

    def errorAction(form: Form[CalculationElectionModel]) = {
      for {
        construct <- calcConnector.createSummary(hc)
        calcFlat <- calcConnector.calculateFlat(construct)
        calcTA <- calcTimeCall(construct)
        otherReliefsFlat <- getOtherReliefsFlat
        otherReliefsTA <- getOtherReliefsTA
        otherReliefsRebased <- getOtherReliefsRebased
        calcRebased <- calcRebasedCall(construct)
      } yield {
        BadRequest(calculation.nonresident.calculationElection(
          form,
          construct,
          calcElectionConstructor.generateElection(construct, hc, calcFlat, calcTA, calcRebased, otherReliefsFlat, otherReliefsTA, otherReliefsRebased)
        ))
      }
    }

    calculationElectionForm.bindFromRequest.fold(errorAction, successAction)
  }

}

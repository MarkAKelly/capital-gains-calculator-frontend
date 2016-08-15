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

import java.time.LocalDate
import common.{Dates, KeystoreKeys}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.nonresident.PrivateResidenceReliefForm._
import models.nonresident.{AcquisitionDateModel, DisposalDateModel, PrivateResidenceReliefModel, RebasedValueModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object PrivateResidenceReliefController extends PrivateResidenceReliefController {
  val calcConnector = CalculatorConnector
}

trait PrivateResidenceReliefController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  val calcConnector: CalculatorConnector

  def getAcquisitionDate(implicit hc: HeaderCarrier): Future[Option[LocalDate]] =
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).map {
      case Some(AcquisitionDateModel("Yes", Some(day), Some(month), Some(year))) => Some(Dates.constructDate(day, month, year))
      case _ => None
    }

  def getDisposalDate(implicit hc: HeaderCarrier): Future[Option[LocalDate]] =
    calcConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate).map {
      case Some(data) => Some(Dates.constructDate(data.day, data.month, data.year))
      case _ => None
    }

  def getRebasedAmount(implicit hc: HeaderCarrier): Future[Boolean] =
    calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).map {
      case Some(data) if data.hasRebasedValue == "Yes" => true
      case _ => false
    }

  def displayBetweenQuestion(disposalDate: Option[LocalDate], acquisitionDate: Option[LocalDate], hasRebasedValue: Boolean): Boolean =
    (disposalDate, acquisitionDate) match {
      case (Some(dDate), Some(aDate)) if Dates.dateAfterOctober(dDate) && !Dates.dateAfterStart(aDate) => true
      case (Some(dDate), aDateOption) if Dates.dateAfterOctober(dDate) && hasRebasedValue => true
      case _ => false
    }

  def displayBeforeQuestion(disposalDate: Option[LocalDate], acquisitionDate: Option[LocalDate], hasRebasedValue: Boolean): Boolean =
    (disposalDate, acquisitionDate) match {
      case (Some(dDate), Some(aDate)) if Dates.dateAfterOctober(dDate) => true
      case (Some(dDate), Some(aDate)) if !Dates.dateAfterStart(aDate) => true
      case _ => false
    }


  val privateResidenceRelief = ValidateSession.async { implicit request =>

    def action(disposalDate: Option[LocalDate], acquisitionDate: Option[LocalDate], hasRebasedValue: Boolean) = {

      val showBetweenQuestion = displayBetweenQuestion(disposalDate, acquisitionDate, hasRebasedValue)
      val showBeforeQuestion = displayBeforeQuestion(disposalDate, acquisitionDate, hasRebasedValue)
      val disposalDateLess18Months = Dates.dateMinusMonths(disposalDate, 18)

      calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief).map {
        case Some(data) => Ok(calculation.nonresident.privateResidenceRelief(privateResidenceReliefForm(showBeforeQuestion,
          showBetweenQuestion).fill(data), showBetweenQuestion, showBeforeQuestion, disposalDateLess18Months))
        case None => Ok(calculation.nonresident.privateResidenceRelief(privateResidenceReliefForm(showBeforeQuestion, showBetweenQuestion),
          showBetweenQuestion, showBeforeQuestion, disposalDateLess18Months))
      }
    }

    for {
      disposalDate <- getDisposalDate
      acquisitionDate <- getAcquisitionDate
      hasRebasedValue <- getRebasedAmount
      finalResult <- action(disposalDate, acquisitionDate, hasRebasedValue)
    } yield finalResult
  }

  val submitPrivateResidenceRelief = ValidateSession.async { implicit request =>

    def action(disposalDate: Option[LocalDate], acquisitionDate: Option[LocalDate], hasRebasedValue: Boolean) = {

      val showBetweenQuestion = displayBetweenQuestion(disposalDate, acquisitionDate, hasRebasedValue)
      val showBeforeQuestion = displayBeforeQuestion(disposalDate, acquisitionDate, hasRebasedValue)
      val disposalDateLess18Months = Dates.dateMinusMonths(disposalDate, 18)

      privateResidenceReliefForm(showBeforeQuestion, showBetweenQuestion).bindFromRequest.fold(
        errors => {
          Future.successful(BadRequest(calculation.nonresident.privateResidenceRelief(errors, showBetweenQuestion,
            showBeforeQuestion, disposalDateLess18Months)))
        },
        success => {
          calcConnector.saveFormData(KeystoreKeys.privateResidenceRelief, success)
          Future.successful(Redirect(routes.AllowableLossesController.allowableLosses()))
        }
      )
    }

    for {
      disposalDate <- getDisposalDate
      acquisitionDate <- getAcquisitionDate
      hasRebasedValue <- getRebasedAmount
      finalResult <- action(disposalDate, acquisitionDate, hasRebasedValue)
    } yield finalResult
  }
}

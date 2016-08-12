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

import common.{Dates, KeystoreKeys}
import forms.nonresident.OtherReliefsForm._
import forms.nonresident.PersonalAllowanceForm._
import forms.nonresident.RebasedValueForm._
import java.util.{Date, UUID}

import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future
import views.html._
import common.DefaultRoutes._
import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import models.nonresident._

trait CalculationController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.CalculationController.restart().url
  val calcConnector: CalculatorConnector
  val calcElectionConstructor: CalculationElectionConstructor

  //################### Shared/Common methods #######################
  def getAcquisitionDate(implicit hc: HeaderCarrier): Future[Option[Date]] =
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).map {
      case Some(AcquisitionDateModel("Yes", Some(day), Some(month), Some(year))) => Some(Dates.constructDate(day, month, year))
      case _ => None
    }

  //################### Disabled Trustee methods #######################

  //################### Personal Allowance methods #######################
  val personalAllowance = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[PersonalAllowanceModel](KeystoreKeys.personalAllowance).map {
      case Some(data) => Ok(calculation.nonresident.personalAllowance(personalAllowanceForm().fill(data)))
      case None => Ok(calculation.nonresident.personalAllowance(personalAllowanceForm()))
    }
  }

  val submitPersonalAllowance = ValidateSession.async { implicit request =>
    calcConnector.getPA(2017).flatMap { pa =>
      personalAllowanceForm(pa.get).bindFromRequest.fold(
        errors => Future.successful(BadRequest(calculation.nonresident.personalAllowance(errors))),
        success => {
          calcConnector.saveFormData(KeystoreKeys.personalAllowance, success)
          Future.successful(Redirect(routes.OtherPropertiesController.otherProperties()))
        }
      )
    }
  }

  //################### Other Properties methods #######################

  //################### Rebased value methods #######################

  //################### Rebased costs methods #######################

  //################### Improvements methods #######################

  //################### Disposal Date methods #######################

  //################### No Capital Gains Tax #######################

  //################### Disposal Value methods #######################

  //################### Disposal Costs methods #######################

  //################### Private Residence Relief methods #######################
  def getDisposalDate(implicit hc: HeaderCarrier): Future[Option[Date]] =
    calcConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate).map {
      case Some(data) => Some(Dates.constructDate(data.day, data.month, data.year))
      case _ => None
    }

   //################### Other Reliefs with no calc selection methods (flat) #######################
  def otherReliefsBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap {
      case (Some(AcquisitionDateModel("Yes", day, month, year))) if Dates.dateAfterStart(day.get, month.get, year.get) =>
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
        case _ => Ok(calculation.nonresident.otherReliefs(otherReliefsForm(true), dataResult.get))
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

    def action(dataResult: Option[CalculationResultModel], construct: SummaryModel, backUrl: String) = otherReliefsForm(false).bindFromRequest.fold(
      errors =>
        calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat).map {
          case Some(data) if data.otherReliefs.isDefined => BadRequest(calculation.nonresident.otherReliefs(errors, dataResult.get))
          case _ => BadRequest(calculation.nonresident.otherReliefs(errors, dataResult.get))
        },
      success => {
        calcConnector.saveFormData(KeystoreKeys.otherReliefsFlat, success)
        (construct.acquisitionDateModel.hasAcquisitionDate, construct.rebasedValueModel.getOrElse(RebasedValueModel("No", None)).hasRebasedValue) match {
          case ("Yes", _) if Dates.dateAfterStart(construct.acquisitionDateModel.day.get,
            construct.acquisitionDateModel.month.get, construct.acquisitionDateModel.year.get) => {
            Future.successful(Redirect(routes.CalculationController.summary()))
          }
          case ("No", "No") => Future.successful(Redirect(routes.CalculationController.summary()))
          case _ => Future.successful(Redirect(routes.CalculationElectionController.calculationElection()))
        }
      }
    )

    for {
      construct <- calcConnector.createSummary(hc)
      calculation <- calcConnector.calculateFlat(construct)
      backUrl <- otherReliefsBackUrl
      finalResult <- action(calculation, construct, backUrl)
    } yield finalResult
  }

  //################### Flat Other Reliefs methods #######################

  //################### Time Apportioned Other Reliefs methods #######################

  //################### Rebased Other Reliefs methods #######################

  //################### Summary Methods ##########################
  def summaryBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).flatMap {
      case Some(AcquisitionDateModel("Yes", day, month, year)) if Dates.dateAfterStart(day.get, month.get, year.get) =>
        Future.successful(routes.CalculationController.otherReliefs().url)
      case Some(AcquisitionDateModel("Yes", _, _, _)) => Future.successful(routes.CalculationElectionController.calculationElection().url)
      case Some(AcquisitionDateModel("No", _, _, _)) =>
        calcConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue).flatMap {
          case Some(RebasedValueModel("Yes", _)) => Future.successful(routes.CalculationElectionController.calculationElection().url)
          case Some(RebasedValueModel("No", _)) => Future.successful(routes.CalculationController.otherReliefs().url)
          case _ => Future.successful(missingDataRoute)
        }
      case _ => Future.successful(missingDataRoute)
    }
  }

  val summary = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String) = {
      calcConnector.createSummary(hc).flatMap(summaryData =>
        summaryData.calculationElectionModel.calculationType match {
          case "flat" =>
            calcConnector.calculateFlat(summaryData).map(result =>
              Ok(calculation.nonresident.summary(summaryData, result.get, backUrl)))
          case "time" =>
            calcConnector.calculateTA(summaryData).map(result =>
              Ok(calculation.nonresident.summary(summaryData, result.get, backUrl)))
          case "rebased" =>
            calcConnector.calculateRebased(summaryData).map(result =>
              Ok(calculation.nonresident.summary(summaryData, result.get, backUrl)))
        })
    }

    for {
      backUrl <- summaryBackUrl
      route <- routeRequest(backUrl)
    } yield route
  }

  def restart(): Action[AnyContent] = Action.async { implicit request =>
    calcConnector.clearKeystore(hc)
    Future.successful(Redirect(routes.CustomerTypeController.customerType()))
  }
}

object CalculationController extends CalculationController {
  val calcConnector = CalculatorConnector
  val calcElectionConstructor = CalculationElectionConstructor
}
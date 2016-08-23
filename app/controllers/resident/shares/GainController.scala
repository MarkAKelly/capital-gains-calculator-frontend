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

package controllers.resident.shares

import java.util.UUID

import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import play.api.mvc.Result
import uk.gov.hmrc.play.http.SessionKeys
import scala.concurrent.Future
import views.html.calculation.{resident => commonViews}
import views.html.calculation.resident.shares.{gain => views}
import forms.resident.DisposalDateForm._
import forms.resident.DisposalCostsForm._
import forms.resident.DisposalValueForm._
import forms.resident.AcquisitionCostsForm._
import forms.resident.AcquisitionValueForm._
import models.resident._
import common.Dates._

object GainController extends GainController {
  val calcConnector = CalculatorConnector
}

trait GainController extends FeatureLock {

  val calcConnector: CalculatorConnector

  override val homeLink = controllers.resident.shares.routes.GainController.disposalDate().url
  override val sessionTimeoutUrl = homeLink

  //################# Disposal Date Actions ####################
  val disposalDate = FeatureLockForRTTShares.asyncNoTimeout { implicit request =>
    if (request.session.get(SessionKeys.sessionId).isEmpty) {
      val sessionId = UUID.randomUUID.toString
      Future.successful(Ok(views.disposalDate(disposalDateForm, homeLink)).withSession(request.session + (SessionKeys.sessionId -> s"session-$sessionId")))
    }
    else {
      calcConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate).map {
        case Some(data) => Ok(views.disposalDate(disposalDateForm.fill(data), homeLink))
        case None => Ok(views.disposalDate(disposalDateForm, homeLink))
      }
    }
  }

  val submitDisposalDate = FeatureLockForRTTShares.async { implicit request =>

    def routeRequest(taxYearResult: Option[TaxYearModel]): Future[Result] = {
      if (taxYearResult.isDefined && !taxYearResult.get.isValidYear) Future.successful(Redirect(routes.GainController.outsideTaxYears()))
      else Future.successful(Redirect(routes.GainController.disposalValue()))
    }

    disposalDateForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.disposalDate(errors, homeLink))),
      success => {
        for {
          save <- calcConnector.saveFormData(keystoreKeys.disposalDate, success)
          taxYearResult <- calcConnector.getTaxYear(s"${success.year}-${success.month}-${success.day}")
          route <- routeRequest(taxYearResult)
        } yield route
      }
    )
  }

  //################ Outside Tax Years Actions ######################
  val outsideTaxYears = FeatureLockForRTTShares.async { implicit request =>
    for {
      disposalDate <- calcConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
      taxYear <- calcConnector.getTaxYear(s"${disposalDate.get.year}-${disposalDate.get.month}-${disposalDate.get.day}")
    } yield {
      Ok(commonViews.outsideTaxYear(
        taxYear = taxYear.get,
        isAfterApril15 = dateAfterStart(constructDate(disposalDate.get.day, disposalDate.get.month, disposalDate.get.year)),
        navBackLink = routes.GainController.disposalDate().url,
        navHomeLink = homeLink,
        continueUrl = routes.GainController.disposalValue().url
      ))
    }
  }

  //################ Disposal Value Actions ######################
  val disposalValue = FeatureLockForRTTShares.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisposalValueModel](keystoreKeys.disposalValue).map {
      case Some(data) => Ok(views.disposalValue(disposalValueForm.fill(data), homeLink))
      case None => Ok(views.disposalValue(disposalValueForm, homeLink))
    }
  }

  val submitDisposalValue = FeatureLockForRTTShares.async { implicit request =>
    disposalValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.disposalValue(errors, homeLink))),
      success => {
        calcConnector.saveFormData[DisposalValueModel](keystoreKeys.disposalValue, success)
        Future.successful(Redirect(routes.GainController.disposalCosts()))
      }
    )
  }

  //################# Disposal Costs Actions ########################
  val disposalCosts = FeatureLockForRTTShares.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisposalCostsModel](keystoreKeys.disposalCosts).map {
      case Some(data) => Ok(views.disposalCosts(disposalCostsForm.fill(data), homeLink))
      case None => Ok(views.disposalCosts(disposalCostsForm, homeLink))
    }
  }

  val submitDisposalCosts = FeatureLockForRTTShares.async { implicit request =>
    disposalCostsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.disposalCosts(errors, homeLink))),
      success => {
        calcConnector.saveFormData(keystoreKeys.disposalCosts, success)
        Future.successful(Redirect(routes.GainController.acquisitionValue()))}
    )
  }

  //################# Acquisition Value Actions ########################
  val acquisitionValue = FeatureLockForRTTShares.async { implicit request =>
    calcConnector.fetchAndGetFormData[AcquisitionValueModel](keystoreKeys.acquisitionValue).map {
      case Some(data) => Ok(views.acquisitionValue(acquisitionValueForm.fill(data), homeLink))
      case None => Ok(views.acquisitionValue(acquisitionValueForm, homeLink))
    }
  }

  val submitAcquisitionValue = FeatureLockForRTTShares.async { implicit request =>
    acquisitionValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.acquisitionValue(errors, homeLink))),
      success => {
        calcConnector.saveFormData(keystoreKeys.acquisitionValue, success)
        Future.successful(Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# Acquisition Costs Actions ########################

  private val acquisitionCostsBackLink = Some(controllers.resident.shares.routes.GainController.acquisitionValue().toString)

  val acquisitionCosts = FeatureLockForRTTShares.async { implicit request =>
    calcConnector.fetchAndGetFormData[AcquisitionCostsModel](keystoreKeys.acquisitionCosts).map {
      case Some(data) => Ok(views.acquisitionCosts(acquisitionCostsForm.fill(data), acquisitionCostsBackLink, homeLink))
      case None => Ok(views.acquisitionCosts(acquisitionCostsForm, acquisitionCostsBackLink, homeLink))
    }
  }

  val submitAcquisitionCosts = FeatureLockForRTTShares.async { implicit request =>

    def routeRequest(totalGain: BigDecimal): Future[Result] = {
      if (totalGain > 0) Future.successful(Redirect(routes.DeductionsController.otherDisposals()))
      else Future.successful(Redirect(routes.SummaryController.summary()))
    }

    acquisitionCostsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.acquisitionCosts(errors, acquisitionCostsBackLink, homeLink))),
      success => {
        for {
          save <- calcConnector.saveFormData(keystoreKeys.acquisitionCosts, success)
          answers <- calcConnector.getShareGainAnswers
          grossGain <- calcConnector.calculateRttShareGrossGain(answers)
          route <- routeRequest(grossGain)
        } yield route
      }
    )
  }
}

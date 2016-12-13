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
import play.api.mvc.{Action, Result}
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future
import views.html.calculation.{resident => commonViews}
import views.html.calculation.resident.shares.{gain => views}
import forms.resident.DisposalDateForm._
import forms.resident.DisposalCostsForm._
import forms.resident.DisposalValueForm._
import forms.resident.AcquisitionCostsForm._
import forms.resident.AcquisitionValueForm._
import forms.resident.WorthWhenInheritedForm._
import forms.resident.shares.OwnerBeforeLegislationStartForm._
import forms.resident.shares.SellForLessForm._
import forms.resident.WorthWhenSoldForLessForm._
import forms.resident.shares.gain.DidYouInheritThemForm._
import forms.resident.shares.gain.ValueBeforeLegislationStartForm._
import models.resident._
import common.{Dates, TaxDates}
import controllers.predicates.ValidActiveSession
import models.resident.shares.OwnerBeforeLegislationStartModel
import models.resident.shares.gain.ValueBeforeLegislationStartModel
import models.resident.shares.gain.DidYouInheritThemModel
import play.api.i18n.Messages
import play.api.data.Form

object GainController extends GainController {
  val calcConnector = CalculatorConnector
}

trait GainController extends ValidActiveSession {

  val calcConnector: CalculatorConnector

  val navTitle = Messages("calc.base.resident.shares.home")
  override val homeLink = controllers.resident.shares.routes.GainController.disposalDate().url
  override val sessionTimeoutUrl = homeLink

  //################# Disposal Date Actions ####################
  val disposalDate = Action.async { implicit request =>
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

  val submitDisposalDate = ValidateSession.async { implicit request =>

    def routeRequest(taxYearResult: Option[TaxYearModel]): Future[Result] = {
      if (taxYearResult.isDefined && !taxYearResult.get.isValidYear) Future.successful(Redirect(routes.GainController.outsideTaxYears()))
      else Future.successful(Redirect(routes.GainController.sellForLess()))
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

  //################ Sell for Less Actions ######################
  private def sellForLessBackLink()(implicit hc: HeaderCarrier): Future[String] = {
    for {
      date <- calcConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
      taxYear <- calcConnector.getTaxYear(s"${date.get.year}-${date.get.month}-${date.get.day}")
    } yield if (taxYear.get.isValidYear) routes.GainController.disposalDate().url else routes.GainController.outsideTaxYears().url
  }

  val sellForLess = ValidateSession.async { implicit request =>

    def routeRequest(model: Option[SellForLessModel], backLink: String) = {
      val view = model match {
        case Some(data) => views.sellForLess(sellForLessForm.fill(data), homeLink, backLink)
        case None => views.sellForLess(sellForLessForm, homeLink, backLink)
      }

      Future.successful(Ok(view))
    }

    val soldForLess = calcConnector.fetchAndGetFormData[SellForLessModel](keystoreKeys.sellForLess)

    for {
      model <- soldForLess
      backLink <- sellForLessBackLink()
      route <- routeRequest(model, backLink)
    } yield route

  }

  val submitSellForLess = ValidateSession.async { implicit request =>
    sellForLessForm.bindFromRequest.fold(
      errors => {
        for {
          backLink <- sellForLessBackLink()
          response <- Future.successful(BadRequest(views.sellForLess(errors, homeLink, backLink)))
        } yield response
      },
      success => {
        calcConnector.saveFormData[SellForLessModel](keystoreKeys.sellForLess, success)
        success.sellForLess match {
          case true => Future.successful(Redirect(routes.GainController.worthWhenSoldForLess()))
          case _ => Future.successful(Redirect(routes.GainController.disposalValue()))
        }
      }
    )
  }

  //################ Worth When Sold Actions ######################
  val worthWhenSoldForLess = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[WorthWhenSoldForLessModel](keystoreKeys.worthWhenSoldForLess).map {
      case Some(data) => Ok(views.worthWhenSoldForLess(worthWhenSoldForLessForm.fill(data), homeLink))
      case _ => Ok(views.worthWhenSoldForLess(worthWhenSoldForLessForm, homeLink))
    }
  }

  val submitWorthWhenSoldForLess = ValidateSession.async { implicit request =>

    worthWhenSoldForLessForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthWhenSoldForLess(errors, homeLink))),
      success => {
        calcConnector.saveFormData(keystoreKeys.worthWhenSoldForLess, success)
        Future.successful(Redirect(routes.GainController.disposalCosts()))
      }
    )
  }


  //################ Outside Tax Years Actions ######################
  val outsideTaxYears = ValidateSession.async { implicit request =>
    for {
      disposalDate <- calcConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
      taxYear <- calcConnector.getTaxYear(s"${disposalDate.get.year}-${disposalDate.get.month}-${disposalDate.get.day}")
    } yield {
      Ok(commonViews.outsideTaxYear(
        taxYear = taxYear.get,
        isAfterApril15 = TaxDates.dateAfterStart(Dates.constructDate(disposalDate.get.day, disposalDate.get.month, disposalDate.get.year)),
        false,
        navBackLink = routes.GainController.disposalDate().url,
        navHomeLink = homeLink,
        continueUrl = routes.GainController.sellForLess().url,
        navTitle = navTitle
      ))
    }
  }

  //################ Disposal Value Actions ######################
  val disposalValue = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisposalValueModel](keystoreKeys.disposalValue).map {
      case Some(data) => Ok(views.disposalValue(disposalValueForm.fill(data), homeLink))
      case None => Ok(views.disposalValue(disposalValueForm, homeLink))
    }
  }

  val submitDisposalValue = ValidateSession.async { implicit request =>
    disposalValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.disposalValue(errors, homeLink))),
      success => {
        calcConnector.saveFormData[DisposalValueModel](keystoreKeys.disposalValue, success)
        Future.successful(Redirect(routes.GainController.disposalCosts()))
      }
    )
  }

  //################# Disposal Costs Actions ########################
  val disposalCosts = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisposalCostsModel](keystoreKeys.disposalCosts).map {
      case Some(data) => Ok(views.disposalCosts(disposalCostsForm.fill(data), homeLink))
      case None => Ok(views.disposalCosts(disposalCostsForm, homeLink))
    }
  }

  val submitDisposalCosts = ValidateSession.async { implicit request =>
    disposalCostsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.disposalCosts(errors, homeLink))),
      success => {
        calcConnector.saveFormData(keystoreKeys.disposalCosts, success)
        Future.successful(Redirect(routes.GainController.ownerBeforeLegislationStart()))}
    )
  }


  //################# Owned Before 1982 Actions ########################
  private val ownerBeforeLegislationStartBackLink = Some(controllers.resident.shares.routes.GainController.disposalCosts().url)

  val ownerBeforeLegislationStart = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[OwnerBeforeLegislationStartModel](keystoreKeys.ownerBeforeLegislationStart).map {
      case Some(data) => Ok(views.ownerBeforeLegislationStart(ownerBeforeLegislationStartForm.fill(data), homeLink, ownerBeforeLegislationStartBackLink))
      case None => Ok(views.ownerBeforeLegislationStart(ownerBeforeLegislationStartForm, homeLink, ownerBeforeLegislationStartBackLink))
    }
  }

  val submitOwnerBeforeLegislationStart = ValidateSession.async { implicit request =>
    ownerBeforeLegislationStartForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.ownerBeforeLegislationStart(errors, homeLink, ownerBeforeLegislationStartBackLink))),
      success => {
        calcConnector.saveFormData(keystoreKeys.ownerBeforeLegislationStart, success)
        success.ownerBeforeLegislationStart match {
          case true => Future.successful(Redirect(routes.GainController.valueBeforeLegislationStart()))
          case _ => Future.successful(Redirect(routes.GainController.didYouInheritThem()))
        }
      }
    )
  }

  //################# What were they worth on 31 March 1982 Actions ########################
  val valueBeforeLegislationStart =  ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[ValueBeforeLegislationStartModel](keystoreKeys.valueBeforeLegislationStart).map {
      case Some(data) => Ok(views.valueBeforeLegislationStart(valueBeforeLegislationStartForm.fill(data)))
      case None => Ok(views.valueBeforeLegislationStart(valueBeforeLegislationStartForm))
    }
  }

  val submitValueBeforeLegislationStart = ValidateSession.async { implicit request =>
    valueBeforeLegislationStartForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.valueBeforeLegislationStart(errors))),
      success => {
        calcConnector.saveFormData[ValueBeforeLegislationStartModel](keystoreKeys.valueBeforeLegislationStart, success)
        Future.successful(Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# Did you Inherit the Shares Actions ########################
  val didYouInheritThem = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[DidYouInheritThemModel](keystoreKeys.didYouInheritThem).map {
      case Some(data) => Ok(views.didYouInheritThem(didYouInheritThemForm.fill(data)))
      case None => Ok(views.didYouInheritThem(didYouInheritThemForm))
    }
  }

  val submitDidYouInheritThem = ValidateSession.async { implicit request =>
    didYouInheritThemForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.didYouInheritThem(errors))),
      success => {
        calcConnector.saveFormData(keystoreKeys.didYouInheritThem, success)
        if(success.wereInherited) Future.successful(Redirect(routes.GainController.worthWhenInherited()))
        else Future.successful(Redirect(routes.GainController.acquisitionValue()))
      }
    )
  }

  //################# Worth when Inherited Actions ########################
  val worthWhenInherited = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[WorthWhenInheritedModel](keystoreKeys.worthWhenInherited).map {
      case Some(data) => Ok(views.worthWhenInherited(worthWhenInheritedForm.fill(data)))
      case None => Ok(views.worthWhenInherited(worthWhenInheritedForm))
    }
  }

  val submitWorthWhenInherited = ValidateSession.async { implicit request =>
    worthWhenInheritedForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthWhenInherited(errors))),
      success => {
        calcConnector.saveFormData(keystoreKeys.worthWhenInherited, success)
        Future.successful(Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# Acquisition Value Actions ########################
  val acquisitionValue = ValidateSession.async { implicit request =>
    calcConnector.fetchAndGetFormData[AcquisitionValueModel](keystoreKeys.acquisitionValue).map {
      case Some(data) => Ok(views.acquisitionValue(acquisitionValueForm.fill(data), homeLink))
      case None => Ok(views.acquisitionValue(acquisitionValueForm, homeLink))
    }
  }

  val submitAcquisitionValue = ValidateSession.async { implicit request =>
    acquisitionValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.acquisitionValue(errors, homeLink))),
      success => {
        calcConnector.saveFormData(keystoreKeys.acquisitionValue, success)
        Future.successful(Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# Acquisition Costs Actions ########################
  private def acquisitionCostsBackLink: (OwnerBeforeLegislationStartModel, Option[DidYouInheritThemModel]) => String = {
      case (x,_) if x.ownerBeforeLegislationStart => routes.GainController.valueBeforeLegislationStart().url
      case (_,y) if y.get.wereInherited => routes.GainController.worthWhenInherited().url
      case (_,_) => routes.GainController.acquisitionValue().url
  }

  val acquisitionCosts = ValidateSession.async { implicit request =>

    def routeRequest(backLink: String): Future[Result] = {
      calcConnector.fetchAndGetFormData[AcquisitionCostsModel](keystoreKeys.acquisitionCosts).map {
        case Some(data) => Ok(views.acquisitionCosts(acquisitionCostsForm.fill(data), Some(backLink), homeLink))
        case None => Ok(views.acquisitionCosts(acquisitionCostsForm, Some(backLink), homeLink))
      }
    }

    for {
      ownedBeforeTax <- calcConnector.fetchAndGetFormData[OwnerBeforeLegislationStartModel](keystoreKeys.ownerBeforeLegislationStart)
      didYouInheritThem <- calcConnector.fetchAndGetFormData[DidYouInheritThemModel](keystoreKeys.didYouInheritThem)
      route <- routeRequest(acquisitionCostsBackLink(ownedBeforeTax.get, didYouInheritThem))
    } yield route
  }

  val submitAcquisitionCosts = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[AcquisitionCostsModel], backLink: String) = {
      Future.successful(BadRequest(views.acquisitionCosts(errors, Some(backLink), homeLink)))
    }

    def successAction(success: AcquisitionCostsModel) = {
      for {
        save <- calcConnector.saveFormData(keystoreKeys.acquisitionCosts, success)
        answers <- calcConnector.getShareGainAnswers
        grossGain <- calcConnector.calculateRttShareGrossGain(answers)
      } yield grossGain match {
        case x if x > 0 => Redirect(routes.DeductionsController.otherDisposals())
        case _ => Redirect(routes.SummaryController.summary())
      }
    }

    def routeRequest(backLink: String): Future[Result] = {
      acquisitionCostsForm.bindFromRequest.fold(
        errors => errorAction(errors, backLink),
        success => successAction(success)
      )
    }

    for {
      ownedBeforeTax <- calcConnector.fetchAndGetFormData[OwnerBeforeLegislationStartModel](keystoreKeys.ownerBeforeLegislationStart)
      didYouInheritThem <- calcConnector.fetchAndGetFormData[DidYouInheritThemModel](keystoreKeys.didYouInheritThem)
      route <- routeRequest(acquisitionCostsBackLink(ownedBeforeTax.get, didYouInheritThem))
    } yield route
  }
}

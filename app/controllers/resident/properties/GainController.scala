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

package controllers.resident.properties

import java.util.UUID

import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import common.{Dates, TaxDates}
import config.{AppConfig, ApplicationConfig}
import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import play.api.mvc.{Action, Result}
import uk.gov.hmrc.play.http.SessionKeys
import scala.concurrent.Future
import views.html.calculation.{resident => commonViews}
import views.html.calculation.resident.properties.{gain => views}
import forms.resident.DisposalValueForm._
import forms.resident.DisposalDateForm._
import forms.resident.DisposalCostsForm._
import forms.resident.AcquisitionValueForm._
import forms.resident.AcquisitionCostsForm._
import forms.resident.properties.ImprovementsForm._
import forms.resident.properties.SellForLessForm._
import forms.resident.properties.gain.OwnerBeforeAprilForm._
import play.api.data.Form
import forms.resident.properties.SellOrGiveAwayForm._
import forms.resident.properties.gain.PropertyWorthWhenSoldForm._
import models.resident.properties.gain.{OwnerBeforeAprilModel, PropertyWorthWhenSoldModel, WorthWhenGiftedModel}
import forms.resident.properties.WorthWhenGaveAwayForm._
import forms.resident.properties.HowBecameOwnerForm._
import forms.resident.properties.WorthOnForm._
import models.resident.properties.{HowBecameOwnerModel, ImprovementsModel, SellOrGiveAwayModel, SellForLessModel, WorthWhenGaveAwayModel, WorthOnModel}
import forms.resident.properties.WorthWhenInheritedForm._
import forms.resident.properties.gain.WorthWhenGiftedForm._
import forms.resident.properties.BoughtForLessThanWorthForm._
import forms.resident.properties.WorthWhenBoughtForm._

import models.resident._
import models.resident.properties._

import play.api.i18n.Messages

object GainController extends GainController {
  val calcConnector = CalculatorConnector
  val config = ApplicationConfig
}

trait GainController extends FeatureLock {

  val calcConnector: CalculatorConnector
  val config: AppConfig

  val navTitle = Messages("calc.base.resident.properties.home")
  override val homeLink = controllers.resident.properties.routes.PropertiesController.introduction().url
  override val sessionTimeoutUrl = homeLink

  //################# Disposal Date Actions ####################
  val disposalDate = FeatureLockForRTT.asyncNoTimeout { implicit request =>
    if (request.session.get(SessionKeys.sessionId).isEmpty) {
      val sessionId = UUID.randomUUID.toString
      Future.successful(Ok(views.disposalDate(disposalDateForm)).withSession(request.session + (SessionKeys.sessionId -> s"session-$sessionId")))
    }
    else {
      calcConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate).map {
        case Some(data) => Ok(views.disposalDate(disposalDateForm.fill(data)))
        case None => Ok(views.disposalDate(disposalDateForm))
      }
    }
  }

  val submitDisposalDate = Action.async { implicit request =>

    def routeRequest(taxYearResult: Option[TaxYearModel]): Future[Result] = {
      if (taxYearResult.isDefined && !taxYearResult.get.isValidYear) Future.successful(Redirect(routes.GainController.outsideTaxYears()))
      else Future.successful(Redirect(routes.GainController.sellOrGiveAway()))
    }

    disposalDateForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.disposalDate(errors))),
      success => {
        for {
          save <- calcConnector.saveFormData(keystoreKeys.disposalDate, success)
          taxYearResult <- calcConnector.getTaxYear(s"${success.year}-${success.month}-${success.day}")
          route <- routeRequest(taxYearResult)
        } yield route
      }
    )
  }

  //################ Sell or Give Away Actions ######################
  val sellOrGiveAway = FeatureLockForRTT.async { implicit request =>
    val backUrl = routes.GainController.disposalDate().url
    val postAction = controllers.resident.properties.routes.GainController.submitSellOrGiveAway()

    calcConnector.fetchAndGetFormData[SellOrGiveAwayModel](keystoreKeys.sellOrGiveAway).map {
      case Some(data) => Ok(views.sellOrGiveAway(sellOrGiveAwayForm.fill(data), Some(backUrl), homeLink, postAction))
      case _ => Ok(views.sellOrGiveAway(sellOrGiveAwayForm, Some(backUrl), homeLink, postAction))
    }
  }

  val submitSellOrGiveAway = FeatureLockForRTT.async { implicit request =>
    val backUrl = routes.GainController.disposalDate().url
    val postAction = controllers.resident.properties.routes.GainController.submitSellOrGiveAway()

    sellOrGiveAwayForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.sellOrGiveAway(errors, Some(backUrl), homeLink, postAction))),
      success => {
        calcConnector.saveFormData[SellOrGiveAwayModel](keystoreKeys.sellOrGiveAway, success)
        success match {
          case SellOrGiveAwayModel(true) => Future.successful(Redirect(routes.GainController.whoDidYouGiveItTo()))
          case SellOrGiveAwayModel(false) => Future.successful(Redirect(routes.GainController.sellForLess()))
        }
      }
    )
  }

  //################ Who Did You Give It To Actions ######################
  val whoDidYouGiveItTo = FeatureLockForRTT.async { implicit request =>
    TODO.apply(request)
  }

  //################ Outside Tax Years Actions ######################
  val outsideTaxYears = FeatureLockForRTT.async { implicit request =>
    for {
      disposalDate <- calcConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
      taxYear <- calcConnector.getTaxYear(s"${disposalDate.get.year}-${disposalDate.get.month}-${disposalDate.get.day}")
    } yield {
      Ok(commonViews.outsideTaxYear(
        taxYear = taxYear.get,
        isAfterApril15 = TaxDates.dateAfterStart(Dates.constructDate(disposalDate.get.day, disposalDate.get.month, disposalDate.get.year)),
        navBackLink = routes.GainController.disposalDate().url,
        navHomeLink = homeLink,
        continueUrl = routes.GainController.disposalValue().url,
        navTitle = navTitle
      ))
    }
  }


  //################# Worth When Gave Away Actions ############################

  private val worthWhenGaveAwayPostAction = controllers.resident.properties.routes.GainController.submitWorthWhenGaveAway()
  private val worthWhenGaveAwayBackLink = Some(controllers.resident.properties.routes.GainController.whoDidYouGiveItTo().toString)

  val worthWhenGaveAway = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[WorthWhenGaveAwayModel](keystoreKeys.worthWhenGaveAway).map {
      case Some(data) => Ok(views.worthWhenGaveAway(worthWhenGaveAwayForm.fill(data), worthWhenGaveAwayBackLink, homeLink, worthWhenGaveAwayPostAction))
      case None => Ok(views.worthWhenGaveAway(worthWhenGaveAwayForm, worthWhenGaveAwayBackLink, homeLink, worthWhenGaveAwayPostAction))
    }
  }

  val submitWorthWhenGaveAway = FeatureLockForRTT.async { implicit request =>
    worthWhenGaveAwayForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthWhenGaveAway(errors, worthWhenGaveAwayBackLink, homeLink, worthWhenGaveAwayPostAction))),
      success => {
        calcConnector.saveFormData[WorthWhenGaveAwayModel](keystoreKeys.worthWhenGaveAway, success)
        Future.successful(Redirect(routes.GainController.disposalCosts()))
      }
    )
  }

  //############## Sell for Less Actions ##################
    val sellForLess = FeatureLockForRTT.async {implicit request =>

    val backLink = Some(controllers.resident.properties.routes.GainController.sellOrGiveAway().toString)

    calcConnector.fetchAndGetFormData[SellForLessModel](keystoreKeys.sellForLess).map{
      case Some(data) => Ok(commonViews.properties.gain.sellForLess(sellForLessForm.fill(data), homeLink, backLink))
      case _ => Ok(commonViews.properties.gain.sellForLess(sellForLessForm, homeLink, backLink))
    }
  }

  val submitSellForLess = FeatureLockForRTT.async { implicit request =>

    lazy val backLink = Some(controllers.resident.properties.GainController.sellOrGiveAway().toString())

    def errorAction(errors: Form[SellForLessModel]) = Future.successful(BadRequest(commonViews.properties.gain.sellForLess(
      errors, homeLink, backLink
    )))

    def routeRequest(model: SellForLessModel) = {
      if (model.sellForLess) Future.successful(Redirect(routes.GainController.propertyWorthWhenSold()))
      else Future.successful(Redirect(routes.GainController.disposalValue()))
    }

    def successAction(model: SellForLessModel) = {
      for {
        save <- calcConnector.saveFormData(keystoreKeys.sellForLess, model)
        route <- routeRequest(model)
      } yield route
    }

    sellForLessForm.bindFromRequest().fold(errorAction, successAction)

  }

  //################ Disposal Value Actions ######################
  val disposalValue = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisposalValueModel](keystoreKeys.disposalValue).map {
      case Some(data) => Ok(views.disposalValue(disposalValueForm.fill(data)))
      case None => Ok(views.disposalValue(disposalValueForm))
    }
  }

  val submitDisposalValue = FeatureLockForRTT.async { implicit request =>
    disposalValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.disposalValue(errors))),
      success => {
        calcConnector.saveFormData[DisposalValueModel](keystoreKeys.disposalValue, success)
        Future.successful(Redirect(routes.GainController.disposalCosts()))
      }
    )
  }

  //################ Property Worth When Sold Actions ######################
  val propertyWorthWhenSold = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[PropertyWorthWhenSoldModel](keystoreKeys.propertyWorthWhenSold).map {
      case Some(data) => Ok(views.propertyWorthWhenSold(propertyWorthWhenSoldForm.fill(data)))
      case _ => Ok(views.propertyWorthWhenSold(propertyWorthWhenSoldForm))
    }
  }

  val submitPropertyWorthWhenSold = FeatureLockForRTT.async { implicit request =>

    propertyWorthWhenSoldForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.propertyWorthWhenSold(errors))),
      success => {
        calcConnector.saveFormData(keystoreKeys.propertyWorthWhenSold, success)
        Future.successful(Redirect(routes.GainController.disposalCosts()))
      }
    )
  }

  //################# Disposal Costs Actions ########################
  val disposalCosts = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[DisposalCostsModel](keystoreKeys.disposalCosts).map {
      case Some(data) => Ok(views.disposalCosts(disposalCostsForm.fill(data)))
      case None => Ok(views.disposalCosts(disposalCostsForm))
    }
  }

  val submitDisposalCosts = FeatureLockForRTT.async { implicit request =>
    disposalCostsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.disposalCosts(errors))),
      success => {
        calcConnector.saveFormData(keystoreKeys.disposalCosts, success)
        Future.successful(Redirect(routes.GainController.ownerBeforeAprilNineteenEightyTwo()))}
    )
  }

  //################# Owner Before April Eighty Two Actions ########################
  val ownerBeforeAprilNineteenEightyTwo = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[OwnerBeforeAprilModel](keystoreKeys.ownerBeforeAprilNineteenEightyTwo).map {
      case Some(data) => Ok(views.ownerBeforeApril(ownerBeforeAprilForm.fill(data)))
      case None => Ok(views.ownerBeforeApril(ownerBeforeAprilForm))
    }
  }

  val submitOwnerBeforeAprilNineteenEightyTwo = FeatureLockForRTT.async { implicit request =>

    def errorAction(errors: Form[OwnerBeforeAprilModel]) = Future.successful(BadRequest(views.ownerBeforeApril(errors)))

    def routeRequest(model: OwnerBeforeAprilModel) = {
      if (model.ownedBeforeAprilNineteenEightyTwo) Future.successful(Redirect(routes.GainController.worthOn()))
      else Future.successful(Redirect(routes.GainController.howBecameOwner()))
    }

    def successAction(model: OwnerBeforeAprilModel) = {
      for {
        save <- calcConnector.saveFormData(keystoreKeys.ownerBeforeAprilNineteenEightyTwo, model)
        route <- routeRequest(model)
      } yield route
    }

    ownerBeforeAprilForm.bindFromRequest().fold(errorAction, successAction)
  }

  //################# Property Worth on 31/03/1982 Actions ########################

  val worthOn = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[WorthOnModel](keystoreKeys.worthOn).map {
      case Some(data) => Ok(views.worthOn(worthOnForm.fill(data)))
      case None => Ok(views.worthOn(worthOnForm))
    }
  }

  val submitWorthOn = FeatureLockForRTT.async { implicit request =>
    worthOnForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthOn(errors))),
      success => {
        calcConnector.saveFormData[WorthOnModel](keystoreKeys.worthOn, success)
        Future.successful(Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# How Became Owner Actions ########################
  val howBecameOwner = FeatureLockForRTT.async { implicit request =>
    val backLink = Some(controllers.resident.properties.routes.GainController.ownerBeforeAprilNineteenEightyTwo().url)
    val postAction = controllers.resident.properties.routes.GainController.submitHowBecameOwner()

    calcConnector.fetchAndGetFormData[HowBecameOwnerModel](keystoreKeys.howBecameOwner).map {
      case Some(data) => Ok(views.howBecameOwner(howBecameOwnerForm.fill(data), backLink, homeLink, postAction))
      case _ => Ok(views.howBecameOwner(howBecameOwnerForm, backLink, homeLink, postAction))
    }
  }

  val submitHowBecameOwner = FeatureLockForRTT.async { implicit request =>
    val backLink = Some(controllers.resident.properties.routes.GainController.ownerBeforeAprilNineteenEightyTwo().url)
    val postAction = controllers.resident.properties.routes.GainController.submitHowBecameOwner()

    howBecameOwnerForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.howBecameOwner(errors, backLink, homeLink, postAction))),
      success => {
        calcConnector.saveFormData(keystoreKeys.howBecameOwner, success)
        success.gainedBy match {
          case "Gifted" => Future.successful(Redirect(routes.GainController.worthWhenGifted()))
          case "Inherited" => Future.successful(Redirect(routes.GainController.worthWhenInherited()))
          case _ => Future.successful(Redirect(routes.GainController.boughtForLessThanWorth()))
        }
      }
    )
  }

  //################# Bought For Less Than Worth Actions ########################
  val boughtForLessThanWorth = FeatureLockForRTT.async {implicit request =>

    val backLink = Some(controllers.resident.properties.routes.GainController.howBecameOwner().toString)

    calcConnector.fetchAndGetFormData[BoughtForLessThanWorthModel](keystoreKeys.boughtForLessThanWorth).map{
      case Some(data) => Ok(commonViews.properties.gain.buyForLess(boughtForLessThanWorthForm.fill(data), homeLink, backLink))
      case _ => Ok(commonViews.properties.gain.buyForLess(boughtForLessThanWorthForm, homeLink, backLink))
    }
  }

  val submitBoughtForLessThanWorth = FeatureLockForRTT.async { implicit request =>

    lazy val backLink = Some(controllers.resident.properties.GainController.howBecameOwner().toString())

    def errorAction(errors: Form[BoughtForLessThanWorthModel]) = Future.successful(BadRequest(commonViews.properties.gain.buyForLess(
      errors, homeLink, backLink
    )))

    def routeRequest(model: BoughtForLessThanWorthModel) = {
      if (model.boughtForLessThanWorth) Future.successful(Redirect(routes.GainController.worthWhenBought()))
      else Future.successful(Redirect(routes.GainController.acquisitionValue()))
    }

    def successAction(model: BoughtForLessThanWorthModel) = {
      for {
        save <- calcConnector.saveFormData(keystoreKeys.boughtForLessThanWorth, model)
        route <- routeRequest(model)
      } yield route
    }

    boughtForLessThanWorthForm.bindFromRequest().fold(errorAction, successAction)

  }

  //################# Worth When Inherited Actions ########################
  val worthWhenInherited = FeatureLockForRTT.async {implicit request =>
    val backLink = Some(controllers.resident.properties.routes.GainController.howBecameOwner().url)
    val postAction = controllers.resident.properties.routes.GainController.submitWorthWhenInherited()

    calcConnector.fetchAndGetFormData[WorthWhenInheritedModel](keystoreKeys.worthWhenInherited).map {
      case Some(data) => Ok(views.worthWhenInherited(worthWhenInheritedForm.fill(data), backLink, homeLink, postAction))
      case _ => Ok(views.worthWhenInherited(worthWhenInheritedForm, backLink, homeLink, postAction))
    }
  }

  val submitWorthWhenInherited = FeatureLockForRTT.async { implicit request =>
    val backLink = Some(controllers.resident.properties.routes.GainController.howBecameOwner().url)
    val postAction = controllers.resident.properties.routes.GainController.submitWorthWhenInherited()

    worthWhenInheritedForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthWhenInherited(errors, backLink, homeLink, postAction))),
      success => {
        calcConnector.saveFormData(keystoreKeys.worthWhenInherited, success)
        Future.successful(Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# Worth When Gifted Actions ########################
   val worthWhenGifted = FeatureLockForRTT.async {implicit request =>
    val backLink = Some(controllers.resident.properties.routes.GainController.howBecameOwner().url)
    val postAction = controllers.resident.properties.routes.GainController.submitWorthWhenGifted()

    calcConnector.fetchAndGetFormData[WorthWhenGiftedModel](keystoreKeys.worthWhenGifted).map {
      case Some(data) => Ok(views.worthWhenGifted(worthWhenGiftedForm.fill(data), backLink, homeLink, postAction))
      case _ => Ok(views.worthWhenGifted(worthWhenGiftedForm, backLink, homeLink, postAction))
    }
  }

  val submitWorthWhenGifted = FeatureLockForRTT.async { implicit request =>
    val backLink = Some(controllers.resident.properties.routes.GainController.howBecameOwner().url)
    val postAction = controllers.resident.properties.routes.GainController.submitWorthWhenGifted()

    worthWhenGiftedForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthWhenGifted(errors, backLink, homeLink, postAction))),
      success => {
        calcConnector.saveFormData(keystoreKeys.worthWhenGifted, success)
        Future.successful(Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# Worth When Bought Actions ########################

  val worthWhenBought = FeatureLockForRTT.async {implicit request =>
    calcConnector.fetchAndGetFormData[WorthWhenBoughtModel](keystoreKeys.worthWhenBought).map {
      case Some(data) => Ok(views.worthWhenBought(worthWhenBoughtForm.fill(data)))
      case _ => Ok(views.worthWhenBought(worthWhenBoughtForm))
    }
  }

  val submitWorthWhenBought = FeatureLockForRTT.async { implicit request =>
    worthWhenBoughtForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthWhenBought(errors))),
      success => {
        calcConnector.saveFormData(keystoreKeys.worthWhenBought, success)
        Future.successful(Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# Acquisition Value Actions ########################
  val acquisitionValue = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[AcquisitionValueModel](keystoreKeys.acquisitionValue).map {
      case Some(data) => Ok(views.acquisitionValue(acquisitionValueForm.fill(data)))
      case None => Ok(views.acquisitionValue(acquisitionValueForm))
    }
  }

  val submitAcquisitionValue = FeatureLockForRTT.async { implicit request =>
    acquisitionValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.acquisitionValue(errors))),
      success => {
        calcConnector.saveFormData(keystoreKeys.acquisitionValue, success)
        Future.successful(Redirect(routes.GainController.acquisitionCosts()))
      }
    )
  }

  //################# Acquisition Costs Actions ########################
  val acquisitionCosts = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[AcquisitionCostsModel](keystoreKeys.acquisitionCosts).map {
      case Some(data) => Ok(views.acquisitionCosts(acquisitionCostsForm.fill(data)))
      case None => Ok(views.acquisitionCosts(acquisitionCostsForm))
    }
  }

  val submitAcquisitionCosts = FeatureLockForRTT.async { implicit request =>
    acquisitionCostsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.acquisitionCosts(errors))),
      success => {
        calcConnector.saveFormData(keystoreKeys.acquisitionCosts, success)
        Future.successful(Redirect(routes.GainController.improvements()))}
    )
  }

  //################# Improvements Actions ########################
  val improvements = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[ImprovementsModel](keystoreKeys.improvements).map {
      case Some(data) => Ok(views.improvements(improvementsForm.fill(data)))
      case None => Ok(views.improvements(improvementsForm))
    }
  }

  val submitImprovements = FeatureLockForRTT.async { implicit request =>

    def routeRequest(totalGain: BigDecimal): Future[Result] = {
      if (totalGain > 0) Future.successful(Redirect(routes.DeductionsController.propertyLivedIn()))
      else Future.successful(Redirect(routes.SummaryController.summary()))
    }

    improvementsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.improvements(errors))),
      success => {
        for {
          save <- calcConnector.saveFormData(keystoreKeys.improvements, success)
          answers <- calcConnector.getPropertyGainAnswers
          grossGain <- calcConnector.calculateRttPropertyGrossGain(answers)
          route <- routeRequest(grossGain)
        } yield route
      }
    )
  }
}
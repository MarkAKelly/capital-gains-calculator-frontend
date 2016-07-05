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
import models.resident._
import models.resident.AllowableLossesValueModel
import forms.resident.LossesBroughtForwardForm._
import forms.resident.LossesBroughtForwardValueForm._
import forms.resident.AllowableLossesForm._
import forms.resident.ReliefsValueForm._
import forms.resident.AllowableLossesValueForm._
import forms.resident.AnnualExemptAmountForm._
import forms.resident.OtherPropertiesForm._
import forms.resident.ReliefsForm._
import play.api.mvc.{Action, Result}
import play.api.data.Form
import views.html.calculation.{resident => views}
import uk.gov.hmrc.play.http.HeaderCarrier
import scala.concurrent.Future

object DeductionsController extends DeductionsController {
  val calcConnector = CalculatorConnector
}

trait DeductionsController extends FeatureLock {

  val calcConnector: CalculatorConnector

  //################# Reliefs Actions ########################

  def totalGain(answerSummary: YourAnswersSummaryModel, hc: HeaderCarrier): Future[BigDecimal] = calcConnector.calculateRttGrossGain(answerSummary)(hc)

  def answerSummary(hc: HeaderCarrier): Future[YourAnswersSummaryModel] = calcConnector.getYourAnswers(hc)

  val reliefs = FeatureLockForRTT.async { implicit request =>

    def routeRequest(totalGain: BigDecimal): Future[Result] = {
      calcConnector.fetchAndGetFormData[ReliefsModel](KeystoreKeys.ResidentKeys.reliefs).map {
        case Some(data) => Ok(views.reliefs(reliefsForm.fill(data), totalGain))
        case None => Ok(views.reliefs(reliefsForm, totalGain))
      }
    }

    for {
      answerSummary <- answerSummary(hc)
      totalGain <- totalGain(answerSummary, hc)
      route <- routeRequest(totalGain)
    } yield route
  }

  val submitReliefs = FeatureLockForRTT.async { implicit request =>
    def routeRequest(errors: Form[ReliefsModel], totalGain: BigDecimal): Future[Result] = {
      Future.successful(BadRequest(views.reliefs(errors, totalGain)))
    }

    reliefsForm.bindFromRequest.fold(
      errors => {for {
        answerSummary <- answerSummary(hc)
        totalGain <- totalGain(answerSummary, hc)
        route <- routeRequest(errors, totalGain)
      } yield route},
      success => {
        calcConnector.saveFormData[ReliefsModel](KeystoreKeys.ResidentKeys.reliefs, success)
        success match {
          case ReliefsModel(true) => Future.successful(Redirect(routes.DeductionsController.reliefsValue()))
          case _ => Future.successful(Redirect(routes.DeductionsController.otherProperties()))
        }
      }
    )
  }

  //################# Reliefs Value Input Actions ########################

  val reliefsValue = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[ReliefsValueModel](KeystoreKeys.ResidentKeys.reliefsValue).map {
      case Some(data) => Ok(views.reliefsValue(reliefsValueForm.fill(data)))
      case None => Ok(views.reliefsValue(reliefsValueForm))
    }
  }

  val submitReliefsValue = FeatureLockForRTT.async { implicit request =>
    reliefsValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.reliefsValue(errors))),
      success => {
        calcConnector.saveFormData[ReliefsValueModel](KeystoreKeys.ResidentKeys.reliefsValue, success)
        Future.successful(Redirect(routes.DeductionsController.otherProperties()))
      }
    )
  }

  //################# Other Properties Actions #########################
  def otherPropertiesBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    calcConnector.fetchAndGetFormData[ReliefsModel](KeystoreKeys.ResidentKeys.reliefs).flatMap {
      case Some(ReliefsModel(true)) => Future.successful(routes.DeductionsController.reliefsValue().url)
      case _ => Future.successful(routes.DeductionsController.reliefs().url)
    }
  }

  val otherProperties = FeatureLockForRTT.async { implicit request =>

    def routeRequest(backUrl: String): Future[Result] = {
      calcConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.ResidentKeys.otherProperties).map {
        case Some(data) => Ok(views.otherProperties(otherPropertiesForm.fill(data), backUrl))
        case None => Ok(views.otherProperties(otherPropertiesForm, backUrl))
      }
    }

    for {
      backUrl <- otherPropertiesBackUrl
      finalResult <- routeRequest(backUrl)
    } yield finalResult
  }

  val submitOtherProperties = FeatureLockForRTT.async { implicit request =>

    def routeRequest(backUrl: String): Future[Result] = {
      otherPropertiesForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(views.otherProperties(errors, backUrl))),
        success => {
          calcConnector.saveFormData[OtherPropertiesModel](KeystoreKeys.ResidentKeys.otherProperties, success)
          if (success.hasOtherProperties) {
            Future.successful(Redirect(routes.DeductionsController.allowableLosses()))
          } else {
            Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
          }
        }
      )
    }
    for {
      backUrl <- otherPropertiesBackUrl
      route <- routeRequest(backUrl)
    } yield route
  }

  //################# Allowable Losses Actions #########################
  val allowableLosses = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[AllowableLossesModel](KeystoreKeys.ResidentKeys.allowableLosses).map {
      case Some(data) => Ok(views.allowableLosses(allowableLossesForm.fill(data)))
      case None => Ok(views.allowableLosses(allowableLossesForm))
    }
  }

  val submitAllowableLosses = FeatureLockForRTT.async { implicit request =>
    allowableLossesForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.allowableLosses(errors))),
      success => {
        calcConnector.saveFormData[AllowableLossesModel](KeystoreKeys.ResidentKeys.allowableLosses, success)
        if (success.isClaiming) {
          Future.successful(Redirect(routes.DeductionsController.allowableLossesValue()))
        }
        else {
          Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
        }
      }
    )
  }

  //################# Allowable Losses Value Actions ############################
  val allowableLossesValue = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[AllowableLossesValueModel](KeystoreKeys.ResidentKeys.allowableLossesValue).map {
      case Some(data) => Ok(views.allowableLossesValue(allowableLossesValueForm.fill(data)))
      case None => Ok(views.allowableLossesValue(allowableLossesValueForm))
    }
  }

  val submitAllowableLossesValue = FeatureLockForRTT.async { implicit request =>
    allowableLossesValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.allowableLossesValue(errors))),
      success => {
        calcConnector.saveFormData[AllowableLossesValueModel](KeystoreKeys.ResidentKeys.allowableLossesValue, success)
        Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
      }
    )
  }

  //################# Brought Forward Losses Actions ############################

  def otherPropertiesCheck(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.ResidentKeys.otherProperties).map {
      case Some(data) => data.hasOtherProperties
      case None => false
    }
  }

  def allowableLossesCheck(implicit hc: HeaderCarrier): Future[Boolean] ={
    calcConnector.fetchAndGetFormData[AllowableLossesModel](KeystoreKeys.ResidentKeys.allowableLosses).map {
      case Some(AllowableLossesModel(false)) => false
      case Some(AllowableLossesModel(true)) => true
      case None => false
    }
  }

  def lossesBroughtForwardBackUrl(implicit hc: HeaderCarrier): Future[String] = {

    for {
      otherPropertiesClaimed <- otherPropertiesCheck
      allowableLossesClaimed <- allowableLossesCheck
    } yield (otherPropertiesClaimed, allowableLossesClaimed)
        
    match {
      case (false, _) => routes.DeductionsController.otherProperties().url
      case (true, false) => routes.DeductionsController.allowableLosses().url
      case (true, true) => routes.DeductionsController.allowableLossesValue().url
    }
  }

  val lossesBroughtForward = FeatureLockForRTT.async { implicit request =>

    def routeRequest(backUrl: String): Future[Result] = {
      calcConnector.fetchAndGetFormData[LossesBroughtForwardModel](KeystoreKeys.ResidentKeys.lossesBroughtForward).map {
        case Some(data) => Ok(views.lossesBroughtForward(lossesBroughtForwardForm.fill(data), backUrl))
        case _ => Ok(views.lossesBroughtForward(lossesBroughtForwardForm, backUrl))
      }
    }
    for {
      backUrl <- lossesBroughtForwardBackUrl
      finalResult <- routeRequest(backUrl)
    } yield finalResult
  }

  def claimingBFLossesCheck(model: LossesBroughtForwardModel)(implicit hc: HeaderCarrier): Future[Boolean] = {
    Future(model.option)
  }

  def positiveChargeableGainCheck(implicit hc: HeaderCarrier): Future[Boolean] = {
    for {
      gainAnswers <- calcConnector.getYourAnswers
      chargeableGainAnswers <- calcConnector.getChargeableGainAnswers
      chargeableGain <- calcConnector.calculateRttChargeableGain(gainAnswers, chargeableGainAnswers, 11000).map(_.get.chargeableGain)
    } yield chargeableGain

    match {
      case result if result.>(0) => true
      case _ => false
    }
  }

  val submitLossesBroughtForward = FeatureLockForRTT.async { implicit request =>
    def routeRequest(backUrl: String): Future[Result] = {
      lossesBroughtForwardForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(views.lossesBroughtForward(errors, backUrl))),
        success => {
          calcConnector.saveFormData[LossesBroughtForwardModel](KeystoreKeys.ResidentKeys.lossesBroughtForward, success)
          for {
            broughtForwardLossesClaimed <- claimingBFLossesCheck(success)
            otherPropertiesClaimed <- otherPropertiesCheck
            positiveChargeableGain <- positiveChargeableGainCheck
          } yield (broughtForwardLossesClaimed, otherPropertiesClaimed, positiveChargeableGain)

          match {
            case (true, _, _) => Redirect(routes.DeductionsController.lossesBroughtForwardValue())
            case (false, true, _) => Redirect(routes.DeductionsController.annualExemptAmount())
            case (false, false, true) => Redirect(routes.IncomeController.currentIncome())
            case _ => Redirect(routes.SummaryController.summary())
          }
        }
      )
    }
    for {
      backUrl <- lossesBroughtForwardBackUrl
      route <- routeRequest(backUrl)
    } yield route
  }

  //################# Brought Forward Losses Value Actions ##############################
  val lossesBroughtForwardValue = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[LossesBroughtForwardValueModel](KeystoreKeys.ResidentKeys.lossesBroughtForwardValue).map {
      case Some(data) => Ok(views.lossesBroughtForwardValue(lossesBroughtForwardValueForm.fill(data)))
      case None => Ok(views.lossesBroughtForwardValue(lossesBroughtForwardValueForm))
    }
  }

  val submitLossesBroughtForwardValue = FeatureLockForRTT.async { implicit request =>
    lossesBroughtForwardValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.lossesBroughtForwardValue(errors))),
      success => {
        calcConnector.saveFormData[LossesBroughtForwardValueModel](KeystoreKeys.ResidentKeys.lossesBroughtForwardValue, success)
        calcConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.ResidentKeys.otherProperties).flatMap {
        case Some(OtherPropertiesModel(true)) => Future.successful(Redirect(routes.DeductionsController.annualExemptAmount()))
        case _ => Future.successful(Redirect(routes.SummaryController.summary()))}
      }
    )
  }

  //################# Annual Exempt Amount Input Actions #############################
  val annualExemptAmount = FeatureLockForRTT.async { implicit request =>
    calcConnector.fetchAndGetFormData[AnnualExemptAmountModel](KeystoreKeys.ResidentKeys.annualExemptAmount).map {
      case Some(data) => Ok(views.annualExemptAmount(annualExemptAmountForm.fill(data)))
      case None => Ok(views.annualExemptAmount(annualExemptAmountForm))
    }
  }
  val submitAnnualExemptAmount = FeatureLockForRTT.async { implicit request =>
    annualExemptAmountForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.annualExemptAmount(errors))),
      success => {
        calcConnector.saveFormData(KeystoreKeys.ResidentKeys.annualExemptAmount, success)
        Future.successful(Redirect(routes.SummaryController.summary()))}
    )
  }

  //################# Second Summary Actions ###############################

}
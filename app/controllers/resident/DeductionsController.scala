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
import models.resident.{OtherPropertiesModel, ReliefsModel, ReliefsValueModel, YourAnswersSummaryModel}
import forms.resident.ReliefsValueForm._
import play.api.mvc.{Action, Result}
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation.{resident => views}
import forms.resident.OtherPropertiesForm._
import forms.resident.ReliefsForm._
import play.api.data.Form

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

  val submitReliefs = Action.async {implicit request =>

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

  val submitOtherProperties = Action.async { implicit request =>

    def routeRequest(backUrl: String): Future[Result] = {
      otherPropertiesForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(views.otherProperties(errors, backUrl))),
        success => {
          calcConnector.saveFormData[OtherPropertiesModel](KeystoreKeys.ResidentKeys.otherProperties, success)
          success match {
            case OtherPropertiesModel(true) => Future.successful(Redirect(routes.DeductionsController.allowableLosses()))
            case _ => Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
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
  val allowableLosses = Action.async { implicit request =>
    Future.successful(Ok(views.allowableLosses()))
  }

  //################# Allowable Losses Value Actions ############################
  val allowableLossesValue = Action.async { implicit request =>
    Future.successful(Ok(views.allowableLossesValue()))
  }

  //################# Brought Forward Losses Actions ############################
  val lossesBroughtForward = Action.async { implicit request =>
    Future.successful(Ok(views.lossesBroughtForward()))
  }

  //################# Brought Forward Losses Value Actions ##############################
  val lossesBroughtForwardValue = Action.async { implicit request =>
    Future.successful(Ok(views.lossesBroughtForwardValue()))
  }

  //################# Annual Exempt Amount Input Actions #############################
  val annualExemptAmount = Action.async { implicit request =>
    Future.successful(Ok(views.annualExemptAmount()))

  }

  //################# Second Summary Actions ###############################

}
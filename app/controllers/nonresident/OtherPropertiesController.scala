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
import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.nonresident.OtherPropertiesForm._
import models.nonresident.{AnnualExemptAmountModel, CurrentIncomeModel, CustomerTypeModel, OtherPropertiesModel}
import play.api.data.Form
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object OtherPropertiesController extends OtherPropertiesController {
  val calcConnector = CalculatorConnector
}

trait OtherPropertiesController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector

  private def otherPropertiesBackUrl(implicit hc: HeaderCarrier): Future[String] =
    calcConnector.fetchAndGetFormData[CustomerTypeModel](KeystoreKeys.customerType).flatMap {
      case Some(CustomerTypeModel("individual")) =>
        calcConnector.fetchAndGetFormData[CurrentIncomeModel](KeystoreKeys.currentIncome).flatMap {
          case Some(data) if data.currentIncome == 0 => Future.successful(routes.CurrentIncomeController.currentIncome().url)
          case _ => Future.successful(routes.CalculationController.personalAllowance().url)
        }
      case Some(CustomerTypeModel("trustee")) => Future.successful(routes.DisabledTrusteeController.disabledTrustee().url)
      case Some(_) => Future.successful(routes.CustomerTypeController.customerType().url)
      case _ => Future.successful(missingDataRoute)
    }

  private def showOtherPropertiesAmt(implicit hc: HeaderCarrier): Future[Boolean] =
    calcConnector.fetchAndGetFormData[CustomerTypeModel](KeystoreKeys.customerType).map {
      case Some(CustomerTypeModel("individual")) => true
      case _ => false
    }

  val otherProperties = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String, showHiddenQuestion: Boolean): Future[Result] = {
      calcConnector.fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.otherProperties).map {
        case Some(data) => Ok(calculation.nonresident.otherProperties(otherPropertiesForm(showHiddenQuestion).fill(data), backUrl, showHiddenQuestion))
        case _ => Ok(calculation.nonresident.otherProperties(otherPropertiesForm(showHiddenQuestion), backUrl, showHiddenQuestion))
      }
    }

    for {
      backUrl <- otherPropertiesBackUrl
      showHiddenQuestion <- showOtherPropertiesAmt
      finalResult <- routeRequest(backUrl, showHiddenQuestion)
    } yield finalResult
  }

  val submitOtherProperties = ValidateSession.async { implicit request =>

    def errorAction(form: Form[OtherPropertiesModel]) = {
      for {
        backUrl <- otherPropertiesBackUrl
        showHiddenQuestion <- showOtherPropertiesAmt
        result <- Future.successful(BadRequest(calculation.nonresident.otherProperties(form, backUrl, showHiddenQuestion)))
      } yield result
    }

    def successAction(model: OtherPropertiesModel, showHiddenQuestion: Boolean) = {
      calcConnector.saveFormData(KeystoreKeys.otherProperties, model)
      model match {
        case OtherPropertiesModel("Yes", Some(value)) if value.equals(BigDecimal(0)) =>
          Future.successful(Redirect(routes.AnnualExemptAmountController.annualExemptAmount()))
        case OtherPropertiesModel("Yes", None) if !showHiddenQuestion =>
          Future.successful(Redirect(routes.AnnualExemptAmountController.annualExemptAmount()))
        case _ => calcConnector.saveFormData(KeystoreKeys.annualExemptAmount, AnnualExemptAmountModel(0))
          Future.successful(Redirect(routes.AcquisitionDateController.acquisitionDate()))
      }
    }

    def routeRequest(showHiddenQuestion: Boolean): Future[Result] = {
      otherPropertiesForm(showHiddenQuestion).bindFromRequest.fold(errorAction, success => successAction(success, showHiddenQuestion))
    }
    for {
      showHiddenQuestion <- showOtherPropertiesAmt
      finalResult <- routeRequest(showHiddenQuestion)
    } yield finalResult
  }
}

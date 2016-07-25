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

import common.Dates
import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import views.html.calculation.{resident => commonViews}
import forms.resident.income.PersonalAllowanceForm._
import models.resident._
import models.resident.income._
import play.api.mvc.Result
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object IncomeController extends IncomeController {
  val calcConnector = CalculatorConnector
}

trait IncomeController extends FeatureLock {

  val calcConnector: CalculatorConnector

  def getDisposalDate(implicit hc: HeaderCarrier): Future[Option[DisposalDateModel]] = {
    calcConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
  }

  def formatDisposalDate(disposalDateModel: DisposalDateModel): Future[String] = {
    Future.successful(s"${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}")
  }
  //################################# Previous Taxable Gain Actions ##########################################

    val previousTaxableGains = TODO

  //################################# Current Income Actions ##########################################

    val currentIncome = TODO

  //################################# Personal Allowance Actions ##########################################
  def getStandardPA(year: Int, hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    calcConnector.getPA(year)(hc)
  }

  def taxYearValue(taxYear: String): Future[Int] = {
    Future.successful(Dates.taxYearStringToInteger(taxYear))
  }

  val personalAllowance = FeatureLockForRTTShares.async { implicit request =>
    def routeRequest(taxYearModel: TaxYearModel, standardPA: BigDecimal): Future[Result] = {
      calcConnector.fetchAndGetFormData[PersonalAllowanceModel](keystoreKeys.personalAllowance).map {
        case Some(data) => Ok(commonViews.personalAllowance(personalAllowanceForm().fill(data), taxYearModel, standardPA))
        case None => Ok(commonViews.personalAllowance(personalAllowanceForm(), taxYearModel, standardPA))
      }
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      year <- taxYearValue(taxYear.get.calculationTaxYear)
      standardPA <- getStandardPA(year, hc)
      route <- routeRequest(taxYear.get, standardPA.get)
    } yield route
  }

  val submitPersonalAllowance = FeatureLockForRTTShares.async { implicit request =>

    def getMaxPA(year: Int): Future[Option[BigDecimal]] = {
      calcConnector.getPA(year, true)(hc)
    }

    def routeRequest(maxPA: BigDecimal, standardPA: BigDecimal, taxYearModel: TaxYearModel): Future[Result] = {
      personalAllowanceForm(maxPA).bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.personalAllowance(errors, taxYearModel, standardPA))),
        success => {
          calcConnector.saveFormData(keystoreKeys.personalAllowance, success)
          Future.successful(Redirect(routes.SummaryController.summary()))
        }
      )
    }

    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      year <- taxYearValue(taxYear.get.calculationTaxYear)
      standardPA <- getStandardPA(year, hc)
      maxPA <- getMaxPA(year)
      route <- routeRequest(maxPA.get, standardPA.get, taxYear.get)
    } yield route
  }

}
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

import common.{Dates, TaxDates}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}
import common.resident.JourneyKeys
import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import views.html.calculation.{resident => commonViews}
import views.html.calculation.resident.properties.{income => views}
import forms.resident.income.PreviousTaxableGainsForm._
import forms.resident.income.PersonalAllowanceForm._
import forms.resident.income.CurrentIncomeForm._
import models.resident._
import models.resident.income._
import org.joda.time.DateTime
import play.api.mvc.Result
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.data.Form
import play.api.i18n.Messages

import scala.concurrent.Future

object IncomeController extends IncomeController {
  val calcConnector = CalculatorConnector
}

trait IncomeController extends FeatureLock {

  val calcConnector: CalculatorConnector

  val navTitle = Messages("calc.base.resident.properties.home")

  def otherPropertiesResponse(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[OtherPropertiesModel](keystoreKeys.otherProperties).map {
      case Some(OtherPropertiesModel(response)) => response
      case None => false
    }
  }

  def lossesBroughtForwardResponse(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward).map {
      case Some(LossesBroughtForwardModel(response)) => response
      case None => false
    }
  }

  def annualExemptAmountEntered(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[AnnualExemptAmountModel](keystoreKeys.annualExemptAmount).map {
      case Some(data) => data.amount == 0
      case None => false
    }
  }

  def allowableLossesCheck(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[AllowableLossesModel](keystoreKeys.allowableLosses).map {
      case Some(data) => data.isClaiming
      case None => false
    }
  }

  def displayAnnualExemptAmountCheck(claimedOtherProperties: Boolean, claimedAllowableLosses: Boolean)(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[AllowableLossesValueModel](keystoreKeys.allowableLossesValue).map {
      case Some(result) if claimedAllowableLosses && claimedOtherProperties => result.amount == 0
      case _ if claimedOtherProperties && !claimedAllowableLosses => true
      case _ => false
    }
  }

  def getDisposalDate(implicit hc: HeaderCarrier): Future[Option[DisposalDateModel]] = {
    calcConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
  }

  def formatDisposalDate(disposalDateModel: DisposalDateModel): Future[String] = {
    Future.successful(s"${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}")
  }

  override val homeLink = controllers.resident.properties.routes.PropertiesController.introduction().url
  override val sessionTimeoutUrl = homeLink

  //################################# Previous Taxable Gain Actions ##########################################
  private val previousTaxableGainsPostAction = controllers.resident.properties.routes.IncomeController.submitPreviousTaxableGains()

  def buildPreviousTaxableGainsBackUrl(implicit hc: HeaderCarrier): Future[String] = {

    for {
      hasOtherProperties <- otherPropertiesResponse
      hasAllowableLosses <- allowableLossesCheck
      displayAnnualExemptAmount <- displayAnnualExemptAmountCheck(hasOtherProperties, hasAllowableLosses)
      hasLossesBroughtForward <- lossesBroughtForwardResponse
    } yield (displayAnnualExemptAmount, hasLossesBroughtForward)

    match {
      case (true, _) => routes.DeductionsController.annualExemptAmount().url
      case (false, true) => routes.DeductionsController.lossesBroughtForwardValue().url
      case (false, false) => routes.DeductionsController.lossesBroughtForward().url
    }
  }

  val previousTaxableGains = FeatureLockForRTT.async { implicit request =>

    def routeRequest(backUrl: String, calculationYear: String): Future[Result] = {
      calcConnector.fetchAndGetFormData[PreviousTaxableGainsModel](keystoreKeys.previousTaxableGains).map {
        case Some(data) => Ok(commonViews.previousTaxableGains(previousTaxableGainsForm.fill(data), backUrl,
          previousTaxableGainsPostAction, homeLink, JourneyKeys.properties, calculationYear, navTitle))
        case None => Ok(commonViews.previousTaxableGains(previousTaxableGainsForm, backUrl,
          previousTaxableGainsPostAction, homeLink, JourneyKeys.properties, calculationYear, navTitle))
      }
    }

    for {
      backUrl <- buildPreviousTaxableGainsBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      finalResult <- routeRequest(backUrl, taxYear.get.calculationTaxYear)
    } yield finalResult
  }

  val submitPreviousTaxableGains = FeatureLockForRTT.async { implicit request =>

    def errorAction (errors: Form[PreviousTaxableGainsModel], backUrl: String, taxYear: String) = {
      Future.successful(BadRequest(commonViews.previousTaxableGains(errors, backUrl, previousTaxableGainsPostAction,
        homeLink, JourneyKeys.properties, taxYear, navTitle)))
    }

    previousTaxableGainsForm.bindFromRequest.fold(
      errors => {
        for {
          backUrl <- buildPreviousTaxableGainsBackUrl
          disposalDate <- getDisposalDate
          disposalDateString <- formatDisposalDate(disposalDate.get)
          taxYear <- calcConnector.getTaxYear(disposalDateString)
          page <- errorAction(errors, backUrl, taxYear.get.taxYearSupplied)
        } yield page
      },
      success => {
        calcConnector.saveFormData(keystoreKeys.previousTaxableGains, success)
        Future.successful(Redirect(routes.IncomeController.currentIncome()))
      }
    )
  }

  //################################# Current Income Actions ##########################################

  def buildCurrentIncomeBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    for {
      hasOtherProperties <- otherPropertiesResponse
      hasAllowableLosses <- allowableLossesCheck
      displayAnnualExemptAmount <- displayAnnualExemptAmountCheck(hasOtherProperties, hasAllowableLosses)
      hasLossesBroughtForward <- lossesBroughtForwardResponse
      enteredAnnualExemptAmount <- annualExemptAmountEntered
    } yield (displayAnnualExemptAmount, hasLossesBroughtForward, enteredAnnualExemptAmount)

    match {
      case (true, _, true) => routes.IncomeController.previousTaxableGains().url
      case (true, _, _) => routes.DeductionsController.annualExemptAmount().url
      case (false, true, _) => routes.DeductionsController.lossesBroughtForwardValue().url
      case (false, false, _) => routes.DeductionsController.lossesBroughtForward().url
    }
  }

  val currentIncome = FeatureLockForRTT.async { implicit request =>

    def routeRequest(backUrl: String, taxYear: TaxYearModel, currentTaxYear: String): Future[Result] = {

      val inCurrentTaxYear = taxYear.taxYearSupplied == currentTaxYear

      calcConnector.fetchAndGetFormData[CurrentIncomeModel](keystoreKeys.currentIncome).map {
        case Some(data) => Ok(views.currentIncome(currentIncomeForm.fill(data), backUrl, taxYear, inCurrentTaxYear))
        case None => Ok(views.currentIncome(currentIncomeForm, backUrl, taxYear, inCurrentTaxYear))
      }
    }

    for {
      backUrl <- buildCurrentIncomeBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      currentTaxYear <- Dates.getCurrentTaxYear
      finalResult <- routeRequest(backUrl, taxYear.get, currentTaxYear)
    } yield finalResult
  }

  val submitCurrentIncome = FeatureLockForRTT.async { implicit request =>

    def routeRequest(taxYearModel: TaxYearModel, currentTaxYear: String): Future[Result] = {

      val inCurrentTaxYear = taxYearModel.taxYearSupplied == currentTaxYear

      currentIncomeForm.bindFromRequest.fold(
        errors => buildCurrentIncomeBackUrl.flatMap(url => Future.successful(BadRequest(views.currentIncome(errors, url, taxYearModel, inCurrentTaxYear)))),
        success => {
          calcConnector.saveFormData[CurrentIncomeModel](keystoreKeys.currentIncome, success)
          Future.successful(Redirect(routes.IncomeController.personalAllowance()))
        }
      )
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      currentTaxYear <- Dates.getCurrentTaxYear
      route <- routeRequest(taxYear.get, currentTaxYear)
    } yield route
  }

  //################################# Personal Allowance Actions ##########################################
  def getStandardPA(year: Int, hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    calcConnector.getPA(year)(hc)
  }

  def taxYearValue(taxYear: String): Future[Int] = {
    Future.successful(TaxDates.taxYearStringToInteger(taxYear))
  }

  private val backLinkPersonalAllowance = Some(controllers.resident.properties.routes.IncomeController.currentIncome().toString)
  private val postActionPersonalAllowance = controllers.resident.properties.routes.IncomeController.submitPersonalAllowance()

  val personalAllowance = FeatureLockForRTT.async { implicit request =>

    def fetchKeystorePersonalAllowance(): Future[Form[PersonalAllowanceModel]] = {
      calcConnector.fetchAndGetFormData[PersonalAllowanceModel](keystoreKeys.personalAllowance).map {
        case Some(data) => personalAllowanceForm().fill(data)
        case _ => personalAllowanceForm()
      }
    }

    def routeRequest(taxYearModel: TaxYearModel, standardPA: BigDecimal, formData: Form[PersonalAllowanceModel], currentTaxYear: String):
    Future[Result] = {
      Future.successful(Ok(commonViews.personalAllowance(formData, taxYearModel, standardPA, homeLink,
        postActionPersonalAllowance, backLinkPersonalAllowance, JourneyKeys.properties, navTitle, currentTaxYear)))
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      year <- taxYearValue(taxYear.get.calculationTaxYear)
      standardPA <- getStandardPA(year, hc)
      formData <- fetchKeystorePersonalAllowance()
      currentTaxYear <- Dates.getCurrentTaxYear
      route <- routeRequest(taxYear.get, standardPA.get, formData, currentTaxYear)
    } yield route
  }

  val submitPersonalAllowance = FeatureLockForRTT.async { implicit request =>

    def getMaxPA(year: Int): Future[Option[BigDecimal]] = {
      calcConnector.getPA(year, isEligibleBlindPersonsAllowance = true)(hc)
    }

    def routeRequest(maxPA: BigDecimal, standardPA: BigDecimal, taxYearModel: TaxYearModel, currentTaxYear: String): Future[Result] = {
      personalAllowanceForm(maxPA).bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.personalAllowance(errors, taxYearModel, standardPA, homeLink,
          postActionPersonalAllowance, backLinkPersonalAllowance, JourneyKeys.properties, navTitle, currentTaxYear))),
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
      currentTaxYear <- Dates.getCurrentTaxYear
      route <- routeRequest(maxPA.get, standardPA.get, taxYear.get, currentTaxYear)
    } yield route
  }

}
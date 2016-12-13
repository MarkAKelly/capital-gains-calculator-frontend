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

import common.Dates._
import java.time.LocalDate

import common.Dates
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import models.resident._
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import play.api.mvc.Result
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation.resident.shares.{summary => views}

import scala.concurrent.Future

object SummaryController extends SummaryController {
  val calculatorConnector = CalculatorConnector
}

trait SummaryController extends ValidActiveSession {

  val calculatorConnector: CalculatorConnector

  override val homeLink = controllers.resident.shares.routes.GainController.disposalDate().url
  override val sessionTimeoutUrl = homeLink

  val summary = ValidateSession.async { implicit request =>


    def getMaxAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
      calculatorConnector.getFullAEA(taxYear)
    }

    def taxYearStringToInteger(taxYear: String): Future[Int] = {
      Future.successful((taxYear.take(2) + taxYear.takeRight(2)).toInt)
    }

    def displayAnnualExemptAmountCheck(claimedOtherDisposals: Boolean,
                                       claimedAllowableLosses: Boolean,
                                       allowableLossesValueModel: Option[AllowableLossesValueModel])(implicit hc: HeaderCarrier): Boolean = {
      allowableLossesValueModel match {
        case Some(result) if claimedAllowableLosses && claimedOtherDisposals => result.amount == 0
        case _ if claimedOtherDisposals && !claimedAllowableLosses => true
        case _ => false
      }
    }

    def getChargeableGain(grossGain: BigDecimal,
                          totalGainAnswers: GainAnswersModel,
                          deductionGainAnswers: DeductionGainAnswersModel,
                          maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[ChargeableGainResultModel]] = {
      if (grossGain > 0) calculatorConnector.calculateRttShareChargeableGain(totalGainAnswers, deductionGainAnswers, maxAEA)
      else Future.successful(None)
    }

    def buildDeductionsSummaryBackUrl(deductionGainAnswers: DeductionGainAnswersModel)(implicit hc: HeaderCarrier): Future[String] = {
      (displayAnnualExemptAmountCheck(deductionGainAnswers.otherPropertiesModel.getOrElse(OtherPropertiesModel(false)).hasOtherProperties,
        deductionGainAnswers.allowableLossesModel.getOrElse(AllowableLossesModel(false)).isClaiming,
        deductionGainAnswers.allowableLossesValueModel)
        , deductionGainAnswers.broughtForwardModel.getOrElse(LossesBroughtForwardModel(false)).option) match {
        case (true, _) => Future.successful(routes.DeductionsController.annualExemptAmount().url)
        case (false, true) => Future.successful(routes.DeductionsController.lossesBroughtForwardValue().url)
        case (false, false) => Future.successful(routes.DeductionsController.lossesBroughtForward().url)
      }
    }

    def getTotalTaxableGain(chargeableGain: Option[ChargeableGainResultModel] = None,
                            totalGainAnswers: GainAnswersModel, deductionGainAnswers: DeductionGainAnswersModel,
                            incomeAnswersModel: IncomeAnswersModel,
                            maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[TotalGainAndTaxOwedModel]] = {
      if (chargeableGain.isDefined && chargeableGain.get.chargeableGain > 0 &&
        incomeAnswersModel.personalAllowanceModel.isDefined && incomeAnswersModel.currentIncomeModel.isDefined) {
        calculatorConnector.calculateRttShareTotalGainAndTax(totalGainAnswers, deductionGainAnswers, maxAEA, incomeAnswersModel)
      }
      else Future.successful(None)
    }

    def getTaxYear(disposalDate: LocalDate): Future[Option[TaxYearModel]] = calculatorConnector.getTaxYear(disposalDate.format(requestFormatter))

    def routeRequest(totalGainAnswers: GainAnswersModel,
                     grossGain: BigDecimal,
                     deductionGainAnswers: DeductionGainAnswersModel,
                     chargeableGain: Option[ChargeableGainResultModel],
                     incomeAnswers: IncomeAnswersModel,
                     totalGainAndTax: Option[TotalGainAndTaxOwedModel],
                     backUrl: String,
                     taxYear: Option[TaxYearModel],
                     currentTaxYear: String)(implicit hc: HeaderCarrier): Future[Result] = {

      if (chargeableGain.isDefined && chargeableGain.get.chargeableGain > 0 &&
        incomeAnswers.personalAllowanceModel.isDefined && incomeAnswers.currentIncomeModel.isDefined) Future.successful(
        Ok(views.finalSummary(totalGainAnswers, deductionGainAnswers, incomeAnswers,
          totalGainAndTax.get, routes.IncomeController.personalAllowance().url, taxYear.get, homeLink, taxYear.get.taxYearSupplied == currentTaxYear)))

      else if (grossGain > 0) Future.successful(Ok(views.deductionsSummary(totalGainAnswers, deductionGainAnswers,
        chargeableGain.get, backUrl, taxYear.get, homeLink)))
      else Future.successful(Ok(views.gainSummary(totalGainAnswers, grossGain, taxYear.get, homeLink)))
    }
    for {
      answers <- calculatorConnector.getShareGainAnswers
      taxYear <- getTaxYear(answers.disposalDate)
      taxYearInt <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- getMaxAEA(taxYearInt)(hc)
      grossGain <- calculatorConnector.calculateRttShareGrossGain(answers)
      deductionAnswers <- calculatorConnector.getShareDeductionAnswers
      backLink <- buildDeductionsSummaryBackUrl(deductionAnswers)
      chargeableGain <- getChargeableGain(grossGain, answers, deductionAnswers, maxAEA.get)
      incomeAnswers <- calculatorConnector.getShareIncomeAnswers
      totalGain <- getTotalTaxableGain(chargeableGain, answers, deductionAnswers, incomeAnswers, maxAEA.get)
      currentTaxYear <- Dates.getCurrentTaxYear
      routeRequest <- routeRequest(answers, grossGain, deductionAnswers, chargeableGain, incomeAnswers, totalGain, backLink, taxYear, currentTaxYear)
    } yield routeRequest
  }
}

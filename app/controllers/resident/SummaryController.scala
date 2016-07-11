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

import java.text.SimpleDateFormat
import java.util.Date

import common.KeystoreKeys
import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import models.resident._
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object SummaryController extends SummaryController {
  val calculatorConnector = CalculatorConnector
}

trait SummaryController extends FeatureLock {

  val calculatorConnector: CalculatorConnector

  val summary = FeatureLockForRTT.async { implicit request =>

    def buildPreviousTaxableGainsBackUrl(chargeableGainAnswers: ChargeableGainAnswers)(implicit hc: HeaderCarrier): Future[String] = {
      (chargeableGainAnswers.otherPropertiesModel.getOrElse(OtherPropertiesModel(false)).hasOtherProperties
        , chargeableGainAnswers.broughtForwardModel.getOrElse(LossesBroughtForwardModel(false)).option) match {
        case (true, _) => Future.successful(routes.DeductionsController.annualExemptAmount().url)
        case (false, true) => Future.successful(routes.DeductionsController.lossesBroughtForwardValue().url)
        case (false, false) => Future.successful(routes.DeductionsController.lossesBroughtForward().url)
      }
    }

    def chargeableGain(grossGain: BigDecimal,
                       yourAnswersSummaryModel: YourAnswersSummaryModel,
                       chargeableGainAnswers: ChargeableGainAnswers)(implicit hc: HeaderCarrier): Future[Option[ChargeableGainResultModel]] = {
      if (grossGain > 0) calculatorConnector.calculateRttChargeableGain(yourAnswersSummaryModel, chargeableGainAnswers, BigDecimal(11100))
      else Future.successful(None)
    }

    def totalTaxableGain(chargeableGain: Option[ChargeableGainResultModel] = None,
                         yourAnswersSummaryModel: YourAnswersSummaryModel,
                         chargeableGainAnswers: ChargeableGainAnswers,
                         incomeAnswersModel: IncomeAnswersModel)(implicit hc: HeaderCarrier): Future[Option[TotalGainAndTaxOwedModel]] = {
      if (chargeableGain.isDefined && chargeableGain.get.chargeableGain > 0 &&
        incomeAnswersModel.personalAllowanceModel.isDefined && incomeAnswersModel.currentIncomeModel.isDefined) {
        calculatorConnector.calculateRttTotalGainAndTax(yourAnswersSummaryModel, chargeableGainAnswers, BigDecimal(11100), incomeAnswersModel)
      }
      else Future.successful(None)
    }

    def getTaxYear(disposalDate: Date): Future[Option[TaxYearModel]] = {
      val formats = new SimpleDateFormat("yyyy-MM-dd")
      calculatorConnector.getTaxYear(formats.format(disposalDate))
    }

    def routeRequest(totalGainAnswers: YourAnswersSummaryModel,
                     grossGain: BigDecimal,
                     chargeableGainAnswers: ChargeableGainAnswers,
                     chargeableGain: Option[ChargeableGainResultModel],
                     incomeAnswers: IncomeAnswersModel,
                     totalGainAndTax: Option[TotalGainAndTaxOwedModel],
                     backUrl: String,
                     taxYear: Option[TaxYearModel])(implicit hc: HeaderCarrier): Future[Result] = {
      if (chargeableGain.isDefined && chargeableGain.get.chargeableGain > 0 &&
        incomeAnswers.personalAllowanceModel.isDefined && incomeAnswers.currentIncomeModel.isDefined) Future.successful(
        Ok(views.html.calculation.resident.summary.finalSummary(totalGainAnswers, chargeableGainAnswers, incomeAnswers,
          totalGainAndTax.get, routes.IncomeController.personalAllowance().url, taxYear.get)))
      else if (grossGain > 0) Future.successful(Ok(views.html.calculation.resident.deductionsSummary(totalGainAnswers, chargeableGainAnswers, chargeableGain.get, backUrl)))
      else Future.successful(Ok(views.html.calculation.resident.gainSummary(totalGainAnswers, grossGain)))
    }

    for {
      answers <- calculatorConnector.getYourAnswers
      taxYear <- getTaxYear(answers.disposalDate)
      grossGain <- calculatorConnector.calculateRttGrossGain(answers)
      deductionAnswers <- calculatorConnector.getChargeableGainAnswers
      backLink <- buildPreviousTaxableGainsBackUrl(deductionAnswers)
      chargeableGain <- chargeableGain(grossGain, answers, deductionAnswers)
      incomeAnswers <- calculatorConnector.getIncomeAnswers
      totalGain <- totalTaxableGain(chargeableGain, answers, deductionAnswers, incomeAnswers)
      routeRequest <- routeRequest(answers, grossGain, deductionAnswers, chargeableGain, incomeAnswers, totalGain, backLink, taxYear)
    } yield routeRequest
  }
}



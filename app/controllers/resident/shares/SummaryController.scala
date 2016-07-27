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

import java.text.SimpleDateFormat
import java.util.Date
import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import models.resident._
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import play.api.mvc.Result
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation.resident.shares.{summary => views}
import scala.concurrent.Future

object SummaryController extends SummaryController {
  val calculatorConnector = CalculatorConnector
}

trait SummaryController extends FeatureLock {

  val calculatorConnector: CalculatorConnector

  private val homeLink = controllers.resident.shares.routes.GainController.disposalDate().url

  val summary = FeatureLockForRTTShares.async { implicit request =>

    def displayAnnualExemptAmountCheck(claimedOtherProperties: Boolean,
                                       claimedAllowableLosses: Boolean,
                                       allowableLossesValueModel: Option[AllowableLossesValueModel])(implicit hc: HeaderCarrier): Boolean = {
      allowableLossesValueModel match {
        case Some(result) if claimedAllowableLosses && claimedOtherProperties => result.amount == 0
        case _ if claimedOtherProperties && !claimedAllowableLosses => true
        case _ => false
      }
    }

    def getChargeableGain(grossGain: BigDecimal,
                       totalGainAnswers: GainAnswersModel,
                       deductionGainAnswers: DeductionGainAnswersModel)(implicit hc: HeaderCarrier): Future[Option[ChargeableGainResultModel]] = {
      if (grossGain > 0) calculatorConnector.calculateRttShareChargeableGain(totalGainAnswers, deductionGainAnswers, BigDecimal(11100))
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

    def getTaxYear(disposalDate: Date): Future[Option[TaxYearModel]] = {
      val formats = new SimpleDateFormat("yyyy-MM-dd")
      calculatorConnector.getTaxYear(formats.format(disposalDate))
    }

    def routeRequest(totalGainAnswers: GainAnswersModel,
                     grossGain: BigDecimal,
                     deductionGainAnswers: DeductionGainAnswersModel,
                     chargeableGain: Option[ChargeableGainResultModel],
                     backUrl: String,
                     taxYear: Option[TaxYearModel])(implicit hc: HeaderCarrier): Future[Result] = {
      if (grossGain > 0) Future.successful(Ok(views.deductionsSummary(totalGainAnswers, deductionGainAnswers, chargeableGain.get, backUrl, taxYear.get, homeLink)))
      else Future.successful(Ok(views.gainSummary(totalGainAnswers, grossGain, taxYear.get, homeLink)))
    }
    for {
      answers <- calculatorConnector.getShareGainAnswers
      taxYear <- getTaxYear(answers.disposalDate)
      grossGain <- calculatorConnector.calculateRttShareGrossGain(answers)
      chargeableGainAnswers <- calculatorConnector.getShareDeductionAnswers
      backLink <- buildDeductionsSummaryBackUrl(chargeableGainAnswers)
      chargeableGain <- getChargeableGain(grossGain, answers, chargeableGainAnswers)
      routeRequest <- routeRequest(answers, grossGain, chargeableGainAnswers, chargeableGain, backLink, taxYear)
    } yield routeRequest
  }
}
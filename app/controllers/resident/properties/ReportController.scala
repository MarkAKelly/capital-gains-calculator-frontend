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

import common.Dates._
import java.time.LocalDate

import common.Dates
import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import it.innove.play.pdf.PdfGenerator
import models.resident.TaxYearModel
import play.api.i18n.Messages
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation.resident.properties.{report => views}

import scala.concurrent.Future

object ReportController extends ReportController {
  val calcConnector = CalculatorConnector
}

trait ReportController extends FeatureLock {

  val calcConnector: CalculatorConnector

  def host(implicit request: RequestHeader): String = {
    s"http://${request.host}/"
  }

  def getTaxYear(disposalDate: LocalDate)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] =
    calcConnector.getTaxYear(disposalDate.format(requestFormatter))

  def getMaxAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    calcConnector.getFullAEA(taxYear)
  }

  def taxYearStringToInteger (taxYear: String): Future[Int] = {
    Future.successful((taxYear.take(2) + taxYear.takeRight(2)).toInt)
  }

  val gainSummaryReport = FeatureLockForRTT.async { implicit request =>
    for {
      answers <- calcConnector.getPropertyGainAnswers
      taxYear <- getTaxYear(answers.disposalDate)
      grossGain <- calcConnector.calculateRttPropertyGrossGain(answers)
    } yield {PdfGenerator.ok(views.gainSummaryReport(answers, grossGain, taxYear.get), host).toScala
      .withHeaders("Content-Disposition" -> s"""attachment; filename="${Messages("calc.resident.summary.title")}.pdf"""")}
  }


  //#####Deductions summary actions#####\\
  val deductionsReport = FeatureLockForRTT.async { implicit request =>
    for {
      answers <- calcConnector.getPropertyGainAnswers
      taxYear <- getTaxYear(answers.disposalDate)
      taxYearInt <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- getMaxAEA(taxYearInt)(hc)
      deductionAnswers <- calcConnector.getPropertyDeductionAnswers
      grossGain <- calcConnector.calculateRttPropertyGrossGain(answers)
      chargeableGain <- calcConnector.calculateRttPropertyChargeableGain(answers, deductionAnswers, maxAEA.get)
    } yield {PdfGenerator.ok(views.deductionsSummaryReport(answers, deductionAnswers, chargeableGain.get, taxYear.get), host).toScala
      .withHeaders("Content-Disposition" -> s"""attachment; filename="${Messages("calc.resident.summary.title")}.pdf"""")}
  }

  //#####Final summary actions#####\\

  val finalSummaryReport = FeatureLockForRTT.async { implicit request =>
    for {
      answers <- calcConnector.getPropertyGainAnswers
      taxYear <- getTaxYear(answers.disposalDate)
      taxYearInt <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- getMaxAEA(taxYearInt)(hc)
      grossGain <- calcConnector.calculateRttPropertyGrossGain(answers)
      deductionAnswers <- calcConnector.getPropertyDeductionAnswers
      chargeableGain <- calcConnector.calculateRttPropertyChargeableGain(answers, deductionAnswers, maxAEA.get)
      incomeAnswers <- calcConnector.getPropertyIncomeAnswers
      currentTaxYear <- Dates.getCurrentTaxYear
      totalGain <- calcConnector.calculateRttPropertyTotalGainAndTax(answers, deductionAnswers, maxAEA.get, incomeAnswers)
    } yield {
      PdfGenerator.ok(views.finalSummaryReport(answers,
        deductionAnswers,
        incomeAnswers,
        totalGain.get,
        taxYear.get,
        taxYear.get.taxYearSupplied == currentTaxYear),
        host).toScala.withHeaders("Content-Disposition" -> s"""attachment; filename="${Messages("calc.resident.summary.title")}.pdf"""")
    }
  }
}
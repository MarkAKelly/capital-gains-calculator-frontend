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
import it.innove.play.pdf.PdfGenerator
import models.resident.TaxYearModel
import play.api.i18n.Messages
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation.resident.shares.{report => views}
import scala.concurrent.Future

object ReportController extends ReportController {
  val calcConnector = CalculatorConnector
}

trait ReportController extends FeatureLock {

  val calcConnector: CalculatorConnector

  def host(implicit request: RequestHeader): String = {
    s"http://${request.host}/"
  }

  def getTaxYear(disposalDate: Date)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] = {
    val formats = new SimpleDateFormat("yyyy-MM-dd")
    calcConnector.getTaxYear(formats.format(disposalDate))
  }

  val gainSummaryReport = FeatureLockForRTTShares.async { implicit request =>
    for {
      answers <- calcConnector.getShareGainAnswers
      taxYear <- getTaxYear(answers.disposalDate)
      grossGain <- calcConnector.calculateRttShareGrossGain(answers)
    } yield {PdfGenerator.ok(views.gainSummaryReport(answers, grossGain, taxYear.get), host).toScala
      .withHeaders("Content-Disposition" -> s"""attachment; filename="${Messages("calc.resident.summary.title")}.pdf"""")}
  }


  //#####Deductions summary actions#####\\

  //#####Final summary actions#####\\

}
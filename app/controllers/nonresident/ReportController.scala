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

import java.time.LocalDate

import common.Dates._
import common.KeystoreKeys
import connectors.CalculatorConnector
import constructors.nonresident.SummaryConstructor
import controllers.predicates.ValidActiveSession
import it.innove.play.pdf.PdfGenerator
import models.nonresident.{CalculationResultModel, DisposalDateModel, SummaryModel}
import models.resident.TaxYearModel
import play.api.i18n.Messages
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc.{Action, RequestHeader}
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation
import views.html.calculation.nonresident.{summaryReport => summaryView}

import scala.concurrent.Future


object ReportController extends ReportController {
  val calcConnector = CalculatorConnector
}

trait ReportController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector

  def host(implicit request: RequestHeader): String ={
    s"http://${request.host}/"
  }

  def getTaxYear(disposalDate: DisposalDateModel)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] =
    calcConnector.getTaxYear(disposalDate.year.toString)

  def resultModel(summaryModel: SummaryModel)(implicit hc: HeaderCarrier): Future[Option[CalculationResultModel]] = {
    summaryModel.calculationElectionModel.calculationType match {
      case "flat" =>
        calcConnector.calculateFlat(summaryModel)
      case "time" =>
        calcConnector.calculateTA(summaryModel)
      case "rebased" =>
        calcConnector.calculateRebased(summaryModel)
    }
  }

  val summaryReport = ValidateSession.async { implicit request =>
    for {
      summary <- calcConnector.createSummary(hc)
      result <- resultModel(summary)(hc)
      taxYear <- getTaxYear(summary.disposalDateModel)

    } yield {
      PdfGenerator.ok(summaryView(summary, result.get, taxYear.get, SummaryConstructor.calcTypeMessage(summary.calculationElectionModel.calculationType)),
        host).toScala
        .withHeaders("Content-Disposition" ->s"""attachment; filename="${Messages("calc.summary.title")}.pdf"""")
    }
  }
}

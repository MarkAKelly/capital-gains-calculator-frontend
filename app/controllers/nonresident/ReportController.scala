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

import common.KeystoreKeys
import connectors.CalculatorConnector
import constructors.nonresident.{AnswersConstructor, SummaryConstructor}
import controllers.predicates.ValidActiveSession
import it.innove.play.pdf.PdfGenerator
import models.nonresident._
import models.resident.TaxYearModel
import play.api.i18n.Messages
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation.nonresident.{summaryReport => summaryView}

import scala.concurrent.Future


object ReportController extends ReportController {
  val calcConnector = CalculatorConnector
  val answersConstructor = AnswersConstructor
}

trait ReportController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  val answersConstructor: AnswersConstructor

  def host(implicit request: RequestHeader): String = {
    s"http://${request.host}/"
  }

  def getTaxYear(disposalDate: DisposalDateModel)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] =
    calcConnector.getTaxYear(disposalDate.year.toString)

  def resultModel(totalGainAnswersModel: TotalGainAnswersModel)(implicit hc: HeaderCarrier): Future[Option[TotalGainResultsModel]] = {
    calcConnector.calculateTotalGain(totalGainAnswersModel)
  }

  def noPRR(acquisitionDateModel: AcquisitionDateModel, rebasedValueModel: Option[RebasedValueModel]): Future[Boolean] =
    (acquisitionDateModel, rebasedValueModel) match {
      case (AcquisitionDateModel("No", _, _, _), Some(rebasedValue)) if rebasedValue.rebasedValueAmt.isEmpty => Future.successful(true)
      case (_, _) => Future.successful(false)
    }

  def getPRRModel(model: Option[TotalGainResultsModel], noPRR: Boolean)(implicit hc: HeaderCarrier): Future[Option[PrivateResidenceReliefModel]] = {

    val optionSeq = Seq(model.get.rebasedGain, model.get.timeApportionedGain).flatten
    val finalSeq = Seq(model.get.flatGain) ++ optionSeq

    (!finalSeq.forall(_ <= 0), noPRR) match {
      case (true, false) => calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief)
      case (_, _) => Future.successful(None)
    }
  }

  val summaryReport = ValidateSession.async { implicit request =>
    for {
      answers <- answersConstructor.getNRTotalGainAnswers
      calculationType <- calcConnector.fetchAndGetFormData[CalculationElectionModel](KeystoreKeys.calculationElection)
      totalGains <- resultModel(answers)(hc)
      taxYear <- getTaxYear(answers.disposalDateModel)
      noPRR <- noPRR(answers.acquisitionDateModel, answers.rebasedValueModel)
      prrModel <- getPRRModel(totalGains, noPRR)

    } yield {
      PdfGenerator.ok(summaryView(answers, totalGains.get, taxYear.get, calculationType.get.calculationType, prrModel),
        host).toScala
        .withHeaders("Content-Disposition" ->s"""attachment; filename="${Messages("calc.summary.title")}.pdf"""")
    }
  }
}

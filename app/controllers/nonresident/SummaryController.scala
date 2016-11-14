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
import common.{KeystoreKeys, TaxDates}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import models.nonresident._
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object SummaryController extends SummaryController {
  val calcConnector = CalculatorConnector
}

trait SummaryController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url
  val calcConnector: CalculatorConnector

  def summaryBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    Future.successful(routes.CheckYourAnswersController.checkYourAnswers().url)
  }

  def displayDateWarning(disposalDate: DisposalDateModel): Future[Boolean] = {
    Future.successful(!TaxDates.dateInsideTaxYear(disposalDate.day, disposalDate.month, disposalDate.year))
  }

  def reliefApplied(summaryModel: SummaryModel): Future[String] = {
    Future.successful(summaryModel.reliefApplied())
  }

  def calculateDetails(summaryData: SummaryModel)(implicit headerCarrier: HeaderCarrier): Future[Option[CalculationResultModel]] = {
    summaryData.calculationElectionModel.calculationType match {
      case "flat" =>
        calcConnector.calculateFlat(summaryData)
      case "time" =>
        calcConnector.calculateTA(summaryData)
      case "rebased" =>
        calcConnector.calculateRebased(summaryData)
    }
  }

  val summary = ValidateSession.async { implicit request =>

    def routeRequest(calculationResult: Option[CalculationResultModel],
                     backUrl: String, displayDateWarning: Boolean,
                     calculationType: String, reliefApplied: String,
                     customerType: String): Future[Result] = {
      Future.successful(Ok(calculation.nonresident.summary(calculationResult.get, backUrl, displayDateWarning,
        calculationType, reliefApplied, customerType)))
    }

    for {
      backUrl <- summaryBackUrl
      answers <- calcConnector.createSummary(hc)
      displayWarning <- displayDateWarning(answers.disposalDateModel)
      calculationDetails <- calculateDetails(answers)(hc)
      reliefApplied <- reliefApplied(answers)
      route <- routeRequest(calculationDetails, backUrl, displayWarning,
        answers.calculationElectionModel.calculationType,
        reliefApplied, answers.customerTypeModel.customerType)
    } yield route
  }

  def restart(): Action[AnyContent] = Action.async { implicit request =>
    calcConnector.clearKeystore(hc)
    Future.successful(Redirect(routes.DisposalDateController.disposalDate()))
  }
}

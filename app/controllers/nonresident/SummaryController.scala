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

import common.{KeystoreKeys, TaxDates}
import connectors.CalculatorConnector
import constructors.nonresident.{AnswersConstructor, FinalTaxAnswersRequestConstructor}
import controllers.predicates.ValidActiveSession
import models.nonresident.{CalculationResultsWithPRRModel, _}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object SummaryController extends SummaryController {
  val calcConnector = CalculatorConnector
  val answersConstructor = AnswersConstructor
}

trait SummaryController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url
  val calcConnector: CalculatorConnector
  val answersConstructor: AnswersConstructor

  val summary = ValidateSession.async { implicit request =>

    def getPRRModel(implicit hc: HeaderCarrier, totalGainResultsModel: TotalGainResultsModel): Future[Option[PrivateResidenceReliefModel]] = {
      val optionSeq = Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten
      val finalSeq = Seq(totalGainResultsModel.flatGain) ++ optionSeq

      if (!finalSeq.forall(_ <= 0)) {
        val prrModel = calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief)

        for {
          prrModel <- prrModel
        } yield prrModel
      } else Future(None)
    }

    def getFinalTaxAnswers(totalGainResultsModel: TotalGainResultsModel,
                           calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel])
                          (implicit hc: HeaderCarrier): Future[Option[TotalPersonalDetailsCalculationModel]] = {
      val optionSeq = Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten
      val finalSeq = Seq(totalGainResultsModel.flatGain) ++ optionSeq
      lazy val finalAnswers = answersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers
      if (!finalSeq.forall(_ <= 0)) {
        calculationResultsWithPRRModel match {
          case Some(model)
            if (Seq(model.flatResult) ++ Seq(model.rebasedResult, model.timeApportionedResult).flatten).forall(_.taxableGain <= 0) =>
            Future.successful(None)
          case _ => for {
            answers <- finalAnswers
          } yield Some(answers)
        }
      } else Future.successful(None)
    }

    def getSection(calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel],
                   privateResidenceReliefModel: Option[PrivateResidenceReliefModel],
                   totalGainResultsModel: TotalGainResultsModel,
                   calculationType: String): Future[Seq[QuestionAnswerModel[Any]]] = {
      privateResidenceReliefModel match {
        case Some(model) if model.isClaimingPRR == "Yes" => Future.successful(calculationResultsWithPRRModel.get.calculationDetailsRows(calculationType))
        case _ => Future.successful(totalGainResultsModel.calculationDetailsRows(calculationType))
      }
    }

    def summaryBackUrl(model: Option[TotalGainResultsModel])(implicit hc: HeaderCarrier): Future[String] = model match {
      case (Some(data)) if data.rebasedGain.isDefined || data.timeApportionedGain.isDefined =>
        Future.successful(routes.CalculationElectionController.calculationElection().url)
      case (Some(data)) =>
        Future.successful(routes.CheckYourAnswersController.checkYourAnswers().url)
      case (None) => Future.successful(common.DefaultRoutes.missingDataRoute)
    }

    def displayDateWarning(disposalDate: DisposalDateModel): Future[Boolean] = {
      Future.successful(!TaxDates.dateInsideTaxYear(disposalDate.day, disposalDate.month, disposalDate.year))
    }

    def calculateDetails(summaryData: TotalGainAnswersModel): Future[Option[TotalGainResultsModel]] = {
      calcConnector.calculateTotalGain(summaryData)
    }

    def calculatePRR(answers: TotalGainAnswersModel, privateResidenceReliefModel: Option[PrivateResidenceReliefModel])
      : Future[Option[CalculationResultsWithPRRModel]] = {
        privateResidenceReliefModel match {
          case Some(model) => calcConnector.calculateTaxableGainAfterPRR(answers, model)
          case None => Future.successful(None)
        }
    }

    def routeRequest(result: Seq[QuestionAnswerModel[Any]],
                     backUrl: String, displayDateWarning: Boolean,
                     calculationType: String): Future[Result] = {
      Future.successful(Ok(calculation.nonresident.summary(result,
        backUrl, displayDateWarning, calculationType)))
    }

    for {
      answers <- answersConstructor.getNRTotalGainAnswers(hc)
      displayWarning <- displayDateWarning(answers.disposalDateModel)
      totalGainResultsModel <- calculateDetails(answers)
      privateResidentReliefModel <- getPRRModel(hc, totalGainResultsModel.get)
      calculationResultsWithPRR <- calculatePRR(answers, privateResidentReliefModel)
      backUrl <- summaryBackUrl(totalGainResultsModel)
      calculationType <- calcConnector.fetchAndGetFormData[CalculationElectionModel](KeystoreKeys.calculationElection)
      results <- getSection(calculationResultsWithPRR, privateResidentReliefModel,
        totalGainResultsModel.get, calculationType.get.calculationType)
      route <- routeRequest(results, backUrl, displayWarning,
        calculationType.get.calculationType)
    } yield route
  }

  def restart(): Action[AnyContent] = Action.async { implicit request =>
    calcConnector.clearKeystore(hc)
    Future.successful(Redirect(routes.DisposalDateController.disposalDate()))
  }
}

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
import constructors.nonresident.{AnswersConstructor, CalculationElectionConstructor}
import controllers.predicates.ValidActiveSession
import forms.nonresident.CalculationElectionForm._
import models.nonresident._
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object CalculationElectionController extends CalculationElectionController {
  val calcConnector = CalculatorConnector
  val calcAnswersConstructor = AnswersConstructor
  val calcElectionConstructor = CalculationElectionConstructor
}

trait CalculationElectionController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl: String = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink: String = controllers.nonresident.routes.DisposalDateController.disposalDate().url
  val calcConnector: CalculatorConnector
  val calcElectionConstructor: CalculationElectionConstructor
  val calcAnswersConstructor: AnswersConstructor

  val calculationElection = ValidateSession.async { implicit request =>

    def getPRRModel(implicit hc: HeaderCarrier, totalGainResultsModel: TotalGainResultsModel): Future[Option[PrivateResidenceReliefModel]] = {
      val optionSeq = Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten
      val finalSeq = Seq(totalGainResultsModel.flatGain) ++ optionSeq

      if (finalSeq.exists(_ > 0)) {
        calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief)
      } else Future(None)
    }

    def action(content: Seq[(String, String, String, Option[String])]) =
      calcConnector.fetchAndGetFormData[CalculationElectionModel](KeystoreKeys.calculationElection).map {
        case Some(data) =>
          Ok(calculation.nonresident.calculationElection(
            calculationElectionForm.fill(data),
            content)
          )
        case None =>
          Ok(calculation.nonresident.calculationElection(
            calculationElectionForm,
            content)
          )
      }

    for {
      totalGainAnswers <- calcAnswersConstructor.getNRTotalGainAnswers(hc)
      totalGain <- calcConnector.calculateTotalGain(totalGainAnswers)(hc)
      content <- calcElectionConstructor.generateElection(totalGain.get, None, None)
      finalResult <- action(content)
    } yield finalResult
  }

  val submitCalculationElection = ValidateSession.async { implicit request =>

    def successAction(model: CalculationElectionModel) = {
      calcConnector.saveFormData(KeystoreKeys.calculationElection, model)
      Future.successful(Redirect(routes.SummaryController.summary()))
    }

    def errorAction(form: Form[CalculationElectionModel]) = {
      for {
        answers <- calcAnswersConstructor.getNRTotalGainAnswers(hc)
        calculationResults <- calcConnector.calculateTotalGain(answers)(hc)
        content <- calcElectionConstructor.generateElection(calculationResults.get, None, None)
      } yield {
        BadRequest(calculation.nonresident.calculationElection(
          form,
          content
        ))
      }
    }

    calculationElectionForm.bindFromRequest.fold(errorAction, successAction)
  }

}

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
import common.nonresident.CalculationType
import connectors.CalculatorConnector
import constructors.nonresident.{AnswersConstructor, CalculationElectionConstructor, YourAnswersConstructor}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import controllers.predicates.ValidActiveSession
import models.nonresident.{CalculationElectionModel, PrivateResidenceReliefModel, TotalGainResultsModel}
import views.html.calculation
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object CheckYourAnswersController extends CheckYourAnswersController {
  val calcElectionConstructor = CalculationElectionConstructor
  val answersConstructor = AnswersConstructor
  val calculatorConnector = CalculatorConnector
}

trait CheckYourAnswersController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url

  val answersConstructor: AnswersConstructor
  val calculatorConnector: CalculatorConnector
  val backLink = controllers.nonresident.routes.ImprovementsController.improvements().url

  def getPRRModel(implicit hc: HeaderCarrier, totalGainResultsModel: TotalGainResultsModel): Future[Option[PrivateResidenceReliefModel]] = {
    val optionSeq = Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten
    val finalSeq = Seq(totalGainResultsModel.flatGain) ++ optionSeq

    if (!finalSeq.forall(_ <= 0)) {
      val prrModel = calculatorConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief)

      for {
        prrModel <- prrModel
      } yield prrModel
    } else Future(None)
  }

  val checkYourAnswers = ValidateSession.async { implicit request =>

    for {
      model <- answersConstructor.getNRTotalGainAnswers
      totalGainResult <- calculatorConnector.calculateTotalGain(model)
      prrModel <- getPRRModel(hc, totalGainResult.get)
      answers <- Future.successful(YourAnswersConstructor.fetchYourAnswers(model, prrModel))
    } yield {
      Ok(calculation.nonresident.checkYourAnswers(answers, backLink))
    }
  }

  val submitCheckYourAnswers = ValidateSession.async { implicit request =>
    calculatorConnector.saveFormData[CalculationElectionModel](KeystoreKeys.calculationElection, CalculationElectionModel(CalculationType.flat))
    Future.successful(Redirect(routes.SummaryController.summary()))
  }
}

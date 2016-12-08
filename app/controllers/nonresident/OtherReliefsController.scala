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
import common.nonresident.CustomerTypeKeys
import connectors.CalculatorConnector
import constructors.nonresident.AnswersConstructor
import controllers.predicates.ValidActiveSession
import forms.nonresident.OtherReliefsForm._
import models.nonresident._
import models.resident.TaxYearModel
import play.api.data.Form
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation

import scala.concurrent.Future

object OtherReliefsController extends OtherReliefsController {
  val calcConnector = CalculatorConnector
  val answersConstructor = AnswersConstructor
}

trait OtherReliefsController extends FrontendController with ValidActiveSession {

  val calcConnector: CalculatorConnector
  val answersConstructor: AnswersConstructor

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  override val homeLink = controllers.nonresident.routes.DisposalDateController.disposalDate().url

  private def getPRRResponse(totalGainResultsModel: TotalGainResultsModel)(implicit hc: HeaderCarrier): Future[Option[PrivateResidenceReliefModel]] = {
    val optionSeq = Seq(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten
    val finalSeq = Seq(totalGainResultsModel.flatGain) ++ optionSeq

    if (finalSeq.exists(_ > 0)) {
      calcConnector.fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief)
    } else Future(None)
  }

  private def getPRRIfApplicable(totalGainAnswersModel: TotalGainAnswersModel,
                         privateResidenceReliefModel: Option[PrivateResidenceReliefModel])(implicit hc: HeaderCarrier):
    Future[Option[CalculationResultsWithPRRModel]] = {

    privateResidenceReliefModel match {
      case Some(data) => calcConnector.calculateTaxableGainAfterPRR(totalGainAnswersModel, data)
      case None => Future.successful(None)
    }
  }

  private def getFinalSectionsAnswers(totalGainResultsModel: TotalGainResultsModel,
                              calculationResultsWithPRRModel: Option[CalculationResultsWithPRRModel])(implicit hc: HeaderCarrier):
    Future[Option[TotalPersonalDetailsCalculationModel]] = {

    calculationResultsWithPRRModel match {

      case Some(data) =>
        val results = data.flatResult :: List(data.rebasedResult, data.timeApportionedResult).flatten

        if (results.exists(_.taxableGain > 0)) {
          answersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(hc)
        } else Future(None)

      case None =>
        val gains = totalGainResultsModel.flatGain :: List(totalGainResultsModel.rebasedGain, totalGainResultsModel.timeApportionedGain).flatten

        if (gains.exists(_ > 0)) {
          answersConstructor.getPersonalDetailsAndPreviousCapitalGainsAnswers(hc)
        } else Future(None)
    }
  }

  private def getMaxAEA(totalPersonalDetailsCalculationModel: Option[TotalPersonalDetailsCalculationModel],
                taxYear: Option[TaxYearModel])(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    totalPersonalDetailsCalculationModel match {
      case Some(data) if data.customerTypeModel.customerType.equals(CustomerTypeKeys.trustee) && data.trusteeModel.get.isVulnerable.equals("No") =>
        calcConnector.getPartialAEA(TaxDates.taxYearStringToInteger(taxYear.get.calculationTaxYear))
      case _ => calcConnector.getFullAEA(TaxDates.taxYearStringToInteger(taxYear.get.calculationTaxYear))
    }
  }

  private def getTaxYear(totalGainAnswersModel: TotalGainAnswersModel)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] = {
    val date = totalGainAnswersModel.disposalDateModel
    calcConnector.getTaxYear(s"${date.year}-${date.month}-${date.day}")
  }

  private def getFlatChargeableGain(totalGainAnswersModel: TotalGainAnswersModel,
                                   prrModel: Option[PrivateResidenceReliefModel],
                                   totalTaxOwedModel: Option[TotalPersonalDetailsCalculationModel],
                                   maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[CalculationResultsWithTaxOwedModel]] = {

    totalTaxOwedModel match {
      case Some(data) => calcConnector.calculateNRCGTTotalTax(totalGainAnswersModel, prrModel, data, maxAEA)
      case None => Future(None)
    }
  }

  val otherReliefs = ValidateSession.async { implicit request =>

    def routeRequest(model: Option[OtherReliefsModel],
                     totalGain: Option[TotalGainResultsModel],
                     chargeableGainResult: Option[CalculationResultsWithTaxOwedModel]) = {
      val gain = totalGain.fold(BigDecimal(0))(_.flatGain)
      val chargeableGain = chargeableGainResult.fold(BigDecimal(0))(_.flatResult.taxableGain)

      val result = model.fold(calculation.nonresident.otherReliefs(otherReliefsForm, chargeableGain, gain)) { data =>
        calculation.nonresident.otherReliefs(otherReliefsForm.fill(data), chargeableGain, gain)
      }

      Ok(result)
    }

    for {
      answers <- answersConstructor.getNRTotalGainAnswers(hc)
      gain <- calcConnector.calculateTotalGain(answers)(hc)
      prrAnswers <- getPRRResponse(gain.get)
      totalGainWithPRR <- getPRRIfApplicable(answers, prrAnswers)(hc)
      allAnswers <- getFinalSectionsAnswers(gain.get, totalGainWithPRR)(hc)
      taxYear <- getTaxYear(answers)(hc)
      maxAEA <- getMaxAEA(allAnswers, taxYear)(hc)
      chargeableGainResult <- getFlatChargeableGain(answers, prrAnswers, allAnswers, maxAEA.get)(hc)
      reliefs <- calcConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat)
    } yield routeRequest(reliefs, gain, chargeableGainResult)

  }

  val submitOtherReliefs = ValidateSession.async { implicit request =>

    def errorAction(form: Form[OtherReliefsModel]) = {

      def routeRequest(totalGain: Option[TotalGainResultsModel], chargeableGainResult: Option[CalculationResultsWithTaxOwedModel]) = {
        val gain = totalGain.fold(BigDecimal(0))(_.flatGain)
        val chargeableGain = chargeableGainResult.fold(BigDecimal(0))(_.flatResult.taxableGain)

        BadRequest(calculation.nonresident.otherReliefs(form, chargeableGain, gain))
      }

      for {
        answers <- answersConstructor.getNRTotalGainAnswers(hc)
        gain <- calcConnector.calculateTotalGain(answers)(hc)
        prrAnswers <- getPRRResponse(gain.get)(hc)
        totalGainWithPRR <- getPRRIfApplicable(answers, prrAnswers)(hc)
        allAnswers <- getFinalSectionsAnswers(gain.get, totalGainWithPRR)(hc)
        taxYear <- getTaxYear(answers)(hc)
        maxAEA <- getMaxAEA(allAnswers, taxYear)(hc)
        chargeableGainResult <- getFlatChargeableGain(answers, prrAnswers, allAnswers, maxAEA.get)(hc)
      } yield routeRequest(gain, chargeableGainResult)
    }

    def successAction(model: OtherReliefsModel) = {
      calcConnector.saveFormData(KeystoreKeys.otherReliefsFlat, model)
      Future.successful(Redirect(routes.SummaryController.summary()))
    }

    otherReliefsForm.bindFromRequest.fold(
      errors => errorAction(errors),
      success => successAction(success))
  }
}

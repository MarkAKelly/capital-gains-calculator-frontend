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

package constructors.nonresident

import models.nonresident._
import common.{Dates, KeystoreKeys => keys}
import play.api.i18n.Messages

object DeductionDetailsConstructor {

  def datesNotValidCheck(acquisitionDateModel: AcquisitionDateModel, disposalDateModel: DisposalDateModel): Boolean = {
    acquisitionDateModel match {
      case AcquisitionDateModel("Yes",_,_,_) if acquisitionDateModel.get.plusMonths(18).isBefore(disposalDateModel.get)=> true
      case _ => false
    }
  }

  def deductionDetailsRows(answers: TotalGainAnswersModel,
                           privateResidenceReliefModel: Option[PrivateResidenceReliefModel] = None): Seq[QuestionAnswerModel[Any]] = {

    val otherReliefsFlatValue = otherReliefsFlatValueRow(answers)
    val privateResidenceReliefQuestion = privateResidenceReliefQuestionRow(privateResidenceReliefModel)

    val sequence = Seq(otherReliefsFlatValue,
      privateResidenceReliefQuestion)

    sequence.flatten
  }

  def otherReliefsFlatValueRow(answers: TotalGainAnswersModel): Option[QuestionAnswerModel[BigDecimal]] = {
    answers.otherReliefsFlat match {
      case Some(OtherReliefsModel(value)) => Some(QuestionAnswerModel(
        keys.otherReliefsFlat,
        value,
        Messages("calc.otherReliefs.question"),
        Some(controllers.nonresident.routes.OtherReliefsController.otherReliefs().url)
      ))
      case _ => None
    }
  }

  def privateResidenceReliefQuestionRow(prr: Option[PrivateResidenceReliefModel]): Option[QuestionAnswerModel[String]] = {
    prr match {
      case Some(PrivateResidenceReliefModel(answer, _, _)) => Some(QuestionAnswerModel(
        keys.privateResidenceRelief,
        answer,
        Messages("calc.privateResidenceRelief.question"),
        Some(controllers.nonresident.routes.PrivateResidenceReliefController.privateResidenceRelief().url)
      ))
      case _ => None
    }
  }

  def privateResidenceReliefDaysClaimedRow(prr: Option[PrivateResidenceReliefModel],
                                           answers: TotalGainAnswersModel): Option[QuestionAnswerModel[BigDecimal]] = {
    prr match {
      case Some(PrivateResidenceReliefModel("Yes", Some(value), _)) if datesNotValidCheck(answers.acquisitionDateModel, answers.disposalDateModel) =>
        Some(QuestionAnswerModel(
          s"${keys.privateResidenceRelief}-daysClaimed",
          value,
          s"${Messages("calc.privateResidenceRelief.questionBefore.partOne")} ${Dates.dateMinusMonths(answers.disposalDateModel, 18)}" +
            s" ${Messages("calc.privateResidenceRelief.questionBefore.partTwo")}",
          Some(controllers.nonresident.routes.PrivateResidenceReliefController.privateResidenceRelief().url)
        ))
      case _ => None
    }
  }
}

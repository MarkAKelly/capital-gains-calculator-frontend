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

import models.nonresident.{CalculationElectionModel, OtherReliefsModel, QuestionAnswerModel}
import common.nonresident.CalculationType
import common.KeystoreKeys
import play.api.i18n.Messages

object OtherReliefsRequestConstructor {

  def getOtherReliefsSection(flatReliefs: Option[OtherReliefsModel],
                             rebasedReliefs: Option[OtherReliefsModel],
                             timeApportionedReliefs: Option[OtherReliefsModel],
                             calculationElectionModel: CalculationElectionModel): Seq[QuestionAnswerModel[Any]] = {

    ???
  }

  def getOtherReliefsRebasedRow(rebasedReliefs: Option[OtherReliefsModel],
                                calculationElectionModel: CalculationElectionModel): Option[QuestionAnswerModel[BigDecimal]] = {
    (calculationElectionModel, rebasedReliefs) match {
      case (CalculationElectionModel(CalculationType.rebased), Some(OtherReliefsModel(value))) if value > 0 =>
        Some(QuestionAnswerModel(
          s"${KeystoreKeys.otherReliefsRebased}",
          value,
          Messages("calc.otherReliefs.question"),
          None
        ))
      case _ => None
    }
  }

  def getOtherReliefsTimeApportionedRow(timeApportionedReliefs: Option[OtherReliefsModel],
                                calculationElectionModel: CalculationElectionModel): Option[QuestionAnswerModel[BigDecimal]] = {
    (calculationElectionModel, timeApportionedReliefs) match {
      case (CalculationElectionModel(CalculationType.timeApportioned), Some(OtherReliefsModel(value))) if value > 0 =>
        Some(QuestionAnswerModel(
          s"${KeystoreKeys.otherReliefsTA}",
          value,
          Messages("calc.otherReliefs.question"),
          None
        ))
      case _ => None
    }
  }

  def getOtherReliefsFlatRow(flatReliefs: Option[OtherReliefsModel],
                                calculationElectionModel: CalculationElectionModel): Option[QuestionAnswerModel[BigDecimal]] = {
    (calculationElectionModel, flatReliefs) match {
      case (CalculationElectionModel(CalculationType.flat), Some(OtherReliefsModel(value))) if value > 0 =>
        Some(QuestionAnswerModel(
          s"${KeystoreKeys.otherReliefsFlat}",
          value,
          Messages("calc.otherReliefs.question"),
          None
        ))
      case _ => None
    }
  }
}

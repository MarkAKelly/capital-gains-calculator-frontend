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

package constructors.resident

import models.resident.{ChargeableGainAnswers, IncomeAnswersModel, YourAnswersSummaryModel}

object CalculateRequestConstructor {

  def totalGainRequestString (answers: YourAnswersSummaryModel): String = {
      s"?disposalValue=${answers.disposalValue}" +
      s"&disposalCosts=${answers.disposalCosts}" +
      s"&acquisitionValue=${answers.acquisitionValue}" +
      s"&acquisitionCosts=${answers.acquisitionCosts}" +
      s"&improvements=${answers.improvements}"
  }

  def chargeableGainRequestString (answers: ChargeableGainAnswers, maxAEA: BigDecimal): String = {
    s"${if (answers.reliefsModel.get.isClaiming)
      s"&reliefs=${answers.reliefsValueModel.get.amount}"
    else ""}" +
    s"${if (answers.otherPropertiesModel.get.hasOtherProperties && answers.allowableLossesModel.get.isClaiming)
      s"&allowableLosses=${answers.allowableLossesValueModel.get.amount}"
    else ""}" +
    s"${if (answers.broughtForwardModel.get.option)
      s"&broughtForwardLosses=${answers.broughtForwardValueModel.get.amount}"
    else ""}" +
    s"&annualExemptAmount=${if (answers.otherPropertiesModel.get.hasOtherProperties)
      answers.annualExemptAmountModel.get.amount
    else maxAEA}"
  }

  def incomeAnswersRequestString (deductionsAnswers: ChargeableGainAnswers, answers: IncomeAnswersModel): String ={
    s"${if (deductionsAnswers.otherPropertiesModel.get.hasOtherProperties && deductionsAnswers.annualExemptAmountModel.isDefined &&
            deductionsAnswers.annualExemptAmountModel.get.amount == 0)
      s"&previousTaxableGain=${answers.previousTaxableGainsModel.get.amount}"
      else ""}" +
    s"&previousIncome=${answers.currentIncomeModel.get.amount}" +
    s"&personalAllowance=${answers.personalAllowanceModel.get.amount}"
  }
}
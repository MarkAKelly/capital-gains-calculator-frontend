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

import common.TaxDates
import models.nonresident._

object TotalGainRequestConstructor {

  def totalGainQuery(totalGainAnswersModel: TotalGainAnswersModel): String = {
    disposalValue(totalGainAnswersModel) +
    disposalCosts(totalGainAnswersModel) +
    acquisitionValue(totalGainAnswersModel) +
    acquisitionCosts(totalGainAnswersModel) +
    rebasedValues(totalGainAnswersModel) +
    timeApportionedValues(totalGainAnswersModel)
  }

  def disposalValue(totalGainAnswersModel: TotalGainAnswersModel): String = {
    s"disposalValue=${totalGainAnswersModel.disposalValueModel.disposalValue}"
  }

  def disposalCosts(totalGainAnswersModel: TotalGainAnswersModel): String = {
    s"&disposalCosts=${totalGainAnswersModel.disposalCostsModel.disposalCosts}"
  }

  def acquisitionValue(totalGainAnswersModel: TotalGainAnswersModel): String = {
    s"&acquisitionValue=${totalGainAnswersModel.acquisitionValueModel.acquisitionValueAmt}"
  }

  def acquisitionCosts(totalGainAnswersModel: TotalGainAnswersModel): String = {
    s"&acquisitionCosts=${totalGainAnswersModel.acquisitionCostsModel.acquisitionCostsAmt}"
  }

  def improvements(totalGainAnswersModel: TotalGainAnswersModel): String = {
    totalGainAnswersModel.improvementsModel match {
      case ImprovementsModel("Yes", Some(value), _) =>
        s"&improvements=$value"
      case _ => "&improvements=0"
    }
  }

  def rebasedValues(totalGainAnswersModel: TotalGainAnswersModel): String = {
    (totalGainAnswersModel.rebasedValueModel, totalGainAnswersModel.acquisitionDateModel) match {
      case (Some(RebasedValueModel("Yes", Some(value))), AcquisitionDateModel("Yes",_,_,_))
        if TaxDates.dateAfterStart(totalGainAnswersModel.acquisitionDateModel.get) =>
        s"&rebasedValue=$value${rebasedCosts(totalGainAnswersModel)}${improvementsAfterTaxStarted(totalGainAnswersModel)}"
      case (Some(RebasedValueModel("Yes", Some(value))), AcquisitionDateModel("No",_,_,_)) =>
        s"&rebasedValue=$value${rebasedCosts(totalGainAnswersModel)}${improvementsAfterTaxStarted(totalGainAnswersModel)}"
      case _ => ""
    }
  }

  def rebasedCosts(totalGainAnswersModel: TotalGainAnswersModel): String = {
    totalGainAnswersModel.rebasedCostsModel match {
      case Some(RebasedCostsModel("Yes", Some(value))) => s"&rebasedCosts=$value"
      case _ => "&rebasedCosts=0"
    }
  }

  def improvementsAfterTaxStarted(totalGainAnswersModel: TotalGainAnswersModel): String = {
    totalGainAnswersModel.improvementsModel match {
      case ImprovementsModel("Yes", _, Some(value)) => s"&improvementsAfterTaxStarted=$value"
      case _ => "improvementsAfterTaxStarted=0"
    }
  }

  def timeApportionedValues(totalGainAnswersModel: TotalGainAnswersModel): String = {
    (totalGainAnswersModel.disposalDateModel, totalGainAnswersModel.acquisitionDateModel) match {
      case (DisposalDateModel(dDay, dMonth, dYear), AcquisitionDateModel("Yes", Some(aDay), Some(aMonth), Some(aYear)))
        if TaxDates.dateAfterStart(totalGainAnswersModel.acquisitionDateModel.get) =>
        s"&disposalDate=$dYear-$dMonth-$dDay&acquisitionDate=$aYear-$aMonth-$aDay"
      case _ => ""
    }
  }
}

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

import models.nonresident.{CalculationResultModel, QuestionAnswerModel, SummaryModel}

object YourAnswersConstructor {

  //TODO update to use the new answers models
  def fetchYourAnswers(summaryModel: SummaryModel, resultModel: CalculationResultModel): Seq[QuestionAnswerModel[Any]] = {
    val salesDetailsRows = SalesDetailsConstructor.salesDetailsRows(summaryModel)
    val purchaseDetailsRows = PurchaseDetailsConstructor.getPurchaseDetailsSection(summaryModel)
    val propertyDetailsRows = PropertyDetailsConstructor.propertyDetailsRows(summaryModel)
    val deductionDetailsRows = DeductionDetailsConstructor.deductionDetailsRows(summaryModel, resultModel)

    for {
      salesDetails <- salesDetailsRows
      purchaseDetails <- purchaseDetailsRows
      propertyDetails <- propertyDetailsRows
      deductionDetails <- deductionDetailsRows
      answerRows <- salesDetailsRows ++ purchaseDetailsRows ++ propertyDetailsRows ++ deductionDetailsRows
    } yield answerRows
  }
}

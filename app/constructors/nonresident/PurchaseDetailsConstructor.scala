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

import common.KeystoreKeys
import models.nonresident.{QuestionAnswerModel, TotalGainAnswersModel}
import play.api.i18n.Messages


object PurchaseDetailsConstructor {

  def getPurchaseDetailsSection(totalGainAnswersModel: TotalGainAnswersModel): Seq[QuestionAnswerModel[Any]] = {

    val acquisitionDateData = getAcquisitionDateAnswer(totalGainAnswersModel)
    val acquisitionValueData = getAcquisitionValueAnswer(totalGainAnswersModel)
    val acquisitionCostsData = getAcquisitionCostsAnswer(totalGainAnswersModel)

    val items = Seq(
      acquisitionDateData,
      acquisitionValueData,
      acquisitionCostsData
    )
    items.flatten
  }

  def getAcquisitionDateAnswer(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[Any]] = {
    Some(QuestionAnswerModel(
      s"${KeystoreKeys.acquisitionDate}-question",
      totalGainAnswersModel.acquisitionDateModel.hasAcquisitionDate,
      Messages("calc.acquisitionDate.question"),
      Some(controllers.nonresident.routes.AcquisitionDateController.acquisitionDate().url)
    ))
  }

  def howBecameOwner(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[String]] = {
    ???
  }

  def getAcquisitionValueAnswer(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[BigDecimal]] = {
    Some(QuestionAnswerModel(
      KeystoreKeys.acquisitionValue,
      totalGainAnswersModel.acquisitionValueModel.acquisitionValueAmt,
      Messages("calc.acquisitionValue.question"),
      Some(controllers.nonresident.routes.AcquisitionValueController.acquisitionValue().url)
    ))
  }

  def getAcquisitionCostsAnswer(totalGainAnswersModel: TotalGainAnswersModel): Option[QuestionAnswerModel[BigDecimal]] = {
    Some(QuestionAnswerModel(
      KeystoreKeys.acquisitionCosts,
      totalGainAnswersModel.acquisitionCostsModel.acquisitionCostsAmt,
      Messages("calc.acquisitionCosts.question"),
      Some(controllers.nonresident.routes.AcquisitionCostsController.acquisitionCosts().url)
    ))
  }
}

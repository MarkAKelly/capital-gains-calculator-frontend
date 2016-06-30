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

package connectors.resident

import common.Dates._
import common.KeystoreKeys.ResidentKeys
import models.resident.{DisposalCostsModel, _}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.HeaderCarrier
import connectors.CalculatorConnector._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object SummaryConnector extends SummaryConnector with ServicesConfig {
}

trait SummaryConnector {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  def getYourAnswers(implicit hc: HeaderCarrier): Future[YourAnswersModel] = {
    val acquisitionValue = fetchAndGetFormData[AcquisitionValueModel](ResidentKeys.acquisitionValue).map(formData => formData.get.amount)
    val disposalDate = fetchAndGetFormData[DisposalDateModel](ResidentKeys.disposalDate).map(formData => constructDate(formData.get.day, formData.get.month, formData.get.year))
    val disposalValue = fetchAndGetFormData[DisposalValueModel](ResidentKeys.disposalValue).map(formData => formData.get.amount)
    val acquisitionCosts = fetchAndGetFormData[AcquisitionCostsModel](ResidentKeys.acquisitionCosts).map(formData => formData.get.amount)
    val disposalCosts = fetchAndGetFormData[DisposalCostsModel](ResidentKeys.disposalCosts).map(formData => formData.get.amount)

    for {
      acquisitionValueModel <- acquisitionValue
      disposalDateModel <- disposalDate
      disposalValueModel <- disposalValue
      acquisitionCostsModel <- acquisitionCosts
      disposalCostsModel <- disposalCosts
    } yield YourAnswersModel(
      disposalDateModel,
      disposalValueModel,
      disposalCostsModel,
      acquisitionValueModel,
      acquisitionCostsModel)
  }

}
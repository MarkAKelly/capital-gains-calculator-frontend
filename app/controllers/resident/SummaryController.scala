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

package controllers.resident

import common.Dates._
import common.KeystoreKeys.ResidentKeys
import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import models.resident._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

trait SummaryController extends FeatureLock {

  val calculatorConnector: CalculatorConnector

  def getYourAnswers(implicit hc: HeaderCarrier): Future[YourAnswersModel] = {
    val acquisitionValue = calculatorConnector.fetchAndGetFormData[AcquisitionValueModel](ResidentKeys.acquisitionValue).map(formData => formData.get.amount)
    val disposalDate = calculatorConnector.fetchAndGetFormData[DisposalDateModel](ResidentKeys.disposalDate).map(formData => constructDate(formData.get.day, formData.get.month, formData.get.year))
    val disposalValue = calculatorConnector.fetchAndGetFormData[DisposalValueModel](ResidentKeys.disposalValue).map(formData => formData.get.amount)
    val acquisitionCosts = calculatorConnector.fetchAndGetFormData[AcquisitionCostsModel](ResidentKeys.acquisitionCosts).map(formData => formData.get.amount)
    val disposalCosts = calculatorConnector.fetchAndGetFormData[DisposalCostsModel](ResidentKeys.disposalCosts).map(formData => formData.get.amount)

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

  val summary = FeatureLockForRTT.async { implicit request =>
    for {
      answers <- getYourAnswers
      grossGain <- calculatorConnector.calculateRttGrossGain(answers)
    } yield Ok(views.html.calculation.resident.summary(answers, grossGain))
  }
}

object SummaryController extends SummaryController {
  val calculatorConnector = CalculatorConnector
}

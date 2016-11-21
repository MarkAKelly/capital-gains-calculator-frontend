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

import common.{KeystoreKeys, TaxDates}
import connectors.CalculatorConnector
import models.nonresident._
import models.resident.properties.gain.WorthWhenGiftedModel
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AnswersConstructor extends AnswersConstructor {
  val calculatorConnector = CalculatorConnector
}

trait AnswersConstructor {
  val calculatorConnector: CalculatorConnector

  def getNRTotalGainAnswers(implicit hc: HeaderCarrier): Future[TotalGainAnswersModel] = {
    val disposalDate = calculatorConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate).map(data => data.get)
    val soldOrGivenAway = calculatorConnector.fetchAndGetFormData[SoldOrGivenAwayModel](KeystoreKeys.soldOrGivenAway).map(data => data.get)
    val soldForLess = calculatorConnector.fetchAndGetFormData[SoldForLessModel](KeystoreKeys.NonResidentKeys.soldForLess)
    val disposalValue = calculatorConnector.fetchAndGetFormData[DisposalValueModel](KeystoreKeys.disposalValue).map(data => data.get)
    val disposalCosts = calculatorConnector.fetchAndGetFormData[DisposalCostsModel](KeystoreKeys.disposalCosts).map(data => data.get)
    val howBecameOwner = calculatorConnector.fetchAndGetFormData[HowBecameOwnerModel](KeystoreKeys.howBecameOwner)
    val boughtForLess = calculatorConnector.fetchAndGetFormData[BoughtForLessModel](KeystoreKeys.boughtForLess)
    val acquisitionCosts = calculatorConnector.fetchAndGetFormData[AcquisitionCostsModel](KeystoreKeys.acquisitionCosts).map(data => data.get)
    val acquisitionDate = calculatorConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).map(data => data.get)
    val rebasedValue = calculatorConnector.fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue)
    val rebasedCosts = calculatorConnector.fetchAndGetFormData[RebasedCostsModel](KeystoreKeys.rebasedCosts)
    val improvements = calculatorConnector.fetchAndGetFormData[ImprovementsModel](KeystoreKeys.improvements).map(data => data.get)
    val otherReliefsFlat = calculatorConnector.fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat)

    def acquisitionValue(acquisitionDateModel: AcquisitionDateModel, howBecameOwnerModel: Option[HowBecameOwnerModel]): Future[AcquisitionValueModel] =
      (acquisitionDateModel, howBecameOwnerModel) match {
        case (AcquisitionDateModel("Yes",_,_,_),_) if TaxDates.dateBeforeLegislationStart(acquisitionDateModel.get) =>
          calculatorConnector.fetchAndGetFormData[WorthBeforeLegislationStartModel](KeystoreKeys.worthBeforeLegislationStart).map(data =>
            AcquisitionValueModel(data.get.worthBeforeLegislationStart)
          )
        case (_, Some(HowBecameOwnerModel(value))) if !value.equals("Bought") =>
          calculatorConnector.fetchAndGetFormData[AcquisitionValueModel](KeystoreKeys.acquisitionMarketValue)
            .map(data => data.get)
        case _ => calculatorConnector.fetchAndGetFormData[AcquisitionValueModel](KeystoreKeys.acquisitionValue).map(data => data.get)
      }

    for {
      disposalDate <- disposalDate
      soldOrGivenAway <- soldOrGivenAway
      soldForLess <- soldForLess
      disposalValue <- disposalValue
      disposalCosts <- disposalCosts
      howBecameOwner <- howBecameOwner
      boughtForLess <- boughtForLess
      acquisitionDate <- acquisitionDate
      acquisitionValue <- acquisitionValue(acquisitionDate, howBecameOwner)
      acquisitionCosts <- acquisitionCosts
      rebasedValue <- rebasedValue
      rebasedCosts <- rebasedCosts
      improvements <- improvements
      otherReliefsFlat <- otherReliefsFlat
    } yield TotalGainAnswersModel(disposalDate, soldOrGivenAway, soldForLess, disposalValue, disposalCosts,
      howBecameOwner, boughtForLess, acquisitionValue, acquisitionCosts, acquisitionDate,
      rebasedValue, rebasedCosts, improvements, otherReliefsFlat)
  }
}

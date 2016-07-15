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

package connectors

import common.Dates._
import common.KeystoreKeys
import common.KeystoreKeys.ResidentKeys
import config.{CalculatorSessionCache, WSHttp}
import constructors.nonresident.CalculateRequestConstructor
import constructors.{resident => residentConstructors}
import models.nonresident._
import models.resident
import models.resident.TaxYearModel
import models.resident.IncomeAnswersModel
import play.api.libs.json.Format
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CalculatorConnector extends CalculatorConnector with ServicesConfig {
  override val sessionCache = CalculatorSessionCache
  override val http = WSHttp
  override val serviceUrl = baseUrl("capital-gains-calculator")
}

trait CalculatorConnector {

  val sessionCache: SessionCache
  val http: HttpGet
  val serviceUrl: String

  implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json")

  def saveFormData[T](key: String, data: T)(implicit hc: HeaderCarrier, formats: Format[T]): Future[CacheMap] = {
    sessionCache.cache(key, data)
  }

  def fetchAndGetFormData[T](key: String)(implicit hc: HeaderCarrier, formats: Format[T]): Future[Option[T]] = {
    sessionCache.fetchAndGetEntry(key)
  }

  def calculateFlat(input: SummaryModel)(implicit hc: HeaderCarrier): Future[Option[CalculationResultModel]] = {
    http.GET[Option[CalculationResultModel]](s"$serviceUrl/capital-gains-calculator/calculate-flat?${
      CalculateRequestConstructor.baseCalcUrl(input)}${
      CalculateRequestConstructor.flatCalcUrlExtra(input)
    }")
  }

  def calculateTA(input: SummaryModel)(implicit hc: HeaderCarrier): Future[Option[CalculationResultModel]] = {
    http.GET[Option[CalculationResultModel]](s"$serviceUrl/capital-gains-calculator/calculate-time-apportioned?${
      CalculateRequestConstructor.baseCalcUrl(input)}${
      CalculateRequestConstructor.taCalcUrlExtra(input)
    }")
  }

  def calculateRebased(input: SummaryModel)(implicit hc: HeaderCarrier): Future[Option[CalculationResultModel]] = {
    http.GET[Option[CalculationResultModel]](s"$serviceUrl/capital-gains-calculator/calculate-rebased?${
      CalculateRequestConstructor.baseCalcUrl(input)}${
      CalculateRequestConstructor.rebasedCalcUrlExtra(input)
    }")
  }

  def getFullAEA (taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-full-aea?taxYear=$taxYear")
  }

  def getPartialAEA (taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-partial-aea?taxYear=$taxYear")
  }

  def getPA (taxYear: Int, isEligibleBlindPersonsAllowance: Boolean = false)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-pa?taxYear=$taxYear" +
      s"${if(isEligibleBlindPersonsAllowance) s"&isEligibleBlindPersonsAllowance=true"
      else ""
      }"
    )
  }

  def getTaxYear (taxYear: String)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] = {
    http.GET[Option[TaxYearModel]](s"$serviceUrl/capital-gains-calculator/tax-year?disposalDate=$taxYear")
  }

  def clearKeystore()(implicit hc: HeaderCarrier) = {
    sessionCache.remove()
  }


  def createSummary(implicit hc: HeaderCarrier): Future[SummaryModel] = {
    val customerType = fetchAndGetFormData[CustomerTypeModel](KeystoreKeys.customerType).map(formData => formData.get)
    val disabledTrustee = fetchAndGetFormData[DisabledTrusteeModel](KeystoreKeys.disabledTrustee)
    val currentIncome = fetchAndGetFormData[CurrentIncomeModel](KeystoreKeys.currentIncome)
    val personalAllowance = fetchAndGetFormData[PersonalAllowanceModel](KeystoreKeys.personalAllowance)
    val otherProperties = fetchAndGetFormData[OtherPropertiesModel](KeystoreKeys.otherProperties).map(formData => formData.get)
    val annualExemptAmount = fetchAndGetFormData[AnnualExemptAmountModel](KeystoreKeys.annualExemptAmount)
    val acquisitionDate = fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).map(formData => formData.get)
    val acquisitionValue = fetchAndGetFormData[AcquisitionValueModel](KeystoreKeys.acquisitionValue).map(formData => formData.get)
    val rebasedValue = fetchAndGetFormData[RebasedValueModel](KeystoreKeys.rebasedValue)
    val rebasedCosts = fetchAndGetFormData[RebasedCostsModel](KeystoreKeys.rebasedCosts)
    val improvements = fetchAndGetFormData[ImprovementsModel](KeystoreKeys.improvements).map(formData => formData.get)
    val disposalDate = fetchAndGetFormData[DisposalDateModel](KeystoreKeys.disposalDate).map(formData => formData.get)
    val disposalValue = fetchAndGetFormData[DisposalValueModel](KeystoreKeys.disposalValue).map(formData => formData.get)
    val acquisitionCosts = fetchAndGetFormData[AcquisitionCostsModel](KeystoreKeys.acquisitionCosts).map(formData => formData.get)
    val disposalCosts = fetchAndGetFormData[DisposalCostsModel](KeystoreKeys.disposalCosts).map(formData => formData.get)
    val allowableLosses = fetchAndGetFormData[AllowableLossesModel](KeystoreKeys.allowableLosses).map(formData => formData.get)
    val calculationElection = fetchAndGetFormData[CalculationElectionModel](KeystoreKeys.calculationElection).map(formData => formData.getOrElse(CalculationElectionModel("")))
    val otherReliefsFlat = fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsFlat).map(formData => formData.getOrElse(OtherReliefsModel(Some("No"), None)))
    val otherReliefsTA = fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsTA).map(formData => formData.getOrElse(OtherReliefsModel(Some("No"), None)))
    val otherReliefsRebased = fetchAndGetFormData[OtherReliefsModel](KeystoreKeys.otherReliefsRebased).map(formData => formData.getOrElse(OtherReliefsModel(Some("No"), None)))
    val privateResidenceRelief = fetchAndGetFormData[PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief)


    for {
      customerTypeModel <- customerType
      disabledTrusteeModel <- disabledTrustee
      currentIncomeModel <- currentIncome
      personalAllowanceModel <- personalAllowance
      otherPropertiesModel <- otherProperties
      annualExemptAmountModel <- annualExemptAmount
      acquisitionDateModel <- acquisitionDate
      acquisitionValueModel <- acquisitionValue
      rebasedValueModel <- rebasedValue
      rebasedCostsModel <- rebasedCosts
      improvementsModel <- improvements
      disposalDateModel <- disposalDate
      disposalValueModel <- disposalValue
      acquisitionCostsModel <- acquisitionCosts
      disposalCostsModel <- disposalCosts
      allowableLossesModel <- allowableLosses
      calculationElectionModel <- calculationElection
      otherReliefsFlatModel <- otherReliefsFlat
      otherReliefsTAModel <- otherReliefsTA
      otherReliefsRebasedModel <- otherReliefsRebased
      privateResidenceReliefModel <- privateResidenceRelief
    } yield SummaryModel(customerTypeModel, disabledTrusteeModel, currentIncomeModel, personalAllowanceModel, otherPropertiesModel,
      annualExemptAmountModel, acquisitionDateModel, acquisitionValueModel, rebasedValueModel, rebasedCostsModel, improvementsModel,
      disposalDateModel, disposalValueModel, acquisitionCostsModel, disposalCostsModel, allowableLossesModel,
      calculationElectionModel, otherReliefsFlatModel, otherReliefsTAModel, otherReliefsRebasedModel, privateResidenceReliefModel)
  }

  def calculateRttGrossGain(input: resident.YourAnswersSummaryModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/calculate-total-gain" +
      residentConstructors.CalculateRequestConstructor.totalGainRequestString(input)
    )
  }

  def calculateRttChargeableGain(totalGainInput: resident.YourAnswersSummaryModel,
                                 chargeableGainInput: resident.ChargeableGainAnswers,
                                 maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[resident.ChargeableGainResultModel]] = {
    http.GET[Option[resident.ChargeableGainResultModel]](s"$serviceUrl/capital-gains-calculator/calculate-chargeable-gain" +
      residentConstructors.CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      residentConstructors.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA)

    )
  }

  def calculateRttTotalGainAndTax(totalGainInput: resident.YourAnswersSummaryModel,
                                  chargeableGainInput: resident.ChargeableGainAnswers,
                                  maxAEA: BigDecimal,
                                  incomeAnswers: IncomeAnswersModel)(implicit hc: HeaderCarrier): Future[Option[resident.TotalGainAndTaxOwedModel]] = {
    http.GET[Option[resident.TotalGainAndTaxOwedModel]](s"$serviceUrl/capital-gains-calculator/calculate-resident-capital-gains-tax" +
      residentConstructors.CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      residentConstructors.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA) +
      residentConstructors.CalculateRequestConstructor.incomeAnswersRequestString(chargeableGainInput, incomeAnswers)
    )
  }

  def getYourAnswers(implicit hc: HeaderCarrier): Future[resident.YourAnswersSummaryModel] = {
    val acquisitionValue = fetchAndGetFormData[resident.AcquisitionValueModel](ResidentKeys.acquisitionValue).map(_.get.amount)
    val disposalDate = fetchAndGetFormData[resident.DisposalDateModel](ResidentKeys.disposalDate).map(formData =>
      constructDate(formData.get.day, formData.get.month, formData.get.year))
    val disposalValue = fetchAndGetFormData[resident.DisposalValueModel](ResidentKeys.disposalValue).map(_.get.amount)
    val acquisitionCosts = fetchAndGetFormData[resident.AcquisitionCostsModel](ResidentKeys.acquisitionCosts).map(_.get.amount)
    val disposalCosts = fetchAndGetFormData[resident.DisposalCostsModel](ResidentKeys.disposalCosts).map(_.get.amount)
    val improvements = fetchAndGetFormData[resident.ImprovementsModel](ResidentKeys.improvements).map(_.get.amount)

    for {
      acquisitionValue <- acquisitionValue
      disposalDate <- disposalDate
      disposalValue <- disposalValue
      acquisitionCosts <- acquisitionCosts
      disposalCosts <- disposalCosts
      improvements <- improvements
    } yield resident.YourAnswersSummaryModel(
      disposalDate,
      disposalValue,
      disposalCosts,
      acquisitionValue,
      acquisitionCosts,
      improvements
    )
  }

  def getChargeableGainAnswers (implicit hc: HeaderCarrier): Future[resident.ChargeableGainAnswers] = {
    val reliefsModel = fetchAndGetFormData[resident.ReliefsModel](ResidentKeys.reliefs)
    val reliefsValueModel = fetchAndGetFormData[resident.ReliefsValueModel](ResidentKeys.reliefsValue)
    val otherPropertiesModel = fetchAndGetFormData[resident.OtherPropertiesModel](ResidentKeys.otherProperties)
    val allowableLossesModel = fetchAndGetFormData[resident.AllowableLossesModel](ResidentKeys.allowableLosses)
    val allowableLossesValueModel = fetchAndGetFormData[resident.AllowableLossesValueModel](ResidentKeys.allowableLossesValue)
    val broughtForwardModel = fetchAndGetFormData[resident.LossesBroughtForwardModel](ResidentKeys.lossesBroughtForward)
    val broughtForwardValueModel = fetchAndGetFormData[resident.LossesBroughtForwardValueModel](ResidentKeys.lossesBroughtForwardValue)
    val annualExemptAmountModel = fetchAndGetFormData[resident.AnnualExemptAmountModel](ResidentKeys.annualExemptAmount)

    for {
      reliefs <- reliefsModel
      reliefsValue <- reliefsValueModel
      otherProperties <- otherPropertiesModel
      allowableLosses <- allowableLossesModel
      allowableLossesValue <- allowableLossesValueModel
      broughtForward <- broughtForwardModel
      broughtForwardValue <- broughtForwardValueModel
      annualExemptAmount <- annualExemptAmountModel
    } yield {
      resident.ChargeableGainAnswers(reliefs,
        reliefsValue,
        otherProperties,
        allowableLosses,
        allowableLossesValue,
        broughtForward,
        broughtForwardValue,
        annualExemptAmount)
    }

  }

  def getIncomeAnswers (implicit hc: HeaderCarrier): Future[resident.IncomeAnswersModel] = {
    val previousTaxableGainsModel = fetchAndGetFormData[resident.income.PreviousTaxableGainsModel](ResidentKeys.previousTaxableGains)
    val currentIncomeModel = fetchAndGetFormData[resident.income.CurrentIncomeModel](ResidentKeys.currentIncome)
    val personalAllowanceModel = fetchAndGetFormData[resident.income.PersonalAllowanceModel](ResidentKeys.personalAllowance)

    for {
      previousGains <- previousTaxableGainsModel
      currentIncome <- currentIncomeModel
      personalAllowance <- personalAllowanceModel
    } yield {
      resident.IncomeAnswersModel(previousGains, currentIncome, personalAllowance)
    }
  }

}
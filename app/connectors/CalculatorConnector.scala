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
import common.KeystoreKeys.{ResidentPropertyKeys, ResidentShareKeys}
import config.{CalculatorSessionCache, WSHttp}
import constructors.nonresident.CalculateRequestConstructor
import constructors.resident.{shares, properties => propertyConstructor}
import constructors.{resident => residentConstructors}
import models.nonresident._
import models.resident
import models.resident.properties.{ImprovementsModel => _, PrivateResidenceReliefModel => _, _}
import models.resident.{IncomeAnswersModel, TaxYearModel, properties}
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
      CalculateRequestConstructor.baseCalcUrl(input)
    }${
      CalculateRequestConstructor.flatCalcUrlExtra(input)
    }")
  }

  def calculateTA(input: SummaryModel)(implicit hc: HeaderCarrier): Future[Option[CalculationResultModel]] = {
    http.GET[Option[CalculationResultModel]](s"$serviceUrl/capital-gains-calculator/calculate-time-apportioned?${
      CalculateRequestConstructor.baseCalcUrl(input)
    }${
      CalculateRequestConstructor.taCalcUrlExtra(input)
    }")
  }

  def calculateRebased(input: SummaryModel)(implicit hc: HeaderCarrier): Future[Option[CalculationResultModel]] = {
    http.GET[Option[CalculationResultModel]](s"$serviceUrl/capital-gains-calculator/calculate-rebased?${
      CalculateRequestConstructor.baseCalcUrl(input)
    }${
      CalculateRequestConstructor.rebasedCalcUrlExtra(input)
    }")
  }

  def getFullAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-full-aea?taxYear=$taxYear")
  }

  def getPartialAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-partial-aea?taxYear=$taxYear")
  }

  def getPA(taxYear: Int, isEligibleBlindPersonsAllowance: Boolean = false)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-pa?taxYear=$taxYear" +
      s"${
        if (isEligibleBlindPersonsAllowance) s"&isEligibleBlindPersonsAllowance=true"
        else ""
      }"
    )
  }

  def getTaxYear(taxYear: String)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] = {
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

  //Rtt property calculation methods
  def calculateRttPropertyGrossGain(input: YourAnswersSummaryModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/calculate-total-gain" +
      propertyConstructor.CalculateRequestConstructor.totalGainRequestString(input)
    )
  }

  def calculateRttPropertyChargeableGain(totalGainInput: YourAnswersSummaryModel,
                                         chargeableGainInput: ChargeableGainAnswers,
                                         maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[resident.ChargeableGainResultModel]] = {
    http.GET[Option[resident.ChargeableGainResultModel]](s"$serviceUrl/capital-gains-calculator/calculate-chargeable-gain" +
      propertyConstructor.CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      propertyConstructor.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA)

    )
  }

  def calculateRttPropertyTotalGainAndTax(totalGainInput: YourAnswersSummaryModel,
                                          chargeableGainInput: ChargeableGainAnswers,
                                          maxAEA: BigDecimal,
                                          incomeAnswers: IncomeAnswersModel)(implicit hc: HeaderCarrier): Future[Option[resident.TotalGainAndTaxOwedModel]] = {
    http.GET[Option[resident.TotalGainAndTaxOwedModel]](s"$serviceUrl/capital-gains-calculator/calculate-resident-capital-gains-tax" +
      propertyConstructor.CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      propertyConstructor.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA) +
      propertyConstructor.CalculateRequestConstructor.incomeAnswersRequestString(chargeableGainInput, incomeAnswers)
    )
  }

  def getPropertyGainAnswers(implicit hc: HeaderCarrier): Future[YourAnswersSummaryModel] = {
    val acquisitionValue = fetchAndGetFormData[resident.AcquisitionValueModel](ResidentPropertyKeys.acquisitionValue).map(_.get.amount)
    val disposalDate = fetchAndGetFormData[resident.DisposalDateModel](ResidentPropertyKeys.disposalDate).map(formData =>
      constructDate(formData.get.day, formData.get.month, formData.get.year))
    val disposalValue = fetchAndGetFormData[resident.DisposalValueModel](ResidentPropertyKeys.disposalValue).map(_.get.amount)
    val acquisitionCosts = fetchAndGetFormData[resident.AcquisitionCostsModel](ResidentPropertyKeys.acquisitionCosts).map(_.get.amount)
    val disposalCosts = fetchAndGetFormData[resident.DisposalCostsModel](ResidentPropertyKeys.disposalCosts).map(_.get.amount)
    val improvements = fetchAndGetFormData[properties.ImprovementsModel](ResidentPropertyKeys.improvements).map(_.get.amount)

    for {
      acquisitionValue <- acquisitionValue
      disposalDate <- disposalDate
      disposalValue <- disposalValue
      acquisitionCosts <- acquisitionCosts
      disposalCosts <- disposalCosts
      improvements <- improvements
    } yield properties.YourAnswersSummaryModel(
      disposalDate,
      disposalValue,
      disposalCosts,
      acquisitionValue,
      acquisitionCosts,
      improvements
    )
  }

  def getPropertyDeductionAnswers(implicit hc: HeaderCarrier): Future[ChargeableGainAnswers] = {
    val privateResidenceReliefModel = fetchAndGetFormData[resident.properties.PrivateResidenceReliefModel](ResidentPropertyKeys.privateResidenceRelief)
    val privateResidenceReliefValueModel = fetchAndGetFormData[PrivateResidenceReliefValueModel](ResidentPropertyKeys.privateResidenceReliefValue)
    val reliefsModel = fetchAndGetFormData[ReliefsModel](ResidentPropertyKeys.reliefs)
    val reliefsValueModel = fetchAndGetFormData[ReliefsValueModel](ResidentPropertyKeys.reliefsValue)
    val otherPropertiesModel = fetchAndGetFormData[resident.OtherPropertiesModel](ResidentPropertyKeys.otherProperties)
    val allowableLossesModel = fetchAndGetFormData[resident.AllowableLossesModel](ResidentPropertyKeys.allowableLosses)
    val allowableLossesValueModel = fetchAndGetFormData[resident.AllowableLossesValueModel](ResidentPropertyKeys.allowableLossesValue)
    val broughtForwardModel = fetchAndGetFormData[resident.LossesBroughtForwardModel](ResidentPropertyKeys.lossesBroughtForward)
    val broughtForwardValueModel = fetchAndGetFormData[resident.LossesBroughtForwardValueModel](ResidentPropertyKeys.lossesBroughtForwardValue)
    val annualExemptAmountModel = fetchAndGetFormData[resident.AnnualExemptAmountModel](ResidentPropertyKeys.annualExemptAmount)

    for {
      privateResidenceRelief <- privateResidenceReliefModel
      privateResidenceReliefValue <- privateResidenceReliefValueModel
      reliefs <- reliefsModel
      reliefsValue <- reliefsValueModel
      otherProperties <- otherPropertiesModel
      allowableLosses <- allowableLossesModel
      allowableLossesValue <- allowableLossesValueModel
      broughtForward <- broughtForwardModel
      broughtForwardValue <- broughtForwardValueModel
      annualExemptAmount <- annualExemptAmountModel
    } yield {
      properties.ChargeableGainAnswers(privateResidenceRelief,
        privateResidenceReliefValue,
        reliefs,
        reliefsValue,
        otherProperties,
        allowableLosses,
        allowableLossesValue,
        broughtForward,
        broughtForwardValue,
        annualExemptAmount)
    }

  }

  def getPropertyIncomeAnswers(implicit hc: HeaderCarrier): Future[resident.IncomeAnswersModel] = {
    val previousTaxableGainsModel = fetchAndGetFormData[resident.income.PreviousTaxableGainsModel](ResidentPropertyKeys.previousTaxableGains)
    val currentIncomeModel = fetchAndGetFormData[resident.income.CurrentIncomeModel](ResidentPropertyKeys.currentIncome)
    val personalAllowanceModel = fetchAndGetFormData[resident.income.PersonalAllowanceModel](ResidentPropertyKeys.personalAllowance)

    for {
      previousGains <- previousTaxableGainsModel
      currentIncome <- currentIncomeModel
      personalAllowance <- personalAllowanceModel
    } yield {
      resident.IncomeAnswersModel(previousGains, currentIncome, personalAllowance)
    }
  }

  //Rtt share calculation methods
  def getShareGainAnswers(implicit hc: HeaderCarrier): Future[resident.shares.GainAnswersModel] = {
    val acquisitionValue = fetchAndGetFormData[resident.AcquisitionValueModel](ResidentShareKeys.acquisitionValue).map(_.get.amount)
    val disposalDate = fetchAndGetFormData[resident.DisposalDateModel](ResidentShareKeys.disposalDate).map(formData =>
      constructDate(formData.get.day, formData.get.month, formData.get.year))
    val disposalValue = fetchAndGetFormData[resident.DisposalValueModel](ResidentShareKeys.disposalValue).map(_.get.amount)
    val acquisitionCosts = fetchAndGetFormData[resident.AcquisitionCostsModel](ResidentShareKeys.acquisitionCosts).map(_.get.amount)
    val disposalCosts = fetchAndGetFormData[resident.DisposalCostsModel](ResidentShareKeys.disposalCosts).map(_.get.amount)
    for {
      acquisitionValue <- acquisitionValue
      disposalDate <- disposalDate
      disposalValue <- disposalValue
      acquisitionCosts <- acquisitionCosts
      disposalCosts <- disposalCosts
    } yield resident.shares.GainAnswersModel(
      disposalDate,
      disposalValue,
      disposalCosts,
      acquisitionValue,
      acquisitionCosts
    )
  }

  def getShareDeductionAnswers(implicit hc: HeaderCarrier): Future[resident.shares.DeductionGainAnswersModel] = {
    val otherPropertiesModel = fetchAndGetFormData[resident.OtherPropertiesModel](ResidentShareKeys.otherProperties)
    val allowableLossesModel = fetchAndGetFormData[resident.AllowableLossesModel](ResidentShareKeys.allowableLosses)
    val allowableLossesValueModel = fetchAndGetFormData[resident.AllowableLossesValueModel](ResidentShareKeys.allowableLossesValue)
    val broughtForwardModel = fetchAndGetFormData[resident.LossesBroughtForwardModel](ResidentShareKeys.lossesBroughtForward)
    val broughtForwardValueModel = fetchAndGetFormData[resident.LossesBroughtForwardValueModel](ResidentShareKeys.lossesBroughtForwardValue)
    val annualExemptAmountModel = fetchAndGetFormData[resident.AnnualExemptAmountModel](ResidentShareKeys.annualExemptAmount)

    for {
      otherProperties <- otherPropertiesModel
      allowableLosses <- allowableLossesModel
      allowableLossesValue <- allowableLossesValueModel
      broughtForward <- broughtForwardModel
      broughtForwardValue <- broughtForwardValueModel
      annualExemptAmount <- annualExemptAmountModel
    } yield {
      resident.shares.DeductionGainAnswersModel(
        otherProperties,
        allowableLosses,
        allowableLossesValue,
        broughtForward,
        broughtForwardValue,
        annualExemptAmount)
    }
  }

  def getShareIncomeAnswers(implicit hc: HeaderCarrier): Future[resident.IncomeAnswersModel] = {
    val previousTaxableGainsModel = fetchAndGetFormData[resident.income.PreviousTaxableGainsModel](ResidentShareKeys.previousTaxableGains)
    val currentIncomeModel = fetchAndGetFormData[resident.income.CurrentIncomeModel](ResidentShareKeys.currentIncome)
    val personalAllowanceModel = fetchAndGetFormData[resident.income.PersonalAllowanceModel](ResidentShareKeys.personalAllowance)

    for {
      previousGains <- previousTaxableGainsModel
      currentIncome <- currentIncomeModel
      personalAllowance <- personalAllowanceModel
    } yield {
      resident.IncomeAnswersModel(previousGains, currentIncome, personalAllowance)
    }
  }

  def calculateRttShareGrossGain(input: resident.shares.GainAnswersModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/shares/calculate-total-gain" +
      shares.CalculateRequestConstructor.totalGainRequestString(input)
    )
  }

  def calculateRttShareChargeableGain(totalGainInput: resident.shares.GainAnswersModel,
                                      chargeableGainInput: resident.shares.DeductionGainAnswersModel,
                                      maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[resident.ChargeableGainResultModel]] = {
    http.GET[Option[resident.ChargeableGainResultModel]](s"$serviceUrl/capital-gains-calculator/shares/calculate-chargeable-gain" +
      shares.CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      shares.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA)

    )
  }

  def calculateRttShareTotalGainAndTax(totalGainInput: resident.shares.GainAnswersModel,
                                       chargeableGainInput: resident.shares.DeductionGainAnswersModel,
                                       maxAEA: BigDecimal,
                                       incomeAnswers: resident.IncomeAnswersModel)(implicit hc: HeaderCarrier): Future[Option[resident.TotalGainAndTaxOwedModel]] = {
    http.GET[Option[resident.TotalGainAndTaxOwedModel]](s"$serviceUrl/capital-gains-calculator/shares/calculate-resident-capital-gains-tax" +
      shares.CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      shares.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA) +
      shares.CalculateRequestConstructor.incomeAnswersRequestString(chargeableGainInput, incomeAnswers)
    )
  }
}
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
import constructors.nonresident._
import constructors.resident.{shares, properties => propertyConstructor}
import models._
import play.api.libs.json.Format
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpResponse}

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

  def calculateTotalGain(totalGainAnswersModel: nonresident.TotalGainAnswersModel)
                        (implicit hc: HeaderCarrier): Future[Option[nonresident.TotalGainResultsModel]] = {
    http.GET[Option[nonresident.TotalGainResultsModel]](s"$serviceUrl/capital-gains-calculator/non-resident/calculate-total-gain?${
      TotalGainRequestConstructor.totalGainQuery(totalGainAnswersModel)
    }")
  }

  def calculateTaxableGainAfterPRR(totalGainAnswersModel: nonresident.TotalGainAnswersModel,
                                   privateResidenceReliefModel: nonresident.PrivateResidenceReliefModel)
                                  (implicit hc: HeaderCarrier): Future[Option[nonresident.CalculationResultsWithPRRModel]] = {
    http.GET[Option[nonresident.CalculationResultsWithPRRModel]](s"$serviceUrl/capital-gains-calculator/non-resident/calculate-gain-after-prr?${
      TotalGainRequestConstructor.totalGainQuery(totalGainAnswersModel) +
        PrivateResidenceReliefRequestConstructor.privateResidenceReliefQuery(totalGainAnswersModel, Some(privateResidenceReliefModel))
    }")
  }

  //########################################################################################################################################

  def calculateNRCGTTotalTax(totalGainAnswersModel: nonresident.TotalGainAnswersModel,
                             privateResidenceReliefModel: Option[nonresident.PrivateResidenceReliefModel],
                             totalTaxPersonalDetailsModel: nonresident.TotalPersonalDetailsCalculationModel,
                             maxAnnualExemptAmount: BigDecimal,
                             otherReliefs: Option[nonresident.AllOtherReliefsModel] = None)(implicit hc: HeaderCarrier):
  Future[Option[nonresident.CalculationResultsWithTaxOwedModel]] = {

    http.GET[Option[nonresident.CalculationResultsWithTaxOwedModel]](s"$serviceUrl/capital-gains-calculator/non-resident/calculate-tax-owed?${
      TotalGainRequestConstructor.totalGainQuery(totalGainAnswersModel) +
        PrivateResidenceReliefRequestConstructor.privateResidenceReliefQuery(totalGainAnswersModel, privateResidenceReliefModel) +
        FinalTaxAnswersRequestConstructor.additionalParametersQuery(totalTaxPersonalDetailsModel, maxAnnualExemptAmount) +
        OtherReliefsRequestConstructor.otherReliefsQuery(otherReliefs)
    }")
  }

  //########################################################################################################################################

  def calculateFlat(input: nonresident.SummaryModel)(implicit hc: HeaderCarrier): Future[Option[nonresident.CalculationResultModel]] = {
    http.GET[Option[nonresident.CalculationResultModel]](s"$serviceUrl/capital-gains-calculator/calculate-flat?${
      CalculateRequestConstructor.baseCalcUrl(input)
    }${
      CalculateRequestConstructor.flatCalcUrlExtra(input)
    }")
  }

  def calculateTA(input: nonresident.SummaryModel)(implicit hc: HeaderCarrier): Future[Option[nonresident.CalculationResultModel]] = {
    http.GET[Option[nonresident.CalculationResultModel]](s"$serviceUrl/capital-gains-calculator/calculate-time-apportioned?${
      CalculateRequestConstructor.baseCalcUrl(input)
    }${
      CalculateRequestConstructor.taCalcUrlExtra(input)
    }")
  }

  def calculateRebased(input: nonresident.SummaryModel)(implicit hc: HeaderCarrier): Future[Option[nonresident.CalculationResultModel]] = {
    http.GET[Option[nonresident.CalculationResultModel]](s"$serviceUrl/capital-gains-calculator/calculate-rebased?${
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

  def getTaxYear(taxYear: String)(implicit hc: HeaderCarrier): Future[Option[resident.TaxYearModel]] = {
    http.GET[Option[resident.TaxYearModel]](s"$serviceUrl/capital-gains-calculator/tax-year?date=$taxYear")
  }

  def clearKeystore(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    sessionCache.remove()
  }

  def createSummary(implicit hc: HeaderCarrier): Future[nonresident.SummaryModel] = {
    val customerType = fetchAndGetFormData[nonresident.CustomerTypeModel](KeystoreKeys.customerType).map(formData => formData.get)
    val disabledTrustee = fetchAndGetFormData[nonresident.DisabledTrusteeModel](KeystoreKeys.disabledTrustee)
    val currentIncome = fetchAndGetFormData[nonresident.CurrentIncomeModel](KeystoreKeys.currentIncome)
    val personalAllowance = fetchAndGetFormData[nonresident.PersonalAllowanceModel](KeystoreKeys.personalAllowance)
    val otherProperties = fetchAndGetFormData[nonresident.OtherPropertiesModel](KeystoreKeys.otherProperties).map(formData => formData.get)
    val annualExemptAmount = fetchAndGetFormData[nonresident.AnnualExemptAmountModel](KeystoreKeys.annualExemptAmount)
    val acquisitionDate = fetchAndGetFormData[nonresident.AcquisitionDateModel](KeystoreKeys.acquisitionDate).map(formData => formData.get)
    val acquisitionValue = fetchAndGetFormData[nonresident.AcquisitionValueModel](KeystoreKeys.acquisitionValue).map(formData => formData.get)
    val rebasedValue = fetchAndGetFormData[nonresident.RebasedValueModel](KeystoreKeys.rebasedValue)
    val rebasedCosts = fetchAndGetFormData[nonresident.RebasedCostsModel](KeystoreKeys.rebasedCosts)
    val improvements = fetchAndGetFormData[nonresident.ImprovementsModel](KeystoreKeys.improvements).map(formData => formData.get)
    val disposalDate = fetchAndGetFormData[nonresident.DisposalDateModel](KeystoreKeys.disposalDate).map(formData => formData.get)
    val disposalValue = fetchAndGetFormData[nonresident.DisposalValueModel](KeystoreKeys.disposalValue).map(formData => formData.get)
    val acquisitionCosts = fetchAndGetFormData[nonresident.AcquisitionCostsModel](KeystoreKeys.acquisitionCosts).map(formData => formData.get)
    val disposalCosts = fetchAndGetFormData[nonresident.DisposalCostsModel](KeystoreKeys.disposalCosts).map(formData => formData.get)
    val allowableLosses = fetchAndGetFormData[nonresident.AllowableLossesModel](KeystoreKeys.allowableLosses).map(formData => formData.get)
    val calculationElection = fetchAndGetFormData[nonresident.CalculationElectionModel](KeystoreKeys.calculationElection).map(formData =>
      formData.getOrElse(nonresident.CalculationElectionModel("")))
    val otherReliefsFlat = fetchAndGetFormData[nonresident.OtherReliefsModel](KeystoreKeys.otherReliefsFlat).map(formData =>
      formData.getOrElse(nonresident.OtherReliefsModel(0)))
    val otherReliefsTA = fetchAndGetFormData[nonresident.OtherReliefsModel](KeystoreKeys.otherReliefsTA).map(formData =>
      formData.getOrElse(nonresident.OtherReliefsModel(0)))
    val otherReliefsRebased = fetchAndGetFormData[nonresident.OtherReliefsModel](KeystoreKeys.otherReliefsRebased).map(formData =>
      formData.getOrElse(nonresident.OtherReliefsModel(0)))
    val privateResidenceRelief = fetchAndGetFormData[nonresident.PrivateResidenceReliefModel](KeystoreKeys.privateResidenceRelief)

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
    } yield nonresident.SummaryModel(customerTypeModel, disabledTrusteeModel, currentIncomeModel, personalAllowanceModel, otherPropertiesModel,
      annualExemptAmountModel, acquisitionDateModel, acquisitionValueModel, rebasedValueModel, rebasedCostsModel, improvementsModel,
      disposalDateModel, disposalValueModel, acquisitionCostsModel, disposalCostsModel, allowableLossesModel,
      calculationElectionModel, otherReliefsFlatModel, otherReliefsTAModel, otherReliefsRebasedModel, privateResidenceReliefModel)
  }

  //Rtt property calculation methods
  def calculateRttPropertyGrossGain(input: resident.properties.YourAnswersSummaryModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/calculate-total-gain" +
      propertyConstructor.CalculateRequestConstructor.totalGainRequestString(input)
    )
  }

  def calculateRttPropertyChargeableGain(totalGainInput: resident.properties.YourAnswersSummaryModel,
                                         chargeableGainInput: resident.properties.ChargeableGainAnswers,
                                         maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[resident.ChargeableGainResultModel]] = {
    http.GET[Option[resident.ChargeableGainResultModel]](s"$serviceUrl/capital-gains-calculator/calculate-chargeable-gain" +
      propertyConstructor.CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      propertyConstructor.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA)

    )
  }

  def calculateRttPropertyTotalGainAndTax(totalGainInput: resident.properties.YourAnswersSummaryModel,
                                          chargeableGainInput: resident.properties.ChargeableGainAnswers,
                                          maxAEA: BigDecimal,
                                          incomeAnswers: resident.IncomeAnswersModel)(implicit hc: HeaderCarrier): Future[Option[resident.TotalGainAndTaxOwedModel]] = {
    http.GET[Option[resident.TotalGainAndTaxOwedModel]](s"$serviceUrl/capital-gains-calculator/calculate-resident-capital-gains-tax" +
      propertyConstructor.CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      propertyConstructor.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA) +
      propertyConstructor.CalculateRequestConstructor.incomeAnswersRequestString(chargeableGainInput, incomeAnswers)
    )
  }

  //scalastyle:off
  def getPropertyGainAnswers(implicit hc: HeaderCarrier): Future[resident.properties.YourAnswersSummaryModel] = {
    val disposalDate = fetchAndGetFormData[resident.DisposalDateModel](ResidentPropertyKeys.disposalDate).map(formData =>
      constructDate(formData.get.day, formData.get.month, formData.get.year))

    //This is a proposed alternate method of writing the map without needing the case statement, need a judgement on whether
    //to use this method or older ones. Fold automatically handles the None/Some cases without matching manually
    val disposalValue = fetchAndGetFormData[resident.DisposalValueModel](ResidentPropertyKeys.disposalValue)
      .map(_.fold[Option[BigDecimal]](None)(input => Some(input.amount)))

    val worthWhenSoldForLess = fetchAndGetFormData[resident.WorthWhenSoldForLessModel](ResidentPropertyKeys.worthWhenSoldForLess).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }

    val whoDidYouGiveItTo = fetchAndGetFormData[resident.properties.gain.WhoDidYouGiveItToModel](ResidentPropertyKeys.whoDidYouGiveItTo).map {
      case Some(data) => Some(data.option)
      case _ => None
    }

    val worthWhenGaveAway = fetchAndGetFormData[resident.properties.WorthWhenGaveAwayModel](ResidentPropertyKeys.worthWhenGaveAway).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }

    val acquisitionValue = fetchAndGetFormData[resident.AcquisitionValueModel](ResidentPropertyKeys.acquisitionValue).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }

    val worthWhenInherited = fetchAndGetFormData[resident.WorthWhenInheritedModel](ResidentPropertyKeys.worthWhenInherited).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }

    val worthWhenGifted = fetchAndGetFormData[resident.properties.gain.WorthWhenGiftedModel](ResidentPropertyKeys.worthWhenGifted).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }

    val worthWhenBoughtForLess = fetchAndGetFormData[resident.properties.WorthWhenBoughtForLessModel](ResidentPropertyKeys.worthWhenBoughtForLess).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }

    val acquisitionCosts = fetchAndGetFormData[resident.AcquisitionCostsModel](ResidentPropertyKeys.acquisitionCosts).map(_.get.amount)
    val disposalCosts = fetchAndGetFormData[resident.DisposalCostsModel](ResidentPropertyKeys.disposalCosts).map(_.get.amount)
    val improvements = fetchAndGetFormData[resident.properties.ImprovementsModel](ResidentPropertyKeys.improvements).map(_.get.amount)
    val givenAway = fetchAndGetFormData[resident.properties.SellOrGiveAwayModel](ResidentPropertyKeys.sellOrGiveAway).map(_.get.givenAway)
    val sellForLess = fetchAndGetFormData[resident.SellForLessModel](ResidentPropertyKeys.sellForLess).map {
      case Some(data) => Some(data.sellForLess)
      case _ => None
    }
    val ownerBeforeLegislationStart = fetchAndGetFormData[resident.properties.gain.OwnerBeforeLegislationStartModel](ResidentPropertyKeys.ownerBeforeLegislationStart)
      .map(_.get.ownedBeforeLegislationStart)
    val valueBeforeLegislationStart = fetchAndGetFormData[resident.properties.ValueBeforeLegislationStartModel](ResidentPropertyKeys.valueBeforeLegislationStart).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }
    val howBecameOwner = fetchAndGetFormData[resident.properties.HowBecameOwnerModel](ResidentPropertyKeys.howBecameOwner).map {
      case Some(data) => Some(data.gainedBy)
      case None => None
    }
    val boughtForLessThanWorth = fetchAndGetFormData[resident.properties.BoughtForLessThanWorthModel](ResidentPropertyKeys.boughtForLessThanWorth).map {
      case Some(data) => Some(data.boughtForLessThanWorth)
      case None => None
    }

    for {
      disposalDate <- disposalDate
      disposalValue <- disposalValue
      disposalCosts <- disposalCosts
      worthWhenSoldForLess <- worthWhenSoldForLess
      whoDidYouGiveItTo <- whoDidYouGiveItTo
      worthWhenGaveAway <- worthWhenGaveAway
      acquisitionValue <- acquisitionValue
      worthWhenInherited <- worthWhenInherited
      worthWhenGifted <- worthWhenGifted
      worthWhenBoughtForLess <- worthWhenBoughtForLess
      acquisitionCosts <- acquisitionCosts
      improvements <- improvements
      givenAway <- givenAway
      sellForLess <- sellForLess
      ownerBeforeLegislationStart <- ownerBeforeLegislationStart
      valueBeforeLegislationStart <- valueBeforeLegislationStart
      howBecameOwner <- howBecameOwner
      boughtForLessThanWorth <- boughtForLessThanWorth
    } yield resident.properties.YourAnswersSummaryModel(
      disposalDate,
      disposalValue,
      worthWhenSoldForLess,
      whoDidYouGiveItTo,
      worthWhenGaveAway,
      disposalCosts,
      acquisitionValue,
      worthWhenInherited,
      worthWhenGifted,
      worthWhenBoughtForLess,
      acquisitionCosts,
      improvements,
      givenAway,
      sellForLess,
      ownerBeforeLegislationStart,
      valueBeforeLegislationStart,
      howBecameOwner,
      boughtForLessThanWorth
    )
  }

  //scalastyle:on

  def getPropertyDeductionAnswers(implicit hc: HeaderCarrier): Future[resident.properties.ChargeableGainAnswers] = {
    val otherPropertiesModel = fetchAndGetFormData[resident.OtherPropertiesModel](ResidentPropertyKeys.otherProperties)
    val allowableLossesModel = fetchAndGetFormData[resident.AllowableLossesModel](ResidentPropertyKeys.allowableLosses)
    val allowableLossesValueModel = fetchAndGetFormData[resident.AllowableLossesValueModel](ResidentPropertyKeys.allowableLossesValue)
    val broughtForwardModel = fetchAndGetFormData[resident.LossesBroughtForwardModel](ResidentPropertyKeys.lossesBroughtForward)
    val broughtForwardValueModel = fetchAndGetFormData[resident.LossesBroughtForwardValueModel](ResidentPropertyKeys.lossesBroughtForwardValue)
    val annualExemptAmountModel = fetchAndGetFormData[resident.AnnualExemptAmountModel](ResidentPropertyKeys.annualExemptAmount)
    val propertyLivedInModel = fetchAndGetFormData[resident.properties.PropertyLivedInModel](ResidentPropertyKeys.propertyLivedIn)
    val privateResidenceReliefModel = fetchAndGetFormData[resident.PrivateResidenceReliefModel](ResidentPropertyKeys.privateResidenceRelief)
    val privateResidenceReliefValueModel = fetchAndGetFormData[resident.properties.PrivateResidenceReliefValueModel](ResidentPropertyKeys.prrValue)
    val lettingsReliefModel = fetchAndGetFormData[resident.properties.LettingsReliefModel](ResidentPropertyKeys.lettingsRelief)
    val lettingsReliefValueModel = fetchAndGetFormData[resident.properties.LettingsReliefValueModel](ResidentPropertyKeys.lettingsReliefValue)

    for {
      propertyLivedIn <- propertyLivedInModel
      lettingsRelief <- lettingsReliefModel
      otherProperties <- otherPropertiesModel
      allowableLosses <- allowableLossesModel
      allowableLossesValue <- allowableLossesValueModel
      broughtForward <- broughtForwardModel
      broughtForwardValue <- broughtForwardValueModel
      annualExemptAmount <- annualExemptAmountModel
      privateResidenceRelief <- privateResidenceReliefModel
      lettingsReliefValue <- lettingsReliefValueModel
      privateResidenceReliefValue <- privateResidenceReliefValueModel
    } yield {
      resident.properties.ChargeableGainAnswers(
        otherProperties,
        allowableLosses,
        allowableLossesValue,
        broughtForward,
        broughtForwardValue,
        annualExemptAmount,
        propertyLivedIn,
        privateResidenceRelief,
        privateResidenceReliefValue,
        lettingsRelief,
        lettingsReliefValue
      )
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
  //scalastyle:off
  def getShareGainAnswers(implicit hc: HeaderCarrier): Future[resident.shares.GainAnswersModel] = {
    val disposalDate = fetchAndGetFormData[resident.DisposalDateModel](ResidentShareKeys.disposalDate).map(formData =>
      constructDate(formData.get.day, formData.get.month, formData.get.year))
    val soldForLessThanWorth = fetchAndGetFormData[resident.SellForLessModel](ResidentShareKeys.sellForLess).map(_.get.sellForLess)
    val disposalValue = fetchAndGetFormData[resident.DisposalValueModel](ResidentShareKeys.disposalValue).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }
    val worthWhenSoldForLess = fetchAndGetFormData[resident.WorthWhenSoldForLessModel](ResidentShareKeys.worthWhenSoldForLess).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }
    val disposalCosts = fetchAndGetFormData[resident.DisposalCostsModel](ResidentShareKeys.disposalCosts).map(_.get.amount)
    val ownedBeforeTaxStartDate = fetchAndGetFormData[resident.shares.OwnerBeforeLegislationStartModel](ResidentShareKeys.ownerBeforeLegislationStart).map(_.get.ownerBeforeLegislationStart)
    val valueBeforeLegislationStart = fetchAndGetFormData[resident.shares.gain.ValueBeforeLegislationStartModel](ResidentShareKeys.valueBeforeLegislationStart).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }
    val inheritedTheShares = fetchAndGetFormData[resident.shares.gain.DidYouInheritThemModel](ResidentShareKeys.didYouInheritThem).map {
      case Some(data) => Some(data.wereInherited)
      case _ => None
    }
    val worthWhenInherited = fetchAndGetFormData[resident.WorthWhenInheritedModel](ResidentShareKeys.worthWhenInherited).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }
    val acquisitionValue = fetchAndGetFormData[resident.AcquisitionValueModel](ResidentShareKeys.acquisitionValue).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }
    val acquisitionCosts = fetchAndGetFormData[resident.AcquisitionCostsModel](ResidentShareKeys.acquisitionCosts).map(_.get.amount)

    for {
      disposalDate <- disposalDate
      soldForLessThanWorth <- soldForLessThanWorth
      disposalValue <- disposalValue
      worthWhenSoldForLess <- worthWhenSoldForLess
      disposalCosts <- disposalCosts
      ownedBeforeTaxStartDate <- ownedBeforeTaxStartDate
      valueBeforeLegislationStart <- valueBeforeLegislationStart
      inheritedTheShares <- inheritedTheShares
      worthWhenInherited <- worthWhenInherited
      acquisitionValue <- acquisitionValue
      acquisitionCosts <- acquisitionCosts
    } yield resident.shares.GainAnswersModel(
      disposalDate,
      soldForLessThanWorth,
      disposalValue,
      worthWhenSoldForLess,
      disposalCosts,
      ownedBeforeTaxStartDate,
      valueBeforeLegislationStart,
      inheritedTheShares,
      worthWhenInherited,
      acquisitionValue,
      acquisitionCosts
    )
  }

  //scalastyle:on

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
                                       incomeAnswers: resident.IncomeAnswersModel)(implicit hc: HeaderCarrier):
  Future[Option[resident.TotalGainAndTaxOwedModel]] = {
    http.GET[Option[resident.TotalGainAndTaxOwedModel]](s"$serviceUrl/capital-gains-calculator/shares/calculate-resident-capital-gains-tax" +
      shares.CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      shares.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA) +
      shares.CalculateRequestConstructor.incomeAnswersRequestString(chargeableGainInput, incomeAnswers)
    )
  }
}
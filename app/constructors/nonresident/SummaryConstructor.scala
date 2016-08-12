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

import common.Validation._
import common._
import common.nonresident._
import controllers.nonresident.routes
import models._
import models.nonresident.{CalculationResultModel, PrivateResidenceReliefModel, SummaryModel}
import org.apache.commons.lang3.text.WordUtils
import play.api.i18n.Messages
import uk.gov.hmrc.play.views.helpers._
import views.html.helpers._

object SummaryConstructor {

  def calcTypeMessage(calculationType: String) = {
    calculationType match {
      case "flat" => Messages("calc.summary.calculation.details.flatCalculation")
      case "time" => Messages("calc.summary.calculation.details.timeCalculation")
      case "rebased" => Messages("calc.summary.calculation.details.rebasedCalculation")
    }
  }

  def simplePRRResult(simplePRR: Option[BigDecimal], privateResidenceReliefModel: Option[PrivateResidenceReliefModel]) = {
    (simplePRR, privateResidenceReliefModel) match {
      case (Some(data), _) => "&pound;" + MoneyPounds(data).quantity
      case (None, Some(PrivateResidenceReliefModel("Yes", _, _))) => "&pound;0.00"
      case _ => "No"
    }
  }

  //scalastyle:off

  def calculationDetails(result: CalculationResultModel, summary: SummaryModel) = summaryPageSection("calcDetails", Messages("calc.summary.calculation.details.title"),
    (result.totalGain, result.taxableGain) match {
      case (totalGain, taxableGain) if isGreaterThanZero(totalGain) && isGreaterThanZero(taxableGain) => Array(
        SummaryDataItemModel(
          Messages("calc.summary.calculation.details.calculationElection"),
          calcTypeMessage(summary.calculationElectionModel.calculationType),
          Some(routes.CalculationElectionController.calculationElection().toString())
        ),
        SummaryDataItemModel(
          Messages("calc.summary.calculation.details.totalGain"),
          "&pound;" + MoneyPounds(result.totalGain.abs, 0).quantity,
          None
        ),
        SummaryDataItemModel(
          Messages("calc.summary.calculation.details.usedAEA"),
          "&pound;" + MoneyPounds(result.usedAnnualExemptAmount, 0).quantity,
          None
        ),
        SummaryDataItemModel(
          Messages("calc.summary.calculation.details.taxableGain"),
          "&pound;" + MoneyPounds(result.taxableGain, 0).quantity,
          None
        ),
        SummaryDataItemModel(
          Messages("calc.summary.calculation.details.taxRate"),
          (isGreaterThanZero(result.baseTaxGain), isGreaterThanZero(result.upperTaxGain.getOrElse(0))) match {
            case (true, true) =>
              s"&pound;${MoneyPounds(result.baseTaxGain, 0).quantity} at ${result.baseTaxRate}%" +
                s"<br>&pound;${MoneyPounds(result.upperTaxGain.get, 0).quantity} at ${result.upperTaxRate.get}%"
            case (false, true) =>
              s"${result.upperTaxRate.get}%"
            case _ =>
              s"${result.baseTaxRate}%"
          },
          None
        )
      )

      case (totalGain, taxableGain) if !isPositive(taxableGain) => Array(
        SummaryDataItemModel(
          Messages("calc.summary.calculation.details.calculationElection"),
          calcTypeMessage(summary.calculationElectionModel.calculationType),
          Some(routes.CalculationElectionController.calculationElection().toString())
        ),
        SummaryDataItemModel(
          Messages("calc.summary.calculation.details.totalGain"),
          "&pound;" + MoneyPounds(result.totalGain.abs, 0).quantity,
          None
        ),
        SummaryDataItemModel(
          Messages("calc.summary.calculation.details.usedAEA"),
          "&pound;" + MoneyPounds(result.usedAnnualExemptAmount, 0).quantity,
          None
        ),
        SummaryDataItemModel(
          Messages("calc.summary.calculation.details.lossCarriedForward"),
          "&pound;" + MoneyPounds(result.taxableGain.abs, 0).quantity,
          None
        )
      )

      case (totalGain, taxableGain) if !isPositive(totalGain) => Array(
        SummaryDataItemModel(
          Messages("calc.summary.calculation.details.calculationElection"),
          calcTypeMessage(summary.calculationElectionModel.calculationType),
          Some(routes.CalculationElectionController.calculationElection().toString())
        ),
        SummaryDataItemModel(
          Messages("calc.summary.calculation.details.totalLoss"),
          "&pound;" + MoneyPounds(result.totalGain.abs, 0).quantity,
          None
        )
      )

      case (totalGain, taxableGain) if isGreaterThanZero(totalGain) && isPositive(taxableGain) && !isGreaterThanZero(taxableGain) => Array(
        SummaryDataItemModel(
          Messages("calc.summary.calculation.details.calculationElection"),
          calcTypeMessage(summary.calculationElectionModel.calculationType),
          Some(routes.CalculationElectionController.calculationElection().toString())
        ),
        SummaryDataItemModel(
          Messages("calc.summary.calculation.details.totalGain"),
          "&pound;" + MoneyPounds(result.totalGain.abs, 0).quantity,
          None
        ),
        SummaryDataItemModel(
          Messages("calc.summary.calculation.details.usedAEA"),
          "&pound;" + MoneyPounds(result.usedAnnualExemptAmount, 0).quantity,
          None
        ),
        SummaryDataItemModel(
          Messages("calc.summary.calculation.details.taxableGain"),
          "&pound;" + MoneyPounds(result.taxableGain, 0).quantity,
          None
        )
      )

      case (totalGain, taxableGain) if !isGreaterThanZero(totalGain) && isPositive(totalGain) => Array(
        SummaryDataItemModel(
          Messages("calc.summary.calculation.details.calculationElection"),
          calcTypeMessage(summary.calculationElectionModel.calculationType),
          Some(routes.CalculationElectionController.calculationElection().toString())
        ),
        SummaryDataItemModel(
          Messages("calc.summary.calculation.details.totalGain"),
          "&pound;" + MoneyPounds(result.totalGain.abs, 0).quantity,
          None
        )
      )
    }
  )

  def personalDetails(result: CalculationResultModel, summary: SummaryModel) = {
    summaryPageSection("personalDetails", Messages("calc.summary.personal.details.title"),
      summary.customerTypeModel.customerType match {
        case CustomerTypeKeys.trustee => summary.otherPropertiesModel.otherProperties match {
          case "Yes" => Array(
            SummaryDataItemModel(
              Messages("calc.customerType.question"),
              WordUtils.capitalize(summary.customerTypeModel.customerType),
              Some(routes.CustomerTypeController.customerType().toString())
            ),
            SummaryDataItemModel(
              Messages("calc.disabledTrustee.question"),
              summary.disabledTrusteeModel.get.isVulnerable,
              Some(routes.DisabledTrusteeController.disabledTrustee().toString())
            ),
            SummaryDataItemModel(
              Messages("calc.otherProperties.question"),
              summary.otherPropertiesModel.otherProperties.toString,
              Some(routes.OtherPropertiesController.otherProperties().toString())
            ),
            SummaryDataItemModel(
              Messages("calc.annualExemptAmount.question"),
              "&pound;" + MoneyPounds(summary.annualExemptAmountModel.get.annualExemptAmount).quantity,
              Some(routes.AnnualExemptAmountController.annualExemptAmount().toString())
            )
          )
          case "No" => Array(
            SummaryDataItemModel(
              Messages("calc.customerType.question"),
              WordUtils.capitalize(summary.customerTypeModel.customerType),
              Some(routes.CustomerTypeController.customerType().toString())
            ),
            SummaryDataItemModel(
              Messages("calc.disabledTrustee.question"),
              summary.disabledTrusteeModel.get.isVulnerable,
              Some(routes.DisabledTrusteeController.disabledTrustee().toString())
            ),
            SummaryDataItemModel(
              Messages("calc.otherProperties.question"),
              summary.otherPropertiesModel.otherProperties.toString,
              Some(routes.OtherPropertiesController.otherProperties().toString())
            )
          )
        }
        case CustomerTypeKeys.individual => summary.otherPropertiesModel.otherProperties match {
          case "Yes" =>
            summary.personalAllowanceModel match {
              case Some(x) =>
                Array(
                  SummaryDataItemModel(
                    Messages("calc.customerType.question"),
                    WordUtils.capitalize(summary.customerTypeModel.customerType),
                    Some(routes.CustomerTypeController.customerType().toString())
                  ),
                  SummaryDataItemModel(
                    Messages("calc.currentIncome.question"),
                    "&pound;" + MoneyPounds(summary.currentIncomeModel.get.currentIncome).quantity,
                    Some(routes.CurrentIncomeController.currentIncome().toString())
                  ),
                  SummaryDataItemModel(
                    Messages("calc.personalAllowance.question"),
                    "&pound;" + MoneyPounds(summary.personalAllowanceModel.get.personalAllowanceAmt).quantity,
                    Some(routes.CalculationController.personalAllowance().toString())
                  ),
                  SummaryDataItemModel(
                    Messages("calc.otherProperties.questionTwo"),
                    "&pound;" + MoneyPounds(summary.otherPropertiesModel.otherPropertiesAmt.get).quantity,
                    Some(routes.OtherPropertiesController.otherProperties().toString())
                  ),
                  SummaryDataItemModel(
                    Messages("calc.annualExemptAmount.question"),
                    "&pound;" + MoneyPounds(summary.annualExemptAmountModel.get.annualExemptAmount).quantity,
                    Some(routes.AnnualExemptAmountController.annualExemptAmount().toString())
                  )
                )
              case _ =>
                Array(
                  SummaryDataItemModel(
                    Messages("calc.customerType.question"),
                    WordUtils.capitalize(summary.customerTypeModel.customerType),
                    Some(routes.CustomerTypeController.customerType().toString())
                  ),
                  SummaryDataItemModel(
                    Messages("calc.currentIncome.question"),
                    "&pound;" + MoneyPounds(summary.currentIncomeModel.get.currentIncome).quantity,
                    Some(routes.CurrentIncomeController.currentIncome().toString())
                  ),
                  SummaryDataItemModel(
                    Messages("calc.otherProperties.questionTwo"),
                    "&pound;" + MoneyPounds(summary.otherPropertiesModel.otherPropertiesAmt.get).quantity,
                    Some(routes.OtherPropertiesController.otherProperties().toString())
                  ),
                  SummaryDataItemModel(
                    Messages("calc.annualExemptAmount.question"),
                    "&pound;" + MoneyPounds(summary.annualExemptAmountModel.get.annualExemptAmount).quantity,
                    Some(routes.AnnualExemptAmountController.annualExemptAmount().toString())
                  )
                )
            }
          case "No" =>
            summary.personalAllowanceModel match {
              case Some(x) =>
                Array(
                  SummaryDataItemModel(
                    Messages("calc.customerType.question"),
                    WordUtils.capitalize(summary.customerTypeModel.customerType),
                    Some(routes.CustomerTypeController.customerType().toString())
                  ),
                  SummaryDataItemModel(
                    Messages("calc.currentIncome.question"),
                    "&pound;" + MoneyPounds(summary.currentIncomeModel.get.currentIncome).quantity,
                    Some(routes.CurrentIncomeController.currentIncome().toString())
                  ),
                  SummaryDataItemModel(
                    Messages("calc.personalAllowance.question"),
                    "&pound;" + MoneyPounds(summary.personalAllowanceModel.get.personalAllowanceAmt).quantity,
                    Some(routes.CalculationController.personalAllowance().toString())
                  ),
                  SummaryDataItemModel(
                    Messages("calc.otherProperties.question"),
                    summary.otherPropertiesModel.otherProperties.toString,
                    Some(routes.OtherPropertiesController.otherProperties().toString())
                  )
                )
              case _ =>
                Array(
                  SummaryDataItemModel(
                    Messages("calc.customerType.question"),
                    WordUtils.capitalize(summary.customerTypeModel.customerType),
                    Some(routes.CustomerTypeController.customerType().toString())
                  ),
                  SummaryDataItemModel(
                    Messages("calc.currentIncome.question"),
                    "&pound;" + MoneyPounds(summary.currentIncomeModel.get.currentIncome).quantity,
                    Some(routes.CurrentIncomeController.currentIncome().toString())
                  ),
                  SummaryDataItemModel(
                    Messages("calc.otherProperties.question"),
                    summary.otherPropertiesModel.otherProperties.toString,
                    Some(routes.OtherPropertiesController.otherProperties().toString())
                  )
                )
            }
        }
        case CustomerTypeKeys.personalRep => summary.otherPropertiesModel.otherProperties match {
          case "Yes" => Array(
            SummaryDataItemModel(
              Messages("calc.customerType.question"),
              "Personal Representative",
              Some(routes.CustomerTypeController.customerType().toString())
            ),
            SummaryDataItemModel(
              Messages("calc.otherProperties.question"),
              summary.otherPropertiesModel.otherProperties.toString,
              Some(routes.OtherPropertiesController.otherProperties().toString())
            ),
            SummaryDataItemModel(
              Messages("calc.annualExemptAmount.question"),
              "&pound;" + MoneyPounds(summary.annualExemptAmountModel.get.annualExemptAmount).quantity,
              Some(routes.AnnualExemptAmountController.annualExemptAmount().toString())
            )
          )
          case "No" => Array(
            SummaryDataItemModel(
              Messages("calc.customerType.question"),
              "Personal Representative",
              Some(routes.CustomerTypeController.customerType().toString())
            ),
            SummaryDataItemModel(
              Messages("calc.otherProperties.question"),
              summary.otherPropertiesModel.otherProperties.toString,
              Some(routes.OtherPropertiesController.otherProperties().toString())
            )
          )
        }
      }
    )
  }

  def acquisitionDetails(result: CalculationResultModel, summary: SummaryModel) = {
    summaryPageSection("purchaseDetails", Messages("calc.summary.purchase.details.title"),
      summary.calculationElectionModel.calculationType match {
        case "rebased" => summary.acquisitionDateModel.hasAcquisitionDate match {
          case "Yes" => Array(
            SummaryDataItemModel(
              Messages("calc.acquisitionDate.questionTwo"),
              Dates.datePageFormat.format(Dates.constructDate(summary.acquisitionDateModel.day.get, summary.acquisitionDateModel.month.get, summary.acquisitionDateModel.year.get)),
              Some(routes.AcquisitionDateController.acquisitionDate().toString())
            ),
            SummaryDataItemModel(
              Messages("calc.rebasedValue.questionTwo"),
              "&pound;" + MoneyPounds(summary.rebasedValueModel.get.rebasedValueAmt.get).quantity,
              Some(routes.CalculationController.rebasedValue().toString())
            ),
            SummaryDataItemModel(
              Messages("calc.rebasedCosts.questionTwo"),
              "&pound;" + (summary.rebasedCostsModel.get.hasRebasedCosts match {
                case "Yes" => MoneyPounds(summary.rebasedCostsModel.get.rebasedCosts.get).quantity
                case "No" => "0.00"
              }),
              Some(routes.CalculationController.rebasedCosts().toString())
            )
          )
          case "No" => Array(
            SummaryDataItemModel(
              Messages("calc.acquisitionDate.question"),
              summary.acquisitionDateModel.hasAcquisitionDate,
              Some(routes.AcquisitionDateController.acquisitionDate().toString())
            ),
            SummaryDataItemModel(
              Messages("calc.rebasedValue.questionTwo"),
              "&pound;" + MoneyPounds(summary.rebasedValueModel.get.rebasedValueAmt.get).quantity,
              Some(routes.CalculationController.rebasedValue().toString())
            ),
            SummaryDataItemModel(
              Messages("calc.rebasedCosts.questionTwo"),
              "&pound;" + (summary.rebasedCostsModel.get.hasRebasedCosts match {
                case "Yes" => MoneyPounds(summary.rebasedCostsModel.get.rebasedCosts.get).quantity
                case "No" => "0.00"
              }),
              Some(routes.CalculationController.rebasedCosts().toString())
            )
          )
        }
        case _ => summary.acquisitionDateModel.hasAcquisitionDate match {
          case "Yes" => Array(
            SummaryDataItemModel(
              Messages("calc.acquisitionDate.questionTwo"),
              Dates.datePageFormat.format(Dates.constructDate(summary.acquisitionDateModel.day.get, summary.acquisitionDateModel.month.get, summary.acquisitionDateModel.year.get)),
              Some(routes.AcquisitionDateController.acquisitionDate().toString())
            ),
            SummaryDataItemModel(
              Messages("calc.acquisitionValue.question"),
              "&pound;" + MoneyPounds(summary.acquisitionValueModel.acquisitionValueAmt).quantity,
              Some(routes.AcquisitionValueController.acquisitionValue().toString())
            ),
            SummaryDataItemModel(
              Messages("calc.acquisitionCosts.question"),
              "&pound;" + MoneyPounds(summary.acquisitionCostsModel.acquisitionCostsAmt).quantity,
              Some(routes.AcquisitionCostsController.acquisitionCosts().toString())
            )
          )
          case "No" => Array(
            SummaryDataItemModel(
              Messages("calc.acquisitionDate.question"),
              summary.acquisitionDateModel.hasAcquisitionDate,
              Some(routes.AcquisitionDateController.acquisitionDate().toString())
            ),
            SummaryDataItemModel(
              Messages("calc.acquisitionValue.question"),
              "&pound;" + MoneyPounds(summary.acquisitionValueModel.acquisitionValueAmt).quantity,
              Some(routes.AcquisitionValueController.acquisitionValue().toString())
            ),
            SummaryDataItemModel(
              Messages("calc.acquisitionCosts.question"),
              "&pound;" + MoneyPounds(summary.acquisitionCostsModel.acquisitionCostsAmt).quantity,
              Some(routes.AcquisitionCostsController.acquisitionCosts().toString())
            )
          )
        }
      }

    )
  }

  def propertyDetails(result: CalculationResultModel, summary: SummaryModel) = {
    summaryPageSection("propertyDetails", Messages("calc.summary.property.details.title"),
      summary.improvementsModel.isClaimingImprovements match {
        case "Yes" => summary.calculationElectionModel.calculationType match {
          case "rebased" => Array(
            SummaryDataItemModel(
              Messages("calc.improvements.question"),
              summary.improvementsModel.isClaimingImprovements,
              Some(routes.CalculationController.improvements().toString())
            ),
            SummaryDataItemModel(
              Messages("calc.improvements.questionFour"),
              "&pound;" + MoneyPounds(summary.improvementsModel.improvementsAmtAfter.getOrElse(BigDecimal(0))).quantity,
              Some(routes.CalculationController.improvements().toString())
            )
          )
          case _ => Array(
            SummaryDataItemModel(
              Messages("calc.improvements.question"),
              summary.improvementsModel.isClaimingImprovements,
              Some(routes.CalculationController.improvements().toString())
            ),
            SummaryDataItemModel(
              Messages("calc.improvements.questionTwo"),
              "&pound;" + {
                MoneyPounds(summary.improvementsModel.improvementsAmt.getOrElse(BigDecimal(0))
                  .+(summary.improvementsModel.improvementsAmtAfter.getOrElse(BigDecimal(0)))).quantity
              },
              Some(routes.CalculationController.improvements().toString())
            )
          )
        }

        case "No" => Array(
          SummaryDataItemModel(
            Messages("calc.improvements.question"),
            summary.improvementsModel.isClaimingImprovements,
            Some(routes.CalculationController.improvements().toString())
          )
        )
      }
    )
  }

  def saleDetails(result: CalculationResultModel, summary: SummaryModel) = {
    summaryPageSection("saleDetails", Messages("calc.summary.sale.details.title"),
      Array(
        SummaryDataItemModel(
          Messages("calc.disposalDate.question"),
          Dates.datePageFormat.format(Dates.constructDate(summary.disposalDateModel.day, summary.disposalDateModel.month, summary.disposalDateModel.year)),
          Some(routes.DisposalDateController.disposalDate().toString())
        ),
        SummaryDataItemModel(
          Messages("calc.disposalValue.question"),
          "&pound;" + MoneyPounds(summary.disposalValueModel.disposalValue).quantity,
          Some(routes.CalculationController.disposalValue().toString())
        ),
        SummaryDataItemModel(
          Messages("calc.disposalCosts.question"),
          "&pound;" + MoneyPounds(summary.disposalCostsModel.disposalCosts).quantity,
          Some(routes.DisposalCostsController.disposalCosts().toString())
        )
      )
    )
  }

  def deductions(result: CalculationResultModel, summary: SummaryModel) = {
    summaryPageSection("deductions", Messages("calc.summary.deductions.title"),
      summary.calculationElectionModel.calculationType match {
        case "flat" => Array(
          SummaryDataItemModel(
            Messages("calc.privateResidenceRelief.question"),
            simplePRRResult(result.simplePRR, summary.privateResidenceReliefModel),
            Some(routes.CalculationController.privateResidenceRelief().toString())
          ),
          SummaryDataItemModel(
            Messages("calc.allowableLosses.question.two"),
            "&pound;" + (summary.allowableLossesModel.isClaimingAllowableLosses match {
              case "Yes" => MoneyPounds(summary.allowableLossesModel.allowableLossesAmt.get).quantity
              case "No" => "0.00"
            }),
            Some(routes.CalculationController.allowableLosses().toString())
          ),
          summary.otherReliefsModelFlat.isClaimingOtherReliefs match {
            case Some("No") =>
              SummaryDataItemModel(
                Messages("calc.otherReliefs.questionTwo"),
                summary.otherReliefsModelFlat.isClaimingOtherReliefs match {
                  case Some(data) => summary.otherReliefsModelFlat.isClaimingOtherReliefs.get
                  case None => "No"
                },
                Some(routes.CalculationController.otherReliefs().toString())
              )
            case Some("Yes") => SummaryDataItemModel(
              Messages("calc.otherReliefs.question"),
              "&pound;" + (summary.otherReliefsModelFlat.otherReliefs match {
                case Some(data) => MoneyPounds(data).quantity
                case None => "0.00"
              }),
              Some(routes.CalculationController.otherReliefs().toString()))
            case _ => SummaryDataItemModel(
              Messages("calc.otherReliefs.question"),
              "&pound;" + (summary.otherReliefsModelFlat.otherReliefs match {
                case Some(data) => MoneyPounds(data).quantity
                case None => "0.00"
              }),
              Some(routes.CalculationController.otherReliefsFlat().toString()))
          })
        case "time" => Array(
          SummaryDataItemModel(
            Messages("calc.privateResidenceRelief.question"),
            simplePRRResult(result.simplePRR, summary.privateResidenceReliefModel),
            Some(routes.CalculationController.privateResidenceRelief().toString())
          ),
          SummaryDataItemModel(
            Messages("calc.allowableLosses.question.two"),
            "&pound;" + (summary.allowableLossesModel.isClaimingAllowableLosses match {
              case "Yes" => MoneyPounds(summary.allowableLossesModel.allowableLossesAmt.get).quantity
              case "No" => "0.00"
            }),
            Some(routes.CalculationController.allowableLosses().toString())
          ),
          SummaryDataItemModel(
            Messages("calc.otherReliefs.question"),
            "&pound;" + (summary.otherReliefsModelTA.otherReliefs match {
              case Some(data) => MoneyPounds(data).quantity
              case None => "0.00"
            }),
            Some(routes.CalculationController.otherReliefsTA().toString())
          )
        )
        case "rebased" => Array(
          SummaryDataItemModel(
            Messages("calc.privateResidenceRelief.question"),
            simplePRRResult(result.simplePRR, summary.privateResidenceReliefModel),
            Some(routes.CalculationController.privateResidenceRelief().toString())
          ),
          SummaryDataItemModel(
            Messages("calc.allowableLosses.question.two"),
            "&pound;" + (summary.allowableLossesModel.isClaimingAllowableLosses match {
              case "Yes" => MoneyPounds(summary.allowableLossesModel.allowableLossesAmt.get).quantity
              case "No" => "0.00"
            }),
            Some(routes.CalculationController.allowableLosses().toString())
          ),
          SummaryDataItemModel(
            Messages("calc.otherReliefs.question"),
            "&pound;" + (summary.otherReliefsModelRebased.otherReliefs match {
              case Some(data) => MoneyPounds(data).quantity
              case None => "0.00"
            }),
            Some(routes.CalculationController.otherReliefsRebased().toString())
          )
        )
      }
    )
  }

  def gainMessage(result: CalculationResultModel) = {
    if (result.totalGain >= 0) Messages("calc.otherReliefs.totalGain")
    else Messages("calc.otherReliefs.totalLoss")
  }

  def setPositive(result: CalculationResultModel) = {
    BigDecimal(Math.abs(result.totalGain.toDouble)).setScale(2).toString()
  }
}

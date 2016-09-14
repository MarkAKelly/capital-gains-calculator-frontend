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

package routes.properties

import org.scalatest._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import controllers.resident.properties.routes._

class RoutesSpec extends UnitSpec with WithFakeApplication with Matchers {

  "The URL for the introduction Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/" in {
      val path = controllers.resident.properties.routes.PropertiesController.introduction().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/"
    }
  }

  "The URL for the disposal date Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/disposal-date" in {
      GainController.disposalDate().url shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-date"
    }
  }

  "The URL for the submit disposal date Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/disposal-date" in {
      GainController.submitDisposalDate().url shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-date"
    }
  }

  "The URL for the outside tax years Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/outside-tax-years" in {
      GainController.outsideTaxYears().url shouldEqual "/calculate-your-capital-gains/resident/properties/outside-tax-years"
    }
  }

  "The URL for the GET Sell Or Give Away action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/sell-or-give-away" in {
      GainController.sellOrGiveAway().url shouldEqual "/calculate-your-capital-gains/resident/properties/sell-or-give-away"
    }
  }

  "The URL for the POST Sell Or Give Away action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/sell-or-give-away" in {
      GainController.submitSellOrGiveAway().url shouldEqual "/calculate-your-capital-gains/resident/properties/sell-or-give-away"
    }
  }

  "The URL for the disposal value or market value Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/disposal-value" in {
      GainController.disposalValue().url shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-value"
    }
  }

  "The URL for the submit disposal value Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/disposal-value" in {
      GainController.submitDisposalValue().url shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-value"
    }
  }

  "The URL for the acquisition value or market value Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/acquisition-value" in {
      GainController.acquisitionValue().url shouldEqual "/calculate-your-capital-gains/resident/properties/acquisition-value"
    }
  }

  "The URL for the submit acquisition value or market value Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/acquisition-value" in {
      GainController.submitAcquisitionValue().url shouldEqual "/calculate-your-capital-gains/resident/properties/acquisition-value"
    }
  }

  "The URL for the disposal costs action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/disposal-costs" in {
      GainController.disposalCosts().url shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-costs"
    }
  }

  "The URL for the acquisition costs action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/acquisition-costs" in {
      GainController.acquisitionCosts().url shouldEqual "/calculate-your-capital-gains/resident/properties/acquisition-costs"
    }
  }

  "The URL for the submit acquisition costs action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/acquisition-costs" in {
      GainController.acquisitionCosts().url shouldEqual "/calculate-your-capital-gains/resident/properties/acquisition-costs"
    }
  }

  "The URL for the improvements Action" should {
    s"be equal to /calculate-your-capital-gains/resident/properties/improvements" in {
      GainController.improvements().url shouldEqual "/calculate-your-capital-gains/resident/properties/improvements"
    }
  }

  "The URL for the submit disposal costs action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/disposal-costs" in {
      GainController.disposalCosts().url shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-costs"
    }
  }

  "The URL for the summary action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/summary" in {
      SummaryController.summary().url shouldEqual "/calculate-your-capital-gains/resident/properties/summary"
    }
  }

  "The URL for the lettings relief value input action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/lettings-relief-value" in {
      val path = controllers.resident.properties.routes.DeductionsController.lettingsReliefValue().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/lettings-relief-value"
    }
  }

  "The URL for the lettingsRelief action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/lettings-relief" in {
      DeductionsController.lettingsRelief().url shouldEqual "/calculate-your-capital-gains/resident/properties/lettings-relief"
    }
  }

  "The URL for the submitLettingsRelief action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/lettings-relief" in {
      DeductionsController.submitLettingsRelief().url shouldEqual "/calculate-your-capital-gains/resident/properties/lettings-relief"
    }
  }

  "The URL for the private residence relief action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/private-residence-relief" in {
      DeductionsController.privateResidenceRelief().url shouldEqual "/calculate-your-capital-gains/resident/properties/private-residence-relief"
    }
  }

  "The URL for the submit private residence relief action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/private-residence-relief" in {
      DeductionsController.submitPrivateResidenceRelief().url shouldEqual "/calculate-your-capital-gains/resident/properties/private-residence-relief"
    }
  }

  "The URL for the other properties action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/other-properties" in {
      DeductionsController.otherProperties().url shouldEqual "/calculate-your-capital-gains/resident/properties/other-properties"
    }
  }

  "The URL for the submit other properties action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/other-properties" in {
      DeductionsController.submitOtherProperties().url shouldEqual "/calculate-your-capital-gains/resident/properties/other-properties"
    }
  }

  "The URL for the allowable losses value action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/allowable-losses-value" in {
      DeductionsController.allowableLossesValue().url shouldEqual "/calculate-your-capital-gains/resident/properties/allowable-losses-value"
    }
  }

  "The URL for the submit allowable losses value action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/allowable-losses-value" in {
      DeductionsController.submitAllowableLossesValue().url shouldEqual "/calculate-your-capital-gains/resident/properties/allowable-losses-value"
    }
  }

  "The URL for the lossesBroughtForward action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/losses-brought-forward" in {
      DeductionsController.lossesBroughtForward().url shouldEqual "/calculate-your-capital-gains/resident/properties/losses-brought-forward"
    }
  }

  "The URL for the annualExemptAmount action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/annual-exempt-amount" in {
      DeductionsController.annualExemptAmount().url shouldEqual "/calculate-your-capital-gains/resident/properties/annual-exempt-amount"
    }
  }

  "The URL for the submit annualExemptAmount action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/annual-exempt-amount" in {
      DeductionsController.submitAnnualExemptAmount().url shouldEqual "/calculate-your-capital-gains/resident/properties/annual-exempt-amount"
    }
  }

  "The URL for the allowableLosses action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/allowable-losses" in {
      DeductionsController.allowableLosses().url shouldEqual "/calculate-your-capital-gains/resident/properties/allowable-losses"
    }
  }

  "The URL for the lossesBroughtForwardValue action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/losses-brought-forward-value" in {
      DeductionsController.lossesBroughtForwardValue().url shouldEqual "/calculate-your-capital-gains/resident/properties/losses-brought-forward-value"
    }
  }

  "The URL for the submitLossesBroughtForwardValue action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/losses-brought-forward-value" in {
      DeductionsController.submitLossesBroughtForwardValue().url shouldEqual "/calculate-your-capital-gains/resident/properties/losses-brought-forward-value"
    }
  }

  "The URL for the previousTaxableGains action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/previous-taxable-gains" in {
      IncomeController.previousTaxableGains().url shouldEqual "/calculate-your-capital-gains/resident/properties/previous-taxable-gains"
    }
  }

  "The URL for the currentIncome action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/current-income" in {
      IncomeController.currentIncome().url shouldEqual "/calculate-your-capital-gains/resident/properties/current-income"
    }
  }

  "The URL for the personalAllowance action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/personal-allowance" in {
      IncomeController.personalAllowance().url shouldEqual "/calculate-your-capital-gains/resident/properties/personal-allowance"
    }
  }

  "The URL for the submit personalAllowance action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/personal-allowance" in {
      IncomeController.submitPersonalAllowance().url shouldEqual "/calculate-your-capital-gains/resident/properties/personal-allowance"
    }
  }

  "The URL for the propertyLivedIn action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/property-lived-in" in {
      DeductionsController.propertyLivedIn().url shouldEqual "/calculate-your-capital-gains/resident/properties/property-lived-in"
    }
  }

  "The URL for the submit propertyLivedIn action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/property-lived-in" in {
      DeductionsController.submitPropertyLivedIn().url shouldEqual "/calculate-your-capital-gains/resident/properties/property-lived-in"
    }
  }

  "The URL for the gainSummaryReport action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/gain-report" in {
      ReportController.gainSummaryReport().url shouldEqual "/calculate-your-capital-gains/resident/properties/gain-report"
    }
  }

  "The URL for the deductionsReport action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/deductions-report" in {
      ReportController.deductionsReport().url shouldEqual "/calculate-your-capital-gains/resident/properties/deductions-report"
    }
  }

  "The URL for the finalSummaryReport action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/final-report" in {
      ReportController.finalSummaryReport().url shouldEqual "/calculate-your-capital-gains/resident/properties/final-report"
    }
  }

  "The URL for the GET Private Residence Relief Value action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/private-residence-relief-value" in {
      DeductionsController.privateResidenceReliefValue().url shouldEqual "/calculate-your-capital-gains/resident/properties/private-residence-relief-value"
    }
  }

  "The URL for the POST Private Residence Relief Value action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/private-residence-relief-value" in {
      DeductionsController.submitPrivateResidenceReliefValue().url shouldEqual "/calculate-your-capital-gains/resident/properties/private-residence-relief-value"
    }
  }
}

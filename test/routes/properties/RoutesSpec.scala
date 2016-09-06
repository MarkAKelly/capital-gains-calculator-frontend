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

class RoutesSpec extends UnitSpec with WithFakeApplication with Matchers {

  "The URL for the disposal date Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/disposal-date" in {
      val path = controllers.resident.properties.routes.GainController.disposalDate().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-date"
    }
  }

  "The URL for the submit disposal date Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/disposal-date" in {
      val path = controllers.resident.properties.routes.GainController.submitDisposalDate().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-date"
    }
  }

  "The URL for the outside tax years Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/outside-tax-years" in {
      val path = controllers.resident.properties.routes.GainController.outsideTaxYears().toString
      path shouldEqual "/calculate-your-capital-gains/resident/properties/outside-tax-years"
    }
  }

  "The URL for the disposal value or market value Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/disposal-value" in {
      val path = controllers.resident.properties.routes.GainController.disposalValue().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-value"
    }
  }

  "The URL for the submit disposal value Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/disposal-value" in {
      val path = controllers.resident.properties.routes.GainController.submitDisposalValue().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-value"
    }
  }

  "The URL for the acquisition value or market value Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/acquisition-value" in {
      val path = controllers.resident.properties.routes.GainController.acquisitionValue().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/acquisition-value"
    }
  }

  "The URL for the submit acquisition value or market value Action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/acquisition-value" in {
      val path = controllers.resident.properties.routes.GainController.submitAcquisitionValue().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/acquisition-value"
    }
  }

  "The URL for the disposal costs action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/disposal-costs" in {
      val path = controllers.resident.properties.routes.GainController.disposalCosts().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-costs"
    }
  }

  "The URL for the acquisition costs action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/acquisition-costs" in {
      val path = controllers.resident.properties.routes.GainController.acquisitionCosts().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/acquisition-costs"
    }
  }

  "The URL for the submit acquisition costs action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/acquisition-costs" in {
      val path = controllers.resident.properties.routes.GainController.acquisitionCosts().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/acquisition-costs"
    }
  }

  "The URL for the improvements Action" should {
    s"be equal to /calculate-your-capital-gains/resident/properties/improvements" in {
      val path = controllers.resident.properties.routes.GainController.improvements().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/improvements"
    }
  }

  "The URL for the submit disposal costs action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/disposal-costs" in {
      val path = controllers.resident.properties.routes.GainController.disposalCosts().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/disposal-costs"
    }
  }

  "The URL for the summary action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/summary" in {
      val path = controllers.resident.properties.routes.SummaryController.summary().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/summary"
    }
  }

  "The URL for the other properties action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/other-properties" in {
      val path = controllers.resident.properties.routes.DeductionsController.otherProperties().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/other-properties"
    }
  }

  "The URL for the submit other properties action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/other-properties" in {
      val path = controllers.resident.properties.routes.DeductionsController.submitOtherProperties().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/other-properties"
    }
  }

  "The URL for the allowable losses value action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/allowable-losses-value" in {
      val path = controllers.resident.properties.routes.DeductionsController.allowableLossesValue().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/allowable-losses-value"
    }
  }

  "The URL for the submit allowable losses value action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/allowable-losses-value" in {
      val path = controllers.resident.properties.routes.DeductionsController.submitAllowableLossesValue().toString
      path shouldEqual "/calculate-your-capital-gains/resident/properties/allowable-losses-value"
    }
  }

  "The URL for the lossesBroughtForward action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/losses-brought-forward" in {
      val path = controllers.resident.properties.routes.DeductionsController.lossesBroughtForward().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/losses-brought-forward"
    }
  }

  "The URL for the annualExemptAmount action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/annual-exempt-amount" in {
      val path = controllers.resident.properties.routes.DeductionsController.annualExemptAmount().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/annual-exempt-amount"
    }
  }

  "The URL for the submit annualExemptAmount action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/annual-exempt-amount" in {
      val path = controllers.resident.properties.routes.DeductionsController.submitAnnualExemptAmount().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/annual-exempt-amount"
    }
  }

  "The URL for the allowableLosses action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/allowable-losses" in {
      val path = controllers.resident.properties.routes.DeductionsController.allowableLosses().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/allowable-losses"
    }
  }

  "The URL for the lossesBroughtForwardValue action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/losses-brought-forward-value" in {
      val path = controllers.resident.properties.routes.DeductionsController.lossesBroughtForwardValue().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/losses-brought-forward-value"
    }
  }

  "The URL for the submitLossesBroughtForwardValue action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/losses-brought-forward-value" in {
      val path = controllers.resident.properties.routes.DeductionsController.submitLossesBroughtForwardValue().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/losses-brought-forward-value"
    }
  }

  "The URL for the previousTaxableGains action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/previous-taxable-gains" in {
      val path = controllers.resident.properties.routes.IncomeController.previousTaxableGains().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/previous-taxable-gains"
    }
  }

  "The URL for the currentIncome action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/current-income" in {
      val path = controllers.resident.properties.routes.IncomeController.currentIncome().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/current-income"
    }
  }

  "The URL for the personalAllowance action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/personal-allowance" in {
      val path = controllers.resident.properties.routes.IncomeController.personalAllowance().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/personal-allowance"
    }
  }

  "The URL for the submit personalAllowance action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/personal-allowance" in {
      val path = controllers.resident.properties.routes.IncomeController.submitPersonalAllowance().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/personal-allowance"
    }
  }

  "The URL for the propertyLivedIn action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/property-lived-in" in {
      val path = controllers.resident.properties.routes.DeductionsController.propertyLivedIn().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/property-lived-in"
    }
  }

  "The URL for the submit propertyLivedIn action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/property-lived-in" in {
      val path = controllers.resident.properties.routes.DeductionsController.submitPropertyLivedIn().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/property-lived-in"
    }
  }

  "The URL for the gainSummaryReport action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/gain-report" in {
      val path = controllers.resident.properties.routes.ReportController.gainSummaryReport().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/gain-report"
    }
  }

  "The URL for the deductionsReport action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/deductions-report" in {
      val path = controllers.resident.properties.routes.ReportController.deductionsReport().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/deductions-report"
    }
  }

  "The URL for the finalSummaryReport action" should {
    "be equal to /calculate-your-capital-gains/resident/properties/final-report" in {
      val path = controllers.resident.properties.routes.ReportController.finalSummaryReport().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/properties/final-report"
    }
  }
}

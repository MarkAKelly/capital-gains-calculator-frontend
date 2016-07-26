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

package routes.shares

import org.scalatest._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import controllers.resident.shares.routes._

class RoutesSpec extends UnitSpec with WithFakeApplication with Matchers {

  /* Outside Tax Years routes */
  "The URL for the resident/shares outside tax years Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/outside-tax-years" in {
      val path = GainController.outsideTaxYears().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/outside-tax-years"
    }
  }

  /* Disposal Date routes */
  "The URL for the resident/shares disposal date Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/disposal-date" in {
      val path = GainController.disposalDate().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-date"
    }
  }

  "The URL for the resident/shares submit disposal date Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/disposal-date" in {
      val path = GainController.submitDisposalDate().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-date"
    }
  }

  /* Disposal Value routes */
  "The URL for the resident/shares disposal value Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/disposal-value" in {
      val path = GainController.disposalValue().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-value"
    }
  }

  "The URL for the resident/shares submit disposal value Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/disposal-value" in {
      val path = GainController.submitDisposalValue().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-value"
    }
  }

  /* Disposal Costs routes */
  "The URL for the resident/shares disposal costs Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/disposal-costs" in {
      val path = GainController.disposalCosts().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-costs"
    }
  }

  "The URL for the resident/shares submit disposal costs Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/disposal-costs" in {
      val path = GainController.submitDisposalCosts().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-costs"
    }
  }

  /* Acquisition Value routes */
  "The URL for the resident/shares acquisition value Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/acquisition-value" in {
      val path = GainController.acquisitionValue().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/acquisition-value"
    }
  }

  "The URL for the resident/shares submit acquisition value Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/acquisition-value" in {
      val path = GainController.submitAcquisitionValue().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/acquisition-value"
    }
  }

  /* Allowable Losses routes */
  "The URL for the resident shares allowableLosses action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/allowable-losses" in {
      val path = DeductionsController.allowableLosses().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/allowable-losses"
    }
  }

  "The URL for the resident shares submitAllowableLosses action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/allowable-losses" in {
      val path = DeductionsController.submitAllowableLosses().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/allowable-losses"
    }
  }

  "The URL for the lossesBroughtForward action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/losses-brought-forward" in {
      val path = DeductionsController.lossesBroughtForward().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/losses-brought-forward"
    }
  }

  /* Losses Brought Forward Value routes */
  "The URL for the resident/shares lossesBroughtForward Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/losses-brought-forward" in {
      val path = DeductionsController.lossesBroughtForwardValue().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/losses-brought-forward-value"
    }
  }

  "The URL for the resident/shares submit disposal date Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/losses-brought-forward" in {
      val path = DeductionsController.submitLossesBroughtForwardValue().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/losses-brought-forward-value"
    }
  }

  /* Annual Exempt Amount routes */
  "The URL for the resident shares annualExemptAmount action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/annual-exempt-amount" in {
      val path = DeductionsController.annualExemptAmount().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/annual-exempt-amount"
    }
  }

  "The URL for the resident shares submitAnnualExemptAmount action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/annual-exempt-amount" in {
      val path = DeductionsController.submitAnnualExemptAmount().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/annual-exempt-amount"
    }
  }

  "The URL for the previousTaxableGains action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/previous-taxable-gains" in {
      val path = IncomeController.previousTaxableGains().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/previous-taxable-gains"
    }
  }

  "The URL for the submitPreviousTaxableGains action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/previous-taxable-gains" in {
      val path = IncomeController.submitPreviousTaxableGains().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/previous-taxable-gains"

    }
  }

  "The URL for the resident shares currentIncome action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/current-income" in {
      val path = controllers.resident.shares.routes.IncomeController.currentIncome().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/current-income"
    }
  }

  "The URL for the resident shares submitCurrentIncome action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/current-income" in {
      val path = controllers.resident.shares.routes.IncomeController.submitCurrentIncome().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/current-income"
    }
  }

  /* Personal Allowance routes */
  "The URL for the resident shares personalAllowance action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/personal-allowance" in {
      val path = IncomeController.personalAllowance().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/personal-allowance"
    }
  }

  "The URL for the resident shares submitPersonalAllowance action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/personal-allowance" in {
      val path = IncomeController.submitPersonalAllowance().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/personal-allowance"
    }
  }
}
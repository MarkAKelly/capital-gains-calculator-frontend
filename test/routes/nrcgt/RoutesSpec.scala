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

package routes.nrcgt

import controllers.resident.properties.GainController
import org.scalatest.Matchers
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

/**
  * Created by emma on 12/09/16.
  */
class RoutesSpec extends UnitSpec with WithFakeApplication with Matchers {
  /* Customer Type routes */
  "The URL for the customer type Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/" in {
      val path = controllers.nonresident.routes.CustomerTypeController.customerType().url

      path shouldEqual "/calculate-your-capital-gains/non-resident/"
    }
  }

  "The URL for the disabled trustee Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/disabled-trustee" in {
      val path = controllers.nonresident.routes.DisabledTrusteeController.disabledTrustee.url

      path shouldEqual "/calculate-your-capital-gains/non-resident/disabled-trustee"
    }
  }

  "The URL for the current income Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/current-income" in {
      val path = controllers.nonresident.routes.CurrentIncomeController.currentIncome.url
      path shouldEqual "/calculate-your-capital-gains/non-resident/current-income"
    }
  }

  "The URL for personal allowance Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/personal-allowance" in {
      val path = controllers.nonresident.routes.PersonalAllowanceController.personalAllowance().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/personal-allowance"
    }
  }

  "The URL for other properties Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-properties" in {
      val path = controllers.nonresident.routes.OtherPropertiesController.otherProperties().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-properties"
    }
  }

  "The URL for the allowance Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/allowance" in {
      val path = controllers.nonresident.routes.AnnualExemptAmountController.annualExemptAmount().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/allowance"
    }
  }

  "The URL for the acquisition date Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/acquisition-date" in {
      val path = controllers.nonresident.routes.AcquisitionDateController.acquisitionDate().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/acquisition-date"
    }
  }

  "The URL for the rebased value Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/rebased-value" in {
      val path = controllers.nonresident.routes.RebasedValueController.rebasedValue().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/rebased-value"
    }
  }

  "The URL for the rebased costs Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/rebased-costs" in {
      val path = controllers.nonresident.routes.RebasedCostsController.rebasedCosts().url.toString
      path shouldEqual "/calculate-your-capital-gains/non-resident/rebased-costs"
    }
  }

  "The URL for the improvements Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/improvements" in {
      val path = controllers.nonresident.routes.ImprovementsController.improvements().toString()
      path shouldEqual "/calculate-your-capital-gains/non-resident/improvements"
    }
  }

  "The URL for the disposal dates Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/disposal-date" in {
      val path = controllers.nonresident.routes.DisposalDateController.disposalDate().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/disposal-date"
    }
  }

  "The URL for the no capital gains tax Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/no-capital-gains-tax" in {
      val path = controllers.nonresident.routes.NoCapitalGainsTaxController.noCapitalGainsTax().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/no-capital-gains-tax"
    }
  }

  "The URL for the disposal value Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/disposal-value" in {
      val path = controllers.nonresident.routes.DisposalValueController.disposalValue().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/disposal-value"
    }
  }

  "The URL for the acquisition costs Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/acquisition-costs" in {
      val path = controllers.nonresident.routes.AcquisitionCostsController.acquisitionCosts().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/acquisition-costs"
    }
  }

  "The URL for the disposal costs Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/disposal-costs" in {
      val path = controllers.nonresident.routes.DisposalCostsController.disposalCosts().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/disposal-costs"
    }
  }

  "The URL for the private residence relief Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/private-residence-relief" in {
      val path = controllers.nonresident.routes.PrivateResidenceReliefController.privateResidenceRelief().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/private-residence-relief"
    }
  }

  "The URL for the allowable losses Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/allowable-losses" in {
      val path = controllers.nonresident.routes.AllowableLossesController.allowableLosses().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/allowable-losses"
    }
  }

  "The URL for the calculation election Action" should {
    "be equal to /calculate/your-capital-gains/non-resident/calculation-election" in {
      val path = controllers.nonresident.routes.CalculationElectionController.calculationElection().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/calculation-election"
    }
  }

  "The URL for the other reliefs Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs" in {
      val path = controllers.nonresident.routes.OtherReliefsController.otherReliefs().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs"
    }
  }

  "The URL for the other reliefs flat Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs-flat" in {
      val path = controllers.nonresident.routes.OtherReliefsFlatController.otherReliefsFlat().url.toString
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs-flat"
    }
  }

  "The URL for the other reliefs time apportioned Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs-time-apportioned" in {
      val path = controllers.nonresident.routes.OtherReliefsTAController.otherReliefsTA().url.toString
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs-time-apportioned"
    }
  }

  "The URL for the other reliefs rebased Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/other-reliefs-rebased" in {
      val path = controllers.nonresident.routes.OtherReliefsRebasedController.otherReliefsRebased().url.toString
      path shouldEqual "/calculate-your-capital-gains/non-resident/other-reliefs-rebased"
    }
  }

  "The URL for the summary Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/summary" in {
      val path = controllers.nonresident.routes.SummaryController.summary().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/summary"
    }
  }

  "The URL for the restart Action" should {
    "be equal to /calculate-your-capital-gains/non-resident/restart" in {
      val path = controllers.nonresident.routes.SummaryController.restart().url
      path shouldEqual "/calculate-your-capital-gains/non-resident/restart"
    }
  }
}
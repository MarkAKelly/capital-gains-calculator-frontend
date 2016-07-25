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

  "The URL for the resident/shares outside tax years Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/outside-tax-years" in {
      val path = GainController.outsideTaxYears().url
      path shouldEqual "/calculate-your-capital-gains/resident/shares/outside-tax-years"
    }
  }

  "The URL for the resident/shares disposal date Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/disposal-date" in {
      val path = controllers.resident.shares.routes.GainController.disposalDate().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-date"
    }
  }

  "The URL for the resident/shares submit disposal date Action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/disposal-date" in {
      val path = controllers.resident.shares.routes.GainController.submitDisposalDate().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-date"
    }
  }

  "The URL for the resident shares allowableLosses action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/allowable-losses" in {
      val path = controllers.resident.shares.routes.DeductionsController.allowableLosses().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/allowable-losses"
    }
  }

  "The URL for the resident shares submitAllowableLosses action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/allowable-losses" in {
      val path = controllers.resident.shares.routes.DeductionsController.submitAllowableLosses().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/allowable-losses"
    }
  }

  "The URL for the resident shares annualExemptAmount action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/annual-exempt-amount" in {
      val path = controllers.resident.shares.routes.DeductionsController.annualExemptAmount().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/annual-exempt-amount"
    }
  }

  "The URL for the resident shares submitAnnualExemptAmount action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/annual-exempt-amount" in {
      val path = controllers.resident.shares.routes.DeductionsController.submitAnnualExemptAmount().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/annual-exempt-amount"
    }
  }

  "The URL for the resident shares personalAllowance action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/personal-allowance" in {
      val path = controllers.resident.shares.routes.IncomeController.personalAllowance().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/personal-allowance"
    }
  }

  "The URL for the resident shares submitPersonalAllowance action" should {
    "be equal to /calculate-your-capital-gains/resident/shares/personal-allowance" in {
      val path = controllers.resident.shares.routes.IncomeController.submitPersonalAllowance().toString()
      path shouldEqual "/calculate-your-capital-gains/resident/shares/personal-allowance"
    }
  }
}
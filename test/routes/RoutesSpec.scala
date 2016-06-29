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

package routes

import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.scalatest._

class RoutesSpec extends UnitSpec with WithFakeApplication with Matchers {

  "The URL for the disposal date Action" should {
   "be equal to /calculate-your-capital-gains/resident/disposal-date" in {
     val path = controllers.resident.routes.GainController.disposalDate.toString()
     path shouldEqual "/calculate-your-capital-gains/resident/disposal-date"
    }
  }

  "The URL for the submit disposal date Action" should {
    "be equal to /calculate-your-capital-gains/resident/disposal-date" in {
      val path = controllers.resident.routes.GainController.submitDisposalDate.toString()
      path shouldEqual "/calculate-your-capital-gains/resident/disposal-date"
    }
  }

  "The URL for the disposal value or market value Action" should {
    "be equal to /calculate-your-capital-gains/resident/disposal-value" in {
      val path = controllers.resident.routes.GainController.disposalValue.toString()
      path shouldEqual "/calculate-your-capital-gains/resident/disposal-value"
    }
  }

  "The URL for the submit disposal value Action" should {
    "be equal to /calculate-your-capital-gains/resident/disposal-value" in {
      val path = controllers.resident.routes.GainController.submitDisposalValue.toString()
      path shouldEqual "/calculate-your-capital-gains/resident/disposal-value"
    }
  }

  "The URL for the acquisition value or market value Action" should {
    "be equal to /calculate-your-capital-gains/resident/acquisition-value" in {
      val path = controllers.resident.routes.GainController.acquisitionValue.toString()
      path shouldEqual "/calculate-your-capital-gains/resident/acquisition-value"
    }
  }

  "The URL for the submit acquisition value or market value Action" should {
    "be equal to /calculate-your-capital-gains/resident/acquisition-value" in {
      val path = controllers.resident.routes.GainController.submitAcquisitionValue.toString()
      path shouldEqual "/calculate-your-capital-gains/resident/acquisition-value"
    }
  }

  "The URL for the disposal costs action" should {
    "be equal to /calculate-your-capital-gains/resident/disposal-costs" in {
      val path = controllers.resident.routes.GainController.disposalCosts.toString()
      path shouldEqual "/calculate-your-capital-gains/resident/disposal-costs"
    }
  }

  "The URL for the acquisition costs action" should {
    "be equal to /calculate-your-capital-gains/resident/acquisition-costs" in {
      val path = controllers.resident.routes.GainController.acquisitionCosts.toString()
      path shouldEqual "/calculate-your-capital-gains/resident/acquisition-costs"
    }
  }

  "The URL for the submit acquisition costs action" should {
        "be equal to /calculate-your-capital-gains/resident/acquisition-costs" in {
            val path = controllers.resident.routes.GainController.acquisitionCosts.toString()
            path shouldEqual "/calculate-your-capital-gains/resident/acquisition-costs"
        }
  }

  "The URL for the improvements Action" should {
    s"be equal to /calculate-your-capital-gains/resident/improvements" in {
      val path = controllers.resident.routes.GainController.improvements.toString()
      path shouldEqual "/calculate-your-capital-gains/resident/improvements"
    }
  }

  "The URL for the submit disposal costs action" should {
    "be equal to /calculate-your-capital-gains/resident/disposal-costs" in {
      val path = controllers.resident.routes.GainController.disposalCosts.toString()
      path shouldEqual "/calculate-your-capital-gains/resident/disposal-costs"
    }
  }

  "The URL for the summary action" should {
    "be equal to /calculate-your-capital-gains/resident/summary" in {
      val path = controllers.resident.routes.SummaryController.summary.toString()
      path shouldEqual "/calculate-your-capital-gains/resident/summary"
    }
  }

  "The URL for the reliefs action" should {
    "be equal to /calculate-your-capital-gains/resident/reliefs" in {
      val path = controllers.resident.routes.DeductionsController.reliefs.toString()
      path shouldEqual "/calculate-your-capital-gains/resident/reliefs"
    }
  }

  "The URL for the reliefs input action" should {
    "be equal to /calculate-your-capital-gains/resident/reliefs-value" in {
      val path = controllers.resident.routes.DeductionsController.reliefsValue.toString()
      path shouldEqual "/calculate-your-capital-gains/resident/reliefs-value"
    }
  }

  "The URL for the other properties action" should {
    "be equal to /calculate-your-capital-gains/resident/other-properties" in {
      val path = controllers.resident.routes.DeductionsController.otherProperties.toString()
      path shouldEqual "/calculate-your-capital-gains/resident/other-properties"
    }
  }

  "The URL for the allowable losses value action" should {
    "be equal to /calculate-your-capital-gains/resident/allowable-losses-value" in {
      val path = controllers.resident.routes.DeductionsController.allowableLossesValue.toString()
      path shouldEqual "/calculate-your-capital-gains/resident/allowable-losses-value"
    }
  }

  "The URL for the lossesBroughtForward action" should {
    "be equal to /calculate-your-capital-gains/resident/losses-brought-forward" in {
      val path = controllers.resident.routes.DeductionsController.lossesBroughtForward.toString()
      path shouldEqual "/calculate-your-capital-gains/resident/losses-brought-forward"
    }
  }

  "The URL for the allowableLosses action" should {
    "be equal to /calculate-your-capital-gains/resident/allowable-losses" in {
      val path = controllers.resident.routes.DeductionsController.allowableLosses.toString()
      path shouldEqual "/calculate-your-capital-gains/resident/allowable-losses"
    }
  }
}

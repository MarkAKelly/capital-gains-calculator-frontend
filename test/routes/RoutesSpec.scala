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

  "The URL for the disposal costs action" should {
    "be equal to /calculate-your-capital-gains/resident/disposal-costs" in {
      val path = controllers.resident.routes.GainController.disposalCosts.toString()
      path shouldEqual "/calculate-your-capital-gains/resident/disposal-costs"
    }
  }
}

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

import common.TestModels
import assets.MessageLookup.NonResident.{Improvements => messages}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class PropertyDetailsConstructorSpec extends UnitSpec with WithFakeApplication {

  "Calling propertyDetailsRow" when {
    "using the businessScenarioOneModel (no improvements)" should {
      lazy val model = TestModels.businessScenarioOneModel
      lazy val result = PropertyDetailsConstructor.propertyDetailsRows(model)

      "return a sequence of size 1" in {
        result.size shouldEqual 1
      }

      "return a isClaiming Improvements of 'No'" in {
        result.contains(PropertyDetailsConstructor.improvementsIsClaimingRow(model).get) shouldEqual true
      }
    }

    "using the sumModelTA model (improvements and time apportioned)" should {
      lazy val model = TestModels.sumModelTA
      lazy val result = PropertyDetailsConstructor.propertyDetailsRows(model)

      "return a sequence of size two" in {
        result.size shouldEqual 2
      }

      "return an isClaiming Improvements of 'Yes'" in {
        result.contains(PropertyDetailsConstructor.improvementsIsClaimingRow(model).get) shouldEqual true
      }

      "return a Improvements value row" in {
        result.contains(PropertyDetailsConstructor.improvementsTotalRow(model, true).get) shouldEqual true
      }
    }

    "using the sumModelRebased model (improvements and rebased)" should {
      lazy val model = TestModels.sumModelRebased
      lazy val result = PropertyDetailsConstructor.propertyDetailsRows(model)

      "return a sequence of size two" in {
        result.size shouldEqual 2
      }

      "return an isClaiming Improvements of 'Yes'" in {
        result.contains(PropertyDetailsConstructor.improvementsIsClaimingRow(model).get) shouldEqual true
      }

      "return a Improvements value row" in {
        result.contains(PropertyDetailsConstructor.improvementsAfterRow(model, true).get) shouldEqual true
      }
    }

    "using the summaryIndividualFlatLoss model (improvements and flat)" should {
      lazy val model = TestModels.summaryIndividualFlatLoss
      lazy val result = PropertyDetailsConstructor.propertyDetailsRows(model)

      "return a sequence of size two" in {
        result.size shouldEqual 2
      }

      "return an isClaiming Improvements of 'Yes'" in {
        result.contains(PropertyDetailsConstructor.improvementsIsClaimingRow(model).get) shouldEqual true
      }

      "return a Improvements value row" in {
        result.contains(PropertyDetailsConstructor.improvementsTotalRow(model, true).get) shouldEqual true
      }
    }
  }

  "Calling improvementsIsClaimingRow" when {

    "supplied with a value of Yes" should {
      lazy val model = TestModels.sumModelTA
      lazy val result = PropertyDetailsConstructor.improvementsIsClaimingRow(model).get

      "have an id of nr:improvements-isClaiming" in {
        result.id shouldBe "nr:improvements-isClaiming"
      }

      "have the data for Yes" in {
        result.data shouldBe "Yes"
      }

      "have the question for improvements is claiming" in {
        result.question shouldBe messages.question
      }

      "have a link to the improvements page" in {
        result.link shouldBe Some(controllers.nonresident.routes.ImprovementsController.improvements().url)
      }
    }

    "supplied with a value of No" should {
      lazy val model = TestModels.sumModelFlat
      lazy val result = PropertyDetailsConstructor.improvementsIsClaimingRow(model).get

      "have the data for 10 October 2018" in {
        result.data shouldBe "No"
      }
    }
  }

  "Calling improvementsTotalRow" when {

    "supplied with a value of 500 and should be displayed it true" should {
      lazy val model = TestModels.sumModelTA
      lazy val result = PropertyDetailsConstructor.improvementsTotalRow(model, true).get

      "have an id of nr:improvements-total" in {
        result.id shouldBe "nr:improvements-total"
      }

      "have the data for the value of 500" in {
        result.data shouldBe BigDecimal(500)
      }

      "have the question for improvements values" in {
        result.question shouldBe messages.questionTwo
      }

      "have a link to the improvements page" in {
        result.link shouldBe Some(controllers.nonresident.routes.ImprovementsController.improvements().url)
      }
    }

    "supplied with a value of 500 but should be displayed it false" should {
      lazy val model = TestModels.sumModelTA
      lazy val result = PropertyDetailsConstructor.improvementsTotalRow(model, false)

      "be a None" in {
        result shouldBe None
      }
    }
  }

  "Calling improvementsAfterRow" when {

    "supplied with a value of 1000 and should be displayed it true" should {
      lazy val model = TestModels.summaryIndividualImprovementsWithRebasedModel
      lazy val result = PropertyDetailsConstructor.improvementsAfterRow(model, true).get

      "have an id of nr:improvements-after" in {
        result.id shouldBe "nr:improvements-after"
      }

      "have the data for the value of 1000" in {
        result.data shouldBe BigDecimal(1000)
      }

      "have the question for improvements value" in {
        result.question shouldBe messages.questionFour
      }

      "have a link to the improvements page" in {
        result.link shouldBe Some(controllers.nonresident.routes.ImprovementsController.improvements().url)
      }
    }

    "supplied with a value of 500 but should be displayed it false" should {
      lazy val model = TestModels.summaryIndividualImprovementsWithRebasedModel
      lazy val result = PropertyDetailsConstructor.improvementsAfterRow(model, false)

      "be a None" in {
        result shouldBe None
      }
    }
  }
}

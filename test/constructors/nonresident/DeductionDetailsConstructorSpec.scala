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
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{NonResident => messages}

class DeductionDetailsConstructorSpec extends UnitSpec with WithFakeApplication {

  "Calling privateResidenceReliefRow" when {

    "provided a result with simple PRR" should {
      val calculation = TestModels.calcModelSomePRR
      lazy val result = DeductionDetailsConstructor.privateResidenceReliefRow(calculation)

      "return a Some" in {
        result.isDefined shouldBe true
      }

      "have an id of nr:privateResidenceRelief" in {
        result.get.id shouldBe "nr:privateResidenceRelief"
      }

      "have the data for 10000" in {
        result.get.data shouldBe BigDecimal(10000)
      }

      "have the question for private residence relief" in {
        result.get.question shouldBe messages.PrivateResidenceRelief.question
      }

      "have a link to the private residence relief page" in {
        result.get.link shouldBe Some(controllers.nonresident.routes.PrivateResidenceReliefController.privateResidenceRelief().url)
      }
    }

    "provided a result without simple PRR" should {
      val calculation = TestModels.calcModelOneRate
      lazy val result = DeductionDetailsConstructor.privateResidenceReliefRow(calculation)

      "return a None" in {
        result.isEmpty shouldBe true
      }
    }
  }

  "Calling allowableLossesRow" when {

    "provided with a summary that contains a value for allowable losses" should {
      val model = TestModels.summaryIndividualFlatWithoutAEA
      lazy val result = DeductionDetailsConstructor.allowableLossesRow(model)

      "have an id of nr:allowableLosses" in {
        result.get.id shouldBe "nr:allowableLosses"
      }

      "have the data for 50000" in {
        result.get.data shouldBe BigDecimal(50000)
      }

      "have the question for allowableLosses" in {
        result.get.question shouldBe messages.AllowableLosses.inputQuestion
      }

      "have a link to the allowable losses page" in {
        result.get.link shouldBe Some(controllers.nonresident.routes.AllowableLossesController.allowableLosses().url)
      }
    }

    "provided with a summary that contains no value for allowable losses" should {
      val model = TestModels.sumModelFlat
      lazy val result = DeductionDetailsConstructor.allowableLossesRow(model)

      "have the data for 0" in {
        result.get.data shouldBe BigDecimal(0)
      }
    }
  }
}

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
import assets.MessageLookup.NonResident.{PrivateResidenceRelief => messages}

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
        result.get.question shouldBe messages.question
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
}

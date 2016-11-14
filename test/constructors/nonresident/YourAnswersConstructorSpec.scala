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
import uk.gov.hmrc.play.test.UnitSpec

class YourAnswersConstructorSpec extends UnitSpec {

  "Calling .fetchYourAnswers" when {

    "only fetching total gain answers" should {
      //TODO update to total gain when new model is available
      val model = TestModels.sumModelFlat
      val result = YourAnswersConstructor.fetchYourAnswers(model, TestModels.calcModelOneRate)

      "contain the answers from sale details" in {
        val salesDetails = SalesDetailsConstructor.salesDetailsRows(model)

        result.containsSlice(salesDetails) shouldBe true
      }

      "contain the answers from purchase details" in {
        val purchaseDetails = PurchaseDetailsConstructor.getPurchaseDetailsSection(model)

        result.containsSlice(purchaseDetails) shouldBe true
      }

      "contain the answers from property details" in {
        val propertyDetails = PropertyDetailsConstructor.propertyDetailsRows(model)

        result.containsSlice(propertyDetails) shouldBe true
      }

      "contain the answers from deduction details" in {
        val deductionDetails = DeductionDetailsConstructor.deductionDetailsRows(model, TestModels.calcModelOneRate)

        result.containsSlice(deductionDetails) shouldBe true
      }
    }
  }
}

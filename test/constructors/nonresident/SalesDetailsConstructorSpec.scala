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

import java.time.LocalDate

import common.TestModels
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{NonResident => messages}

class SalesDetailsConstructorSpec extends UnitSpec with WithFakeApplication {

  "Calling disposalDateRow" when {

    "supplied with a date of 10 October 2010" should {
      val model = TestModels.summaryIndividualImprovementsNoRebasedModel
      lazy val result = SalesDetailsConstructor.disposalDateRow(model)

      "have an id of disposalDate" in {
        result.id shouldBe "nr:disposalDate"
      }

      "have the data for 10 October 2010" in {
        result.data shouldBe LocalDate.parse("2010-10-10")
      }

      "have the question for disposal date" in {
        result.question shouldBe "" //TODO
      }

      "have a link to the disposal date page" in {
        result.link shouldBe Some(controllers.nonresident.routes.DisposalDateController.disposalDate().url)
      }
    }

    "supplied with a date of 10 October 2018" should {
      val model = TestModels.summaryIndividualPRRAcqDateAfterAndDisposalDateBefore
      lazy val result = SalesDetailsConstructor.disposalDateRow(model)

      "have the data for 10 October 2018" in {
        result.data shouldBe LocalDate.parse("2018-10-10")
      }
    }
  }
}

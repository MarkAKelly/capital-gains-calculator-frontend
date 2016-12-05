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

import models.nonresident.{CalculationElectionModel, OtherReliefsModel, QuestionAnswerModel}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import common.nonresident.CalculationType
import helpers.AssertHelpers
import assets.MessageLookup.{NonResident => messages}
import common.KeystoreKeys

class OtherReliefsRequestConstructorSpec extends UnitSpec with AssertHelpers with WithFakeApplication {

  private def assertExpectedResult[T](option: Option[T])(test: T => Unit) = assertOption("expected option is None")(option)(test)

  "Calling .getOtherReliefsRebasedRow" when {

    "provided with no model" should {
      val result = OtherReliefsRequestConstructor.getOtherReliefsRebasedRow(None, CalculationElectionModel(CalculationType.rebased))

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with no value for other reliefs" should {
      val result = OtherReliefsRequestConstructor.getOtherReliefsRebasedRow(Some(OtherReliefsModel(0)), CalculationElectionModel(CalculationType.rebased))

      "should return a None" in {
        result shouldBe None
      }
    }

    "provided with a calculation type which is not rebased" should {
      val result = OtherReliefsRequestConstructor.getOtherReliefsRebasedRow(Some(OtherReliefsModel(10)), CalculationElectionModel(CalculationType.flat))

      "return a None" in {
        result shouldBe None
      }
    }

    "provided with a rebased calculation and other reliefs value" should {
      lazy val result = OtherReliefsRequestConstructor.getOtherReliefsRebasedRow(Some(OtherReliefsModel(10)), CalculationElectionModel(CalculationType.rebased))

      "return a QuestionAnswerModel" in {
        result.isDefined shouldBe true
      }

      s"have a question of ${messages.OtherReliefs.question}" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.question shouldBe messages.OtherReliefs.question)
      }

      s"have an id of ${KeystoreKeys.otherReliefsRebased}" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.id shouldBe KeystoreKeys.otherReliefsRebased)
      }

      "have a value of 10" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.data shouldBe 10)
      }

      "have no link" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.link shouldBe None)
      }
    }
  }
}

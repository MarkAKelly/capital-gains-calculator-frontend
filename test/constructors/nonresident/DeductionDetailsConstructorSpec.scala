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

import assets.MessageLookup.{NonResident => messages}
import helpers.AssertHelpers
import models.nonresident._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class DeductionDetailsConstructorSpec extends UnitSpec with WithFakeApplication with AssertHelpers {

  val noneOtherReliefs = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2010),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel("No", None, None, None),
    None,
    None,
    ImprovementsModel("No", None, None),
    None
  )

  val noOtherReliefs = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2010),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel("No", None, None, None),
    None,
    None,
    ImprovementsModel("No", None, None),
    Some(OtherReliefsModel(Some("No"), None))
  )

  val yesOtherReliefs = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2010),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    Some(HowBecameOwnerModel("Gifted")),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel("No", None, None, None),
    None,
    None,
    ImprovementsModel("No", None, None),
    Some(OtherReliefsModel(Some("Yes"), Some(1450)))
  )

  private def assertExpectedResult[T](option: Option[T])(test: T => Unit) = assertOption("expected option is None")(option)(test)

  "Calling .deductionDetailsRows" when {

    "provided with reliefs" should {
      lazy val result = DeductionDetailsConstructor.deductionDetailsRows(yesOtherReliefs)

      "have a sequence of size 2" in {
        result.size shouldBe 2
      }

      "return a sequence with an other reliefs flat question" in {
        result.contains(DeductionDetailsConstructor.otherReliefsFlatQuestionRow(yesOtherReliefs).get)
      }

      "return a sequence with an other reliefs flat value" in {
        result.contains(DeductionDetailsConstructor.otherReliefsFlatQuestionRow(yesOtherReliefs).get)
      }
    }
  }

  "Calling .otherReliefsFlatQuestionRow" when {

    "no other reliefs data is found" should {
      lazy val result = DeductionDetailsConstructor.otherReliefsFlatQuestionRow(noneOtherReliefs)

      "return a None" in {
        result shouldBe None
      }
    }

    "an answer of 'No' to other reliefs is found" should {
      lazy val result = DeductionDetailsConstructor.otherReliefsFlatQuestionRow(noOtherReliefs)

      "return some value" in {
        result.isDefined shouldBe true
      }

      "return an id of nr:otherReliefsFlat-question" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.id shouldBe "nr:otherReliefsFlat-question")
      }

      "return a data value of No" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.data shouldBe "No")
      }

      "return a question for Other Reliefs" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.question shouldBe messages.OtherReliefs.question)
      }

      "return a link for the Other Reliefs page" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.link shouldBe
          Some(controllers.nonresident.routes.OtherReliefsController.otherReliefs().url))
      }
    }

    "an answer of 'Yes' to other reliefs is found" should {
      lazy val result = DeductionDetailsConstructor.otherReliefsFlatQuestionRow(yesOtherReliefs)

      "return some value" in {
        result.isDefined shouldBe true
      }

      "return a data value of Yes" in {
        assertExpectedResult[QuestionAnswerModel[String]](result)(_.data shouldBe "Yes")
      }
    }
  }

  "Calling .otherReliefsFlatValueRow" when {

    "no other reliefs data is found" should {
      lazy val result = DeductionDetailsConstructor.otherReliefsFlatValueRow(noneOtherReliefs)

      "return a None" in {
        result shouldBe None
      }
    }

    "an answer of No to other reliefs is found" should {
      lazy val result = DeductionDetailsConstructor.otherReliefsFlatValueRow(noOtherReliefs)

      "return a None" in {
        result shouldBe None
      }
    }

    "an answer of Yes to other reliefs is found" should {
      lazy val result = DeductionDetailsConstructor.otherReliefsFlatValueRow(yesOtherReliefs)

      "return some value" in {
        result.isDefined shouldBe true
      }

      "return an id of nr:otherReliefsFlat" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.id shouldBe "nr:otherReliefsFlat")
      }

      "return a value of 1450" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.data shouldBe 1450)
      }

      "return a question for Other Reliefs Value" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.question shouldBe messages.OtherReliefs.inputQuestion)
      }

      "return a link to the Other Reliefs page" in {
        assertExpectedResult[QuestionAnswerModel[BigDecimal]](result)(_.link shouldBe
          Some(controllers.nonresident.routes.OtherReliefsController.otherReliefs().url))
      }
    }
  }
}

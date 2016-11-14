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

import assets.MessageLookup.{NonResident => messages}
import models.nonresident._
import common.{KeystoreKeys, TestModels}
import helpers.AssertHelpers
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class PurchaseDetailsConstructorSpec extends UnitSpec with WithFakeApplication with AssertHelpers {

  val totalGainGiven = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2010),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    HowBecameOwnerModel("Gifted"),
    None,
    AcquisitionValueModel(300000),
    AcquisitionCostsModel(2500),
    AcquisitionDateModel("No", None, None, None),
    None,
    None,
    ImprovementsModel("No", None, None)
  )

  val totalGainInherited = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2010),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(150000),
    DisposalCostsModel(600),
    HowBecameOwnerModel("Inherited"),
    None,
    AcquisitionValueModel(300000),
    AcquisitionCostsModel(2500),
    AcquisitionDateModel("No", None, None, None),
    None,
    None,
    ImprovementsModel("No", None, None)
  )

  val totalGainSold = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2018),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(90000),
    DisposalCostsModel(0),
    HowBecameOwnerModel("Bought"),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel("Yes", Some(1), Some(4), Some(2013)),
    Some(RebasedValueModel("Yes", Some(7500))),
    Some(RebasedCostsModel("Yes", Some(150))),
    ImprovementsModel("Yes", Some(50), Some(25))
  )

  val totalGainForLess = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(true)),
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    HowBecameOwnerModel("Bought"),
    Some(BoughtForLessModel(true)),
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel("Yes", Some(1), Some(4), Some(2013)),
    Some(RebasedValueModel("Yes", Some(7500))),
    Some(RebasedCostsModel("Yes", Some(150))),
    ImprovementsModel("Yes", Some(50), Some(25))
  )

  private def assertExpectedResult[T](option: Option[T])(test: T => Unit) = assertOption("expected option is None")(option)(test)

  "Calling purchaseDetailsRow" when {

    "using the totalGainForLess model" should {
      lazy val result = PurchaseDetailsConstructor.getPurchaseDetailsSection(totalGainForLess)

      "will return a Sequence with size 6" in {
        result.size shouldBe 6
      }

      "return a Sequence that will contain an acquisitionDateAnswer data item" in {
        result.contains(PurchaseDetailsConstructor.acquisitionDateAnswerRow(totalGainForLess).get) shouldBe true
      }

      "return a Sequence that will contain an acquisitionDate data item" in {
        result.contains(PurchaseDetailsConstructor.acquisitionDateRow(totalGainForLess).get) shouldBe true
      }

      "return a Sequence that will contain an acquisitionCost data item" in {
        result.contains(PurchaseDetailsConstructor.acquisitionCostsRow(totalGainForLess).get) shouldBe true
      }

      "return a Sequence that will contain an acquisitionValue data item" in {
        result.contains(PurchaseDetailsConstructor.acquisitionValueRow(totalGainForLess).get) shouldBe true
      }

      "return a Sequence that will contain a howBecameOwner data item" in {
        result.contains(PurchaseDetailsConstructor.howBecameOwnerRow(totalGainForLess).get) shouldBe true
      }

      "return a Sequence that will contain a boughtForLess data item" in {
        result.contains(PurchaseDetailsConstructor.boughtForLessRow(totalGainForLess).get) shouldBe true
      }
    }
  }

  "Calling .acquisitionDateAnswerRow" when {

    "no acquisition date is given" should {
      lazy val result = PurchaseDetailsConstructor.acquisitionDateAnswerRow(totalGainGiven).get

      "have an id of nr:acquisitionDate-question" in {
        result.id shouldBe "nr:acquisitionDate-question"
      }

      "have the data for 'No'" in {
        result.data shouldBe "No"
      }

      "have the question for acquisition date" in {
        result.question shouldBe messages.AcquisitionDate.question
      }

      "have a link to the acquisition date page" in {
        result.link shouldBe Some(controllers.nonresident.routes.AcquisitionDateController.acquisitionDate().url)
      }
    }

    "an acquisition date is given" should {
      lazy val result = PurchaseDetailsConstructor.acquisitionDateAnswerRow(totalGainForLess).get

      "have the data for 'Yes'" in {
        result.data shouldBe "Yes"
      }
    }
  }

  "Calling .acquisitionDateRow" when {

    "no acquisition date is given" should {
      lazy val result = PurchaseDetailsConstructor.acquisitionDateRow(totalGainGiven)

      "return a None" in {
        result shouldBe None
      }
    }

    "an acquisition date is given" should {
      lazy val result = PurchaseDetailsConstructor.acquisitionDateRow(totalGainForLess)

      "return Some value" in {
        result.isDefined shouldBe true
      }

      "have an id of nr:acquisitionDate" in {
        assertExpectedResult[QuestionAnswerModel[LocalDate]](result)(_.id shouldBe "nr:acquisitionDate")
      }

      "have the date for 2013-04-01" in {
        assertExpectedResult[QuestionAnswerModel[LocalDate]](result)(_.data shouldBe LocalDate.parse("2013-04-01"))
      }

      "have the question for acquisition date entry" in {
        assertExpectedResult[QuestionAnswerModel[LocalDate]](result)(_.question shouldBe messages.AcquisitionDate.questionTwo)
      }

      "have a link to the acquisition date page" in {
        assertExpectedResult[QuestionAnswerModel[LocalDate]](result)(_.link shouldBe
          Some(controllers.nonresident.routes.AcquisitionDateController.acquisitionDate().url))
      }
    }
  }

  "Calling .howBecameOwnerRow" when {

    "the property was received as a gift" should {
      lazy val result = PurchaseDetailsConstructor.howBecameOwnerRow(totalGainGiven).get

      "have an id of nr:howBecameOwner" in {
        result.id shouldBe "nr:howBecameOwner"
      }

      "have the value of Gifted" in {
        result.data shouldBe messages.HowBecameOwner.gifted
      }

      "have the question for how became owner" in {
        result.question shouldBe messages.HowBecameOwner.question
      }

      "have a link to the how became owner page" in {
        result.link shouldBe Some(controllers.nonresident.routes.HowBecameOwnerController.howBecameOwner().url)
      }
    }

    "the property was bought" should {
      lazy val result = PurchaseDetailsConstructor.howBecameOwnerRow(totalGainSold).get

      "have the value of Bought" in {
        result.data shouldBe messages.HowBecameOwner.bought
      }
    }

    "the property was inherited" should {
      lazy val result = PurchaseDetailsConstructor.howBecameOwnerRow(totalGainInherited).get

      "have the value of Inherited" in {
        result.data shouldBe messages.HowBecameOwner.inherited
      }
    }
  }

  "Calling .boughtForLessRow" when {

    "the property was not bought" should {
      lazy val result = PurchaseDetailsConstructor.boughtForLessRow(totalGainGiven)

      "return a None" in {
        result shouldBe None
      }
    }

    "the property was bought" should {
      lazy val result = PurchaseDetailsConstructor.boughtForLessRow(totalGainSold)

      "return Some value" in {
        result.isDefined shouldBe true
      }

      "have an id of nr:boughtForLess" in {
        assertExpectedResult[QuestionAnswerModel[Boolean]](result)(_.id shouldBe "nr:boughtForLess")
      }

      "have a value of false" in {
        assertExpectedResult[QuestionAnswerModel[Boolean]](result)(_.data shouldBe false)
      }

      "have the question for bought for less" in {
        assertExpectedResult[QuestionAnswerModel[Boolean]](result)(_.question shouldBe messages.BoughtForLess.question)
      }

      "have a link to the bought for less page" in {
        assertExpectedResult[QuestionAnswerModel[Boolean]](result)(_.link
          shouldBe Some(controllers.nonresident.routes.BoughtForLessController.boughtForLess().url))
      }
    }

    "the property was bought for less" should {
      lazy val result = PurchaseDetailsConstructor.boughtForLessRow(totalGainForLess)

      "return Some value" in {
        result.isDefined shouldBe true
      }

      "have a value of true" in {
        assertExpectedResult[QuestionAnswerModel[Boolean]](result)(_.data shouldBe true)
      }
    }
  }

  "Calling .acquisitionValueRow" when {

    "a value of 300000 is given" should {
      lazy val result = PurchaseDetailsConstructor.acquisitionValueRow(totalGainGiven).get

      "have an id of nr:acquisitionValue" in {
        result.id shouldBe "nr:acquisitionValue"
      }

      "have the data for '300000'" in {
        result.data shouldBe 300000
      }

      "have the question for acquisition value" in {
        result.question shouldBe messages.AcquisitionValue.question
      }

      "have a link to the acquisition date page" in {
        result.link shouldBe Some(controllers.nonresident.routes.AcquisitionValueController.acquisitionValue().url)
      }
    }

    "a value of 5000 is given" should {
      lazy val result = PurchaseDetailsConstructor.acquisitionValueRow(totalGainSold).get

      "have the data for '5000'" in {
        result.data shouldBe 5000
      }
    }
  }

  "Calling .acquisitionCostsRow" when {

    "a value of 2500 is given" should {
      lazy val result = PurchaseDetailsConstructor.acquisitionCostsRow(totalGainGiven).get

      "have an id of nr:acquisitionCosts" in {
        result.id shouldBe "nr:acquisitionCosts"
      }

      "have the data for '2500'" in {
        result.data shouldBe 2500
      }

      "have the question for acquisition value" in {
        result.question shouldBe messages.AcquisitionCosts.question
      }

      "have a link to the acquisition date page" in {
        result.link shouldBe Some(controllers.nonresident.routes.AcquisitionCostsController.acquisitionCosts().url)
      }
    }

    "a value of 200 is given" should {
      lazy val result = PurchaseDetailsConstructor.acquisitionCostsRow(totalGainSold).get

      "have the data for '200'" in {
        result.data shouldBe 200
      }
    }
  }
}
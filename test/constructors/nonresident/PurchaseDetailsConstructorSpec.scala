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
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class PurchaseDetailsConstructorSpec extends UnitSpec with WithFakeApplication {

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

  "Calling purchaseDetailsRow" when {

    "using the totalGainForLess model" should {
      lazy val result = PurchaseDetailsConstructor.getPurchaseDetailsSection(totalGainForLess)

      "will return a Sequence with size 3" in {
        result.size shouldBe 3
      }

      "return a Sequence that will contain an acquisitionDateData item" in {
        result.contains(PurchaseDetailsConstructor.getAcquisitionDateAnswer(totalGainForLess).get) shouldBe true
      }

      "return a Sequence that will contain an acquisitionCostData item" in {
        result.contains(PurchaseDetailsConstructor.getAcquisitionCostsAnswer(totalGainForLess).get) shouldBe true
      }

      "return a Sequence that will contain an acquisitionValueData item" in {
        result.contains(PurchaseDetailsConstructor.getAcquisitionValueAnswer(totalGainForLess).get) shouldBe true
      }
    }
  }

  "Calling .getAcquisitionDateAnswer" when {

    "no acquisition date is given" should {
      lazy val result = PurchaseDetailsConstructor.getAcquisitionDateAnswer(totalGainGiven).get

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

    "with an acquisition date given" should {
      lazy val result = PurchaseDetailsConstructor.getAcquisitionDateAnswer(totalGainForLess).get

      "have the data for 'Yes'" in {
        result.data shouldBe "Yes"
      }
    }
  }

  "Calling .getAcquisitionValue" when {

    "a value of 300000 is given" should {
      lazy val result = PurchaseDetailsConstructor.getAcquisitionValueAnswer(totalGainGiven).get

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
      lazy val result = PurchaseDetailsConstructor.getAcquisitionValueAnswer(totalGainSold).get

      "have the data for '5000'" in {
        result.data shouldBe 5000
      }
    }
  }

  "Calling .getAcquisitionCosts" when {

    "a value of 2500 is given" should {
      lazy val result = PurchaseDetailsConstructor.getAcquisitionCostsAnswer(totalGainGiven).get

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
      lazy val result = PurchaseDetailsConstructor.getAcquisitionCostsAnswer(totalGainSold).get

      "have the data for '200'" in {
        result.data shouldBe 200
      }
    }
  }
}
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

import assets.MessageLookup
import common.nonresident.CustomerTypeKeys
import models.nonresident._
import common.KeystoreKeys
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

/**
  * Created by emma on 24/10/16.
  */
class PurchaseDetailsConstructorSpec extends UnitSpec with WithFakeApplication {

  lazy val summaryFlatOptionValuesModel = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    Some(CurrentIncomeModel(30000.0)),
    Some(PersonalAllowanceModel(11000.0)),
    OtherPropertiesModel("Yes", Some(250000.0)),
    Some(AnnualExemptAmountModel(10000.0)),
    AcquisitionDateModel("Yes", Some(4), Some(9), Some(2016)),
    AcquisitionValueModel(300000.0),
    Some(RebasedValueModel("Yes", Some(350000.0))),
    Some(RebasedCostsModel("Yes", Some(4000.0))),
    ImprovementsModel("Yes", Some(2000.0)),
    DisposalDateModel(5, 9, 2016),
    DisposalValueModel(5000),
    AcquisitionCostsModel(250000.0),
    DisposalCostsModel(5000.0),
    AllowableLossesModel("Yes", Some(20000.0)),
    CalculationElectionModel("flat"),
    OtherReliefsModel(Some("Yes"), Some(100.0)),
    OtherReliefsModel(Some("Yes"), Some(100.0)),
    OtherReliefsModel(Some("Yes"), Some(100.0)),
    Some(PrivateResidenceReliefModel("Yes", Some(2500.0), Some(0.0)))
  )

  lazy val summaryRebasedOptionsModel = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    None,
    None,
    OtherPropertiesModel("Yes", None),
    None,
    AcquisitionDateModel("Yes", Some(4), Some(9), Some(2016)),
    AcquisitionValueModel(300000.0),
    Some(RebasedValueModel("Yes", Some(350000.0))),
    Some(RebasedCostsModel("Yes", Some(4000.0))),
    ImprovementsModel("No", None),
    DisposalDateModel(5, 9, 2016),
    DisposalValueModel(5000),
    AcquisitionCostsModel(250000.0),
    DisposalCostsModel(5000.0),
    AllowableLossesModel("No", None),
    CalculationElectionModel("rebased"),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    None
  )

  "Using the summaryFlatOptionsValuesModel" when {

    ".getPurchaseDetailsSection" should {

      val target = PurchaseDetailsConstructor
        .getPurchaseDetailsSection(summaryFlatOptionValuesModel)

      " will return a Sequence[QuestionAnswerModel[Any]] with size 3" in {
        target.size shouldBe 3
      }

      "return a Sequence[QuestionAnswerModel[Any]] that will contain an acquisitionDateData item" in {
        target.contains(PurchaseDetailsConstructor
            .getAcquisitionDateAnswer(summaryFlatOptionValuesModel).get) shouldBe true
      }

      "return a Sequence[QuestionAnswersModel[Any]] that will contain an acquisitionCostData item" in {
        target.contains(PurchaseDetailsConstructor
            .getAcquisitionCostsAnswer(summaryFlatOptionValuesModel).get) shouldBe true
      }

      "return a Sequence[QuestionAnswersModel[Any]] that will contain an acquisitionValueData item" in {
        target.contains(PurchaseDetailsConstructor
            .getAcquisitionDateAnswer(summaryFlatOptionValuesModel).get) shouldBe true
      }

      ".getAcquisitionDateAnswer" should {

        val target = PurchaseDetailsConstructor.getAcquisitionDateAnswer(summaryFlatOptionValuesModel)

        "will return a data item of 4/9/16" in {
          val obtainedDate: LocalDate = target.get.data
          val stringConversion = obtainedDate.getDayOfMonth() + "/" + obtainedDate.getMonthValue() +
            "/" + obtainedDate.getYear
          stringConversion shouldBe "4/9/2016"
        }

        "will return an id of " + s"${KeystoreKeys.acquisitionDate}" in {
          val obtainedKey = target.get.id
          obtainedKey shouldBe KeystoreKeys.acquisitionDate
        }
        "will return a url of " +
          s"${target.get.link.get}" in {
          val correctURL = controllers.nonresident.AcquisitionDateController.acquisitionDate.toString()
          val obtainedURL = target.get.link.get
          obtainedURL shouldBe correctURL
        }

        "will return a message of " + s"${MessageLookup.NonResident.AcquisitionDate.questionTwo}" in {
          val obtainedMessage = target.get.question
          val expectedMessage = MessageLookup.NonResident.AcquisitionDate.questionTwo
          obtainedMessage shouldBe expectedMessage
        }
      }

      ".getAcquisitionValueAnswer" should {
        
        "will return a data item with a value of 300000.0" in {
          val obtainedValue = PurchaseDetailsConstructor
            .getAcquisitionValueAnswer((summaryFlatOptionValuesModel)).get.data
          obtainedValue shouldBe 300000.0
        }

        "will return an id of " + s"${KeystoreKeys.acquisitionValue}" in {
          val obtainedKey = PurchaseDetailsConstructor
            .getAcquisitionValueAnswer(summaryFlatOptionValuesModel).get.id
          obtainedKey shouldBe KeystoreKeys.acquisitionValue
        }

        "will return a url of " +
          s"${
            controllers.nonresident.AcquisitionValueController.acquisitionValue.toString
          }" in {
          val correctUrl = controllers.nonresident.AcquisitionValueController.acquisitionValue.toString
          val obtainedUrl = constructors.nonresident.PurchaseDetailsConstructor
            .getAcquisitionValueAnswer(summaryFlatOptionValuesModel).get.link.get.toString
          obtainedUrl shouldBe correctUrl
        }

        "will return a message of " + s"${MessageLookup.NonResident.AcquisitionValue.question}" in {
          val obtainedMessage = PurchaseDetailsConstructor
            .getAcquisitionValueAnswer(summaryFlatOptionValuesModel).get.question
          val expectedMessage = MessageLookup.NonResident.AcquisitionValue.question
          obtainedMessage shouldBe expectedMessage
        }
      }

      ".getAcquisitionCostsAnswer" should {

        "will return a value of 250000.0" in {
          val obtainedValue = PurchaseDetailsConstructor
            .getAcquisitionCostsAnswer((summaryFlatOptionValuesModel)).get.data
          obtainedValue shouldBe 250000.0
        }

        "will return an id of " + s"${KeystoreKeys.acquisitionCosts}" in {
          val obtainedKey = PurchaseDetailsConstructor
            .getAcquisitionCostsAnswer(summaryFlatOptionValuesModel).get.id
          obtainedKey shouldBe KeystoreKeys.acquisitionCosts
        }

        "will return a url of " +
          s"${
            constructors.nonresident.PurchaseDetailsConstructor
              .getAcquisitionValueAnswer(summaryFlatOptionValuesModel).toString
          }" in {
          val obtainedUrl = constructors.nonresident.PurchaseDetailsConstructor
            .getAcquisitionCostsAnswer(summaryFlatOptionValuesModel).get.link.get.toString
          val correctUrl = controllers.nonresident.AcquisitionCostsController.acquisitionCosts.toString()
          obtainedUrl shouldBe correctUrl
        }

        "will return a message of " + s"${MessageLookup.NonResident.AcquisitionCosts.question}" in {
          val obtainedMessage = PurchaseDetailsConstructor
            .getAcquisitionCostsAnswer(summaryFlatOptionValuesModel).get.question
          val expectedMessage = MessageLookup.NonResident.AcquisitionCosts.question
          obtainedMessage shouldBe expectedMessage
        }
      }

      "The rebased functions" should {

        ".getRebasedValueAnswer will return a value of None" in {
          val obtainedValue = PurchaseDetailsConstructor
            .getRebasedValueAnswer((summaryFlatOptionValuesModel))
          obtainedValue shouldBe None
        }

        ".getRebasedCostsAnswer will return a value of None" in {
          val obtainedValue = PurchaseDetailsConstructor
            .getRebasedCostsAnswer((summaryFlatOptionValuesModel))
          obtainedValue shouldBe None
        }
      }
    }

    "using the summaryRebasedOptionsModel" should {

      ".getPurchaseDetailsSection" should {
        "return a Sequence[QuestionAnswerModel[Any]] with size 3.0" in {
          PurchaseDetailsConstructor
            .getPurchaseDetailsSection(summaryRebasedOptionsModel).size shouldBe 3
        }
      }

      ".rebasedValueAnswer" should {

        "return a data item value of Some(35000)" in {
          val obtainedData = PurchaseDetailsConstructor
            .getRebasedValueAnswer(summaryRebasedOptionsModel).get.data
          obtainedData shouldBe Some(350000)
        }

        "return an id of " + s"${KeystoreKeys.rebasedValue}" in {
          val obtainedKey = PurchaseDetailsConstructor
            .getRebasedValueAnswer(summaryRebasedOptionsModel).get.id
          obtainedKey shouldBe KeystoreKeys.rebasedValue
        }

        "return a URL of " + s"${}" in {
          val obtainedUrl = PurchaseDetailsConstructor.getRebasedValueAnswer(summaryRebasedOptionsModel)
            .get.link.get.toString
          val correctUrl = controllers.nonresident.RebasedValueController.rebasedValue.toString()
          obtainedUrl shouldBe correctUrl
        }

        "will return a message of " in {
          val obtainedMessage = PurchaseDetailsConstructor.getRebasedValueAnswer(summaryRebasedOptionsModel)
            .get.question
          val expectedMessage = MessageLookup.NonResident.RebasedValue.inputQuestion
          obtainedMessage shouldBe expectedMessage
        }
      }

      ".rebasedCostsAnswers" should {

        "return data of Some(4000)" in {
          val obtainedData = PurchaseDetailsConstructor
            .getRebasedCostsAnswer(summaryRebasedOptionsModel).get.data
          obtainedData shouldBe Some(4000)
        }

        "return an id of " + s"${KeystoreKeys.rebasedCosts}" in {
          val obtainedKey = PurchaseDetailsConstructor
            .getRebasedCostsAnswer(summaryRebasedOptionsModel).get.id
          obtainedKey shouldBe KeystoreKeys.rebasedCosts
        }

        "return a URL of " +
          s"${
            constructors.nonresident.PurchaseDetailsConstructor
              .getRebasedCostsAnswer(summaryRebasedOptionsModel).toString
          }" in {
          val obtainedUrl = constructors.nonresident.PurchaseDetailsConstructor
            .getRebasedCostsAnswer(summaryRebasedOptionsModel).get.link.get.toString
          val correctUrl = controllers.nonresident.RebasedCostsController.rebasedCosts.toString()
          obtainedUrl shouldBe correctUrl
        }

        "return a message of " in {
          val obtainedMessage = PurchaseDetailsConstructor.getRebasedCostsAnswer(summaryRebasedOptionsModel)
            .get.question
          val expectedMessage = MessageLookup.NonResident.RebasedCosts.inputQuestion
          obtainedMessage shouldBe expectedMessage
        }
      }

      "the flat election methods" should {
        ".getAcquisitionCostsAnswer will return a value of None" in {
          val obtainedValue = PurchaseDetailsConstructor
            .getAcquisitionCostsAnswer((summaryRebasedOptionsModel))
          obtainedValue shouldBe None
        }

        ".getAcquisitionValueAnswer will return a value of None" in {
          val obtainedValue = PurchaseDetailsConstructor
            .getAcquisitionValueAnswer((summaryRebasedOptionsModel))
          obtainedValue shouldBe None
        }


      }
    }
  }
}
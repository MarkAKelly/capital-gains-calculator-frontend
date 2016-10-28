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
import controllers.helpers.FakeRequestHelper
import play.api.test.FakeApplication
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

      lazy val target = PurchaseDetailsConstructor
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
            .getAcquisitionValueAnswer(summaryFlatOptionValuesModel).get) shouldBe true
      }

      ".getAcquisitionDateAnswer" should {

        lazy val target = PurchaseDetailsConstructor.getAcquisitionDateAnswer(summaryFlatOptionValuesModel)

        "will return a data item of 4/9/16" in {
          lazy val obtainedDate: LocalDate = target.get.data
          /*lazy val stringConversion = obtainedDate.getDayOfMonth() + "/" + obtainedDate.getMonthValue() +
            "/" + obtainedDate.getYear*/
          obtainedDate shouldBe LocalDate.parse("2016-09-04")
        }

        "will return an id of " + s"${KeystoreKeys.acquisitionDate}" in {
          lazy val obtainedKey = target.get.id
          obtainedKey shouldBe KeystoreKeys.acquisitionDate
        }
        "will return a url of " in {
          lazy val correctURL = controllers.nonresident.routes.AcquisitionDateController.acquisitionDate().url
          lazy val obtainedURL = target.get.link.get

          obtainedURL shouldBe correctURL
        }

        s"will return a message of ${MessageLookup.NonResident.AcquisitionDate.questionTwo}" in {
          target.get.question shouldBe MessageLookup.NonResident.AcquisitionDate.questionTwo
        }
      }

      ".getAcquisitionValueAnswer" should {

        lazy val target = PurchaseDetailsConstructor
          .getAcquisitionValueAnswer(summaryFlatOptionValuesModel).get

        "will return a data item with a lazy value of 300000.0" in {
          lazy val obtainedValue = target.data
          obtainedValue shouldBe 300000.0
        }

        "will return an id of " + s"${KeystoreKeys.acquisitionValue}" in {
          lazy val obtainedKey = target.id
          obtainedKey shouldBe KeystoreKeys.acquisitionValue
        }

        "will return a url of " +
          s"${
            controllers.nonresident.routes.AcquisitionValueController.acquisitionValue.toString
          }" in {
          lazy val correctUrl = controllers.nonresident.routes.AcquisitionValueController.acquisitionValue.url
          lazy val obtainedUrl = target.link.get.toString
          obtainedUrl shouldBe correctUrl
        }

        s"will return a message of ${MessageLookup.NonResident.AcquisitionValue.question}"in {
            target.question shouldBe MessageLookup.NonResident.AcquisitionValue.question
        }
      }

      ".getAcquisitionCostsAnswer" should {

        lazy val target = PurchaseDetailsConstructor
          .getAcquisitionCostsAnswer((summaryFlatOptionValuesModel))

        "will return a lazy value of 250000.0" in {
          lazy val obtainedValue = target.get.data
          obtainedValue shouldBe 250000.0
        }

        "will return an id of " + s"${KeystoreKeys.acquisitionCosts}" in {
          lazy val obtainedKey = target.get.id
          obtainedKey shouldBe KeystoreKeys.acquisitionCosts
        }

        "will return a url of " +
          s"${
            controllers.nonresident.routes.AcquisitionCostsController.acquisitionCosts.url
          }" in {
          lazy val obtainedUrl = target.get.link.get
          lazy val correctUrl = controllers.nonresident.routes.AcquisitionCostsController.acquisitionCosts.url
          obtainedUrl shouldBe correctUrl
        }

        "will return a message of "  in {
          lazy val obtainedMessage = target.get.question
          lazy val expectedMessage = MessageLookup.NonResident.AcquisitionCosts.question
          print(obtainedMessage + " : " + expectedMessage)
          obtainedMessage shouldBe expectedMessage
        }
      }

      "The rebased functions" should {

        ".getRebasedValueAnswer will return a lazy value of None" in {
          lazy val obtainedValue = PurchaseDetailsConstructor
            .getRebasedValueAnswer((summaryFlatOptionValuesModel))
          obtainedValue shouldBe None
        }

        ".getRebasedCostsAnswer will return a lazy value of None" in {
          lazy val obtainedValue = PurchaseDetailsConstructor
            .getRebasedCostsAnswer((summaryFlatOptionValuesModel))
          obtainedValue shouldBe None
        }
      }
    }

    "using the summaryRebasedOptionsModel" should {

      ".getPurchaseDetailsSection" should {

        lazy val target = PurchaseDetailsConstructor
          .getPurchaseDetailsSection(summaryRebasedOptionsModel)

        "return a Sequence[QuestionAnswerModel[Any]] with size 3.0" in {
          target.size shouldBe 3
        }

        "return a Sequence[QuestionAnswerModel[Any]] that will contain an acquisitionDate item" in {
          target.contains(PurchaseDetailsConstructor
            .getAcquisitionDateAnswer(summaryRebasedOptionsModel).get) shouldBe true
        }

        "return a Sequence[QuestionAnswerModel[Any]] that will contain a rebasedCosts item" in {
          target.contains(PurchaseDetailsConstructor
            .getRebasedCostsAnswer(summaryRebasedOptionsModel).get) shouldBe true
        }

        "return a Sequence[QuestionAnswerModel[Any]] that will contain a rebasedValue item" in {
          target.contains(PurchaseDetailsConstructor
            .getRebasedValueAnswer(summaryRebasedOptionsModel).get) shouldBe true
        }
      }

      ".rebasedValueAnswer" should {

        lazy val target = PurchaseDetailsConstructor.getRebasedValueAnswer(summaryRebasedOptionsModel)

        "return a data item lazy value of Some(35000)" in {
          lazy val obtainedData = target.get.data
          obtainedData shouldBe Some(350000)
        }

        "return an id of " + s"${KeystoreKeys.rebasedValue}" in {
          lazy val obtainedKey = target.get.id
          obtainedKey shouldBe KeystoreKeys.rebasedValue
        }

        "return a URL of " + s"${}" in {
          lazy val obtainedUrl = target.get.link.get.toString
          lazy val correctUrl = controllers.nonresident.routes.RebasedValueController.rebasedValue.url
          obtainedUrl shouldBe correctUrl
        }

        "will return a message of " in {
          lazy val obtainedMessage = target.get.question
          lazy val expectedMessage = MessageLookup.NonResident.RebasedValue.inputQuestion
          obtainedMessage shouldBe expectedMessage
        }
      }

      ".rebasedCostsAnswers" should {

        lazy val target =  PurchaseDetailsConstructor
          .getRebasedCostsAnswer(summaryRebasedOptionsModel)

        "return data of Some(4000)" in {
          lazy val obtainedData = target.get.data
          obtainedData shouldBe Some(4000)
        }

        "return an id of " + s"${KeystoreKeys.rebasedCosts}" in {
          lazy val obtainedKey = target.get.id
          obtainedKey shouldBe KeystoreKeys.rebasedCosts
        }

        "return a URL of " +
          s"${
            constructors.nonresident.PurchaseDetailsConstructor
              .getRebasedCostsAnswer(summaryRebasedOptionsModel).toString
          }" in {
          lazy val obtainedUrl = target.get.link.get.toString
          lazy val correctUrl = controllers.nonresident.routes.RebasedCostsController.rebasedCosts.url
          obtainedUrl shouldBe correctUrl
        }

        s"return a message of ${MessageLookup.NonResident.RebasedCosts.inputQuestion}" in {
          lazy val obtainedMessage = target.get.question
          lazy val expectedMessage = MessageLookup.NonResident.RebasedCosts.inputQuestion
          obtainedMessage shouldBe expectedMessage
        }
      }

      "the flat election methods" should {
        ".getAcquisitionCostsAnswer will return a lazy value of None" in {
          lazy val obtainedValue = PurchaseDetailsConstructor
            .getAcquisitionCostsAnswer((summaryRebasedOptionsModel))
          obtainedValue shouldBe None
        }

        ".getAcquisitionValueAnswer will return a lazy value of None" in {
          lazy val obtainedValue = PurchaseDetailsConstructor
            .getAcquisitionValueAnswer((summaryRebasedOptionsModel))
          obtainedValue shouldBe None
        }


      }
    }
  }
}
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

import java.text.DateFormat
import java.time.{LocalDate, LocalTime}

import common.nonresident.CustomerTypeKeys
import models.nonresident._
import common.KeystoreKeys
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import constructors.nonresident.PurchaseDetailsConstructor

/**
  * Created by emma on 24/10/16.
  */
object PurchaseDetailsConstructorSpec extends UnitSpec with WithFakeApplication {

  val summaryWithAllOptionValuesModel = SummaryModel(
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

  val summaryNoOptionsModel = SummaryModel(
    CustomerTypeModel(CustomerTypeKeys.individual),
    None,
    None,
    None,
    OtherPropertiesModel("Yes", None),
    None,
    AcquisitionDateModel("No", None, None, None),
    AcquisitionValueModel(300000.0),
    None,
    None,
    ImprovementsModel("No", None),
    DisposalDateModel(5, 9, 2016),
    DisposalValueModel(5000),
    AcquisitionCostsModel(250000.0),
    DisposalCostsModel(5000.0),
    AllowableLossesModel("No", None),
    CalculationElectionModel("flat"),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    OtherReliefsModel(None, None),
    None
  )

  "Calling the PurchaseDetailsConstructor" when {

    "using the summaryWithAllOptionsValuesModel" should {
      ".getPurchaseDetailsSection will return a Sequence[QuestionAnswerModel[Any]] with size 5" in {
        PurchaseDetailsConstructor
          .getPurchaseDetailsSection(summaryWithAllOptionValuesModel).size shouldBe 5
      }

      ".getPurchaseDetailsSection will return a Sequence[QuestionAnswerModel[Any]] that will contain an acquisitionDateData item" in {
        PurchaseDetailsConstructor
          .getPurchaseDetailsSection(summaryWithAllOptionValuesModel)
          .count(_.equals(PurchaseDetailsConstructor
            .getAcquisitionDateAnswer(summaryWithAllOptionValuesModel))) shouldBe 1
      }

      ".getPurchaseDetailsSection will return a Sequence[QuestionAnswersModel[Any]] that will contain an acquisitionCostData item" in {
        PurchaseDetailsConstructor
          .getPurchaseDetailsSection(summaryWithAllOptionValuesModel)
          .count(_.equals(PurchaseDetailsConstructor
            .getAcquisitionCostsAnswer(summaryWithAllOptionValuesModel))) shouldBe 1
      }

      ".getPurchaseDetailsSection will return a Sequence[QuestionAnswersModel[Any]] that will contain an acquisitionValueData item" in {
        PurchaseDetailsConstructor
          .getPurchaseDetailsSection(summaryWithAllOptionValuesModel)
          .count(_.equals(PurchaseDetailsConstructor
            .getAcquisitionDateAnswer(summaryWithAllOptionValuesModel))) shouldBe 1
      }

      ".getPurchaseDetailsSection will return a Sequence[QuestionAnswersModel[Any]] that will contain a rebasedValueData item" in {
        PurchaseDetailsConstructor
          .getPurchaseDetailsSection(summaryWithAllOptionValuesModel)
          .count(_.equals(PurchaseDetailsConstructor
            .getRebasedValueAnswer(summaryWithAllOptionValuesModel))) shouldBe 1
      }

      ".getPurchaseDetailsSection will return a Sequence[QuestionAnswersModel[Any]] that will contain a rebasedCostsData item" in {
        PurchaseDetailsConstructor
          .getPurchaseDetailsSection(summaryWithAllOptionValuesModel)
          .count(_.equals(PurchaseDetailsConstructor
            .getRebasedCostsAnswer(summaryWithAllOptionValuesModel))) shouldBe 1
      }

      ".getAcquisitionDateAnswer will return a date of 4/9/16" in {
        val obtainedDate: LocalDate = PurchaseDetailsConstructor.getAcquisitionDateAnswer(summaryWithAllOptionValuesModel).get.data
        val stringConversion = obtainedDate.getDayOfMonth + "/" + obtainedDate.getMonth +
          "/" + obtainedDate.getYear
        PurchaseDetailsConstructor.getAcquisitionDateAnswer(summaryWithAllOptionValuesModel) shouldBe stringConversion
      }

      ".getAcquisitionValueAnswer will return  a value of 300000.0" in {
        val obtainedValue = PurchaseDetailsConstructor
          .getAcquisitionValueAnswer((summaryWithAllOptionValuesModel)).get.data
        obtainedValue shouldBe 3000.0
      }

      ".getAcquisitionCostsAnswer will return a value of 25000.0" in {
        val obtainedValue = PurchaseDetailsConstructor
          .getAcquisitionCostsAnswer((summaryWithAllOptionValuesModel)).get.data
        obtainedValue shouldBe 25000.0
      }

      ".getRebasedValueAnswer will return a value of 350000.0" in {
        val obtainedValue = PurchaseDetailsConstructor
          .getRebasedValueAnswer((summaryWithAllOptionValuesModel)).get.data
        obtainedValue shouldBe 35000.0
      }

      ".getRebasedCostsAnswer will return a value of 4000.0" in {
        val obtainedValue = PurchaseDetailsConstructor
          .getRebasedCostsAnswer((summaryWithAllOptionValuesModel)).get.data
        obtainedValue shouldBe 4000.0
      }

      ".getAcquisitionDateAnswer will return a value of 4/9/16" in {
        val obtainedDate = PurchaseDetailsConstructor
          .getAcquisitionDateAnswer(summaryWithAllOptionValuesModel).get.data
        val stringConversion = obtainedDate.getDayOfMonth + "/" + obtainedDate.getMonth +
          "/" + obtainedDate.getYear

        stringConversion shouldBe "4/9/16"
      }

      ".getAcquisitionDateAnswer will return an id of " + s"${KeystoreKeys.acquisitionDate}" in {
        val obtainedKey = PurchaseDetailsConstructor
          .getAcquisitionDateAnswer(summaryWithAllOptionValuesModel).get.id
        obtainedKey shouldBe KeystoreKeys.acquisitionDate
      }

      ".getAcquisitionDateAnswer will return a url of " +
        s"${constructors.nonresident.PurchaseDetailsConstructor
          .getAcquisitionDateAnswer(summaryWithAllOptionValuesModel).toString}" in {
        val obtainedUrl = constructors.nonresident.PurchaseDetailsConstructor
          .getAcquisitionDateAnswer(summaryWithAllOptionValuesModel).toString
        obtainedUrl shouldBe constructors.nonresident.PurchaseDetailsConstructor
          .getAcquisitionDateAnswer(summaryWithAllOptionValuesModel).toString
      }

      ".getAcquisitionValueAnswer will return a value of 30000.0" in {
        val obtainedValue = PurchaseDetailsConstructor
          .getAcquisitionValueAnswer(summaryWithAllOptionValuesModel).get.data
        obtainedValue shouldBe 30000.0
      }

      ".getAcquisitionValue will return an id of " + s"${KeystoreKeys.acquisitionValue}" in {
        val obtainedKey = PurchaseDetailsConstructor
          .getAcquisitionValueAnswer(summaryWithAllOptionValuesModel).get.id
        obtainedKey shouldBe KeystoreKeys.acquisitionValue
      }

      ".getAcquisitionValueAnswer will return a url of " +
        s"${constructors.nonresident.PurchaseDetailsConstructor
          .getAcquisitionValueAnswer(summaryWithAllOptionValuesModel).toString}" in {
        val obtainedUrl = constructors.nonresident.PurchaseDetailsConstructor
          .getAcquisitionValueAnswer(summaryWithAllOptionValuesModel).toString
        obtainedUrl shouldBe constructors.nonresident.PurchaseDetailsConstructor
          .getAcquisitionValueAnswer(summaryWithAllOptionValuesModel).toString
      }


      ".getAcquisitionCostsAnswer will return a value of 250000.0" in {
        val obtainedValue = PurchaseDetailsConstructor
          .getAcquisitionCostsAnswer(summaryWithAllOptionValuesModel).get.data
        obtainedValue shouldBe 250000.0
      }

      ".getAcquisitionCostsAnswer will return an id of " + s"${KeystoreKeys.acquisitionCosts}" in {
        val obtainedKey = PurchaseDetailsConstructor
          .getAcquisitionCostsAnswer(summaryWithAllOptionValuesModel).get.id
        obtainedKey shouldBe KeystoreKeys.acquisitionCosts
      }

      ".getAcquisitionCostsAnswer will return a url of " +
        s"${constructors.nonresident.PurchaseDetailsConstructor
          .getAcquisitionValueAnswer(summaryWithAllOptionValuesModel).toString}" in {
        val obtainedUrl = constructors.nonresident.PurchaseDetailsConstructor
          .getAcquisitionCostsAnswer(summaryWithAllOptionValuesModel).toString
        obtainedUrl shouldBe constructors.nonresident.PurchaseDetailsConstructor
          .getAcquisitionCostsAnswer(summaryWithAllOptionValuesModel).toString
      }

      ".getRebasedValue will return a value of 350000.0" in {
        val obtainedValue = PurchaseDetailsConstructor
          .getRebasedValueAnswer(summaryWithAllOptionValuesModel).get.data
        obtainedValue shouldBe 350000.0
      }

      ".getRebasedValue will return an id of " + s"${KeystoreKeys.rebasedValue}" in {
        val obtainedKey = PurchaseDetailsConstructor
          .getRebasedValueAnswer(summaryWithAllOptionValuesModel).get.id
        obtainedKey shouldBe KeystoreKeys.rebasedValue
      }

      ".getRebasedValueAnswer will return a url of " +
        s"${constructors.nonresident.PurchaseDetailsConstructor
          .getAcquisitionValueAnswer(summaryWithAllOptionValuesModel).toString}" in {
        val obtainedUrl = constructors.nonresident.PurchaseDetailsConstructor
          .getRebasedValueAnswer(summaryWithAllOptionValuesModel).toString
        obtainedUrl shouldBe constructors.nonresident.PurchaseDetailsConstructor
          .getRebasedValueAnswer(summaryWithAllOptionValuesModel).toString
      }

      ".getRebasedCostsAnswer will return a value of 250000.0" in {
        val obtainedValue = PurchaseDetailsConstructor
          .getRebasedCostsAnswer(summaryWithAllOptionValuesModel).get.data
        obtainedValue shouldBe 250000.0
      }

      ".getRebasedCostsAnswer will return an id of " + s"${KeystoreKeys.rebasedCosts}" in {
        val obtainedKey = PurchaseDetailsConstructor
          .getRebasedValueAnswer(summaryWithAllOptionValuesModel).get.id
        obtainedKey shouldBe KeystoreKeys.rebasedCosts
      }

      ".getRebasedCostsAnswer will return a url of " +
        s"${constructors.nonresident.PurchaseDetailsConstructor
          .getRebasedCostsAnswer(summaryWithAllOptionValuesModel).toString}" in {
        val obtainedUrl = constructors.nonresident.PurchaseDetailsConstructor
          .getRebasedCostsAnswer(summaryWithAllOptionValuesModel).toString
        obtainedUrl shouldBe constructors.nonresident.PurchaseDetailsConstructor
          .getRebasedCostsAnswer(summaryWithAllOptionValuesModel).toString
      }
    }

    "using the summaryNoOptionsModel" should {
      ".getRebasedValueAnswer will return data of None" in {
        val obtainedData = PurchaseDetailsConstructor
          .getRebasedValueAnswer(summaryNoOptionsModel)
        obtainedData shouldBe None
      }

      ".getRebasedCostsAnswer will return data of None" in {
        val obtainedData = PurchaseDetailsConstructor
          .getRebasedCostsAnswer(summaryNoOptionsModel)
        obtainedData shouldBe None
      }
    }

  }
}

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

package views.nonResident.report

import assets.MessageLookup.{summaryPage => messages}
import common.TestModels._
import controllers.helpers.FakeRequestHelper
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import views.html.calculation.nonresident.summaryReport
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class SummaryReportViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "In CalculationController calling the .summary action" when {

    "individual is chosen with a flat calculation" when {

      "return some HTML that" should {

        lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

        lazy val view = summaryReport(workingModel, calcModelTwoRates, taxYear,
          sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
        lazy val document = Jsoup.parse(view.body)

        "have the correct sub-heading 'You owe'" in {
          document.select("h1 span").text shouldEqual Messages("calc.summary.secondaryHeading")
        }

        "have a result amount currently set to £8,000.00" in {
          document.select("h1 b").text shouldEqual "£8,000.00"
        }

        "have a 'Calculation details' section that" should {

          "include the section heading 'Calculation details" in {
            document.select("#calcDetails").text should include(Messages("calc.summary.calculation.details.title"))
          }

          "include 'What would you like to base your tax on?'" in {
            document.select("#calcDetails").text should include(Messages("calc.summary.calculation.details.calculationElection"))
          }

          "have an election description of 'How much of your total gain you've made since 5 April 2015'" in {
            document.body().getElementById("calculationDetails(0)").text() shouldBe Messages("calc.summary.calculation.details.rebasedCalculation")
          }

          "include 'Your total gain'" in {
            document.select("#calcDetails").text should include(Messages("calc.summary.calculation.details.totalGain"))
          }

          "have a total gain equal to £40,000.00" in {
            document.body().getElementById("calculationDetails(1)").text() shouldBe "£40,000"
          }

          "include 'Capital Gains Tax allowance used" in {
            document.select("#calcDetails").text should include(Messages("calc.summary.calculation.details.usedAEA"))
          }

          "have a used AEA value equal to £0" in {
            document.body().getElementById("calculationDetails(2)").text() shouldBe "£0"
          }

          "include 'Your taxable gain'" in {
            document.select("#calcDetails").text should include(Messages("calc.summary.calculation.details.taxableGain"))
          }

          "have a taxable gain equal to £40,000" in {
            document.body().getElementById("calculationDetails(3)").text() shouldBe "£40,000"
          }

          "include 'Your tax rate'" in {
            document.select("#calcDetails").text should include(Messages("calc.summary.calculation.details.taxRate"))
          }

          "have a combined tax rate of £32,000 and £8,000" in {
            document.body().getElementById("calculationDetails(4)").text() shouldBe "£32,000 at 18% £8,000 at 28%"
          }

        }

        "have a 'Personal details' section that" should {

          "include the section heading 'Personal details" in {
            document.select("#personalDetails").text should include(Messages("calc.summary.personal.details.title"))
          }

          "include the question 'Who owned the property?'" in {
            document.select("#personalDetails").text should include(Messages("calc.customerType.question"))
          }

          "have an 'individual' owner and link to the customer-type page" in {
            document.body().getElementById("personalDetails(0)").text() shouldBe "Individual"
          }

          "include the question 'What’s your total income for this tax year?'" in {
            document.select("#personalDetails").text should include(Messages("calc.currentIncome.question"))
          }

          "have an total income of £0 and link to the current-income screen" in {
            document.body().getElementById("personalDetails(1)").text() shouldBe "£0.00"
          }
        }

        "have a 'Purchase details' section that" should {

          "include the section heading 'Purchase details" in {
            document.select("#acquisitionDetails").text should include(Messages("calc.summary.purchase.details.title"))
          }

          "include the question for whether the acquisition date is provided" in {
            document.select("#acquisitionDetails").text should include(Messages("calc.acquisitionDate.questionTwo"))
          }

          "have an answer to the question for providing an acquisition date of 'Yes and 16/10/2005'" in {
            document.body().getElementById("purchaseDetails(0)").text() shouldBe Messages("16 October 2005")

          }

          "include the question 'How much did you pay for the property?'" in {
            document.select("#acquisitionDetails").text should include(Messages("calc.rebasedValue.questionTwo"))
          }

          "have an acquisition value of £100,000 and link to the acquisition value page" in {
            document.body().getElementById("purchaseDetails(1)").text() shouldBe "£1,000,000.00"
          }

          "have a acquisition costs of £0 and link to the acquisition-costs page" in {
            document.body().getElementById("purchaseDetails(2)").text() shouldBe "£0.00"
          }
        }

        "have a 'Property details' section that" should {

          "include the section heading 'Property details" in {
            document.select("#propertyDetails").text should include(Messages("calc.summary.property.details.title"))
          }

          "include the question 'Did you make any improvements to the property?'" in {
            document.select("#propertyDetails").text should include(Messages("calc.improvements.question"))
          }

          "the answer to the improvements question should be No and should link to the improvements page" in {
            document.body.getElementById("propertyDetails(0)").text shouldBe "No"
          }
        }

        "have a 'Sale details' section that" should {

          "include the section heading 'Sale details" in {
            document.select("#saleDetails").text should include(Messages("calc.summary.sale.details.title"))
          }

          "include the question 'When did you sign the contract that made someone else the owner?'" in {
            document.select("#saleDetails").text should include(Messages("calc.disposalDate.question"))
          }

          "the date of disposal should be '2 January 2017'" in {
            document.body().getElementById("saleDetails(0)").text shouldBe "2 January 2017"
          }

          "include the question 'How much did you sell or give away the property for?'" in {
            document.select("#saleDetails").text should include(Messages("calc.disposalValue.question"))
          }

          "the value of the sale should be £1,250,000" in {
            document.body().getElementById("saleDetails(1)").text shouldBe "£1,250,000.00"
          }

          "include the question 'How much did you pay in costs when you stopped being the property owner?'" in {
            document.select("#saleDetails").text should include(Messages("calc.disposalCosts.question"))
          }

          "the value of the costs should be £15,000.00" in {
            document.body().getElementById("saleDetails(2)").text shouldBe "£15,000.00"
          }
        }

        "have a 'Deductions details' section that" should {

          "include the section heading 'Deductions" in {
            document.select("#deductions").text should include(Messages("calc.summary.deductions.title"))
          }

          "include the question 'Whats the total value of your allowable losses?'" in {
            document.select("#deductions").text should include(Messages("calc.allowableLosses.question.two"))
          }

          "the value of allowable losses should be £0" in {
            document.body().getElementById("deductions(1)").text shouldBe "£0.00"
          }

          "include the question 'How much extra tax relief are you claiming?'" in {
            document.select("#deductions").text should include(Messages("calc.otherReliefs.question"))
          }

          "the answer to question should be £0.00" in {
            document.body().getElementById("deductions(2)").text shouldBe "£0.00"
          }

          "the PRR claimed question's answer should be '£0.00'" in {
            document.body().getElementById("deductions(0)").text shouldBe "£0.00"
          }

        }
      }

      "the user has provided no value for the AEA and elected Flat calc" should {

        lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

        lazy val view = summaryReport(summaryIndividualFlatWithoutAEA, calcModelOneRate, taxYear,
          sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
        lazy val document = Jsoup.parse(view.body)

        "have the answer for Previous Disposals (Other Properties) of 'No'" in {
          document.body().getElementById("personalDetails(3)").text() shouldBe "No"
        }

        "the answer to the improvements question should be Yes" in {
          document.body.getElementById("propertyDetails(0)").text shouldBe "Yes"
        }

        "the value of the improvements should be £8,000" in {
          document.body.getElementById("propertyDetails(1)").text shouldBe "£8,000.00"
        }

        "the value of the disposal costs should be £600" in {
          document.body().getElementById("saleDetails(2)").text shouldBe "£600.00"
        }

        "include the question for whether the acquisition date is provided" in {
          document.select("#purchaseDetails").text should include(Messages("calc.acquisitionDate.question"))
        }

        "have an answer to the question for providing an acquisition date of 'No'" in {
          document.body().getElementById("purchaseDetails(0)").text() shouldBe Messages("No")
        }

        "have a acquisition costs of £300" in {
          document.body().getElementById("purchaseDetails(2)").text() shouldBe "£300.00"
        }

        "the value of allowable losses should be £50,000" in {
          document.body().getElementById("deductions(1)").text shouldBe "£50,000.00"
        }

        "the value of other reliefs should be £999" in {
          document.body().getElementById("deductions(2)").text shouldBe "£999.00"
        }

        "have a base tax rate of 20%" in {
          document.body().getElementById("calculationDetails(4)").text() shouldBe "20%"
        }
      }

      "the user has £0 current income, with no other properties" should {
        lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

        lazy val view = summaryReport(summaryIndividualFlatNoIncomeOtherPropNo, calcModelOneRate, taxYear,
          sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
        lazy val document = Jsoup.parse(view.body)

        "Element 3 of the personalDetails array should be 'No' for Other Properties no Personal Allowance" in {
          document.body().getElementById("personalDetails(2)").text() shouldBe "No"
        }
      }

      "the user has £0 current income, with other properties" should {
        lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

        lazy val view = summaryReport(summaryIndividualFlatNoIncomeOtherPropYes, calcModelOneRate, taxYear,
          sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
        lazy val document = Jsoup.parse(view.body)

        "Element 3 of the personalDetails array should be £0.00 for Other Properties Gain not Personal Allowance" in {
          document.body().getElementById("personalDetails(2)").text() shouldBe "£0.00"
        }
      }


      "users calculation results in a loss" should {
        lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

        lazy val view = summaryReport(summaryIndividualFlatLoss, calcModelLoss, taxYear,
          sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
        lazy val document = Jsoup.parse(view.body)

        s"have ${Messages("calc.summary.calculation.details.totalLoss")} output" in {
          document.body.getElementById("calcDetails").text() should include(Messages("calc.summary.calculation.details.totalLoss"))
        }

        s"have £10,000.00 loss" in {
          document.body.getElementById("calculationDetails(1)").text() shouldBe "£10,000"
        }
      }


      "regular trustee is chosen with a time apportioned calculation" when {

        "the user has provided a value for the AEA" should {

          lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

          lazy val view = summaryReport(summaryTrusteeTAWithAEA, calcModelOneRate, taxYear,
            sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
          lazy val document = Jsoup.parse(view.body)

          "have an election description of time apportionment method" in {
            document.body().getElementById("calculationDetails(0)").text() shouldBe Messages("calc.summary.calculation.details.timeCalculation")
          }

          "have an acquisition date of '9 September 1990'" in {
            document.body().getElementById("purchaseDetails(0)").text() shouldBe "9 September 1999"
          }

          "have a 'trustee' owner" in {
            document.body().getElementById("personalDetails(0)").text() shouldBe "Trustee"
          }

          "have an answer of 'No to the disabled trustee question" in {
            document.body().getElementById("personalDetails(1)").text() shouldBe "No"
          }

          "have the answer for Previous Disposals (Other Properties) of 'Yes'" in {
            document.body.getElementById("personalDetails(2)").text() shouldBe "Yes"
          }

          "have a remaining CGT Allowance of £1,500" in {
            document.body().getElementById("personalDetails(3)").text() shouldBe "£1,500.00"
          }

          "have a base tax rate of 20%" in {
            document.body().getElementById("calculationDetails(4)").text() shouldBe "20%"
          }
        }

        "the user has provided no value for the AEA" should {
          lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

          lazy val view = summaryReport(summaryTrusteeTAWithoutAEA, calcModelTwoRates, taxYear,
            sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
          lazy val document = Jsoup.parse(view.body)

          "have an answer of 'No to the disabled trustee question" in {
            document.getElementById("personalDetails(1)").text() shouldBe "No"
          }

          "have the answer for Previous Disposals (Other Properties) of 'No'" in {
            document.body().getElementById("personalDetails(2)").text() shouldBe "No"
          }
        }
      }

      "disabled trustee is chosen with a time apportioned calculation" when {

        "the user has provided a value for the AEA" should {
          lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

          lazy val view = summaryReport(summaryDisabledTrusteeTAWithAEA, calcModelTwoRates, taxYear,
            sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
          lazy val document = Jsoup.parse(view.body)

          "have an answer of 'Yes' to the disabled trustee question" in {
            document.body().getElementById("personalDetails(1)").text() shouldBe "Yes"
          }

          "have a remaining CGT Allowance of £1,500" in {
            document.body().getElementById("personalDetails(3)").text() shouldBe "£1,500.00"
          }
        }

        "the user has provided no value for the AEA" should {
          lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

          lazy val view = summaryReport(summaryDisabledTrusteeTAWithoutAEA, calcModelTwoRates, taxYear,
            sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
          lazy val document = Jsoup.parse(view.body)

          "have an answer of 'Yes' to the disabled trustee question" in {
            document.body().getElementById("personalDetails(1)").text() shouldBe "Yes"
          }

          "have the answer for Previous Disposals (Other Properties) of 'No'" in {
            document.body().getElementById("personalDetails(2)").text() shouldBe "No"
          }
        }
      }

      "personal representative is chosen with a flat calculation" when {

        "the user has provided a value for the AEA" should {
          lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

          lazy val view = summaryReport(summaryRepresentativeFlatWithAEA, calcModelTwoRates, taxYear,
            sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
          lazy val document = Jsoup.parse(view.body)

          "have a 'Personal Representative' owner" in {
            document.body().getElementById("personalDetails(0)").text() shouldBe "Personal Representative"
          }

          "have the answer for Previous Disposals (Other Properties) of 'Yes' " in {
            document.body.getElementById("personalDetails(1)").text() shouldBe "Yes"
          }

          "have a remaining CGT Allowance of £1,500" in {
            document.body().getElementById("personalDetails(2)").text() shouldBe "£1,500.00"
          }
        }

        "the user has provided no value for the AEA" should {
          lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

          lazy val view = summaryReport(summaryRepresentativeFlatWithoutAEA, calcModelTwoRates, taxYear,
            sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
          lazy val document = Jsoup.parse(view.body)

          "have a 'Personal Representative' owner" in {
            document.body().getElementById("personalDetails(0)").text() shouldBe "Personal Representative"
          }

          "have the answer for Previous Disposals (Other Properties) of 'No'" in {
            document.body().getElementById("personalDetails(1)").text() shouldBe "No"
          }
        }

      }

      "individual is chosen with a rebased calculation" when {

        "user provides no acquisition date and has two tax rates" should {
          lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

          lazy val view = summaryReport(summaryIndividualRebased, calcModelTwoRates, taxYear,
            sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
          lazy val document = Jsoup.parse(view.body)

          "have an election description of 'How much of your total gain you've made since 5 April 2015'" in {
            document.body().getElementById("calculationDetails(0)").text() shouldBe Messages("calc.summary.calculation.details.rebasedCalculation")
          }

          "include the question for the rebased value" in {
            document.select("#purchaseDetails").text should include(Messages("calc.rebasedValue.questionTwo"))
          }

          "have a value for the rebased value" in {
            document.body.getElementById("purchaseDetails(1)").text() shouldBe "£150,000.00"
          }

          "include the question for the rebased costs" in {
            document.select("#purchaseDetails").text should include(Messages("calc.rebasedCosts.questionTwo"))
          }

          "have a value for the rebased costs" in {
            document.body.getElementById("purchaseDetails(2)").text() shouldBe "£1,000.00"
          }

          "include the question for the improvements after" in {
            document.select("#propertyDetails").text should include(Messages("calc.improvements.questionFour"))
          }

          "have a value for the improvements after" in {
            document.body.getElementById("propertyDetails(1)").text() shouldBe "£3,000.00"
          }

          "have a value for the other reliefs rebased" in {
            document.body.getElementById("deductions(2)").text() shouldBe "£777.00"
          }

        }

        "user provides no acquisition date and has one tax rate" should {
          lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

          lazy val view = summaryReport(summaryIndividualRebasedNoAcqDate, calcModelOneRate, taxYear,
            sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
          lazy val document = Jsoup.parse(view.body)

          "have an election description of 'How much of your total gain you've made since 5 April 2015'" in {
            document.body().getElementById("calculationDetails(0)").text() shouldBe Messages("calc.summary.calculation.details.rebasedCalculation")
          }

          "include the question for whether the acquisition date is provided" in {
            document.select("#purchaseDetails").text should include(Messages("calc.acquisitionDate.question"))
          }

          "have an answer to the question for providing an acquisition date of 'No'" in {
            document.body().getElementById("purchaseDetails(0)").text() shouldBe Messages("No")
          }

          "the value of allowable losses should be £0" in {
            document.body().getElementById("deductions(1)").text shouldBe "£0.00"
          }

          "the value of other reliefs should be £0" in {
            document.body().getElementById("deductions(2)").text shouldBe "£0.00"
          }
        }

        "user provides acquisition date and no rebased costs" should {
          lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

          lazy val view = summaryReport(summaryIndividualRebasedNoRebasedCosts, calcModelOneRate, taxYear,
            sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
          lazy val document = Jsoup.parse(view.body)

          "have no value for the rebased costs" in {
            document.body.getElementById("purchaseDetails(2)").text() shouldBe "£0.00"
          }
        }

        "user provides no acquisition date and no rebased costs" should {
          lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

          lazy val view = summaryReport(summaryIndividualRebasedNoAcqDateOrRebasedCosts, calcModelOneRate, taxYear,
            sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
          lazy val document = Jsoup.parse(view.body)

          "have no value for the rebased costs" in {
            document.body.getElementById("purchaseDetails(2)").text() shouldBe "£0.00"
          }
        }
      }

      "only an upper rate result is returned" should {
        lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

        lazy val view = summaryReport(summaryIndividualFlatWithAEA, calcModelUpperRate, taxYear,
          sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
        lazy val document = Jsoup.parse(view.body)

        "return a value of 28% for the tax rate" in {
          document.body.getElementById("calculationDetails(4)").text() shouldBe "28%"
        }
      }

      "a negative taxable gain is returned" should {
        lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

        lazy val view = summaryReport(summaryIndividualFlatWithAEA, calcModelNegativeTaxable, taxYear,
          sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
        lazy val document = Jsoup.parse(view.body)

        "include 'Loss carried forward'" in {
          document.select("#calcDetails").text should include(Messages("calc.summary.calculation.details.lossCarriedForward"))
        }

        "return a value of £10,000 for loss carried forward" in {
          document.body.getElementById("calculationDetails(3)").text() shouldBe "£10,000"
        }
      }

      "a zero taxable gain is returned" should {
        lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

        lazy val view = summaryReport(summaryIndividualFlatWithAEA, calcModelZeroTaxable, taxYear,
          sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
        lazy val document = Jsoup.parse(view.body)

        "return a value of £0 for taxable gain" in {
          document.body.getElementById("calculationDetails(3)").text() shouldBe "£0"
        }
      }

      "a total gain of zero is returned" should {
        lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

        lazy val view = summaryReport(summaryIndividualFlatWithAEA, calcModelZeroTotal, taxYear,
          sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
        lazy val document = Jsoup.parse(view.body)

        "return a value of £0 for total gain" in {
          document.body.getElementById("calculationDetails(1)").text() shouldBe "£0"
        }
      }

      "a value with some PRR is returned" should {
        lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

        lazy val view = summaryReport(summaryIndividualFlatWithAEA, calcModelSomePRR, taxYear,
          sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
        lazy val document = Jsoup.parse(view.body)

        "return a value of £10,000 for the simple PRR" in {
          document.body.getElementById("deductions(0)").text() shouldBe "£10,000.00"
        }
      }

      "a value with PRR claimed but no value" should {
        lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

        lazy val view = summaryReport(summaryIndividualWithAllOptions, calcModelOneRate, taxYear,
          sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
        lazy val document = Jsoup.parse(view.body)

        "return a value of £0 for the simple PRR" in {
          document.body.getElementById("deductions(0)").text() shouldBe "£0.00"
        }
      }
    }
  }
}

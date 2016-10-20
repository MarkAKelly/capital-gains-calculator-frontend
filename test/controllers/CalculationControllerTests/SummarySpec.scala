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

package controllers.CalculationControllerTests


import common.DefaultRoutes._
import common.{KeystoreKeys, TestModels}
import connectors.CalculatorConnector
import controllers.nonresident.{SummaryController, routes}
import models.nonresident.{AcquisitionDateModel, CalculationResultModel, RebasedValueModel, SummaryModel}
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.MessageLookup.{NonResident => commonMessages}
import assets.MessageLookup.NonResident.{Summary => messages}

import scala.concurrent.Future

class SummarySpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  implicit val hc = new HeaderCarrier()
  def setupTarget(
                   summary: SummaryModel,
                   result: CalculationResultModel,
                   acquisitionDateData: Option[AcquisitionDateModel],
                   rebasedValueData: Option[RebasedValueModel]
                 ): SummaryController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[RebasedValueModel](Matchers.eq(KeystoreKeys.rebasedValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(rebasedValueData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(acquisitionDateData))

    when(mockCalcConnector.createSummary(Matchers.any()))
      .thenReturn(Future.successful(summary))

    when(mockCalcConnector.calculateFlat(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(result)))

    when(mockCalcConnector.calculateTA(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(result)))

    when(mockCalcConnector.calculateRebased(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(result)))

    new SummaryController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "In CalculationController calling the .summary action" when {
    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/non-resident/summary").withSession(SessionKeys.sessionId -> "12345")

    "Testing the back links for all user types" when {

      "Acquisition Date is > 5 April 2015" should {
        val target = setupTarget(
          TestModels.summaryIndividualFlatWithAEA,
          TestModels.calcModelTwoRates,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to ${routes.OtherReliefsController.otherReliefs().url}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
          document.body.getElementById("back-link").attr("href") shouldEqual routes.OtherReliefsController.otherReliefs().url
        }
      }

      "Acquisition Date is not supplied and no rebased value has been supplied" should {
        val target = setupTarget(
          TestModels.summaryIndividualFlatWithAEA,
          TestModels.calcModelTwoRates,
          Some(AcquisitionDateModel("No", None,None,None)),
          Some(RebasedValueModel("No", None))
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to ${routes.OtherReliefsController.otherReliefs().url}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
          document.body.getElementById("back-link").attr("href") shouldEqual routes.OtherReliefsController.otherReliefs().url
        }
      }

      "Acquisition Date is not supplied and rebased value is supplied" should {
        val target = setupTarget(
          TestModels.summaryIndividualFlatWithAEA,
          TestModels.calcModelTwoRates,
          Some(AcquisitionDateModel("No", None,None,None)),
          Some(RebasedValueModel("Yes", Some(500)))
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to ${routes.CalculationElectionController.calculationElection().url}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
          document.body.getElementById("back-link").attr("href") shouldEqual routes.CalculationElectionController.calculationElection().url
        }
      }

      "Acquisition Date <= 5 April 2015" should {
        val target = setupTarget(
          TestModels.summaryIndividualFlatWithAEA,
          TestModels.calcModelTwoRates,
          Some(AcquisitionDateModel("Yes", Some(1),Some(1),Some(2014))),
          Some(RebasedValueModel("Yes", Some(500)))
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to ${routes.CalculationElectionController.calculationElection().url}" in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
          document.body.getElementById("back-link").attr("href") shouldEqual routes.CalculationElectionController.calculationElection().url
        }
      }

      "Acquisition Date Model is not supplied" should {
        val target = setupTarget(
          TestModels.summaryIndividualFlatWithAEA,
          TestModels.calcModelTwoRates,
          None,
          Some(RebasedValueModel("Yes", Some(500)))
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to $missingDataRoute " in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
          document.body.getElementById("back-link").attr("href") shouldEqual missingDataRoute
        }
      }

      "Acquisition Date Model is supplied with no date but Rebased Value Model is not" should {
        val target = setupTarget(
          TestModels.summaryIndividualFlatWithAEA,
          TestModels.calcModelTwoRates,
          Some(AcquisitionDateModel("No", None,None,None)),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have a 'Back' link to $missingDataRoute " in {
          document.body.getElementById("back-link").text shouldEqual commonMessages.back
          document.body.getElementById("back-link").attr("href") shouldEqual missingDataRoute
        }
      }
    }

    "individual is chosen with a flat calculation" when {

      "the user has provided a value for the AEA" should {
        val target = setupTarget(
          TestModels.summaryIndividualFlatWithAEA,
          TestModels.calcModelTwoRates,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        "return a 200" in {
          status(result) shouldBe 200
        }

        "return some HTML that" should {

          s"should have the title '${messages.title}'" in {
            document.getElementsByTag("title").text shouldEqual messages.title
          }

          "have a back button" in {
            document.getElementById("back-link").text shouldEqual commonMessages.back
          }

          s"have the correct sub-heading '${messages.secondaryHeading}'" in {
            document.select("h1 span").text shouldEqual messages.secondaryHeading
          }

          "have a result amount currently set to £8,000.00" in {
            document.select("h1 b").text shouldEqual "£8,000.00"
          }

          "have a 'Calculation details' section that" should {

            s"include the section heading '${messages.calculationDetailsTitle}" in {
              document.select("#calcDetails").text should include(messages.calculationDetailsTitle)
            }

            s"include '${messages.calculationElection}'" in {
              document.select("#calcDetails").text should include(messages.calculationElection)
            }

            s"have an election description of '${messages.flatCalculation}'" in {
              document.body().getElementById("calcDetails(0)").text() shouldBe messages.flatCalculation
            }

            s"include '${messages.totalGain}'" in {
              document.select("#calcDetails").text should include(messages.totalGain)
            }

            "have a total gain equal to £40,000.00" in {
              document.body().getElementById("calcDetails(1)").text() shouldBe "£40,000"
            }

            s"include '${messages.usedAEA}'" in {
              document.select("#calcDetails").text should include(messages.usedAEA)
            }

            "have a used AEA value equal to £0" in {
              document.body().getElementById("calcDetails(2)").text() shouldBe "£0"
            }

            s"include '${messages.taxableGain}'" in {
              document.select("#calcDetails").text should include(messages.taxableGain)
            }

            "have a taxable gain equal to £40,000" in {
              document.body().getElementById("calcDetails(3)").text() shouldBe "£40,000"
            }

            s"include '${messages.taxRate}'" in {
              document.select("#calcDetails").text should include(messages.taxRate)
            }

            "have a combined tax rate of £32,000 and £8,000" in {
              document.body().getElementById("calcDetails(4)").text() shouldBe "£32,000 at 18% £8,000 at 28%"
            }

          }

          "have a 'Personal details' section that" should {

            s"include the section heading '${messages.personalDetailsTitle}'" in {
              document.select("#personalDetails").text should include(messages.personalDetailsTitle)
            }

            s"include the question '${commonMessages.CustomerType.question}'" in {
              document.select("#personalDetails").text should include(commonMessages.CustomerType.question)
            }

            "have an 'individual' owner and link to the customer-type page" in {
              document.body().getElementById("personalDetails(0)").text() shouldBe "Individual"
              document.body().getElementById("personalDetails(0)").attr("href") shouldEqual routes.CustomerTypeController.customerType().toString()
            }

            s"include the question '${commonMessages.CurrentIncome.question}'" in {
              document.select("#personalDetails").text should include(commonMessages.CurrentIncome.question)
            }

            "have an total income of £1,000 and link to the current-income screen" in {
              document.body().getElementById("personalDetails(1)").text() shouldBe "£1,000.00"
              document.body().getElementById("personalDetails(1)").attr("href") shouldEqual routes.CurrentIncomeController.currentIncome().toString()
            }

            s"include the question '${commonMessages.PersonalAllowance.question}'" in {
              document.select("#personalDetails").text should include(commonMessages.PersonalAllowance.question)
            }

            "have a personal allowance of £9,000 that has a link to the personal allowance page." in {
              document.body().getElementById("personalDetails(2)").text() shouldBe "£9,000.00"
              document.body().getElementById("personalDetails(2)").attr("href") shouldEqual routes.PersonalAllowanceController.personalAllowance().toString()
            }

            s"include the question '${commonMessages.OtherProperties.questionTwo}'" in {
              document.select("#personalDetails").text should include(commonMessages.OtherProperties.questionTwo)
            }

            "have a total taxable gain of prior disposals of £9,600 and link to the other-properties page" in {
              document.body().getElementById("personalDetails(3)").text() shouldBe "£9,600.00"
              document.body().getElementById("personalDetails(3)").attr("href") shouldEqual routes.OtherPropertiesController.otherProperties().toString()
            }

            s"include the question '${commonMessages.AnnualExemptAmount.question}'" in {
              document.select("#personalDetails").text should include(commonMessages.AnnualExemptAmount.question)
            }

            "have a remaining CGT Allowance of £1,500 and link to the allowance page" in {
              document.body().getElementById("personalDetails(4)").text() shouldBe "£1,500.00"
              document.body().getElementById("personalDetails(4)").attr("href") shouldEqual routes.AnnualExemptAmountController.annualExemptAmount().toString()
            }
          }

          "have a 'Purchase details' section that" should {

            s"include the section heading '${messages.purchaseDetailsTitle}" in {
              document.select("#purchaseDetails").text should include(messages.purchaseDetailsTitle)
            }

            "include the question for whether the acquisition date is provided" in {
              document.select("#purchaseDetails").text should include(commonMessages.AcquisitionDate.question)
            }

            "have an answer to the question for providing an acquisition date of 'No'" in {
              document.body().getElementById("purchaseDetails(0)").text() shouldBe Messages("No")
              document.body().getElementById("purchaseDetails(0)").attr("href") shouldEqual routes.AcquisitionDateController.acquisitionDate().toString()

            }

            s"include the question '${commonMessages.AcquisitionValue.question}'" in {
              document.select("#purchaseDetails").text should include(commonMessages.AcquisitionValue.question)
            }

            "have an acquisition value of £100,000 and link to the acquisition value page" in {
              document.body().getElementById("purchaseDetails(1)").text() shouldBe "£100,000.00"
              document.body().getElementById("purchaseDetails(1)").attr("href") shouldEqual routes.AcquisitionValueController.acquisitionValue().toString()
            }

            s"include the question '${commonMessages.AcquisitionCosts.question}'" in {
              document.select("#purchaseDetails").text should include(commonMessages.AcquisitionCosts.question)
            }

            "have a acquisition costs of £0 and link to the acquisition-costs page" in {
              document.body().getElementById("purchaseDetails(2)").text() shouldBe "£0.00"
              document.body().getElementById("purchaseDetails(2)").attr("href") shouldEqual routes.AcquisitionCostsController.acquisitionCosts().toString()
            }
          }

          "have a 'Property details' section that" should {

            "include the section heading 'Property details" in {
              document.select("#propertyDetails").text should include(Messages("calc.summary.property.details.title"))
            }

            s"include the question '${commonMessages.Improvements.question}'" in {
              document.select("#propertyDetails").text should include(commonMessages.Improvements.question)
            }

            "the answer to the improvements question should be No and should link to the improvements page" in {
              document.body.getElementById("propertyDetails(0)").text shouldBe "No"
              document.body().getElementById("propertyDetails(0)").attr("href") shouldEqual routes.ImprovementsController.improvements().toString()
            }
          }

          "have a 'Sale details' section that" should {

            s"include the section heading ${messages.saleDetailsTitle}" in {
              document.select("#saleDetails").text should include(messages.saleDetailsTitle)
            }

            s"include the question ${commonMessages.DisposalDate.question}" in {
              document.select("#saleDetails").text should include(commonMessages.DisposalDate.question)
            }

            "the date of disposal should be '10 October 2010 and link to the disposal-date page" in {
              document.body().getElementById("saleDetails(0)").text shouldBe "10 October 2010"
              document.body().getElementById("saleDetails(0)").attr("href") shouldEqual routes.DisposalDateController.disposalDate().toString()
            }

            s"include the question '${commonMessages.DisposalValue.question}'" in {
              document.select("#saleDetails").text should include(commonMessages.DisposalValue.question)
            }

            "the value of the sale should be £150,000 and link to the disposal-value page" in {
              document.body().getElementById("saleDetails(1)").text shouldBe "£150,000.00"
              document.body().getElementById("saleDetails(1)").attr("href") shouldEqual routes.DisposalValueController.disposalValue().toString()
            }

            s"include the question ${commonMessages.DisposalCosts.question}" in {
              document.select("#saleDetails").text should include(commonMessages.DisposalCosts.question)
            }

            "the value of the costs should be £0 and link to the disposal costs page" in {
              document.body().getElementById("saleDetails(2)").text shouldBe "£0.00"
              document.body().getElementById("saleDetails(2)").attr("href") shouldEqual routes.DisposalCostsController.disposalCosts().toString()
            }
          }

          "have a 'Deductions details' section that" should {

            s"include the section heading '${messages.deductionsTitle}" in {
              document.select("#deductions").text should include(messages.deductionsTitle)
            }

            s"include the question '${Messages("calc.allowableLosses.question.two")}'" in {
              ////////////////////////////////////////////////////////////////////////////////////////////////////////
              document.select("#deductions").text should include(Messages("calc.allowableLosses.question.two"))
              ////////////////////////////////////////////////////////////////////////////////////////////////////////
            }

            "the value of allowable losses should be £0 and link to the allowable-losses page" in {
              document.body().getElementById("deductions(1)").text shouldBe "£0.00"
              document.body().getElementById("deductions(1)").attr("href") shouldEqual routes.AllowableLossesController.allowableLosses().toString()
            }

            s"include the question '${commonMessages.PrivateResidenceRelief.question}'" in {
              document.select("#deductions").text should include(commonMessages.PrivateResidenceRelief.question)
            }

            "the answer to question should be No and link to the other-reliefs page" in {
              document.body().getElementById("deductions(2)").text shouldBe "No"
              document.body().getElementById("deductions(2)").attr("href") shouldEqual routes.OtherReliefsController.otherReliefs().toString()
            }

            s"include the question '${commonMessages.OtherReliefs.question}'" in {
              document.select("#deductions").text should include(commonMessages.OtherReliefs.question)
            }

            "the PRR claimed question's answer should be 'No' and be a link to the PRR page" in {
              document.body().getElementById("deductions(0)").text shouldBe "No"
              document.body().getElementById("deductions(0)").attr("href") shouldEqual
                routes.PrivateResidenceReliefController.privateResidenceRelief().toString()
            }

          }

          "have a 'What to do next' section that" should {

            s"have the heading '${messages.whatToDoNextText}'" in {
              document.select("#whatToDoNext H2").text shouldEqual messages.whatToDoNextText
            }

            "include the text 'You need to tell HMRC about the property'" in {
              document.select("#whatToDoNext").text should
                include(Messages("calc.summary.next.actions.text"))
              include(Messages("calc.summary.next.actions.link"))
            }
          }

          s"have a link to '${messages.startAgain}'" in {
            document.select("#startAgain").text shouldEqual messages.startAgain
          }
        }
      }

      "the user has provided no value for the AEA and elected Flat calc" should {
        val target = setupTarget(
          TestModels.summaryIndividualFlatWithoutAEA,
          TestModels.calcModelOneRate,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

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
          document.select("#purchaseDetails").text should include(commonMessages.AcquisitionDate.question)
        }

        "have an answer to the question for providing an acquisition date of 'No'" in {
          document.body().getElementById("purchaseDetails(0)").text() shouldBe commonMessages.no
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
          document.body().getElementById("calcDetails(4)").text() shouldBe "20%"
        }
      }

      "the user has £0 current income, with no other properties" should {
        val target = setupTarget(
          TestModels.summaryIndividualFlatNoIncomeOtherPropNo,
          TestModels.calcModelOneRate,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        "Element 3 of the personalDetails array should be 'No' for Other Properties no Personal Allowance" in {
          document.body().getElementById("personalDetails(2)").text() shouldBe commonMessages.no
        }
      }

      "the user has £0 current income, with other properties" should {
        val target = setupTarget(
          TestModels.summaryIndividualFlatNoIncomeOtherPropYes,
          TestModels.calcModelOneRate,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        "Element 3 of the personalDetails array should be £0.00 for Other Properties Gain not Personal Allowance" in {
          document.body().getElementById("personalDetails(2)").text() shouldBe "£0.00"
        }
      }


      "users calculation results in a loss" should {
        val target = setupTarget(
          TestModels.summaryIndividualFlatLoss,
          TestModels.calcModelLoss,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have ${messages.totalLoss} output" in {
          document.body.getElementById("calcDetails").text() should include (messages.totalLoss)
        }

        s"have £10,000.00 loss" in {
          document.body.getElementById("calcDetails(1)").text() shouldBe "£10,000"
        }
      }
    }

    "regular trustee is chosen with a time apportioned calculation" when {

      "the user has provided a value for the AEA" should {
        val target = setupTarget(
          TestModels.summaryTrusteeTAWithAEA,
          TestModels.calcModelOneRate,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        "have an election description of time apportionment method" in {
          document.body().getElementById("calcDetails(0)").text() shouldBe messages.timeCalculation
        }

        "have an acquisition date of '9 September 1990'" in{
          document.body().getElementById("purchaseDetails(0)").text() shouldBe "9 September 1999"
        }

        "have a 'trustee' owner" in {
          document.body().getElementById("personalDetails(0)").text() shouldBe "Trustee"
        }

        "have an answer of 'No to the disabled trustee question" in {
          document.body().getElementById("personalDetails(1)").text() shouldBe commonMessages.no
        }

        "have the answer for Previous Disposals (Other Properties) of 'Yes'" in {
          document.body.getElementById("personalDetails(2)").text() shouldBe commonMessages.yes

        }

        "have a remaining CGT Allowance of £1,500" in {
          document.body().getElementById("personalDetails(3)").text() shouldBe "£1,500.00"
        }

        "have a base tax rate of 20%" in {
          document.body().getElementById("calcDetails(4)").text() shouldBe "20%"
        }
      }

      "the user has provided no value for the AEA" should {
        val target = setupTarget(
          TestModels.summaryTrusteeTAWithoutAEA,
          TestModels.calcModelTwoRates,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        "have an answer of 'No to the disabled trustee question" in {
         document.getElementById("personalDetails(1)").text() shouldBe commonMessages.no
        }

        "have the answer for Previous Disposals (Other Properties) of 'No'" in {
          document.body().getElementById("personalDetails(2)").text() shouldBe commonMessages.no
        }
      }
    }

    "disabled trustee is chosen with a time apportioned calculation" when {

      "the user has provided a value for the AEA" should {
        val target = setupTarget(
          TestModels.summaryDisabledTrusteeTAWithAEA,
          TestModels.calcModelTwoRates,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        "have an answer of 'Yes' to the disabled trustee question" in {
          document.body().getElementById("personalDetails(1)").text() shouldBe commonMessages.yes
        }

        "have a remaining CGT Allowance of £1,500" in {
          document.body().getElementById("personalDetails(3)").text() shouldBe "£1,500.00"
        }
      }

      "the user has provided no value for the AEA" should {
        val target = setupTarget(
          TestModels.summaryDisabledTrusteeTAWithoutAEA,
          TestModels.calcModelTwoRates,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        "have an answer of 'Yes' to the disabled trustee question" in {
          document.body().getElementById("personalDetails(1)").text() shouldBe commonMessages.yes
        }

        "have the answer for Previous Disposals (Other Properties) of 'No'" in {
          document.body().getElementById("personalDetails(2)").text() shouldBe commonMessages.no
        }
      }
    }

    "personal representative is chosen with a flat calculation" when {

      "the user has provided a value for the AEA" should {
        val target = setupTarget(
          TestModels.summaryRepresentativeFlatWithAEA,
          TestModels.calcModelTwoRates,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        "have a 'Personal Representative' owner" in {
          document.body().getElementById("personalDetails(0)").text() shouldBe "Personal Representative"
        }

        "have the answer for Previous Disposals (Other Properties) of 'Yes' " in {
          document.body.getElementById("personalDetails(1)").text() shouldBe commonMessages.yes
        }

        "have a remaining CGT Allowance of £1,500" in {
          document.body().getElementById("personalDetails(2)").text() shouldBe "£1,500.00"
        }
      }

      "the user has provided no value for the AEA" should {
        val target = setupTarget(
          TestModels.summaryRepresentativeFlatWithoutAEA,
          TestModels.calcModelTwoRates,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        "have a 'Personal Representative' owner" in {
          document.body().getElementById("personalDetails(0)").text() shouldBe "Personal Representative"
        }

        "have the answer for Previous Disposals (Other Properties) of 'No'" in {
          document.body().getElementById("personalDetails(1)").text() shouldBe commonMessages.no
        }
      }

    }
    
    "individual is chosen with a rebased calculation" when {

      "user provides no acquisition date and has two tax rates" should {
        val target = setupTarget(
          TestModels.summaryIndividualRebased,
          TestModels.calcModelTwoRates,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        "have an election description of 'How much of your total gain you've made since 5 April 2015'" in {
          document.body().getElementById("calcDetails(0)").text() shouldBe Messages("calc.summary.calculation.details.rebasedCalculation")
        }

        "include the question for the rebased value" in {
          document.select("#purchaseDetails").text should include(commonMessages.RebasedValue.inputQuestion)
        }

        "have a value for the rebased value" in {
          document.body.getElementById("purchaseDetails(1)").text() shouldBe "£150,000.00"
        }

        "include the question for the rebased costs" in {
          document.select("#purchaseDetails").text should include(commonMessages.RebasedCosts.inputQuestion)
        }

        "have a value for the rebased costs" in {
          document.body.getElementById("purchaseDetails(2)").text() shouldBe "£1,000.00"
        }

        "include the question for the improvements after" in {
          document.select("#propertyDetails").text should include(commonMessages.Improvements.questionFour)
        }

        "have a value for the improvements after" in {
          document.body.getElementById("propertyDetails(1)").text() shouldBe "£3,000.00"
        }

        "have a value for the other reliefs rebased" in {
          document.body.getElementById("deductions(2)").text() shouldBe "£777.00"
          document.body().getElementById("deductions(2)").attr("href") shouldEqual routes.OtherReliefsRebasedController.otherReliefsRebased().toString()
        }

      }

      "user provides no acquisition date and has one tax rate" should {
        val target = setupTarget(
          TestModels.summaryIndividualRebasedNoAcqDate,
          TestModels.calcModelOneRate,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        s"have an election description of '${messages.rebasedCalculation}'" in {
          document.body().getElementById("calcDetails(0)").text() shouldBe messages.rebasedCalculation
        }

        "include the question for whether the acquisition date is provided" in {
          document.select("#purchaseDetails").text should include(commonMessages.AcquisitionDate.question)
        }

        "have an answer to the question for providing an acquisition date of 'No'" in {
          document.body().getElementById("purchaseDetails(0)").text() shouldBe commonMessages.no
        }

        "the value of allowable losses should be £0" in {
          document.body().getElementById("deductions(1)").text shouldBe "£0.00"
        }

        "the value of other reliefs should be £0" in {
          document.body().getElementById("deductions(2)").text shouldBe "£0.00"
        }
      }

      "user provides acquisition date and no rebased costs" should {
        val target = setupTarget(
          TestModels.summaryIndividualRebasedNoRebasedCosts,
          TestModels.calcModelOneRate,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        "have no value for the rebased costs" in {
          document.body.getElementById("purchaseDetails(2)").text() shouldBe "£0.00"
        }
      }

      "user provides no acquisition date and no rebased costs" should {
        val target = setupTarget(
          TestModels.summaryIndividualRebasedNoAcqDateOrRebasedCosts,
          TestModels.calcModelOneRate,
          Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
          None
        )
        lazy val result = target.summary()(fakeRequest)
        lazy val document = Jsoup.parse(bodyOf(result))

        "have no value for the rebased costs" in {
          document.body.getElementById("purchaseDetails(2)").text() shouldBe "£0.00"
        }
      }
    }

    "only an upper rate result is returned" should {
      val target = setupTarget(TestModels.summaryIndividualFlatWithAEA, TestModels.calcModelUpperRate, None, None)
      lazy val result = target.summary()(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a value of 28% for the tax rate" in {
        document.body.getElementById("calcDetails(4)").text() shouldBe "28%"
      }
    }

    "a negative taxable gain is returned" should {
      val target = setupTarget(TestModels.summaryIndividualFlatWithAEA, TestModels.calcModelNegativeTaxable, None, None)
      lazy val result = target.summary()(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "include 'Loss carried forward'" in {
        document.select("#calcDetails").text should include(messages.lossesCarriedForward)
      }

      "return a value of £10,000 for loss carried forward" in {
        document.body.getElementById("calcDetails(3)").text() shouldBe "£10,000"
      }
    }

    "a zero taxable gain is returned" should {
      val target = setupTarget(TestModels.summaryIndividualFlatWithAEA, TestModels.calcModelZeroTaxable, None, None)
      lazy val result = target.summary()(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a value of £0 for taxable gain" in {
        document.body.getElementById("calcDetails(3)").text() shouldBe "£0"
      }
    }

    "a total gain of zero is returned" should {
      val target = setupTarget(TestModels.summaryIndividualFlatWithAEA, TestModels.calcModelZeroTotal, None, None)
      lazy val result = target.summary()(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a value of £0 for total gain" in {
        document.body.getElementById("calcDetails(1)").text() shouldBe "£0"
      }
    }

    "a value with some PRR is returned" should {
      val target = setupTarget(TestModels.summaryIndividualFlatWithAEA, TestModels.calcModelSomePRR, None, None)
      lazy val result = target.summary()(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a value of £10,000 for the simple PRR" in {
        document.body.getElementById("deductions(0)").text() shouldBe "£10,000.00"
      }
    }

    "a value with PRR claimed but no value" should {
      val target = setupTarget(TestModels.summaryIndividualWithAllOptions, TestModels.calcModelOneRate, None, None)
      lazy val result = target.summary()(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a value of £0 for the simple PRR" in {
        document.body.getElementById("deductions(0)").text() shouldBe "£0.00"
      }
    }
  }

  "calling the .restart action" should {
    lazy val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/restart").withSession(SessionKeys.sessionId -> "12345")
    val target = setupTarget(
      TestModels.summaryIndividualFlatWithAEA,
      TestModels.calcModelTwoRates,
      Some(AcquisitionDateModel("Yes", Some(1), Some(1), Some(2017))),
      None
    )
    lazy val result = target.restart()(fakeRequest)

    "return a 303" in {
      status(result) shouldBe 303
    }
  }

}

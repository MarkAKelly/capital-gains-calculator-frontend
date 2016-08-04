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

package views.resident.properties.report

import assets.MessageLookup.{summaryPage => messages}
import assets.{MessageLookup => commonMessages}
import common.Dates
import common.Dates._
import controllers.helpers.FakeRequestHelper
import controllers.resident.properties.routes
import models.resident._
import models.resident.properties.{ChargeableGainAnswers, ReliefsModel, ReliefsValueModel, YourAnswersSummaryModel}
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{report => views}

class PropertiesDeductionsReportViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Deductions Report view" should {
    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
      BigDecimal(200000),
      BigDecimal(10000),
      BigDecimal(100000),
      BigDecimal(10000),
      BigDecimal(30000))
    lazy val deductionAnswers = ChargeableGainAnswers(Some(ReliefsModel(false)),
      None,
      Some(OtherPropertiesModel(false)),
      None,
      None,
      Some(LossesBroughtForwardModel(false)),
      None,
      None)
    lazy val results = ChargeableGainResultModel(BigDecimal(50000),
      BigDecimal(38900),
      BigDecimal(11100),
      BigDecimal(0),
      BigDecimal(11100),
      BigDecimal(0),
      BigDecimal(0),
      Some(BigDecimal(0)))

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    lazy val view = views.deductionsSummaryReport(gainAnswers, deductionAnswers, results, taxYearModel)(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }


    s"have a page heading" which {

      s"includes a secondary heading with text '${messages.pageHeading}'" in {
        doc.select("h1 span.pre-heading").text shouldBe messages.pageHeading
      }

      "includes an amount of tax due of £0.00" in {
        doc.select("h1").text should include("£0.00")
      }
    }

    "does not have a notice summary" in {
      doc.select("div.notice-wrapper").isEmpty() shouldBe true
    }

    s"have a section for the Calculation details" which {

      "has the class 'summary-section' to underline the heading" in {

        doc.select("section#calcDetails h2").hasClass("summary-underline") shouldBe true

      }

      s"has a h2 tag" which {

        s"should have the title '${messages.calcDetailsHeadingDate("2015/16")}'" in {
          doc.select("section#calcDetails h2").text shouldBe messages.calcDetailsHeadingDate("2015/16")
        }

        "has the class 'heading-large'" in {
          doc.select("section#calcDetails h2").hasClass("heading-large") shouldBe true
        }
      }

      "has a numeric output row for the gain" which {

        "should have the question text 'Total Gain'" in {
          doc.select("#gain-question").text shouldBe messages.totalGain
        }

        "should have the value '£50,000'" in {
          doc.select("#gain-amount").text shouldBe "£50,000"
        }
      }

      "has a numeric output row for the deductions" which {

        "should have the question text 'Deductions'" in {
          doc.select("#deductions-question").text shouldBe messages.deductions
        }

        "should have the value '£11,100'" in {
          doc.select("#deductions-amount span.bold-medium").text should include("£11,100")
        }

        "has a breakdown that" should {

          "include a value for Reliefs of £0" in {
            doc.select("#deductions-amount").text should include("Reliefs £0")
          }

          "include a value for Allowable Losses of £0" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsAllowableLosses("2015/16")} £0")
          }

          "include a value for Capital gains tax allowance used of £11,100" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsCapitalGainsTax} £11,100")
          }

          "include a value for Loss brought forward of £0" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsLossBeforeYear("2015/16")} £0")
          }
        }
      }

      "has no numeric output row for allowable losses remaining" in {
        doc.select("#allowableLossRemaining").isEmpty shouldBe true
      }

      "has no numeric output row for brought forward losses remaining" in {
        doc.select("#broughtForwardLossRemaining").isEmpty shouldBe true
      }

      "has a numeric output row for the AEA remaining" which {

        "should have the question text 'Capital gains tax allowance left" in {
          doc.select("#aeaRemaining-question").text should include(messages.aeaRemaining)
        }

        "include a value for Capital gains tax allowance left of £0" in {
          doc.select("#aeaRemaining-amount span.bold-medium").text should include("£0")
        }

        "not include the additional help text for AEA" in {
          doc.select("#aeaRemaining-amount div span").isEmpty shouldBe true
        }
      }
    }

    s"have a section for Your answers" which {

      "has the class 'summary-section' to underline the heading" in {

        doc.select("section#yourAnswers h2").hasClass("summary-underline") shouldBe true

      }

      s"has a h2 tag" which {

        s"should have the title '${messages.yourAnswersHeading}'" in {
          doc.select("section#yourAnswers h2").text shouldBe messages.yourAnswersHeading
        }

        "has the class 'heading-large'" in {
          doc.select("section#yourAnswers h2").hasClass("heading-large") shouldBe true
        }
      }

      "has a date output row for the Disposal Date" which {

        s"should have the question text '${commonMessages.disposalDate.question}'" in {
          doc.select("#disposalDate-question").text shouldBe commonMessages.disposalDate.question
        }

        "should have the date '10 October 2016'" in {
          doc.select("#disposalDate-date span.bold-medium").text shouldBe "10 October 2016"
        }
      }

      "has a numeric output row for the Disposal Value" which {

        s"should have the question text '${commonMessages.disposalValue.question}'" in {
          doc.select("#disposalValue-question").text shouldBe commonMessages.disposalValue.question
        }

        "should have the value '£200,000'" in {
          doc.select("#disposalValue-amount span.bold-medium").text shouldBe "£200,000"
        }
      }

      "has a numeric output row for the Disposal Costs" which {

        s"should have the question text '${commonMessages.disposalCosts.title}'" in {
          doc.select("#disposalCosts-question").text shouldBe commonMessages.disposalCosts.title
        }

        "should have the value '£10,000'" in {
          doc.select("#disposalCosts-amount span.bold-medium").text shouldBe "£10,000"
        }
      }

      "has a numeric output row for the Acquisition Value" which {

        s"should have the question text '${commonMessages.acquisitionValue.title}'" in {
          doc.select("#acquisitionValue-question").text shouldBe commonMessages.acquisitionValue.title
        }

        "should have the value '£100,000'" in {
          doc.select("#acquisitionValue-amount span.bold-medium").text shouldBe "£100,000"
        }
      }

      "has a numeric output row for the Acquisition Costs" which {

        s"should have the question text '${commonMessages.acquisitionCosts.title}'" in {
          doc.select("#acquisitionCosts-question").text shouldBe commonMessages.acquisitionCosts.title
        }

        "should have the value '£10,000'" in {
          doc.select("#acquisitionCosts-amount span.bold-medium").text shouldBe "£10,000"
        }
      }

      "has a numeric output row for the Improvements" which {

        s"should have the question text '${commonMessages.improvementsView.title}'" in {
          doc.select("#improvements-question").text shouldBe commonMessages.improvementsView.title
        }

        "should have the value '£30,000'" in {
          doc.select("#improvements-amount span.bold-medium").text shouldBe "£30,000"
        }
      }

      "has an option output row for tax reliefs" which {

        s"should have the question text '${commonMessages.reliefs.questionSummary}'" in {
          doc.select("#reliefs-question").text shouldBe commonMessages.reliefs.questionSummary
        }

        "should have the value 'No'" in {
          doc.select("#reliefs-option span.bold-medium").text shouldBe "No"
        }
      }

      "has an option output row for other properties" which {

        s"should have the question text '${commonMessages.otherProperties.title("2015/16")}'" in {
          doc.select("#otherProperties-question").text shouldBe commonMessages.otherProperties.title("2015/16")
        }

        "should have the value 'No'" in {
          doc.select("#otherProperties-option span.bold-medium").text shouldBe "No"
        }
      }

      "has an option output row for brought forward losses" which {

        s"should have the question text '${commonMessages.lossesBroughtForward.title("2015/16")}'" in {
          doc.select("#broughtForwardLosses-question").text shouldBe commonMessages.lossesBroughtForward.title("2015/16")
        }

        "should have the value 'No'" in {
          doc.select("#broughtForwardLosses-option span.bold-medium").text shouldBe "No"
        }
      }
    }
  }

  "Deductions Report view with all options selected" should {
    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
      BigDecimal(200000),
      BigDecimal(10000),
      BigDecimal(100000),
      BigDecimal(10000),
      BigDecimal(30000))
    lazy val deductionAnswers = ChargeableGainAnswers(Some(ReliefsModel(true)),
      Some(ReliefsValueModel(BigDecimal(50000))),
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(true)),
      Some(AllowableLossesValueModel(10000)),
      Some(LossesBroughtForwardModel(true)),
      Some(LossesBroughtForwardValueModel(10000)),
      Some(AnnualExemptAmountModel(1000)))
    lazy val results = ChargeableGainResultModel(BigDecimal(50000),
      BigDecimal(-11000),
      BigDecimal(0),
      BigDecimal(11000),
      BigDecimal(71000),
      BigDecimal(1000),
      BigDecimal(2000),
      Some(BigDecimal(50000)))

    lazy val taxYearModel = TaxYearModel("2013/14", false, "2015/16")

    lazy val view = views.deductionsSummaryReport(gainAnswers, deductionAnswers, results, taxYearModel)(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)


    "has a notice summary that" should {

      "have the class notice-wrapper" in {
        doc.select("div.notice-wrapper").isEmpty shouldBe false
      }

      s"have the text ${messages.noticeWarning("2015/16")}" in {
        doc.select("strong.bold-small").text shouldBe messages.noticeWarning("2015/16")
      }

    }

    s"have a section for the Calculation details" which {

      "has the class 'summary-section' to underline the heading" in {

        doc.select("section#calcDetails h2").hasClass("summary-underline") shouldBe true

      }

      s"has a h2 tag" which {

        s"should have the title '${messages.calcDetailsHeadingDate("2013/14")}'" in {
          doc.select("section#calcDetails h2").text shouldBe messages.calcDetailsHeadingDate("2013/14")
        }

        "has the class 'heading-large'" in {
          doc.select("section#calcDetails h2").hasClass("heading-large") shouldBe true
        }
      }

      "has a numeric output row for the gain" which {

        "should have the question text 'Total Gain'" in {
          doc.select("#gain-question").text shouldBe messages.totalGain
        }

        "should have the value '£50,000'" in {
          doc.select("#gain-amount").text shouldBe "£50,000"
        }
      }

      "has a numeric output row for the deductions" which {

        "should have the question text 'Deductions'" in {
          doc.select("#deductions-question").text shouldBe messages.deductions
        }

        "should have the value '£71,000'" in {
          doc.select("#deductions-amount span.bold-medium").text should include("£71,000")
        }

        "has a breakdown that" should {

          "include a value for Reliefs of £50,000" in {
            doc.select("#deductions-amount").text should include("Reliefs £50,000")
          }

          "include a value for Allowable Losses of £10,000" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsAllowableLosses("2013/14")} £10,000")
          }

          "include a value for Capital gains tax allowance used of £0" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsCapitalGainsTax} £0")
          }

          "include a value for Loss brought forward of £10,000" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsLossBeforeYear("2013/14")} £10,000")
          }
        }
      }

      "has a numeric output row for allowable losses remaining" which {

        "should have the question text for an in year loss" in {
          doc.select("#allowableLossRemaining-question").text() shouldBe messages.remainingAllowableLoss("2013/14")
        }

        "should have the value £1000" in {
          doc.select("#allowableLossRemaining-amount").text() should include("£1,000")
        }

        "should have the correct help text" in {
          doc.select("#allowableLossRemaining-amount div span").text() should include(s"${messages.remainingLossHelp} ${messages.remainingLossLink} ${messages.remainingAllowableLossHelp}")
        }
      }

      "has a numeric output row for brought forward losses remaining" which {

        "should have the question text for an out of year loss" in {
          doc.select("#broughtForwardLossRemaining-question").text() shouldBe messages.remainingBroughtForwardLoss("2013/14")
        }

        "should have the value £2000" in {
          doc.select("#broughtForwardLossRemaining-amount").text() should include("£2,000")
        }

        "should have the correct help text" in {
          doc.select("#broughtForwardLossRemaining-amount div span").text() should include(s"${messages.remainingLossHelp} ${messages.remainingLossLink} ${messages.remainingBroughtForwardLossHelp}")
        }
      }

      "has a numeric output row for the AEA remaining" which {

        "should have the question text 'Capital gains tax allowance left" in {
          doc.select("#aeaRemaining-question").text should include(messages.aeaRemaining)
        }

        "include a value for Capital gains tax allowance left of £11,000" in {
          doc.select("#aeaRemaining-amount span.bold-medium").text should include("£11,000")
        }

        "include the additional help text for AEA" in {
          doc.select("#aeaRemaining-amount div span").text shouldBe messages.aeaHelp
        }
      }
    }

    s"have a section for Your answers" which {

      "has the class 'summary-section' to underline the heading" in {
        doc.select("section#yourAnswers h2").hasClass("summary-underline") shouldBe true
      }

      s"has a h2 tag" which {

        s"should have the title '${messages.yourAnswersHeading}'" in {
          doc.select("section#yourAnswers h2").text shouldBe messages.yourAnswersHeading
        }

        "has the class 'heading-large'" in {
          doc.select("section#yourAnswers h2").hasClass("heading-large") shouldBe true
        }
      }

      "has an option output row for tax reliefs" which {

        s"should have the question text '${commonMessages.reliefs.questionSummary}'" in {
          doc.select("#reliefs-question").text shouldBe commonMessages.reliefs.questionSummary
        }

        "should have the value 'Yes'" in {
          doc.select("#reliefs-option span.bold-medium").text shouldBe "Yes"
        }
      }

      "has a numeric output row for tax relief value" which {

        s"should have the question text '${commonMessages.reliefsValue.title}'" in {
          doc.select("#reliefsValue-question").text shouldBe commonMessages.reliefsValue.title
        }

        "should have the value '£50,000'" in {
          doc.select("#reliefsValue-amount span.bold-medium").text shouldBe "£50,000"
        }
      }

      "has an option output row for other properties" which {

        s"should have the question text '${commonMessages.otherProperties.title("2013/14")}'" in {
          doc.select("#otherProperties-question").text shouldBe commonMessages.otherProperties.title("2013/14")
        }

        "should have the value 'Yes'" in {
          doc.select("#otherProperties-option span.bold-medium").text shouldBe "Yes"
        }
      }

      "has an option output row for allowable losses" which {

        s"should have the question text '${commonMessages.allowableLosses.title("2013/14")}'" in {
          doc.select("#allowableLosses-question").text shouldBe commonMessages.allowableLosses.title("2013/14")
        }

        "should have the value 'Yes'" in {
          doc.select("#allowableLosses-option span.bold-medium").text shouldBe "Yes"
        }
      }

      "has a numeric output row for allowable losses value" which {

        s"should have the question text '${commonMessages.allowableLossesValue.title("2013/14")}'" in {
          doc.select("#allowableLossesValue-question").text shouldBe commonMessages.allowableLossesValue.title("2013/14")
        }

        "should have the value '£10,000'" in {
          doc.select("#allowableLossesValue-amount span.bold-medium").text shouldBe "£10,000"
        }
      }

      "has an option output row for brought forward losses" which {

        s"should have the question text '${commonMessages.lossesBroughtForward.title("2013/14")}'" in {
          doc.select("#broughtForwardLosses-question").text shouldBe commonMessages.lossesBroughtForward.title("2013/14")
        }

        "should have the value 'Yes'" in {
          doc.select("#broughtForwardLosses-option span.bold-medium").text shouldBe "Yes"
        }
      }

      "has a numeric output row for brought forward losses value" which {

        s"should have the question text '${commonMessages.lossesBroughtForwardValue.title("2013/14")}'" in {
          doc.select("#broughtForwardLossesValue-question").text shouldBe commonMessages.lossesBroughtForwardValue.title("2013/14")
        }

        "should have the value '£10,000'" in {
          doc.select("#broughtForwardLossesValue-amount span.bold-medium").text shouldBe "£10,000"
        }
      }
    }
  }

  "Deductions Report view with AEA options selected" which {

    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
      BigDecimal(200000),
      BigDecimal(10000),
      BigDecimal(100000),
      BigDecimal(10000),
      BigDecimal(30000))
    lazy val deductionAnswers = ChargeableGainAnswers(Some(ReliefsModel(true)),
      Some(ReliefsValueModel(BigDecimal(50000))),
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(false)),
      Some(AllowableLossesValueModel(10000)),
      Some(LossesBroughtForwardModel(true)),
      Some(LossesBroughtForwardValueModel(10000)),
      Some(AnnualExemptAmountModel(1000)))
    lazy val results = ChargeableGainResultModel(BigDecimal(50000),
      BigDecimal(-11000),
      BigDecimal(0),
      BigDecimal(11000),
      BigDecimal(71000),
      BigDecimal(1000),
      BigDecimal(0),
      Some(BigDecimal(30000)))
    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    lazy val view = views.deductionsSummaryReport(gainAnswers, deductionAnswers, results, taxYearModel)(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)

    "has a numeric output row for the deductions" which {

      "should have the question text 'Deductions'" in {
        doc.select("#deductions-question").text shouldBe messages.deductions
      }

      "has a breakdown that" should {

        "include a value for Reliefs of £30,000" in {
          doc.select("#deductions-amount").text should include("Reliefs £30,000")
        }
      }
    }

    "has a numeric output row for allowable losses remaining" which {

      "should have the question text for an in year loss" in {
        doc.select("#allowableLossRemaining-question").text() shouldBe messages.remainingAllowableLoss("2015/16")
      }

      "should have the value £1000" in {
        doc.select("#allowableLossRemaining-amount").text() should include("£1,000")
      }

      "should have the correct help text" in {
        doc.select("#allowableLossRemaining-amount div span").text() should include(s"${messages.remainingLossHelp} ${messages.remainingLossLink} ${messages.remainingAllowableLossHelp}")
      }
    }

    "has no numeric output row for brought forward losses remaining" in {
      doc.select("#broughtForwardLossRemaining").isEmpty shouldBe true
    }

    "has a numeric output row for AEA value" should {

      s"should have the question text '${commonMessages.annualExemptAmount.title}'" in {
        doc.select("#annualExemptAmount-question").text shouldBe commonMessages.annualExemptAmount.title
      }

      "should have the value '£1,000'" in {
        doc.select("#annualExemptAmount-amount span.bold-medium").text shouldBe "£1,000"
      }
    }
  }


  "Report when supplied with a date above the known tax years" should {

    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2018),
      BigDecimal(200000),
      BigDecimal(10000),
      BigDecimal(100000),
      BigDecimal(10000),
      BigDecimal(30000))
    lazy val deductionAnswers = ChargeableGainAnswers(Some(ReliefsModel(true)),
      Some(ReliefsValueModel(BigDecimal(50000))),
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(true)),
      Some(AllowableLossesValueModel(10000)),
      Some(LossesBroughtForwardModel(true)),
      Some(LossesBroughtForwardValueModel(10000)),
      Some(AnnualExemptAmountModel(1000)))
    lazy val results = ChargeableGainResultModel(BigDecimal(50000),
      BigDecimal(-11000),
      BigDecimal(0),
      BigDecimal(11000),
      BigDecimal(71000),
      BigDecimal(0),
      BigDecimal(2000),
      Some(BigDecimal(50000)))

    lazy val taxYearModel = TaxYearModel("2017/18", false, "2015/16")

    lazy val view = views.deductionsSummaryReport(gainAnswers, deductionAnswers, results, taxYearModel)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "has a numeric output row for allowable losses remaining" in {
      doc.select("#allowableLossRemaining").isEmpty() shouldBe true
    }

    "has a numeric output row for brought forward losses remaining" which {

      "should have the question text for an out of year loss" in {
        doc.select("#broughtForwardLossRemaining-question").text() shouldBe messages.remainingBroughtForwardLoss("2017/18")
      }

      "should have the value £2000" in {
        doc.select("#broughtForwardLossRemaining-amount").text() should include("£2,000")
      }

      "should have the correct help text" in {
        doc.select("#broughtForwardLossRemaining-amount div span").text() should include(s"${messages.remainingLossHelp} ${messages.remainingLossLink} ${messages.remainingBroughtForwardLossHelp}")
      }
    }

    "does not display the section for what to do next" in {
      doc.select("#whatToDoNext").isEmpty shouldBe true
    }
  }
}

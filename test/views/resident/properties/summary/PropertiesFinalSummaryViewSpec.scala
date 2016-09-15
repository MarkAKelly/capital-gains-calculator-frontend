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

package views.resident.properties.summary

import assets.MessageLookup.{summaryPage => messages}
import common.Dates
import controllers.helpers.FakeRequestHelper
import models.resident._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{summary => views}
import assets.{MessageLookup => commonMessages}
import controllers.resident.properties.routes
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel, PreviousTaxableGainsModel}
import models.resident.properties._

class PropertiesFinalSummaryViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Final Summary view" should {
    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
      BigDecimal(200000),
      BigDecimal(10000),
      BigDecimal(100000),
      BigDecimal(10000),
      BigDecimal(30000),
      true)
    lazy val deductionAnswers = ChargeableGainAnswers(
      Some(OtherPropertiesModel(false)),
      None,
      None,
      Some(LossesBroughtForwardModel(false)),
      None,
      None,
      Some(PropertyLivedInModel(false)),
      None,
      None,
      None,
      None
    )
    lazy val incomeAnswers = IncomeAnswersModel(None, Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))
    lazy val results = TotalGainAndTaxOwedModel(
      50000,
      20000,
      0,
      30000,
      3600,
      30000,
      18,
      None,
      None,
      Some(BigDecimal(0)),
      Some(BigDecimal(0)),
      0,
      0
    )
    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
    lazy val backLink = "/calculate-your-capital-gains/resident/properties/personal-allowance"
    lazy val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel)(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    s"have a back button" which {

      lazy val backLink = doc.getElementById("back-link")

      "has the id 'back-link'" in {
        backLink.attr("id") shouldBe "back-link"
      }

      s"has the text '${commonMessages.calcBaseBack}'" in {
        backLink.text shouldBe commonMessages.calcBaseBack
      }

      s"has a link to '${routes.IncomeController.personalAllowance().toString()}'" in {
        backLink.attr("href") shouldBe routes.IncomeController.personalAllowance().toString
      }

    }

    s"have a page heading" which {

      s"includes a secondary heading with text '${messages.pageHeading}'" in {
        doc.select("h1 span.pre-heading").text shouldBe messages.pageHeading
      }

      "includes an amount of tax due of £3,600.00" in {
        doc.select("h1").text should include ("£3,600.00")
      }
    }

    "does not have a notice summary" in {
      doc.select("div.notice-wrapper").isEmpty shouldBe true
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

        "should have the question text 'Total gain'" in {
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

        "should have the value '£0'" in {
          doc.select("#deductions-amount").text should include("£0")
        }

        "has a breakdown that" should {

          "include a value for PRR of £0" in {
            doc.select("#deductions-amount").text should include("Private Residence Relief used £0")
          }

          "include a value for Reliefs of £0" in {
            doc.select("#deductions-amount").text should include(s"${messages.lettingReliefsUsed} £0")
          }

          "include a value for Allowable Losses of £0" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsAllowableLossesUsed("2015/16")} £0")
          }

          "include a value for Capital gains tax allowance used of £0" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsCapitalGainsTax} £0")
          }

          "include a value for Loss brought forward of £0" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsLossBeforeYearUsed("2015/16")} £0")
          }
        }
      }

      "has a numeric output row for the chargeable gain" which {

        "should have the question text 'Taxable gain'" in {
          doc.select("#chargeableGain-question").text shouldBe messages.chargeableGain
        }

        "should have the value '£20,000'" in {
          doc.select("#chargeableGain-amount").text should include("£20,000")
        }
      }

      "has a numeric output row and a tax rate" which {

        "Should have the question text 'Tax Rate'" in {
          doc.select("#gainAndRate-question").text shouldBe messages.taxRate
        }

        "Should have the value £30,000" in {
          doc.select("#firstBand").text should include("£30,000")
        }
        "Should have the tax rate 18%" in {
          doc.select("#firstBand").text should include("18%")
        }
      }

      "has a numeric output row for the AEA remaining" which {

        "should have the question text 'Capital Gains Tax allowance left for 2015/16" in {
          doc.select("#aeaRemaining-question").text should include(messages.aeaRemaining("2015/16"))
        }

        "include a value for Capital gains tax allowance left of £0" in {
          doc.select("#aeaRemaining-amount").text should include("£0")
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

        s"should have a change link to ${routes.GainController.disposalDate().url}" in {
          doc.select("#disposalDate-date a").attr("href") shouldBe routes.GainController.disposalDate().url
        }

        "has the question as part of the link" in {
          doc.select("#disposalDate-date a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.disposalDate.question}"
        }

        "has the question component of the link is visuallyhidden" in {
          doc.select("#disposalDate-date a span.visuallyhidden").text shouldBe commonMessages.disposalDate.question
        }
      }

      "has an option output row for sell or give away" which {

        s"should have the question text '${commonMessages.propertiesSellOrGiveAway.title}'" in {
          doc.select("#sellOrGiveAway-question").text shouldBe commonMessages.propertiesSellOrGiveAway.title
        }

        "should have the value 'Gave it away'" in {
          doc.select("#sellOrGiveAway-option span.bold-medium").text shouldBe "Gave it away"
        }

        s"should have a change link to ${routes.GainController.sellOrGiveAway().url}" in {
          doc.select("#sellOrGiveAway-option a").attr("href") shouldBe routes.GainController.sellOrGiveAway().url
        }

        "has the question as part of the link" in {
          doc.select("#sellOrGiveAway-option a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.propertiesSellOrGiveAway.title}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#sellOrGiveAway-option a span.visuallyhidden").text shouldBe commonMessages.propertiesSellOrGiveAway.title
        }
      }

      "has a numeric output row for the Disposal Value" which {

        s"should have the question text '${commonMessages.disposalValue.question}'" in {
          doc.select("#disposalValue-question").text shouldBe commonMessages.disposalValue.question
        }

        "should have the value '£200,000'" in {
          doc.select("#disposalValue-amount span.bold-medium").text shouldBe "£200,000"
        }

        s"should have a change link to ${routes.GainController.disposalValue().url}" in {
          doc.select("#disposalValue-amount a").attr("href") shouldBe routes.GainController.disposalValue().url
        }

      }

      "has a numeric output row for the Disposal Costs" which {

        s"should have the question text '${commonMessages.disposalCosts.title}'" in {
          doc.select("#disposalCosts-question").text shouldBe commonMessages.disposalCosts.title
        }

        "should have the value '£10,000'" in {
          doc.select("#disposalCosts-amount span.bold-medium").text shouldBe "£10,000"
        }

        s"should have a change link to ${routes.GainController.disposalCosts().url}" in {
          doc.select("#disposalCosts-amount a").attr("href") shouldBe routes.GainController.disposalCosts().url
        }

      }

      "has a numeric output row for the Acquisition Value" which {

        s"should have the question text '${commonMessages.acquisitionValue.title}'" in {
          doc.select("#acquisitionValue-question").text shouldBe commonMessages.acquisitionValue.title
        }

        "should have the value '£100,000'" in {
          doc.select("#acquisitionValue-amount span.bold-medium").text shouldBe "£100,000"
        }

        s"should have a change link to ${routes.GainController.acquisitionValue().url}" in {
          doc.select("#acquisitionValue-amount a").attr("href") shouldBe routes.GainController.acquisitionValue().url
        }

      }

      "has a numeric output row for the Acquisition Costs" which {

        s"should have the question text '${commonMessages.acquisitionCosts.title}'" in {
          doc.select("#acquisitionCosts-question").text shouldBe commonMessages.acquisitionCosts.title
        }

        "should have the value '£10,000'" in {
          doc.select("#acquisitionCosts-amount span.bold-medium").text shouldBe "£10,000"
        }

        s"should have a change link to ${routes.GainController.acquisitionCosts().url}" in {
          doc.select("#acquisitionCosts-amount a").attr("href") shouldBe routes.GainController.acquisitionCosts().url
        }

      }

      "has a numeric output row for the Improvements" which {

        s"should have the question text '${commonMessages.improvementsView.title}'" in {
          doc.select("#improvements-question").text shouldBe commonMessages.improvementsView.title
        }

        "should have the value '£30,000'" in {
          doc.select("#improvements-amount span.bold-medium").text shouldBe "£30,000"
        }

        s"should have a change link to ${routes.GainController.improvements().url}" in {
          doc.select("#improvements-amount a").attr("href") shouldBe routes.GainController.improvements().url
        }
      }

      "has an option output row for property lived in" which {

        s"should have the question text '${commonMessages.propertyLivedIn.title}'" in {
          doc.select("#propertyLivedIn-question").text shouldBe commonMessages.propertyLivedIn.title
        }

        "should have the value 'No'" in {
          doc.select("#propertyLivedIn-option span.bold-medium").text shouldBe "No"
        }

        s"should have a change link to ${routes.DeductionsController.propertyLivedIn().url}" in {
          doc.select("#propertyLivedIn-option a").attr("href") shouldBe routes.DeductionsController.propertyLivedIn().url
        }

        "has the question as part of the link" in {
          doc.select("#propertyLivedIn-option a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.propertyLivedIn.title}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#propertyLivedIn-option a span.visuallyhidden").text shouldBe commonMessages.propertyLivedIn.title
        }
      }

      "does not have an option output row for the eligible for private residence relief" which {

        s"should not display" in {
          doc.select("#privateResidenceRelief-question").size() shouldBe 0
        }
      }

      "does not have an option output row for private residence relief value" which {

        s"should not display" in {
          doc.select("#privateResidenceReliefValue-question").size() shouldBe 0
        }
      }

      "does not have an option output row for the lettings relief" which {

        s"should not display" in {
          doc.select("#lettingsRelief-question").size() shouldBe 0
        }
      }

      "has an option output row for other properties" which {

        s"should have the question text '${commonMessages.otherProperties.title("2015/16")}'" in {
          doc.select("#otherProperties-question").text shouldBe commonMessages.otherProperties.title("2015/16")
        }

        "should have the value 'No'" in {
          doc.select("#otherProperties-option span.bold-medium").text shouldBe "No"
        }

        s"should have a change link to ${routes.DeductionsController.otherProperties().url}" in {
          doc.select("#otherProperties-option a").attr("href") shouldBe routes.DeductionsController.otherProperties().url
        }

        "has the question as part of the link" in {
          doc.select("#otherProperties-option a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.otherProperties.title("2015/16")}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#otherProperties-option a span.visuallyhidden").text shouldBe commonMessages.otherProperties.title("2015/16")
        }
      }

      "has an option output row for brought forward losses" which {

        s"should have the question text '${commonMessages.lossesBroughtForward.title("2015/16")}'" in {
          doc.select("#broughtForwardLosses-question").text shouldBe commonMessages.lossesBroughtForward.title("2015/16")
        }

        "should have the value 'No'" in {
          doc.select("#broughtForwardLosses-option span.bold-medium").text shouldBe "No"
        }

        s"should have a change link to ${routes.DeductionsController.lossesBroughtForward().url}" in {
          doc.select("#broughtForwardLosses-option a").attr("href") shouldBe routes.DeductionsController.lossesBroughtForward().url
        }

        "has the question as part of the link" in {
          doc.select("#broughtForwardLosses-option a").text shouldBe
            s"${commonMessages.calcBaseChange} ${commonMessages.lossesBroughtForward.question("2015/16")}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#broughtForwardLosses-option a span.visuallyhidden").text shouldBe commonMessages.lossesBroughtForward.question("2015/16")
        }
      }
      "has a numeric output row for current income" which {

        s"should have the question text '${commonMessages.currentIncome.title("2015/16")}'" in {
          doc.select("#currentIncome-question").text shouldBe commonMessages.currentIncome.title("2015/16")
        }

        "should have the value '£0'" in {
          doc.select("#currentIncome-amount span.bold-medium").text shouldBe "£0"
        }

        s"should have a change link to ${routes.IncomeController.currentIncome().url}" in {
          doc.select("#currentIncome-amount a").attr("href") shouldBe routes.IncomeController.currentIncome().url
        }
      }
      "has a numeric output row for personal allowance" which {

        s"should have the question text '${commonMessages.personalAllowance.question("2015/16")}'" in {
          doc.select("#personalAllowance-question").text shouldBe commonMessages.personalAllowance.question("2015/16")
        }

        "should have the value '£0'" in {
          doc.select("#personalAllowance-amount span.bold-medium").text shouldBe "£0"
        }

        s"should have a change link to ${routes.IncomeController.personalAllowance().url}" in {
          doc.select("#personalAllowance-amount a").attr("href") shouldBe routes.IncomeController.personalAllowance().url
        }
      }

      "display the save as PDF Button" which {

        "should render only one button" in {
          doc.select("a.save-pdf-button").size() shouldEqual 1
        }

        "with the class save-pdf-button" in {
          doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
        }

        s"with an href to ${controllers.resident.properties.routes.ReportController.gainSummaryReport().toString}" in {
          doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/final-report"
        }

        s"have the text ${messages.saveAsPdf}" in {
          doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
        }
      }
    }
  }

  "Final Summary view with a calculation has some previous taxable gains" should {
    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
      BigDecimal(200000),
      BigDecimal(10000),
      BigDecimal(100000),
      BigDecimal(10000),
      BigDecimal(30000),
      false)
    lazy val deductionAnswers = ChargeableGainAnswers(
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(false)),
      None,
      Some(LossesBroughtForwardModel(false)),
      None,
      Some(AnnualExemptAmountModel(0)),
      Some(PropertyLivedInModel(true)),
      Some(PrivateResidenceReliefModel(false)),
      None,
      None,
      None
    )

    lazy val incomeAnswers = IncomeAnswersModel(Some(PreviousTaxableGainsModel(1000)), Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))

    lazy val results = TotalGainAndTaxOwedModel(
      50000,
      20000,
      0,
      30000,
      3600,
      30000,
      18,
      None,
      None,
      Some(BigDecimal(0)),
      Some(BigDecimal(0)),
      0,
      0
    )

    lazy val taxYearModel = TaxYearModel("2013/14", false, "2015/16")

    lazy val backLink = "/calculate-your-capital-gains/resident/properties/personal-allowance"

    lazy val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel)(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    s"have a back button" which {

      lazy val backLink = doc.getElementById("back-link")

      "has the id 'back-link'" in {
        backLink.attr("id") shouldBe "back-link"
      }

      s"has the text '${commonMessages.calcBaseBack}'" in {
        backLink.text shouldBe commonMessages.calcBaseBack
      }

      s"has a link to '${routes.IncomeController.personalAllowance().toString()}'" in {
        backLink.attr("href") shouldBe routes.IncomeController.personalAllowance().toString
      }

    }

    s"have a page heading" which {

      s"includes a secondary heading with text '${messages.pageHeading}'" in {
        doc.select("h1 span.pre-heading").text shouldBe messages.pageHeading
      }

      "includes an amount of tax due of £3,600.00" in {
        doc.select("h1").text should include ("£3,600.00")
      }
    }

    "has a notice summary that" should {

      "have the class notice-wrapper" in {
        doc.select("div.notice-wrapper").isEmpty shouldBe false
      }

      s"have the text ${messages.noticeWarning("2015/16")}" in {
        doc.select("strong.bold-small").text shouldBe messages.noticeWarning("2015/16")
      }

      "have a warning icon" in {
        doc.select("i.icon-important").isEmpty shouldBe false
      }

      "have a visually hidden warning text" in {
        doc.select("div.notice-wrapper span.visuallyhidden").text shouldBe messages.warning
      }
    }

    s"has a h2 tag" which {

      s"should have the title '${messages.calcDetailsHeadingDate("2013/14")}'" in {
        doc.select("section#calcDetails h2").text shouldBe messages.calcDetailsHeadingDate("2013/14")
      }

      "has the class 'heading-large'" in {
        doc.select("section#calcDetails h2").hasClass("heading-large") shouldBe true
      }
    }

    "has a numeric output row and a tax rate" which {

      "Should have the question text 'Tax Rate'" in {
        doc.select("#gainAndRate-question").text shouldBe messages.taxRate
      }

      "Should have the value £30,000" in {
        doc.select("#firstBand").text should include("£30,000")
      }
      "Should have the tax rate 18%" in {
        doc.select("#firstBand").text should include("18%")
      }
    }

    "has an option output row for sell or give away" which {

      s"should have the question text '${commonMessages.propertiesSellOrGiveAway.title}'" in {
        doc.select("#sellOrGiveAway-question").text shouldBe commonMessages.propertiesSellOrGiveAway.title
      }

      "should have the value 'Sold it'" in {
        doc.select("#sellOrGiveAway-option span.bold-medium").text shouldBe "Sold it"
      }

      s"should have a change link to ${routes.GainController.sellOrGiveAway().url}" in {
        doc.select("#sellOrGiveAway-option a").attr("href") shouldBe routes.GainController.sellOrGiveAway().url
      }

      "has the question as part of the link" in {
        doc.select("#sellOrGiveAway-option a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.propertiesSellOrGiveAway.title}"
      }

      "has the question component of the link as visuallyhidden" in {
        doc.select("#sellOrGiveAway-option a span.visuallyhidden").text shouldBe commonMessages.propertiesSellOrGiveAway.title
      }
    }

    "has an option output row for property lived in" which {

      s"should have the question text '${commonMessages.propertyLivedIn.title}'" in {
        doc.select("#propertyLivedIn-question").text shouldBe commonMessages.propertyLivedIn.title
      }

      "should have the value 'Yes'" in {
        doc.select("#propertyLivedIn-option span.bold-medium").text shouldBe "Yes"
      }

      s"should have a change link to ${routes.DeductionsController.propertyLivedIn().url}" in {
        doc.select("#propertyLivedIn-option a").attr("href") shouldBe routes.DeductionsController.propertyLivedIn().url
      }

      "has the question as part of the link" in {
        doc.select("#propertyLivedIn-option a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.propertyLivedIn.title}"
      }

      "has the question component of the link as visuallyhidden" in {
        doc.select("#propertyLivedIn-option a span.visuallyhidden").text shouldBe commonMessages.propertyLivedIn.title
      }
    }

    "has an option output row for eligible for private residence relief in" which {

      s"should have the question text '${commonMessages.privateResidenceRelief.title}'" in {
        doc.select("#privateResidenceRelief-question").text shouldBe commonMessages.privateResidenceRelief.title
      }

      "should have the value 'No'" in {
        doc.select("#privateResidenceRelief-option span.bold-medium").text shouldBe "No"
      }

      s"should have a change link to ${routes.DeductionsController.privateResidenceRelief().url}" in {
        doc.select("#privateResidenceRelief-option a").attr("href") shouldBe routes.DeductionsController.privateResidenceRelief().url
      }

      "has the question as part of the link" in {
        doc.select("#privateResidenceRelief-option a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.privateResidenceRelief.title}"
      }

      "has the question component of the link as visuallyhidden" in {
        doc.select("#privateResidenceRelief-option a span.visuallyhidden").text shouldBe commonMessages.privateResidenceRelief.title
      }
    }

    "has an option output row for previous taxable gains" which {

      s"should have the question text '${commonMessages.previousTaxableGains.title("2013/14")}'" in {
        doc.select("#previousTaxableGains-question").text shouldBe commonMessages.previousTaxableGains.title("2013/14")
      }

      "should have the value '£1,000'" in {
        doc.select("#previousTaxableGains-amount span.bold-medium").text shouldBe "£1,000"
      }

      s"should have a change link to ${routes.IncomeController.previousTaxableGains().url}" in {
        doc.select("#previousTaxableGains-amount a").attr("href") shouldBe routes.IncomeController.previousTaxableGains().url
      }
    }

    "has an option output row for current income" which {

      s"should have the question text '${commonMessages.currentIncome.title("2013/14")}'" in {
        doc.select("#currentIncome-question").text shouldBe commonMessages.currentIncome.title("2013/14")
      }

      "should have the value '£0'" in {
        doc.select("#currentIncome-amount span.bold-medium").text shouldBe "£0"
      }

      s"should have a change link to ${routes.IncomeController.currentIncome().url}" in {
        doc.select("#currentIncome-amount a").attr("href") shouldBe routes.IncomeController.currentIncome().url
      }
    }
    "has an option output row for personal allowance" which {

      s"should have the question text '${commonMessages.personalAllowance.question("2013/14")}'" in {
        doc.select("#personalAllowance-question").text shouldBe commonMessages.personalAllowance.question("2013/14")
      }

      "should have the value '£0'" in {
        doc.select("#personalAllowance-amount span.bold-medium").text shouldBe "£0"
      }

      s"should have a change link to ${routes.IncomeController.personalAllowance().url}" in {
        doc.select("#personalAllowance-amount a").attr("href") shouldBe routes.IncomeController.personalAllowance().url
      }
    }

    "display the save as PDF Button" which {

      "should render only one button" in {
        doc.select("a.save-pdf-button").size() shouldEqual 1
      }

      "with the class save-pdf-button" in {
        doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
      }

      s"with an href to ${controllers.resident.properties.routes.ReportController.gainSummaryReport().toString}" in {
        doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/final-report"
      }

      s"have the text ${messages.saveAsPdf}" in {
        doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
      }
    }
  }

  "Final Summary view with a calculation that returns tax on both side of the rate boundary" should {
    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
      BigDecimal(200000),
      BigDecimal(10000),
      BigDecimal(100000),
      BigDecimal(10000),
      BigDecimal(30000),
      true)
    lazy val deductionAnswers = ChargeableGainAnswers(
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(false)),
      None,
      Some(LossesBroughtForwardModel(false)),
      None,
      Some(AnnualExemptAmountModel(0)),
      Some(PropertyLivedInModel(true)),
      Some(PrivateResidenceReliefModel(true)),
      Some(PrivateResidenceReliefValueModel(5000)),
      Some(LettingsReliefModel(true)),
      Some(LettingsReliefValueModel(5000))
    )

    lazy val incomeAnswers = IncomeAnswersModel(Some(PreviousTaxableGainsModel(1000)), Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))

    lazy val results = TotalGainAndTaxOwedModel(
      50000,
      20000,
      0,
      30000,
      3600,
      30000,
      18,
      Some(10000),
      Some(28),
      Some(BigDecimal(1000)),
      Some(BigDecimal(2000)),
      5,
      10
    )

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    lazy val backLink = "/calculate-your-capital-gains/resident/properties/personal-allowance"

    lazy val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel)(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    s"have a back button" which {

      lazy val backLink = doc.getElementById("back-link")

      "has the id 'back-link'" in {
        backLink.attr("id") shouldBe "back-link"
      }

      s"has the text '${commonMessages.calcBaseBack}'" in {
        backLink.text shouldBe commonMessages.calcBaseBack
      }

      s"has a link to '${routes.IncomeController.personalAllowance().toString()}'" in {
        backLink.attr("href") shouldBe routes.IncomeController.personalAllowance().toString
      }
    }

    "has a breakdown that" should {

      "include a value for Reliefs of £1,000" in {
        doc.select("#deductions-amount").text should include(s"${messages.lettingReliefsUsed} £1,000")
      }

      "include a value for PRR of £2,000" in {
        doc.select("#deductions-amount").text should include("Private Residence Relief used £2,000")
      }
    }

    "has a numeric output row and a tax rate" which {

      "Should have the question text 'Tax Rate'" in {
        doc.select("#gainAndRate-question").text shouldBe messages.taxRate
      }

      "Should have the value £30,000 in the first band" in {
        doc.select("#firstBand").text should include("£30,000")
      }
      "Should have the tax rate 18% for the first band" in {
        doc.select("#firstBand").text should include("18%")
      }

      "Should have the value £10,000 in the second band" in {
        doc.select("#secondBand").text should include("£10,000")
      }
      "Should have the tax rate 28% for the first band" in {
        doc.select("#secondBand").text should include("28%")
      }
    }

    "has an option output row for eligible for private residence relief in" which {

      s"should have the question text '${commonMessages.privateResidenceRelief.title}'" in {
        doc.select("#privateResidenceRelief-question").text shouldBe commonMessages.privateResidenceRelief.title
      }

      "should have the value 'Yes'" in {
        doc.select("#privateResidenceRelief-option span.bold-medium").text shouldBe "Yes"
      }

      s"should have a change link to ${routes.DeductionsController.privateResidenceRelief().url}" in {
        doc.select("#privateResidenceRelief-option a").attr("href") shouldBe routes.DeductionsController.privateResidenceRelief().url
      }

      "has the question as part of the link" in {
        doc.select("#privateResidenceRelief-option a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.privateResidenceRelief.title}"
      }

      "has the question component of the link as visuallyhidden" in {
        doc.select("#privateResidenceRelief-option a span.visuallyhidden").text shouldBe commonMessages.privateResidenceRelief.title
      }
    }

    "has an option output row for private residence relief value in" which {

      s"should have the question text '${commonMessages.privateResidenceReliefValue.title}'" in {
        doc.select("#privateResidenceReliefValue-question").text shouldBe commonMessages.privateResidenceReliefValue.title
      }

      "should have the value '5000'" in {
        doc.select("#privateResidenceReliefValue-amount span.bold-medium").text shouldBe "£5,000"
      }

      s"should have a change link to ${routes.DeductionsController.privateResidenceReliefValue().url}" in {
        doc.select("#privateResidenceReliefValue-amount a").attr("href") shouldBe routes.DeductionsController.privateResidenceReliefValue().url
      }

      "has the question as part of the link" in {
        doc.select("#privateResidenceReliefValue-amount a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.privateResidenceReliefValue.title}"
      }

      "has the question component of the link as visuallyhidden" in {
        doc.select("#privateResidenceReliefValue-amount a span.visuallyhidden").text shouldBe commonMessages.privateResidenceReliefValue.title
      }
    }

    "has an option output row for eligible for lettings relief in" which {

      s"should have the question text '${commonMessages.lettingsRelief.title}'" in {
        doc.select("#lettingsRelief-question").text shouldBe commonMessages.lettingsRelief.title
      }

      "should have the value 'No'" in {
        doc.select("#lettingsRelief-option span.bold-medium").text shouldBe "Yes"
      }

      s"should have a change link to ${routes.DeductionsController.lettingsRelief().url}" in {
        doc.select("#lettingsRelief-option a").attr("href") shouldBe routes.DeductionsController.lettingsRelief().url
      }

      "has the question as part of the link" in {
        doc.select("#lettingsRelief-option a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.lettingsRelief.title}"
      }

      "has the question component of the link as visuallyhidden" in {
        doc.select("#lettingsRelief-option a span.visuallyhidden").text shouldBe commonMessages.lettingsRelief.title
      }
    }

    "has an option output row for lettings relief value" which {

      s"should have the question text '${commonMessages.lettingsReliefValue.title}'" in {
        doc.select("#lettingsReliefValue-question").text shouldBe commonMessages.lettingsReliefValue.title
      }

      "should have the value '£4500'" in {
        doc.select("#lettingsReliefValue-amount span.bold-medium").text shouldBe "£5,000"
      }

      s"should have a change link to ${routes.DeductionsController.lettingsReliefValue().url}" in {
        doc.select("#lettingsReliefValue-amount a").attr("href") shouldBe routes.DeductionsController.lettingsReliefValue().url
      }

      "has the question as part of the link" in {
        doc.select("#lettingsReliefValue-amount a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.lettingsReliefValue.title}"
      }

      "has the question component of the link as visuallyhidden" in {
        doc.select("#lettingsReliefValue-amount a span.visuallyhidden").text shouldBe commonMessages.lettingsReliefValue.title
      }
    }

    "display the save as PDF Button" which {

      "should render only one button" in {
        doc.select("a.save-pdf-button").size() shouldEqual 1
      }

      "with the class save-pdf-button" in {
        doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
      }

      s"with an href to ${controllers.resident.properties.routes.ReportController.gainSummaryReport().toString}" in {
        doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/final-report"
      }

      s"have the text ${messages.saveAsPdf}" in {
        doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
      }
    }
  }

  "Summary when supplied with a date within the known tax years and tax owed" should {

    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2015),
      BigDecimal(200000),
      BigDecimal(0),
      BigDecimal(0),
      BigDecimal(0),
      BigDecimal(0),
      false)
    lazy val deductionAnswers = ChargeableGainAnswers(
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(false)),
      None,
      Some(LossesBroughtForwardModel(false)),
      None,
      Some(AnnualExemptAmountModel(0)),
      Some(PropertyLivedInModel(false)),
      None,
      None,
      None,
      None
    )

    lazy val incomeAnswers = IncomeAnswersModel(Some(PreviousTaxableGainsModel(0)), Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))

    lazy val results = TotalGainAndTaxOwedModel(
      0,
      0,
      0,
      0,
      0,
      0,
      18,
      Some(0),
      Some(28),
      Some(BigDecimal(0)),
      Some(BigDecimal(0)),
      0,
      0
    )

    lazy val backLink = "/calculate-your-capital-gains/resident/properties/personal-allowance"

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    lazy val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel)(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)

    "display the what to do next section" in {
      doc.select("#whatToDoNext").hasText shouldEqual true
    }

    s"display the title ${messages.whatToDoNextTitle}" in {
      doc.select("#whatToDoNextTitle").text shouldEqual messages.whatToDoNextTitle
    }

    s"display the text ${messages.whatToDoNextTextTwo}" in {
      doc.select("#whatToDoNextText").text shouldEqual s"${messages.whatToDoNextTextTwo} ${commonMessages.calcBaseExternalLink}"
    }

    "have a link" which {

      "should have a href attribute" in {
        doc.select("#whatToDoNextLink").hasAttr("href") shouldEqual true
      }

      "should link to the what-you-pay-on-it govuk page" in {
        doc.select("#whatToDoNextLink").attr("href") shouldEqual "https://www.gov.uk/capital-gains-tax/report-and-pay-capital-gains-tax"
      }

      "have the externalLink attribute" in {
        doc.select("#whatToDoNextLink").hasClass("external-link") shouldEqual true
      }

      "has a visually hidden span with the text opens in a new tab" in {
        doc.select("span#opensInANewTab").text shouldEqual commonMessages.calcBaseExternalLink
      }
    }

    "display the save as PDF Button" which {

      "should render only one button" in {
        doc.select("a.save-pdf-button").size() shouldEqual 1
      }

      "with the class save-pdf-button" in {
        doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
      }

      s"with an href to ${controllers.resident.properties.routes.ReportController.gainSummaryReport().toString}" in {
        doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/final-report"
      }

      s"have the text ${messages.saveAsPdf}" in {
        doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
      }
    }
  }

  "Summary when supplied with a date above the known tax years" should {

    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2018),
      BigDecimal(200000),
      BigDecimal(10000),
      BigDecimal(100000),
      BigDecimal(10000),
      BigDecimal(30000),
      true)
    lazy val deductionAnswers = ChargeableGainAnswers(
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(false)),
      None,
      Some(LossesBroughtForwardModel(false)),
      None,
      Some(AnnualExemptAmountModel(0)),
      Some(PropertyLivedInModel(false)),
      None,
      None,
      None,
      None
    )

    lazy val incomeAnswers = IncomeAnswersModel(Some(PreviousTaxableGainsModel(1000)), Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))

    lazy val results = TotalGainAndTaxOwedModel(
      50000,
      20000,
      0,
      30000,
      3600,
      30000,
      18,
      Some(10000),
      Some(28),
      Some(BigDecimal(0)),
      Some(BigDecimal(0)),
      0,
      0
    )

    lazy val backLink = "/calculate-your-capital-gains/resident/properties/personal-allowance"

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    lazy val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel)(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)

    "does not display the what to do next content" in {
      doc.select("#whatToDoNext").isEmpty shouldBe true
    }

    "display the save as PDF Button" which {

      "should render only one button" in {
        doc.select("a.save-pdf-button").size() shouldEqual 1
      }

      "with the class save-pdf-button" in {
        doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
      }

      s"with an href to ${controllers.resident.properties.routes.ReportController.gainSummaryReport().toString}" in {
        doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/final-report"
      }

      s"have the text ${messages.saveAsPdf}" in {
        doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
      }
    }
  }

  "Summary when supplied with a date in 2016/17" should {

    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
      BigDecimal(200000),
      BigDecimal(10000),
      BigDecimal(100000),
      BigDecimal(10000),
      BigDecimal(30000),
      false)
    lazy val deductionAnswers = ChargeableGainAnswers(
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(false)),
      None,
      Some(LossesBroughtForwardModel(false)),
      None,
      Some(AnnualExemptAmountModel(0)),
      Some(PropertyLivedInModel(false)),
      None,
      None,
      None,
      None
    )

    lazy val incomeAnswers = IncomeAnswersModel(Some(PreviousTaxableGainsModel(1000)), Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))

    lazy val results = TotalGainAndTaxOwedModel(
      50000,
      20000,
      0,
      30000,
      3600,
      30000,
      18,
      Some(10000),
      Some(28),
      Some(BigDecimal(0)),
      Some(BigDecimal(0)),
      0,
      0
    )

    lazy val backLink = "/calculate-your-capital-gains/resident/properties/personal-allowance"

    lazy val taxYearModel = TaxYearModel("2016/17", true, "2016/17")

    lazy val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel)(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)

    "has an option output row for current income" which {

      s"should have the question text '${commonMessages.currentIncome.currentYearTitle}'" in {
        doc.select("#currentIncome-question").text shouldBe commonMessages.currentIncome.currentYearTitle
      }
    }

    "display the save as PDF Button" which {

      "should render only one button" in {
        doc.select("a.save-pdf-button").size() shouldEqual 1
      }

      "with the class save-pdf-button" in {
        doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
      }

      s"with an href to ${controllers.resident.properties.routes.ReportController.gainSummaryReport().toString}" in {
        doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/final-report"
      }

      s"have the text ${messages.saveAsPdf}" in {
        doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
      }
    }
  }

  "Properties Final Summary view" should {

    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2018),
      BigDecimal(200000),
      BigDecimal(10000),
      BigDecimal(100000),
      BigDecimal(10000),
      BigDecimal(30000),
      true)

    lazy val deductionAnswers = ChargeableGainAnswers(
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(false)),
      None,
      Some(LossesBroughtForwardModel(false)),
      None,
      Some(AnnualExemptAmountModel(0)),
      Some(PropertyLivedInModel(false)),
      None,
      None,
      None,
      None)

    lazy val incomeAnswers = IncomeAnswersModel(Some(PreviousTaxableGainsModel(1000)), Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))

    lazy val results = TotalGainAndTaxOwedModel(
      50000,
      20000,
      0,
      30000,
      3600,
      30000,
      18,
      Some(10000),
      Some(28),
      Some(BigDecimal(0)),
      Some(BigDecimal(0)),
      0,
      0
    )

    lazy val backLink = "/calculate-your-capital-gains/resident/properties/personal-allowance"

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    "not have PRR GA metrics when PRR is not in scope" in {
      val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel)(fakeRequestWithSession)
      val doc = Jsoup.parse(view.body)

      doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 0
      doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 0
    }

    "not have lettings relief GA metrics when it is not in scope" in {
      val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel)(fakeRequestWithSession)
      val doc = Jsoup.parse(view.body)

      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 0
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 0
    }

    "have PRR GA metrics when PRR is used" in {
      val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel, Some(true))(fakeRequestWithSession)
      val doc = Jsoup.parse(view.body)

      doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 1
      doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 0
    }

    "not have lettings relief GA metrics when it is used" in {
      val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel, None, Some(true))(fakeRequestWithSession)
      val doc = Jsoup.parse(view.body)

      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 1
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 0
    }

    "have PRR GA metrics when PRR is not used" in {
      val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel, Some(false))(fakeRequestWithSession)
      val doc = Jsoup.parse(view.body)

      doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 0
      doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 1
    }

    "have lettings relief GA metrics when it is not used" in {
      val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel, None, Some(false))(fakeRequestWithSession)
      val doc = Jsoup.parse(view.body)

      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 0
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 1
    }

  }
}

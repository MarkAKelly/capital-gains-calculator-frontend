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
import assets.{MessageLookup => commonMessages}
import common.Dates
import controllers.helpers.FakeRequestHelper
import controllers.resident.properties.routes
import models.resident._
import models.resident.properties._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{summary => views}

class PropertiesDeductionsSummaryViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Properties Deductions Summary view" should {
    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
      None,
      BigDecimal(10000),
      None,
      BigDecimal(10000),
      BigDecimal(30000),
      true,
      None,
      true,
      Some(BigDecimal(5000)),
      None,
      None)

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
    lazy val results = ChargeableGainResultModel(BigDecimal(50000),
      BigDecimal(38900),
      BigDecimal(11100),
      BigDecimal(0),
      BigDecimal(11100),
      BigDecimal(0),
      BigDecimal(0),
      Some(BigDecimal(0)),
      Some(BigDecimal(0)),
      0,
      0
    )
    lazy val backLink = "/calculate-your-capital-gains/resident/properties/losses-brought-forward"

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    lazy val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backLink, taxYearModel)(fakeRequestWithSession)
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

      s"has a link to '${routes.DeductionsController.lossesBroughtForward().toString()}'" in {
        backLink.attr("href") shouldBe routes.DeductionsController.lossesBroughtForward().toString
      }

    }

    s"have a page heading" which {

      s"includes a secondary heading with text '${messages.pageHeading}'" in {
        doc.select("h1 span.pre-heading").text shouldBe messages.pageHeading
      }

      "includes an amount of tax due of £0.00" in {
        doc.select("h1").text should include ("£0.00")
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

          "include a value for PRR of £0" in {
            doc.select("#deductions-amount").text should include("Private Residence Relief used £0")
          }

          "include a value for Reliefs of £0" in {
            doc.select("#deductions-amount").text should include(s"${messages.lettingReliefsUsed} £0")
          }

          "include a value for Allowable Losses of £0" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsAllowableLossesUsed("2015/16")} £0")
          }

          "include a value for Capital gains tax allowance used of £11,100" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsCapitalGainsTax} £11,100")
          }

          "include a value for Loss brought forward of £0" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsLossBeforeYearUsed("2015/16")} £0")
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

        "should have the question text 'Capital Gains Tax allowance left for 2015/16" in {
          doc.select("#aeaRemaining-question").text should include(messages.aeaRemaining("2015/16"))
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

      "has an option output row for owner before april 1982" which {

        s"should have the question text '${commonMessages.Resident.Properties.ownerBeforeAprilNineteenEightyTwo.title}'" in {
          doc.select("#ownerBeforeAprilNineteenEightyTwo-question").text shouldBe commonMessages.Resident.Properties.ownerBeforeAprilNineteenEightyTwo.title
        }

        "should have the value 'Yes'" in {
          doc.select("#ownerBeforeAprilNineteenEightyTwo-option span.bold-medium").text shouldBe "Yes"
        }

        s"should have a change link to ${routes.GainController.ownerBeforeAprilNineteenEightyTwo().url}" in {
          doc.select("#ownerBeforeAprilNineteenEightyTwo-option a").attr("href") shouldBe routes.GainController.ownerBeforeAprilNineteenEightyTwo().url
        }

        "has the question as part of the link" in {
          doc.select("#ownerBeforeAprilNineteenEightyTwo-option a").text shouldBe
            s"${commonMessages.calcBaseChange} ${commonMessages.Resident.Properties.ownerBeforeAprilNineteenEightyTwo.title}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#ownerBeforeAprilNineteenEightyTwo-option a span.visuallyhidden").text shouldBe
            commonMessages.Resident.Properties.ownerBeforeAprilNineteenEightyTwo.title
        }
      }

      "has a numeric output row for the Worth Before April Nineteen Eighty Two" which {

        s"should have the question text '${commonMessages.Resident.Properties.worthOn.question}'" in {
          doc.select("#worthOn-question").text shouldBe commonMessages.Resident.Properties.worthOn.question
        }

        "should have the value '£5,000'" in {
          doc.select("#worthOn-amount span.bold-medium").text shouldBe "£5,000"
        }

        s"should have a change link to ${routes.GainController.worthOn.url}" in {
          doc.select("#worthOn-amount a").attr("href") shouldBe routes.GainController.worthOn.url
        }

        "has the question as part of the link" in {
          doc.select("#worthOn-amount a").text shouldBe
            s"${commonMessages.calcBaseChange} ${commonMessages.Resident.Properties.worthOn.question}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#worthOn-amount a span.visuallyhidden").text shouldBe
            commonMessages.Resident.Properties.worthOn.question
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

        s"should have the question text '${commonMessages.Resident.Properties.improvementsView.questionBefore}'" in {
          doc.select("#improvements-question").text shouldBe commonMessages.Resident.Properties.improvementsView.questionBefore
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

      s"display the text ${messages.whatToDoNextText}" in {
        doc.select("div#whatToDoNextNoLossText").text shouldBe
          s"${messages.whatToDoNextNoLossText} ${messages.whatToDoNextNoLossLinkProperties} ${commonMessages.calcBaseExternalLink} ."
      }

      s"have the link text ${messages.whatToDoNextNoLossLinkProperties}${commonMessages.calcBaseExternalLink}" in {
        doc.select("div#whatToDoNextNoLossText a").text should include(s"${messages.whatToDoNextNoLossLinkProperties}")
      }

      s"have a link to https://www.gov.uk/capital-gains-tax/report-and-pay-capital-gains-tax" in {
        doc.select("div#whatToDoNextNoLossText a").attr("href") shouldBe "https://www.gov.uk/capital-gains-tax/report-and-pay-capital-gains-tax"
      }

      s"have the visually hidden text ${commonMessages.calcBaseExternalLink}" in {
        doc.select("div#whatToDoNextNoLossText span#opensInANewTab").text shouldBe s"${commonMessages.calcBaseExternalLink}"
      }

      "display the save as PDF Button" which {

        "should render only one button" in {
          doc.select("a.save-pdf-button").size() shouldEqual 1
        }

        "with the class save-pdf-button" in {
          doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
        }

        s"with an href to ${controllers.resident.properties.routes.ReportController.deductionsReport().toString}" in {
          doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/deductions-report"
        }

        s"have the text ${messages.saveAsPdf}" in {
          doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
        }
      }
    }
  }

  "Properties Deductions Summary view with all options selected" should {
    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
      None,
      BigDecimal(10000),
      Some(BigDecimal(100000)),
      BigDecimal(10000),
      BigDecimal(30000),
      false,
      Some(true),
      false,
      None,
      Some("Bought"),
      Some(false))

    lazy val deductionAnswers = ChargeableGainAnswers(
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(true)),
      Some(AllowableLossesValueModel(10000)),
      Some(LossesBroughtForwardModel(true)),
      Some(LossesBroughtForwardValueModel(10000)),
      Some(AnnualExemptAmountModel(1000)),
      Some(PropertyLivedInModel(true)),
      Some(PrivateResidenceReliefModel(true)),
      Some(PrivateResidenceReliefValueModel(4000)),
      Some(LettingsReliefModel(true)),
      Some(LettingsReliefValueModel(4500))
    )
    lazy val results = ChargeableGainResultModel(BigDecimal(50000),
      BigDecimal(-11000),
      BigDecimal(0),
      BigDecimal(11000),
      BigDecimal(71000),
      BigDecimal(1000),
      BigDecimal(2000),
      Some(BigDecimal(50000)),
      Some(BigDecimal(1000)),
      10000,
      10000
    )

    lazy val taxYearModel = TaxYearModel("2013/14", false, "2015/16")

    lazy val backLink = "/calculate-your-capital-gains/resident/properties/annual-exempt-amount"
    lazy val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backLink, taxYearModel)(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)

    s"have a back button" which {

      lazy val backLink = doc.getElementById("back-link")

      "has the id 'back-link'" in {
        backLink.attr("id") shouldBe "back-link"
      }

      s"has the text '${commonMessages.calcBaseBack}'" in {
        backLink.text shouldBe commonMessages.calcBaseBack
      }

      s"has a link to '${routes.GainController.improvements().toString()}'" in {
        backLink.attr("href") shouldBe routes.DeductionsController.annualExemptAmount().toString
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

          "include a value for PRR of £1,000" in {
            doc.select("#deductions-amount").text should include("Private Residence Relief used £1,000")
          }

          "include a value for Reliefs of £50,000" in {
            doc.select("#deductions-amount").text should include(s"${messages.lettingReliefsUsed} £50,000")
          }

          "include a value for Allowable Losses of £10,000" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsAllowableLossesUsed("2013/14")} £10,000")
          }

          "include a value for Capital gains tax allowance used of £0" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsCapitalGainsTax} £0")
          }

          "include a value for Loss brought forward of £10,000" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsLossBeforeYearUsed("2013/14")} £10,000")
          }
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

      "has an option output row for sell for less" which {

        s"should have the question text '${commonMessages.sellForLess.title}'" in {
          doc.select("#sellForLess-question").text shouldBe commonMessages.sellForLess.title
        }

        "should have the value 'Yes'" in {
          doc.select("#sellForLess-option span.bold-medium").text shouldBe "Yes"
        }

        s"should have a change link to ${routes.GainController.sellForLess().url}" in {
          doc.select("#sellForLess-option a").attr("href") shouldBe routes.GainController.sellForLess().url
        }

        "has the question as part of the link" in {
          doc.select("#sellForLess-option a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.sellForLess.title}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#sellForLess-option a span.visuallyhidden").text shouldBe commonMessages.sellForLess.title
        }
      }
      "has an option output row for owner before april 1982" which {

        s"should have the question text '${commonMessages.Resident.Properties.ownerBeforeAprilNineteenEightyTwo.title}'" in {
          doc.select("#ownerBeforeAprilNineteenEightyTwo-question").text shouldBe commonMessages.Resident.Properties.ownerBeforeAprilNineteenEightyTwo.title
        }

        "should have the value 'No'" in {
          doc.select("#ownerBeforeAprilNineteenEightyTwo-option span.bold-medium").text shouldBe "No"
        }

        s"should have a change link to ${routes.GainController.ownerBeforeAprilNineteenEightyTwo().url}" in {
          doc.select("#ownerBeforeAprilNineteenEightyTwo-option a").attr("href") shouldBe routes.GainController.ownerBeforeAprilNineteenEightyTwo().url
        }

        "has the question as part of the link" in {
          doc.select("#ownerBeforeAprilNineteenEightyTwo-option a").text shouldBe
            s"${commonMessages.calcBaseChange} ${commonMessages.Resident.Properties.ownerBeforeAprilNineteenEightyTwo.title}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#ownerBeforeAprilNineteenEightyTwo-option a span.visuallyhidden").text shouldBe
            commonMessages.Resident.Properties.ownerBeforeAprilNineteenEightyTwo.title
        }
      }

      "has an output row for how became owner" which {

        s"should have the question text '${commonMessages.howBecameOwner.title}'" in {
          doc.select("#howBecameOwner-question").text shouldBe commonMessages.howBecameOwner.title
        }

        s"should have the value '${commonMessages.howBecameOwner.bought}'" in {
          doc.select("#howBecameOwner-option span.bold-medium").text shouldBe commonMessages.howBecameOwner.bought
        }

        s"should have a change link to ${routes.GainController.howBecameOwner().url}" in {
          doc.select("#howBecameOwner-option a").attr("href") shouldBe routes.GainController.howBecameOwner().url
        }

        "has the question as part of the link" in {
          doc.select("#howBecameOwner-option a").text shouldBe
            s"${commonMessages.calcBaseChange} ${commonMessages.howBecameOwner.title}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#howBecameOwner-option a span.visuallyhidden").text shouldBe
            commonMessages.howBecameOwner.title
        }
      }

      "has an option output row for bought for less than worth" which {

        s"should have the question text '${commonMessages.boughtForLessThanWorth.title}'" in {
          doc.select("#boughtForLessThanWorth-question").text shouldBe commonMessages.boughtForLessThanWorth.title
        }

        "should have the value 'No'" in {
          doc.select("#boughtForLessThanWorth-option span.bold-medium").text shouldBe "No"
        }

        s"should have a change link to ${routes.GainController.boughtForLessThanWorth().url}" in {
          doc.select("#boughtForLessThanWorth-option a").attr("href") shouldBe routes.GainController.boughtForLessThanWorth().url
        }

        "has the question as part of the link" in {
          doc.select("#boughtForLessThanWorth-option a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.boughtForLessThanWorth.title}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#boughtForLessThanWorth-option a span.visuallyhidden").text shouldBe commonMessages.boughtForLessThanWorth.title
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

      "has a numeric output row for the Improvements" which {

        s"should have the question text '${commonMessages.Resident.Properties.improvementsView.question}'" in {
          doc.select("#improvements-question").text shouldBe commonMessages.Resident.Properties.improvementsView.question
        }

        "should have the value '£30,000'" in {
          doc.select("#improvements-amount span.bold-medium").text shouldBe "£30,000"
        }

        s"should have a change link to ${routes.GainController.improvements().url}" in {
          doc.select("#improvements-amount a").attr("href") shouldBe routes.GainController.improvements().url
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
          doc.select("#allowableLossRemaining-amount div span").text() should
            include(s"${messages.remainingLossHelp} ${messages.remainingLossLink} " +
              s"${commonMessages.calcBaseExternalLink} ${messages.remainingAllowableLossHelp}")
        }

        "should have a link in the help text to https://www.gov.uk/capital-gains-tax/losses" in {
          doc.select("#allowableLossRemaining-amount div span a").attr("href") shouldBe "https://www.gov.uk/capital-gains-tax/losses"
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
          doc.select("#broughtForwardLossRemaining-amount div span").text() should
            include(s"${messages.remainingLossHelp} ${messages.remainingLossLink} " +
              s"${commonMessages.calcBaseExternalLink} ${messages.remainingBroughtForwardLossHelp}")
        }

        "should have a link in the help text to https://www.gov.uk/capital-gains-tax/losses" in {
          doc.select("#broughtForwardLossRemaining-amount div span a").attr("href") shouldBe "https://www.gov.uk/capital-gains-tax/losses"
        }
      }
    }

    "has a numeric output row for the AEA remaining" which {

      "should have the question text 'Capital Gains Tax allowance left for 2015/16" in {
        doc.select("#aeaRemaining-question").text should include(messages.aeaRemaining("2015/16"))
      }

      "include a value for Capital gains tax allowance left of £11,000" in {
        doc.select("#aeaRemaining-amount span.bold-medium").text should include("£11,000")
      }

      "include the additional help text for AEA" in {
        doc.select("#aeaRemaining-amount div span").text shouldBe messages.aeaHelp
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

      "has an option output row for lettings relief value" which {

        s"should have the question text '${commonMessages.lettingsReliefValue.title}'" in {
          doc.select("#lettingsReliefValue-question").text shouldBe commonMessages.lettingsReliefValue.title
        }

        "should have the value '£4500'" in {
          doc.select("#lettingsReliefValue-amount span.bold-medium").text shouldBe "£4,500"
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

      "has an option output row for other properties" which {

        s"should have the question text '${commonMessages.otherProperties.title("2013/14")}'" in {
          doc.select("#otherProperties-question").text shouldBe commonMessages.otherProperties.title("2013/14")
        }

        "should have the value 'Yes'" in {
          doc.select("#otherProperties-option span.bold-medium").text shouldBe "Yes"
        }

        s"should have a change link to ${routes.DeductionsController.otherProperties().url}" in {
          doc.select("#otherProperties-option a").attr("href") shouldBe routes.DeductionsController.otherProperties().url
        }

        "has the question as part of the link" in {
          doc.select("#otherProperties-option a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.otherProperties.title("2013/14")}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#otherProperties-option a span.visuallyhidden").text shouldBe commonMessages.otherProperties.title("2013/14")
        }
      }

      "has an option output row for allowable losses" which {

        s"should have the question text '${commonMessages.allowableLosses.title("2013/14")}'" in {
          doc.select("#allowableLosses-question").text shouldBe commonMessages.allowableLosses.title("2013/14")
        }

        "should have the value 'Yes'" in {
          doc.select("#allowableLosses-option span.bold-medium").text shouldBe "Yes"
        }

        s"should have a change link to ${routes.DeductionsController.allowableLosses().url}" in {
          doc.select("#allowableLosses-option a").attr("href") shouldBe routes.DeductionsController.allowableLosses().url
        }

        "has the question as part of the link" in {
          doc.select("#allowableLosses-option a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.allowableLosses.title("2013/14")}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#allowableLosses-option a span.visuallyhidden").text shouldBe commonMessages.allowableLosses.title("2013/14")
        }
      }

      "has a numeric output row for allowable losses value" which {

        s"should have the question text '${commonMessages.allowableLossesValue.title("2013/14")}'" in {
          doc.select("#allowableLossesValue-question").text shouldBe commonMessages.allowableLossesValue.title("2013/14")
        }

        "should have the value '£10,000'" in {
          doc.select("#allowableLossesValue-amount span.bold-medium").text shouldBe "£10,000"
        }

        s"should have a change link to ${routes.DeductionsController.allowableLossesValue().url}" in {
          doc.select("#allowableLossesValue-amount a").attr("href") shouldBe routes.DeductionsController.allowableLossesValue().url
        }
      }

      "has an option output row for brought forward losses" which {

        s"should have the question text '${commonMessages.lossesBroughtForward.title("2013/14")}'" in {
          doc.select("#broughtForwardLosses-question").text shouldBe commonMessages.lossesBroughtForward.title("2013/14")
        }

        "should have the value 'Yes'" in {
          doc.select("#broughtForwardLosses-option span.bold-medium").text shouldBe "Yes"
        }

        s"should have a change link to ${routes.DeductionsController.lossesBroughtForward().url}" in {
          doc.select("#broughtForwardLosses-option a").attr("href") shouldBe routes.DeductionsController.lossesBroughtForward().url
        }

        "has the question as part of the link" in {
          doc.select("#broughtForwardLosses-option a").text shouldBe
            s"${commonMessages.calcBaseChange} ${commonMessages.lossesBroughtForward.question("2013/14")}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#broughtForwardLosses-option a span.visuallyhidden").text shouldBe commonMessages.lossesBroughtForward.question("2013/14")
        }
      }

      "has a numeric output row for brought forward losses value" which {

        s"should have the question text '${commonMessages.lossesBroughtForwardValue.title("2013/14")}'" in {
          doc.select("#broughtForwardLossesValue-question").text shouldBe commonMessages.lossesBroughtForwardValue.title("2013/14")
        }

        "should have the value '£10,000'" in {
          doc.select("#broughtForwardLossesValue-amount span.bold-medium").text shouldBe "£10,000"
        }

        s"should have a change link to ${routes.DeductionsController.lossesBroughtForwardValue().url}" in {
          doc.select("#broughtForwardLossesValue-amount a").attr("href") shouldBe routes.DeductionsController.lossesBroughtForwardValue().url
        }
      }

      "display the save as PDF Button" which {

        "should render only one button" in {
          doc.select("a.save-pdf-button").size() shouldEqual 1
        }

        "with the class save-pdf-button" in {
          doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
        }

        s"with an href to ${controllers.resident.properties.routes.ReportController.deductionsReport().toString}" in {
          doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/deductions-report"
        }

        s"have the text ${messages.saveAsPdf}" in {
          doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
        }
      }
    }
  }

  "Properties Deductions Summary view when property was sold for less than worth" should {
    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
      None,
      BigDecimal(10000),
      None,
      BigDecimal(10000),
      BigDecimal(30000),
      false,
      Some(true),
      false,
      None,
      Some("Bought"),
      Some(true))
    lazy val deductionAnswers = ChargeableGainAnswers(
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(true)),
      Some(AllowableLossesValueModel(10000)),
      Some(LossesBroughtForwardModel(true)),
      Some(LossesBroughtForwardValueModel(10000)),
      Some(AnnualExemptAmountModel(1000)),
      Some(PropertyLivedInModel(true)),
      Some(PrivateResidenceReliefModel(true)),
      Some(PrivateResidenceReliefValueModel(4000)),
      Some(LettingsReliefModel(true)),
      Some(LettingsReliefValueModel(4500))
    )
    lazy val results = ChargeableGainResultModel(BigDecimal(50000),
      BigDecimal(-11000),
      BigDecimal(0),
      BigDecimal(11000),
      BigDecimal(71000),
      BigDecimal(1000),
      BigDecimal(2000),
      Some(BigDecimal(50000)),
      Some(BigDecimal(1000)),
      10000,
      10000
    )

    lazy val taxYearModel = TaxYearModel("2013/14", false, "2015/16")

    lazy val backLink = "/calculate-your-capital-gains/resident/properties/annual-exempt-amount"
    lazy val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backLink, taxYearModel)(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)

    "has an option output row for bought for less than worth" which {

      s"should have the question text '${commonMessages.boughtForLessThanWorth.title}'" in {
        doc.select("#boughtForLessThanWorth-question").text shouldBe commonMessages.boughtForLessThanWorth.title
      }

      "should have the value 'Yes'" in {
        doc.select("#boughtForLessThanWorth-option span.bold-medium").text shouldBe "Yes"
      }

      s"should have a change link to ${routes.GainController.boughtForLessThanWorth().url}" in {
        doc.select("#boughtForLessThanWorth-option a").attr("href") shouldBe routes.GainController.boughtForLessThanWorth().url
      }

      "has the question as part of the link" in {
        doc.select("#boughtForLessThanWorth-option a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.boughtForLessThanWorth.title}"
      }

      "has the question component of the link as visuallyhidden" in {
        doc.select("#boughtForLessThanWorth-option a span.visuallyhidden").text shouldBe commonMessages.boughtForLessThanWorth.title
      }
    }
  }

  "Properties Deductions Summary view with AEA options selected" which {

    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
      None,
      BigDecimal(10000),
      None,
      BigDecimal(10000),
      BigDecimal(30000),
      true,
      None,
      true,
      Some(BigDecimal(5000)),
      None,
      None)

    lazy val deductionAnswers = ChargeableGainAnswers(
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(false)),
      Some(AllowableLossesValueModel(10000)),
      Some(LossesBroughtForwardModel(true)),
      Some(LossesBroughtForwardValueModel(10000)),
      Some(AnnualExemptAmountModel(1000)),
      Some(PropertyLivedInModel(true)),
      Some(PrivateResidenceReliefModel(true)),
      Some(PrivateResidenceReliefValueModel(5000)),
      Some(LettingsReliefModel(true)),
      Some(LettingsReliefValueModel(4500))
    )
    lazy val results = ChargeableGainResultModel(BigDecimal(50000),
      BigDecimal(-11000),
      BigDecimal(0),
      BigDecimal(11000),
      BigDecimal(71000),
      BigDecimal(1000),
      BigDecimal(0),
      Some(BigDecimal(30000)),
      Some(BigDecimal(1500)),
      10000,
      10000
    )
    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    lazy val backLink = "/calculate-your-capital-gains/resident/properties/annual-exempt-amount"
    lazy val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backLink, taxYearModel)(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)

    "has a numeric output row for the deductions" which {

      "should have the question text 'Deductions'" in {
        doc.select("#deductions-question").text shouldBe messages.deductions
      }

      "has a breakdown that" should {

        "include a value for Reliefs of £30,000" in {
          doc.select("#deductions-amount").text should include(s"${messages.lettingReliefsUsed} £30,000")
        }

        "include a value for PRR of £1,500" in {
          doc.select("#deductions-amount").text should include("Private Residence Relief used £1,500")
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
        doc.select("#allowableLossRemaining-amount div span").text() should
          include(s"${messages.remainingLossHelp} ${messages.remainingLossLink} " +
            s"${commonMessages.calcBaseExternalLink} ${messages.remainingAllowableLossHelp}")
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

      "should have the value 'Yes'" in {
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

      s"should have a change link to ${routes.DeductionsController.annualExemptAmount().url}" in {
        doc.select("#annualExemptAmount-amount a").attr("href") shouldBe routes.DeductionsController.annualExemptAmount().url
      }

      s"display the text ${messages.whatToDoNextText}" in {
        doc.select("div#whatToDoNextNoLossText").text shouldBe
          s"${messages.whatToDoNextNoLossText} ${messages.whatToDoNextNoLossLinkProperties} ${commonMessages.calcBaseExternalLink} ${messages.whatToDoNextLossRemaining}."
      }

      s"have the link text ${messages.whatToDoNextNoLossLinkProperties}${commonMessages.calcBaseExternalLink}" in {
        doc.select("div#whatToDoNextNoLossText a").text should include(s"${messages.whatToDoNextNoLossLinkProperties}")
      }

      s"have a link to https://www.gov.uk/capital-gains-tax/report-and-pay-capital-gains-tax" in {
        doc.select("div#whatToDoNextNoLossText a").attr("href") shouldBe "https://www.gov.uk/capital-gains-tax/losses"
      }

      s"have the visually hidden text ${commonMessages.calcBaseExternalLink}" in {
        doc.select("div#whatToDoNextNoLossText span#opensInANewTab").text shouldBe s"${commonMessages.calcBaseExternalLink}"
      }
    }

    "display the save as PDF Button" which {

      "should render only one button" in {
        doc.select("a.save-pdf-button").size() shouldEqual 1
      }

      "with the class save-pdf-button" in {
        doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
      }

      s"with an href to ${controllers.resident.properties.routes.ReportController.deductionsReport().toString}" in {
        doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/deductions-report"
      }

      s"have the text ${messages.saveAsPdf}" in {
        doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
      }
    }
  }

  "Properties Deductions Summary when supplied with a date within the known tax years and no gain or loss" should {


    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
      None,
      BigDecimal(0),
      None,
      BigDecimal(0),
      BigDecimal(0),
      true,
      None,
      false,
      None,
      Some("Inherited"),
      None)

    lazy val deductionAnswers = ChargeableGainAnswers(
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(true)),
      Some(AllowableLossesValueModel(0)),
      Some(LossesBroughtForwardModel(true)),
      Some(LossesBroughtForwardValueModel(0)),
      Some(AnnualExemptAmountModel(0)),
      Some(PropertyLivedInModel(false)),
      None,
      None,
      None,
      None
    )
    lazy val results = ChargeableGainResultModel(BigDecimal(0),
      BigDecimal(0),
      BigDecimal(0),
      BigDecimal(0),
      BigDecimal(50000),
      BigDecimal(0),
      BigDecimal(2000),
      Some(BigDecimal(100000)),
      Some(BigDecimal(0)),
      0,
      0
    )

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    lazy val backLink = "/calculate-your-capital-gains/resident/properties/annual-exempt-amount"
    lazy val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backLink, taxYearModel)(fakeRequest)


    lazy val doc = Jsoup.parse(view.body)

    "has a numeric output row for allowable losses remaining" in {
      doc.select("#allowableLossRemaining").isEmpty shouldBe true
    }

    "has a numeric output row for brought forward losses remaining" which {

      "should have the question text for an out of year loss" in {
        doc.select("#broughtForwardLossRemaining-question").text() shouldBe messages.remainingBroughtForwardLoss("2015/16")
      }

      "should have the value £2000" in {
        doc.select("#broughtForwardLossRemaining-amount").text() should include("£2,000")
      }

      "should have the correct help text" in {
        doc.select("#broughtForwardLossRemaining-amount div span").text() should
          include(s"${messages.remainingLossHelp} ${messages.remainingLossLink} " +
            s"${commonMessages.calcBaseExternalLink} ${messages.remainingBroughtForwardLossHelp}")
      }
    }

    "has an output row for how became owner" which {

      s"should have the question text '${commonMessages.howBecameOwner.title}'" in {
        doc.select("#howBecameOwner-question").text shouldBe commonMessages.howBecameOwner.title
      }

      s"should have the value '${commonMessages.howBecameOwner.inherited}'" in {
        doc.select("#howBecameOwner-option span.bold-medium").text shouldBe commonMessages.howBecameOwner.inherited
      }

      s"should have a change link to ${routes.GainController.howBecameOwner().url}" in {
        doc.select("#howBecameOwner-option a").attr("href") shouldBe routes.GainController.howBecameOwner().url
      }

      "has the question as part of the link" in {
        doc.select("#howBecameOwner-option a").text shouldBe
          s"${commonMessages.calcBaseChange} ${commonMessages.howBecameOwner.title}"
      }

      "has the question component of the link as visuallyhidden" in {
        doc.select("#howBecameOwner-option a span.visuallyhidden").text shouldBe
          commonMessages.howBecameOwner.title
      }
    }

    "display the what to do next section" in {
      doc.select("#whatToDoNext").hasText shouldEqual true
    }

    s"display the title ${messages.whatToDoNextTitle}" in {
      doc.select("h2#whatToDoNextNoLossTitle").text shouldEqual messages.whatToDoNextTitle
    }

    s"display the text ${messages.whatToDoNextText}" in {
      doc.select("div#whatToDoNextNoLossText").text shouldBe
        s"${messages.whatToDoNextNoLossText} ${messages.whatToDoNextNoLossLinkProperties} ${commonMessages.calcBaseExternalLink} ${messages.whatToDoNextLossRemaining}."
    }

    s"have the link text ${messages.whatToDoNextNoLossLinkProperties}${commonMessages.calcBaseExternalLink}" in {
      doc.select("div#whatToDoNextNoLossText a").text should include(s"${messages.whatToDoNextNoLossLinkProperties}")
    }

    s"have a link to https://www.gov.uk/capital-gains-tax/report-and-pay-capital-gains-tax" in {
      doc.select("div#whatToDoNextNoLossText a").attr("href") shouldBe "https://www.gov.uk/capital-gains-tax/losses"
    }

    s"have the visually hidden text ${commonMessages.calcBaseExternalLink}" in {
      doc.select("div#whatToDoNextNoLossText span#opensInANewTab").text shouldBe s"${commonMessages.calcBaseExternalLink}"
    }

    "display the save as PDF Button" which {

      "should render only one button" in {
        doc.select("a.save-pdf-button").size() shouldEqual 1
      }

      "with the class save-pdf-button" in {
        doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
      }

      s"with an href to ${controllers.resident.properties.routes.ReportController.deductionsReport().toString}" in {
        doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/deductions-report"
      }

      s"have the text ${messages.saveAsPdf}" in {
        doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
      }
    }
  }

  "Properties Deductions Summary when supplied with a date above the known tax years" should {

    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2018),
      Some(BigDecimal(200000)),
      BigDecimal(10000),
      None,
      BigDecimal(10000),
      BigDecimal(30000),
      false,
      Some(false),
      true,
      Some(BigDecimal(5000)),
      None,
      None)

    lazy val deductionAnswers = ChargeableGainAnswers(
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(true)),
      Some(AllowableLossesValueModel(10000)),
      Some(LossesBroughtForwardModel(true)),
      Some(LossesBroughtForwardValueModel(10000)),
      Some(AnnualExemptAmountModel(1000)),
      Some(PropertyLivedInModel(false)),
      None,
      None,
      None,
      None)
    lazy val results = ChargeableGainResultModel(BigDecimal(50000),
      BigDecimal(-11000),
      BigDecimal(0),
      BigDecimal(11000),
      BigDecimal(71000),
      BigDecimal(0),
      BigDecimal(0),
      Some(BigDecimal(50000)),
      Some(BigDecimal(0)),
      10000,
      10000
    )

    lazy val taxYearModel = TaxYearModel("2017/18", false, "2015/16")

    lazy val backLink = "/calculate-your-capital-gains/resident/properties/annual-exempt-amount"
    lazy val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backLink, taxYearModel)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "does not display the section for what to do next" in {
      doc.select("#whatToDoNext").isEmpty shouldBe true
    }

    "display the save as PDF Button" which {

      "should render only one button" in {
        doc.select("a.save-pdf-button").size() shouldEqual 1
      }

      "with the class save-pdf-button" in {
        doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
      }

      s"with an href to ${controllers.resident.properties.routes.ReportController.deductionsReport().toString}" in {
        doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/deductions-report"
      }

      s"have the text ${messages.saveAsPdf}" in {
        doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
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

    "has an option output row for sell for less" which {

      s"should have the question text '${commonMessages.sellForLess.title}'" in {
        doc.select("#sellForLess-question").text shouldBe commonMessages.sellForLess.title
      }

      "should have the value 'No'" in {
        doc.select("#sellForLess-option span.bold-medium").text shouldBe "No"
      }

      s"should have a change link to ${routes.GainController.sellForLess().url}" in {
        doc.select("#sellForLess-option a").attr("href") shouldBe routes.GainController.sellForLess().url
      }

      "has the question as part of the link" in {
        doc.select("#sellForLess-option a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.sellForLess.title}"
      }

      "has the question component of the link as visuallyhidden" in {
        doc.select("#sellForLess-option a span.visuallyhidden").text shouldBe commonMessages.sellForLess.title
      }
    }
  }

  "Properties Deductions Summary view" should {

    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
      None,
      BigDecimal(10000),
      None,
      BigDecimal(10000),
      BigDecimal(30000),
      true,
      Some(false),
      false,
      None,
      Some("Gifted"),
      None)

    lazy val deductionAnswers = ChargeableGainAnswers(
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(false)),
      Some(AllowableLossesValueModel(10000)),
      Some(LossesBroughtForwardModel(true)),
      Some(LossesBroughtForwardValueModel(10000)),
      Some(AnnualExemptAmountModel(1000)),
      Some(PropertyLivedInModel(true)),
      Some(PrivateResidenceReliefModel(true)),
      Some(PrivateResidenceReliefValueModel(5000)),
      Some(LettingsReliefModel(true)),
      Some(LettingsReliefValueModel(2000)))
    lazy val results = ChargeableGainResultModel(BigDecimal(50000),
      BigDecimal(-11000),
      BigDecimal(0),
      BigDecimal(11000),
      BigDecimal(71000),
      BigDecimal(1000),
      BigDecimal(0),
      Some(BigDecimal(30000)),
      Some(BigDecimal(1500)),
      10000,
      10000
    )
    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    lazy val backLink = "/calculate-your-capital-gains/resident/properties/annual-exempt-amount"

    "not have PRR GA metrics when PRR is not in scope" in {
      val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backLink, taxYearModel)(fakeRequestWithSession)
      val doc = Jsoup.parse(view.body)

      doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 0
      doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 0
    }

    "not have lettings relief GA metrics when it is not in scope" in {
      val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backLink, taxYearModel)(fakeRequestWithSession)
      val doc = Jsoup.parse(view.body)

      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 0
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 0
    }

    "have PRR GA metrics when PRR is used" in {
      val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backLink, taxYearModel, Some(true))(fakeRequestWithSession)
      val doc = Jsoup.parse(view.body)

      doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 1
      doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 0
    }

    "not have lettings relief GA metrics when it is used" in {
      val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backLink, taxYearModel, None, Some(true))(fakeRequestWithSession)
      val doc = Jsoup.parse(view.body)

      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 1
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 0
    }

    "have PRR GA metrics when PRR is not used" in {
      val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backLink, taxYearModel, Some(false))(fakeRequestWithSession)
      val doc = Jsoup.parse(view.body)

      doc.select("[data-metrics=\"rtt-properties-summary:prr:yes\"]").size shouldBe 0
      doc.select("[data-metrics=\"rtt-properties-summary:prr:no\"]").size shouldBe 1
    }

    "have lettings relief GA metrics when it is not used" in {
      val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backLink, taxYearModel, None, Some(false))(fakeRequestWithSession)
      val doc = Jsoup.parse(view.body)

      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:yes\"]").size shouldBe 0
      doc.select("[data-metrics=\"rtt-properties-summary:lettingsRelief:no\"]").size shouldBe 1
    }

    "has an output row for how became owner" which {
      lazy val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backLink, taxYearModel, None, Some(false))(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(view.body)

      s"should have the question text '${commonMessages.howBecameOwner.title}'" in {
        doc.select("#howBecameOwner-question").text shouldBe commonMessages.howBecameOwner.title
      }

      s"should have the value '${commonMessages.howBecameOwner.gifted}'" in {
        doc.select("#howBecameOwner-option span.bold-medium").text shouldBe commonMessages.howBecameOwner.gifted
      }

      s"should have a change link to ${routes.GainController.howBecameOwner().url}" in {
        doc.select("#howBecameOwner-option a").attr("href") shouldBe routes.GainController.howBecameOwner().url
      }

      "has the question as part of the link" in {
        doc.select("#howBecameOwner-option a").text shouldBe
          s"${commonMessages.calcBaseChange} ${commonMessages.howBecameOwner.title}"
      }

      "has the question component of the link as visuallyhidden" in {
        doc.select("#howBecameOwner-option a span.visuallyhidden").text shouldBe
          commonMessages.howBecameOwner.title
      }
    }

  }
}

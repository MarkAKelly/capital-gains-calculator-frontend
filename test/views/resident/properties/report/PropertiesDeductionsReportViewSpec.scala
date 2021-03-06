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

import assets.MessageLookup.{SummaryPage => messages}
import assets.{MessageLookup => commonMessages}
import assets.MessageLookup.Resident.{Properties => propertiesMessages}
import common.Dates
import controllers.helpers.FakeRequestHelper
import models.resident._
import models.resident.properties._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{report => views}

class PropertiesDeductionsReportViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Deductions Report view" should {
    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
      None,
      None,
      whoDidYouGiveItTo = Some("Other"),
      worthWhenGaveAway = Some(10000),
      BigDecimal(10000),
      None,
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
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

        s"should have the question text '${commonMessages.DisposalDate.question}'" in {
          doc.select("#disposalDate-question").text shouldBe commonMessages.DisposalDate.question
        }

        "should have the date '10 October 2016'" in {
          doc.select("#disposalDate-date span.bold-medium").text shouldBe "10 October 2016"
        }
      }

      "has an option output row for sell or give away" which {

        s"should have the question text '${commonMessages.PropertiesSellOrGiveAway.title}'" in {
          doc.select("#sellOrGiveAway-question").text shouldBe commonMessages.PropertiesSellOrGiveAway.title
        }

        "should have the value 'Gave it away'" in {
          doc.select("#sellOrGiveAway-option span.bold-medium").text shouldBe "Gave it away"
        }
      }

      //#########################################################################
      "has an option output row for who did you give it to" which {
        s"should have the question text '${commonMessages.WhoDidYouGiveItTo.title}'" in {
          doc.select("#whoDidYouGiveItTo-question").text shouldBe commonMessages.WhoDidYouGiveItTo.title
        }

        "should have the value 'Someone else'" in {
          doc.select("#whoDidYouGiveItTo-option span.bold-medium").text shouldBe "Someone else"
        }
      }

      "has a numeric output row for the Value when you gave it away" which {

        s"should have the question text '${propertiesMessages.PropertiesWorthWhenGaveAway.title}'" in {
          doc.select("#worthWhenGaveAway-question").text shouldBe propertiesMessages.PropertiesWorthWhenGaveAway.title
        }

        "should have the value '£10,000'" in {
          doc.select("#worthWhenGaveAway-amount span.bold-medium").text shouldBe "£10,000"
        }
      }
      //#########################################################################

      "has a numeric output row for the Disposal Costs" which {

        s"should have the question text '${commonMessages.DisposalCosts.title}'" in {
          doc.select("#disposalCosts-question").text shouldBe commonMessages.DisposalCosts.title
        }

        "should have the value '£10,000'" in {
          doc.select("#disposalCosts-amount span.bold-medium").text shouldBe "£10,000"
        }
      }

      "has an option output row for owner before april 1982" which {

        s"should have the question text '${commonMessages.Resident.Properties.OwnerBeforeLegislationStart.title}'" in {
          doc.select("#ownerBeforeLegislationStart-question").text shouldBe commonMessages.Resident.Properties.OwnerBeforeLegislationStart.title
        }

        "should have the value 'Yes'" in {
          doc.select("#ownerBeforeLegislationStart-option span.bold-medium").text shouldBe "Yes"
        }
      }

      "has a numeric output row for the Value Before Legislation Start" which {

        s"should have the question text '${commonMessages.Resident.Properties.ValueBeforeLegislationStart.question}'" in {
          doc.select("#valueBeforeLegislationStart-question").text shouldBe commonMessages.Resident.Properties.ValueBeforeLegislationStart.question
        }

        "should have the value '£5,000'" in {
          doc.select("#valueBeforeLegislationStart-amount span.bold-medium").text shouldBe "£5,000"
        }
      }

      "has a numeric output row for the Acquisition Costs" which {

        s"should have the question text '${commonMessages.AcquisitionCosts.title}'" in {
          doc.select("#acquisitionCosts-question").text shouldBe commonMessages.AcquisitionCosts.title
        }

        "should have the value '£10,000'" in {
          doc.select("#acquisitionCosts-amount span.bold-medium").text shouldBe "£10,000"
        }
      }

      "has a numeric output row for the Improvements" which {

        s"should have the question text '${commonMessages.Resident.Properties.ImprovementsView.questionBefore}'" in {
          doc.select("#improvements-question").text shouldBe commonMessages.Resident.Properties.ImprovementsView.questionBefore
        }

        "should have the value '£30,000'" in {
          doc.select("#improvements-amount span.bold-medium").text shouldBe "£30,000"
        }
      }

      "has an option output row for property lived in" which {

        s"should have the question text '${commonMessages.PropertyLivedIn.title}'" in {
          doc.select("#propertyLivedIn-question").text shouldBe commonMessages.PropertyLivedIn.title
        }

        "should have the value 'No'" in {
          doc.select("#propertyLivedIn-option span.bold-medium").text shouldBe "No"
        }
      }

      "does not have an option output row for the eligible for private residence relief" which {

        s"should not display" in {
          doc.select("#privateResidenceRelief-question").size() shouldBe 0
        }
      }

      "does not have an option output row for the private residence relief value entry" which {

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

        s"should have the question text '${commonMessages.OtherProperties.title("2015/16")}'" in {
          doc.select("#otherProperties-question").text shouldBe commonMessages.OtherProperties.title("2015/16")
        }

        "should have the value 'No'" in {
          doc.select("#otherProperties-option span.bold-medium").text shouldBe "No"
        }
      }

      "has an option output row for brought forward losses" which {

        s"should have the question text '${commonMessages.LossesBroughtForward.title("2015/16")}'" in {
          doc.select("#broughtForwardLosses-question").text shouldBe commonMessages.LossesBroughtForward.title("2015/16")
        }

        "should have the value 'No'" in {
          doc.select("#broughtForwardLosses-option span.bold-medium").text shouldBe "No"
        }
      }
    }
  }

  "Deductions Report view with all options selected" should {
    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
      None,
      Some(500),
      whoDidYouGiveItTo = None,
      worthWhenGaveAway = None,
      BigDecimal(10000),
      Some(BigDecimal(100000)),
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      BigDecimal(10000),
      BigDecimal(30000),
      false,
      Some(true),
      false,
      None,
      Some("Bought"),
      Some(false)
    )

    lazy val deductionAnswers = ChargeableGainAnswers(
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(true)),
      Some(AllowableLossesValueModel(10000)),
      Some(LossesBroughtForwardModel(true)),
      Some(LossesBroughtForwardValueModel(10000)),
      Some(AnnualExemptAmountModel(1000)),
      Some(PropertyLivedInModel(true)),
      Some(PrivateResidenceReliefModel(true)),
      Some(PrivateResidenceReliefValueModel(1000)),
      Some(LettingsReliefModel(true)),
      Some(LettingsReliefValueModel(6000))
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

      "has an amount output row for worth when sold for less" which {

        s"should have the question text ${propertiesMessages.WorthWhenSoldForLess.question}" in {
          doc.select("#worthWhenSoldForLess-question").text shouldBe propertiesMessages.WorthWhenSoldForLess.question
        }

        "should have the value £500" in {
          doc.select("#worthWhenSoldForLess-amount span.bold-medium").text shouldBe "£500"
        }
      }

      "has an option output row for sell or give away" which {

        s"should have the question text '${commonMessages.PropertiesSellOrGiveAway.title}'" in {
          doc.select("#sellOrGiveAway-question").text shouldBe commonMessages.PropertiesSellOrGiveAway.title
        }

        "should have the value 'Sold it'" in {
          doc.select("#sellOrGiveAway-option span.bold-medium").text shouldBe "Sold it"
        }
      }

      "has an option output row for sell for less" which {

        s"should have the question text '${commonMessages.Resident.Properties.SellForLess.title}'" in {
          doc.select("#sellForLess-question").text shouldBe commonMessages.Resident.Properties.SellForLess.title
        }

        "should have the value 'Yes'" in {
          doc.select("#sellForLess-option span.bold-medium").text shouldBe "Yes"
        }
      }

      "has an option output row for owner before april 1982" which {

        s"should have the question text '${commonMessages.Resident.Properties.OwnerBeforeLegislationStart.title}'" in {
          doc.select("#ownerBeforeLegislationStart-question").text shouldBe commonMessages.Resident.Properties.OwnerBeforeLegislationStart.title
        }

        "should have the value 'No'" in {
          doc.select("#ownerBeforeLegislationStart-option span.bold-medium").text shouldBe "No"

        }
      }

      "has an output row for how became owner" which {

        s"should have the question text '${commonMessages.HowBecameOwner.title}'" in {
          doc.select("#howBecameOwner-question").text shouldBe commonMessages.HowBecameOwner.title
        }

        s"should have the value '${commonMessages.HowBecameOwner.bought}'" in {
          doc.select("#howBecameOwner-option span.bold-medium").text shouldBe commonMessages.HowBecameOwner.bought
        }
      }


      "has an option output row for bought for less than worth" which {

        s"should have the question text '${commonMessages.BoughtForLessThanWorth.title}'" in {
          doc.select("#boughtForLessThanWorth-question").text shouldBe commonMessages.BoughtForLessThanWorth.title
        }

        "should have the value 'No'" in {
          doc.select("#boughtForLessThanWorth-option span.bold-medium").text shouldBe "No"
        }
      }

      "has a numeric output row for the Acquisition Value" which {

        s"should have the question text '${commonMessages.AcquisitionValue.title}'" in {
          doc.select("#acquisitionValue-question").text shouldBe commonMessages.AcquisitionValue.title
        }

        "should have the value '£100,000'" in {
          doc.select("#acquisitionValue-amount span.bold-medium").text shouldBe "£100,000"
        }
      }

      "has a numeric output row for the Improvements" which {

        s"should have the question text '${commonMessages.Resident.Properties.ImprovementsView.question}'" in {
          doc.select("#improvements-question").text shouldBe commonMessages.Resident.Properties.ImprovementsView.question
        }

        "should have the value '£30,000'" in {
          doc.select("#improvements-amount span.bold-medium").text shouldBe "£30,000"
        }
      }

      "has an option output row for property lived in" which {

        s"should have the question text '${commonMessages.PropertyLivedIn.title}'" in {
          doc.select("#propertyLivedIn-question").text shouldBe commonMessages.PropertyLivedIn.title
        }

        "should have the value 'Yes'" in {
          doc.select("#propertyLivedIn-option span.bold-medium").text shouldBe "Yes"
        }
      }

      "has an option output row for the eligible for private residence relief" which {

        s"should have the question text '${commonMessages.PrivateResidenceRelief.title}'" in {
          doc.select("#privateResidenceRelief-question").text shouldBe commonMessages.PrivateResidenceRelief.title
        }

        "should have the value 'Yes'" in {
          doc.select("#privateResidenceRelief-option span.bold-medium").text shouldBe "Yes"
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

      "have an option output row for the private residence relief value entry" which {

        s"should display" in {
          doc.select("#privateResidenceReliefValue-question").size() shouldBe 1
        }
      }

      "has an option output row for other properties" which {

        s"should have the question text '${commonMessages.OtherProperties.title("2013/14")}'" in {
          doc.select("#otherProperties-question").text shouldBe commonMessages.OtherProperties.title("2013/14")
        }

        "should have the value 'Yes'" in {
          doc.select("#otherProperties-option span.bold-medium").text shouldBe "Yes"
        }
      }

      "has an option output row for allowable losses" which {

        s"should have the question text '${commonMessages.AllowableLosses.title("2013/14")}'" in {
          doc.select("#allowableLosses-question").text shouldBe commonMessages.AllowableLosses.title("2013/14")
        }

        "should have the value 'Yes'" in {
          doc.select("#allowableLosses-option span.bold-medium").text shouldBe "Yes"
        }
      }

      "has a numeric output row for allowable losses value" which {

        s"should have the question text '${commonMessages.AllowableLossesValue.title("2013/14")}'" in {
          doc.select("#allowableLossesValue-question").text shouldBe commonMessages.AllowableLossesValue.title("2013/14")
        }

        "should have the value '£10,000'" in {
          doc.select("#allowableLossesValue-amount span.bold-medium").text shouldBe "£10,000"
        }
      }

      "has an option output row for brought forward losses" which {

        s"should have the question text '${commonMessages.LossesBroughtForward.title("2013/14")}'" in {
          doc.select("#broughtForwardLosses-question").text shouldBe commonMessages.LossesBroughtForward.title("2013/14")
        }

        "should have the value 'Yes'" in {
          doc.select("#broughtForwardLosses-option span.bold-medium").text shouldBe "Yes"
        }
      }

      "has a numeric output row for brought forward losses value" which {

        s"should have the question text '${commonMessages.LossesBroughtForwardValue.title("2013/14")}'" in {
          doc.select("#broughtForwardLossesValue-question").text shouldBe commonMessages.LossesBroughtForwardValue.title("2013/14")
        }

        "should have the value '£10,000'" in {
          doc.select("#broughtForwardLossesValue-amount span.bold-medium").text shouldBe "£10,000"
        }
      }

      "has an option output row for eligible for lettings relief value in" which {

        s"should have the question text '${commonMessages.LettingsReliefValue.title}'" in {
          doc.select("#lettingsReliefValue-question").text shouldBe commonMessages.LettingsReliefValue.title
        }

        "should have the value 'No'" in {
          doc.select("#lettingsReliefValue-amount span.bold-medium").text shouldBe "£6,000"
        }
      }
    }
  }

  "Deductions Report view when property bought for less than worth" should {
    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
      None,
      None,
      whoDidYouGiveItTo = Some("Other"),
      worthWhenGaveAway = Some(10000),
      BigDecimal(10000),
      None,
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = Some(3000),
      BigDecimal(10000),
      BigDecimal(30000),
      true,
      Some(false),
      false,
      None,
      Some("Bought"),
      Some(true)
    )
    lazy val deductionAnswers = ChargeableGainAnswers(
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(true)),
      Some(AllowableLossesValueModel(10000)),
      Some(LossesBroughtForwardModel(true)),
      Some(LossesBroughtForwardValueModel(10000)),
      Some(AnnualExemptAmountModel(1000)),
      Some(PropertyLivedInModel(true)),
      Some(PrivateResidenceReliefModel(true)),
      Some(PrivateResidenceReliefValueModel(1000)),
      Some(LettingsReliefModel(true)),
      Some(LettingsReliefValueModel(6000))
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

    lazy val view = views.deductionsSummaryReport(gainAnswers, deductionAnswers, results, taxYearModel)(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)

    "has an option output row for bought for less than worth" which {

      s"should have the question text '${commonMessages.BoughtForLessThanWorth.title}'" in {
        doc.select("#boughtForLessThanWorth-question").text shouldBe commonMessages.BoughtForLessThanWorth.title
      }

      "should have the value 'Yes'" in {
        doc.select("#boughtForLessThanWorth-option span.bold-medium").text shouldBe "Yes"
      }
    }

    "has an amount output row for bought for less than worth value" which {

      s"should have the question text '${propertiesMessages.WorthWhenBoughtForLess.question}'" in {
        doc.select("#worthWhenBoughtForLess-question").text shouldBe propertiesMessages.WorthWhenBoughtForLess.question
      }

      "should have the value '£3,000'" in {
        doc.select("#worthWhenBoughtForLess-amount span.bold-medium").text shouldBe "£3,000"
      }
    }
  }

  "Deductions Report view with AEA options selected" which {

    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2016),
      None,
      None,
      whoDidYouGiveItTo = Some("Other"),
      worthWhenGaveAway = Some(10000),
      BigDecimal(10000),
      None,
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      BigDecimal(10000),
      BigDecimal(30000),
      true,
      None,
      true,
      Some(BigDecimal(5000)),
      None,
      None
    )

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
      Some(LettingsReliefValueModel(6000))
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

    lazy val view = views.deductionsSummaryReport(gainAnswers, deductionAnswers, results, taxYearModel)(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)

    "has a numeric output row for the deductions" which {

      "should have the question text 'Deductions'" in {
        doc.select("#deductions-question").text shouldBe messages.deductions
      }

      "has a breakdown that" should {

        "include a value for PRR of £1,500" in {
          doc.select("#deductions-amount").text should include("Private Residence Relief used £1,500")
        }

        "include a value for Reliefs of £30,000" in {
          doc.select("#deductions-amount").text should include(s"${messages.lettingReliefsUsed} £30,000")
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

      s"should have the question text '${commonMessages.AnnualExemptAmount.title}'" in {
        doc.select("#annualExemptAmount-question").text shouldBe commonMessages.AnnualExemptAmount.title
      }

      "should have the value '£1,000'" in {
        doc.select("#annualExemptAmount-amount span.bold-medium").text shouldBe "£1,000"
      }
    }

    "has an option output row for eligible for private residence relief in" which {

      s"should have the question text '${commonMessages.PrivateResidenceRelief.title}'" in {
        doc.select("#privateResidenceRelief-question").text shouldBe commonMessages.PrivateResidenceRelief.title
      }

      "should have the value 'Yes'" in {
        doc.select("#privateResidenceRelief-option span.bold-medium").text shouldBe "Yes"
      }
    }

    "has an option output row for private residence relief value in" which {

      s"should have the question text '${commonMessages.PrivateResidenceReliefValue.title}'" in {
        doc.select("#privateResidenceReliefValue-question").text shouldBe commonMessages.PrivateResidenceReliefValue.title
      }

      "should have the value '£5,000'" in {
        doc.select("#privateResidenceReliefValue-amount span.bold-medium").text shouldBe "£5,000"
      }
    }

    "has an option output row for eligible for lettings relief in" which {

      s"should have the question text '${commonMessages.LettingsRelief.title}'" in {
        doc.select("#lettingsRelief-question").text shouldBe commonMessages.LettingsRelief.title
      }

      "should have the value 'Yes'" in {
        doc.select("#lettingsRelief-option span.bold-medium").text shouldBe "Yes"
      }

    }

    "has an option output row for eligible for lettings relief value in" which {

      s"should have the question text '${commonMessages.LettingsReliefValue.title}'" in {
        doc.select("#lettingsReliefValue-question").text shouldBe commonMessages.LettingsReliefValue.title
      }

      "should have the value 'No'" in {
        doc.select("#lettingsReliefValue-amount span.bold-medium").text shouldBe "£6,000"
      }

    }
  }


  "Report when supplied with a date above the known tax years" should {

    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2018),
      Some(BigDecimal(200000)),
      None,
      whoDidYouGiveItTo = None,
      worthWhenGaveAway = None,
      BigDecimal(10000),
      None,
      worthWhenInherited = Some(3000),
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      BigDecimal(10000),
      BigDecimal(30000),
      false,
      Some(false),
      false,
      None,
      Some("Inherited"),
      None
    )

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
      None
    )
    lazy val results = ChargeableGainResultModel(BigDecimal(50000),
      BigDecimal(-11000),
      BigDecimal(0),
      BigDecimal(11000),
      BigDecimal(71000),
      BigDecimal(0),
      BigDecimal(2000),
      Some(BigDecimal(50000)),
      Some(BigDecimal(0)),
      10000,
      10000
    )

    lazy val taxYearModel = TaxYearModel("2017/18", false, "2015/16")

    lazy val view = views.deductionsSummaryReport(gainAnswers, deductionAnswers, results, taxYearModel)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "has a numeric output row for the Disposal Value" which {

      s"should have the question text '${commonMessages.DisposalValue.question}'" in {
        doc.select("#disposalValue-question").text shouldBe commonMessages.DisposalValue.question
      }

      "should have the value '£200,000'" in {
        doc.select("#disposalValue-amount span.bold-medium").text shouldBe "£200,000"
      }
    }

    "has an output row for how became owner" which {

      s"should have the question text '${commonMessages.HowBecameOwner.title}'" in {
        doc.select("#howBecameOwner-question").text shouldBe commonMessages.HowBecameOwner.title
      }

      s"should have the value '${commonMessages.HowBecameOwner.inherited}'" in {
        doc.select("#howBecameOwner-option span.bold-medium").text shouldBe commonMessages.HowBecameOwner.inherited
      }
    }

    "has an amount output row for inherited value" which {

      s"should have the question text '${propertiesMessages.WorthWhenInherited.question}'" in {
        doc.select("#worthWhenInherited-question").text shouldBe propertiesMessages.WorthWhenInherited.question
      }

      "should have the value '£3,000'" in {
        doc.select("#worthWhenInherited-amount span.bold-medium").text shouldBe "£3,000"
      }
    }

    "has a numeric output row for allowable losses remaining" in {
      doc.select("#allowableLossRemaining").isEmpty shouldBe true
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

    "has an option output row for sell for less" which {

      s"should have the question text '${commonMessages.Resident.Properties.SellForLess.title}'" in {
        doc.select("#sellForLess-question").text shouldBe commonMessages.Resident.Properties.SellForLess.title
      }

      "should have the value 'No'" in {
        doc.select("#sellForLess-option span.bold-medium").text shouldBe "No"
      }
    }

    "does not display the section for what to do next" in {
      doc.select("#whatToDoNext").isEmpty shouldBe true
    }
  }

  "Report when supplied with a gifted property" should {

    lazy val gainAnswers = YourAnswersSummaryModel(Dates.constructDate(10, 10, 2018),
      Some(BigDecimal(200000)),
      None,
      whoDidYouGiveItTo = None,
      worthWhenGaveAway = None,
      BigDecimal(10000),
      None,
      worthWhenInherited = None,
      worthWhenGifted = Some(3000),
      worthWhenBoughtForLess = None,
      BigDecimal(10000),
      BigDecimal(30000),
      false,
      Some(false),
      false,
      None,
      Some("Gifted"),
      None
    )
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
      None
    )
    lazy val results = ChargeableGainResultModel(BigDecimal(50000),
      BigDecimal(-11000),
      BigDecimal(0),
      BigDecimal(11000),
      BigDecimal(71000),
      BigDecimal(0),
      BigDecimal(2000),
      Some(BigDecimal(50000)),
      Some(BigDecimal(0)),
      10000,
      10000
    )

    lazy val taxYearModel = TaxYearModel("2017/18", false, "2015/16")

    lazy val view = views.deductionsSummaryReport(gainAnswers, deductionAnswers, results, taxYearModel)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "has an output row for how became owner" which {

      s"should have the question text '${commonMessages.HowBecameOwner.title}'" in {
        doc.select("#howBecameOwner-question").text shouldBe commonMessages.HowBecameOwner.title
      }

      s"should have the value '${commonMessages.HowBecameOwner.gifted}'" in {
        doc.select("#howBecameOwner-option span.bold-medium").text shouldBe commonMessages.HowBecameOwner.gifted
      }
    }

    "has an amount output row for gifted value" which {

      s"should have the question text '${propertiesMessages.WorthWhenGifted.question}'" in {
        doc.select("#worthWhenGifted-question").text shouldBe propertiesMessages.WorthWhenGifted.question
      }

      "should have the value '£3,000'" in {
        doc.select("#worthWhenGifted-amount span.bold-medium").text shouldBe "£3,000"
      }
    }
  }
}

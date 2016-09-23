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
import assets.MessageLookup.Resident.{Properties => propertiesMessages}
import common.Dates._
import controllers.helpers.FakeRequestHelper
import controllers.resident.properties.routes
import models.resident.TaxYearModel
import models.resident.properties.YourAnswersSummaryModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{summary => views}

class PropertiesGainSummaryViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Summary view" should {

    val testModel = YourAnswersSummaryModel(
      constructDate(12, 9, 1990),
      None,
      None,
      worthWhenGaveAway = None,
      20,
      None,
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      40,
      50,
      true,
      None,
      true,
      Some(BigDecimal(5000)),
      None,
      None
    )


    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    lazy val view = views.gainSummary(testModel, -2000, taxYearModel)(fakeRequest)
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

      s"has a link to '${routes.GainController.improvements().toString()}'" in {
        backLink.attr("href") shouldBe routes.GainController.improvements().toString
      }

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

        "should have the question text 'Loss'" in {
          doc.select("#gain-question").text shouldBe messages.totalLoss
        }

        "should have the value '£2,000'" in {
          doc.select("#gain-amount").text shouldBe "£2,000"
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

        "should have the value '12 September 1990'" in {
          doc.select("#disposalDate-date span.bold-medium").text shouldBe "12 September 1990"
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

        "should have the value '£20'" in {
          doc.select("#disposalCosts-amount span.bold-medium").text shouldBe "£20"
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

        "should have the value '£40'" in {
          doc.select("#acquisitionCosts-amount span.bold-medium").text shouldBe "£40"
        }

        s"should have a change link to ${routes.GainController.acquisitionCosts().url}" in {
          doc.select("#acquisitionCosts-amount a").attr("href") shouldBe routes.GainController.acquisitionCosts().url
        }

      }

      "has a numeric output row for the Improvements" which {

        s"should have the question text '${commonMessages.Resident.Properties.improvementsView.questionBefore}'" in {
          doc.select("#improvements-question").text shouldBe commonMessages.Resident.Properties.improvementsView.questionBefore
        }

        "should have the value '£50'" in {
          doc.select("#improvements-amount span.bold-medium").text shouldBe "£50"
        }

        s"should have a change link to ${routes.GainController.improvements().url}" in {
          doc.select("#improvements-amount a").attr("href") shouldBe routes.GainController.improvements().url
        }
      }

      "does not display the section for what to do next" in {
        doc.select("#whatToDoNext").text shouldEqual ""
      }

      "display the save as PDF Button" which {

        "should render only one button" in {
          doc.select("a.save-pdf-button").size() shouldEqual 1
        }

        "with the class save-pdf-button" in {
          doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
        }

        s"with an href to ${controllers.resident.properties.routes.ReportController.gainSummaryReport().toString}" in {
          doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/gain-report"
        }

        s"have the text ${messages.saveAsPdf}" in {
          doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
        }
      }
    }
  }

  "Summary when supplied with a date within the known tax years and a loss" should {

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    val testModel = YourAnswersSummaryModel(
      constructDate(12, 9, 2015),
      None,
      Some(500),
      worthWhenGaveAway = None,
      20,
      Some(30),
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      40,
      50,
      false,
      Some(true),
      false,
      None,
      Some("Bought"),
      Some(false)
    )

    lazy val view = views.gainSummary(testModel, -2000, taxYearModel)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "display the what to do next section" in {
      doc.select("#whatToDoNext").hasText shouldEqual true
    }

    s"display the title ${messages.whatToDoNextTitle}" in {
      doc.select("#whatToDoNextTitle").text shouldEqual messages.whatToDoNextTitle
    }

    s"display the text ${messages.whatToDoNextText}" in {
      doc.select("#whatToDoNextText").text shouldEqual
        s"${messages.whatNextYouCan}${messages.whatNextLink}${commonMessages.calcBaseExternalLink} ${messages.whatNextText}"
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

      s"should have the question text '${commonMessages.Resident.Properties.sellForLess.title}'" in {
        doc.select("#sellForLess-question").text shouldBe commonMessages.Resident.Properties.sellForLess.title
      }

      "should have the value 'Yes'" in {
        doc.select("#sellForLess-option span.bold-medium").text shouldBe "Yes"
      }

      s"should have a change link to ${routes.GainController.sellForLess().url}" in {
        doc.select("#sellForLess-option a").attr("href") shouldBe routes.GainController.sellForLess().url
      }

      "has the question as part of the link" in {
        doc.select("#sellForLess-option a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.Resident.Properties.sellForLess.title}"
      }

      "has the question component of the link as visuallyhidden" in {
        doc.select("#sellForLess-option a span.visuallyhidden").text shouldBe commonMessages.Resident.Properties.sellForLess.title
      }
    }

    "has an amount output row for worth when sold for less" which {

      s"should have the question text '${propertiesMessages.WorthWhenSoldForLess.question}'" in {
        doc.select("#worthWhenSoldForLess-question").text shouldBe propertiesMessages.WorthWhenSoldForLess.question
      }

      "should have the value '£500'" in {
        doc.select("#worthWhenSoldForLess-amount span.bold-medium").text shouldBe "£500"
      }

      s"should have a change link to ${routes.GainController.worthWhenSoldForLess().url}" in {
        doc.select("#worthWhenSoldForLess-amount a").attr("href") shouldBe routes.GainController.worthWhenSoldForLess().url
      }

      "has the question as part of the link" in {
        doc.select("#worthWhenSoldForLess-amount a").text shouldBe s"${commonMessages.calcBaseChange} ${propertiesMessages.WorthWhenSoldForLess.question}"
      }

      "has the question component of the link as visuallyhidden" in {
        doc.select("#worthWhenSoldForLess-amount a span.visuallyhidden").text shouldBe propertiesMessages.WorthWhenSoldForLess.question
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

      "should have the value '£30'" in {
        doc.select("#acquisitionValue-amount span.bold-medium").text shouldBe "£30"
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
        doc.select("#improvements-amount span.bold-medium").text shouldBe "£50"
      }

      s"should have a change link to ${routes.GainController.improvements().url}" in {
        doc.select("#improvements-amount a").attr("href") shouldBe routes.GainController.improvements().url
      }
    }

    "have a link" which {

      "should have a href attribute" in {
        doc.select("#whatToDoNextLink").hasAttr("href") shouldEqual true
      }

      "should link to the what-you-pay-on-it govuk page" in {
        doc.select("#whatToDoNextLink").attr("href") shouldEqual "https://www.gov.uk/capital-gains-tax/losses"
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
        doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/gain-report"
      }
    }
  }

  "Summary when supplied with a property bought for less than worth" should {

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    val testModel = YourAnswersSummaryModel(
      constructDate(12, 9, 2015),
      None,
      Some(500),
      worthWhenGaveAway = None,
      20,
      None,
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = Some(3000),
      40,
      50,
      false,
      Some(true),
      false,
      None,
      Some("Bought"),
      Some(true)
    )
    lazy val view = views.gainSummary(testModel, -2000, taxYearModel)(fakeRequest)
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

    "has a numeric output row for the bought for less than worth value" which {

      s"should have the question text '${propertiesMessages.worthWhenBought.question}'" in {
        doc.select("#worthWhenBought-question").text shouldBe propertiesMessages.worthWhenBought.question
      }

      "should have the value '£3,000'" in {
        doc.select("#worthWhenBought-amount span.bold-medium").text shouldBe "£3,000"
      }

      s"should have a change link to ${routes.GainController.worthWhenBought().url}" in {
        doc.select("#worthWhenBought-amount a").attr("href") shouldBe routes.GainController.worthWhenBought().url
      }

      "has the question as part of the link" in {
        doc.select("#worthWhenBought-amount a").text shouldBe s"${commonMessages.calcBaseChange} ${propertiesMessages.worthWhenBought.question}"
      }

      "has the question component of the link as visuallyhidden" in {
        doc.select("#worthWhenBought-amount a span.visuallyhidden").text shouldBe propertiesMessages.worthWhenBought.question
      }
    }
  }

  "Summary when supplied with a date within the known tax years and no gain or loss" should {

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    val testModel = YourAnswersSummaryModel(
      constructDate(12, 9, 2015),
      None,
      None,
      worthWhenGaveAway = None,
      20,
      None,
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      40,
      50,
      true,
      None,
      true,
      Some(BigDecimal(5000)),
      None,
      None
    )
    lazy val view = views.gainSummary(testModel, 0, taxYearModel)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "display the what to do next section" in {
      doc.select("#whatToDoNext").hasText shouldEqual true
    }

    s"display the title ${messages.whatToDoNextTitle}" in {
      doc.select("h2#whatToDoNextNoLossTitle").text shouldEqual messages.whatToDoNextTitle
    }

    s"display the text ${messages.whatToDoNextText}" in {
      doc.select("div#whatToDoNextNoLossText").text shouldBe
        s"${messages.whatToDoNextNoLossText} ${messages.whatToDoNextNoLossLinkProperties} ${commonMessages.calcBaseExternalLink}."
    }

    s"have the link text ${messages.whatToDoNextNoLossLinkProperties}${commonMessages.calcBaseExternalLink}" in {
      doc.select("div#whatToDoNextNoLossText a").text should include(s"${messages.whatToDoNextNoLossLinkProperties}")
    }

    s"have a link to https://www.gov.uk/capital-gains-tax/report-and-pay-capital-gains-tax" in {
      doc.select("div#whatToDoNextNoLossText a").attr("href") shouldBe "https://www.gov.uk/capital-gains-tax/report-and-pay-capital-gains-tax"
    }

    s"have the visually hidden text ${commonMessages.calcBaseExternalLink}" in {
      doc.select("div#whatToDoNextNoLossText span#opensInANewTab2").text shouldBe s"${commonMessages.calcBaseExternalLink}"
    }

    "display the save as PDF Button" which {

      "should render only one button" in {
        doc.select("a.save-pdf-button").size() shouldEqual 1
      }

      "with the class save-pdf-button" in {
        doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
      }

      s"with an href to ${controllers.resident.properties.routes.ReportController.gainSummaryReport().toString}" in {
        doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/gain-report"
      }

      s"have the text ${messages.saveAsPdf}" in {
        doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
      }
    }
  }

  "Summary when supplied with a date above the known tax years" should {

    lazy val taxYearModel = TaxYearModel("2018/19", false, "2016/17")

    val testModel = YourAnswersSummaryModel(
      constructDate(12,9,2018),
      Some(10),
      None,
      worthWhenGaveAway = None,
      20,
      None,
      worthWhenInherited = Some(3000),
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      40,
      50,
      false,
      Some(false),
      false,
      None,
      Some("Inherited"),
      None
    )
    lazy val view = views.gainSummary(testModel,-2000, taxYearModel)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "does not display the what to do next content" in {
      doc.select("#whatToDoNext").isEmpty shouldBe true
    }

    "has a numeric output row for the Disposal Value" which {

      s"should have the question text '${commonMessages.disposalValue.question}'" in {
        doc.select("#disposalValue-question").text shouldBe commonMessages.disposalValue.question
      }

      "should have the value '£10'" in {
        doc.select("#disposalValue-amount span.bold-medium").text shouldBe "£10"
      }

      s"should have a change link to ${routes.GainController.disposalValue().url}" in {
        doc.select("#disposalValue-amount a").attr("href") shouldBe routes.GainController.disposalValue().url
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

    "has a numeric output row for the inherited value" which {

      s"should have the question text '${propertiesMessages.worthWhenInherited.question}'" in {
        doc.select("#worthWhenInherited-question").text shouldBe propertiesMessages.worthWhenInherited.question
      }

      "should have the value '£3,000'" in {
        doc.select("#worthWhenInherited-amount span.bold-medium").text shouldBe "£3,000"
      }

      s"should have a change link to ${routes.GainController.worthWhenInherited().url}" in {
        doc.select("#worthWhenInherited-amount a").attr("href") shouldBe routes.GainController.worthWhenInherited().url
      }
    }
  }

  "Summary view with an out of tax year date" should {

    val testModel = YourAnswersSummaryModel(
      constructDate(12, 9, 2013),
      Some(10),
      None,
      worthWhenGaveAway = None,
      20,
      None,
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      40,
      50,
      false,
      Some(false),
      true,
      Some(BigDecimal(5000)),
      None,
      None
    )

    lazy val taxYearModel = TaxYearModel("2013/14", false, "2015/16")

    lazy val view = views.gainSummary(testModel, -2000, taxYearModel)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

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

    s"has a h2 tag" which {

      s"should have the title '${messages.calcDetailsHeadingDate("2013/14")}'" in {
        doc.select("section#calcDetails h2").text shouldBe messages.calcDetailsHeadingDate("2013/14")
      }
    }

    "has an option output row for sell for less" which {

      s"should have the question text '${commonMessages.Resident.Properties.sellForLess.title}'" in {
        doc.select("#sellForLess-question").text shouldBe commonMessages.Resident.Properties.sellForLess.title
      }

      "should have the value 'No'" in {
        doc.select("#sellForLess-option span.bold-medium").text shouldBe "No"
      }

      s"should have a change link to ${routes.GainController.sellForLess().url}" in {
        doc.select("#sellForLess-option a").attr("href") shouldBe routes.GainController.sellForLess().url
      }

      "has the question as part of the link" in {
        doc.select("#sellForLess-option a").text shouldBe s"${commonMessages.calcBaseChange} ${commonMessages.Resident.Properties.sellForLess.title}"
      }

      "has the question component of the link as visuallyhidden" in {
        doc.select("#sellForLess-option a span.visuallyhidden").text shouldBe commonMessages.Resident.Properties.sellForLess.title
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
        doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/properties/gain-report"
      }

      s"have the text ${messages.saveAsPdf}" in {
        doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
      }
    }
  }

  "Summary when supplied with a gifted property" should {

    lazy val taxYearModel = TaxYearModel("2018/19", false, "2016/17")

    val testModel = YourAnswersSummaryModel(
      constructDate(12,9,2018),
      Some(10),
      None,
      worthWhenGaveAway = None,
      20,
      None,
      worthWhenInherited = None,
      worthWhenGifted = Some(3000),
      worthWhenBoughtForLess = None,
      40,
      50,
      false,
      Some(false),
      false,
      None,
      Some("Gifted"),
      None
    )
    lazy val view = views.gainSummary(testModel,-2000, taxYearModel)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "has an output row for how became owner" which {

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

    "has a numeric output row for the gifted value" which {

      s"should have the question text '${propertiesMessages.worthWhenGifted.question}'" in {
        doc.select("#worthWhenGifted-question").text shouldBe propertiesMessages.worthWhenGifted.question
      }

      "should have the value '£3,000'" in {
        doc.select("#worthWhenGifted-amount span.bold-medium").text shouldBe "£3,000"
      }

      s"should have a change link to ${routes.GainController.worthWhenGifted().url}" in {
        doc.select("#worthWhenGifted-amount a").attr("href") shouldBe routes.GainController.worthWhenGifted().url
      }
    }
  }
}

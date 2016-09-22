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

import assets.{MessageLookup => commonMessages}
import assets.MessageLookup.{summaryPage => messages}
import common.Dates._
import controllers.helpers.FakeRequestHelper
import models.resident.TaxYearModel
import models.resident.properties.YourAnswersSummaryModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{report => views}

class PropertiesGainReportViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Summary view" should {

    val testModel = YourAnswersSummaryModel(
      constructDate(12, 9, 1990),
      10,
      None,
      20,
      30,
      40,
      50,
      true,
      None,
      true,
      None,
      None
    )

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    lazy val view = views.gainSummaryReport(testModel, -2000, taxYearModel)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

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

    "have the hmrc logo with the hmrc name" in {
      doc.select("div.logo span").text shouldBe "HM Revenue & Customs"
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
      }

      "has an option output row for sell or give away" which {

        s"should have the question text '${commonMessages.propertiesSellOrGiveAway.title}'" in {
          doc.select("#sellOrGiveAway-question").text shouldBe commonMessages.propertiesSellOrGiveAway.title
        }

        "should have the value 'Gave it away'" in {
          doc.select("#sellOrGiveAway-option span.bold-medium").text shouldBe "Gave it away"
        }
      }


      "has a numeric output row for the Disposal Value" which {

        s"should have the question text '${commonMessages.disposalValue.question}'" in {
          doc.select("#disposalValue-question").text shouldBe commonMessages.disposalValue.question
        }

        "should have the value '£10'" in {
          doc.select("#disposalValue-amount span.bold-medium").text shouldBe "£10"
        }
      }

      "has a numeric output row for the Disposal Costs" which {

        s"should have the question text '${commonMessages.disposalCosts.title}'" in {
          doc.select("#disposalCosts-question").text shouldBe commonMessages.disposalCosts.title
        }

        "should have the value '£20'" in {
          doc.select("#disposalCosts-amount span.bold-medium").text shouldBe "£20"
        }
      }

      "has an option output row for owner before april 1982" which {

        s"should have the question text '${commonMessages.Resident.Properties.ownerBeforeAprilNineteenEightyTwo.title}'" in {
          doc.select("#ownerBeforeAprilNineteenEightyTwo-question").text shouldBe commonMessages.Resident.Properties.ownerBeforeAprilNineteenEightyTwo.title
        }

        "should have the value 'Yes'" in {
          doc.select("#ownerBeforeAprilNineteenEightyTwo-option span.bold-medium").text shouldBe "Yes"
        }
      }

      "has a numeric output row for the Acquisition Value" which {

        s"should have the question text '${commonMessages.acquisitionValue.title}'" in {
          doc.select("#acquisitionValue-question").text shouldBe commonMessages.acquisitionValue.title
        }

        "should have the value '£30'" in {
          doc.select("#acquisitionValue-amount span.bold-medium").text shouldBe "£30"
        }
      }

      "has a numeric output row for the Acquisition Costs" which {

        s"should have the question text '${commonMessages.acquisitionCosts.title}'" in {
          doc.select("#acquisitionCosts-question").text shouldBe commonMessages.acquisitionCosts.title
        }

        "should have the value '£40'" in {
          doc.select("#acquisitionCosts-amount span.bold-medium").text shouldBe "£40"
        }
      }

      "has a numeric output row for the Improvements" which {

        s"should have the question text '${commonMessages.Resident.Properties.improvementsView.questionBefore}'" in {
          doc.select("#improvements-question").text shouldBe commonMessages.Resident.Properties.improvementsView.questionBefore
        }

        "should have the value '£50'" in {
          doc.select("#improvements-amount span.bold-medium").text shouldBe "£50"
        }
      }

      "does not display the section for what to do next" in {
        doc.select("#whatToDoNext").text shouldEqual ""
      }
    }
  }

  "Summary when supplied with a date outside the known tax years and no gain or loss" should {

    lazy val taxYearModel = TaxYearModel("2018/19", false, "2016/17")

    val testModel = YourAnswersSummaryModel(
      constructDate(12, 9, 2015),
      10,
      None,
      20,
      30,
      40,
      50,
      false,
      Some(true),
      false,
      Some("Bought"),
      Some(false)
    )

    lazy val view = views.gainSummaryReport(testModel, 0, taxYearModel)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "should have the question text 'Total gain'" in {
      doc.select("#gain-question").text shouldBe messages.totalGain
    }

    "have the class notice-wrapper" in {
      doc.select("div.notice-wrapper").isEmpty shouldBe false
    }

    s"have the text ${messages.noticeWarning("2016/17")}" in {
      doc.select("strong.bold-small").text shouldBe messages.noticeWarning("2016/17")
    }

    "has an option output row for sell or give away" which {

      s"should have the question text '${commonMessages.propertiesSellOrGiveAway.title}'" in {
        doc.select("#sellOrGiveAway-question").text shouldBe commonMessages.propertiesSellOrGiveAway.title
      }

      "should have the value 'Sold it'" in {
        doc.select("#sellOrGiveAway-option span.bold-medium").text shouldBe "Sold it"
      }
    }

    "has an option output row for sell for less" which {

      s"should have the question text '${commonMessages.sellForLess.title}'" in {
        doc.select("#sellForLess-question").text shouldBe commonMessages.sellForLess.title
      }

      "should have the value 'Yes'" in {
        doc.select("#sellForLess-option span.bold-medium").text shouldBe "Yes"
      }
    }

    "has an option output row for owner before april 1982" which {

      s"should have the question text '${commonMessages.Resident.Properties.ownerBeforeAprilNineteenEightyTwo.title}'" in {
        doc.select("#ownerBeforeAprilNineteenEightyTwo-question").text shouldBe commonMessages.Resident.Properties.ownerBeforeAprilNineteenEightyTwo.title
      }

      "should have the value 'No'" in {
        doc.select("#ownerBeforeAprilNineteenEightyTwo-option span.bold-medium").text shouldBe "No"
      }
    }

    "has an output row for how became owner" which {

      s"should have the question text '${commonMessages.howBecameOwner.title}'" in {
        doc.select("#howBecameOwner-question").text shouldBe commonMessages.howBecameOwner.title
      }

      s"should have the value '${commonMessages.howBecameOwner.bought}'" in {
        doc.select("#howBecameOwner-option span.bold-medium").text shouldBe commonMessages.howBecameOwner.bought
      }
    }

    "has an option output row for bought for less than worth" which {

      s"should have the question text '${commonMessages.boughtForLessThanWorth.title}'" in {
        doc.select("#boughtForLessThanWorth-question").text shouldBe commonMessages.boughtForLessThanWorth.title
      }

      "should have the value 'No'" in {
        doc.select("#boughtForLessThanWorth-option span.bold-medium").text shouldBe "No"
      }
    }

    "has a numeric output row for the Improvements" which {

      s"should have the question text '${commonMessages.Resident.Properties.improvementsView.question}'" in {
        doc.select("#improvements-question").text shouldBe commonMessages.Resident.Properties.improvementsView.question
      }

      "should have the value '£50'" in {
        doc.select("#improvements-amount span.bold-medium").text shouldBe "£50"
      }
    }
  }

  "Summary when supplied with a property bought for less than its worth" should {

    lazy val taxYearModel = TaxYearModel("2018/19", false, "2016/17")

    val testModel = YourAnswersSummaryModel(
      constructDate(12, 9, 2015),
      10,
      None,
      20,
      30,
      40,
      50,
      false,
      Some(true),
      false,
      Some("Bought"),
      Some(true)
    )
    lazy val view = views.gainSummaryReport(testModel, 0, taxYearModel)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "has an option output row for bought for less than worth" which {

      s"should have the question text '${commonMessages.boughtForLessThanWorth.title}'" in {
        doc.select("#boughtForLessThanWorth-question").text shouldBe commonMessages.boughtForLessThanWorth.title
      }

      "should have the value 'Yes'" in {
        doc.select("#boughtForLessThanWorth-option span.bold-medium").text shouldBe "Yes"
      }
    }
  }

  "Summary when supplied with an inherited property" should {

    lazy val taxYearModel = TaxYearModel("2018/19", false, "2016/17")

    val testModel = YourAnswersSummaryModel(
      constructDate(12, 9, 2015),
      10,
      None,
      20,
      30,
      40,
      50,
      false,
      Some(true),
      false,
      Some("Inherited"),
      None
    )
    lazy val view = views.gainSummaryReport(testModel, 0, taxYearModel)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "has an output row for how became owner" which {

      s"should have the question text '${commonMessages.howBecameOwner.title}'" in {
        doc.select("#howBecameOwner-question").text shouldBe commonMessages.howBecameOwner.title
      }

      s"should have the value '${commonMessages.howBecameOwner.inherited}'" in {
        doc.select("#howBecameOwner-option span.bold-medium").text shouldBe commonMessages.howBecameOwner.inherited
      }
    }
  }

  "Summary when supplied with a gifted property" should {

    lazy val taxYearModel = TaxYearModel("2018/19", false, "2016/17")

    val testModel = YourAnswersSummaryModel(
      constructDate(12, 9, 2015),
      10,
      None,
      20,
      30,
      40,
      50,
      false,
      Some(true),
      false,
      Some("Gifted"),
      None
    )
    lazy val view = views.gainSummaryReport(testModel, 0, taxYearModel)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "has an output row for how became owner" which {

      s"should have the question text '${commonMessages.howBecameOwner.title}'" in {
        doc.select("#howBecameOwner-question").text shouldBe commonMessages.howBecameOwner.title
      }

      s"should have the value '${commonMessages.howBecameOwner.gifted}'" in {
        doc.select("#howBecameOwner-option span.bold-medium").text shouldBe commonMessages.howBecameOwner.gifted
      }
    }
  }

  "Summary for Sell for Loss with option No" should {

    lazy val taxYearModel = TaxYearModel("2018/19", false, "2016/17")

    val testModel = YourAnswersSummaryModel(
      constructDate(12, 9, 2015),
      10,
      None,
      20,
      30,
      40,
      50,
      false,
      Some(false),
      false,
      Some("Inherited"),
      None
    )
    lazy val view = views.gainSummaryReport(testModel, 0, taxYearModel)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "has an option output row for sell for less" which {

      s"should have the question text '${commonMessages.sellForLess.title}'" in {
        doc.select("#sellForLess-question").text shouldBe commonMessages.sellForLess.title
      }

      "should have the value 'No'" in {
        doc.select("#sellForLess-option span.bold-medium").text shouldBe "No"
      }
    }
  }
}

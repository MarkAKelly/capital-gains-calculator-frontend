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

package views.nonResident

import common.TestModels._
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.summary
import assets.MessageLookup.{NonResident => messages}

class SummaryViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "When called the summary view" should {
    lazy val view = summary(businessScenarioOneModel, calcModelTwoRates, "back-link")(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)

    "have a back link" which {
      lazy val backLink = doc.select("#back-link")

      "has the class back-link" in {
        backLink.attr("class") shouldBe "back-link"
      }

      "has the text 'Back'" in {
        backLink.text() shouldBe messages.back
      }

      "has a link to 'back-link'" in {
        backLink.attr("href") shouldBe "back-link"
      }
    }

    "have a tax owed heading" which {
      lazy val heading = doc.select("h1")

      "has the class 'heading-xlarge heading-xxlarge'" in {
        heading.attr("class") shouldBe "heading-xlarge heading-xxlarge"
      }

      "has a span of class 'heading-secondary'" in {
        heading.select("span").attr("class") shouldBe "heading-secondary"
      }

      s"has the text '${messages.Summary.secondaryHeading}'" in {
        heading.select("span").text() shouldBe messages.Summary.secondaryHeading
      }

      "has a value of tax owed of £8000" in {
        heading.select("b").text() shouldBe "£8,000.00"
      }
    }

    "not display a tax year warning" in {
      doc.select("div.notice-wrapper").isEmpty shouldBe true
    }

    "have a section for calculation details" in {
      doc.select("#calculationDetails span.heading-large").text() shouldBe messages.Summary.calculationDetailsTitle
    }

    "have a section for personal details" in {
      doc.select("#personalDetails span.heading-large").text() shouldBe messages.Summary.personalDetailsTitle
    }

    "have a section for purchase details" in {
      doc.select("#purchaseDetails span.heading-large").text() shouldBe messages.Summary.purchaseDetailsTitle
    }

    "have a section for property details" in {
      doc.select("#propertyDetails span.heading-large").text() shouldBe messages.Summary.propertyDetailsTitle
    }

    "have a section for sale details" in {
      doc.select("#saleDetails span.heading-large").text() shouldBe messages.Summary.saleDetailsTitle
    }

    "have a section for deductions details" in {
      doc.select("#deductionsDetails span.heading-large").text() shouldBe messages.Summary.deductionsTitle
    }

    "have a what to do next section" which {
      lazy val whatToDoNext = doc.select("#whatToDoNext")

      "have a heading with the class 'heading-medium'" in {
        whatToDoNext.select("h2").attr("class") shouldBe "heading-medium"
      }

      "has the heading 'What to do next'" in {
        whatToDoNext.select("h2").text() shouldBe messages.Summary.whatToDoNextText
      }

      "has a link to 'https://www.gov.uk/guidance/capital-gains-tax-for-non-residents-uk-residential-property'" in {
        whatToDoNext.select("a").attr("href") shouldBe "https://www.gov.uk/guidance/capital-gains-tax-for-non-residents-uk-residential-property"
      }

      "should have the text describing what to do next" in {
        whatToDoNext.select("p").text() shouldBe s"${messages.Summary.whatToDoNextContent} ${messages.Summary.whatToDoNextLink} ${messages.externalLink}"
      }
    }

    "have a link to start again" which {
      lazy val startAgain = doc.select("#startAgain")

      "have a class of bold-medium" in {
        startAgain.attr("class") shouldBe "bold-medium"
      }

      "have the text 'Start Again" in {
        startAgain.text() shouldBe messages.Summary.startAgain
      }

      "have a link to /calculate-your-capital-gains/non-resident/restart" in {
        startAgain.attr("href") shouldBe controllers.nonresident.routes.SummaryController.restart().url
      }
    }

    "have a save pdf button" which {
      lazy val savePDF = doc.select("a.button")

      "which has the class 'button save-pdf-button'" in {
        savePDF.attr("class") shouldBe "button nr save-pdf-button"
      }

      "which has the text 'Save as PDF'" in {
        savePDF.text() shouldBe messages.Summary.saveAsPdf
      }

      "which has the link to the summary report" in {
        savePDF.attr("href") shouldBe controllers.nonresident.routes.ReportController.summaryReport().url
      }
    }
  }

}

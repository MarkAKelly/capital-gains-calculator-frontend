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

import assets.MessageLookup.{NonResident => nrMessages}
import common.TestModels._
import controllers.helpers.FakeRequestHelper
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.summaryReport

class SummaryReportViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "In CalculationController calling the .summary action" when {

    "return some HTML that" should {

      lazy val taxYear = TaxYearModel("2016/17", true, "2016/17")

      lazy val view = summaryReport(businessScenarioOneModel, calcModelTwoRates, taxYear,
        sumModelFlat.calculationElectionModel.calculationType)(fakeRequestWithSession)
      lazy val document = Jsoup.parse(view.body)

      "have the correct sub-heading 'You owe'" in {
        document.select("h1 span").text shouldEqual nrMessages.Summary.secondaryHeading
      }

      "have a result amount currently set to £8,000.00" in {
        document.select("h1 b").text shouldEqual "£8,000.00"
      }

      "have the HMRC logo with the HMRC name" in {
        document.select("div.logo span").text shouldBe "HM Revenue & Customs"
      }

      "does not have a notice summary" in {
        document.select("div.notice-wrapper").isEmpty shouldBe true
      }

      "have a 'Calculation details' section that" in {
        document.select("#calculationDetails span.heading-large").text should include(nrMessages.Summary.calculationDetailsTitle)
      }

      "have a 'Personal details' section that" in {
        document.select("#personalDetails span.heading-large").text should include(nrMessages.Summary.personalDetailsTitle)
      }

      "have a 'Purchase details' section that" in {
        document.select("#acquisitionDetails span.heading-large").text should include(nrMessages.Summary.purchaseDetailsTitle)
      }

      "have a 'Property details' section that" in {
        document.select("#propertyDetails span.heading-large").text should include(nrMessages.Summary.propertyDetailsTitle)
      }

      "have a 'Sale details' section that" in {
        document.select("#saleDetails span.heading-large").text should include(nrMessages.Summary.saleDetailsTitle)
      }

      "have a 'Deductions details' section that" in {
        document.select("#deductions span.heading-large").text should include(nrMessages.Summary.deductionsTitle)
      }

      "have a what to do next section that " should {

        lazy val whatToDoNext = document.select("#whatToDoNext")

        "have a div with the id of 'whatToDoNext'" in {
          whatToDoNext.select("h2").isEmpty shouldBe false
        }

        "have the text 'You need to tell HMRC about the property'" in {
          whatToDoNext.select("h2").text shouldBe nrMessages.whatToDoNextTextTwo
        }

        "have the text 'Further details on how to tell HMRC about this property can be found at'" in {
          whatToDoNext.select("p").text should include (nrMessages.whatToDoNextFurtherDetails)
        }

        "have the correct link" in {
          whatToDoNext.select("a").text shouldBe "https://www.gov.uk/guidance/capital-gains-tax-for-non-residents-uk-residential-property"
        }
      }
    }
  }
}

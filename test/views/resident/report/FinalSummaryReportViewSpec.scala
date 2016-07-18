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

package views.resident.report

import common.Dates
import controllers.helpers.FakeRequestHelper
import models.resident.income.{PersonalAllowanceModel, CurrentIncomeModel, PreviousTaxableGainsModel}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import assets.{MessageLookup => commonMessaages}
import assets.MessageLookup.{summary => messages}
import common.Dates._
import models.resident._
import org.jsoup.Jsoup
import views.html.calculation.resident.report.finalSummaryReport

class FinalSummaryReportViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Final Summary view" should {

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
      None
    )

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    lazy val view = finalSummaryReport(gainAnswers, deductionAnswers, incomeAnswers, results, taxYearModel)(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    "have a page heading" which {

      s"includes a secondary heading with text '${messages.pageHeading}'" in {
        doc.select("h1 span.pre-heading").text shouldBe messages.pageHeading
      }

      "includes an amount of tax due of £3,600.00" in {
        doc.select("h1").text should include ("£3,600.00")
      }
    }

    "have the HMRC logo with the HMRC name" in {
      doc.select("div.logo span").text shouldBe "HM Revenue & Customs"
    }

    "does not have a notice summary" in {
      doc.select("div.notice-wrapper").isEmpty shouldBe true
    }

    "have a section for the Calculation Details" which {

      "has the class 'summary-section' to underline the heading" in {
        doc.select("section#calcDetails h2").hasClass("summary-underline") shouldBe true
      }


    }
  }
}

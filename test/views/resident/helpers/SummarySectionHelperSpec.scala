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

package views.resident.helpers

import models.SummaryDataItemModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.helpers.resident.summarySectionHelper
import assets.{MessageLookup => commonMessages}

class SummarySectionHelperSpec extends UnitSpec with WithFakeApplication {

  lazy val TestObject = summarySectionHelper(
    "TestID",
    "TestTitle",
    Array(
      SummaryDataItemModel("Question1","Answer1",None),
      SummaryDataItemModel("Question2","Answer2",Some("TestLink"))
    )
  )
  lazy val doc = Jsoup.parse(TestObject.body)

  "The summary section helper" should {

    s"have a section tag" which {

      "has the id 'TestID'" in {
        doc.select("section").attr("id") shouldBe "TestID"
      }

      s"contains a h2 tag" which {

        s"should have the title 'TestTitle'" in {
          doc.select("section#TestID h2").text shouldBe "TestTitle"
        }

        "has the class 'heading-large'" in {
          doc.select("section#TestID h2").hasClass("heading-large") shouldBe true
        }

        "has the class 'summary-underline'" in {
          doc.select("section#TestID h2").hasClass("summary-underline") shouldBe true
        }
      }
    }

    "have a stacked grid layout div" which {

      "has a half page width sized column" which {

        "includes the question 'Question1'" in {
          doc.select("#question-1").text shouldBe "Question1"
        }

        "includes a span for the first answer" which {

          lazy val answer = doc.select("#answer-1 span")

          "has the text 'Answer1'" in {
            answer.text shouldBe "Answer1"
          }

          "has the class 'bold-medium'" in {
            answer.hasClass("bold-medium")
          }
        }

        "includes the question 'Question2'" in {
          doc.select("#question-2").text shouldBe "Question2"
        }

        "includes a span for the answer" which {

          lazy val answer = doc.select("#answer-2 span")

          "has the text 'Answer2'" in {
            answer.text shouldBe "Answer2"
          }

          "has the class 'bold-medium'" in {
            answer.hasClass("bold-medium")
          }
        }

        "includes a change link for the second answer" which {

          lazy val changeLink = doc.select("#answer-2 a")

          s"has the text '${commonMessages.calcBaseChange}'" in {
            changeLink.text shouldBe commonMessages.calcBaseChange
          }

          "has a link to 'TestLink'" in {
            changeLink.attr("href") shouldBe "TestLink"
          }

          "has the class 'font-xsmall'" in {
            changeLink.hasClass("font-xsmall") shouldBe true
          }
        }
      }
    }
  }
}

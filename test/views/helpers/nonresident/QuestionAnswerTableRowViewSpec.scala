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

package views.helpers.nonresident

import models.nonresident.QuestionAnswerModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.UnitSpec
import views.html.helpers.nonresident.questionAnswerTableRow

/**
  * Created by emma on 15/11/16.
  */
class QuestionAnswerTableRowViewSpec extends UnitSpec {
  "Creating questionAnswerTableRow" when {
    "passing in a String answer" should {
      val model = QuestionAnswerModel[String]("id", "answer", "question", Some("change-link"))
      val result = questionAnswerTableRow(model)
      val doc = Jsoup.parse(result.body)

      "have a table row with a table row for the question with ID id-question" which {
        "has a question column with the question 'question'" in {
          doc shouldBe ""
        }
      }
    }
  }
}

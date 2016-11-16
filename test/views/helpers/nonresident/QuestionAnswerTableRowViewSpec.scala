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
import java.time.LocalDate

/**
  * Created by emma on 15/11/16.
  */
class QuestionAnswerTableRowViewSpec extends UnitSpec {

  def assertDataColumnContents(expectedOutput: String, model:QuestionAnswerModel[Any]): Unit ={
    lazy val result = questionAnswerTableRow(Seq(model))
    lazy val doc = Jsoup.parse(result.body)
    //function assumes sequence of one only being passed, purpose specific for testing data conversion functionality only

    lazy val dataColumnContents = doc.select(s"${model.id}-data").text()

    s"generate a row with a data column with the data '${expectedOutput}'" in {
      dataColumnContents shouldBe ""
    }
  }

  "Creating a single table of one row" when {
    "passing in a String answer" should {
      val model = Seq(QuestionAnswerModel[String]("id", "answer", "question", Some("change-link")))
      val result = questionAnswerTableRow(model)
      val doc = Jsoup.parse(result.body)

      "have a table row with a table row for the question with ID id-question" which {
        lazy val row = doc.select("#id")
        "has a question column with the question 'question'" in {
          doc.select("#id-question").text() shouldBe "question"
        }

        "has a data column with the data 'answer'" in {
          doc.select("#id-data").text() shouldBe "answer"
        }

        "has a change link column with a hyper-link" which {
          lazy val changeLink = row.select("#id-changeLink")
          "has the hyper-link text 'Change'" in {
            //needs to be a message
            changeLink.text() shouldBe "Change"
          }

          "has a link to 'change-link'" in {
            changeLink.select("a").attr("href") shouldBe "change-link"
          }
        }
      }
    }

      "passing in a Int answer" should {
        val model = QuestionAnswerModel[Int]("id", 200, "question", Some("change-link"))

        assertDataColumnContents("200", model)
      }

      "passing in a BigDecimal answer" should {
        val model = QuestionAnswerModel[BigDecimal]("id", BigDecimal(1000.01), "question", Some("change-link"))

        assertDataColumnContents("£1,000.01", model)
      }

      "passing in a Date answer" should {
        val model = QuestionAnswerModel[LocalDate]("id", LocalDate.parse("2016-05-04"), "question", Some("change-link"))

        assertDataColumnContents("4 May 2016", model)
      }

      "passing in a Boolean answer" should {
        val model = QuestionAnswerModel[Boolean]("id", true, "question", Some("change-link"))

        assertDataColumnContents("Yes", model)
      }

      "passing in a non-matching type" should {
        val model = Seq(QuestionAnswerModel[Double]("id", 50.2, "question", Some("change-link")))
        val result = questionAnswerTableRow(model)
        val doc = Jsoup.parse(result.body)

        "generate a data column with a blank answer" in {
          doc.select("id-data").text() shouldBe ""
        }
      }
  }

  "Creating a table of multiple rows" should {
    val idString = "stringQA"
    val idBoolean = "booleanQA"

    val model = Seq(QuestionAnswerModel[String](idString, "answer", "question", Some("change-link")),
      QuestionAnswerModel[Boolean](idBoolean, false, "question", Some("change-link-diff")))
    val result = questionAnswerTableRow(model)
    val doc = Jsoup.parse(result.body)

    s"have a table row with a table row for the question with ID ${idString}" which {
      lazy val row = doc.select("#stringQ")
      "has a question column with the question 'question'" in {
        doc.select(s"#${idString}-question").text() shouldBe "question"
      }

      "has a data column with the data 'answer'" in {
        doc.select(s"#${idString}-data").text() shouldBe "answer"
      }

      "has a change link column with a hyper-link" which {
        lazy val changeLink = row.select(s"#${idString}-changeLink")
        "has the hyper-link text 'Change'" in {
          //needs to be a message
          changeLink.text() shouldBe "Change"
        }

        "has a link to 'change-link'" in {
          changeLink.select("a").attr("href") shouldBe "change-link"
        }
      }
    }

    s"have a table row with a table row for the question with ID ${idBoolean}" which {
      lazy val row = doc.select("#stringQ")
      "has a question column with the question 'question'" in {
        doc.select(s"#${idBoolean}-question").text() shouldBe "question"
      }

      "has a data column with the data 'No'" in {
        doc.select(s"#${idBoolean}-data").text() shouldBe "No"
      }

      "has a change link column with a hyper-link" which {
        lazy val changeLink = row.select(s"#${idBoolean}-changeLink")
        "has the hyper-link text 'Change'" in {
          //needs to be a message
          changeLink.text() shouldBe "Change"
        }

        "has a link to 'change-link-diff'" in {
          changeLink.select("a").attr("href") shouldBe "change-link-diff"
        }
      }
    }
  }
}

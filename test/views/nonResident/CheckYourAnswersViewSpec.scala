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

import assets.MessageLookup
import controllers.helpers.FakeRequestHelper
import models.nonresident.QuestionAnswerModel
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.checkYourAnswers

/**
  * Created by emma on 14/11/16.
  */
class CheckYourAnswersViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {
    val answersSequence = Seq(QuestionAnswerModel("dummyId", 200, "dummyQuestion", None))
    "The check your answers view" when {
      "provided with a valid sequence of question answers" should {
        lazy val view = checkYourAnswers(answersSequence, "hello")(fakeRequestWithSession)

        lazy val document = Jsoup.parse(view.body)
        "have a heading" which {
          lazy val heading = document.select("h1")
          s"has the title text ${MessageLookup.NonResident.CheckYourAnswers.heading}" in {
            heading.text() shouldBe MessageLookup.NonResident.CheckYourAnswers.heading
          }

          "has a class of 'heading-xlarge'" in {
            heading.attr("class") shouldBe "heading-xlarge"
          }
        }
      }
    }
}

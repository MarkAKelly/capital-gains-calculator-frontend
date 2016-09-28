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

package views.resident.properties.gain

import assets.baseGainPageMessages
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup.nodes.Document
import assets.{MessageLookup => commonMessages}

/**
  * Created by emma on 28/09/16.
  */
trait BaseGainViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  def standardGainView(doc: => Document, title: String, messages: baseGainPageMessages): Unit = {

    s"The '$title' view" should {
      "have charset UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
        println("CUSTOM TEST VERSION 2.0!!!!")
      }
      s"Have the title '$messages.title' " in {
        doc.title shouldBe messages.title
      }
    }

    "have a back button that" should {

      lazy val backLink = doc.select("a#back-link")

      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.calcBaseBack
      }

      "have the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "have a link to Bought For Less Than Worth" in {
        backLink.attr("href") shouldBe controllers.resident.properties.routes.GainController.boughtForLessThanWorth().toString
      }
    }

    "have a H1 tag that" should {
      lazy val heading = doc.select("h1")

      s"have the page heading '${messages.pageHeading}'" in {
        heading.text shouldBe messages.pageHeading
      }

      "have the heading-large class" in {
        heading.hasClass("heading-large") shouldBe true
      }
    }
  }
}

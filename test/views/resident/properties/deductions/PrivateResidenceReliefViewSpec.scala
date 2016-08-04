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

package views.resident.properties.deductions

import assets.MessageLookup.{privateResidenceRelief => messages}
import assets.{MessageLookup => commonMessages}
import controllers.helpers.FakeRequestHelper
import forms.resident.properties.PrivateResidenceReliefForm._
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.properties.{deductions => views}

class PrivateResidenceReliefViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Allowable Losses Value view with no form errors" should {

    lazy val form = privateResidenceReliefForm
    lazy val view = views.privateResidenceRelief(
      form,
      "home",
      Some("back"))(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title of ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    s"have the home link too 'home'" in {
      doc.select("#homeNavHref").attr("href") shouldEqual "home"
    }

    "have a back button" which {

      lazy val backLink = doc.select("a#back-link")

      "has the correct back link text" in {
        backLink.text shouldBe commonMessages.calcBaseBack
      }

      "has the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "has a back link to 'back'" in {
        backLink.attr("href") shouldBe "back"
      }
    }

    "have a H1 tag that" should {

      lazy val h1Tag = doc.select("h1")

      s"have the page heading '${messages.title}'" in {
        h1Tag.text shouldBe messages.title
      }

      "have the heading-large class" in {
        h1Tag.hasClass("heading-large") shouldBe true
      }
    }

    "have a set of radio inputs" which {

      "are surrounded in a div with class form-group" in {
        doc.select("div#radio-input").hasClass("form-group") shouldEqual true
      }

      "for the option 'Yes claiming full prr'" should {

        lazy val fullRadioOption = doc.select(".block-label[for=prrClaiming-full]")

        "have a label with class 'block-label'" in {
          fullRadioOption.hasClass("block-label") shouldEqual true
        }

        "have the property 'for'" in {
          fullRadioOption.hasAttr("for") shouldEqual true
        }

        "the for attribute has the value prrClaiming-Full" in {
          fullRadioOption.attr("for") shouldEqual "prrClaiming-full"
        }

        "have the text 'Yes, full relief'" in {
          fullRadioOption.text shouldEqual "Yes, full relief"
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#prrClaiming-full")

          "have the id 'prrClaiming-Full'" in {
            optionLabel.attr("id") shouldEqual "prrClaiming-full"
          }

          "have the value 'Full'" in {
            optionLabel.attr("value") shouldEqual "Full"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }

      "for the option 'Yes claiming part prr'" should {

        lazy val fullRadioOption = doc.select(".block-label[for=prrClaiming-part]")

        "have a label with class 'block-label'" in {
          fullRadioOption.hasClass("block-label") shouldEqual true
        }

        "have the property 'for'" in {
          fullRadioOption.hasAttr("for") shouldEqual true
        }

        "the for attribute has the value prrClaiming-part" in {
          fullRadioOption.attr("for") shouldEqual "prrClaiming-part"
        }

        "have the text 'Yes, part relief'" in {
          fullRadioOption.text shouldEqual "Yes, part relief"
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#prrClaiming-part")

          "have the id 'prrClaiming-part'" in {
            optionLabel.attr("id") shouldEqual "prrClaiming-part"
          }

          "have the value 'Part'" in {
            optionLabel.attr("value") shouldEqual "Part"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }

      "for the option 'No not claiming prr'" should {

        lazy val fullRadioOption = doc.select(".block-label[for=prrClaiming-none]")

        "have a label with class 'block-label'" in {
          fullRadioOption.hasClass("block-label") shouldEqual true
        }

        "have the property 'for'" in {
          fullRadioOption.hasAttr("for") shouldEqual true
        }

        "the for attribute has the value prrClaiming-none" in {
          fullRadioOption.attr("for") shouldEqual "prrClaiming-none"
        }

        "have the text 'No'" in {
          fullRadioOption.text shouldEqual "No"
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#prrClaiming-none")

          "have the id 'prrClaiming-none'" in {
            optionLabel.attr("id") shouldEqual "prrClaiming-none"
          }

          "have the value 'None'" in {
            optionLabel.attr("value") shouldEqual "None"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }
    }
  }
}
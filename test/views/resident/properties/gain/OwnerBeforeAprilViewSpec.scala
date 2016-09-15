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

import controllers.helpers.FakeRequestHelper
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import forms.resident.properties.gain.OwnerBeforeAprilForm._
import models.resident.properties.gain.OwnerBeforeAprilModel
import views.html.calculation.resident.properties.{gain => views}
import assets.MessageLookup.{ownerBeforeAprilNineteenEightyTwo => messages}
import assets.{MessageLookup => commonMessages}
import org.jsoup.Jsoup

/**
  * Created by david on 14/09/16.
  */
class OwnerBeforeAprilViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "The Owner Before April view" should {


    lazy val view = views.ownerBeforeApril(ownerBeforeAprilForm)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)
    lazy val form = doc.getElementsByTag("form")

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title shouldBe messages.title
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

    "have a back button" which {

      lazy val backLink = doc.select("a#back-link")

      "has the correct back link text" in {
        backLink.text shouldBe commonMessages.calcBaseBack
      }

      "has the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      s"has a back link to '${controllers.resident.properties.routes.GainController.disposalCosts().toString}'" in {
        backLink.attr("href") shouldBe controllers.resident.properties.routes.GainController.disposalCosts().toString
      }
    }

    "render a form tag with a submit action" in {
      doc.select("form").attr("action") shouldEqual "/calculate-your-capital-gains/resident/properties/owner-before-april"
    }

    "has the method of POST" in {
      form.attr("method") shouldBe "POST"
    }

    "have a legend for the radio inputs" which {

      lazy val legend = doc.select("legend")

      s"contain the text ${messages.title}" in {
        legend.text should include(s"${messages.title}")
      }

      "that is visually hidden" in {
        legend.hasClass("visuallyhidden") shouldEqual true
      }
    }

    "have a set of radio inputs" which {

      "are surrounded in a fieldset" which {

        "has the class form-group" in {
          doc.select("#ownedBeforeAprilNineteenEightyTwo").hasClass("form-group") shouldEqual true
        }

        "has the class inline" in {
          doc.select("#ownedBeforeAprilNineteenEightyTwo").hasClass("inline") shouldEqual true
        }
      }

      "for the option 'Yes'" should {

        lazy val YesRadioOption = doc.select(".block-label[for=ownedBeforeAprilNineteenEightyTwo-yes]")

        "have a label with class 'block-label'" in {
          YesRadioOption.hasClass("block-label") shouldEqual true
        }

        "have the property 'for'" in {
          YesRadioOption.hasAttr("for") shouldEqual true
        }

        "the for attribute has the value ownedBeforeAprilNineteenEightyTwo-Yes" in {
          YesRadioOption.attr("for") shouldEqual "ownedBeforeAprilNineteenEightyTwo-yes"
        }

        "have the text 'Yes'" in {
          YesRadioOption.text shouldEqual "Yes"
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#ownedBeforeAprilNineteenEightyTwo-yes")

          "have the id 'ownedBeforeAprilNineteenEightyTwo-Yes'" in {
            optionLabel.attr("id") shouldEqual "ownedBeforeAprilNineteenEightyTwo-yes"
          }

          "have the value 'Yes'" in {
            optionLabel.attr("value") shouldEqual "Yes"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }

      "for the option 'No'" should {

        lazy val NoRadioOption = doc.select(".block-label[for=ownedBeforeAprilNineteenEightyTwo-no]")

        "have a label with class 'block-label'" in {
          NoRadioOption.hasClass("block-label") shouldEqual true
        }

        "have the property 'for'" in {
          NoRadioOption.hasAttr("for") shouldEqual true
        }

        "the for attribute has the value ownedBeforeAprilNineteenEightyTwo-No" in {
          NoRadioOption.attr("for") shouldEqual "ownedBeforeAprilNineteenEightyTwo-no"
        }

        "have the text 'No'" in {
          NoRadioOption.text shouldEqual "No"
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#ownedBeforeAprilNineteenEightyTwo-no")

          "have the id 'livedInProperty-No'" in {
            optionLabel.attr("id") shouldEqual "ownedBeforeAprilNineteenEightyTwo-no"
          }

          "have the value 'No'" in {
            optionLabel.attr("value") shouldEqual "No"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }
    }

    "have a continue button" which {

      lazy val button = doc.select("button")

      "has class 'button'" in {
        button.hasClass("button") shouldEqual true
      }

      "has attribute 'type'" in {
        button.hasAttr("type") shouldEqual true
      }

      "has type value of 'submit'" in {
        button.attr("type") shouldEqual "submit"
      }

      "has attribute id" in {
        button.hasAttr("id") shouldEqual true
      }

      "has id equal to continue-button" in {
        button.attr("id") shouldEqual "continue-button"
      }

      s"has the text ${commonMessages.calcBaseContinue}" in {
        button.text shouldEqual s"${commonMessages.calcBaseContinue}"
      }
    }
  }

  "ownedBeforeAprilNineteenEightyTwo view with a filled form" which {

    "for the option 'Yes'" should {
      lazy val view = views.ownerBeforeApril(ownerBeforeAprilForm.fill(OwnerBeforeAprilModel(true)))(fakeRequest)
      lazy val doc = Jsoup.parse(view.body)
      lazy val YesRadioOption = doc.select(".block-label[for=ownedBeforeAprilNineteenEightyTwo-yes]")

      "have the option auto-selected" in {
        YesRadioOption.attr("class") shouldBe "block-label selected"
      }
    }

    "for the option 'No'" should {
      lazy val view = views.ownerBeforeApril(ownerBeforeAprilForm.fill(OwnerBeforeAprilModel(false)))(fakeRequest)
      lazy val doc = Jsoup.parse(view.body)
      lazy val NoRadioOption = doc.select(".block-label[for=ownedBeforeAprilNineteenEightyTwo-no]")

      "have the option auto-selected" in {
        NoRadioOption.attr("class") shouldBe "block-label selected"
      }
    }
  }

  "ownedBeforeAprilNineteenEightyTwo view with form errors" should {

    lazy val form = ownerBeforeAprilForm.bind(Map("ownedBeforeAprilNineteenEightyTwo" -> ""))
    lazy val view = views.ownerBeforeApril(form)(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have an error summary" which {
      "display an error summary message for the page" in {
        doc.body.select("#ownedBeforeAprilNineteenEightyTwo-error-summary").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select(".form-group .error-notification").size shouldBe 1
      }
    }

  }

}

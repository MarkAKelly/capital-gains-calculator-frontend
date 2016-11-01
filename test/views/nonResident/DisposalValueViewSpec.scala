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

import assets.MessageLookup.{NonResident => messages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.disposalValue
import forms.nonresident.DisposalValueForm._

class DisposalValueViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  "Disposal value view" when {

    "supplied with no errors" should {
      lazy val view = disposalValue(disposalValueForm)(fakeRequest)
      lazy val document = Jsoup.parse(view.body)

      s"have a title of '${messages.DisposalValue.question}'" in {
        document.title() shouldBe messages.DisposalValue.question
      }

      "have a back link" which {
        lazy val backLink = document.body().select("#back-link")

        "has the text" in {
          backLink.text shouldBe messages.back
        }

        s"has a route to 'disposal-date'" in {
          backLink.attr("href") shouldBe controllers.nonresident.routes.DisposalDateController.disposalDate().url
        }
      }

      "have a heading" which {
        lazy val heading = document.body().select("h1")

        "has a class of heading-large" in {
          heading.attr("class") shouldBe "heading-large"
        }

        s"has the text '${messages.pageHeading}'" in {
          heading.text shouldBe messages.pageHeading
        }
      }

      s"have the question '${messages.DisposalValue.question}'" in {
        document.body.select("label span").first().text shouldBe messages.DisposalValue.question
      }

      "have additional content" which {
        s"has a paragraph with the text ${messages.DisposalValue.bulletIntro}" in {
          document.body().select("#bullet-list-title").text() shouldBe messages.DisposalValue.bulletIntro
        }

        "has a bullet list" which {
          lazy val bulletList = document.body().select("form ul")

          "has a class of 'list-bullet'" in {
            bulletList.attr("class") shouldBe "list-bullet"
          }

          "has three bullet points" in {
            bulletList.select("li").size() shouldBe 3
          }

          s"has a bullet point with the message ${messages.DisposalValue.bulletOne}" in {
            bulletList.select("li").text() should include (messages.DisposalValue.bulletOne)
          }

          s"has a bullet point with the message ${messages.DisposalValue.bulletTwo}" in {
            bulletList.select("li").text() should include (messages.DisposalValue.bulletTwo)
          }

          s"has a bullet point with the message ${messages.DisposalValue.bulletThree}" in {
            bulletList.select("li").text() should include (messages.DisposalValue.bulletThree)
          }

          "has a link" which {
            lazy val link = bulletList.select("#lossesLink")

            "has a class of 'external-link'" in {
              link.attr("class") shouldBe "external-link"
            }

            "has a rel of 'external'" in {
              link.attr("rel") shouldBe "external"
            }

            "has a target of '_blank'" in {
              link.attr("target") shouldBe "_blank"
            }

            "has an href of 'https://www.gov.uk/capital-gains-tax/losses'" in {
              link.attr("href") shouldBe "https://www.gov.uk/capital-gains-tax/losses"
            }

            "has the correct link text" in {
              link.text() shouldBe s"${messages.DisposalValue.bulletTwoLink} ${messages.externalLink}"
            }
          }
        }
      }

      "have an input with the id 'disposalValue" in {
        document.body().select("input").attr("id") shouldBe "disposalValue"
      }

      "have a form" which {
        lazy val form = document.body().select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of '${controllers.nonresident.routes.DisposalValueController.submitDisposalValue().url}'" in {
          form.attr("action") shouldBe controllers.nonresident.routes.DisposalValueController.submitDisposalValue().url
        }
      }

      "have a button" which {
        lazy val button = document.select("button")

        "has the class 'button'" in {
          button.attr("class") shouldBe "button"
        }

        "has the type 'submit'" in {
          button.attr("type") shouldBe "submit"
        }

        "has the id 'continue-button'" in {
          button.attr("id") shouldBe "continue-button"
        }
      }
    }
  }
}

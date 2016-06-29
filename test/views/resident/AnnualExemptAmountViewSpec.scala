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

package views.resident

import assets.MessageLookup.{annualExemptAmount => messages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{resident => views}

class AnnualExemptAmountViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Reliefs view" should {

    lazy val view = views.annualExemptAmount()(fakeRequest)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }
  }
}

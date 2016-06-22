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

import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}

class DisposalDateViewSpec extends UnitSpec with WithFakeApplication {

  "Disposal Date view" should {

    "have charset UTF-8" in {
      val view = views.html.calculation.resident.disposalDate()
      val doc = Jsoup.parse(view.body)
      doc.charset().toString shouldBe "UTF-8"
    }
  }
}

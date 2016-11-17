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
 * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms.nonResident

import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class WorthBeforeLegislationStartFormSpec extends UnitSpec with WithFakeApplication {

  "The Worth Before Legislation Start Form" when {

    "passing in a valid model" should {
      val model = WorthBeforeLegislationStartModel(250000)
      lazy val form = worthBeforeLegislationStartForm.fill(model)

      "return a form with 0 errors" in {
        form.errors.size shouldBe 0
      }

      "return a form containing 250000" in {
        form.data shouldBe Map("worthBeforeLegislationStart" -> "250000")
      }
    }

  }

}

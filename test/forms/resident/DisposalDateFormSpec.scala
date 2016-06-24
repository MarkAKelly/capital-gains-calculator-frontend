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

package forms.resident

import models.resident.DisposalDateModel
import uk.gov.hmrc.play.test.UnitSpec
import forms.resident.DisposalDateForm._

class DisposalDateFormSpec extends UnitSpec {

  "Creating the form for the disposal date" should {
    "return a populated form using .fill" in {
      val model = DisposalDateModel(10, 10, 2016)
      val form = disposalDateForm.fill(model)
      form.value.get shouldBe DisposalDateModel(10, 10, 2016)
    }

    "return a Some if a model with valid inputs is supplied using .bind" in {
      val map = Map(("disposalDateDay", "10"), ("disposalDateMonth", "10"), ("disposalDateYear", "2016"))
      val form = disposalDateForm.bind(map)
      form.value shouldBe Some(DisposalDateModel(10, 10, 2016))
    }

    "return a None if a model with non-numeric inputs is supplied using .bind" in {
      val map = Map(("disposalDateDay", "a"), ("disposalDateMonth", "b"), ("disposalDateYear", "c"))
      val form = disposalDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a non-valid date input is supplied using .bind" in {
      val map = Map(("disposalDateDay", "32"), ("disposalDateMonth", "4"), ("disposalDateYear", "2016"))
      val form = disposalDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty input is supplied using .bind" in {
      val map = Map(("disposalDateDay", ""), ("disposalDateMonth", "4"), ("disposalDateYear", "2016"))
      val form = disposalDateForm.bind(map)
      form.hasErrors shouldBe true
    }
  }
}

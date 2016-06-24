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

import uk.gov.hmrc.play.test.UnitSpec
import models.resident.DisposalValueModel
import forms.resident.DisposalValueForm._

class DisposalValueFormSpec extends UnitSpec {

  "Creating the Form for the Disposal Value" should {
    "return a populated form using .fill" in {
      val model = DisposalValueModel(1.0)
      val form = disposalValueForm.fill(model)
      form.value.get shouldBe DisposalValueModel(1.0)
    }

    "return a None if a model without a numeric value is supplied using .bind" in {
      val map = Map(("amount", ""))
      val form = disposalValueForm.bind(map)
      form.value shouldBe None
    }
  }
}

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

import common.Transformers._
import common.Validation._
import models.resident.AcquisitionValueModel
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages

object AcquisitionValueForm {

  val acquisitionValueForm = Form(
    mapping(
      "amount" -> text
        .verifying(Messages("calc.base.undefinedMessage"), mandatoryCheck)
        .verifying(Messages("calc.base.undefinedMessage"), bigDecimalCheck)
        .transform[BigDecimal](stringToBigDecimal, _.toString())
        .verifying(Messages("calc.base.undefinedMessage"), maxCheck)
        .verifying(Messages("calc.base.undefinedMessage"), minCheck)
        .verifying(Messages("calc.base.undefinedMessage"), decimalPlacesCheck)
    )(AcquisitionValueModel.apply)(AcquisitionValueModel.unapply)
  )
}

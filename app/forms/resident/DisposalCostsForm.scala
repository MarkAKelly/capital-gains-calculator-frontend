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

import common.Constants
import play.api.data.Forms._
import play.api.data._
import models.resident.DisposalCostsModel
import play.api.i18n.Messages
import common.Validation._
import uk.gov.hmrc.play.views.helpers.MoneyPounds

object DisposalCostsForm {

  val disposalCostsForm = Form(
    mapping(
      "amount" -> text
        .verifying(Messages("calc.base.undefinedMessage"), amt => amt != "")
        .verifying(Messages("calc.base.undefinedMessage"), amt => isBigDecimalNumber(amt))
        .transform[BigDecimal](amt => BigDecimal(amt), amt => amt.toString())
        .verifying(Messages("calc.common.error.maxNumericExceeded") +
          MoneyPounds(Constants.maxNumeric, 0).quantity + " " +
          Messages("calc.common.error.maxNumericExceeded.OrLess"),
          amt => isLessThanEqualMaxNumeric(amt))
        .verifying(Messages("calc.base.underfinedMessage"), amt => isGreaterThanZero(amt))
        .verifying(Messages("calc.base.undefinedMessage"),amt => isMaxTwoDecimalPlaces(amt))
    )(DisposalCostsModel.apply)(DisposalCostsModel.unapply)
  )
}

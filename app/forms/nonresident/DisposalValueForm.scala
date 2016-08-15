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

package forms.nonresident

import common.Constants
import common.Validation._
import models.nonresident.DisposalValueModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import uk.gov.hmrc.play.views.helpers.MoneyPounds

object DisposalValueForm {

  val disposalValueForm = Form(
    mapping(
      "disposalValue" -> bigDecimal
        .verifying(Messages("calc.disposalValue.errorNegative"), isPositive)
        .verifying(Messages("calc.disposalValue.errorDecimalPlaces"), isMaxTwoDecimalPlaces)
        .verifying(Messages("calc.common.error.maxNumericExceeded") + MoneyPounds(Constants.maxNumeric, 0).quantity + " " +
          Messages("calc.common.error.maxNumericExceeded.OrLess"), isLessThanEqualMaxNumeric)
    )(DisposalValueModel.apply)(DisposalValueModel.unapply)
  )
}

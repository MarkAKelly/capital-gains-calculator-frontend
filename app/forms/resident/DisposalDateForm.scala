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
import play.api.data.Forms._
import play.api.data._
import common.Validation._
import play.api.i18n.Messages

object DisposalDateForm {

  val disposalDateForm = Form(
    mapping(
      "disposalDateDay" -> text
        .verifying(Messages("calc.base.undefinedMessage"), day => isNotEmpty(day))
          .verifying(Messages("calc.base.undefinedMessage"), day => isIntNumber(day))
        .transform[Int](day => day.toInt, day => day.toString),
      "disposalDateMonth" -> text
        .verifying(Messages("calc.base.undefinedMessage"), month => isNotEmpty(month))
        .verifying(Messages("calc.base.undefinedMessage"), month => isIntNumber(month))
        .transform[Int](month => month.toInt, month => month.toString),
      "disposalDateYear" -> text
        .verifying(Messages("calc.base.undefinedMessage"), year => isNotEmpty(year))
        .verifying(Messages("calc.base.undefinedMessage"), year => isIntNumber(year))
        .transform[Int](year => year.toInt, year => year.toString)
    )(DisposalDateModel.apply)(DisposalDateModel.unapply)
      .verifying(Messages("calc.base.undefinedMessage"), fields => isValidDate(fields.day, fields.month, fields.year))
  )
}

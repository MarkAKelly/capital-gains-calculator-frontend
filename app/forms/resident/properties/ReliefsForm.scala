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

package forms.resident.properties

import common.Transformers._
import common.Validation._
import models.resident.properties.ReliefsModel
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import uk.gov.hmrc.play.views.helpers.MoneyPounds


object ReliefsForm {

  def reliefsForm(totalGain: BigDecimal): Form[ReliefsModel] = Form(
    mapping(
      "isClaiming" -> text
        .verifying(Messages("calc.resident.reliefs.errorSelect", MoneyPounds(totalGain, 0).quantity), _.nonEmpty)
        .verifying(Messages("calc.resident.reliefs.errorSelect", MoneyPounds(totalGain, 0).quantity), yesNoCheck)
        .transform[Boolean](stringToBoolean, booleanToString)
    )(ReliefsModel.apply)(ReliefsModel.unapply)
  )
}
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
import models.nonresident.OtherReliefsModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import uk.gov.hmrc.play.views.helpers.MoneyPounds

object OtherReliefsForm {

  def validateReliefSupplied(data: OtherReliefsModel) = {
    data.isClaimingOtherReliefs match {
      case Some("Yes") => data.otherReliefs.isDefined
      case _ => true
    }
  }

  def otherReliefsForm (electionMade: Boolean): Form[OtherReliefsModel] = Form (
    mapping(
      "isClaimingOtherReliefs" -> optional(text)
        .verifying(Messages("calc.common.error.fieldRequired"), isClaimingOtherReliefs => if (!electionMade) {isClaimingOtherReliefs.isDefined} else true),
      "otherReliefs" -> optional(bigDecimal)
        .verifying(Messages("calc.otherReliefs.errorMinimum"), otherReliefs => isPositive(otherReliefs.getOrElse(0)))
        .verifying(Messages("calc.otherReliefs.errorDecimal"), otherReliefs => isMaxTwoDecimalPlaces(otherReliefs.getOrElse(0)))
        .verifying(Messages("calc.common.error.maxNumericExceeded") + MoneyPounds(Constants.maxNumeric, 0).quantity + " " + Messages("calc.common.error.maxNumericExceeded.OrLess"),
          otherReliefs => isLessThanEqualMaxNumeric(otherReliefs.getOrElse(0)))
    )(OtherReliefsModel.apply)(OtherReliefsModel.unapply)
      .verifying(Messages("calc.otherReliefs.errorNoValue"),
        otherReliefsForm => if(!electionMade) {validateReliefSupplied(otherReliefsForm)} else true)
  )
}
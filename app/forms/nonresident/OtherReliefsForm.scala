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
import common.Transformers._
import models.nonresident.OtherReliefsModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import uk.gov.hmrc.play.views.helpers.MoneyPounds

object OtherReliefsForm {

  private def isRealNumberOption(electionMade: Boolean): OtherReliefsModel => Boolean = {
    model => (model.isClaimingOtherReliefs, model.otherReliefs) match {
      case (Some("Yes"), _) => model.otherReliefs.isDefined
      case _ if electionMade => model.otherReliefs.isDefined
      case _ => true
    }
  }

  private def isPositiveOption(electionMade: Boolean): OtherReliefsModel => Boolean = {
    model => (model.isClaimingOtherReliefs, model.otherReliefs) match {
      case (Some("Yes"), Some(value)) => isPositive(value)
      case (_, Some(value)) if electionMade => isPositive(value)
      case _ => true
    }
  }

  private def decimalPlacesCheckOption(electionMade: Boolean): OtherReliefsModel => Boolean = {
    model => (model.isClaimingOtherReliefs, model.otherReliefs) match {
      case (Some("Yes"), Some(value)) => decimalPlacesCheck(value)
      case (_, Some(value)) if electionMade => decimalPlacesCheck(value)
      case _ => true
    }
  }

  private def maxNumericOption(electionMade: Boolean): OtherReliefsModel => Boolean = {
    model => (model.isClaimingOtherReliefs, model.otherReliefs) match {
      case (Some("Yes"), Some(value)) => maxCheck(value)
      case (_, Some(value)) if electionMade => maxCheck(value)
      case _ => true
    }
  }

  def otherReliefsForm (electionMade: Boolean): Form[OtherReliefsModel] =
    Form(
      mapping(
        "isClaimingOtherReliefs" -> optional(text)
          .verifying(Messages("calc.common.error.fieldRequired"), isClaimingOtherReliefs => if (!electionMade) {isClaimingOtherReliefs.isDefined} else true)
          .verifying(Messages("calc.common.error.fieldRequired"), isYesNoOption(electionMade)),
        "otherReliefs" -> text
          .transform(stringToOptionBigDecimal, optionBigDecimalToString)
      )(OtherReliefsModel.apply)(OtherReliefsModel.unapply)
        .verifying(Messages("error.real"), isRealNumberOption(electionMade))
        .verifying(Messages("calc.otherReliefs.errorMinimum"), isPositiveOption(electionMade))
        .verifying(Messages("calc.otherReliefs.errorDecimal"), decimalPlacesCheckOption(electionMade))
        .verifying(Messages("calc.common.error.maxNumericExceeded") + MoneyPounds(Constants.maxNumeric, 0).quantity +
          " " + Messages("calc.common.error.maxNumericExceeded.OrLess"),
          maxNumericOption(electionMade))
    )
}
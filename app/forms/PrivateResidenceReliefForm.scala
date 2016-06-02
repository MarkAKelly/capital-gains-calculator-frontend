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

package forms

import common.Constants
import play.api.data._
import play.api.data.Forms._
import models._
import common.Validation._
import play.api.i18n.Messages

object PrivateResidenceReliefForm {

  def verifyAmountSupplied(data: PrivateResidenceReliefModel, showBefore: Boolean, showAfter: Boolean): Boolean = {
    data.isClaimingPRR match {
      case "Yes" if showBefore && showAfter => data.daysClaimed.isDefined
      case "Yes" if showBefore ^ showAfter => data.daysClaimed.isDefined || data.daysClaimedAfter.isDefined
      case _ => true
    }
  }

  def verifyPositive (data: PrivateResidenceReliefModel, showBefore: Boolean, showAfter: Boolean): Boolean = {
    data.isClaimingPRR match {
      case "Yes" if showBefore || showAfter => isPositive(data.daysClaimed.getOrElse(0)) && isPositive(data.daysClaimedAfter.getOrElse(0))
      case _ => true
    }
  }

  def verifyNoDecimalPlaces (data: PrivateResidenceReliefModel, showBefore: Boolean, showAfter: Boolean): Boolean = {
    data.isClaimingPRR match {
      case "Yes" if showBefore || showAfter => hasNoDecimalPlaces(data.daysClaimed.getOrElse(0)) && hasNoDecimalPlaces(data.daysClaimedAfter.getOrElse(0))
      case _ => true
    }
  }

  def validateMax (data: PrivateResidenceReliefModel, showBefore: Boolean, showAfter: Boolean): Boolean = {
    data.isClaimingPRR match {
      case "Yes" if showBefore && showAfter => isLessThanEqualMaxNumeric(data.daysClaimed.getOrElse(0)) && isLessThanEqualMaxNumeric(data.daysClaimedAfter.getOrElse(0))
      case "Yes" if showBefore || showAfter => isLessThanEqualMaxNumeric(data.daysClaimed.getOrElse(0)) && isLessThanEqualMaxNumeric(data.daysClaimedAfter.getOrElse(0))
      case _ => true
    }
  }

  def privateResidenceReliefForm (showBefore: Boolean, showAfter: Boolean): Form[PrivateResidenceReliefModel] = Form(
    mapping(
      "isClaimingPRR" -> nonEmptyText,
      "daysClaimed" -> optional(bigDecimal),
      "daysClaimedAfter" -> optional(bigDecimal)
    )(PrivateResidenceReliefModel.apply)(PrivateResidenceReliefModel.unapply)
      .verifying(Messages("calc.privateResidenceRelief.error.noValueProvided"), improvementsForm => verifyAmountSupplied(improvementsForm, showBefore, showAfter))
      .verifying(Messages("calc.privateResidenceRelief.error.errorNegative"), improvementsForm => verifyPositive(improvementsForm, showBefore, showAfter))
      .verifying(Messages("calc.privateResidenceRelief.error.errorDecimalPlaces"), improvementsForm => verifyNoDecimalPlaces(improvementsForm, showBefore, showAfter))
      .verifying(Messages("calc.privateResidenceRelief.error.maxNumericExceeded") + " " + Constants.maxNumeric + " " + Messages("calc.privateResidenceRelief.error.maxNumericExceeded.OrLess"),
        improvementsForm => validateMax(improvementsForm, showBefore, showAfter)
      )
  )
}

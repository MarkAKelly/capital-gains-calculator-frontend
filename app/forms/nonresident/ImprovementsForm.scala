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
import models.nonresident.ImprovementsModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import uk.gov.hmrc.play.views.helpers.MoneyPounds


object ImprovementsForm {

  def verifyAmountSupplied(data: ImprovementsModel): Boolean = {
    data.isClaimingImprovements match {
      case "Yes" => data.improvementsAmt.isDefined || data.improvementsAmtAfter.isDefined
      case "No" => true
    }
  }

  def verifyPositive(data: ImprovementsModel): Boolean = {
    (data.isClaimingImprovements match {
      case "Yes" => isPositive(data.improvementsAmt.getOrElse(0))
      case "No" => true
    }) && (data.isClaimingImprovements match {
      case "Yes" => isPositive(data.improvementsAmtAfter.getOrElse(0))
      case "No" => true
    })
  }

  def verifyTwoDecimalPlaces(data: ImprovementsModel): Boolean = {
    (data.isClaimingImprovements match {
      case "Yes" => decimalPlacesCheck(data.improvementsAmt.getOrElse(0))
      case "No" => true
    }) && (data.isClaimingImprovements match {
      case "Yes" => decimalPlacesCheck(data.improvementsAmtAfter.getOrElse(0))
      case "No" => true
    })
  }

  def validateMax(data: ImprovementsModel): Boolean = {
    (data.isClaimingImprovements match {
      case "Yes" => maxCheck(data.improvementsAmt.getOrElse(0))
      case "No" => true
    }) && (data.isClaimingImprovements match {
      case "Yes" => maxCheck(data.improvementsAmtAfter.getOrElse(0))
      case "No" => true
    })
  }

  val improvementsForm = Form(
    mapping(
      "isClaimingImprovements" -> text,
      "improvementsAmt" -> optional(bigDecimal),
      "improvementsAmtAfter" -> optional(bigDecimal)
    )(ImprovementsModel.apply)(ImprovementsModel.unapply)
      .verifying(Messages("calc.improvements.error.no.value.supplied"),
        improvementsForm => verifyAmountSupplied(improvementsForm))
      .verifying(Messages("calc.improvements.errorNegative"),
        improvementsForm => verifyPositive(improvementsForm))
      .verifying(Messages("calc.improvements.errorDecimalPlaces"),
        improvementsForm => verifyTwoDecimalPlaces(improvementsForm))
      .verifying(Messages("calc.common.error.maxNumericExceeded")  + MoneyPounds(Constants.maxNumeric, 0).quantity + " " + Messages("calc.common.error.maxNumericExceeded.OrLess"),
        improvementsForm => validateMax(improvementsForm))
  )
}
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
import models.OtherPropertiesModel
import play.api.data.Forms._
import play.api.data._
import common.Validation._
import play.api.i18n.Messages

object OtherPropertiesForm {

  def validate(data: OtherPropertiesModel, showHiddenQuestion: Boolean) = {
    data.otherProperties match {
        case "Yes" if showHiddenQuestion => data.otherPropertiesAmt.isDefined
        case _ => true
    }
  }

  def validateMinimum(data: OtherPropertiesModel, showHiddenQuestion: Boolean) = {
    data.otherProperties match {
        case "Yes" if showHiddenQuestion => isPositive(data.otherPropertiesAmt.getOrElse(0))
        case _ => true
    }
  }

  def validateTwoDec(data: OtherPropertiesModel, showHiddenQuestion: Boolean) = {
    data.otherProperties match {
        case "Yes" if showHiddenQuestion => isMaxTwoDecimalPlaces(data.otherPropertiesAmt.getOrElse(0))
        case _ => true
    }
  }

  def validateMax(data: OtherPropertiesModel, showHiddenQuestion: Boolean) = {
      data.otherProperties match {
        case "Yes" if showHiddenQuestion => isLessThanEqualMaxNumeric(data.otherPropertiesAmt.getOrElse(0))
        case _ => true
      }
  }

  def otherPropertiesForm (showHiddenQuestion: Boolean): Form[OtherPropertiesModel] = Form (
    mapping(
      "otherProperties" -> nonEmptyText,
      "otherPropertiesAmt" -> optional(bigDecimal)
    )(OtherPropertiesModel.apply)(OtherPropertiesModel.unapply).verifying(Messages("calc.otherProperties.errorQuestion"),
      otherPropertiesForm => validate(otherPropertiesForm, showHiddenQuestion))
      .verifying(Messages("calc.otherProperties.errorNegative"),
        otherPropertiesForm => validateMinimum(otherPropertiesForm, showHiddenQuestion))
      .verifying(Messages("calc.otherProperties.errorDecimalPlaces"),
        otherPropertiesForm => validateTwoDec(otherPropertiesForm, showHiddenQuestion))
      .verifying(Messages("calc.common.error.maxNumericExceeded") + Constants.maxNumeric + " " + Messages("calc.common.error.maxNumericExceeded.OrLess"),
        otherPropertiesForm => validateMax(otherPropertiesForm, showHiddenQuestion))
  )
}

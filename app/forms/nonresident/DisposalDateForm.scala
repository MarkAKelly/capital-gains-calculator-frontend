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

import java.util.Date

import common.Dates._
import common.Validation._
import models.nonresident.DisposalDateModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages

object DisposalDateForm {

  def isAfterAcquisitionDate(day: Int, month: Int, year: Int, acquisitionDate: Option[Date]): Boolean = acquisitionDate match {
    case Some(acquisitionDateValue) => constructDate(day,month,year).after(acquisitionDateValue)
    case _ => true
  }

  def disposalDateForm(acquisitionDate: Option[Date]): Form[DisposalDateModel] = Form(
    mapping(
      "disposalDateDay" -> number,
      "disposalDateMonth" -> number,
      "disposalDateYear" -> number
    )(DisposalDateModel.apply)(DisposalDateModel.unapply)
      .verifying(Messages("calc.common.date.error.invalidDate"), fields =>
        isValidDate(fields.day, fields.month, fields.year))
      .verifying(Messages("calc.disposalDate.error.disposalDateAfterAcquisition"), fields =>
        isAfterAcquisitionDate(fields.day, fields.month, fields.year, acquisitionDate))
  )
}
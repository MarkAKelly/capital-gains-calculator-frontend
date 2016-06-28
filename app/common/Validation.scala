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

package common

import java.text.SimpleDateFormat
import scala.util.{Failure, Success, Try}

object Validation {

  def isValidDate(day:Int,month:Int,year:Int): Boolean = {
    try {
      val fmt = new SimpleDateFormat("dd/MM/yyyy")
      fmt.setLenient(false)
      fmt.parse(s"${day}/${month}/${year}")
      true
    } catch {
      case e: Exception => false
    }
  }

  def isMaxTwoDecimalPlaces(amount: BigDecimal): Boolean = {
    amount match {
      case amount if amount.scale <= 2 => true
      case _ => false
    }
  }

  def hasNoDecimalPlaces (amount: BigDecimal): Boolean = {
    amount match {
      case value if amount.scale <= 0 => true
      case _ => false
    }
  }

  def isPositive(amount: BigDecimal): Boolean = {
    amount match {
      case amount if amount < 0 => false
      case _ => true
    }
  }

  def isGreaterThanZero (amount: BigDecimal): Boolean = {
    if (amount > 0) true
    else false
  }

  def isLessThanEqualMaxNumeric(amount: BigDecimal): Boolean = {
    amount <= Constants.maxNumeric
  }

  def isNotEmpty (input: String): Boolean = !input.isEmpty

  def isIntNumber (input: String): Boolean = {
    Try (input.toInt) match {
      case Success(_) => true
      case Failure(_) => false
    }
  }

  def isBigDecimalNumber(input: String): Boolean = {
    Try (BigDecimal(input)) match {
      case Success(_) => true
      case Failure(_) => false
    }
  }

  val bigDecimalCheck: String => Boolean = (input) => Try(BigDecimal(input)) match {
    case Success(_) => true
    case Failure(_) => false
  }

  val mandatoryCheck: String => Boolean = (input) => input.trim != ""

  val decimalPlacesCheck: BigDecimal => Boolean = (input) => input.scale < 3

  val maxCheck: BigDecimal => Boolean = (input) => input <= Constants.maxNumeric

  val minCheck: BigDecimal => Boolean = (input) => input >= 0
}

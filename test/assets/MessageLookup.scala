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

package assets

object MessageLookup {

  //Base messages
  val calcBaseBack = "Back"
  val calcBaseContinue = "Continue"
  val calcBaseExternalLink = "opens in a new window"
  val undefinedMessage = "Undefined message"

  //Common messages

  //Disposal Date messages
  object disposalDate {
    val title = "When did you sign the contract that made someone else the owner?"
    val question = "When did you sign the contract that made someone else the owner?"
    val helpText = "For example, 4 9 2016"
    val day = "Day"
    val month = "Month"
    val year = "Year"
    val invalidDateError = "error-placeholder"
    val emptyDateFieldError = "empty-error-placeholder"
    val realDateError = "Enter a real date"
    val nonNumericError = "non-numeric-placeholder"
  }

  //Disposal Value messages
  object disposalValue {
    val title = "How much did you sell the property for?"
    val question = "How much did you sell the property for?"
    val bulletListTitle = "Put the market value of the property instead if you:"
    val bulletListOne = "gave it away as a gift"
    val bulletListTwo = "sold it to a relative, business partner or"
    val bulletListTwoLink = "someone else you're connected to"
    val bulletListThree = "sold it for less than it’s worth to help the buyer"
  }

  //Disposal Costs messages
  object disposalCosts {
    val title = "How much did you pay in costs when you stopped owning the property?"
  }

  //Acquisition Value messages

  //Acquisition Costs messages
  object acquisitionCosts {
    val title = "How much did you pay in costs when you became the property owner?"
  }

  //Improvements messages

  //Summary messages
  object summary {
    val title = "Summary"
  }
}

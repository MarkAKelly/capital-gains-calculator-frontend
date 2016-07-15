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
  val calcBaseChange = "change"
  val undefinedMessage = "Undefined message"

  //Common messages
  val maxNumericExceededStart = "Enter an amount that's £"
  val maxNumericExceededEnd = "or less"

  object errorMessages {
    val mandatoryAmount = "Enter an amount"
    val minimumAmount = "Enter an amount that's £0 or more"
    val maximumAmount = "Enter an amount that's £1,000,000,000 or less"
    def maximumLimit(limit: String): String = s"Enter an amount that's £$limit or less"
    val invalidAmount = "Enter an amount in the correct format e.g. 10000.00"
    val invalidAmountNoDecimal = "Enter an amount in the correct format e.g. 10000"
  }

  //Disposal Date messages
  object disposalDate {
    val title = "When did you sign the contract that made someone else the owner?"
    val question = "When did you sign the contract that made someone else the owner?"
    val helpText = "For example, 4 9 2016"
    val day = "Day"
    val month = "Month"
    val year = "Year"
    val invalidDayError = "Enter a day"
    val invalidMonthError = "Enter a month"
    val invalidYearError = "Enter a year"
    val realDateError = "Enter a real date"
  }

  //Outside Tax Years messages
  object outsideTaxYears {
    val title = "The date you've entered isn't supported by this calculator"
    def content(year: String): String = s"You can continue to use it, but we'll use the tax rates from the $year tax year."
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
    val bulletEnd = "You can use a valuation from a surveyor or a property website."
  }

  //Disposal Costs messages
  object disposalCosts {
    val title = "How much did you pay in costs when you stopped owning the property?"
    val pageHeading = "How much did you pay in costs when you stopped owning the property?"
    val helpText = "Costs include agent fees, legal fees and surveys"
  }

  //Acquisition Value messages
  object acquisitionValue {
    val title = "How much did you pay for the property?"
    val pageHeading = "How much did you pay for the property?"
    val bulletListTitle = "Put the market value of the property instead if you:"
    val bulletListOne = "inherited it"
    val bulletListTwo = "got it as a gift"
    val bulletListThree = "bought it from a relative, business partner or"
    val bulletListThreeLink = "someone else you're connected to"
    val bulletListFour = "bought it for less than it’s worth because the seller wanted to help you"
    val bulletListFive = "became the owner before 1 April 1982"
    val bulletLink = "https://www.gov.uk/capital-gains-tax/losses"
    val bulletEnd = "You can use a valuation from a surveyor or a property website."
  }

  //Acquisition Costs messages
  object acquisitionCosts {
    val title = "How much did you pay in costs when you became the property owner?"
    val pageHeading = "How much did you pay in costs when you became the property owner?"
    val helpText = "Costs include stamp duty, agent fees, legal fees and surveys"
  }

  //Improvements messages
  object improvementsView {
    val title = "How much have you spent on improvements since you became the property owner?"
    val note = "If you used the market value of the property, tell us how much you've spent on improvements since the date of the valuation."
    val label = "How much have you spent on improvements since you became the property owner?"
    val hint = "Improvements are permanent changes that raise the value of a property, like adding extensions or garages"
  }

  //Summary messages
  object summary {
    val title = "Summary"
    val pageHeading = "Tax owed"
    val calcDetailsHeading = "Calculation details"
    def calcDetailsHeadingDate(input: String): String = s"Calculation details for $input tax year"
    val yourAnswersHeading = "Your answers"
    val totalLoss = "Loss"
    val totalGain = "Total gain"
    val deductions = "Deductions"
    val chargeableLoss = "Carried forward loss"
    val chargeableGain = "Taxable gain"
    val taxRate = "Tax Rate"
    def noticeWarning(input: String) = s"These figures are based on the tax rates from the $input tax year"
    val warning = "Warning"
    val whatToDoNextTitle = "What to do next"
    val whatToDoNextText = "You can tell us about this loss so that you might need to pay less tax in the future."
    val whatNextYouCan = "You can "
    val whatNextLink = "tell us about this loss "
    val whatNextText = "so that you might need to pay less tax in the future."
    val whatToDoNextTextTwo = "You need to tell HMRC about the property."
    val whatToDoNextNoLossText = "As you have no tax to pay, you don't need to tell HMRC about the property."
    val aeaRemaining = "Capital gains tax allowance left"
  }

  //Reliefs messages
  object reliefs {
    val title = "Do you want to claim any tax reliefs on your total gain of £10,000?"
    val questionSummary = "Do you want to claim any tax reliefs on your total gain of £50,000?"
    def question(input: String = "100") = s"Do you want to claim any tax reliefs on your total gain of £$input?"
    val help = "For example, lettings relief"
    val helpOne = "Tax reliefs can lower the amount of tax you owe. For example, you might be able to claim:"
    val helpLinkOne = "Private Residence Relief"
    val helpLinkTwo = "Lettings Relief"
    val helpTwo = "Tax reliefs are different from your Capital Gains Tax Allowance and Personal Allowance."
    def errorSelect(value: String) = s"Tell us if you want to claim any tax reliefs on your total gain of £$value"
  }

  //Reliefs Value messages
  object reliefsValue {
    val title = "How much tax relief are you claiming?"
    val question = "How much tax relief are you claiming?"
  }

  //Other Properties messages
  object otherProperties {
    def title(input: String) = s"In the $input tax year, did you sell or give away anything else that's covered by Capital Gains Tax?"
    def pageHeading(input: String) = s"In the $input tax year, did you sell or give away anything else that's covered by Capital Gains Tax?"
    val help = "This includes things like:"
    val helpOne = "shares"
    val helpTwo = "antiques"
    val helpThree = "other UK residential properties"
    def errorSelect(input: String) = s"Tell us if you sold or gave away anything else that's covered by Capital Gains Tax in the $input tax year"

  }

  //Allowable Losses Value messages
  object allowableLossesValue {
    def title(input: String) = s"What's the total value of your allowable losses from the $input tax year?"
    def question(input: String) = s"What's the total value of your allowable losses from the $input tax year?"
  }

  //Losses Brought Forward messages
  object lossesBroughtForward {
    val title = "Are there any previous losses you want to bring forward?"
    val question = "Are there any previous losses you want to bring forward?"
    def errorSelect(input: String) = s"Tell us if you're claiming any allowable losses from tax years before $input"
  }

  //Allowable Losses messages
  object allowableLosses {
    def title(input: String) = s"Are you claiming any allowable losses from the $input tax year?"
    val helpInfoTitle = "What are allowable losses?"
    val helpInfoSubtitle = "They're losses you've made that:"
    val helpInfoPoint1 = "are covered by Capital Gains Tax"
    val helpInfoPoint2 = "you've declared within 4 years of making the loss"
    val helpInfoPoint3 = "you haven't already used in an allowable losses claim"
    def errorSelect(input: String) = s"Tell us if you're claiming any allowable losses from the $input tax year"
  }

  //Losses Brought Forward Value messages
  object lossesBroughtForwardValue {
    val title = "What's the total value of the loss to bring forward?"
    val question = "What's the total value of the loss to bring forward?"
  }

  //Annual Exempt Amount messages
  object annualExemptAmount {
    val title = "How much of your Capital Gains Tax allowance have you got left?"
    val question = "How much of your Capital Gains Tax allowance have you got left?"
    val help = "This is the amount you can make in capital gains before you have to pay tax."
    val helpOne = "It's £11,100 a year."
    val helpLinkOne = "Tax-free allowances for Capital Gains Tax"
  }

  //Previous Taxable Gains messages
  object previousTaxableGains {
    val title = "What was your taxable gain?"
    val question = "What was your taxable gain?"
    val helpLinkOne = "How to work out your taxable gain"
  }

  //Current Income messages
  object currentIncome {
    def title(input: String) = s"In the $input tax year, what was your income?"
    def question(input: String) = s"In the $input tax year, what was your income?"
    val currentYearTitle = "How much do you expect your income to be in this tax year?"
    val currentYearQuestion = "How much do you expect your income to be in this tax year?"
    val helpText = "Include your salary and any other income, but not the money you made from selling the property."
  }

  //Personal Allowance messages
  object personalAllowance {
    def title(input: String) = s"In the $input tax year, what was your UK Personal Allowance?"
    val inYearTitle = "How much is your UK Personal Allowance?"
    def question(input: String) = s"In the $input tax year, what was your UK Personal Allowance?"
    val inYearQuestion = "How much is your UK Personal Allowance?"
    def help(input: String) = s"This is the amount of your income you don't pay tax on. It's £$input unless you're claiming other allowances."
    val helpLinkOne = "Personal Allowance"
  }
}

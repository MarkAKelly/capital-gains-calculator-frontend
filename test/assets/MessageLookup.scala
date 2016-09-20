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

  // TO MOVE


  //Base messages
  val calcBaseBack = "Back"
  val calcBaseContinue = "Continue"
  val calcBaseExternalLink = "(opens in a new window)"
  val calcBaseChange = "change"
  val undefinedMessage = "Undefined message"
  val propertiesHomeText = "Calculate your Capital Gains Tax"

  //Common messages
  val maxNumericExceededStart = "Enter an amount that's £"
  val maxNumericExceededEnd = "or less"

  //########################################################################################
  //These nested objects have been created in anticipation of the Tech-Debt to refactor
  // the message lookup and add the non-resident messages.  Any new added pages should be added
  // to the right place in this object.

  object Resident {

    object Properties {

      object PropertyWorthWhenSold {
        val title = "What was the property worth when you sold it?"
        val paragraphText = "You can use a valuation from a surveyor or a property website."

      }
      
      object ownerBeforeAprilNineteenEightyTwo {
        val title = "Did you become the property owner before 1 April 1982?"
        val errorSelectAnOption = "Tell us if you became the property owner before 1 April 1982"
      }

      object propertiesWorthWhenGaveAway {
        val title = "What was the property worth when you gave it away?"
      }

      object worthOn {
        val title = "What was the property worth on 31 March 1982?"
      }
    }
  }


  //########################################################################################

  object errorMessages {
    val mandatoryAmount = "Enter an amount"
    val minimumAmount = "Enter an amount that's £0 or more"
    val maximumAmount = "Enter an amount that's £1,000,000,000 or less"
    def maximumLimit(limit: String): String = s"Enter an amount that's £$limit or less"
    val invalidAmount = "Enter an amount in the correct format e.g. 10000.00"
    val invalidAmountNoDecimal = "Enter an amount in the correct format e.g. 10000"
    val numericPlayErrorOverride = "Enter a number without commas, for example 10000.00"
  }

  object introductionView {
    val title = "Work out how much Capital Gains Tax you owe"
    val subheading = "Do you need to use this calculator?"
    val paragraph = "You probably don't need to pay Capital Gains Tax if the property you've sold is your own home. You'll be entitled to a tax relief called Private Residence Relief."
    val entitledLinkText = "Find out if you're entitled to Private Residence Relief (opens in a new window)."
    val continuationInstructions = "Continue to use this calculator if you've never lived at the property, or you're entitled to only some or no Private Residence Relief."
  }

  //Disposal Date messages
  object disposalDate {
    val title = "When did you sell or give away the property?"
    val question = "When did you sell or give away the property?"
    val helpText = "For example, 4 9 2016"
    val day = "Day"
    val month = "Month"
    val year = "Year"
    val invalidDayError = "Enter a day"
    val invalidMonthError = "Enter a month"
    val invalidYearError = "Enter a year"
    val realDateError = "Enter a real date"
    val invalidYearRange = "Enter a date in the correct format e.g. 9 12 2015"
  }

  //Outside Tax Years messages
  object outsideTaxYears {
    val title = "The date you've entered isn't supported by this calculator"
    val tooEarly = "You can use this calculator if you've sold a property since 5 April 2015."
    val changeDate = "Change your date"
    def content(year: String): String = s"You can continue to use it, but we'll use the tax rates from the $year tax year."
  }

  //############ Sell For Less messages #################//
  object sellForLess {
    val title = "Did you sell the property for less than it was worth to help the buyer?"
  }

  //############ Worth When Inherited messages #################//
  object worthWhenInherited {
    val title = "What was the property worth when you inherited it?"
    val additionalContent = "You can use a valuation from a surveyor or a property website."
  }

  //Disposal Value messages
  object disposalValue {
    val title = "How much did you sell the property for?"
    val question = "How much did you sell the property for?"
    val bulletListTitlePartOne = "Put the"
    val bulletListTitlePartTwo = "market value"
    val bulletListTitlePartThree = "of the property instead if you:"
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

  //How Became Owner messages
  object howBecameOwner {
    val title = "How did you become the property owner?"
    val errorMandatory = "Tell us how you became the property owner"
    val bought = "Bought it"
    val gifted = "Got it as a gift"
    val inherited = "Inherited it"
  }

  //Acquisition Value messages
  object acquisitionValue {
    val title = "How much did you pay for the property?"
    val pageHeading = "How much did you pay for the property?"
    val bulletListTitleOne = "Put the "
    val bulletListTitleTwo = "market value (opens in a new window) "
    val bulletListTitleThree = "of the property instead if you:"
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
    val label = "How much have you spent on improvements since you became the property owner?"
    val hint = "Improvements are permanent changes that raise the value of a property, like adding extensions or garages. Normal maintenance costs don't count."
    val improvementsHelpButton = "Show me an example"
    val improvementsAdditionalContentOne = "Replacing a basic kitchen or bathroom with a luxury version is normally considered an improvement."
    val improvementsAdditionalContentTwo = "But replacing them with something of a similar standard is normally not an improvement."
  }

  //Summary messages
  object summaryPage {
    val title = "Summary"
    val pageHeading = "Tax owed"
    val calcDetailsHeading = "Calculation details"
    def calcDetailsHeadingDate(input: String): String = s"Calculation details for $input tax year"
    val aeaHelp = "You can use this to reduce your tax if you sell something else that's covered by Capital Gains Tax in the same tax year."
    val yourAnswersHeading = "Your answers"
    val totalLoss = "Loss"
    val totalGain = "Total gain"
    val deductions = "Deductions"
    val chargeableLoss = "Carried forward loss"
    val chargeableGain = "Taxable gain"
    val taxRate = "Tax rate"
    def noticeWarning(input: String): String = s"These figures are based on the tax rates from the $input tax year"
    val warning = "Warning"
    val whatToDoNextTitle = "What to do next"
    val whatToDoNextText = "You can tell us about this loss so that you might need to pay less tax in the future."
    val whatNextYouCan = "You can "
    val whatNextLink = "tell us about this loss "
    val whatNextText = "so that you might need to pay less tax in the future."
    val whatToDoNextTextTwo = "You need to tell HMRC about the property"
    val whatToDoNextTextTwoShares = "You need to tell HMRC about the shares"
    val whatToDoNextNoLossText = "Find out whether you need to"
    val whatToDoNextNoLossLinkProperties = "tell HMRC about the property"
    val whatToDoNextNoLossLinkShares = "tell HMRC about the shares"
    val whatToDoNextLossRemaining = "so that you might need to pay less tax in the future"
    def aeaRemaining(taxYear: String): String = s"Capital Gains Tax allowance left for $taxYear"
    val saveAsPdf = "Save as PDF"
    def remainingAllowableLoss(taxYear: String): String = s"Remaining loss from $taxYear tax year"
    def remainingBroughtForwardLoss(taxYear: String): String = s"Remaining loss from tax years before $taxYear"
    val remainingLossHelp = "You can"
    val remainingLossLink = "use this loss"
    val remainingAllowableLossHelp = "to reduce your Capital Gains Tax if you sell something in the same tax year"
    val remainingBroughtForwardLossHelp = "to reduce your Capital Gains Tax in the future"
    val lettingReliefsUsed = "Letting Relief used"
    def deductionsDetailsAllowableLosses(taxYear: String): String = s"Loss from $taxYear tax year"
    val deductionsDetailsCapitalGainsTax = "Capital Gains Tax allowance used"
    def deductionsDetailsLossBeforeYear(taxYear: String): String = s"Loss from tax years before $taxYear"
    def deductionsDetailsAllowableLossesUsed(taxYear: String): String = s"Loss used from $taxYear tax year"
    def deductionsDetailsLossBeforeYearUsed(taxYear: String): String = s"Loss used from tax years before $taxYear"
  }

  //Private Residence Relief Value messages
  object privateResidenceReliefValue {
    val title = "How much Private Residence Relief are you entitled to?"
    val question = title
    val link = "Find out how much you're entitled to"
    def help(value: String): String = s"We've calculated that you've made a gain of £$value on your property. " +
      s"You'll need this figure to calculate your Private Residence Relief."
    def error(value: String): String = s"Enter an amount that is less than your gain of £$value"
  }

  //Reliefs messages
  object reliefs {
    val title = "Do you want to claim any other tax reliefs?"
    val questionSummary = "Do you want to claim any other tax reliefs?"
    val question = s"Do you want to claim any other tax reliefs?"
    val help = "For example, lettings relief"
    val helpOne = "Capital Gains Tax reliefs can lower the amount of tax you owe. For example, you might be able to claim"
    val helpLinkOne = "Private Residence Relief"
    val errorSelect = s"Tell us if you want to claim any other tax reliefs"
    def errorSelectNoPrr(value: String) = s"Tell us if you want to claim any Capital Gains Tax reliefs on your total gain of £$value"
    val titleNoPrr = "Do you want to claim any Capital Gains Tax reliefs on your total gain of £10,000?"
    val questionSummaryNoPrr = "Do you want to claim any Capital Gains Tax reliefs on your total gain of £50,000?"
    def questionNoPrr(input: String = "100") = s"Do you want to claim any Capital Gains Tax reliefs on your total gain of £$input?"
    val helpButton = "What are Capital Gains Tax reliefs?"
    val helpNoPrr = "For example, Private Residence Relief"
  }

  //Reliefs Value messages
  object reliefsValue {
    def title(input: String): String = s"How much tax relief are you claiming on your total gain of £$input?"
    def question(input: String): String = s"How much tax relief are you claiming on your total gain of £$input?"
    val prrLink = "Private Residence Relief"
    val lettingsReliefLink = "Lettings Relief"
  }

  //Lettings Relief Value messages
  object lettingsReliefValue {
    val title = s"How much Letting Relief are you entitled to?"
    val question = s"How much Letting Relief are you entitled to?"
    def additionalContent(input: String): String = s"We've calculated that you've made a gain of £$input on your property. " +
      s"You'll need this figure to calculate your Letting Relief."
  }

  //No Prr Reliefs Value messages
  object reliefsValueNoPrr {
    val title = "How much Capital Gains Tax relief are you claiming?"
    val question = "How much Capital Gains Tax relief are you claiming?"
    val prrLink = "Private Residence Relief"
    val lettingsReliefLink = "Lettings Relief"
  }

  //Lettings Relief messages
  object lettingsRelief {
    val title = "Are you entitled to Letting Relief?"
    val help = "You may be able entitled to Letting Relief if you've rented out the property. Find out more about Letting Relief (opens in a new window)"
    val helpOne = "Letting Relief (opens in a new window)"
    val helpLink = "https://www.gov.uk/government/publications/private-residence-relief-hs283-self-assessment-helpsheet/hs283-private-residence-relief-2016#letting-relief"
    val errorSelect = "Tell us if you want to claim Letting Relief"
  }

  //Other Properties messages
  object otherProperties {
    def title(input: String): String = s"In the $input tax year, did you sell or give away anything else that's covered by Capital Gains Tax?"
    def pageHeading(input: String): String = s"In the $input tax year, did you sell or give away anything else that's covered by Capital Gains Tax?"
    val help = "This includes things like:"
    val helpOne = "shares"
    val helpTwo = "antiques"
    val helpThree = "other UK residential properties"
    def errorSelect(input: String): String = s"Tell us if you sold or gave away anything else that's covered by Capital Gains Tax in the $input tax year"
  }

  //Allowable Losses Value messages
  object allowableLossesValue {
    def title(input: String): String = s"What's the total value of your Capital Gains Tax losses from the $input tax year?"
    def question(input: String): String = s"What's the total value of your Capital Gains Tax losses from the $input tax year?"
  }

  //Losses Brought Forward messages
  object lossesBroughtForward {
    def title(input: String): String = s"Are you claiming any Capital Gains Tax losses from tax years before $input?"
    def question(input: String): String = s"Are you claiming any Capital Gains Tax losses from tax years before $input?"
    val helpInfoTitle = "What are Capital Gains Tax losses?"
    val helpInfoSubtitle = "They're losses you've made that:"
    val helpInfoPoint1 = "are covered by Capital Gains Tax"
    val helpInfoPoint2 = "you've declared within 4 years of making the loss"
    val helpInfoPoint3 = "you haven't already used to reduce the amount of Capital Gains Tax you had to pay"
    def errorSelect(input: String): String = s"Tell us if you're claiming any Capital Gains Tax losses from tax years before $input"
  }

  //Allowable Losses messages
  object allowableLosses {
    def title(input: String): String = s"Are you claiming any Capital Gains Tax losses from the $input tax year?"
    val helpInfoTitle = "What are Capital Gains Tax losses?"
    val helpInfoSubtitle = "They're losses you've made that:"
    val helpInfoPoint1 = "are covered by Capital Gains Tax"
    val helpInfoPoint2 = "you've declared within 4 years of making the loss"
    val helpInfoPoint3 = "you haven't already used to reduce the amount of Capital Gains Tax you had to pay"
    def errorSelect(input: String): String = s"Tell us if you're claiming any Capital Gains Tax losses from the $input tax year"
  }

  //Losses Brought Forward Value messages
  object lossesBroughtForwardValue {
    def title(input: String): String = s"What's the total value of your Capital Gains Tax losses from tax years before $input?"
    def question(input: String): String = s"What's the total value of your Capital Gains Tax losses from tax years before $input?"
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
    def title(year: String): String = s"What was your taxable gain in the $year tax year?"
    def question(year: String): String = s"What was your taxable gain in the $year tax year?"
    val helpLinkOne = "How to work out your taxable gain"
  }

  //Current Income messages
  object currentIncome {
    def title(input: String): String = s"In the $input tax year, what was your income?"
    def question(input: String): String = s"In the $input tax year, what was your income?"
    val currentYearTitle = "How much do you expect your income to be in this tax year?"
    val currentYearQuestion = "How much do you expect your income to be in this tax year?"
    val helpText = "Include your salary before tax, and anything else you pay income tax on, but not the money you made from selling the property."
    val helpTextShares = "Include your salary before tax, and anything else you pay income tax on, but not the money you made from selling the shares."
    val linkText = "Income tax"
  }

  //Personal Allowance messages
  object personalAllowance {
    def question(input: String): String = s"In the $input tax year, what was your Personal Allowance?"
    val inYearQuestion = "How much is your Personal Allowance?"
    def help(input: String): String = s"This is the amount of your income you don't pay tax on. It was £$input unless you were claiming other allowances."
    def inYearHelp(input: String): String = s"This is the amount of your income you don't pay tax on. It's £$input unless you're claiming other allowances."
    val helpLinkOne = "Personal Allowance"
  }

  //############ Private Residence Relief messages #################//
  object privateResidenceRelief {
    val title = "Are you entitled to Private Residence Relief?"
    val helpTextOne = "You'll be entitled to Private Residence Relief if you've lived in the property as your main home " +
      "at some point while you owned it. Find out more about"
    val helpTextLink = "Private Residence Relief"
    val errorSelect = "Tell us if you want to claim Private Residence Relief"
  }

  //############ Property Lived In messages #################//
  object propertyLivedIn {
    val title = "Have you ever lived in the property since you became the owner?"
    val errorNoSelect = "Tell us if you have ever lived in the property since you became the owner"
  }

  //############ Shares messages ##############//
  object sharesDisposalDate {
    val title = "When did you sell or give away the shares?"
  }

  object sharesDisposalValue {
    val title = "How much did you sell the shares for?"
    val helpText = "Put the market value of the shares instead if you sold them for less than they were worth to help the buyer"
  }

  object sharesAcquisitionCosts {
    val title = "How much did you pay in costs when you got the shares?"
    val helpText = "Costs include stockbroker fees and Stamp Duty tax"
  }

  object sharesDisposalCosts {
    val title = "How much did you pay in costs when you sold or gave away the shares?"
    val helpText = "For example, stockbroker fees"
  }

  object sharesAcquisitionValue {
    val title = "How much did you pay for the shares?"
    val bulletListTitle = "Put the market value of the shares instead if you:"
    val bulletListOne = "inherited them"
    val bulletListTwo = "owned them before 1 April 1982"
  }

  object sharesOtherDisposals {
    val helpOne = "UK residential properties"
    val helpThree = "other shares"
  }

  object propertiesSellOrGiveAway {
    val title = "Did you sell the property or give it away?"
    val errorMandatory = "Tell us if you sold the property or gave it away"
    val sold = "Sold it"
    val gift = "Gave it away"
  }

}

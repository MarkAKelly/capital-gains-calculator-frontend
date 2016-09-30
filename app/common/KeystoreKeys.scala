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

object KeystoreKeys {

  val customerType = "nr:customerType"
  val disabledTrustee = "nr:disabledTrustee"
  val currentIncome = "nr:currentIncome"
  val personalAllowance = "nr:personalAllowance"
  val otherProperties = "nr:otherProperties"
  val annualExemptAmount = "nr:annualExemptAmount"
  val acquisitionDate = "nr:acquisitionDate"
  val acquisitionValue = "nr:acquisitionValue"
  val rebasedValue = "nr:rebasedValue"
  val rebasedCosts = "nr:rebasedCosts"
  val improvements = "nr:improvements"
  val disposalDate = "nr:disposalDate"
  val disposalValue = "nr:disposalValue"
  val acquisitionCosts = "nr:acquisitionCosts"
  val disposalCosts = "nr:disposalCosts"
  val entrepreneursRelief = "nr:entrepreneursRelief"
  val allowableLosses = "nr:allowableLosses"
  val calculationElection = "nr:calculationElection"
  val otherReliefsFlat = "nr:otherReliefsFlat"
  val otherReliefsTA = "nr:otherReliefsTA"
  val otherReliefsRebased = "nr:otherReliefsRebased"
  val privateResidenceRelief = "nr:privateResidenceRelief"

  object ResidentPropertyKeys {
    val disposalDate = "res:property:disposalDate"
    val disposalValue = "res:property:disposalValue"
    val worthWhenSoldForLess = "res:property:worthWhenSoldForLess"
    val disposalCosts = "res:property:disposalCosts"
    val ownerBeforeLegislationStart = "res:property:ownerBeforeLegislationStart"
    val howBecameOwner = "res:property:howBecameOwner"
    val acquisitionValue = "res:property:acquisitionValue"
    val acquisitionCosts = "res:property:acquisitionCosts"
    val improvements = "res:property:improvements"
    val allowableLosses = "res:property:allowableLosses"
    val prrValue = "res:property:prrValue"
    val reliefs = "res:property:reliefs"
    val reliefsValue = "res:property:reliefsValue"
    val lettingsReliefValue = "res:property:lettingsReliefValue"
    val lettingsRelief = "res:property:lettingsRelief"
    val lossesBroughtForward = "res:property:lossesBroughtForward"
    val lossesBroughtForwardValue = "res:property:lossesBroughtForwardValue"
    val allowableLossesValue = "res:property:allowableLossesValue"
    val otherProperties = "res:property:otherProperties"
    val annualExemptAmount = "res:property:annualExemptAmount"
    val currentIncome = "res:property:currentIncome"
    val previousTaxableGains = "res:property:previousTaxableGains"
    val incomeForPreviousYear = "res:property:incomeForPreviousYear"
    val personalAllowance = "res:property:personalAllowance"
    val privateResidenceRelief = "res:property:privateResidenceRelief"
    val propertyLivedIn = "res:property:propertyLivedIn"
    val boughtForLessThanWorth = "res:property:boughtForLessThanWorth"
    val sellForLess = "res:property:sellForLess"
    val sellOrGiveAway = "res:property:sellOrGiveAway"
    val worthWhenBoughtForLess = "res:property:worthWhenBoughtForLess"
    val worthWhenInherited = "res:property:worthWhenInherited"
    val worthWhenGaveAway = "res:property:worthWhenGaveAway"
    val worthWhenGifted = "res:property:worthWhenGifted"
    val valueBeforeLegislationStart = "res:property:valueBeforeLegislationStart"
    val propertyRecipient = "res:property:propertyRecipient"
  }

  object ResidentShareKeys {
    val disposalDate = "res:share:disposalDate"
    val sellForLess = "res:share:sellForLess"
    val worthWhenSoldForLess = "res:share:worthWhenSoldForLess"
    val disposalValue = "res:share:disposalValue"
    val disposalCosts = "res:share:disposalCosts"
    val ownedBeforeEightyTwo = "res:share:ownedBeforeEightyTwo"
    val worthOn = "res:share:worthOn"
    val acquisitionValue = "res:share:acquisitionValue"
    val acquisitionCosts = "res:share:acquisitionCosts"
    val otherProperties = "res:share:otherProperties"
    val allowableLosses = "res:share:allowableLosses"
    val allowableLossesValue = "res:share:allowableLossesValue"
    val lossesBroughtForward = "res:share:lossesBroughtForward"
    val lossesBroughtForwardValue = "res:share:lossesBroughtForwardValue"
    val annualExemptAmount = "res:share:annualExemptAmount"
    val currentIncome = "res:share:currentIncome"
    val previousTaxableGains = "res:share:previousTaxableGains"
    val personalAllowance = "res:share:personalAllowance"
    val worthWhenInherited = "res:share:worthWhenInherited"
    val inheritedShares = "res:share:inheritedShares"
  }
}

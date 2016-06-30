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

object KeystoreKeys extends KeystoreKeys

trait KeystoreKeys {

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

  object ResidentKeys {
    val disposalDate = "res:disposalDate"
    val disposalValue = "res:disposalValue"
    val disposalCosts = "res:disposalCosts"
    val acquisitionValue = "res:acquisitionValue"
    val acquisitionCosts = "res:acquisitionCosts"
    val improvements = "res:improvements"
    val reliefs = "res:reliefs"
    val reliefsValue = "res:reliefsValue"
  }

}

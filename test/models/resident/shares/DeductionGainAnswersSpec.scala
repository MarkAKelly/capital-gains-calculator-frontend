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

package models.resident.shares

import models.resident._
import uk.gov.hmrc.play.test.UnitSpec

class ChargeableGainAnswersSpec extends UnitSpec {

  "Creating a model for a property that has not been lived in and no other properties have been disposed of" should {
    val model = DeductionGainAnswersModel(
      otherPropertiesModel = Some(OtherPropertiesModel(false)),
      allowableLossesModel = None,
      allowableLossesValueModel = None,
      broughtForwardModel = None,
      broughtForwardValueModel = None,
      annualExemptAmountModel = None
    )

    "return a result of false for displayAllowableLossesValue" in {
      val result = model.displayAllowableLossesValue

      result shouldBe false
    }

    "return a result of false for displayAnnualExemptAmount" in {
      val result = model.displayAnnualExemptAmount

      result shouldBe false
    }

    "return a result of false for displayPreviousTaxableGains" in {
      val result = model.displayPreviousTaxableGains

      result shouldBe false
    }
  }

  "Creating a model for shares that has no allowable losses and have had other disposals with no taxable gains" should {
    val model = DeductionGainAnswersModel(
      otherPropertiesModel = Some(OtherPropertiesModel(true)),
      allowableLossesModel = Some(AllowableLossesModel(false)),
      allowableLossesValueModel = None,
      broughtForwardModel = None,
      broughtForwardValueModel = None,
      annualExemptAmountModel = None
    )

    "return a result of false for displayAllowableLossesValue" in {
      val result = model.displayAllowableLossesValue

      result shouldBe false
    }

    "return a result of true for displayAnnualExemptAmount" in {
      val result = model.displayAnnualExemptAmount

      result shouldBe true
    }

    "return a result of false for displayPreviousTaxableGains" in {
      val result = model.displayPreviousTaxableGains

      result shouldBe false
    }
  }

  "Creating a model for shares with non-zero allowable losses and non-zero AEA" should {
    val model = DeductionGainAnswersModel(
      otherPropertiesModel = Some(OtherPropertiesModel(true)),
      allowableLossesModel = Some(AllowableLossesModel(true)),
      allowableLossesValueModel = Some(AllowableLossesValueModel(1000)),
      broughtForwardModel = None,
      broughtForwardValueModel = None,
      annualExemptAmountModel = Some(AnnualExemptAmountModel(50.0))
    )

    "return a result of true for displayAllowableLossesValue" in {
      val result = model.displayAllowableLossesValue

      result shouldBe true
    }

    "return a result of false for displayAnnualExemptAmount" in {
      val result = model.displayAnnualExemptAmount

      result shouldBe false
    }

    "return a result of false for displayPreviousTaxableGains" in {
      val result = model.displayPreviousTaxableGains

      result shouldBe false
    }
  }

  "Creating a model for shares with zero allowable losses with zero remaining AEA" should {
    val model = DeductionGainAnswersModel(
      otherPropertiesModel = Some(OtherPropertiesModel(true)),
      allowableLossesModel = Some(AllowableLossesModel(true)),
      allowableLossesValueModel = Some(AllowableLossesValueModel(0)),
      broughtForwardModel = None,
      broughtForwardValueModel = None,
      annualExemptAmountModel = Some(AnnualExemptAmountModel(0))
    )

    "return a result of true for displayAllowableLossesValue" in {
      val result = model.displayAllowableLossesValue

      result shouldBe true
    }

    "return a result of true for displayAnnualExemptAmount" in {
      val result = model.displayAnnualExemptAmount

      result shouldBe true
    }

    "return a result of true for displayPreviousTaxableGains" in {
      val result = model.displayPreviousTaxableGains

      result shouldBe true
    }
  }
}
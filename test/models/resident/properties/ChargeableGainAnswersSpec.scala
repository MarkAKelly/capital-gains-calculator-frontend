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

package models.resident.properties

import models.resident.{AllowableLossesModel, AllowableLossesValueModel, OtherPropertiesModel, PrivateResidenceReliefModel}
import uk.gov.hmrc.play.test.UnitSpec

class ChargeableGainAnswersSpec extends UnitSpec {

  "Creating a model for a property that has not been lived in and no other properties have been disposed of" should {
    val model = ChargeableGainAnswers(
      otherPropertiesModel = Some(OtherPropertiesModel(false)),
      allowableLossesModel = None,
      allowableLossesValueModel = None,
      broughtForwardModel = None,
      broughtForwardValueModel = None,
      annualExemptAmountModel = None,
      propertyLivedInModel = Some(PropertyLivedInModel(false)),
      privateResidenceReliefModel = None,
      privateResidenceReliefValueModel = None,
      lettingsReliefModel = None,
      lettingsReliefValueModel = None
    )

    "return a result of false for displayPRRValueAndLettingsRelief" in {
      val result = model.displayPRRValueAndLettingsRelief

      result shouldBe false
    }

    "return a result of false for displayLettingsReliefValue" in {
      val result = model.displayLettingsReliefValue

      result shouldBe false
    }

    "return a result of false for displayAllowableLossesValue" in {
      val result = model.displayAllowableLossesValue

      result shouldBe false
    }

    "return a result of false for displayAnnualExemptAmount" in {
      val result = model.displayAnnualExemptAmount

      result shouldBe false
    }
  }

  "Creating a model for a property that has been lived in but not claiming PRR and other properties have been disposed of with no allowable losses" should {
    val model = ChargeableGainAnswers(
      otherPropertiesModel = Some(OtherPropertiesModel(true)),
      allowableLossesModel = Some(AllowableLossesModel(false)),
      allowableLossesValueModel = None,
      broughtForwardModel = None,
      broughtForwardValueModel = None,
      annualExemptAmountModel = None,
      propertyLivedInModel = Some(PropertyLivedInModel(true)),
      privateResidenceReliefModel = Some(PrivateResidenceReliefModel(false)),
      privateResidenceReliefValueModel = None,
      lettingsReliefModel = None,
      lettingsReliefValueModel = None
    )

    "return a result of false for displayPRRValueAndLettingsRelief" in {
      val result = model.displayPRRValueAndLettingsRelief

      result shouldBe false
    }

    "return a result of false for displayLettingsReliefValue" in {
      val result = model.displayLettingsReliefValue

      result shouldBe false
    }

    "return a result of false for displayAllowableLossesValue" in {
      val result = model.displayAllowableLossesValue

      result shouldBe false
    }

    "return a result of true for displayAnnualExemptAmount" in {
      val result = model.displayAnnualExemptAmount

      result shouldBe true
    }
  }

  "Creating a model for a property where PRR is claimed but not lettings relief and allowable losses have been claimed with a non-zero value" should {
    val model = ChargeableGainAnswers(
      otherPropertiesModel = Some(OtherPropertiesModel(true)),
      allowableLossesModel = Some(AllowableLossesModel(true)),
      allowableLossesValueModel = Some(AllowableLossesValueModel(1000)),
      broughtForwardModel = None,
      broughtForwardValueModel = None,
      annualExemptAmountModel = None,
      propertyLivedInModel = Some(PropertyLivedInModel(true)),
      privateResidenceReliefModel = Some(PrivateResidenceReliefModel(true)),
      privateResidenceReliefValueModel = None,
      lettingsReliefModel = Some(LettingsReliefModel(false)),
      lettingsReliefValueModel = None
    )

    "return a result of true for displayPRRValueAndLettingsRelief" in {
      val result = model.displayPRRValueAndLettingsRelief

      result shouldBe true
    }

    "return a result of false for displayLettingsReliefValue" in {
      val result = model.displayLettingsReliefValue

      result shouldBe false
    }

    "return a result of true for displayAllowableLossesValue" in {
      val result = model.displayAllowableLossesValue

      result shouldBe true
    }

    "return a result of false for displayAnnualExemptAmount" in {
      val result = model.displayAnnualExemptAmount

      result shouldBe false
    }
  }

  "Creating a model for a property where PRR is claimed with lettings relief and allowable losses have been claimed with a zero value" should {
    val model = ChargeableGainAnswers(
      otherPropertiesModel = Some(OtherPropertiesModel(true)),
      allowableLossesModel = Some(AllowableLossesModel(true)),
      allowableLossesValueModel = Some(AllowableLossesValueModel(0)),
      broughtForwardModel = None,
      broughtForwardValueModel = None,
      annualExemptAmountModel = None,
      propertyLivedInModel = Some(PropertyLivedInModel(true)),
      privateResidenceReliefModel = Some(PrivateResidenceReliefModel(true)),
      privateResidenceReliefValueModel = None,
      lettingsReliefModel = Some(LettingsReliefModel(true)),
      lettingsReliefValueModel = None
    )

    "return a result of true for displayPRRValueAndLettingsRelief" in {
      val result = model.displayPRRValueAndLettingsRelief

      result shouldBe true
    }

    "return a result of true for displayLettingsReliefValue" in {
      val result = model.displayLettingsReliefValue

      result shouldBe true
    }

    "return a result of true for displayAllowableLossesValue" in {
      val result = model.displayAllowableLossesValue

      result shouldBe true
    }

    "return a result of true for displayAnnualExemptAmount" in {
      val result = model.displayAnnualExemptAmount

      result shouldBe true
    }
  }
}

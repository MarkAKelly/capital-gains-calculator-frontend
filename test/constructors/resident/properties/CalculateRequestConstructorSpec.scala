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

package constructors.resident.properties

import common.Dates
import models.resident._
import models.resident.properties._
import uk.gov.hmrc.play.test.UnitSpec

class CalculateRequestConstructorSpec extends UnitSpec {

  "totalGainRequestString" should {

    "return a valid url variable string" in {
      val answers = YourAnswersSummaryModel(Dates.constructDate(10, 2, 2016),
        BigDecimal(1000),
        BigDecimal(0),
        BigDecimal(500),
        BigDecimal(100),
        BigDecimal(10))
      val result = CalculateRequestConstructor.totalGainRequestString(answers)
      result shouldBe s"?disposalValue=1000" +
        s"&disposalCosts=0" +
        s"&acquisitionValue=500" +
        s"&acquisitionCosts=100" +
        s"&improvements=10" +
        s"&disposalDate=2016-02-10"
    }
  }

  "chargeableGainRequestString" when {

    "supplied with no optional values" should {

      "return a valid url variable string" in {
        val answers = ChargeableGainAnswers(
          Some(ReliefsModel(false)),
          None,
          Some(OtherPropertiesModel(false)),
          None,
          None,
          Some(LossesBroughtForwardModel(false)),
          None,
          None)
        val result = CalculateRequestConstructor.chargeableGainRequestString(answers, BigDecimal(11100))
        result shouldBe "&annualExemptAmount=11100"
      }
    }

    "supplied with all optional values except allowable losses" should {

      "return a valid url variable string" in {
        val answers = ChargeableGainAnswers(
          Some(ReliefsModel(true)),
          Some(ReliefsValueModel(BigDecimal(1000))),
          Some(OtherPropertiesModel(true)),
          Some(AllowableLossesModel(false)),
          None,
          Some(LossesBroughtForwardModel(true)),
          Some(LossesBroughtForwardValueModel(BigDecimal(2000))),
          Some(AnnualExemptAmountModel(BigDecimal(3000))))
        val result = CalculateRequestConstructor.chargeableGainRequestString(answers, BigDecimal(11100))
        result shouldBe "&reliefs=1000&broughtForwardLosses=2000&annualExemptAmount=3000"
      }
    }

    "supplied with all optional values including allowable losses" should {

      "return a valid url variable string" in {
        val answers = ChargeableGainAnswers(
          Some(ReliefsModel(true)),
          Some(ReliefsValueModel(BigDecimal(1000))),
          Some(OtherPropertiesModel(true)),
          Some(AllowableLossesModel(true)),
          Some(AllowableLossesValueModel(BigDecimal(1000))),
          Some(LossesBroughtForwardModel(true)),
          Some(LossesBroughtForwardValueModel(BigDecimal(2000))),
          Some(AnnualExemptAmountModel(BigDecimal(3000))))
        val result = CalculateRequestConstructor.chargeableGainRequestString(answers, BigDecimal(11100))
        result shouldBe "&reliefs=1000&allowableLosses=1000&broughtForwardLosses=2000&annualExemptAmount=11100"
      }
    }
  }

  "calling .isUsingAnnualExemptAmount" when {

    "disposed other properties and claimed non-zero allowable losses" should {

      "return a true" in {
        val result = CalculateRequestConstructor.isUsingAnnualExemptAmount(Some(OtherPropertiesModel(true)),
          Some(AllowableLossesModel(true)),
          Some(AllowableLossesValueModel(BigDecimal(1000))))
        result shouldBe false
      }
    }

    "disposed other properties and claimed zero allowable losses" should {

      "return a false" in {
        val result = CalculateRequestConstructor.isUsingAnnualExemptAmount(Some(OtherPropertiesModel(true)),
          Some(AllowableLossesModel(true)),
          Some(AllowableLossesValueModel(BigDecimal(0))))
        result shouldBe true
      }
    }

    "disposed other properties and didn't claim allowable losses" should {

      "return a true" in {
        val result = CalculateRequestConstructor.isUsingAnnualExemptAmount(Some(OtherPropertiesModel(true)),
          Some(AllowableLossesModel(false)),
          Some(AllowableLossesValueModel(BigDecimal(0))))
        result shouldBe true
      }
    }

    "disposed no other properties or allowable losses" should {

      "return a false" in {
        val result = CalculateRequestConstructor.isUsingAnnualExemptAmount(Some(OtherPropertiesModel(false)),
          Some(AllowableLossesModel(false)),
          Some(AllowableLossesValueModel(BigDecimal(100))))
        result shouldBe false
      }
    }

    "disposed no other properties and but claimed allowable losses" should {

      "return a false" in {
        val result = CalculateRequestConstructor.isUsingAnnualExemptAmount(Some(OtherPropertiesModel(false)),
          Some(AllowableLossesModel(true)),
          Some(AllowableLossesValueModel(BigDecimal(100))))
        result shouldBe false
      }
    }
  }
}

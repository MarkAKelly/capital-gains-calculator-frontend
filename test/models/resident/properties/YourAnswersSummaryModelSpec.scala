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

import java.time.LocalDate

import uk.gov.hmrc.play.test.UnitSpec

class YourAnswersSummaryModelSpec extends UnitSpec {

  "Creating a model for a property given away and owned before legislation start date" should {
    val model = YourAnswersSummaryModel(
      disposalDate = LocalDate.parse("2015-05-05"),
      disposalValue = None,
      worthWhenSoldForLess = None,
      whoDidYouGiveItTo = None,
      worthWhenGaveAway = None,
      disposalCosts = 1000,
      acquisitionValue = None,
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      acquisitionCosts = 1000,
      improvements = 0,
      givenAway = true,
      sellForLess = None,
      ownerBeforeLegislationStart = true,
      valueBeforeLegislationStart = None,
      howBecameOwner = None,
      boughtForLessThanWorth = None
    )
    "return a result of false for displayWorthWhenSold" in {
      val result = model.displayWorthWhenSold

      result shouldBe false
    }

    "return a result of false for displayWorthWhenSoldForLess" in {
      val result = model.displayWorthWhenSoldForLess

      result shouldBe false
    }

    "return a result of false for displayBoughtForLessThanWorth" in {
      val result = model.displayBoughtForLessThanWorth

      result shouldBe false
    }

    "return a result of false for displayWorthWhenBought" in {
      val result = model.displayWorthWhenBought

      result shouldBe false
    }

    "return a result of false for displayWorthWhenBoughtForLess" in {
      val result = model.displayWorthWhenBoughtForLess

      result shouldBe false
    }

    "return a result of false for displayWorthWhenGifted" in {
      val result = model.displayWorthWhenGifted

      result shouldBe false
    }

    "return a result of false for displayWorthWhenInherited" in {
      val result = model.displayWorthWhenInherited

      result shouldBe false
    }
  }

  "Creating a model for a property sold for what it is worth and bought for what it was worth after legislation start date" should {
    val model = YourAnswersSummaryModel(
      disposalDate = LocalDate.parse("2015-05-05"),
      disposalValue = None,
      worthWhenSoldForLess = None,
      whoDidYouGiveItTo = None,
      worthWhenGaveAway = None,
      disposalCosts = 1000,
      acquisitionValue = None,
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      acquisitionCosts = 1000,
      improvements = 0,
      givenAway = false,
      sellForLess = Some(false),
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      howBecameOwner = Some("Bought"),
      boughtForLessThanWorth = Some(false)
    )

    "return a result of true for displayWorthWhenSold" in {
      val result = model.displayWorthWhenSold

      result shouldBe true
    }

    "return a result of false for displayWorthWhenSoldForLess" in {
      val result = model.displayWorthWhenSoldForLess

      result shouldBe false
    }

    "return a result of true for displayBoughtForLessThanWorth" in {
      val result = model.displayBoughtForLessThanWorth

      result shouldBe true
    }

    "return a result of true for displayWorthWhenBought" in {
      val result = model.displayWorthWhenBought

      result shouldBe true
    }

    "return a result of false for displayWorthWhenBoughtForLess" in {
      val result = model.displayWorthWhenBoughtForLess

      result shouldBe false
    }

    "return a result of false for displayWorthWhenGifted" in {
      val result = model.displayWorthWhenGifted

      result shouldBe false
    }

    "return a result of false for displayWorthWhenInherited" in {
      val result = model.displayWorthWhenInherited

      result shouldBe false
    }
  }

  "Creating a model for a property sold for less than it is worth and bought for less than what it was worth after legislation start date" should {
    val model = YourAnswersSummaryModel(
      disposalDate = LocalDate.parse("2015-05-05"),
      disposalValue = None,
      worthWhenSoldForLess = None,
      whoDidYouGiveItTo = None,
      worthWhenGaveAway = None,
      disposalCosts = 1000,
      acquisitionValue = None,
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      acquisitionCosts = 1000,
      improvements = 0,
      givenAway = false,
      sellForLess = Some(true),
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      howBecameOwner = Some("Bought"),
      boughtForLessThanWorth = Some(true)
    )

    "return a result of false for displayWorthWhenSold" in {
      val result = model.displayWorthWhenSold

      result shouldBe false
    }

    "return a result of true for displayWorthWhenSoldForLess" in {
      val result = model.displayWorthWhenSoldForLess

      result shouldBe true
    }

    "return a result of true for displayBoughtForLessThanWorth" in {
      val result = model.displayBoughtForLessThanWorth

      result shouldBe true
    }

    "return a result of false for displayWorthWhenBought" in {
      val result = model.displayWorthWhenBought

      result shouldBe false
    }

    "return a result of true for displayWorthWhenBoughtForLess" in {
      val result = model.displayWorthWhenBoughtForLess

      result shouldBe true
    }

    "return a result of false for displayWorthWhenGifted" in {
      val result = model.displayWorthWhenGifted

      result shouldBe false
    }

    "return a result of false for displayWorthWhenInherited" in {
      val result = model.displayWorthWhenInherited

      result shouldBe false
    }
  }

  "Creating a model for a property received as a gift after legislation start date" should {
    val model = YourAnswersSummaryModel(
      disposalDate = LocalDate.parse("2015-05-05"),
      disposalValue = None,
      worthWhenSoldForLess = None,
      whoDidYouGiveItTo = None,
      worthWhenGaveAway = None,
      disposalCosts = 1000,
      acquisitionValue = None,
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      acquisitionCosts = 1000,
      improvements = 0,
      givenAway = false,
      sellForLess = Some(false),
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      howBecameOwner = Some("Gifted"),
      boughtForLessThanWorth = None
    )

    "return a result of true for displayWorthWhenSold" in {
      val result = model.displayWorthWhenSold

      result shouldBe true
    }

    "return a result of false for displayWorthWhenSoldForLess" in {
      val result = model.displayWorthWhenSoldForLess

      result shouldBe false
    }

    "return a result of false for displayBoughtForLessThanWorth" in {
      val result = model.displayBoughtForLessThanWorth

      result shouldBe false
    }

    "return a result of false for displayWorthWhenBought" in {
      val result = model.displayWorthWhenBought

      result shouldBe false
    }

    "return a result of false for displayWorthWhenBoughtForLess" in {
      val result = model.displayWorthWhenBoughtForLess

      result shouldBe false
    }

    "return a result of true for displayWorthWhenGifted" in {
      val result = model.displayWorthWhenGifted

      result shouldBe true
    }

    "return a result of false for displayWorthWhenInherited" in {
      val result = model.displayWorthWhenInherited

      result shouldBe false
    }
  }

  "Creating a model for a property inherited after legislation start date" should {
    val model = YourAnswersSummaryModel(
      disposalDate = LocalDate.parse("2015-05-05"),
      disposalValue = None,
      worthWhenSoldForLess = None,
      whoDidYouGiveItTo = None,
      worthWhenGaveAway = None,
      disposalCosts = 1000,
      acquisitionValue = None,
      worthWhenInherited = None,
      worthWhenGifted = None,
      worthWhenBoughtForLess = None,
      acquisitionCosts = 1000,
      improvements = 0,
      givenAway = false,
      sellForLess = Some(false),
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      howBecameOwner = Some("Inherited"),
      boughtForLessThanWorth = None
    )

    "return a result of true for displayWorthWhenSold" in {
      val result = model.displayWorthWhenSold

      result shouldBe true
    }

    "return a result of false for displayWorthWhenSoldForLess" in {
      val result = model.displayWorthWhenSoldForLess

      result shouldBe false
    }

    "return a result of false for displayBoughtForLessThanWorth" in {
      val result = model.displayBoughtForLessThanWorth

      result shouldBe false
    }

    "return a result of false for displayWorthWhenBought" in {
      val result = model.displayWorthWhenBought

      result shouldBe false
    }

    "return a result of false for displayWorthWhenBoughtForLess" in {
      val result = model.displayWorthWhenBoughtForLess

      result shouldBe false
    }

    "return a result of false for displayWorthWhenGifted" in {
      val result = model.displayWorthWhenGifted

      result shouldBe false
    }

    "return a result of true for displayWorthWhenInherited" in {
      val result = model.displayWorthWhenInherited

      result shouldBe true
    }
  }
}

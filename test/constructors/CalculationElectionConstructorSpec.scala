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

package constructors

import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import models.nonresident.TotalGainResultsModel
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class CalculationElectionConstructorSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  object TestCalculationElectionConstructor extends CalculationElectionConstructor {
    val calcConnector: CalculatorConnector = mockCalcConnector
  }

  implicit val hc = new HeaderCarrier()
  val mockCalcConnector: CalculatorConnector = mock[CalculatorConnector]

  val onlyFlat = TotalGainResultsModel(BigDecimal(0), None, None)
  val flatAndRebased = TotalGainResultsModel(BigDecimal(-100), Some(BigDecimal(-50)), None)
  val flatAndTime = TotalGainResultsModel(BigDecimal(-20), None, Some(BigDecimal(-300)))
  val flatRebasedAndTime = TotalGainResultsModel(BigDecimal(0), Some(BigDecimal(0)), Some(BigDecimal(-300)))

  "Calling generateElection" should {

    "when only a flat calculation result is provided" should {

      val calculations = TestCalculationElectionConstructor.generateElection(hc, onlyFlat)
      "produce sequence with one element" in {
        calculations.size shouldBe 1
      }

      "contain a flat calculation" in {
        calculations.head._1 shouldEqual "flat"
      }
    }

    "when a flat calculation and a rebased calculation result are provided" should {

      val calculations = TestCalculationElectionConstructor.generateElection(hc, flatAndRebased)

      "produce two entries in the sequence" in {
        calculations.size shouldBe 2
      }

      "should be returned with an order" which {

        "contain a flat calculation" in {
          calculations.head._1 shouldEqual "flat"
        }

        "should have rebased as the second element" in {
          calculations(1)._1 shouldEqual "rebased"
        }
      }
    }

    "when a flat calculation and a time calculation result are provided" should {

      val calculations = TestCalculationElectionConstructor.generateElection(hc, flatAndTime)

      "produce two entries in the sequence" in {
        calculations.size shouldBe 2
      }

      "should be returned with an order" which {

        "contain a time calculation" in {
          calculations.head._1 shouldEqual "time"
        }

        "should have flat as the second element" in {
          calculations(1)._1 shouldEqual "flat"
        }
      }
    }

    "when a flat, rebased and time are all provided" should {

      val calculations = TestCalculationElectionConstructor.generateElection(hc, flatRebasedAndTime)

      "produce a three entry sequence" in {
        calculations.size shouldBe 3
      }

      "should be returned with an order" which {

        "contain a time calculation" in {
          calculations.head._1 shouldEqual "time"
        }

        "should have flat as the second element" in {
          calculations(1)._1 shouldEqual "flat"
        }

        "should have rebased as the third element" in {
          calculations(2)._1 shouldEqual "rebased"
        }
      }
    }
  }
}

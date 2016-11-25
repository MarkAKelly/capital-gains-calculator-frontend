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

package constructors.nonresident

import connectors.CalculatorConnector
import models.nonresident.TotalGainResultsModel
import play.api.i18n.Messages
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object CalculationElectionConstructor extends CalculationElectionConstructor {
  val calcConnector = CalculatorConnector
}

trait CalculationElectionConstructor {

  val calcConnector: CalculatorConnector

  def generateElection(hc: HeaderCarrier,
                       totalGainResults: TotalGainResultsModel
                      ): Future[Seq[(String, String, String, Option[String])]] = {

    val flatElement = Some((flatElementConstructor(), totalGainResults.flatGain))
    val timeElement = totalGainResults.timeApportionedGain.collect { case el => (timeElementConstructor(), el)}
    val rebasedElement = totalGainResults.rebasedGain.collect { case el => (rebasedElementConstructor(), el)}
    val items = Seq(flatElement, timeElement, rebasedElement).collect { case Some(item) => item}
    Future.successful(items.sortBy(_._2).map(_._1))
  }

  private def rebasedElementConstructor() = {
    (
      "rebased",
      BigDecimal(0).setScale(2).toString(),
      Messages("calc.calculationElection.message.rebased"),
      Some(Messages("calc.calculationElection.message.rebasedDate"))
      )
  }

  private def flatElementConstructor() = {
    (
      "flat",
      BigDecimal(0).setScale(2).toString(),
      Messages("calc.calculationElection.message.flat"),
      None
      )
  }

  private def timeElementConstructor() = {
    (
      "time",
      BigDecimal(0).setScale(2).toString(),
      Messages("calc.calculationElection.message.time"),
      Some(Messages("calc.calculationElection.message.timeDate"))
      )
  }
}

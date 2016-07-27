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

package controllers.resident.shares

import java.text.SimpleDateFormat
import java.util.Date
import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import models.resident._
import models.resident.shares.GainAnswersModel
import play.api.mvc.Result
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.calculation.resident.shares.{summary => views}
import scala.concurrent.Future

object SummaryController extends SummaryController {
  val calculatorConnector = CalculatorConnector
}

trait SummaryController extends FeatureLock {

  val calculatorConnector: CalculatorConnector

//  private val homeLink = controllers.resident.shares.routes.GainController.disposalDate().url

  val summary = FeatureLockForRTTShares.async { implicit request =>

    def getTaxYear(disposalDate: Date): Future[Option[TaxYearModel]] = {
      val formats = new SimpleDateFormat("yyyy-MM-dd")
      calculatorConnector.getTaxYear(formats.format(disposalDate))
    }

    def routeRequest(totalGainAnswers: GainAnswersModel,
                     grossGain: BigDecimal,
                     taxYear: Option[TaxYearModel])(implicit hc: HeaderCarrier): Future[Result] = {
      Future.successful(Ok(views.gainSummary(totalGainAnswers, grossGain, taxYear.get)))
    }
    for {
      answers <- calculatorConnector.getShareGainAnswers
      taxYear <- getTaxYear(answers.disposalDate)
      grossGain <- calculatorConnector.calculateRttShareGrossGain(answers)
      routeRequest <- routeRequest(answers, grossGain, taxYear)
    } yield routeRequest
  }
}
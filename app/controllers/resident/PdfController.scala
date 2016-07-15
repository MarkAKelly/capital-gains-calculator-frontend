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

package controllers.resident

import common.Dates._
import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import it.innove.play.pdf.PdfGenerator
import models.resident.{TaxYearModel, YourAnswersSummaryModel}
import play.api.mvc.{Action, RequestHeader}

import scala.concurrent.Future

object PdfController extends PdfController {
  val calcConnector = CalculatorConnector
}

trait PdfController extends FeatureLock {

  val calcConnector: CalculatorConnector

  private def host(implicit request: RequestHeader): String = {
    s"http://${request.host}/"
  }

  //#####Gain summary actions#####\\
  val gainSummaryPdf = Action.async {implicit request =>
    lazy val taxYearModel = TaxYearModel("2018/19", false, "2016/17")

    val testModel = YourAnswersSummaryModel(
      constructDate(12,9,2018),
      10,
      20,
      30,
      40,
      50
    )
    Future.successful(PdfGenerator.ok(views.html.pdf.resident.gainSummaryPdf(testModel, -2000, taxYearModel), host).toScala)
  }

  //#####Deductions summary actions#####\\

  //#####Final summary actions#####\\
}

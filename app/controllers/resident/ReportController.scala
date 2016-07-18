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

import connectors.CalculatorConnector
import controllers.predicates.FeatureLock
import it.innove.play.pdf.PdfGenerator
import play.api.i18n.Messages
import play.api.mvc.{Action, RequestHeader, Result}
import play.mvc.BodyParser.AnyContent
import scala.concurrent.Future
import play.api.mvc.{Action, RequestHeader}

import scala.concurrent.Future

object ReportController extends ReportController {
  val calcConnector = CalculatorConnector
}

trait ReportController extends FeatureLock {

  val calcConnector: CalculatorConnector

  private def host(implicit request: RequestHeader): String = {
    s"http://${request.host}/"
  }

  //#####Gain summary actions#####\\
  val gainSummaryReport = FeatureLockForRTT.async { implicit request =>
    val fileName = Messages("calc.resident.summary.title")
    Future.successful(PdfGenerator.ok(views.html.pdf.resident.gainSummaryPdf(), host).toScala
      .withHeaders("Content-Disposition" -> s"""attachment; filename="$fileName""""))
  }

  //#####Deductions summary actions#####\\

  //#####Final summary actions#####\\
}

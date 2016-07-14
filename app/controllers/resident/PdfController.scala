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

import controllers.predicates.FeatureLock
import it.innove.play.pdf.PdfGenerator
import play.api.mvc.{Action, RequestHeader}

import scala.concurrent.Future

object PdfController extends PdfController

trait PdfController extends FeatureLock {

  private def host(implicit request: RequestHeader): String = {
    s"http://${request.host}/"
  }

  val pdfTest = Action.async { implicit request =>
    val fileName = "capital-gains-summary.pdf"
    Ok(PdfGenerator.toBytes(views.html.introduction.intro(), host))
    Future.successful(PdfGenerator.ok(views.html.introduction.intro(), host).toScala.withHeaders("Content-Disposition" -> s"""attachment; filename="$fileName""""))
  }
}

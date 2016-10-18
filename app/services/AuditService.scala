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

package services

import config.FrontendGlobal
import play.api.mvc.Request
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.{DataEvent, DeviceId}
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.Future

object AuditService extends AuditService {
  override lazy val auditSource = "capital-gains-calculator-frontend"
  override lazy val auditConnector = FrontendGlobal.auditConnector
}

trait AuditService {

  def auditSource: String

  def auditConnector: AuditConnector

  import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

  def sendEvent(auditType: String,
                details: Map[String, String])(implicit request: Request[_], hc: HeaderCarrier): Future[AuditResult] = {
    val sessionId = request.session.get(SessionKeys.sessionId)
    auditConnector.sendEvent(buildEvent(auditType, details, sessionId))
  }

  def buildEvent(auditType: String,
                 details: Map[String, String],
                 sessionId: Option[String] = None)(implicit request: Request[_], hc: HeaderCarrier): DataEvent = {
    val auditEvent = DataEvent(
      auditSource = auditSource,
      auditType = auditType,
      tags = hc.headers.toMap,
      detail = generateDetails(request, details))
    auditEvent
  }

  private def generateDetails(request: Request[_], details: Map[String, String]): Map[String, String] = {
    details ++ Map("deviceID" -> DeviceId(request).map(_.id).getOrElse("-"))
  }
}

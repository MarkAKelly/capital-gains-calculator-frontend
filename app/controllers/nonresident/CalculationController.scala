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

package controllers.nonresident

import java.time.LocalDate
import common.{Dates, KeystoreKeys}
import forms.nonresident.OtherReliefsForm._
import forms.nonresident.PersonalAllowanceForm._
import forms.nonresident.RebasedValueForm._
import java.util.{Date, UUID}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import scala.concurrent.Future
import views.html._
import common.DefaultRoutes._
import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import controllers.predicates.ValidActiveSession
import models.nonresident._

trait CalculationController extends FrontendController with ValidActiveSession {

  override val sessionTimeoutUrl = controllers.nonresident.routes.SummaryController.restart().url
  val calcConnector: CalculatorConnector
  val calcElectionConstructor: CalculationElectionConstructor

  //################### Shared/Common methods #######################
  def getAcquisitionDate(implicit hc: HeaderCarrier): Future[Option[LocalDate]] =
    calcConnector.fetchAndGetFormData[AcquisitionDateModel](KeystoreKeys.acquisitionDate).map {
      case Some(AcquisitionDateModel("Yes", Some(day), Some(month), Some(year))) => Some(Dates.constructDate(day, month, year))
      case _ => None
    }

  //################### Disabled Trustee methods #######################

  //################### Personal Allowance methods #######################
  
  //################### Other Properties methods #######################

  //################### Rebased value methods #######################

  //################### Rebased costs methods #######################

  //################### Improvements methods #######################

  //################### Disposal Date methods #######################

  //################### No Capital Gains Tax #######################

  //################### Disposal Value methods #######################

  //################### Disposal Costs methods #######################

  //################### Private Residence Relief methods #######################

  //################### Allowable Losses methods #######################

  //################### Other Reliefs with no calc selection methods (flat) #######################

  //################### Flat Other Reliefs methods #######################

  //################### Time Apportioned Other Reliefs methods #######################

  //################### Rebased Other Reliefs methods #######################

  //################### Summary Methods ##########################

}

object CalculationController extends CalculationController {
  val calcConnector = CalculatorConnector
  val calcElectionConstructor = CalculationElectionConstructor
}
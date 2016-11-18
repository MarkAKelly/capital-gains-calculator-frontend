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

package views.nonResident

import controllers.helpers.FakeRequestHelper
import forms.nonresident.marketValueGaveAwayForm._
import org.jsoup.Jsoup
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.marketValueGaveAway

/**
  * Created by emma on 17/11/16.
  */
class marketValueGaveAwayViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{
  "The market value when gave away page" should {

    lazy val view = marketValueGaveAway(marketValueForm)(fakeRequestWithSession)
    lazy val document = Jsoup.parse(view.body)

    "supplied with no errors" should {
      s"have a title of" in {
        document.title() shouldBe Messages("calc.marketValue.gaveItAway.question")
      }


    }
  }
}

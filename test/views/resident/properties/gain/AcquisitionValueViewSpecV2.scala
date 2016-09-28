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

package views.resident.properties.gain

import forms.resident.AcquisitionValueForm._
import org.jsoup.Jsoup
import views.html.calculation.resident.properties.gain
import views.html.calculation.resident.properties.{gain => views}

/**
  * Created by emma on 28/09/16.
  */
class AcquisitionValueViewSpecV2 extends BaseGainViewSpec {
  lazy val view = views.acquisitionValue(acquisitionValueForm)(fakeRequest)
  lazy val doc= Jsoup.parse(view.body)

  standardGainView(doc, "AcquisitionValue", assets.MessageLookup.acquisitionValue)
}

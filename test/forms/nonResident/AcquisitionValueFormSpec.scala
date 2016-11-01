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

package forms.nonResident

import common.KeystoreKeys
import connectors.CalculatorConnector
import constructors.nonresident.CalculationElectionConstructor
import controllers.nonresident.AcquisitionValueController
import forms.nonresident.AcquisitionValueForm
import models.nonresident.{AcquisitionDateModel, AcquisitionValueModel}
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap
import org.scalatest.mock.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.SessionKeys
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import models.nonresident.AcquisitionValueModel._

import scala.concurrent.Future
import AcquisitionValueForm._
import assets.MessageLookup
import assets.MessageLookup.NonResident.AllowableLosses
import play.api.i18n.Messages
/**
  * Created by emma on 31/10/16.
  */


class AcquisitionValueFormSpec extends UnitSpec with WithFakeApplication with MockitoSugar{

  def setupTarget(
                   getData: Option[AcquisitionValueModel],
                   postData: Option[AcquisitionValueModel],
                   acquisitionDateModel: Option[AcquisitionDateModel] = None): AcquisitionValueController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockCalcElectionConstructor = mock[CalculationElectionConstructor]

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionValueModel](Matchers.eq(KeystoreKeys.acquisitionValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(acquisitionDateModel))

    lazy val data = CacheMap("form-id", Map("data" -> Json.toJson(postData.getOrElse(AcquisitionValueModel(0)))))
    when(mockCalcConnector.saveFormData[AcquisitionValueModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))

    new AcquisitionValueController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val calcElectionConstructor: CalculationElectionConstructor = mockCalcElectionConstructor
    }
  }

  def checkMessageAndError(messageLookup: String, mapping: String): Unit ={
    lazy val form = acquisitionValueForm.bind(Map("acquisitionValue" -> mapping))

    "return a form with errors" in {
      form.hasErrors shouldBe true
    }

    "return 1 error" in {
      form.errors.size shouldBe 1
    }

    s"return an error message ${messageLookup}" in {
      form.error("acquisitionValue").get.message shouldBe messageLookup
    }
  }

  "Creating a form using a valid model" should {
    "return a form with the data specified in the model" in {
      lazy val model = AcquisitionValueModel(1000)
      lazy val form = acquisitionValueForm.fill(model)
      form.value shouldBe Some(model)
    }
  }

  "Creating a form using a valid map" should {
    "return a form with the data specific in the model (1000)" in {
      lazy val form = acquisitionValueForm.bind(Map("acquisitionValue" -> "1000"))
      form.value shouldBe Some(AcquisitionValueModel(1000))
    }
  }

  "Creating a form using an invalid map" when {
    "supplied with no data" should {
      lazy val form = acquisitionValueForm.bind(Map("acquisitionValue"-> ""))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message ${MessageLookup.NonResident.mandatoryAmount}" in {
        form.error("acquisitionValue").get.message shouldBe MessageLookup.NonResident.mandatoryAmount
      }
    }

    "supplied with data of the wrong format (incorrect value for acquisitionValue...)" should {
      lazy val form = acquisitionValueForm.bind(Map("acquisitionValue" -> "junk text"))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }
      "return 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message ${MessageLookup.NonResident.mandatoryAmount}" in {
        form.error("acquisitionValue").get.message shouldBe MessageLookup.NonResident.mandatoryAmount
      }
    }

    "supplied with data containing a negative value" should {
      lazy val form = acquisitionValueForm.bind(Map("acquisitionValue" -> "-1000"))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error message ${MessageLookup.NonResident.AcquisitionValue.errorNegative}" in {
        form.error("acquisitionValue").get.message shouldBe MessageLookup.NonResident.AcquisitionValue.errorNegative
      }
    }

    "supplied with data containg a value with too many decimal places" should {
      //lazy val form = acquisitionValueForm.bind(Map("acquisitionValue" -> "1.111"))

      /*"return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error with message ${MessageLookup.NonResident.AcquisitionValue.errorDecimalPlaces}" in {
        form.error("acquisitionValue").get.message shouldBe MessageLookup.NonResident.AcquisitionValue.errorDecimalPlaces
      }*/
      checkMessageAndError(MessageLookup.NonResident.AcquisitionValue.errorDecimalPlaces, "1.11111111111")
    }
  }
}

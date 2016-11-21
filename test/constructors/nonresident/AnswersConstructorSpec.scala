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

import common.KeystoreKeys
import connectors.CalculatorConnector
import models.nonresident._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class AnswersConstructorSpec extends UnitSpec with MockitoSugar {

  def setupMockedAnswersConstructor(totalGainAnswersModel: TotalGainAnswersModel): AnswersConstructor = {

    val mockConnector = mock[CalculatorConnector]

    when(mockConnector.fetchAndGetFormData[DisposalDateModel](Matchers.eq(KeystoreKeys.disposalDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalDateModel)))

    when(mockConnector.fetchAndGetFormData[SoldOrGivenAwayModel](Matchers.eq(KeystoreKeys.soldOrGivenAway))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.soldOrGivenAwayModel)))

    when(mockConnector.fetchAndGetFormData[SoldForLessModel](Matchers.eq(KeystoreKeys.NonResidentKeys.soldForLess))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.soldForLessModel))

    when(mockConnector.fetchAndGetFormData[DisposalValueModel](Matchers.eq(KeystoreKeys.disposalValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalValueModel)))

    when(mockConnector.fetchAndGetFormData[DisposalCostsModel](Matchers.eq(KeystoreKeys.disposalCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.disposalCostsModel)))

    when(mockConnector.fetchAndGetFormData[HowBecameOwnerModel](Matchers.eq(KeystoreKeys.howBecameOwner))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.howBecameOwnerModel)))

    when(mockConnector.fetchAndGetFormData[BoughtForLessModel](Matchers.eq(KeystoreKeys.boughtForLess))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.boughtForLessModel))

    when(mockConnector.fetchAndGetFormData[AcquisitionValueModel](Matchers.eq(KeystoreKeys.acquisitionValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionValueModel)))

    when(mockConnector.fetchAndGetFormData[AcquisitionCostsModel](Matchers.eq(KeystoreKeys.acquisitionCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionCostsModel)))

    when(mockConnector.fetchAndGetFormData[AcquisitionDateModel](Matchers.eq(KeystoreKeys.acquisitionDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.acquisitionDateModel)))

    when(mockConnector.fetchAndGetFormData[RebasedValueModel](Matchers.eq(KeystoreKeys.rebasedValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.rebasedValueModel))

    when(mockConnector.fetchAndGetFormData[RebasedCostsModel](Matchers.eq(KeystoreKeys.rebasedCosts))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.rebasedCostsModel))

    when(mockConnector.fetchAndGetFormData[ImprovementsModel](Matchers.eq(KeystoreKeys.improvements))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(totalGainAnswersModel.improvementsModel)))

    when(mockConnector.fetchAndGetFormData[OtherReliefsModel](Matchers.eq(KeystoreKeys.otherReliefsFlat))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalGainAnswersModel.otherReliefsFlat))

    new AnswersConstructor {
      override val calculatorConnector: CalculatorConnector = mockConnector
    }
  }

  val totalGainNoOptionalModel = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2016),
    SoldOrGivenAwayModel(false),
    None,
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    HowBecameOwnerModel("Gifted"),
    None,
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel("No", None, None, None),
    None,
    None,
    ImprovementsModel("No", None, None),
    None
  )

  val totalGainAllOptionalModel = TotalGainAnswersModel(
    DisposalDateModel(10, 10, 2016),
    SoldOrGivenAwayModel(true),
    Some(SoldForLessModel(false)),
    DisposalValueModel(10000),
    DisposalCostsModel(100),
    HowBecameOwnerModel("Bought"),
    Some(BoughtForLessModel(false)),
    AcquisitionValueModel(5000),
    AcquisitionCostsModel(200),
    AcquisitionDateModel("Yes", Some(1), Some(4), Some(2013)),
    Some(RebasedValueModel("Yes", Some(7500))),
    Some(RebasedCostsModel("Yes", Some(150))),
    ImprovementsModel("Yes", Some(50), Some(25)),
    Some(OtherReliefsModel(1000))
  )

  "Calling getNRTotalGainAnswers" should {

    "return a valid TotalGainAnswersModel with no optional values" in {
      val hc = mock[HeaderCarrier]
      val constructor = setupMockedAnswersConstructor(totalGainNoOptionalModel)
      val result = constructor.getNRTotalGainAnswers(hc)

      await(result) shouldBe totalGainNoOptionalModel
    }

    "return a valid TotalGainAnswersModel with all optional values" in {
      val hc = mock[HeaderCarrier]
      val constructor = setupMockedAnswersConstructor(totalGainAllOptionalModel)
      val result = constructor.getNRTotalGainAnswers(hc)

      await(result) shouldBe totalGainAllOptionalModel
    }
  }
}

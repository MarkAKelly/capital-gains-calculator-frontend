package controllers.resident.properties.GainControllerSpec

import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.properties.GainController
import models.resident.properties.gain.PropertyRecipientModel
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import scala.concurrent.Future
import org.mockito.Mockito._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import common.KeystoreKeys.{ResidentPropertyKeys => keystoreKeys}

/**
  * Created by emma on 21/09/16.
  */
class PropertyRecipientActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar{

  def setupTarget(getData: Option[PropertyRecipientModel]) : GainController = {
    val mockCalcConnector = mock[CalculatorConnector]
    when(
      mockCalcConnector.fetchAndGetFormData([PropertyRecipientModel](Matchers.eq(keystoreKeys.propertyRecipient))(Matchers.any(), Matchers.any())).
      thenReturn(Future.successful(getData))
    )
  }
}

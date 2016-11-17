package views.nonResident

import controllers.helpers.FakeRequestHelper
import forms.nonresident.marketValueGaveAwayForm
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.nonresident.marketValueGaveAway

/**
  * Created by emma on 17/11/16.
  */
class marketValueGaveAwayViewSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{
  "The market value when gave away page" should {

    lazy val view = marketValueGaveAway(marketValueGaveAwayForm)
  }
}

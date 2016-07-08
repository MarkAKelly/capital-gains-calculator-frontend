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

package forms.resident
import common.Constants
import common.Transformers.stringToBigDecimal
import common.Validation._
import models.resident.AnnualExemptAmountModel
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import uk.gov.hmrc.play.views.helpers.MoneyPounds

object AnnualExemptAmountForm {

  def validateMaxAEA (limit: BigDecimal): BigDecimal => Boolean = {
    input => if(input > limit) false else true
  }

  def annualExemptAmountForm(maxAEA: BigDecimal = BigDecimal(0)): Form[AnnualExemptAmountModel] = Form(
    mapping(
      "amount" -> text
        .verifying(Messages("calc.common.error.mandatoryAmount"), mandatoryCheck)
        .verifying(Messages("calc.common.error.invalidAmount"), bigDecimalCheck)
        .transform[BigDecimal](stringToBigDecimal, _.toString)
        .verifying(Messages("calc.common.error.maxAmountExceeded", MoneyPounds(Constants.maxNumeric, 0).quantity), maxCheck)
        .verifying(Messages("calc.common.error.maxAmountExceeded", MoneyPounds(maxAEA, 0).quantity), validateMaxAEA(maxAEA))
        .verifying(Messages("calc.common.error.minimumAmount"), minCheck)
        .verifying(Messages("calc.common.error.invalidAmount"), decimalPlacesCheck)
    )(AnnualExemptAmountModel.apply)(AnnualExemptAmountModel.unapply)
  )

}
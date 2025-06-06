/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.common

import forms.mappings.{Mappings, Transforms}
import models.common.PaymentDetails
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages
import utils.DateConstraintHandlers.{localDateMappingWithDateRange, localDatesConstraintHandler}

import java.time.LocalDate
import javax.inject.Inject

class PaymentDetailsFormProvider @Inject() extends Mappings with Transforms {

  import PaymentDetailsFormProvider._

  def apply(min: LocalDate, max: LocalDate)(implicit messages: Messages): Form[PaymentDetails] =
    Form(
      mapping("amountPaid" ->
        bigDecimal2DP("paymentDetails.value.error.nothingEntered",
          "paymentDetails.value.error.notANumber",
          "paymentDetails.value.error.tooManyDecimals")
          .verifying(
            maximumValue[BigDecimal](maxPaymentValue, "paymentDetails.value.error.amountTooHigh"),
            minimumValue[BigDecimal](0, "paymentDetails.value.error.negativeValue"),
            zeroValue[BigDecimal](0, "paymentDetails.value.error.zeroEntered")
          ),
        localDateMappingWithDateRange(field = "eventDate", date = (min, max))
      )
      (PaymentDetails.apply)(p => Some(Tuple.fromProductTyped(p)))
    )
}

object PaymentDetailsFormProvider {
  private val maxPaymentValue: BigDecimal = 999999999.99
}

/*
 * Copyright 2022 HM Revenue & Customs
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

package forms.event1

import base.SpecBase
import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

import java.time.LocalDate

class PaymentValueAndDateFormProviderSpec extends IntFieldBehaviours with SpecBase {

  // TODO: change implementation to real date once preceding pages are implemented, using stubDate for now.
  //  private val stubDate: LocalDate = LocalDate.now()
  private val stubMin: LocalDate = LocalDate of(2022, 4, 6)
  private val stubMax: LocalDate = LocalDate of(2023, 4, 5)

  private val form = new PaymentValueAndDateFormProvider().apply(stubMin, stubMax)

  ".value" - {

    val fieldName = "paymentValue"

    val minimum = 0
    val maximum = Int.MaxValue

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "paymentValueAndDate.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "paymentValueAndDate.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "paymentValueAndDate.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "paymentValueAndDate.error.required")
    )
  }
}

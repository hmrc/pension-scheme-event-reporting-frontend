/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.event7

import forms.mappings.{Mappings, Transforms}
import models.event7.PaymentDate
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.i18n.Messages
import utils.DateHelper.formatDateDMY

import java.time.LocalDate
import javax.inject.Inject

class PaymentDateFormProvider @Inject() extends Mappings with Transforms { // scalastyle:off magic.number

  private val startDate: LocalDate = LocalDate.of(2006, 4, 6)

  private def endDate(date: LocalDate): LocalDate = {
    date match {
      case _ if date.isBefore(LocalDate.of(date.getYear, 4, 6)) =>
        LocalDate.of(date.getYear, 4, 5)
      case _ =>
        LocalDate.of(date.getYear + 1, 4, 5)
    }
  }

  def apply(max: LocalDate)(implicit messages: Messages): Form[PaymentDate] =
    Form(
      mapping("paymentDate" ->
        localDate(
          invalidKey = "genericDate.error.invalid"
        ).verifying(
          minDate(startDate, messages("paymentDate.date.error.outsideReportedYear", formatDateDMY(endDate(max)))),
          maxDate(endDate(max), messages("paymentDate.date.error.outsideReportedYear", formatDateDMY(endDate(max))))
        )
      )
      (PaymentDate.apply)(PaymentDate.unapply)
    )
}

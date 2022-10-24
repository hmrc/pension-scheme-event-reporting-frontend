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

package viewmodels.event1.checkAnswers

import base.SpecBase
import models.UserAnswers
import models.enumeration.EventType.Event1
import models.event1.PaymentDetails
import pages.event1.PaymentValueAndDatePage
import pages.{CheckAnswersPage, CheckYourAnswersPage, EmptyWaypoints, Waypoints}
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._

import java.time.LocalDate
import java.time.format.DateTimeFormatter


class PaymentValueAndDateSummarySpec extends SpecBase with SummaryListFluency {


  "rowPaymentValue" - {

    "must display correct information for the payment value" in {

      val paymentDetails = PaymentDetails(1000.00, LocalDate.now())

      val answer = UserAnswers().setOrException(PaymentValueAndDatePage, paymentDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val paymentValueAsString = if (paymentDetails.paymentValue.isWhole()) {
        s"£${paymentDetails.paymentValue}.00"
      } else {
        s"£${paymentDetails.paymentValue}"
      }

      PaymentValueAndDateSummary.rowPaymentValue(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "Payment value",
          value = ValueViewModel(paymentValueAsString),
          actions = Seq(
            ActionItemViewModel("site.change", PaymentValueAndDatePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("paymentValueAndDate.value.change.hidden"))
          )
        )
      )
    }
  }

  "rowPaymentDate" - {

    "must display correct information for the payment date" in {

      val paymentDetails = PaymentDetails(1000.00, LocalDate.now())

      val answer = UserAnswers().setOrException(PaymentValueAndDatePage, paymentDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val date = paymentDetails.paymentDate
      val format = DateTimeFormatter.ofPattern("dd/MM/yyyy")

      PaymentValueAndDateSummary.rowPaymentDate(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "Payment date",
          value = ValueViewModel((format.format(date))),
          actions = Seq(
            ActionItemViewModel("site.change", PaymentValueAndDatePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("paymentValueAndDate.date.change.hidden"))
          )
        )
      )
    }
  }
}

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

package viewmodels.event7.checkAnswers

import models.UserAnswers
import models.enumeration.EventType.Event7
import models.event7.PaymentDate
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.event7.{Event7CheckYourAnswersPage, PaymentDatePage}
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._

import java.time.LocalDate
import java.time.format.DateTimeFormatter


class PaymentDateSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()
  private val eventType = Event7


  "row PaymentDate" - {

    "must display correct information for the date" in {

      val paymentDateDetails = PaymentDate(LocalDate.now())

      val answer = UserAnswers().setOrException(PaymentDatePage(0), paymentDateDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event7CheckYourAnswersPage(0)

      val date = paymentDateDetails.date
      val format = DateTimeFormatter.ofPattern("dd MMMM yyyy")

      PaymentDateSummary.rowPaymentDate(answer, waypoints, sourcePage, eventType, 0) mustBe Some(
        SummaryListRowViewModel(
          key = messages("paymentDate.date.checkYourAnswersLabel"),
          value = ValueViewModel(format.format(date)),
          actions = Seq(
            ActionItemViewModel("site.change", PaymentDatePage(0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("site.change") + " " + messages("paymentDate.date.change.hidden"))
          )
        )
      )
    }
  }
}

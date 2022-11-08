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

import models.UserAnswers
import models.event1.PaymentDetails
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.event1.{Event1CheckYourAnswersPage, PaymentValueAndDatePage}
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._

import java.time.LocalDate
import java.time.format.DateTimeFormatter


class PaymentValueAndDateSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()


  "rowPaymentValue" - {

    "must display correct information for the payment value" in {

      val paymentDetails = PaymentDetails(1000000.00, LocalDate.now())
      val paymentDetailsValue = "£1,000,000.00"

      val answer = UserAnswers().setOrException(PaymentValueAndDatePage(0), paymentDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)

      PaymentValueAndDateSummary.rowPaymentValue(answer, waypoints, 0, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = messages("paymentValueAndDate.value.checkYourAnswersLabel"),
          value = ValueViewModel(HtmlContent(Html(paymentDetailsValue))),
          actions = Seq(
            ActionItemViewModel("site.change", PaymentValueAndDatePage(0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("paymentValueAndDate.value.change.hidden"))
          )
        )
      )
    }
  }

  "rowPaymentDate" - {

    "must display correct information for the payment date" in {

      val paymentDetails = PaymentDetails(1000.00, LocalDate.now())

      val answer = UserAnswers().setOrException(PaymentValueAndDatePage(0), paymentDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)

      val date = paymentDetails.paymentDate
      val format = DateTimeFormatter.ofPattern("dd/MM/yyyy")

      PaymentValueAndDateSummary.rowPaymentDate(answer, waypoints, 0, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = messages("paymentValueAndDate.date.checkYourAnswersLabel"),
          value = ValueViewModel(format.format(date)),
          actions = Seq(
            ActionItemViewModel("site.change", PaymentValueAndDatePage(0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("paymentValueAndDate.date.change.hidden"))
          )
        )
      )
    }
  }
}

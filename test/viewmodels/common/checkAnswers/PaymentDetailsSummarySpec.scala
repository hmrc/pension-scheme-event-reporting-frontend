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

package viewmodels.common.checkAnswers

import models.UserAnswers
import models.common.PaymentDetails
import models.enumeration.EventType
import models.enumeration.EventType.{Event4, Event5}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.common.PaymentDetailsPage
import pages.event4.Event4CheckYourAnswersPage
import pages.event5.Event5CheckYourAnswersPage
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._

import java.time.LocalDate
import java.time.format.DateTimeFormatter


class PaymentDetailsSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()
  private val paymentDetails = PaymentDetails(54.23, LocalDate.now())
  private val amountPaid = "£54.23"

  testRowAmountPaid(Event4, Event4CheckYourAnswersPage(0), true)
  testRowEventDate(Event4, Event4CheckYourAnswersPage(0), true)
  testRowAmountPaid(Event5, Event5CheckYourAnswersPage(0), true)
  testRowEventDate(Event5, Event5CheckYourAnswersPage(0), true)
  testRowAmountPaid(Event4, Event4CheckYourAnswersPage(0), false)
  testRowEventDate(Event4, Event4CheckYourAnswersPage(0), false)
  testRowAmountPaid(Event5, Event5CheckYourAnswersPage(0), false)
  testRowEventDate(Event5, Event5CheckYourAnswersPage(0), false)
  private def testRowAmountPaid(eventType: EventType, sourcePage: CheckAnswersPage, isReadOnly: Boolean) = {
    s"rowAmountPaid for Event $eventType and isReadOnly is $isReadOnly" - {

      "must display correct information for the amount paid" in {

        val answer = UserAnswers().setOrException(PaymentDetailsPage(eventType, 0), paymentDetails)
        val waypoints: Waypoints = EmptyWaypoints

        PaymentDetailsSummary.rowAmountPaid(answer, waypoints, sourcePage, isReadOnly, eventType, 0) mustBe Some(
          SummaryListRow(
            key = messages("paymentDetails.value.checkYourAnswersLabel"),
            value = ValueViewModel(HtmlContent(Html(amountPaid))),
            actions = if (isReadOnly) None else {
              Some(Actions(items = Seq(
                ActionItemViewModel("site.change", PaymentDetailsPage(eventType, index = 0).changeLink(waypoints, sourcePage).url)
                  .withVisuallyHiddenText(messages("paymentDetails.value.change.hidden"))
              )))
            }
          )
        )
      }
    }
  }

  private def testRowEventDate(eventType: EventType, sourcePage: CheckAnswersPage, isReadOnly: Boolean) = {
    s"rowEventDate for Event $eventType and isReadOnly is $isReadOnly" - {

      "must display correct information for the event date" in {

        val answer = UserAnswers().setOrException(PaymentDetailsPage(eventType, 0), paymentDetails)
        val waypoints: Waypoints = EmptyWaypoints

        val date = paymentDetails.eventDate
        val format = DateTimeFormatter.ofPattern("dd MMMM yyyy")

        PaymentDetailsSummary.rowEventDate(answer, waypoints, sourcePage, isReadOnly, eventType, 0) mustBe Some(
          SummaryListRow(
            key = messages("paymentDetails.date.checkYourAnswersLabel"),
            value = ValueViewModel(format.format(date)),
            actions = if (isReadOnly) None else {
              Some(Actions(items = Seq(
                ActionItemViewModel("site.change", PaymentDetailsPage(eventType, 0).changeLink(waypoints, sourcePage).url)
                  .withVisuallyHiddenText(messages("paymentDetails.date.change.hidden"))
              )))
            }
          )
        )
      }
    }
  }
}

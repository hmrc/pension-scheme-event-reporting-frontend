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

package viewmodels.checkAnswers

import models.UserAnswers
import pages.event1.PaymentValueAndDatePage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import java.time.LocalDate
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import scala.util.Try

object PaymentValueAndDateSummary {

  def rowPaymentValue(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PaymentValueAndDatePage).map {
      answer =>

        val paymentValueAsString = if (answer.paymentValue.isWhole()) {
          s"£${answer.paymentValue}.00"
        } else {
          s"£${answer.paymentValue}"
        }

        SummaryListRowViewModel(
          key = "Payment value",
          value = ValueViewModel(paymentValueAsString),
          actions = Seq(
            ActionItemViewModel("site.change", PaymentValueAndDatePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("paymentValueAndDate.value.change.hidden"))
          )
        )
    }

  def rowPaymentDate(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PaymentValueAndDatePage).map {
      answer =>

        val d = answer.paymentDate
        val f = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        SummaryListRowViewModel(
          key = "Payment date",
          value = ValueViewModel((f.format(d))),
          actions = Seq(
            ActionItemViewModel("site.change", PaymentValueAndDatePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("paymentValueAndDate.date.change.hidden"))
          )
        )
    }
}

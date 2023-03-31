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

import forms.mappings.Formatters
import models.enumeration.EventType
import models.{Index, UserAnswers}
import pages.event6.AmountCrystallisedAndDatePage
import pages.event7.PaymentDatePage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PaymentDateSummary extends Formatters {

  def rowPaymentDate(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage, eventType: EventType, index: Index)
                         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PaymentDatePage(index)).map {
      answer =>
        SummaryListRowViewModel(
          key = messages("paymentDate.date.checkYourAnswersLabel"),
          value = ValueViewModel(dateFormatter.format(answer.date)),
          actions = Seq(
            ActionItemViewModel("site.change", PaymentDatePage(index).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("site.change") + " " + messages("paymentDate.date.change.hidden"))
          )
        )
    }
}
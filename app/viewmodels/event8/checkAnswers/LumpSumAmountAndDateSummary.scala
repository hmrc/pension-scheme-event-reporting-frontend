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

package viewmodels.event8.checkAnswers

import forms.mappings.Formatters
import models.enumeration.EventType
import models.{Index, UserAnswers}
import pages.event8.LumpSumAmountAndDatePage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object LumpSumAmountAndDateSummary extends Formatters {

  def rowLumpSumValue(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage, isReadOnly: Boolean, eventType: EventType, index: Index)
                     (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(LumpSumAmountAndDatePage(eventType, index)).map {
      answer =>

        SummaryListRow(
          key = messages("lumpSumAmountAndDate.value.checkYourAnswersLabel"),
          value = ValueViewModel(HtmlContent(s"£${currencyFormatter.format(answer.lumpSumAmount)}")),
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", LumpSumAmountAndDatePage(eventType, index).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("lumpSumAmountAndDate.value.change.hidden"))
            )))
          }
        )
    }

  def rowLumpSumDate(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage, isReadOnly: Boolean, eventType: EventType, index: Index)
                    (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(LumpSumAmountAndDatePage(eventType, index)).map {
      answer =>

        SummaryListRow(
          key = messages("lumpSumAmountAndDate.date.checkYourAnswersLabel"),
          value = ValueViewModel(dateFormatter.format(answer.lumpSumDate)),
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", LumpSumAmountAndDatePage(eventType, index).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("lumpSumAmountAndDate.date.change.hidden"))
            )))
          }
        )
    }
}

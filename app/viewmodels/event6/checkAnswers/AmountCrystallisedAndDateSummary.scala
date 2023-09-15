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

package viewmodels.event6.checkAnswers

import forms.mappings.Formatters
import models.enumeration.EventType.Event6
import models.{Index, UserAnswers}
import pages.event6.AmountCrystallisedAndDatePage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AmountCrystallisedAndDateSummary extends Formatters {

  def rowCrystallisedValue(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage, isReadOnly: Boolean, index: Index)
                          (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AmountCrystallisedAndDatePage(Event6, index)).map {
      answer =>

        SummaryListRow(
          key = messages("amountCrystallisedAndDate.value.checkYourAnswersLabel"),
          value = ValueViewModel(HtmlContent(s"£${currencyFormatter.format(answer.amountCrystallised)}")),
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", AmountCrystallisedAndDatePage(Event6, index).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("amountCrystallisedAndDate.value.change.hidden"))
            )))
          }
        )
    }

  def rowCrystallisedDate(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage, isReadOnly: Boolean, index: Index)
                         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AmountCrystallisedAndDatePage(Event6, index)).map {
      answer =>

        SummaryListRow(
          key = messages("amountCrystallisedAndDate.date.checkYourAnswersLabel"),
          value = ValueViewModel(dateFormatter.format(answer.crystallisedDate)),
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", AmountCrystallisedAndDatePage(Event6, index).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("amountCrystallisedAndDate.date.change.hidden"))
            )))
          }
        )
    }
}

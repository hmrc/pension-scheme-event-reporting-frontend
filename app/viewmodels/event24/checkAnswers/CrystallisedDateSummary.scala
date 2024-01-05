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

package viewmodels.event24.checkAnswers

import forms.mappings.Formatters
import models.{Index, UserAnswers}
import pages.event24.CrystallisedDatePage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CrystallisedDateSummary extends Formatters {

  def rowCrystallisedDate(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage, isReadOnly: Boolean, index: Index)
                    (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(CrystallisedDatePage(index)).map {
      answer =>
        SummaryListRow(
          key = messages("crystallisedDate.event24.date.checkYourAnswersLabel"),
          value = ValueViewModel(dateFormatter.format(answer.date)),
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", CrystallisedDatePage(index).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("crystallisedDate.event24.date.change.hidden"))
            )))
          }
        )
    }
}

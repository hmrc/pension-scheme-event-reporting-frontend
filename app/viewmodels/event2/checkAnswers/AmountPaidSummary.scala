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

package viewmodels.event2.checkAnswers

import forms.mappings.Formatters
import models.enumeration.EventType.Event2
import models.{Index, UserAnswers}
import pages.event2.AmountPaidPage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.BeneficiaryDetailsEvent2.getBeneficiaryName
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AmountPaidSummary extends Formatters {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage, isReadOnly: Boolean, index: Index)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AmountPaidPage(index, Event2)).map {
      answer =>
        SummaryListRow(
          key = messages("amountPaid.event2.checkYourAnswersLabel", getBeneficiaryName(Some(answers), index)),
          value = ValueViewModel(Text(s"£${currencyFormatter.format(answer)}")),
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", AmountPaidPage(index, Event2).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("amountPaid.event2.change.hidden", getBeneficiaryName(Some(answers), index)))
            )))
          }
        )
    }
}

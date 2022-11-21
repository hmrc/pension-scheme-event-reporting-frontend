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

import models.{Index, UserAnswers}
import models.enumeration.EventType
import pages.common.TotalPensionAmountsPage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.event1.checkAnswers.PaymentValueAndDateSummary.currencyFormatter
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object TotalPensionAmountsSummary {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage, eventType: EventType, index: Index)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(TotalPensionAmountsPage(eventType, index)).map {
      answer =>

        SummaryListRowViewModel(
          key = s"totalPensionAmounts.event${eventType.toString}.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(s"£${currencyFormatter.format(answer)}")),
          actions = Seq(
            ActionItemViewModel("site.change", TotalPensionAmountsPage(eventType, index).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages(s"totalPensionAmounts.event${eventType.toString}.change.hidden"))
          )
        )
    }
}

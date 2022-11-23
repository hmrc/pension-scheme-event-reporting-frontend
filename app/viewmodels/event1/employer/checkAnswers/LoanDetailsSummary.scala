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

package viewmodels.event1.employer.checkAnswers

import forms.mappings.Formatters
import models.{Index, UserAnswers}
import org.apache.commons.lang3.StringUtils
import pages.event1.employer.LoanDetailsPage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object LoanDetailsSummary extends Formatters {

  def rowLoanAmount(answers: UserAnswers, waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage)
                   (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(LoanDetailsPage(index)).map {
      answer =>

        val value = ValueViewModel(
          HtmlContent(
            Html(answer.loanAmount.map(t => s"£${currencyFormatter.format(t)}").getOrElse(StringUtils.EMPTY))
          )
        )

        SummaryListRowViewModel(
          key = "loanDetails.CYA.loanAmountLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", LoanDetailsPage(index).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("loanAmount.change.hidden"))
          )
        )
    }

  def rowFundValue(answers: UserAnswers, waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage)
                  (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(LoanDetailsPage(index)).map {
      answer =>

        val value = ValueViewModel(
          HtmlContent(
            Html(answer.fundValue.map(t => s"£${currencyFormatter.format(t)}").getOrElse(StringUtils.EMPTY))
          )
        )

        SummaryListRowViewModel(
          key = "loanDetails.CYA.fundValueLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", LoanDetailsPage(index).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("fundAmount.change.hidden"))
          )
        )
    }
}

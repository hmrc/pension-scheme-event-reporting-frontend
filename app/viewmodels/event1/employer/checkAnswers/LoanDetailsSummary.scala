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

import models.UserAnswers
import pages.event1.employer.LoanDetailsPage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object LoanDetailsSummary {

  private def loanDetailsAnswer(loanAmount: Option[BigDecimal])(implicit messages: Messages): Html = {
    def loanDetailsToHtml(amountVal: BigDecimal): String = s"£$amountVal"

    def optionalAmountToHtml(optionalAmount: Option[BigDecimal]): String = optionalAmount match {
      case Some(amount) => loanDetailsToHtml(amount)
      case None => ""
    }

    Html(
      optionalAmountToHtml(loanAmount)
    )
  }

  def rowLoanAmount(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage)
                   (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(LoanDetailsPage).map {
      answer =>

        val value = ValueViewModel(
          HtmlContent(
            loanDetailsAnswer(answer.loanAmount)
          )
        )

        SummaryListRowViewModel(
          key = "loanDetails.CYA.loanAmountLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", LoanDetailsPage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("loanDetails.change.hidden"))
          )
        )
    }

  def rowFundValue(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage)
                  (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(LoanDetailsPage).map {
      answer =>

        val value = ValueViewModel(
          HtmlContent(
            loanDetailsAnswer(answer.fundValue)
          )
        )

        SummaryListRowViewModel(
          key = "loanDetails.CYA.fundValueLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", LoanDetailsPage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("loanDetails.change.hidden"))
          )
        )
    }
}

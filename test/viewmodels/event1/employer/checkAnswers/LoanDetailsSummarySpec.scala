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

import base.SpecBase
import data.SampleData.loanDetails
import models.UserAnswers
import models.enumeration.EventType.Event1
import pages.event1.employer.LoanDetailsPage
import pages.{CheckAnswersPage, CheckYourAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class LoanDetailsSummarySpec extends SpecBase with SummaryListFluency {

  private def loanDetailsAnswer(loanAmount: Option[BigDecimal])(implicit messages: Messages): Html = {
    def loanDetailsToHtml(amountVal: BigDecimal): String = if (amountVal.isWhole()) {
      s"£$amountVal.00"
    } else {
      s"£${amountVal}"
    }

    def optionalAmountToHtml(optionalAmount: Option[BigDecimal]): String = optionalAmount match {
      case Some(amount) => loanDetailsToHtml(amount)
      case None => ""
    }

    Html(
      optionalAmountToHtml(loanAmount)
    )
  }

  "rowLoanAmount" - {

    "must display correct information for loan amount" in {

      val answer = UserAnswers().setOrException(LoanDetailsPage, loanDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          loanDetailsAnswer(loanDetails.loanAmount)
        )
      )

      LoanDetailsSummary.rowLoanAmount(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "loanDetails.CYA.loanAmountLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", LoanDetailsPage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("loanDetails.change.hidden"))
          )
        )
      )
    }

    "must display correct information for fund value" in {

      val answer = UserAnswers().setOrException(LoanDetailsPage, loanDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          loanDetailsAnswer(loanDetails.fundValue)
        )
      )

      LoanDetailsSummary.rowFundValue(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "loanDetails.CYA.fundValueLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", LoanDetailsPage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("loanDetails.change.hidden"))
          )
        )
      )
    }
  }
}

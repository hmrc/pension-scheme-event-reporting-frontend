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

import data.SampleData.loanDetails
import models.UserAnswers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.event1.Event1CheckYourAnswersPage
import pages.event1.employer.LoanDetailsPage
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class LoanDetailsSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()

  "rowLoanAmount" - {

    "must display correct information for loan amount" in {
      val loanAmountValue = "£10.00"
      val answer = UserAnswers().setOrException(LoanDetailsPage(0), loanDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)

      val value = ValueViewModel(
        HtmlContent(
          Html(loanAmountValue)
        )
      )

      LoanDetailsSummary.rowLoanAmount(answer, waypoints, 0, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "loanDetails.CYA.loanAmountLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", LoanDetailsPage(0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("loanDetails.change.hidden"))
          )
        )
      )
    }

    "must display correct information for fund value" in {
      val fundValue = "£20.57"
      val answer = UserAnswers().setOrException(LoanDetailsPage(0), loanDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)

      val value = ValueViewModel(
        HtmlContent(
          Html(fundValue)
        )
      )

      LoanDetailsSummary.rowFundValue(answer, waypoints, 0, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "loanDetails.CYA.fundValueLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", LoanDetailsPage(0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("loanDetails.change.hidden"))
          )
        )
      )
    }
  }
}

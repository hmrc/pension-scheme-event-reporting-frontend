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
import play.twirl.api.HtmlFormat
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class LoanDetailsSummarySpec extends SpecBase with SummaryListFluency {

/*
  "rowLoanAmount" - {

    "must display correct information" in {

      val answer = UserAnswers().setOrException(LoanDetailsPage, loanDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      LoanDetailsSummary.rowLoanAmount(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "loanDetails.CYA.loanAmountLabel",
          value = ValueViewModel(HtmlFormat.escape(loanDetails.loanAmount.toString).toString),
          actions = Seq(
            ActionItemViewModel("site.change", LoanDetailsPage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("loanDetails.change.hidden"))
          )
        )
      )
    }
  }*/


  "rowFundValue" - {

    "must display correct information" in {

      val answer = UserAnswers().setOrException(LoanDetailsPage, loanDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      LoanDetailsSummary.rowLoanAmount(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "loanDetails.CYA.fundValueLabel",
          value = ValueViewModel(HtmlFormat.escape(loanDetails.fundValue.toString).toString),
          actions = Seq(
            ActionItemViewModel("site.change", LoanDetailsPage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("loanDetails.change.hidden"))
          )
        )
      )
    }
  }
}

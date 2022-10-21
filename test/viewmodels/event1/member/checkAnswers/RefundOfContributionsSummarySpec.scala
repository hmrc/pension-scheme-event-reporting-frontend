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

package viewmodels.event1.member.checkAnswers

import base.SpecBase
import models.UserAnswers
import models.enumeration.EventType.Event1
import models.event1.member.RefundOfContributions
import pages.event1.member.RefundOfContributionsPage
import pages.{CheckAnswersPage, CheckYourAnswersPage, EmptyWaypoints, Waypoints}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class RefundOfContributionsSummarySpec extends SpecBase with SummaryListFluency {


  "row" - {

    "must display correct information for refund of contributions (Widow Or Orphan)" in {

      val answer = UserAnswers().setOrException(RefundOfContributionsPage, RefundOfContributions.WidowOrOrphan)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"refundOfContributions.${RefundOfContributions.WidowOrOrphan}"))
        )
      )

      RefundOfContributionsSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "refundOfContributions.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", RefundOfContributionsPage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("refundOfContributions.change.hidden"))
          )
        )
      )
    }

    "must display correct information for refund of contributions (Other)" in {

      val answer = UserAnswers().setOrException(RefundOfContributionsPage, RefundOfContributions.Other)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"refundOfContributions.${RefundOfContributions.Other}"))
        )
      )

      RefundOfContributionsSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "refundOfContributions.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", RefundOfContributionsPage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("refundOfContributions.change.hidden"))
          )
        )
      )
    }
  }
}

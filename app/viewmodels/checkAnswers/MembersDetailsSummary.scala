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

import models.UserAnswers
import models.enumeration.EventType
import pages.common.MembersDetailsPage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object MembersDetailsSummary {

  def rowFullName(answers: UserAnswers, waypoints: Waypoints, index: Option[Int], sourcePage: CheckAnswersPage, eventType: EventType)
                 (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(MembersDetailsPage(eventType, index)).map {
      answer =>

        val value = ValueViewModel(HtmlContent(HtmlFormat.escape(messages(answer.fullName)).toString))
        SummaryListRowViewModel(
          key = "membersDetails.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", MembersDetailsPage(eventType, index).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("membersDetails.change.hidden"))
          )
        )
    }

  def rowNino(answers: UserAnswers, waypoints: Waypoints, index: Option[Int], sourcePage: CheckAnswersPage, eventType: EventType)
             (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(MembersDetailsPage(eventType, index)).map {
      answer =>

        val value = ValueViewModel(HtmlContent(HtmlFormat.escape(messages(answer.nino)).toString))
        SummaryListRowViewModel(
          key = "membersDetails.checkYourAnswersLabel.nino",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", MembersDetailsPage(eventType, index).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("membersDetails.change.nino.hidden"))
          )
        )
    }
}

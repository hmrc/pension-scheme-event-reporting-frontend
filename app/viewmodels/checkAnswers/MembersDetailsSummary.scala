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

package viewmodels.checkAnswers

import models.UserAnswers
import models.enumeration.EventType
import models.enumeration.EventType.Event2
import pages.common.MembersDetailsPage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object MembersDetailsSummary {

  def rowFullName(answers: UserAnswers, waypoints: Waypoints, index: Int, sourcePage: CheckAnswersPage, isReadOnly: Boolean,
                  eventType: EventType, memberPageNo: Int = 0)
                 (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(MembersDetailsPage(eventType, index, memberPageNo)).map {
      val detailsType = (eventType, memberPageNo) match {
        case (Event2, 1) => "deceasedMembersDetails"
        case (Event2, 2) => "beneficiaryDetails"
        case _ => "membersDetails"
      }
      answer =>
        val value = ValueViewModel(HtmlContent(HtmlFormat.escape(messages(answer.fullName)).toString))
        SummaryListRow(
          key = s"$detailsType.checkYourAnswersLabel",
          value = value,
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", MembersDetailsPage(eventType, index, memberPageNo).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages(s"$detailsType.change.hidden"))
            )))
          }
        )
    }

  def rowNino(answers: UserAnswers, waypoints: Waypoints, index: Int, sourcePage: CheckAnswersPage, isReadOnly: Boolean,
              eventType: EventType, memberPageNo: Int = 0)
             (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(MembersDetailsPage(eventType, index, memberPageNo)).map {
      val detailsType = (eventType, memberPageNo) match {
        case (Event2, 1) => "deceasedMembersDetails"
        case (Event2, 2) => "beneficiaryDetails"
        case _ => "membersDetails"
      }
      answer =>

        val value = ValueViewModel(HtmlContent(HtmlFormat.escape(messages(answer.nino)).toString))
        SummaryListRow(
          key = s"$detailsType.checkYourAnswersLabel.nino",
          value = value,
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", MembersDetailsPage(eventType, index, memberPageNo).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages(s"$detailsType.change.nino.hidden"))
            )))
          }
        )
    }
}

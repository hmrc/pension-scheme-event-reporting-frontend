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

package viewmodels.event2.checkAnswers

import models.common.MembersDetails
import models.enumeration.EventType.Event2
import models.{Index, UserAnswers}
import pages.common.MembersDetailsPage
import pages.event2.DatePaidPage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.Event2MemberPageNumbers
import viewmodels.Message
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import java.time.format.DateTimeFormatter

object DatePaidSummary {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage, isReadOnly: Boolean, index: Index)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(DatePaidPage(index, Event2)).map {
      answer =>
        val beneficiaryName = answers.get(MembersDetailsPage(Event2, index, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY))
          .map { membersDetails => MembersDetails(membersDetails.firstName, membersDetails.lastName, membersDetails.nino).fullName }
          .getOrElse(messages("site.unknown"))
        val amountPaidHeadingMessage: String = Message("datePaid.event2.checkYourAnswersLabel", beneficiaryName)
        val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

        SummaryListRow(
          key = amountPaidHeadingMessage,
          value = ValueViewModel(answer.format(dateFormatter)),
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", DatePaidPage(index, Event2).changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("datePaid.event2.change.hidden"))
            )))
          }
        )
    }
}

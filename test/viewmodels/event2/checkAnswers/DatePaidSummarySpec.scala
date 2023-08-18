/*
 * Copyright 2023 HM Revenue & Customs
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

import models.UserAnswers
import models.common.MembersDetails
import models.enumeration.EventType.Event2
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.common.MembersDetailsPage
import pages.event2.{DatePaidPage, Event2CheckYourAnswersPage}
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DatePaidSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()
  "row date paid" - {
    "must display correct information for the date" in {
      val datePaid = LocalDate.now()
      val memberDetails: MembersDetails = MembersDetails("Joe", "Bloggs", "AA234567D")
      val answer = UserAnswers().setOrException(DatePaidPage(0, Event2), datePaid).setOrException(MembersDetailsPage(Event2, 0, 2), memberDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event2CheckYourAnswersPage(0)
      val format = DateTimeFormatter.ofPattern("dd MMMM yyyy")
      DatePaidSummary.row(answer, waypoints, sourcePage, 0) mustBe Some(
        SummaryListRowViewModel(
          key = messages("datePaid.event2.checkYourAnswersLabel", memberDetails.fullName),
          value = ValueViewModel(format.format(datePaid)),
          actions = Seq(
            ActionItemViewModel("site.change", DatePaidPage(0, Event2).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("datePaid.event2.change.hidden"))
          )
        )
      )
    }
  }
}

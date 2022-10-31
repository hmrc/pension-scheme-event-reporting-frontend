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

import data.SampleData.memberDetails
import models.UserAnswers
import models.enumeration.EventType.Event1
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.common.MembersDetailsPage
import pages.{CheckAnswersPage, CheckYourAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.checkAnswers.MembersDetailsSummary
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class MembersDetailsSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()


  "rowFullName" - {

    "must display correct information" in {

      val answers = UserAnswers().setOrException(MembersDetailsPage(Event1), memberDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)
      val value = ValueViewModel(HtmlContent(HtmlFormat.escape(messages(memberDetails.fullName)).toString))

      MembersDetailsSummary.rowFullName(answers, waypoints, sourcePage, Event1) mustBe Some(
        SummaryListRowViewModel(
          key = "membersDetails.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", MembersDetailsPage(Event1).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("membersDetails.change.hidden"))
          )
        )
      )
    }
  }

  "rowNino" - {

    "must display correct information" in {

      val answers = UserAnswers().setOrException(MembersDetailsPage(Event1), memberDetails)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)
      val value = ValueViewModel(HtmlContent(HtmlFormat.escape(messages(memberDetails.nino)).toString))

      MembersDetailsSummary.rowNino(answers, waypoints, sourcePage, Event1) mustBe Some(
        SummaryListRowViewModel(
          key = "membersDetails.checkYourAnswersLabel.nino",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", MembersDetailsPage(Event1).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("membersDetails.change.hidden"))
          )
        )
      )
    }
  }
}

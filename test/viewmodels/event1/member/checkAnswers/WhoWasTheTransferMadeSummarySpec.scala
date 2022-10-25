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

import models.UserAnswers
import models.enumeration.EventType.Event1
import models.event1.member.WhoWasTheTransferMade
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.event1.member.WhoWasTheTransferMadePage
import pages.{CheckAnswersPage, CheckYourAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._

class WhoWasTheTransferMadeSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()

  "row" - {

    "must display correct information for An employer-financed retirement benefit scheme (EFRBS) option" in {

      val answer = UserAnswers().setOrException(WhoWasTheTransferMadePage, WhoWasTheTransferMade.AnEmployerFinanced)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"whoWasTheTransferMade.${WhoWasTheTransferMade.AnEmployerFinanced}"))
        )
      )

      WhoWasTheTransferMadeSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "whoWasTheTransferMade.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", WhoWasTheTransferMadePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("whoWasTheTransferMade.change.hidden"))
          )
        )
      )
    }

    "must display correct information for A non-recognised pension scheme which is not a qualifying overseas pension scheme option" in {

      val answer = UserAnswers().setOrException(WhoWasTheTransferMadePage, WhoWasTheTransferMade.NonRecognisedScheme)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"whoWasTheTransferMade.${WhoWasTheTransferMade.NonRecognisedScheme}"))
        )
      )

      WhoWasTheTransferMadeSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "whoWasTheTransferMade.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", WhoWasTheTransferMadePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("whoWasTheTransferMade.change.hidden"))
          )
        )
      )
    }

    "must display correct information for Other option" in {

      val answer = UserAnswers().setOrException(WhoWasTheTransferMadePage, WhoWasTheTransferMade.Other)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"whoWasTheTransferMade.${WhoWasTheTransferMade.Other}"))
        )
      )

      WhoWasTheTransferMadeSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "whoWasTheTransferMade.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", WhoWasTheTransferMadePage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("whoWasTheTransferMade.change.hidden"))
          )
        )
      )
    }
  }
}

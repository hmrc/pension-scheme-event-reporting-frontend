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

package viewmodels.event1.member.checkAnswers

import models.UserAnswers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.event1.Event1CheckYourAnswersPage
import pages.event1.member.UnauthorisedPaymentRecipientNamePage
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, SummaryListRow}
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class MemberUnauthorisedPaymentRecipientNameSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()

  "row" - {

    "must display correct information for unauthorised payment recipient name" in {

      val name = "Harry"
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)
      val answer = UserAnswers().setOrException(UnauthorisedPaymentRecipientNamePage(0), name)
      val isReadOnly = false

      answer match {
        case memberJourney if answer.toString.contains("""whoReceivedUnauthPayment":"member""") =>
          MemberUnauthorisedPaymentRecipientNameSummary.row(answer, waypoints, 0, sourcePage, isReadOnly) mustBe Some(
            SummaryListRow(
              key = "unauthorisedPaymentRecipientName.member.checkYourAnswersLabel",
              value = ValueViewModel(HtmlFormat.escape(name).toString),
              actions = if (isReadOnly) None else {
                Some(Actions(items = Seq(
                  ActionItemViewModel("site.change", UnauthorisedPaymentRecipientNamePage(0).changeLink(waypoints, sourcePage).url)
                    .withVisuallyHiddenText(messages("unauthorisedPaymentRecipientName.member.change.hidden"))
                )))
              }
            )
          )
        case _ => None
      }
    }
  }
}

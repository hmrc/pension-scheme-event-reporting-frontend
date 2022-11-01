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

import models.UserAnswers
import models.enumeration.EventType.Event1
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.event1.employer.UnauthorisedPaymentRecipientNamePage
import pages.{CheckAnswersPage, CheckYourAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class EmployerUnauthorisedPaymentRecipientNameSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()

  "row" - {

    "must display correct information for unauthorised payment recipient name" in {

      val name = "Martin"
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1, Some(0))
      val answer = UserAnswers().setOrException(UnauthorisedPaymentRecipientNamePage(0), name)

      answer match {
        case employerJourney if answer.toString.contains("""whoReceivedUnauthPayment":"employer""") =>
          EmployerUnauthorisedPaymentRecipientNameSummary.row(answer, waypoints, 0, sourcePage) mustBe Some(
            SummaryListRowViewModel(
              key = "unauthorisedPaymentRecipientName.employer.checkYourAnswersLabel",
              value = ValueViewModel(HtmlFormat.escape(name).toString),
              actions = Seq(
                ActionItemViewModel("site.change", UnauthorisedPaymentRecipientNamePage(0).changeLink(waypoints, sourcePage).url)
                  .withVisuallyHiddenText(messages("unauthorisedPaymentRecipientName.employer.change.hidden"))
              )
            )
          )
        case _ => None
      }
    }
  }
}

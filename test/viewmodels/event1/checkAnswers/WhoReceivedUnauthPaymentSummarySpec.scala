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

package viewmodels.event1.checkAnswers

import base.SpecBase
import models.UserAnswers
import models.enumeration.EventType.Event1
import models.event1.WhoReceivedUnauthPayment
import pages.event1.WhoReceivedUnauthPaymentPage
import pages.{CheckAnswersPage, CheckYourAnswersPage, EmptyWaypoints, Waypoints}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class WhoReceivedUnauthPaymentSummarySpec extends SpecBase with SummaryListFluency {


  "row" - {

    "must display correct information for who received the unauthorised payment (Member)" in {

      val answer = UserAnswers().setOrException(WhoReceivedUnauthPaymentPage, WhoReceivedUnauthPayment.Member)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"whoReceivedUnauthPayment.${WhoReceivedUnauthPayment.Member}"))
        )
      )

      WhoReceivedUnauthPaymentSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "whoReceivedUnauthPayment.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", WhoReceivedUnauthPaymentPage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("whoReceivedUnauthPayment.change.hidden"))
          )
        )
      )
    }

    "must display correct information for who received the unauthorised payment (Employer)" in {

      val answer = UserAnswers().setOrException(WhoReceivedUnauthPaymentPage, WhoReceivedUnauthPayment.Employer)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      val value = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"whoReceivedUnauthPayment.${WhoReceivedUnauthPayment.Employer}"))
        )
      )

      WhoReceivedUnauthPaymentSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "whoReceivedUnauthPayment.checkYourAnswersLabel",
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", WhoReceivedUnauthPaymentPage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("whoReceivedUnauthPayment.change.hidden"))
          )
        )
      )
    }
  }
}

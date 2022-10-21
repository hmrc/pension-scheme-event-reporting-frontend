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
import pages.event1.member.ErrorDescriptionPage
import pages.{CheckAnswersPage, CheckYourAnswersPage, EmptyWaypoints, Waypoints}
import play.twirl.api.HtmlFormat
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class ErrorDescriptionSummarySpec extends SpecBase with SummaryListFluency {


  "row" - {

    "must display correct information for the error description" in {

      val answer = UserAnswers().setOrException(ErrorDescriptionPage, "brief description of the error")
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      ErrorDescriptionSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "errorDescription.checkYourAnswersLabel",
          value = ValueViewModel(HtmlFormat.escape("brief description of the error").toString),
          actions = Seq(
            ActionItemViewModel("site.change", ErrorDescriptionPage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("errorDescription.change.hidden"))
          )
        )
      )
    }
  }
}

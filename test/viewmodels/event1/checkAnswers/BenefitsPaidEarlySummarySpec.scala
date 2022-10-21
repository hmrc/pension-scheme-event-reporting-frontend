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
import pages.event1.member.BenefitsPaidEarlyPage
import pages.{CheckAnswersPage, CheckYourAnswersPage, EmptyWaypoints, Waypoints}
import play.twirl.api.HtmlFormat
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class BenefitsPaidEarlySummarySpec extends SpecBase with SummaryListFluency {

  "row" - {

    "must display correct information for benefits paid early" in {

      val answer = UserAnswers().setOrException(BenefitsPaidEarlyPage, "brief description of why the benefits are being paid early")
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      BenefitsPaidEarlySummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "benefitsPaidEarly.checkYourAnswersLabel",
          value = ValueViewModel(HtmlFormat.escape("brief description of why the benefits are being paid early").toString),
          actions = Seq(
            ActionItemViewModel("site.change", BenefitsPaidEarlyPage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("benefitsPaidEarly.change.hidden"))
          )
        )
      )
    }
  }
}

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
import data.SampleData.booleanCYAVal
import models.UserAnswers
import models.enumeration.EventType.Event1
import pages.event1.{ValueOfUnauthorisedPaymentPage, MembersDetailsPage}
import pages.{CheckAnswersPage, CheckYourAnswersPage, EmptyWaypoints, Waypoints}
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class ValueOfUnauthorisedPaymentSummarySpec extends SpecBase with SummaryListFluency {


  "row" - {

    "should display correct information if value is true" in {

      val answer = UserAnswers().setOrException(ValueOfUnauthorisedPaymentPage, true)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      ValueOfUnauthorisedPaymentSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "valueOfUnauthorisedPayment.checkYourAnswersLabel",
          value = ValueViewModel(booleanCYAVal(true)),
          actions = Seq(
            ActionItemViewModel("site.change", ValueOfUnauthorisedPaymentPage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("valueOfUnauthorisedPayment.change.hidden"))
          )
        )
      )
    }

    "should display correct information if value is false" in {

      val answer = UserAnswers().setOrException(ValueOfUnauthorisedPaymentPage, false)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = CheckYourAnswersPage(Event1)

      ValueOfUnauthorisedPaymentSummary.row(answer, waypoints, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "valueOfUnauthorisedPayment.checkYourAnswersLabel",
          value = ValueViewModel(booleanCYAVal(false)),
          actions = Seq(
            ActionItemViewModel("site.change", ValueOfUnauthorisedPaymentPage.changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("valueOfUnauthorisedPayment.change.hidden"))
          )
        )
      )
    }
  }
}

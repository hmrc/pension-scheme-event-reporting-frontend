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

package viewmodels.event1.checkAnswers

import data.SampleData.booleanCYAVal
import models.UserAnswers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.event1.{DoYouHoldSignedMandatePage, Event1CheckYourAnswersPage}
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class DoYouHoldSignedMandateSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()

  "row" - {

    "must redirect to the CYA page when true" in {

      val answer = UserAnswers().setOrException(DoYouHoldSignedMandatePage(0), true)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)

      DoYouHoldSignedMandateSummary.row(answer, waypoints, 0, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "doYouHoldSignedMandate.checkYourAnswersLabel",
          value = ValueViewModel(booleanCYAVal(true)),
          actions = Seq(
            ActionItemViewModel("site.change", DoYouHoldSignedMandatePage(0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("doYouHoldSignedMandate.change.hidden"))
          )
        )
      )
    }

    "must redirect to the CYA page when false" in {

      val answer = UserAnswers().setOrException(DoYouHoldSignedMandatePage(0), false)
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event1CheckYourAnswersPage(0)

      DoYouHoldSignedMandateSummary.row(answer, waypoints, 0, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "doYouHoldSignedMandate.checkYourAnswersLabel",
          value = ValueViewModel(booleanCYAVal(false)),
          actions = Seq(
            ActionItemViewModel("site.change", DoYouHoldSignedMandatePage(0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("doYouHoldSignedMandate.change.hidden"))
          )
        )
      )
    }
  }
}

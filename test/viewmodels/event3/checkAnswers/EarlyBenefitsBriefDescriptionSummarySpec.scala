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

package viewmodels.event3.checkAnswers

import models.UserAnswers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.event3.{EarlyBenefitsBriefDescriptionPage, Event3CheckYourAnswersPage}
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._

class EarlyBenefitsBriefDescriptionSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {
  private implicit val messages: Messages = stubMessages()
  "row" - {
    "must display correct information" in {
      val answer = UserAnswers().setOrException(EarlyBenefitsBriefDescriptionPage(0), "early benefit brief description")
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event3CheckYourAnswersPage(0)
      EarlyBenefitsBriefDescriptionSummary.row(answer, waypoints, 0, sourcePage) mustBe Some(
        SummaryListRowViewModel(
          key = "earlyBenefitsBriefDescription.checkYourAnswersLabel",
          value = ValueViewModel(HtmlFormat.escape("early benefit brief description").toString),
          actions = Seq(
            ActionItemViewModel("site.change", EarlyBenefitsBriefDescriptionPage(0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("earlyBenefitsBriefDescription.change.hidden"))
          )
        )
      )
    }
  }
}
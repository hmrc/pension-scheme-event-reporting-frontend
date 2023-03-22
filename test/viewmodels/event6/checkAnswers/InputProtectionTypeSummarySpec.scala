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

package viewmodels.event6.checkAnswers

import models.UserAnswers
import models.enumeration.EventType.Event6
import models.event6.TypeOfProtection
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.event6.{Event6CheckYourAnswersPage, InputProtectionTypePage}
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import viewmodels.govuk.SummaryListFluency
import viewmodels.implicits._


class InputProtectionTypeSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues with SummaryListFluency {

  private implicit val messages: Messages = stubMessages()
  private val typeOfProtectionSeq: Seq[TypeOfProtection] = TypeOfProtection.values

  "row" - {
    for (typeOfProtection <- typeOfProtectionSeq) testTypeOfProtectionSummaryRow(typeOfProtection)
  }

  private def testTypeOfProtectionSummaryRow(typeOfProtection: TypeOfProtection): Unit = {
    s"must display correct information for $typeOfProtection" in {

      val answers = UserAnswers().setOrException(InputProtectionTypePage(Event6, 0), "1234567A")
      val waypoints: Waypoints = EmptyWaypoints
      val sourcePage: CheckAnswersPage = Event6CheckYourAnswersPage(0)
      val value = ValueViewModel("1234567A")

      InputProtectionTypeSummary.row(answers, waypoints, sourcePage, Event6, 0) mustBe Some(
        SummaryListRowViewModel(
          key = messages(s"inputProtectionType.checkYourAnswersLabel", messages(s"typeOfProtection.${typeOfProtection.toString}")),
          value = value,
          actions = Seq(
            ActionItemViewModel("site.change", InputProtectionTypePage(Event6, 0).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("site.change") + " " + messages("inputProtectionType.change.hidden",
                messages(s"typeOfProtection.${typeOfProtection.toString}").toLowerCase()))
          )
        )
      )
    }
  }
}

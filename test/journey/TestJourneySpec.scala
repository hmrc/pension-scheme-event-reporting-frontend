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

package journey

import models.EventSelection.EventWoundUp
import org.scalatest.freespec.AnyFreeSpec
import pages.event18.Event18ConfirmationPage
import pages.eventWindUp.SchemeWindUpDatePage
import pages.{CheckYourAnswersPage, EventSelectionPage}

import java.time.LocalDate

class TestJourneySpec extends AnyFreeSpec with JourneyHelpers {
  
  "test journey" in {

    startingFrom(Event18ConfirmationPage)
      .run(
        submitAnswer(Event18ConfirmationPage, true),
        pageMustBe(CheckYourAnswersPage.event18)
      )
  }
  "test windUp journey" in {

    startingFrom(EventSelectionPage)
      .run(
        submitAnswer(EventSelectionPage, EventWoundUp),
        pageMustBe(SchemeWindUpDatePage),
        submitAnswer(SchemeWindUpDatePage, LocalDate.of(2021, 5, 4)),
        pageMustBe(pages.CheckYourAnswersPage.windUp)
      )
  }
}

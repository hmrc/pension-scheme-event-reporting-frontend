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

package pages

import models.Index
import models.enumeration.EventType.Event1
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class WaypointSpec extends AnyFreeSpec with Matchers with OptionValues {

  ".fromString" - {

    "must return CheckYourAnswers for event 18 when given its waypoint" in {

      Waypoint.fromString("event-18-check-answers").value mustEqual CheckYourAnswersPage.event18.waypoint
    }

    "must return CheckYourAnswers for event 1 when given its waypoint, including index of up to 5 digits" in {

      Waypoint.fromString("event-1-check-answers-99999").value mustEqual CheckYourAnswersPage(Event1, Some(Index(99999))).waypoint
    }

    "must return CheckYourAnswers for event wind up when given its waypoint" in {

      Waypoint.fromString("event-0-check-answers").value mustEqual CheckYourAnswersPage.windUp.waypoint
    }
  }
}

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

package pages.event25

import models.event25.CrystallisedDate
import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class CrystallisedDatePageSpec extends PageBehaviours {

  "CrystallisedDatePage" - {

    implicit lazy val arbitraryCrystallisationDatePage: Arbitrary[CrystallisedDate] = Arbitrary {
      CrystallisedDate(LocalDate.of(2023, 5, 3))
    }

    beRetrievable[CrystallisedDate](CrystallisedDatePage(0))

    beSettable[CrystallisedDate](CrystallisedDatePage(0))

    beRemovable[CrystallisedDate](CrystallisedDatePage(0))
  }
}

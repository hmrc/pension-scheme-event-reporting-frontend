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

package pages.event20

import models.event20.Event20Date

import java.time.LocalDate
import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

class BecameDatePageSpec extends PageBehaviours {

  "BecameDatePage" - {

    implicit lazy val arbitraryEvent20Date: Arbitrary[Event20Date] = Arbitrary {
      for {
        date <- datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
      } yield Event20Date(date)
    }

    beRetrievable[Event20Date](BecameDatePage)

    beSettable[Event20Date](BecameDatePage)

    beRemovable[Event20Date](BecameDatePage)
  }
}

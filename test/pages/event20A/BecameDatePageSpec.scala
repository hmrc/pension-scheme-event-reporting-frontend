/*
 * Copyright 2024 HM Revenue & Customs
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

package pages.event20A


import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class BecameDatePageSpec extends PageBehaviours {

  "BecameDatePage" - {

    implicit lazy val arbitraryEvent20Date: Arbitrary[LocalDate] = Arbitrary {
      for {
        date <- datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
      } yield date
    }

    beRetrievable[LocalDate](BecameDatePage)

    beSettable[LocalDate](BecameDatePage)

    beRemovable[LocalDate](BecameDatePage)
  }
}

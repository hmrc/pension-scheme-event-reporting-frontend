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

package pages.event6

import models.event6.CrystallisedDetails
import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class AmountCrystallisedAndDatePageSpec extends PageBehaviours {

  "AmountCrystallisedAndDatePage" - {

    implicit lazy val arbitraryAmountCrystallisedAndDate: Arbitrary[CrystallisedDetails] = Arbitrary {
      CrystallisedDetails(1000.00, LocalDate.now())
    }

    beRetrievable[CrystallisedDetails](AmountCrystallisedAndDatePage(0))

    beSettable[CrystallisedDetails](AmountCrystallisedAndDatePage(0))

    beRemovable[CrystallisedDetails](AmountCrystallisedAndDatePage(0))
  }
}

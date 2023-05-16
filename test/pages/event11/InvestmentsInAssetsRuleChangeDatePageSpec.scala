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

package pages.event11

import models.event11.Event11Date

import java.time.LocalDate
import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

class InvestmentsInAssetsRuleChangeDatePageSpec extends PageBehaviours {

  "InvestmentsInAssetsRuleChangeDatePage" - {

    implicit lazy val arbitraryEvent11Date: Arbitrary[Event11Date] = Arbitrary {
      for {
        date <- datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
      } yield Event11Date(date)
    }

    beRetrievable[Event11Date](InvestmentsInAssetsRuleChangeDatePage)

    beSettable[Event11Date](InvestmentsInAssetsRuleChangeDatePage)

    beRemovable[Event11Date](InvestmentsInAssetsRuleChangeDatePage)
  }
}

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

package pages.common

import models.common.ChooseTaxYear
import models.enumeration.EventType.{Event22, Event23}
import pages.behaviours.PageBehaviours
import pages.common

class ChooseTaxYearSpec extends PageBehaviours {

  "ChooseTaxYearPage" - {
    "event23" - {
      beRetrievable[ChooseTaxYear](ChooseTaxYearPage(Event23))

      beSettable[ChooseTaxYear](common.ChooseTaxYearPage(Event23))

      beRemovable[ChooseTaxYear](common.ChooseTaxYearPage(Event23))
    }
    "event22" - {
      beRetrievable[ChooseTaxYear](ChooseTaxYearPage(Event22))

      beSettable[ChooseTaxYear](common.ChooseTaxYearPage(Event22))

      beRemovable[ChooseTaxYear](common.ChooseTaxYearPage(Event22))
    }

  }
}

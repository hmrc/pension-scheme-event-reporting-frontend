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

package helpers

import base.SpecBase

import java.time.LocalDate

class DateHelperSpec extends SpecBase {

  "Date helper" - {
    "must return OK and the correct view for a GET" in {
      DateHelper.formatDateDMYWithSlash(LocalDate.of(2022,8,2)) mustBe "02/08/2022"
    }
  }
}

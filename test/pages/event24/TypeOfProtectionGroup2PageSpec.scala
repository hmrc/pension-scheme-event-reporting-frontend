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

package pages.event24

import models.event24.TypeOfProtectionGroup2
import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

class TypeOfProtectionGroup2PageSpec extends PageBehaviours {

  "TypeOfProtectionGroup2Page" - {

    implicit lazy val arbitraryTypeOfProtectionPage: Arbitrary[TypeOfProtectionGroup2] = Arbitrary {
      TypeOfProtectionGroup2.FixedProtection
    }

    beRetrievable[TypeOfProtectionGroup2](TypeOfProtectionGroup2Page(0))

    beSettable[TypeOfProtectionGroup2](TypeOfProtectionGroup2Page(0))

    beRemovable[TypeOfProtectionGroup2](TypeOfProtectionGroup2Page(0))
  }
}

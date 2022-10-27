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

package pages.event23

import models.event23.HowAddDualAllowance
import pages.behaviours.PageBehaviours

class HowAddDualAllowanceSpec extends PageBehaviours {

  "HowAddDualAllowancePage" - {

    beRetrievable[HowAddDualAllowance](HowAddDualAllowancePage)

<<<<<<< HEAD:test/pages/event1/MembersDetailsPageSpec.scala
    beRetrievable[MembersDetails](MembersDetailsPage(0))

    beSettable[MembersDetails](MembersDetailsPage(0))

    beRemovable[MembersDetails](MembersDetailsPage(0))
=======
    beSettable[HowAddDualAllowance](HowAddDualAllowancePage)

    beRemovable[HowAddDualAllowance](HowAddDualAllowancePage)
>>>>>>> c82eb80c87950d8ee4d4965fce60d2ec0337ed20:test/pages/event23/HowAddDualAllowanceSpec.scala
  }
}

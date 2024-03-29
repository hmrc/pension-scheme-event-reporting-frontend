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

package pages.common

import models.common.MembersDetails
import models.enumeration.EventType.{Event1, Event3, Event6, Event7, Event8, Event8A}
import pages.behaviours.PageBehaviours

class MembersDetailsPageSpec extends PageBehaviours {

  "MembersDetailsPage" - {

    "event1" - {
      beRetrievable[MembersDetails](MembersDetailsPage(Event1, 0))

      beSettable[MembersDetails](MembersDetailsPage(Event1, 0))

      beRemovable[MembersDetails](MembersDetailsPage(Event1, 0))
    }

    "event3" - {
      beRetrievable[MembersDetails](MembersDetailsPage(Event3, 0))

      beSettable[MembersDetails](MembersDetailsPage(Event3, 0))

      beRemovable[MembersDetails](MembersDetailsPage(Event3, 0))
    }

    "event6" - {
      beRetrievable[MembersDetails](MembersDetailsPage(Event6, 0))

      beSettable[MembersDetails](MembersDetailsPage(Event6, 0))

      beRemovable[MembersDetails](MembersDetailsPage(Event6, 0))
    }

    "event7" - {
      beRetrievable[MembersDetails](MembersDetailsPage(Event7, 0))

      beSettable[MembersDetails](MembersDetailsPage(Event7, 0))

      beRemovable[MembersDetails](MembersDetailsPage(Event7, 0))
    }


    "event8" - {
      beRetrievable[MembersDetails](MembersDetailsPage(Event8, 0))

      beSettable[MembersDetails](MembersDetailsPage(Event8, 0))

      beRemovable[MembersDetails](MembersDetailsPage(Event8, 0))
    }

    "event8A" - {
      beRetrievable[MembersDetails](MembersDetailsPage(Event8A, 0))

      beSettable[MembersDetails](MembersDetailsPage(Event8A, 0))

      beRemovable[MembersDetails](MembersDetailsPage(Event8A, 0))
    }
  }
}

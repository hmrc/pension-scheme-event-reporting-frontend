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

package pages.event8a

import models.enumeration.EventType.Event8A
import models.event8a.PaymentType
import pages.behaviours.PageBehaviours

class PaymentTypeSpec extends PageBehaviours {

  "PaymentTypePage" - {

    beRetrievable[PaymentType](PaymentTypePage(Event8A, 0))

    beSettable[PaymentType](PaymentTypePage(Event8A, 0))

    beRemovable[PaymentType](PaymentTypePage(Event8A, 0))
  }
}

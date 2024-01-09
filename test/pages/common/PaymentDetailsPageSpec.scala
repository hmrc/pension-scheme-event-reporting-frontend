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

import models.common.PaymentDetails
import models.enumeration.EventType.{Event3, Event4, Event5}
import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class PaymentDetailsPageSpec extends PageBehaviours {

  "PaymentDetailsPage" - {
    implicit lazy val arbitraryPaymentDetailsEvent3: Arbitrary[PaymentDetails] = Arbitrary {
      PaymentDetails(1000.00, LocalDate.now())
    }

    "event3" - {

      beRetrievable[PaymentDetails](PaymentDetailsPage(Event3, 0))

      beSettable[PaymentDetails](PaymentDetailsPage(Event3, 0))

      beRemovable[PaymentDetails](PaymentDetailsPage(Event3, 0))
    }

    "event4" - {

      beRetrievable[PaymentDetails](PaymentDetailsPage(Event4, 0))

      beSettable[PaymentDetails](PaymentDetailsPage(Event4, 0))

      beRemovable[PaymentDetails](PaymentDetailsPage(Event4, 0))
    }

    "event5" - {

      beRetrievable[PaymentDetails](PaymentDetailsPage(Event5, 0))

      beSettable[PaymentDetails](PaymentDetailsPage(Event5, 0))

      beRemovable[PaymentDetails](PaymentDetailsPage(Event5, 0))
    }


  }
}

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

package pages.event1

import models.event1.PaymentDetails
import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class PaymentValueAndDatePageSpec extends PageBehaviours {

  "PaymentValueAndDatePage" - {

    implicit lazy val arbitraryPaymentValueAndDate: Arbitrary[PaymentDetails] = Arbitrary {
        PaymentDetails(1000.00, LocalDate.now())
    }

    beRetrievable[PaymentDetails](PaymentValueAndDatePage)

    beSettable[PaymentDetails](PaymentValueAndDatePage)

    beRemovable[PaymentDetails](PaymentValueAndDatePage)
  }
}

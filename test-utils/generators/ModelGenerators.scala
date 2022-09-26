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

package generators

import models._
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryWhoReceivedUnauthPayment: Arbitrary[event1.WhoReceivedUnauthPayment] =
    Arbitrary {
      Gen.oneOf(event1.WhoReceivedUnauthPayment.values.toSeq)
    }

  implicit lazy val arbitraryHowAddUnauthPayment: Arbitrary[event1.HowAddUnauthPayment] =
    Arbitrary {
      Gen.oneOf(event1.HowAddUnauthPayment.values.toSeq)

  implicit lazy val arbitraryPaymentNature: Arbitrary[event1.PaymentNature] =
    Arbitrary {
      Gen.oneOf(event1.PaymentNature.values.toSeq)
    }

  implicit lazy val arbitraryeventSelection: Arbitrary[EventSelection] =
    Arbitrary {
      Gen.oneOf(EventSelection.values.toSeq)
    }

  implicit lazy val arbitraryTestRadioButton: Arbitrary[TestRadioButton] =
    Arbitrary {
      Gen.oneOf(TestRadioButton.values.toSeq)
    }

  implicit lazy val arbitraryTestCheckBox: Arbitrary[TestCheckBox] =
    Arbitrary {
      Gen.oneOf(TestCheckBox.values)
    }
}

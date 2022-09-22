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
import models.event1.MembersDetails
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryWhoReceivedUnauthPayment: Arbitrary[event1.WhoReceivedUnauthPayment] =
    Arbitrary {
      Gen.oneOf(event1.WhoReceivedUnauthPayment.values)
    }

  implicit lazy val arbitraryMembersDetails: Arbitrary[MembersDetails] =
    Arbitrary {
      val list = for {
        firstName <- Seq("validFirstName1", "validFirstName2")
        lastName <- Seq("validLastName1", "validLastName2")
        nino <- Seq("AA123456D", "AA123457D")
      } yield MembersDetails(firstName, lastName, nino)

      Gen.oneOf(list)
    }

  implicit lazy val arbitraryHowAddUnauthPayment: Arbitrary[event1.HowAddUnauthPayment] =
    Arbitrary {
      Gen.oneOf(event1.HowAddUnauthPayment.values)
    }

  implicit lazy val arbitraryeventSelection: Arbitrary[EventSelection] =
    Arbitrary {
      Gen.oneOf(EventSelection.values)
    }

  implicit lazy val arbitraryTestRadioButton: Arbitrary[TestRadioButton] =
    Arbitrary {
      Gen.oneOf(TestRadioButton.values)
    }

  implicit lazy val arbitraryTestCheckBox: Arbitrary[TestCheckBox] =
    Arbitrary {
      Gen.oneOf(TestCheckBox.values)
    }
}

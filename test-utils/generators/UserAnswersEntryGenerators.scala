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
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import pages.eventWindUp.SchemeWindUpDatePage
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryValueOfUnauthorisedPaymentUserAnswersEntry: Arbitrary[(pages.event1.ValueOfUnauthorisedPaymentPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.event1.ValueOfUnauthorisedPaymentPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDoYouHoldSignedMandateUserAnswersEntry: Arbitrary[(pages.event1.DoYouHoldSignedMandatePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.event1.DoYouHoldSignedMandatePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryMembersDetailsUserAnswersEntry: Arbitrary[(pages.event1.MembersDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.event1.MembersDetailsPage.type]
        value <- arbitrary[models.event1.MembersDetails].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhoReceivedUnauthPaymentUserAnswersEntry: Arbitrary[(pages.event1.WhoReceivedUnauthPaymentPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.event1.WhoReceivedUnauthPaymentPage.type]
        value <- arbitrary[models.event1.WhoReceivedUnauthPayment].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHowAddUnauthPaymentUserAnswersEntry: Arbitrary[(pages.event1.HowAddUnauthPaymentPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.event1.HowAddUnauthPaymentPage.type]
        value <- arbitrary[models.event1.HowAddUnauthPayment].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySchemeWindUpDateUserAnswersEntry: Arbitrary[(SchemeWindUpDatePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SchemeWindUpDatePage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEvent18ConfirmationUserAnswersEntry: Arbitrary[(pages.event18.Event18ConfirmationPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[pages.event18.Event18ConfirmationPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEventSummaryUserAnswersEntry: Arbitrary[(EventSummaryPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EventSummaryPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryeventSelectionUserAnswersEntry: Arbitrary[(EventSelectionPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EventSelectionPage.type]
        value <- arbitrary[EventSelection].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTestIntPageUserAnswersEntry: Arbitrary[(TestIntPagePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TestIntPagePage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTestStringPageUserAnswersEntry: Arbitrary[(TestStringPagePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TestStringPagePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTestRadioButtonUserAnswersEntry: Arbitrary[(TestRadioButtonPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TestRadioButtonPage.type]
        value <- arbitrary[TestRadioButton].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTestCheckBoxUserAnswersEntry: Arbitrary[(TestCheckBoxPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TestCheckBoxPage.type]
        value <- arbitrary[TestCheckBox].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDateUserAnswersEntry: Arbitrary[(TestDatePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TestDatePage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }
}

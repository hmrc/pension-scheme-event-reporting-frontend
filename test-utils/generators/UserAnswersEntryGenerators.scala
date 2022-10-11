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
import models.event1.member.{ReasonForTheOverpaymentOrWriteOff, RefundOfContributions, WhoWasTheTransferMade}
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import pages.event1.employer.{EmployerPaymentNatureDescriptionPage, EmployerTangibleMoveablePropertyPage, UnauthorisedPaymentRecipientNamePage}
import pages.event1.member._
import pages.eventWindUp.SchemeWindUpDatePage
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryLoanDetailsUserAnswersEntry: Arbitrary[(pages.event1.employer.LoanDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.employer.LoanDetailsPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryMemberUnauthorisedPaymentRecipientNameUserAnswersEntry: Arbitrary[(pages.event1.member.UnauthorisedPaymentRecipientNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.member.UnauthorisedPaymentRecipientNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEmployerPaymentNatureDescriptionUserAnswersEntry: Arbitrary[(EmployerPaymentNatureDescriptionPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[EmployerPaymentNatureDescriptionPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryMemberPaymentNatureDescriptionUserAnswersEntry: Arbitrary[(MemberPaymentNatureDescriptionPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[MemberPaymentNatureDescriptionPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEmployerTangibleMoveablePropertyUserAnswersEntry: Arbitrary[(EmployerTangibleMoveablePropertyPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[EmployerTangibleMoveablePropertyPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryMemberTangibleMoveablePropertyUserAnswersEntry: Arbitrary[(MemberTangibleMoveablePropertyPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[MemberTangibleMoveablePropertyPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryUnauthorisedPaymentRecipientNameUserAnswersEntry: Arbitrary[(UnauthorisedPaymentRecipientNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[UnauthorisedPaymentRecipientNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryReasonForTheOverpaymentOrWriteOffUserAnswersEntry: Arbitrary[(ReasonForTheOverpaymentOrWriteOffPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[ReasonForTheOverpaymentOrWriteOffPage.type]
        value <- arbitrary[ReasonForTheOverpaymentOrWriteOff].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryRefundOfContributionsUserAnswersEntry: Arbitrary[(RefundOfContributionsPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[RefundOfContributionsPage.type]
        value <- arbitrary[RefundOfContributions].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySchemeDetailsUserAnswersEntry: Arbitrary[(SchemeDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.member.SchemeDetailsPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhoWasTheTransferMadeUserAnswersEntry: Arbitrary[(WhoWasTheTransferMadePage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.member.WhoWasTheTransferMadePage.type]
        value <- arbitrary[WhoWasTheTransferMade].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryErrorDescriptionUserAnswersEntry: Arbitrary[(pages.event1.member.ErrorDescriptionPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.member.ErrorDescriptionPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBenefitsPaidEarlyUserAnswersEntry: Arbitrary[(BenefitsPaidEarlyPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[BenefitsPaidEarlyPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEmployerPaymentNatureUserAnswersEntry: Arbitrary[(pages.event1.employer.PaymentNaturePage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.employer.PaymentNaturePage.type]
        value <- arbitrary[models.event1.employer.PaymentNature].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEnterPostcodeUserAnswersEntry: Arbitrary[(pages.address.EnterPostcodePage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.address.EnterPostcodePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCompanyDetailsUserAnswersEntry: Arbitrary[(pages.event1.employer.CompanyDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.employer.CompanyDetailsPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBenefitInKindBriefDescriptionUserAnswersEntry: Arbitrary[(pages.event1.BenefitInKindBriefDescriptionPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.BenefitInKindBriefDescriptionPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySchemeUnAuthPaySurchargeMemberUserAnswersEntry: Arbitrary[(pages.event1.SchemeUnAuthPaySurchargeMemberPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.SchemeUnAuthPaySurchargeMemberPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryValueOfUnauthorisedPaymentUserAnswersEntry: Arbitrary[(pages.event1.ValueOfUnauthorisedPaymentPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.ValueOfUnauthorisedPaymentPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDoYouHoldSignedMandateUserAnswersEntry: Arbitrary[(pages.event1.DoYouHoldSignedMandatePage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.DoYouHoldSignedMandatePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryMembersDetailsUserAnswersEntry: Arbitrary[(pages.event1.MembersDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.MembersDetailsPage.type]
        value <- arbitrary[models.event1.MembersDetails].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhoReceivedUnauthPaymentUserAnswersEntry: Arbitrary[(pages.event1.WhoReceivedUnauthPaymentPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.WhoReceivedUnauthPaymentPage.type]
        value <- arbitrary[models.event1.WhoReceivedUnauthPayment].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHowAddUnauthPaymentUserAnswersEntry: Arbitrary[(pages.event1.HowAddUnauthPaymentPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.HowAddUnauthPaymentPage.type]
        value <- arbitrary[models.event1.HowAddUnauthPayment].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPaymentNatureUserAnswersEntry: Arbitrary[(pages.event1.PaymentNaturePage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.PaymentNaturePage.type]
        value <- arbitrary[models.event1.PaymentNature].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySchemeWindUpDateUserAnswersEntry: Arbitrary[(SchemeWindUpDatePage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[SchemeWindUpDatePage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEvent18ConfirmationUserAnswersEntry: Arbitrary[(pages.event18.Event18ConfirmationPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event18.Event18ConfirmationPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEventSummaryUserAnswersEntry: Arbitrary[(EventSummaryPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[EventSummaryPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryeventSelectionUserAnswersEntry: Arbitrary[(EventSelectionPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[EventSelectionPage.type]
        value <- arbitrary[EventSelection].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTestIntPageUserAnswersEntry: Arbitrary[(TestIntPagePage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[TestIntPagePage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTestStringPageUserAnswersEntry: Arbitrary[(TestStringPagePage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[TestStringPagePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTestRadioButtonUserAnswersEntry: Arbitrary[(TestRadioButtonPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[TestRadioButtonPage.type]
        value <- arbitrary[TestRadioButton].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTestCheckBoxUserAnswersEntry: Arbitrary[(TestCheckBoxPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[TestCheckBoxPage.type]
        value <- arbitrary[TestCheckBox].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDateUserAnswersEntry: Arbitrary[(TestDatePage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[TestDatePage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }
}

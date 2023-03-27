/*
 * Copyright 2023 HM Revenue & Customs
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
import models.common.MembersDetails
import models.event1.member.{ReasonForTheOverpaymentOrWriteOff, RefundOfContributions, WhoWasTheTransferMade}
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import pages.common.MembersDetailsPage
import pages.event1.employer.{EmployerPaymentNatureDescriptionPage, EmployerTangibleMoveablePropertyPage, UnauthorisedPaymentRecipientNamePage}
import pages.event1.member._
import pages.event6._
import pages.eventWindUp.SchemeWindUpDatePage
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryEvent8aTypeOfProtectionUserAnswersEntry: Arbitrary[(pages.event8a.TypeOfProtectionPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event8a.TypeOfProtectionPage.type]
        value <- arbitrary[models.event8a.TypeOfProtection].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPaymentTypeUserAnswersEntry: Arbitrary[(pages.event8a.PaymentTypePage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event8a.PaymentTypePage.type]
        value <- arbitrary[models.event8a.PaymentType].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryLumpSumAmountAndDateUserAnswersEntry: Arbitrary[(pages.event8.LumpSumAmountAndDatePage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event8.LumpSumAmountAndDatePage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTypeOfProtectionReferenceUserAnswersEntry: Arbitrary[(pages.event8.TypeOfProtectionReferencePage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event8.TypeOfProtectionReferencePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTypeOfProtectionUserAnswersEntry: Arbitrary[(pages.event8.TypeOfProtectionPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event8.TypeOfProtectionPage.type]
        value <- arbitrary[models.event8.TypeOfProtection].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryInputProtectionTypeUserAnswersEntry: Arbitrary[(InputProtectionTypePage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[InputProtectionTypePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }


  implicit lazy val arbitraryTaxYearUserAnswersEntry: Arbitrary[(pages.TaxYearPage.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.TaxYearPage.type]
        value <- arbitrary[models.TaxYear].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryManualOrUploadUserAnswersEntry: Arbitrary[(pages.common.ManualOrUploadPage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.common.ManualOrUploadPage]
        value <- arbitrary[models.common.ManualOrUpload].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryRemoveEvent18UserAnswersEntry: Arbitrary[(pages.event18.RemoveEvent18Page.type, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event18.RemoveEvent18Page.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPaymentValueAndDateUserAnswersEntry: Arbitrary[(pages.event1.PaymentValueAndDatePage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.PaymentValueAndDatePage]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }


  implicit lazy val arbitraryLoanDetailsUserAnswersEntry: Arbitrary[(pages.event1.employer.LoanDetailsPage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.employer.LoanDetailsPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryMemberUnauthorisedPaymentRecipientNameUserAnswersEntry: Arbitrary[(pages.event1.member.UnauthorisedPaymentRecipientNamePage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.member.UnauthorisedPaymentRecipientNamePage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEmployerPaymentNatureDescriptionUserAnswersEntry: Arbitrary[(EmployerPaymentNatureDescriptionPage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[EmployerPaymentNatureDescriptionPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryMemberPaymentNatureDescriptionUserAnswersEntry: Arbitrary[(MemberPaymentNatureDescriptionPage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[MemberPaymentNatureDescriptionPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEmployerTangibleMoveablePropertyUserAnswersEntry: Arbitrary[(EmployerTangibleMoveablePropertyPage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[EmployerTangibleMoveablePropertyPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryMemberTangibleMoveablePropertyUserAnswersEntry: Arbitrary[(MemberTangibleMoveablePropertyPage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[MemberTangibleMoveablePropertyPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryUnauthorisedPaymentRecipientNameUserAnswersEntry: Arbitrary[(UnauthorisedPaymentRecipientNamePage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[UnauthorisedPaymentRecipientNamePage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryReasonForTheOverpaymentOrWriteOffUserAnswersEntry: Arbitrary[(ReasonForTheOverpaymentOrWriteOffPage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[ReasonForTheOverpaymentOrWriteOffPage]
        value <- arbitrary[ReasonForTheOverpaymentOrWriteOff].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryRefundOfContributionsUserAnswersEntry: Arbitrary[(RefundOfContributionsPage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[RefundOfContributionsPage]
        value <- arbitrary[RefundOfContributions].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySchemeDetailsUserAnswersEntry: Arbitrary[(SchemeDetailsPage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.member.SchemeDetailsPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWhoWasTheTransferMadeUserAnswersEntry: Arbitrary[(WhoWasTheTransferMadePage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.member.WhoWasTheTransferMadePage]
        value <- arbitrary[WhoWasTheTransferMade].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryErrorDescriptionUserAnswersEntry: Arbitrary[(pages.event1.member.ErrorDescriptionPage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.member.ErrorDescriptionPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBenefitsPaidEarlyUserAnswersEntry: Arbitrary[(BenefitsPaidEarlyPage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[BenefitsPaidEarlyPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEmployerPaymentNatureUserAnswersEntry: Arbitrary[(pages.event1.employer.PaymentNaturePage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.employer.PaymentNaturePage]
        value <- arbitrary[models.event1.employer.PaymentNature].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEnterPostcodeUserAnswersEntry: Arbitrary[(pages.address.EnterPostcodePage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.address.EnterPostcodePage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCompanyDetailsUserAnswersEntry: Arbitrary[(pages.event1.employer.CompanyDetailsPage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.employer.CompanyDetailsPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBenefitInKindBriefDescriptionUserAnswersEntry: Arbitrary[(BenefitInKindBriefDescriptionPage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[BenefitInKindBriefDescriptionPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySchemeUnAuthPaySurchargeMemberUserAnswersEntry: Arbitrary[(pages.event1.SchemeUnAuthPaySurchargeMemberPage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.SchemeUnAuthPaySurchargeMemberPage]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryValueOfUnauthorisedPaymentUserAnswersEntry: Arbitrary[(pages.event1.ValueOfUnauthorisedPaymentPage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.ValueOfUnauthorisedPaymentPage]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDoYouHoldSignedMandateUserAnswersEntry: Arbitrary[(pages.event1.DoYouHoldSignedMandatePage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.DoYouHoldSignedMandatePage]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryMembersDetailsUserAnswersEntry: Arbitrary[(MembersDetailsPage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[MembersDetailsPage]
        value <- arbitrary[MembersDetails].map(Json.toJson(_))
      } yield (page, value)
    }


  implicit lazy val arbitraryWhoReceivedUnauthPaymentUserAnswersEntry: Arbitrary[(pages.event1.WhoReceivedUnauthPaymentPage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[pages.event1.WhoReceivedUnauthPaymentPage]
        value <- arbitrary[models.event1.WhoReceivedUnauthPayment].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPaymentNatureUserAnswersEntry: Arbitrary[(PaymentNaturePage, JsValue)] =
    Arbitrary {
      for {
        page <- arbitrary[PaymentNaturePage]
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
}

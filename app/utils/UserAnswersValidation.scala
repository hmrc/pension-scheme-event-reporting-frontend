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

package utils

import models.TaxYear.{getTaxYear, reads}
import models.common.ChooseTaxYear
import models.enumeration.EventType._
import models.enumeration.{AddressJourneyType, EventType}
import models.event1.PaymentNature.{OverpaymentOrWriteOff, RefundOfContributions, ResidentialPropertyHeld, TransferToNonRegPensionScheme}
import models.event1.WhoReceivedUnauthPayment.{Employer, Member}
import models.event1.employer.PaymentNature.ResidentialProperty
import models.event10.BecomeOrCeaseScheme.{ItBecameAnInvestmentRegulatedPensionScheme, ItHasCeasedToBeAnInvestmentRegulatedPensionScheme}
import models.event13.SchemeStructure
import models.event3.ReasonForBenefits.Other
import models.event8a.PaymentType
import models.requests.DataRequest
import models.{Index, MemberSummaryPath}
import pages.EmptyWaypoints
import pages.address.ManualAddressPage
import pages.common.{ChooseTaxYearPage, MembersDetailsPage, PaymentDetailsPage, TotalPensionAmountsPage}
import pages.event1._
import pages.event1.employer.CompanyDetailsPage
import pages.event1.member.{ReasonForTheOverpaymentOrWriteOffPage, RefundOfContributionsPage, WhoWasTheTransferMadePage}
import pages.event10.{BecomeOrCeaseSchemePage, ContractsOrPoliciesPage, Event10CheckYourAnswersPage, SchemeChangeDatePage}
import pages.event11._
import pages.event12.{DateOfChangePage, Event12CheckYourAnswersPage}
import pages.event13.{ChangeDatePage, Event13CheckYourAnswersPage, SchemeStructureDescriptionPage, SchemeStructurePage}
import pages.event14.{Event14CheckYourAnswersPage, HowManySchemeMembersPage}
import pages.event19.{CountryOrTerritoryPage, DateChangeMadePage, Event19CheckYourAnswersPage}
import pages.event2.{AmountPaidPage, DatePaidPage, Event2CheckYourAnswersPage}
import pages.event20.{BecameDatePage, CeasedDatePage, Event20CheckYourAnswersPage, WhatChangePage}
import pages.event22.Event22CheckYourAnswersPage
import pages.event23.Event23CheckYourAnswersPage
import pages.event3.{EarlyBenefitsBriefDescriptionPage, Event3CheckYourAnswersPage, ReasonForBenefitsPage}
import pages.event4.Event4CheckYourAnswersPage
import pages.event5.Event5CheckYourAnswersPage
import pages.event6.{Event6CheckYourAnswersPage, InputProtectionTypePage, TypeOfProtectionPage}
import pages.event7.{CrystallisedAmountPage, Event7CheckYourAnswersPage, LumpSumAmountPage, PaymentDatePage}
import pages.event8.{Event8CheckYourAnswersPage, LumpSumAmountAndDatePage}
import pages.event8a.{Event8ACheckYourAnswersPage, PaymentTypePage}
import play.api.libs.json.JsBoolean
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import services.CompileService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserAnswersValidation @Inject()(compileService: CompileService) {
  def validateMemberPaymentNature(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val memberPaymentNatureAnswer = request.userAnswers.get(pages.event1.member.PaymentNaturePage(index))
    val transferRecipientAnswer = request.userAnswers.get(WhoWasTheTransferMadePage(index))
    val refundDetailsAnswer = request.userAnswers.get(RefundOfContributionsPage(index))
    val overpaymentAnswer = request.userAnswers.get(ReasonForTheOverpaymentOrWriteOffPage(index))
    val memberPropertyAddressAnswer = request.userAnswers.get(ManualAddressPage(AddressJourneyType.Event1MemberPropertyAddressJourney, index))
    val paymentDetailsAnswer = request.userAnswers.get(PaymentDetailsPage(Event1, index))

    (memberPaymentNatureAnswer, transferRecipientAnswer, refundDetailsAnswer, overpaymentAnswer, memberPropertyAddressAnswer, paymentDetailsAnswer) match {
      case (Some(TransferToNonRegPensionScheme), Some(_), None, None, None, Some(_)) |
           (Some(RefundOfContributions), None, Some(_), None, None, Some(_)) |
           (Some(OverpaymentOrWriteOff), None, None, Some(_), None, Some(_)) |
           (Some(ResidentialPropertyHeld), None, None, None, Some(_), Some(_)) |
           (Some(_), None, None, None, None, Some(_)) => compileService.compileEvent(Event1, request.pstr, request.userAnswers).map {
        _ => Redirect(controllers.event1.routes.UnauthPaymentSummaryController.onPageLoad(EmptyWaypoints))
      }
      case (Some(TransferToNonRegPensionScheme), None, _, _, _, _) => Future.successful(
        Redirect(WhoWasTheTransferMadePage(index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
      case (Some(RefundOfContributions), _, None, _, _, _) => Future.successful(
        Redirect(RefundOfContributionsPage(index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
      case (Some(OverpaymentOrWriteOff), _, _, None, _, _) => Future.successful(
        Redirect(ReasonForTheOverpaymentOrWriteOffPage(index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
      case (Some(ResidentialPropertyHeld), _, _, _, None, _) => Future.successful(
        Redirect(ManualAddressPage(AddressJourneyType.Event1MemberPropertyAddressJourney, index)
          .changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
      case (Some(_), None, None, None, None, None) => Future.successful(
        Redirect(PaymentDetailsPage(Event1, index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
      case _ => Future.successful(
        Redirect(pages.event1.member.PaymentNaturePage(index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
    }
  }

  def validateMemberRoute(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val membersDetailsAnswer = request.userAnswers.get(MembersDetailsPage(Event1, index))
    val doYouHoldSignedMandateAnswer = request.userAnswers.get(DoYouHoldSignedMandatePage(index))
    val valueOfUnauthorisedPaymentAnswer = request.userAnswers.get(ValueOfUnauthorisedPaymentPage(index))
    val surchargeMemberAnswer = request.userAnswers.get(SchemeUnAuthPaySurchargeMemberPage(index))

    (membersDetailsAnswer, doYouHoldSignedMandateAnswer, valueOfUnauthorisedPaymentAnswer, surchargeMemberAnswer) match {
      case (Some(_), Some(_), Some(true), Some(_)) | (Some(_), Some(_), Some(false), _) => validateMemberPaymentNature(index)
      case (Some(_), None, _, _) => Future.successful(
        Redirect(DoYouHoldSignedMandatePage(index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
      case (Some(_), Some(_), None, _) => Future.successful(
        Redirect(ValueOfUnauthorisedPaymentPage(index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
      case (Some(_), Some(_), Some(true), None) => Future.successful(
        Redirect(SchemeUnAuthPaySurchargeMemberPage(index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
      case _ => Future.successful(
        Redirect(MembersDetailsPage(Event1, index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
    }
  }

  def validateEmployerPaymentNature(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val employerPaymentNatureAnswer = request.userAnswers.get(pages.event1.employer.PaymentNaturePage(index))
    val employerPropertyAddressAnswer = request.userAnswers.get(ManualAddressPage(AddressJourneyType.Event1EmployerPropertyAddressJourney, index))
    val paymentDetailsAnswer = request.userAnswers.get(PaymentDetailsPage(Event1, index))

    (employerPaymentNatureAnswer, employerPropertyAddressAnswer, paymentDetailsAnswer) match {
      case (Some(ResidentialProperty), Some(_), Some(_)) => compileService.compileEvent(Event1, request.pstr, request.userAnswers).map {
        _ => Redirect(controllers.event1.routes.UnauthPaymentSummaryController.onPageLoad(EmptyWaypoints))
      }
      case (Some(_), None, Some(_)) => compileService.compileEvent(Event1, request.pstr, request.userAnswers).map {
        _ => Redirect(controllers.event1.routes.UnauthPaymentSummaryController.onPageLoad(EmptyWaypoints))
      }
      case (Some(ResidentialProperty), None, _) => Future.successful(
        Redirect(ManualAddressPage(AddressJourneyType.Event1EmployerPropertyAddressJourney, index)
          .changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url
        )
      )
      case (None, None, None) => Future.successful(
        Redirect(pages.event1.employer.PaymentNaturePage(index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
      case _ => Future.successful(
        Redirect(PaymentDetailsPage(Event1, index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
    }
  }

  def validateEmployerRoute(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val companyDetailsAnswer = request.userAnswers.get(CompanyDetailsPage(index))
    val companyAddressAnswer = request.userAnswers.get(ManualAddressPage(AddressJourneyType.Event1EmployerAddressJourney, index))

    (companyDetailsAnswer, companyAddressAnswer) match {
      case (Some(_), Some(_)) => validateEmployerPaymentNature(index)
      case (None, _) => Future.successful(
        Redirect(CompanyDetailsPage(index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
      case _ => Future.successful(
        Redirect(ManualAddressPage(AddressJourneyType.Event1EmployerAddressJourney, index)
          .changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
    }
  }

  def event1AnswerValidation(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val paymentTypeAnswer = request.userAnswers.get(WhoReceivedUnauthPaymentPage(index))

    paymentTypeAnswer match {
      case Some(Member) => validateMemberRoute(index)
      case Some(Employer) => validateEmployerRoute(index)
      case _ => Future.successful(
        Redirect(WhoReceivedUnauthPaymentPage(index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
    }
  }

  def event2AnswerValidation(index: Index)
                            (implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val deceasedMembersDetailsAnswer = request.userAnswers.get(MembersDetailsPage(Event2, index, Event2MemberPageNumbers.FIRST_PAGE_DECEASED))
    val beneficiaryMembersDetailsAnswer = request.userAnswers.get(MembersDetailsPage(Event2, index, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY))
    val amountPaidAnswer = request.userAnswers.get(AmountPaidPage(index, Event2))
    val datePaidAnswer = request.userAnswers.get(DatePaidPage(index, Event2))

    (deceasedMembersDetailsAnswer, beneficiaryMembersDetailsAnswer, amountPaidAnswer, datePaidAnswer) match {
      case (Some(_), Some(_), Some(_), Some(_)) => compileService.compileEvent(Event2, request.pstr, request.userAnswers).map {
        _ =>
          Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event2)))
      }
      case (None, _, _, _) =>
        Future.successful(
          Redirect(MembersDetailsPage(Event2, index, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY)
            .changeLink(EmptyWaypoints, Event2CheckYourAnswersPage(index)).url)
        )
      case (Some(_), None, _, _) => Future.successful(
        Redirect(MembersDetailsPage(
          Event2, index, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY)
          .changeLink(EmptyWaypoints, Event2CheckYourAnswersPage(index)).url
        )
      )
      case (Some(_), Some(_), None, _) => Future.successful(
        Redirect(AmountPaidPage(index, Event2).changeLink(EmptyWaypoints, Event2CheckYourAnswersPage(index)).url)
      )
      case (Some(_), Some(_), Some(_), None) => Future.successful(
        Redirect(DatePaidPage(index, Event2).changeLink(EmptyWaypoints, Event2CheckYourAnswersPage(index)).url)
      )
    }
  }

  def event3AnswerValidation(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val membersDetailsAnswer = request.userAnswers.get(MembersDetailsPage(Event3, index))
    val reasonForBenefitsAnswer = request.userAnswers.get(ReasonForBenefitsPage(index))
    val earlyBenefitsDescriptionAnswer = request.userAnswers.get(EarlyBenefitsBriefDescriptionPage(index))
    val paymentDetailsAnswer = request.userAnswers.get(PaymentDetailsPage(Event3, index))

    (membersDetailsAnswer, reasonForBenefitsAnswer, earlyBenefitsDescriptionAnswer, paymentDetailsAnswer) match {
      case (Some(_), Some(Other), Some(_), Some(_)) => compileService.compileEvent(Event3, request.pstr, request.userAnswers).map {
        _ =>
          Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event3)).url)
      }
      case (Some(_), Some(_), None, Some(_)) => compileService.compileEvent(Event3, request.pstr, request.userAnswers).map {
        _ =>
          Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event3)).url)
      }
      case (None, _, _, _) => Future.successful(
        Redirect(MembersDetailsPage(Event3, index)
          .changeLink(EmptyWaypoints, Event3CheckYourAnswersPage(index)).url)
      )
      case (Some(_), None, _, _) => Future.successful(
        Redirect(ReasonForBenefitsPage(index)
          .changeLink(EmptyWaypoints, Event3CheckYourAnswersPage(index)).url)
      )
      case (Some(_), Some(Other), None, _) => Future.successful(
        Redirect(EarlyBenefitsBriefDescriptionPage(index)
          .changeLink(EmptyWaypoints, Event3CheckYourAnswersPage(index)).url)
      )
      case _ => Future.successful(
        Redirect(PaymentDetailsPage(Event3, index)
          .changeLink(EmptyWaypoints, Event3CheckYourAnswersPage(index)).url)
      )
    }
  }

  def event4AnswerValidation(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val membersDetailsAnswer = request.userAnswers.get(MembersDetailsPage(Event4, index))
    val paymentDetailsAnswer = request.userAnswers.get(PaymentDetailsPage(Event4, index))

    (membersDetailsAnswer, paymentDetailsAnswer) match {
      case (Some(_), Some(_)) => compileService.compileEvent(Event4, request.pstr, request.userAnswers).map {
        _ =>
          Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event4)).url)
      }
      case (Some(_), None) => Future.successful(
        Redirect(PaymentDetailsPage(Event4, index).changeLink(EmptyWaypoints, Event4CheckYourAnswersPage(index)).url)
      )
      case _ => Future.successful(
        Redirect(MembersDetailsPage(Event4, index).changeLink(EmptyWaypoints, Event4CheckYourAnswersPage(index)).url)
      )
    }
  }

  def event5AnswerValidation(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val membersDetailsAnswer = request.userAnswers.get(MembersDetailsPage(Event5, index))
    val paymentDetailsAnswer = request.userAnswers.get(PaymentDetailsPage(Event5, index))

    (membersDetailsAnswer, paymentDetailsAnswer) match {
      case (Some(_), Some(_)) => compileService.compileEvent(Event5, request.pstr, request.userAnswers).map {
        _ =>
          Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event5)).url)
      }
      case (Some(_), None) => Future.successful(
        Redirect(PaymentDetailsPage(Event5, index).changeLink(EmptyWaypoints, Event5CheckYourAnswersPage(index)).url)
      )
      case _ => Future.successful(
        Redirect(MembersDetailsPage(Event5, index).changeLink(EmptyWaypoints, Event5CheckYourAnswersPage(index)).url)
      )
    }
  }

  def event6AnswerValidation(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val membersDetailsAnswer = request.userAnswers.get(MembersDetailsPage(Event6, index))
    val typeOfProtectionAnswer = request.userAnswers.get(TypeOfProtectionPage(Event6, index))
    val protectionReferenceAnswer = request.userAnswers.get(InputProtectionTypePage(Event6, index))
    val paymentDetailsAnswer = request.userAnswers.get(PaymentDetailsPage(Event6, index))

    (membersDetailsAnswer, typeOfProtectionAnswer, protectionReferenceAnswer, paymentDetailsAnswer) match {
      case (Some(_), Some(_), Some(_), Some(_)) => compileService.compileEvent(Event6, request.pstr, request.userAnswers).map {
        _ =>
          Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event6)).url)
      }
      case (Some(_), Some(_), Some(_), None) => Future.successful(
        Redirect(PaymentDetailsPage(Event6, index).changeLink(EmptyWaypoints, Event6CheckYourAnswersPage(index)).url)
      )
      case (Some(_), Some(_), None, _) => Future.successful(
        Redirect(InputProtectionTypePage(Event6, index).changeLink(EmptyWaypoints, Event6CheckYourAnswersPage(index)).url)
      )
      case (Some(_), None, _, _) => Future.successful(
        Redirect(TypeOfProtectionPage(Event6, index).changeLink(EmptyWaypoints, Event6CheckYourAnswersPage(index)).url)
      )
      case _ => Future.successful(
        Redirect(MembersDetailsPage(Event6, index).changeLink(EmptyWaypoints, Event6CheckYourAnswersPage(index)).url)
      )
    }
  }

  def event7AnswerValidation(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val membersDetailsAnswer = request.userAnswers.get(MembersDetailsPage(Event7, index))
    val lumpSumAmountAnswer = request.userAnswers.get(LumpSumAmountPage(index))
    val cystallisedAmountAnswer = request.userAnswers.get(CrystallisedAmountPage(index))
    val paymentDateAnswer = request.userAnswers.get(PaymentDatePage(index))

    (membersDetailsAnswer, lumpSumAmountAnswer, cystallisedAmountAnswer, paymentDateAnswer) match {
      case (Some(_), Some(_), Some(_), Some(_)) => compileService.compileEvent(Event7, request.pstr, request.userAnswers).map {
        _ =>
          Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event7)).url)
      }
      case (Some(_), Some(_), Some(_), None) => Future.successful(
        Redirect(PaymentDatePage(index).changeLink(EmptyWaypoints, Event7CheckYourAnswersPage(index)).url)
      )
      case (Some(_), Some(_), None, _) => Future.successful(
        Redirect(CrystallisedAmountPage(index).changeLink(EmptyWaypoints, Event7CheckYourAnswersPage(index)).url)
      )
      case (Some(_), None, _, _) => Future.successful(
        Redirect(LumpSumAmountPage(index).changeLink(EmptyWaypoints, Event7CheckYourAnswersPage(index)).url)
      )
      case _ => Future.successful(
        Redirect(MembersDetailsPage(Event7, index).changeLink(EmptyWaypoints, Event7CheckYourAnswersPage(index)).url)
      )
    }
  }

  def event8AnswerValidation(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val membersDetailsAnswer = request.userAnswers.get(MembersDetailsPage(Event8, index))
    val typeOfProtectionAnswer = request.userAnswers.get(pages.event8.TypeOfProtectionPage(Event8, index))
    val typeOfProtectionReferenceAnswer = request.userAnswers.get(pages.event8.TypeOfProtectionReferencePage(Event8, index))
    val lumpSumDetailsAnswer = request.userAnswers.get(LumpSumAmountAndDatePage(Event8, index))

    (membersDetailsAnswer, typeOfProtectionAnswer, typeOfProtectionReferenceAnswer, lumpSumDetailsAnswer) match {
      case (Some(_), Some(_), Some(_), Some(_)) => compileService.compileEvent(Event8, request.pstr, request.userAnswers).map {
        _ =>
          Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event8)).url)
      }
      case (Some(_), Some(_), Some(_), None) => Future.successful(
        Redirect(LumpSumAmountAndDatePage(Event8, index).changeLink(EmptyWaypoints, Event8CheckYourAnswersPage(index)).url)
      )
      case (Some(_), Some(_), None, _) => Future.successful(
        Redirect(pages.event8.TypeOfProtectionReferencePage(Event8, index).changeLink(EmptyWaypoints, Event8CheckYourAnswersPage(index)).url)
      )
      case (Some(_), None, _, _) => Future.successful(
        Redirect(pages.event8.TypeOfProtectionPage(Event8, index).changeLink(EmptyWaypoints, Event8CheckYourAnswersPage(index)).url)
      )
      case _ => Future.successful(
        Redirect(MembersDetailsPage(Event8, index).changeLink(EmptyWaypoints, Event8CheckYourAnswersPage(index)).url)
      )
    }
  }

  def event8AAnswerValidation(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val membersDetailsAnswer = request.userAnswers.get(MembersDetailsPage(Event8A, index))
    val paymentTypeAnswer = request.userAnswers.get(PaymentTypePage(Event8A, index))
    val typeOfProtectionAnswer = request.userAnswers.get(pages.event8.TypeOfProtectionPage(Event8A, index))
    val typeOfProtectionReferenceAnswer = request.userAnswers.get(pages.event8.TypeOfProtectionReferencePage(Event8A, index))
    val lumpSumDetailsAnswer = request.userAnswers.get(LumpSumAmountAndDatePage(Event8A, index))

    (membersDetailsAnswer, paymentTypeAnswer, typeOfProtectionAnswer, typeOfProtectionReferenceAnswer, lumpSumDetailsAnswer) match {
      case (Some(_), Some(PaymentType.PaymentOfAStandAloneLumpSum), Some(_), Some(_), Some(_)) |
           (Some(_), Some(PaymentType.PaymentOfASchemeSpecificLumpSum), None, None, Some(_)) =>
        compileService.compileEvent(Event8A, request.pstr, request.userAnswers).map { _ =>
          Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event8A)).url)
        }
      case (Some(_), Some(_), Some(_), Some(_), None) => Future.successful(
        Redirect(LumpSumAmountAndDatePage(Event8A, index).changeLink(EmptyWaypoints, Event8CheckYourAnswersPage(index)).url)
      )
      case (Some(_), Some(_), Some(_), None, _) => Future.successful(
        Redirect(pages.event8.TypeOfProtectionReferencePage(Event8A, index).changeLink(EmptyWaypoints, Event8ACheckYourAnswersPage(index)).url)
      )
      case (Some(_), Some(PaymentType.PaymentOfAStandAloneLumpSum), None, _, _) => Future.successful(
        Redirect(pages.event8.TypeOfProtectionPage(Event8A, index).changeLink(EmptyWaypoints, Event8ACheckYourAnswersPage(index)).url)
      )
      case (Some(_), None, _, _, _) => Future.successful(
        Redirect(PaymentTypePage(Event8A, index).changeLink(EmptyWaypoints, Event8ACheckYourAnswersPage(index)).url)
      )
      case _ => Future.successful(
        Redirect(MembersDetailsPage(Event8A, index).changeLink(EmptyWaypoints, Event8ACheckYourAnswersPage(index)).url)
      )
    }
  }

  def event10AnswerValidation()(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val becomeOrCeaseAnswer = request.userAnswers.get(BecomeOrCeaseSchemePage)
    val schemeChangeDateAnswer = request.userAnswers.get(SchemeChangeDatePage)

    val contractsOrPoliciesAnswer = request.userAnswers.get(ContractsOrPoliciesPage)

    (becomeOrCeaseAnswer, schemeChangeDateAnswer, contractsOrPoliciesAnswer) match {
      case (Some(ItBecameAnInvestmentRegulatedPensionScheme), Some(_), Some(_)) |
           (Some(ItHasCeasedToBeAnInvestmentRegulatedPensionScheme), Some(_), _) =>
        compileService.compileEvent(Event10, request.pstr, request.userAnswers).map { _ =>
          Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)
        }
      case (Some(_), None, _) => Future.successful(
        Redirect(SchemeChangeDatePage.changeLink(EmptyWaypoints, Event10CheckYourAnswersPage()).url)
      )
      case (Some(ItBecameAnInvestmentRegulatedPensionScheme), Some(_), None) => Future.successful(
        Redirect(ContractsOrPoliciesPage.changeLink(EmptyWaypoints, Event10CheckYourAnswersPage()).url)
      )
      case _ => Future.successful(
        Redirect(BecomeOrCeaseSchemePage.changeLink(EmptyWaypoints, Event10CheckYourAnswersPage()).url)
      )

    }
  }

  def event11AnswerValidation()(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val hasSchemeChangedRulesAnswer = request.userAnswers.get(HasSchemeChangedRulesPage)
    val unAuthPaymentsRuleChangeDateAnswer = request.userAnswers.get(UnAuthPaymentsRuleChangeDatePage)
    val hasSchemeChangedRulesInvestmentsInAssetsAnswer = request.userAnswers.get(HasSchemeChangedRulesInvestmentsInAssetsPage)
    val investmentsInAssetsRuleChangeDateAnswer = request.userAnswers.get(InvestmentsInAssetsRuleChangeDatePage)

    def hasSchemeChangedRulesInvestmentsInAssetsValidation: Future[Result] = {
      (hasSchemeChangedRulesInvestmentsInAssetsAnswer, investmentsInAssetsRuleChangeDateAnswer) match {
        case (Some(true), Some(_)) | (Some(false), _) => compileService.compileEvent(Event11, request.pstr, request.userAnswers).map {
          _ =>
            Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)
        }
        case (Some(true), None) => Future.successful(
          Redirect(InvestmentsInAssetsRuleChangeDatePage.changeLink(EmptyWaypoints, Event11CheckYourAnswersPage()).url)
        )
        case _ => Future.successful(
          Redirect(HasSchemeChangedRulesInvestmentsInAssetsPage.changeLink(EmptyWaypoints, Event11CheckYourAnswersPage()).url)
        )
      }
    }

    (hasSchemeChangedRulesAnswer, unAuthPaymentsRuleChangeDateAnswer) match {
      case (Some(true), Some(_)) | (Some(false), _) => hasSchemeChangedRulesInvestmentsInAssetsValidation
      case (Some(true), None) => Future.successful(
        Redirect(UnAuthPaymentsRuleChangeDatePage.changeLink(EmptyWaypoints, Event11CheckYourAnswersPage()).url)
      )
      case _ => Future.successful(
        Redirect(HasSchemeChangedRulesPage.changeLink(EmptyWaypoints, Event11CheckYourAnswersPage()).url)
      )
    }
  }

  def event12AnswerValidation()(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val hasSchemeChangedRulesAnswer = request.userAnswers.get(pages.event12.HasSchemeChangedRulesPage)
    val dateOfChangeAnswer = request.userAnswers.get(DateOfChangePage)

    (hasSchemeChangedRulesAnswer, dateOfChangeAnswer) match {
      case (Some(true), Some(_)) => compileService.compileEvent(Event12, request.pstr, request.userAnswers).map {
        _ =>
          Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)
      }
      case (Some(true), None) => Future.successful(
        Redirect(DateOfChangePage.changeLink(EmptyWaypoints, Event12CheckYourAnswersPage()).url)
      )
      case _ => Future.successful(
        Redirect(pages.event12.HasSchemeChangedRulesPage.changeLink(EmptyWaypoints, Event12CheckYourAnswersPage()).url)
      )
    }
  }

  def event13AnswerValidation()(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val schemeStructureAnswer = request.userAnswers.get(SchemeStructurePage)
    val schemeDescriptionAnswer = request.userAnswers.get(SchemeStructureDescriptionPage)
    val changeDateAnswer = request.userAnswers.get(ChangeDatePage)

    (schemeStructureAnswer, schemeDescriptionAnswer, changeDateAnswer) match {
      case (Some(SchemeStructure.Other), Some(_), Some(_)) =>
        compileService.compileEvent(Event13, request.pstr, request.userAnswers).map {
          _ =>
            Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)

        }
      case (Some(_), _, None) => Future.successful(
        Redirect(ChangeDatePage.changeLink(EmptyWaypoints, Event13CheckYourAnswersPage()).url)
      )
      case (Some(SchemeStructure.Other), None, _) => Future.successful(
        Redirect(SchemeStructureDescriptionPage.changeLink(EmptyWaypoints, Event13CheckYourAnswersPage()).url)
      )
      case (Some(_), _, Some(_)) => {
        compileService.compileEvent(Event13, request.pstr, request.userAnswers).map {
          _ =>
            Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)

        }
      }
      case _ => Future.successful(
        Redirect(SchemeStructurePage.changeLink(EmptyWaypoints, Event13CheckYourAnswersPage()).url)
      )
    }
  }

  def event14AnswerValidation()(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val noOfSchemeMembersAnswer = request.userAnswers.get(HowManySchemeMembersPage)

    noOfSchemeMembersAnswer match {
      case Some(_) => compileService.compileEvent(Event14, request.pstr, request.userAnswers).map {
        _ =>
          Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)
      }
      case _ => Future.successful(
        Redirect(HowManySchemeMembersPage.changeLink(EmptyWaypoints, Event14CheckYourAnswersPage).url)
      )
    }
  }

  def event19AnswerValidation()(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val countryOrTerritoryAnswer = request.userAnswers.get(CountryOrTerritoryPage)
    val dateAnswer = request.userAnswers.get(DateChangeMadePage)

    val originalUserAnswers = request.userAnswers
    val isCountryUkEuOrEEA = originalUserAnswers.get(CountryOrTerritoryPage.booleanPath)
    val countryCodeForNonUkEuOrEEACountries = "ZZ"

    val maybeUpdatedAnswers = isCountryUkEuOrEEA.collect {
      case JsBoolean(false) =>
        originalUserAnswers.setOrException(CountryOrTerritoryPage, countryCodeForNonUkEuOrEEACountries)
    }.getOrElse(originalUserAnswers)

    (countryOrTerritoryAnswer, dateAnswer) match {
      case (Some(_), Some(_)) => compileService.compileEvent(Event19, request.pstr, maybeUpdatedAnswers).map { _ =>
        Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)
      }
      case (Some(_), None) => Future.successful(
        Redirect(DateChangeMadePage.changeLink(EmptyWaypoints, Event19CheckYourAnswersPage).url)
      )
      case _ => Future.successful(
        Redirect(CountryOrTerritoryPage.changeLink(EmptyWaypoints, Event19CheckYourAnswersPage).url)
      )
    }
  }

  def event20AnswerValidation()(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val whatChangeAnswer = request.userAnswers.get(WhatChangePage)
    val becameDateAnswer = request.userAnswers.get(BecameDatePage)
    val ceasedDateAnswer = request.userAnswers.get(CeasedDatePage)

    (whatChangeAnswer, becameDateAnswer, ceasedDateAnswer) match {
      case (Some(models.event20.WhatChange.BecameOccupationalScheme), Some(_), _) |
           (Some(models.event20.WhatChange.CeasedOccupationalScheme), _, Some(_)) =>
        compileService.compileEvent(Event20, request.pstr, request.userAnswers).map { _ =>
          Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)
      }
      case (Some(models.event20.WhatChange.BecameOccupationalScheme), None, _) => Future.successful(
        Redirect(BecameDatePage.changeLink(EmptyWaypoints, Event20CheckYourAnswersPage()).url)
      )
      case (Some(models.event20.WhatChange.CeasedOccupationalScheme), _, None) => Future.successful(
        Redirect(CeasedDatePage.changeLink(EmptyWaypoints, Event20CheckYourAnswersPage()).url)
      )
      case _ => Future.successful(
        Redirect(WhatChangePage.changeLink(EmptyWaypoints, Event20CheckYourAnswersPage()).url)
      )
    }
  }

  def event22and23AnswerValidation(index: Index, eventType: EventType)
                                  (implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {

    val membersDetailsAnswer = request.userAnswers.get(MembersDetailsPage(eventType, index))
    val taxYearChosen = getTaxYear(request.userAnswers)
    val rdsTaxYear = ChooseTaxYear.reads(ChooseTaxYear.enumerable(taxYearChosen))
    val taxYearAnswer = request.userAnswers.get(ChooseTaxYearPage(eventType, index))(rdsTaxYear)
    val pensionAmountsAnswer = request.userAnswers.get(TotalPensionAmountsPage(eventType, index))

    def validateEvent22 = {
      (membersDetailsAnswer, taxYearAnswer, pensionAmountsAnswer) match {
        case (Some(_), Some(_), Some(_)) => compileService.compileEvent(Event22, request.pstr, request.userAnswers).map {
          _ =>
            Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event22)).url)
        }
        case (Some(_), Some(_), None) => Future.successful(
          Redirect(TotalPensionAmountsPage(Event22, index).changeLink(EmptyWaypoints, Event22CheckYourAnswersPage(index)).url)
        )
        case (Some(_), None, _) => Future.successful(
          Redirect(ChooseTaxYearPage(Event22, index).changeLink(EmptyWaypoints, Event22CheckYourAnswersPage(index)).url)
        )
        case _ => Future.successful(
          Redirect(MembersDetailsPage(Event22, index).changeLink(EmptyWaypoints, Event22CheckYourAnswersPage(index)).url)
        )
      }
    }

    def validateEvent23 = {
      (membersDetailsAnswer, taxYearAnswer, pensionAmountsAnswer) match {
        case (Some(_), Some(_), Some(_)) => compileService.compileEvent(Event23, request.pstr, request.userAnswers).map {
          _ =>
            Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event23)).url)
        }
        case (Some(_), Some(_), None) => Future.successful(
          Redirect(TotalPensionAmountsPage(Event23, index).changeLink(EmptyWaypoints, Event23CheckYourAnswersPage(index)).url)
        )
        case (Some(_), None, _) => Future.successful(
          Redirect(ChooseTaxYearPage(Event23, index).changeLink(EmptyWaypoints, Event23CheckYourAnswersPage(index)).url)
        )
        case _ => Future.successful(
          Redirect(MembersDetailsPage(Event23, index).changeLink(EmptyWaypoints, Event23CheckYourAnswersPage(index)).url)
        )
      }
    }

    eventType match {
      case Event22 => validateEvent22
      case _ => validateEvent23
    }
  }

}

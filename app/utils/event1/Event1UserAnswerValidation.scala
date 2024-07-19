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

package utils.event1

import models.Index
import models.enumeration.AddressJourneyType
import models.enumeration.EventType.Event1
import models.event1.PaymentNature.{OverpaymentOrWriteOff, RefundOfContributions, ResidentialPropertyHeld, TransferToNonRegPensionScheme}
import models.event1.WhoReceivedUnauthPayment.{Employer, Member}
import models.requests.DataRequest
import pages.EmptyWaypoints
import pages.address.{EnterPostcodePage, ManualAddressPage}
import pages.common.MembersDetailsPage
import pages.event1._
import pages.event1.employer.CompanyDetailsPage
import pages.event1.member.{ReasonForTheOverpaymentOrWriteOffPage, RefundOfContributionsPage, WhoWasTheTransferMadePage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import services.CompileService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Event1UserAnswerValidation @Inject()(compileService: CompileService) {

  def validatePaymentDetails(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val paymentDetailsAnswer = request.userAnswers.get(PaymentValueAndDatePage(index))

    paymentDetailsAnswer match {
      case Some(_) => compileService.compileEvent(Event1, request.pstr, request.userAnswers).map {
        _ => Redirect(controllers.event1.routes.UnauthPaymentSummaryController.onPageLoad(EmptyWaypoints))
      }
      case None => Future.successful(
        Redirect(PaymentValueAndDatePage(index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
    }
  }

  def validateTransferToNonRegPensionScheme(index: Index)
                                           (implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val transferRecipientAnswer = request.userAnswers.get(WhoWasTheTransferMadePage(index))

    transferRecipientAnswer match {
      case Some(_) => validatePaymentDetails(index)
      case None => Future.successful(
        Redirect(WhoWasTheTransferMadePage(index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
    }
  }

  def validateRefundOfContributions(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val refundDetailsAnswer = request.userAnswers.get(RefundOfContributionsPage(index))

    refundDetailsAnswer match {
      case Some(_) => validatePaymentDetails(index)
      case None => Future.successful(
        Redirect(RefundOfContributionsPage(index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
    }
  }

  def validateOverpaymentOrWriteOff(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val overpaymentDetailsAnswer = request.userAnswers.get(ReasonForTheOverpaymentOrWriteOffPage(index))

    overpaymentDetailsAnswer match {
      case Some(_) => validatePaymentDetails(index)
      case None => Future.successful(
        Redirect(ReasonForTheOverpaymentOrWriteOffPage(index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
    }
  }

  def validateResidentialPropertyHeld(index: Index)
                                     (implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val postcodeAnswer = request.userAnswers.get(EnterPostcodePage(AddressJourneyType.Event1MemberPropertyAddressJourney, index))
    val manualAddressAnswer = request.userAnswers.get(ManualAddressPage(AddressJourneyType.Event1MemberPropertyAddressJourney, index))

    (postcodeAnswer, manualAddressAnswer) match {
      case (Some(_), _) | (_, Some(_)) => validatePaymentDetails(index)
      case _ => Future.successful(
        Redirect(EnterPostcodePage(AddressJourneyType.Event1MemberPropertyAddressJourney, index)
          .changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
    }
  }

  def validateMemberPaymentNature(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val memberPaymentNatureAnswer = request.userAnswers.get(pages.event1.member.PaymentNaturePage(index))

    memberPaymentNatureAnswer match {
      case Some(TransferToNonRegPensionScheme) => validateTransferToNonRegPensionScheme(index)
      case Some(RefundOfContributions) => validateRefundOfContributions(index)
      case Some(OverpaymentOrWriteOff) => validateOverpaymentOrWriteOff(index)
      case Some(ResidentialPropertyHeld) => validateResidentialPropertyHeld(index)
      case Some(_) => validatePaymentDetails(index)
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
    val employerPostcodeAnswer = request.userAnswers.get(EnterPostcodePage(AddressJourneyType.Event1EmployerPropertyAddressJourney, index))
    val employerPropertyAddressAnswer = request.userAnswers.get(ManualAddressPage(AddressJourneyType.Event1EmployerPropertyAddressJourney, index))
    val paymentDetailsAnswer = request.userAnswers.get(PaymentValueAndDatePage(index))

    (employerPaymentNatureAnswer, employerPostcodeAnswer, employerPropertyAddressAnswer, paymentDetailsAnswer) match {
      case (Some(_), Some(_), _, Some(_)) |
           (Some(_), _, Some(_), Some(_)) => compileService.compileEvent(Event1, request.pstr, request.userAnswers).map {
        _ => Redirect(controllers.event1.routes.UnauthPaymentSummaryController.onPageLoad(EmptyWaypoints))
      }
      case (Some(_), None, None,  _) => Future.successful(
        Redirect(ManualAddressPage(AddressJourneyType.Event1EmployerPropertyAddressJourney, index)
          .changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url
        )
      )
      case (Some(_), Some(_), None, None) | (Some(_), None, Some(_), None) => Future.successful(
        Redirect(PaymentValueAndDatePage(index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
      case _ => Future.successful(
        Redirect(pages.event1.employer.PaymentNaturePage(index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
    }
  }

  def validateEmployerRoute(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val companyDetailsAnswer = request.userAnswers.get(CompanyDetailsPage(index))
    val companyPostcodeAnswer = request.userAnswers.get(EnterPostcodePage(AddressJourneyType.Event1EmployerAddressJourney, index))
    val companyAddressAnswer = request.userAnswers.get(ManualAddressPage(AddressJourneyType.Event1EmployerAddressJourney, index))

    (companyDetailsAnswer, companyPostcodeAnswer, companyAddressAnswer) match {
      case (Some(_), Some(_), _) | (Some(_), _, Some(_)) => validateEmployerPaymentNature(index)
      case (Some(_), None, None) => Future.successful(
        Redirect(EnterPostcodePage(AddressJourneyType.Event1EmployerAddressJourney, index)
          .changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
      case _ => Future.successful(
        Redirect(CompanyDetailsPage(index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
    }
  }

  def validateAnswers(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val paymentTypeAnswer = request.userAnswers.get(WhoReceivedUnauthPaymentPage(index))

    paymentTypeAnswer match {
      case Some(Member) => validateMemberRoute(index)
      case Some(Employer) => validateEmployerRoute(index)
      case _ => Future.successful(
        Redirect(WhoReceivedUnauthPaymentPage(index).changeLink(EmptyWaypoints, Event1CheckYourAnswersPage(index)).url)
      )
    }
  }
}

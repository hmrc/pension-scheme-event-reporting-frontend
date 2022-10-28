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

package controllers

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.enumeration.EventType.{Event1, Event18, WindUp}
import models.enumeration.{AddressJourneyType, EventType}
import models.event1.PaymentNature._
import models.event1.WhoReceivedUnauthPayment.Member
import models.event1.employer.PaymentNature._
import models.requests.DataRequest
import pages.event1.employer.{PaymentNaturePage => EmployerPaymentNaturePage}
import pages.event1.member.{PaymentNaturePage => MemberPaymentNaturePage}
import pages.event1.{ValueOfUnauthorisedPaymentPage, WhoReceivedUnauthPaymentPage}
import pages.{CheckAnswersPage, CheckYourAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.address.checkAnswers.ChooseAddressSummary
import viewmodels.checkAnswers.{Event18ConfirmationSummary, MembersDetailsSummary, SchemeWindUpDateSummary}
import viewmodels.event1.checkAnswers._
import viewmodels.event1.employer.checkAnswers.{CompanyDetailsSummary, EmployerUnauthorisedPaymentRecipientNameSummary, LoanDetailsSummary, PaymentNatureSummary => EmployerPaymentNatureSummary}
import viewmodels.event1.member.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData) { implicit request =>

    val thisPage = CheckYourAnswersPage(eventType)
    val waypoints = EmptyWaypoints

    val rows = eventType match {
      case WindUp => buildEventWindUpCYARows(waypoints, thisPage)
      case Event1 => buildEvent1CYARows(waypoints, thisPage)
      case Event18 => buildEvent18CYARows(waypoints, thisPage)
      case _ => Nil
    }

    Ok(view(SummaryListViewModel(rows = rows)))
  }


  private def event1MemberJourney(implicit request: DataRequest[AnyContent]): Boolean = {

    request.userAnswers.get(WhoReceivedUnauthPaymentPage) match {
      case Some(Member) =>
        true
      case _ =>
        false
    }
  }

  private def schemeUnAuthPaySurchargeRow(waypoints: Waypoints, sourcePage: CheckAnswersPage)
                                         (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {

    request.userAnswers.get(ValueOfUnauthorisedPaymentPage) match {
      case Some(true) =>
        SchemeUnAuthPaySurchargeMemberSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
      case _ =>
        Nil
    }
  }

  // scalastyle:off cyclomatic.complexity
  // scalastyle:off method.length
  private def buildEvent1CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage)(implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {

    val basicMemberOrEmployerRows = if (event1MemberJourney) {
      MembersDetailsSummary.rowFullName(request.userAnswers, waypoints, sourcePage, Event1).toSeq ++
        MembersDetailsSummary.rowNino(request.userAnswers, waypoints, sourcePage, Event1).toSeq ++
        DoYouHoldSignedMandateSummary.row(request.userAnswers, waypoints, sourcePage).toSeq ++
        ValueOfUnauthorisedPaymentSummary.row(request.userAnswers, waypoints, sourcePage).toSeq ++
        schemeUnAuthPaySurchargeRow(waypoints, sourcePage) ++
        PaymentNatureSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
    } else {
      CompanyDetailsSummary.rowCompanyName(request.userAnswers, waypoints, sourcePage).toSeq ++
        CompanyDetailsSummary.rowCompanyNumber(request.userAnswers, waypoints, sourcePage).toSeq ++
        ChooseAddressSummary.row(request.userAnswers, waypoints, sourcePage, AddressJourneyType.Event1EmployerAddressJourney).toSeq ++
        EmployerPaymentNatureSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
    }

    val memberOrEmployerPaymentNatureRows = {

      if (event1MemberJourney) {
        request.userAnswers.get(MemberPaymentNaturePage) match {
          case Some(BenefitInKind) =>
            BenefitInKindBriefDescriptionSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
          case Some(TransferToNonRegPensionScheme) =>
            WhoWasTheTransferMadeSummary.row(request.userAnswers, waypoints, sourcePage).toSeq ++
              SchemeDetailsSummary.rowSchemeName(request.userAnswers, waypoints, sourcePage).toSeq ++
              SchemeDetailsSummary.rowSchemeReference(request.userAnswers, waypoints, sourcePage).toSeq
          case Some(ErrorCalcTaxFreeLumpSums) =>
            ErrorDescriptionSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
          case Some(BenefitsPaidEarly) =>
            BenefitsPaidEarlySummary.row(request.userAnswers, waypoints, sourcePage).toSeq
          case Some(RefundOfContributions) =>
            RefundOfContributionsSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
          case Some(OverpaymentOrWriteOff) =>
            ReasonForTheOverpaymentOrWriteOffSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
          case Some(ResidentialPropertyHeld) =>
            ReasonForTheOverpaymentOrWriteOffSummary.row(request.userAnswers, waypoints, sourcePage).toSeq ++
              ChooseAddressSummary.row(request.userAnswers, waypoints, sourcePage, AddressJourneyType.Event1MemberPropertyAddressJourney).toSeq
          case Some(TangibleMoveablePropertyHeld) =>
            MemberTangibleMoveablePropertySummary.row(request.userAnswers, waypoints, sourcePage).toSeq
          case Some(CourtOrConfiscationOrder) =>
            MemberUnauthorisedPaymentRecipientNameSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
          case Some(MemberOther) =>
            MemberPaymentNatureDescriptionSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
          case _ => Nil
        }
      } else {
        request.userAnswers.get(EmployerPaymentNaturePage) match {
          case Some(LoansExceeding50PercentOfFundValue) =>
            LoanDetailsSummary.rowLoanAmount(request.userAnswers, waypoints, sourcePage).toSeq ++
              LoanDetailsSummary.rowFundValue(request.userAnswers, waypoints, sourcePage).toSeq
          case Some(ResidentialProperty) =>
            ChooseAddressSummary.row(request.userAnswers, waypoints, sourcePage, AddressJourneyType.Event1EmployerPropertyAddressJourney).toSeq
          case Some(TangibleMoveableProperty) =>
            EmployerTangibleMoveablePropertySummary.row(request.userAnswers, waypoints, sourcePage).toSeq
          case Some(CourtOrder) =>
            EmployerUnauthorisedPaymentRecipientNameSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
          case Some(EmployerOther) =>
            EmployerPaymentNatureDescriptionSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
          case _ => Nil
        }
      }
    }

    val paymentValueAndDateRows =
      PaymentValueAndDateSummary.rowPaymentValue(request.userAnswers, waypoints, sourcePage).toSeq ++
        PaymentValueAndDateSummary.rowPaymentDate(request.userAnswers, waypoints, sourcePage).toSeq

    basicMemberOrEmployerRows ++ memberOrEmployerPaymentNatureRows ++ paymentValueAndDateRows
  }

  private def buildEventWindUpCYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage)(implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] =
    SchemeWindUpDateSummary.row(request.userAnswers, waypoints, sourcePage).toSeq

  private def buildEvent18CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage)(implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] =
    Event18ConfirmationSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
}

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

package controllers.event1

import com.google.inject.Inject
import connectors.EventReportingConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.Index
import models.enumeration.AddressJourneyType
import models.enumeration.EventType.Event1
import models.event1.PaymentNature._
import models.event1.WhoReceivedUnauthPayment.Member
import models.event1.employer.PaymentNature._
import models.requests.DataRequest
import pages.event1.employer.{PaymentNaturePage => EmployerPaymentNaturePage}
import pages.event1.member.{PaymentNaturePage => MemberPaymentNaturePage}
import pages.event1.{Event1CheckYourAnswersPage, ValueOfUnauthorisedPaymentPage, WhoReceivedUnauthPaymentPage}
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.address.checkAnswers.ChooseAddressSummary
import viewmodels.checkAnswers.MembersDetailsSummary
import viewmodels.event1.checkAnswers._
import viewmodels.event1.employer.checkAnswers.{CompanyDetailsSummary, EmployerUnauthorisedPaymentRecipientNameSummary, LoanDetailsSummary, PaymentNatureSummary => EmployerPaymentNatureSummary}
import viewmodels.event1.member.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class Event1CheckYourAnswersController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  connector: EventReportingConnector,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: CheckYourAnswersView
                                                )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] =
    (identify andThen getData(Event1) andThen requireData) { implicit request =>

      val thisPage = Event1CheckYourAnswersPage(index)
      val waypoints = EmptyWaypoints

      val continueUrl = controllers.event1.routes.Event1CheckYourAnswersController.onClick.url

      Ok(view(SummaryListViewModel(rows = buildEvent1CYARows(waypoints, thisPage, index)), continueUrl))
    }

  def onClick: Action[AnyContent] =
    (identify andThen getData(Event1) andThen requireData).async { implicit request =>
      val waypoints = EmptyWaypoints
      connector.compileEvent("123", Event1).map {
        _ =>
          Redirect(controllers.event1.routes.UnauthPaymentSummaryController.onPageLoad(waypoints))
      }
    }

  private def event1MemberJourney(index: Int)(implicit request: DataRequest[AnyContent]): Boolean = {
    request.userAnswers.get(WhoReceivedUnauthPaymentPage(index)) match {
      case Some(Member) => true
      case _ => false
    }
  }

  private def schemeUnAuthPaySurchargeRow(waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage)
                                         (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {

    request.userAnswers.get(ValueOfUnauthorisedPaymentPage(index)) match {
      case Some(true) =>
        SchemeUnAuthPaySurchargeMemberSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
      case _ =>
        Nil
    }
  }

  private def buildEvent1CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Int)
                                (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {

    val basicMemberOrEmployerRows = if (event1MemberJourney(index)) {
      event1BasicMemberDetailsRows(waypoints, sourcePage, index)
    } else {
      event1BasicEmployerDetailsRows(waypoints, sourcePage, index)
    }

    val memberOrEmployerPaymentNatureRows = {
      if (event1MemberJourney(index)) {
        event1MemberPaymentNatureRows(waypoints, sourcePage, index)
      } else {
        event1EmployerPaymentNatureRows(waypoints, sourcePage, index)
      }
    }

    val paymentValueAndDateRows = event1PaymentValueAndDateRows(waypoints, sourcePage, index)

    basicMemberOrEmployerRows ++ memberOrEmployerPaymentNatureRows ++ paymentValueAndDateRows
  }

  private def event1BasicMemberDetailsRows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Int)
                                          (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] =
    MembersDetailsSummary.rowFullName(request.userAnswers, waypoints, index, sourcePage, Event1).toSeq ++
      MembersDetailsSummary.rowNino(request.userAnswers, waypoints, index, sourcePage, Event1).toSeq ++
      DoYouHoldSignedMandateSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq ++
      ValueOfUnauthorisedPaymentSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq ++
      schemeUnAuthPaySurchargeRow(waypoints, index, sourcePage) ++
      PaymentNatureSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq

  private def event1BasicEmployerDetailsRows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Index)
                                            (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] =
    CompanyDetailsSummary.rowCompanyName(request.userAnswers, waypoints, index, sourcePage).toSeq ++
      CompanyDetailsSummary.rowCompanyNumber(request.userAnswers, waypoints, index, sourcePage).toSeq ++
      ChooseAddressSummary.row(request.userAnswers, waypoints, index, sourcePage, AddressJourneyType.Event1EmployerAddressJourney).toSeq ++
      EmployerPaymentNatureSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq

  // scalastyle:off cyclomatic.complexity
  private def event1MemberPaymentNatureRows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Int)
                                           (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] =
    request.userAnswers.get(MemberPaymentNaturePage(index)) match {
      case Some(BenefitInKind) =>
        BenefitInKindBriefDescriptionSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
      case Some(TransferToNonRegPensionScheme) =>
        WhoWasTheTransferMadeSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq ++
          SchemeDetailsSummary.rowSchemeName(request.userAnswers, waypoints, index, sourcePage).toSeq ++
          SchemeDetailsSummary.rowSchemeReference(request.userAnswers, waypoints, index, sourcePage).toSeq
      case Some(ErrorCalcTaxFreeLumpSums) =>
        ErrorDescriptionSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
      case Some(BenefitsPaidEarly) =>
        BenefitsPaidEarlySummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
      case Some(RefundOfContributions) =>
        RefundOfContributionsSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
      case Some(OverpaymentOrWriteOff) =>
        ReasonForTheOverpaymentOrWriteOffSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
      case Some(ResidentialPropertyHeld) =>
        ReasonForTheOverpaymentOrWriteOffSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq ++
          ChooseAddressSummary.row(request.userAnswers, waypoints, index, sourcePage, AddressJourneyType.Event1MemberPropertyAddressJourney).toSeq
      case Some(TangibleMoveablePropertyHeld) =>
        MemberTangibleMoveablePropertySummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
      case Some(CourtOrConfiscationOrder) =>
        MemberUnauthorisedPaymentRecipientNameSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
      case Some(MemberOther) =>
        MemberPaymentNatureDescriptionSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
      case _ => Nil
    }

  private def event1EmployerPaymentNatureRows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Int)
                                             (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] =
    request.userAnswers.get(EmployerPaymentNaturePage(index)) match {
      case Some(LoansExceeding50PercentOfFundValue) =>
        LoanDetailsSummary.rowLoanAmount(request.userAnswers, waypoints, index, sourcePage).toSeq ++
          LoanDetailsSummary.rowFundValue(request.userAnswers, waypoints, index, sourcePage).toSeq
      case Some(ResidentialProperty) =>
        ChooseAddressSummary.row(request.userAnswers, waypoints, index, sourcePage, AddressJourneyType.Event1EmployerPropertyAddressJourney).toSeq
      case Some(TangibleMoveableProperty) =>
        EmployerTangibleMoveablePropertySummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
      case Some(CourtOrder) =>
        EmployerUnauthorisedPaymentRecipientNameSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
      case Some(EmployerOther) =>
        EmployerPaymentNatureDescriptionSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
      case _ => Nil
    }

  private def event1PaymentValueAndDateRows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Int)
                                           (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] =
    PaymentValueAndDateSummary.rowPaymentValue(request.userAnswers, waypoints, index, sourcePage).toSeq ++
      PaymentValueAndDateSummary.rowPaymentDate(request.userAnswers, waypoints, index, sourcePage).toSeq
}

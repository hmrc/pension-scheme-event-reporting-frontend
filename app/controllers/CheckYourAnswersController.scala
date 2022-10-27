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
import models.Index
import models.enumeration.EventType.{Event1, Event18, WindUp}
import models.enumeration.{AddressJourneyType, EventType}
import models.event1.WhoReceivedUnauthPayment.Member
import models.requests.DataRequest
import pages.event1.{ValueOfUnauthorisedPaymentPage, WhoReceivedUnauthPaymentPage}
import pages.{CheckAnswersPage, CheckYourAnswersPage, EmptyWaypoints, MembersOrEmployersPage, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.address.checkAnswers.ChooseAddressSummary
import viewmodels.checkAnswers.{Event18ConfirmationSummary, SchemeWindUpDateSummary}
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

  def onPageLoad(eventType: EventType): Action[AnyContent] =
    (identify andThen getData(eventType) andThen requireData) { implicit request =>

      val thisPage = CheckYourAnswersPage(eventType)
      val waypoints = EmptyWaypoints

      val rows = eventType match {
        case WindUp => buildEventWindUpCYARows(waypoints, thisPage)
        case Event18 => buildEvent18CYARows(waypoints, thisPage)
        case _ => Nil
      }

      Ok(view(SummaryListViewModel(rows = rows)))
    }

  def onPageLoadWithIndex(eventType: EventType, index: Index): Action[AnyContent] =
    (identify andThen getData(eventType) andThen requireData) { implicit request =>

      val thisPage = CheckYourAnswersPage.applyWithIndex(eventType, index)
      val waypoints = EmptyWaypoints

      val rows = eventType match {
        case Event1 => buildEvent1CYARows(waypoints, thisPage, index)
        case _ => Nil
      }

      Ok(view(SummaryListViewModel(rows = rows)))
    }


  private def event1MemberJourney(index: Int)(implicit request: DataRequest[AnyContent]): Boolean = {
    request.userAnswers.get(WhoReceivedUnauthPaymentPage(index)) match {
      case Some(Member) => true
      case _ => false
    }

    //    if ((request.userAnswers.data \ "whoReceivedUnauthPayment").as[String] == "member") {
    //      true
    //    }
    //    else {
    //      false
    //    }
  }

  private def schemeUnAuthPaySurchargeRow(waypoints: Waypoints, index: Index, sourcePage: CheckAnswersPage)
                                         (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {


    request.userAnswers.get(ValueOfUnauthorisedPaymentPage(index)) match {
      case Some(true) => SchemeUnAuthPaySurchargeMemberSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
      case _ => Nil
    }

    //    if ((request.userAnswers.data \ "valueOfUnauthorisedPayment").as[Boolean]) {
    //      SchemeUnAuthPaySurchargeMemberSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
    //    }
    //    else {
    //      Nil
    //    }
  }

  // scalastyle:off cyclomatic.complexity
  // scalastyle:off method.length
  private def buildEvent1CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage,
                                 index: Int)(implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    val basicMemberOrEmployerRows = if (event1MemberJourney(index)) {
      MembersDetailsSummary.rowFullName(request.userAnswers, waypoints, index, sourcePage).toSeq ++
        MembersDetailsSummary.rowNino(request.userAnswers, waypoints, index, sourcePage).toSeq ++
        DoYouHoldSignedMandateSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq ++
        ValueOfUnauthorisedPaymentSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq ++
        schemeUnAuthPaySurchargeRow(waypoints, index, sourcePage) ++
        PaymentNatureSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
    } else {
      CompanyDetailsSummary.rowCompanyName(request.userAnswers, waypoints, index, sourcePage).toSeq ++
        CompanyDetailsSummary.rowCompanyNumber(request.userAnswers, waypoints, index, sourcePage).toSeq ++
        ChooseAddressSummary.row(request.userAnswers, waypoints, index, sourcePage,
          AddressJourneyType.Event1EmployerAddressJourney).toSeq ++
        EmployerPaymentNatureSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
    }

    val memberOrEmployerPaymentNatureRows = {
      val paymentNatureKey = request.userAnswers.data.as[String]((MembersOrEmployersPage(index).path \ "paymentNature").read[String])
      paymentNatureKey match {
        case "benefitInKind" =>
          BenefitInKindBriefDescriptionSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
        case "transferToNonRegPensionScheme" =>
          WhoWasTheTransferMadeSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq ++
            SchemeDetailsSummary.rowSchemeName(request.userAnswers, waypoints, index, sourcePage).toSeq ++
            SchemeDetailsSummary.rowSchemeReference(request.userAnswers, waypoints, index, sourcePage).toSeq
        case "errorCalcTaxFreeLumpSums" =>
          ErrorDescriptionSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
        case "benefitsPaidEarly" =>
          BenefitsPaidEarlySummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
        case "refundOfContributions" =>
          RefundOfContributionsSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
        case "overpaymentOrWriteOff" =>
          ReasonForTheOverpaymentOrWriteOffSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
        case "residentialPropertyHeld" =>
          ReasonForTheOverpaymentOrWriteOffSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq ++
            ChooseAddressSummary.row(request.userAnswers, waypoints, index, sourcePage,
              AddressJourneyType.Event1MemberPropertyAddressJourney).toSeq
        case "tangibleMoveablePropertyHeld" =>
          MemberTangibleMoveablePropertySummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
        case "courtOrConfiscationOrder" =>
          MemberUnauthorisedPaymentRecipientNameSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
        case "memberOther" =>
          MemberPaymentNatureDescriptionSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
        case "loansExceeding50PercentOfFundValue" =>
          LoanDetailsSummary.rowLoanAmount(request.userAnswers, waypoints, index, sourcePage).toSeq ++
            LoanDetailsSummary.rowFundValue(request.userAnswers, waypoints, index, sourcePage).toSeq
        case "residentialProperty" =>
          ChooseAddressSummary.row(request.userAnswers, waypoints, index, sourcePage,
            AddressJourneyType.Event1EmployerPropertyAddressJourney).toSeq
        case "tangibleMoveableProperty" =>
          EmployerTangibleMoveablePropertySummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
        case "courtOrder" =>
          EmployerUnauthorisedPaymentRecipientNameSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
        case "employerOther" =>
          EmployerPaymentNatureDescriptionSummary.row(request.userAnswers, waypoints, index, sourcePage).toSeq
        case _ => Nil
      }
    }

    val paymentValueAndDateRows =
      PaymentValueAndDateSummary.rowPaymentValue(request.userAnswers, waypoints, index, sourcePage).toSeq ++
        PaymentValueAndDateSummary.rowPaymentDate(request.userAnswers, waypoints, index, sourcePage).toSeq

    basicMemberOrEmployerRows ++ memberOrEmployerPaymentNatureRows ++ paymentValueAndDateRows
  }

  private def buildEventWindUpCYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage)(implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] =
    SchemeWindUpDateSummary.row(request.userAnswers, waypoints, sourcePage).toSeq

  private def buildEvent18CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage)(implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] =
    Event18ConfirmationSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
}

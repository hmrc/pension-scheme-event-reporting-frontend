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
import models.requests.DataRequest
import pages.{CheckAnswersPage, CheckYourAnswersPage, EmptyWaypoints, Waypoints}
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
    if ((request.userAnswers.data \ "whoReceivedUnauthPayment").as[String] == "member") {
      true
    }
    else {
      false
    }
  }

  private def schemeUnAuthPaySurchargeRow(waypoints: Waypoints, sourcePage: CheckAnswersPage)
                                         (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    if ((request.userAnswers.data \ "valueOfUnauthorisedPayment").as[Boolean]) {
      SchemeUnAuthPaySurchargeMemberSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
    }
    else {
      Nil
    }
  }

  // scalastyle:off
  private def buildEvent1CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage)(implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {

    val basicMemberOrEmployerRows = if (event1MemberJourney) {
      MembersDetailsSummary.rowFullName(request.userAnswers, waypoints, sourcePage).toSeq ++
        MembersDetailsSummary.rowNino(request.userAnswers, waypoints, sourcePage).toSeq ++
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

      val paymentNatureKey = (request.userAnswers.data \ "paymentNature").as[String]

      paymentNatureKey match {
        case "benefitInKind" =>
          BenefitInKindBriefDescriptionSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
        case "transferToNonRegPensionScheme" =>
          WhoWasTheTransferMadeSummary.row(request.userAnswers, waypoints, sourcePage).toSeq ++
            SchemeDetailsSummary.rowSchemeName(request.userAnswers, waypoints, sourcePage).toSeq ++
            SchemeDetailsSummary.rowSchemeReference(request.userAnswers, waypoints, sourcePage).toSeq
        case "errorCalcTaxFreeLumpSums" =>
          ErrorDescriptionSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
        case "benefitsPaidEarly" =>
          BenefitsPaidEarlySummary.row(request.userAnswers, waypoints, sourcePage).toSeq
        case "refundOfContributions" =>
          RefundOfContributionsSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
        case "overpaymentOrWriteOff" =>
          ReasonForTheOverpaymentOrWriteOffSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
        case "residentialPropertyHeld" =>
          ReasonForTheOverpaymentOrWriteOffSummary.row(request.userAnswers, waypoints, sourcePage).toSeq ++
            ChooseAddressSummary.row(request.userAnswers, waypoints, sourcePage, AddressJourneyType.Event1MemberPropertyAddressJourney).toSeq
        case "tangibleMoveablePropertyHeld" =>
          MemberTangibleMoveablePropertySummary.row(request.userAnswers, waypoints, sourcePage).toSeq
        case "courtOrConfiscationOrder" =>
          MemberUnauthorisedPaymentRecipientNameSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
        case "memberOther" =>
          MemberPaymentNatureDescriptionSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
        case "loansExceeding50PercentOfFundValue" =>
          LoanDetailsSummary.rowLoanAmount(request.userAnswers, waypoints, sourcePage).toSeq ++
            LoanDetailsSummary.rowFundValue(request.userAnswers, waypoints, sourcePage).toSeq
        case "residentialProperty" =>
          ChooseAddressSummary.row(request.userAnswers, waypoints, sourcePage, AddressJourneyType.Event1EmployerPropertyAddressJourney).toSeq
        case "tangibleMoveableProperty" =>
          EmployerTangibleMoveablePropertySummary.row(request.userAnswers, waypoints, sourcePage).toSeq
        case "courtOrder" =>
          EmployerUnauthorisedPaymentRecipientNameSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
        case "employerOther" =>
          EmployerPaymentNatureDescriptionSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
        case _ => Nil
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

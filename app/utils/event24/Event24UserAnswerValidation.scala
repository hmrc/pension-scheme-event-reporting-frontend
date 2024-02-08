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

package utils.event24

import models.enumeration.EventType.Event24
import models.event24.TypeOfProtectionGroup1
import models.requests.DataRequest
import models.{Index, MemberSummaryPath}
import pages.EmptyWaypoints
import pages.common.MembersDetailsPage
import pages.event24._
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import services.CompileService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Event24UserAnswerValidation @Inject()(compileService: CompileService) {
  def validateMarginalRate(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val marginalRateAnswer = request.userAnswers.get(MarginalRatePage(index))
    val employerPAYEAnswer = request.userAnswers.get(EmployerPayeReferencePage(index))

    (marginalRateAnswer, employerPAYEAnswer) match {
      case (Some(true), Some(_)) | (Some(false), _) => compileService.compileEvent(Event24, request.pstr, request.userAnswers).map {
        _ =>
          Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event24)).url)
      }
      case (Some(true), None) => Future.successful(
        Redirect(EmployerPayeReferencePage(index).changeLink(EmptyWaypoints, Event24CheckYourAnswersPage(index)).url)
      )
      case _ => Future.successful(
        Redirect(MarginalRatePage(index).changeLink(EmptyWaypoints, Event24CheckYourAnswersPage(index)).url)
      )
    }
  }

  def validateOverAllowanceAndDeathBenefit(index: Index)
                                          (implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val overAllowanceAndDeathBenefitAnswer = request.userAnswers.get(OverAllowanceAndDeathBenefitPage(index))

    overAllowanceAndDeathBenefitAnswer match {
      case Some(true) => validateMarginalRate(index)
      case Some(false) => compileService.compileEvent(Event24, request.pstr, request.userAnswers).map {
        _ =>
          Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event24)).url)
      }
      case _ => Future.successful(
        Redirect(OverAllowanceAndDeathBenefitPage(index).changeLink(EmptyWaypoints, Event24CheckYourAnswersPage(index)).url)
      )
    }
  }

  def validateOverAllowance(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val overAllowanceAnswer = request.userAnswers.get(OverAllowancePage(index))

    overAllowanceAnswer match {
      case Some(true) => validateMarginalRate(index)
      case Some(false) => validateOverAllowanceAndDeathBenefit(index)
      case _ => Future.successful(
        Redirect(OverAllowancePage(index).changeLink(EmptyWaypoints, Event24CheckYourAnswersPage(index)).url)
      )
    }
  }

  def validateSelectProtection(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val selectProtectionAnswer = request.userAnswers.get(TypeOfProtectionGroup1Page(index))
    val protectionReferenceAnswer = request.userAnswers.get(TypeOfProtectionGroup1ReferencePage(index))

    (selectProtectionAnswer, protectionReferenceAnswer) match {
//      case (Some(TypeOfProtectionGroup1.SchemeSpecific), None) | (Some(_), Some(_)) => validateOverAllowance(index)
      case (Some(_), None) => Future.successful(
        Redirect(TypeOfProtectionGroup1ReferencePage(index).changeLink(EmptyWaypoints, Event24CheckYourAnswersPage(index)).url)
      )
      case _ => Future.successful(
        Redirect(pages.event24.TypeOfProtectionGroup1Page(index).changeLink(EmptyWaypoints, Event24CheckYourAnswersPage(index)).url)
      )
    }
  }

  def validateHoldsProtection(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val holdsProtectionAnswer = request.userAnswers.get(ValidProtectionPage(index))

    holdsProtectionAnswer match {
      case Some(true) => validateSelectProtection(index)
      case Some(false) => validateOverAllowance(index)
      case _ => Future.successful(
        Redirect(ValidProtectionPage(index).changeLink(EmptyWaypoints, Event24CheckYourAnswersPage(index)).url)
      )
    }
  }

  def validateAnswers(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val membersDetailsAnswer = request.userAnswers.get(MembersDetailsPage(Event24, index))
    val dateAnswer = request.userAnswers.get(CrystallisedDatePage(index))
    val typeOfBenefitAnswer = request.userAnswers.get(BCETypeSelectionPage(index))
    val amountAnswer = request.userAnswers.get(TotalAmountBenefitCrystallisationPage(index))

    (membersDetailsAnswer, dateAnswer, typeOfBenefitAnswer, amountAnswer) match {
      case (Some(_), Some(_), Some(_), Some(_)) => validateHoldsProtection(index)
      case (Some(_), Some(_), Some(_), None) => Future.successful(
        Redirect(TotalAmountBenefitCrystallisationPage(index).changeLink(EmptyWaypoints, Event24CheckYourAnswersPage(index)).url)
      )
      case (Some(_), Some(_), None, _) => Future.successful(
        Redirect(BCETypeSelectionPage(index).changeLink(EmptyWaypoints, Event24CheckYourAnswersPage(index)).url)
      )
      case (Some(_), None, _, _) => Future.successful(
        Redirect(CrystallisedDatePage(index).changeLink(EmptyWaypoints, Event24CheckYourAnswersPage(index)).url)
      )
      case _ => Future.successful(
        Redirect(MembersDetailsPage(Event24, index).changeLink(EmptyWaypoints, Event24CheckYourAnswersPage(index)).url)
      )
    }
  }
}

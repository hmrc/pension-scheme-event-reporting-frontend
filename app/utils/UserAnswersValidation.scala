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

import models.enumeration.EventType
import models.enumeration.EventType.Event2
import models.requests.DataRequest
import models.{Index, MemberSummaryPath}
import pages.EmptyWaypoints
import pages.common.MembersDetailsPage
import pages.event2.{AmountPaidPage, DatePaidPage}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import services.CompileService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

object UserAnswersValidation {

  def answerValidationAndRedirect(index: Index, eventType: EventType, compileService: CompileService, request: DataRequest[_])
                                 (implicit hc: HeaderCarrier, executor: ExecutionContext): Future[Result] = {
    eventType match {
      case Event2 => event2AnswerValidation(index, compileService, request)
      case _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }

  def event2AnswerValidation(index: Index, compileService: CompileService, request: DataRequest[_])
                            (implicit hc: HeaderCarrier, executor: ExecutionContext): Future[Result] = {
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
          Redirect(controllers.common.routes.MembersDetailsController.onPageLoad(
            EmptyWaypoints, Event2, index, Event2MemberPageNumbers.FIRST_PAGE_DECEASED
          ))
        )
      case (Some(_), None, _, _) => Future.successful(
        Redirect(controllers.common.routes.MembersDetailsController.onPageLoad(
          EmptyWaypoints, Event2, index, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY
        ))
      )
      case (Some(_), Some(_), None, _) => Future.successful(
        Redirect(controllers.event2.routes.AmountPaidController.onPageLoad(EmptyWaypoints, index))
      )
      case (Some(_), Some(_), Some(_), None) => Future.successful(Redirect(
        controllers.event2.routes.DatePaidController.onPageLoad(EmptyWaypoints, index)
      ))
    }
  }
}

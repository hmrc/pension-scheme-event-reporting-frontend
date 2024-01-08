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

package utils.event2

import models.{Index, MemberSummaryPath}
import models.enumeration.EventType.Event2
import models.requests.DataRequest
import pages.EmptyWaypoints
import pages.common.MembersDetailsPage
import pages.event2.{AmountPaidPage, DatePaidPage, Event2CheckYourAnswersPage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import services.CompileService
import uk.gov.hmrc.http.HeaderCarrier
import utils.Event2MemberPageNumbers

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Event2UserAnswerValidation @Inject()(compileService: CompileService) {
  def validateAnswers(index: Index)
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
}

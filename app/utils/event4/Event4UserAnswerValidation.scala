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

package utils.event4

import models.{Index, MemberSummaryPath}
import models.enumeration.EventType.Event4
import models.requests.DataRequest
import pages.EmptyWaypoints
import pages.common.{MembersDetailsPage, PaymentDetailsPage}
import pages.event4.Event4CheckYourAnswersPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import services.CompileService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Event4UserAnswerValidation @Inject()(compileService: CompileService) {
  def validateAnswers(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
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
}

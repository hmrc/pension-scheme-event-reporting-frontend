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

package utils.event8A

import models.{Index, MemberSummaryPath}
import models.enumeration.EventType.Event8A
import models.event8a.PaymentType
import models.requests.DataRequest
import pages.EmptyWaypoints
import pages.common.MembersDetailsPage
import pages.event8.LumpSumAmountAndDatePage
import pages.event8a.{Event8ACheckYourAnswersPage, PaymentTypePage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import services.CompileService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Event8AUserAnswerValidation @Inject()(compileService: CompileService) {
  def validateAnswers(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
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
        Redirect(LumpSumAmountAndDatePage(Event8A, index).changeLink(EmptyWaypoints, Event8ACheckYourAnswersPage(index)).url)
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
}

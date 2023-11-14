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

package utils.event8

import models.{Index, MemberSummaryPath}
import models.enumeration.EventType.Event8
import models.requests.DataRequest
import pages.EmptyWaypoints
import pages.common.MembersDetailsPage
import pages.event8.{Event8CheckYourAnswersPage, LumpSumAmountAndDatePage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import services.CompileService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Event8UserAnswerValidation @Inject()(compileService: CompileService) {
  def validateAnswers(index: Index)(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val membersDetailsAnswer = request.userAnswers.get(MembersDetailsPage(Event8, index))
    val typeOfProtectionAnswer = request.userAnswers.get(pages.event8.TypeOfProtectionPage(Event8, index))
    val typeOfProtectionReferenceAnswer = request.userAnswers.get(pages.event8.TypeOfProtectionReferencePage(Event8, index))
    val lumpSumDetailsAnswer = request.userAnswers.get(LumpSumAmountAndDatePage(Event8, index))

    (membersDetailsAnswer, typeOfProtectionAnswer, typeOfProtectionReferenceAnswer, lumpSumDetailsAnswer) match {
      case (Some(_), Some(_), Some(_), Some(_)) => compileService.compileEvent(Event8, request.pstr, request.userAnswers).map {
        _ =>
          Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event8)).url)
      }
      case (Some(_), Some(_), Some(_), None) => Future.successful(
        Redirect(LumpSumAmountAndDatePage(Event8, index).changeLink(EmptyWaypoints, Event8CheckYourAnswersPage(index)).url)
      )
      case (Some(_), Some(_), None, _) => Future.successful(
        Redirect(pages.event8.TypeOfProtectionReferencePage(Event8, index).changeLink(EmptyWaypoints, Event8CheckYourAnswersPage(index)).url)
      )
      case (Some(_), None, _, _) => Future.successful(
        Redirect(pages.event8.TypeOfProtectionPage(Event8, index).changeLink(EmptyWaypoints, Event8CheckYourAnswersPage(index)).url)
      )
      case _ => Future.successful(
        Redirect(MembersDetailsPage(Event8, index).changeLink(EmptyWaypoints, Event8CheckYourAnswersPage(index)).url)
      )
    }
  }
}

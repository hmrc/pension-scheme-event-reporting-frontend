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

package utils.event12

import models.enumeration.EventType.Event12
import models.requests.DataRequest
import pages.EmptyWaypoints
import pages.event12.{DateOfChangePage, Event12CheckYourAnswersPage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import services.CompileService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Event12UserAnswerValidation @Inject()(compileService: CompileService) {
  def validateAnswers()(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val hasSchemeChangedRulesAnswer = request.userAnswers.get(pages.event12.HasSchemeChangedRulesPage)
    val dateOfChangeAnswer = request.userAnswers.get(DateOfChangePage)

    (hasSchemeChangedRulesAnswer, dateOfChangeAnswer) match {
      case (Some(true), Some(_)) => compileService.compileEvent(Event12, request.pstr, request.userAnswers).map {
        _ =>
          Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)
      }
      case (Some(true), None) => Future.successful(
        Redirect(DateOfChangePage.changeLink(EmptyWaypoints, Event12CheckYourAnswersPage()).url)
      )
      case _ => Future.successful(
        Redirect(pages.event12.HasSchemeChangedRulesPage.changeLink(EmptyWaypoints, Event12CheckYourAnswersPage()).url)
      )
    }
  }
}

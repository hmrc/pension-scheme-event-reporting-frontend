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

package utils.event14

import models.enumeration.EventType.Event14
import models.requests.DataRequest
import pages.EmptyWaypoints
import pages.event14.{Event14CheckYourAnswersPage, HowManySchemeMembersPage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import services.CompileService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Event14UserAnswerValidation @Inject()(compileService: CompileService) {
  def validateAnswers()(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val noOfSchemeMembersAnswer = request.userAnswers.get(HowManySchemeMembersPage)

    noOfSchemeMembersAnswer match {
      case Some(_) => compileService.compileEvent(Event14, request.pstr, request.userAnswers).map {
        _ =>
          Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)
      }
      case _ => Future.successful(
        Redirect(HowManySchemeMembersPage.changeLink(EmptyWaypoints, Event14CheckYourAnswersPage).url)
      )
    }
  }
}

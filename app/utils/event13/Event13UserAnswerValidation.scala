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

package utils.event13

import models.enumeration.EventType.Event13
import models.event13.SchemeStructure
import models.requests.DataRequest
import pages.EmptyWaypoints
import pages.event13.{ChangeDatePage, Event13CheckYourAnswersPage, SchemeStructureDescriptionPage, SchemeStructurePage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import services.CompileService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Event13UserAnswerValidation @Inject()(compileService: CompileService) {
  def validateAnswers()(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val schemeStructureAnswer = request.userAnswers.get(SchemeStructurePage)
    val schemeDescriptionAnswer = request.userAnswers.get(SchemeStructureDescriptionPage)
    val changeDateAnswer = request.userAnswers.get(ChangeDatePage)

    (schemeStructureAnswer, schemeDescriptionAnswer, changeDateAnswer) match {
      case (Some(SchemeStructure.Other), Some(_), Some(_)) =>
        compileService.compileEvent(Event13, request.pstr, request.userAnswers).map {
          _ =>
            Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)

        }
      case (Some(_), _, None) => Future.successful(
        Redirect(ChangeDatePage.changeLink(EmptyWaypoints, Event13CheckYourAnswersPage()).url)
      )
      case (Some(SchemeStructure.Other), None, _) => Future.successful(
        Redirect(SchemeStructureDescriptionPage.changeLink(EmptyWaypoints, Event13CheckYourAnswersPage()).url)
      )
      case (Some(_), _, Some(_)) => {
        compileService.compileEvent(Event13, request.pstr, request.userAnswers).map {
          _ =>
            Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)

        }
      }
      case _ => Future.successful(
        Redirect(SchemeStructurePage.changeLink(EmptyWaypoints, Event13CheckYourAnswersPage()).url)
      )
    }
  }
}

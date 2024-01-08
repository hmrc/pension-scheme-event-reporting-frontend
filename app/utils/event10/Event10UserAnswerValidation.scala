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

package utils.event10

import models.enumeration.EventType.Event10
import models.event10.BecomeOrCeaseScheme.{ItBecameAnInvestmentRegulatedPensionScheme, ItHasCeasedToBeAnInvestmentRegulatedPensionScheme}
import models.requests.DataRequest
import pages.EmptyWaypoints
import pages.event10.{BecomeOrCeaseSchemePage, ContractsOrPoliciesPage, Event10CheckYourAnswersPage, SchemeChangeDatePage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import services.CompileService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Event10UserAnswerValidation @Inject()(compileService: CompileService) {
  def validateAnswers()(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val becomeOrCeaseAnswer = request.userAnswers.get(BecomeOrCeaseSchemePage)
    val schemeChangeDateAnswer = request.userAnswers.get(SchemeChangeDatePage)

    val contractsOrPoliciesAnswer = request.userAnswers.get(ContractsOrPoliciesPage)

    (becomeOrCeaseAnswer, schemeChangeDateAnswer, contractsOrPoliciesAnswer) match {
      case (Some(ItBecameAnInvestmentRegulatedPensionScheme), Some(_), Some(_)) |
           (Some(ItHasCeasedToBeAnInvestmentRegulatedPensionScheme), Some(_), _) =>
        compileService.compileEvent(Event10, request.pstr, request.userAnswers).map { _ =>
          Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)
        }
      case (Some(_), None, _) => Future.successful(
        Redirect(SchemeChangeDatePage.changeLink(EmptyWaypoints, Event10CheckYourAnswersPage()).url)
      )
      case (Some(ItBecameAnInvestmentRegulatedPensionScheme), Some(_), None) => Future.successful(
        Redirect(ContractsOrPoliciesPage.changeLink(EmptyWaypoints, Event10CheckYourAnswersPage()).url)
      )
      case _ => Future.successful(
        Redirect(BecomeOrCeaseSchemePage.changeLink(EmptyWaypoints, Event10CheckYourAnswersPage()).url)
      )

    }
  }
}

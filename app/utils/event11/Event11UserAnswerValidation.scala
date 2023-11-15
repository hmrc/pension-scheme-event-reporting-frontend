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

package utils.event11

import models.enumeration.EventType.Event11
import models.requests.DataRequest
import pages.EmptyWaypoints
import pages.event11._
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import services.CompileService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Event11UserAnswerValidation @Inject()(compileService: CompileService) {
  def validateAnswers()(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val hasSchemeChangedRulesAnswer = request.userAnswers.get(HasSchemeChangedRulesPage)
    val unAuthPaymentsRuleChangeDateAnswer = request.userAnswers.get(UnAuthPaymentsRuleChangeDatePage)
    val hasSchemeChangedRulesInvestmentsInAssetsAnswer = request.userAnswers.get(HasSchemeChangedRulesInvestmentsInAssetsPage)
    val investmentsInAssetsRuleChangeDateAnswer = request.userAnswers.get(InvestmentsInAssetsRuleChangeDatePage)

    def hasSchemeChangedRulesInvestmentsInAssetsValidation: Future[Result] = {
      (hasSchemeChangedRulesInvestmentsInAssetsAnswer, investmentsInAssetsRuleChangeDateAnswer) match {
        case (Some(true), Some(_)) | (Some(false), _) => compileService.compileEvent(Event11, request.pstr, request.userAnswers).map {
          _ =>
            Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)
        }
        case (Some(true), None) => Future.successful(
          Redirect(InvestmentsInAssetsRuleChangeDatePage.changeLink(EmptyWaypoints, Event11CheckYourAnswersPage()).url)
        )
        case _ => Future.successful(
          Redirect(HasSchemeChangedRulesInvestmentsInAssetsPage.changeLink(EmptyWaypoints, Event11CheckYourAnswersPage()).url)
        )
      }
    }

    (hasSchemeChangedRulesAnswer, unAuthPaymentsRuleChangeDateAnswer) match {
      case (Some(true), Some(_)) | (Some(false), _) => hasSchemeChangedRulesInvestmentsInAssetsValidation
      case (Some(true), None) => Future.successful(
        Redirect(UnAuthPaymentsRuleChangeDatePage.changeLink(EmptyWaypoints, Event11CheckYourAnswersPage()).url)
      )
      case _ => Future.successful(
        Redirect(HasSchemeChangedRulesPage.changeLink(EmptyWaypoints, Event11CheckYourAnswersPage()).url)
      )
    }
  }
}

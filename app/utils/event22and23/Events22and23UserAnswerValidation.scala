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

package utils.event22and23

import models.{Index, MemberSummaryPath}
import models.TaxYear.getTaxYear
import models.common.ChooseTaxYear
import models.enumeration.EventType
import models.enumeration.EventType.{Event22, Event23}
import models.requests.DataRequest
import pages.EmptyWaypoints
import pages.common.{ChooseTaxYearPage, MembersDetailsPage, TotalPensionAmountsPage}
import pages.event22.Event22CheckYourAnswersPage
import pages.event23.Event23CheckYourAnswersPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import services.CompileService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Events22and23UserAnswerValidation @Inject()(compileService: CompileService) {
  def validateAnswers(index: Index, eventType: EventType)
                                  (implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {

    val membersDetailsAnswer = request.userAnswers.get(MembersDetailsPage(eventType, index))
    val taxYearChosen = getTaxYear(request.userAnswers)
    val rdsTaxYear = ChooseTaxYear.reads(ChooseTaxYear.enumerable(taxYearChosen))
    val taxYearAnswer = request.userAnswers.get(ChooseTaxYearPage(eventType, index))(rdsTaxYear)
    val pensionAmountsAnswer = request.userAnswers.get(TotalPensionAmountsPage(eventType, index))

    def validateEvent22 = {
      (membersDetailsAnswer, taxYearAnswer, pensionAmountsAnswer) match {
        case (Some(_), Some(_), Some(_)) => compileService.compileEvent(Event22, request.pstr, request.userAnswers).map {
          _ =>
            Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event22)).url)
        }
        case (Some(_), Some(_), None) => Future.successful(
          Redirect(TotalPensionAmountsPage(Event22, index).changeLink(EmptyWaypoints, Event22CheckYourAnswersPage(index)).url)
        )
        case (Some(_), None, _) => Future.successful(
          Redirect(ChooseTaxYearPage(Event22, index).changeLink(EmptyWaypoints, Event22CheckYourAnswersPage(index)).url)
        )
        case _ => Future.successful(
          Redirect(MembersDetailsPage(Event22, index).changeLink(EmptyWaypoints, Event22CheckYourAnswersPage(index)).url)
        )
      }
    }

    def validateEvent23 = {
      (membersDetailsAnswer, taxYearAnswer, pensionAmountsAnswer) match {
        case (Some(_), Some(_), Some(_)) => compileService.compileEvent(Event23, request.pstr, request.userAnswers).map {
          _ =>
            Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event23)).url)
        }
        case (Some(_), Some(_), None) => Future.successful(
          Redirect(TotalPensionAmountsPage(Event23, index).changeLink(EmptyWaypoints, Event23CheckYourAnswersPage(index)).url)
        )
        case (Some(_), None, _) => Future.successful(
          Redirect(ChooseTaxYearPage(Event23, index).changeLink(EmptyWaypoints, Event23CheckYourAnswersPage(index)).url)
        )
        case _ => Future.successful(
          Redirect(MembersDetailsPage(Event23, index).changeLink(EmptyWaypoints, Event23CheckYourAnswersPage(index)).url)
        )
      }
    }

    eventType match {
      case Event22 => validateEvent22
      case _ => validateEvent23
    }
  }
}

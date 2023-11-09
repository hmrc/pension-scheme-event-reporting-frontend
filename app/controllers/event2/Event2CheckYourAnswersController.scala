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

package controllers.event2

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import helpers.ReadOnlyCYA
import models.enumeration.EventType.Event2
import models.requests.DataRequest
import models.{Index, MemberSummaryPath}
import pages.common.MembersDetailsPage
import pages.event2.{AmountPaidPage, DatePaidPage, Event2CheckYourAnswersPage}
import pages.{CheckAnswersPage, EmptyWaypoints, QuestionPage, VersionInfoPage, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Reads
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CompileService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Event2MemberPageNumbers
import utils.MissingAnswersCheck.checkAnAnswerIsPresent
import viewmodels.checkAnswers.MembersDetailsSummary
import viewmodels.event2.checkAnswers.{AmountPaidSummary, DatePaidSummary}
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class Event2CheckYourAnswersController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  compileService: CompileService,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: CheckYourAnswersView
                                                )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(index: Index): Action[AnyContent] =
    (identify andThen getData(Event2) andThen requireData) { implicit request =>
      val thisPage = Event2CheckYourAnswersPage(index)
      val waypoints = EmptyWaypoints
      val continueUrl = controllers.event2.routes.Event2CheckYourAnswersController.onClick(index).url
      val version = request.userAnswers.get(VersionInfoPage).map(_.version)
      val readOnlyHeading = ReadOnlyCYA.readOnlyHeading(Event2, version, request.readOnly())
      Ok(view(SummaryListViewModel(rows = buildEvent2CYARows(waypoints, thisPage, index)), continueUrl, readOnlyHeading))
    }

  def onClick(index: Index)(implicit request: DataRequest[AnyContent], rds: Reads[Any]): Action[AnyContent] =
    (identify andThen getData(Event2) andThen requireData).async { implicit request =>
      val pagesControllerPairs: Map[QuestionPage[_], String] = Map(
        MembersDetailsPage(Event2, index, Event2MemberPageNumbers.FIRST_PAGE_DECEASED) ->
          controllers.common.routes.MembersDetailsController.onPageLoad(EmptyWaypoints, Event2, index, Event2MemberPageNumbers.FIRST_PAGE_DECEASED).url,
        MembersDetailsPage(Event2, index, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY) ->
          controllers.common.routes.MembersDetailsController.onPageLoad(EmptyWaypoints, Event2, index, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY).url,
        AmountPaidPage(index, Event2) -> controllers.event2.routes.AmountPaidController.onPageLoad(EmptyWaypoints, index).url,
        DatePaidPage(index, Event2) -> controllers.event2.routes.DatePaidController.onPageLoad(EmptyWaypoints, index).url)

      if (checkAllAnswersPresent(pagesControllerPairs).isEmpty) {
        compileService.compileEvent(Event2, request.pstr, request.userAnswers).map {
          _ =>
            Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event2)).url)
        }
      } else {
        val missingAnswersPage = checkAllAnswersPresent(pagesControllerPairs).head
        Future.successful(Redirect(getUrl(missingAnswersPage, index)))
      }
    }

  private def buildEvent2CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Index)
                                (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    MembersDetailsSummary.rowFullName(request.userAnswers, waypoints, index, sourcePage, request.readOnly(),
      Event2, Event2MemberPageNumbers.FIRST_PAGE_DECEASED).toSeq ++
      MembersDetailsSummary.rowNino(request.userAnswers, waypoints, index, sourcePage, request.readOnly(),
        Event2, Event2MemberPageNumbers.FIRST_PAGE_DECEASED).toSeq ++
      MembersDetailsSummary.rowFullName(request.userAnswers, waypoints, index, sourcePage, request.readOnly(),
        Event2, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY).toSeq ++
      MembersDetailsSummary.rowNino(request.userAnswers, waypoints, index, sourcePage, request.readOnly(),
        Event2, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY).toSeq ++
      AmountPaidSummary.row(request.userAnswers, waypoints, sourcePage, request.readOnly(), index).toSeq ++
      DatePaidSummary.row(request.userAnswers, waypoints, sourcePage, request.readOnly(), index).toSeq
  }

//  private def checkAllAnswersPresent(index: Index)(implicit request: DataRequest[AnyContent]): Seq[QuestionPage[_]] = {
//    Seq(checkAnAnswerIsPresent(MembersDetailsPage(Event2, index, Event2MemberPageNumbers.FIRST_PAGE_DECEASED)),
//      checkAnAnswerIsPresent(MembersDetailsPage(Event2, index, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY)),
//      checkAnAnswerIsPresent(AmountPaidPage(index, Event2)),
//      checkAnAnswerIsPresent(DatePaidPage(index, Event2))
//    ).flatten
//  }

  private def checkAllAnswersPresent(pagesControllerPairs: Map[QuestionPage[_], String])
                                    (implicit request: DataRequest[AnyContent], reads: Reads[Any]): Seq[QuestionPage[_]] = {
    val pages = pagesControllerPairs.keys.toSeq
    pages.flatMap(page => checkAnAnswerIsPresent(page))
  }

  private def getUrl[A](page: QuestionPage[A], index: Index) = {
    page match {
      case MembersDetailsPage(_, _, 1) =>
        controllers.common.routes.MembersDetailsController.onPageLoad(EmptyWaypoints, Event2, index, Event2MemberPageNumbers.FIRST_PAGE_DECEASED).url
      case MembersDetailsPage(_, _, 2) =>
        controllers.common.routes.MembersDetailsController.onPageLoad(EmptyWaypoints, Event2, index, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY).url
      case AmountPaidPage(_, _) => controllers.event2.routes.AmountPaidController.onPageLoad(EmptyWaypoints, index).url
      case DatePaidPage(_, _) => controllers.event2.routes.DatePaidController.onPageLoad(EmptyWaypoints, index).url
    }
  }
}

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

package controllers

import connectors.EventReportingConnector
import controllers.actions._
import forms.EventSummaryFormProvider
import models.UserAnswers
import models.enumeration.EventType
import models.enumeration.EventType.{Event22, Event23}
import models.requests.{DataRequest, IdentifierRequest}
import pages.{EmptyWaypoints, EventSummaryPage, Waypoints}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.Message
import views.html.EventSummaryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EventSummaryController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        connector: EventReportingConnector,
                                        formProvider: EventSummaryFormProvider,
                                        view: EventSummaryView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  private def summaryListRows(waypoints: Waypoints)(implicit request: DataRequest[AnyContent]): Future[Seq[SummaryListRow]] = {
    connector.getEventReportSummary(request.pstr, "21/01/22").map { seqOfEventTypes =>
      seqOfEventTypes.map { event =>
        SummaryListRow(
          key = Key(
            content = Text(Message(s"eventSummary.event${event.toString}"))
          ),
          actions = Some(Actions(
            items = Seq(
              ActionItem(
                content = Text(Message("site.change")),
                href = changeLinkForEvent(event)
              ),
              ActionItem(
                content = Text(Message("site.remove")),
                href = event match {
                  case EventType.Event18 => controllers.event18.routes.RemoveEvent18Controller.onPageLoad(waypoints).url
                  case _ => "#"
                }
              )
            )
          ))
        )
      }
    }
  }

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async { implicit request =>
    summaryListRows(waypoints).map { rows =>
      Ok(view(form, waypoints, rows))
    }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => summaryListRows(waypoints).map(rows => BadRequest(view(formWithErrors, waypoints, rows))),
        value => {
          val userAnswerUpdated = UserAnswers().setOrException(EventSummaryPage, value)
          Future.successful(Redirect(EventSummaryPage.navigate(waypoints, userAnswerUpdated, userAnswerUpdated).route))
        }
      )
  }

  private def changeLinkForEvent(eventType: EventType): String = {
    eventType match {
      case Event22 => controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, Event22).url
      case Event23 => controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, Event23).url
      case _ => "#"
    }
  }
}

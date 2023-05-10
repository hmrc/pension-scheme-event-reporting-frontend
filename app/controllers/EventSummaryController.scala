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
import models.TaxYear.getSelectedTaxYearAsString
import models.UserAnswers
import models.enumeration.EventType
import pages.{EmptyWaypoints, EventSummaryPage, Waypoints, TaxYearPage}
import models.requests.DataRequest
import play.api.Logger
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
  private val logger = Logger(classOf[EventSummaryController])

  private def summaryListRows(implicit request: DataRequest[AnyContent]): Future[Seq[SummaryListRow]] = {
    request.userAnswers.get(TaxYearPage) match {
      case Some(taxYear) =>
        val startYear = s"${taxYear.startYear}-04-06"
        connector.getEventReportSummary(request.pstr, startYear).map { seqOfEventTypes =>
          seqOfEventTypes.map { event =>
            SummaryListRow(
              key = Key(
                content = Text(Message(s"eventSummary.event${event.toString}"))
              ),
              actions = Some(Actions(
                items = Seq(
                  changeLinkForEvent(event).map { link =>  ActionItem(
                    content = Text(Message("site.change")),
                    href = link
                  )},
                  Some(ActionItem(
                    content = Text(Message("site.remove")),
                    href = removeLinkForEvent(event)
                  ))
                ).flatten
              ))
            )
          }
        }

      case _ =>
        logger.warn("No tax year selected on load of summary page")
        Future.successful(Nil)
    }
  }

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async { implicit request =>
    summaryListRows.map { rows =>
      val selectedTaxYear = getSelectedTaxYearAsString(request.userAnswers)
      Ok(view(form, waypoints, rows, selectedTaxYear))
    }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      val selectedTaxYear = getSelectedTaxYearAsString(request.userAnswers)
      form.bindFromRequest().fold(
        formWithErrors => summaryListRows.map(rows => BadRequest(view(formWithErrors, waypoints, rows, selectedTaxYear))),
        value => {
          val originalUserAnswers = UserAnswers()
          val updatedUserAnswers = originalUserAnswers.setOrException(EventSummaryPage, value)
          Future.successful(Redirect(EventSummaryPage.navigate(waypoints,originalUserAnswers, updatedUserAnswers).route))
        }
      )
  }

  private def changeLinkForEvent(eventType: EventType): Option[String] = {
    println(eventType)
    eventType match {
      //case EventType.Event1 => ???
      case EventType.Event2 | EventType.Event3 |
           EventType.Event4 | EventType.Event5 |
           EventType.Event6 | EventType.Event8 |
           EventType.Event8A | EventType.Event22 |
           EventType.Event23 => Some(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event7 => Some(controllers.event7.routes.Event7MembersSummaryController.onPageLoad(EmptyWaypoints).url)
      case EventType.Event13 => Some(controllers.event13.routes.Event13CheckYourAnswersController.onPageLoad.url)
      case EventType.Event18 => None
      case _ =>
        logger.error(s"Missing event type $eventType")
        None
    }
  }

  private def removeLinkForEvent(eventType: EventType): String = {
    eventType match {
      case EventType.Event18 => controllers.event18.routes.RemoveEvent18Controller.onPageLoad(EmptyWaypoints).url
      case _ => "#"
    }
  }
}

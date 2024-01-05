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

package controllers

import connectors.EventReportingConnector
import controllers.actions._
import forms.EventSummaryFormProvider
import models.TaxYear.getSelectedTaxYearAsString
import models.{EventSummary, MemberSummaryPath, UserAnswers}
import models.enumeration.EventType
import models.enumeration.EventType.{Event18, Event20A, Event8A, WindUp}
import models.requests.DataRequest
import pages.{EmptyWaypoints, EventSummaryPage, TaxYearPage, VersionInfoPage, Waypoints}
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

  private val sortExpr: EventSummary => Int = es => {
    es.eventType match {
      case WindUp => 99
      case Event8A => 9
      case Event20A => 21
      case e => e.toString.toInt
    }
  }

  private def summaryListRows(implicit request: DataRequest[AnyContent]): Future[Seq[SummaryListRow]] = {
    (request.userAnswers.get(TaxYearPage), request.userAnswers.get(VersionInfoPage)) match {
      case (Some(taxYear), Some(versionInfo)) =>
        val startYear = s"${taxYear.startYear}-04-06"
        connector.getEventReportSummary(request.pstr, startYear, versionInfo.version)
          .map { seqOfEventTypes =>
            seqOfEventTypes.sortBy(sortExpr).map { eventSummary =>
              SummaryListRow(
                key = Key(
                  content = {
                    val lockedHtml = eventSummary.lockedBy.map(lockedBy => "<br/>" + Message("eventSummary.lockedBy").resolve + " " + lockedBy).getOrElse("")
                    def notBold(txt: String) = s"""<span style="font-weight:400;">$txt</span>"""
                    HtmlContent(Text(Message(s"eventSummary.event${eventSummary.eventType.toString}")).asHtml.toString() + notBold(lockedHtml))
                  }
                ),
                actions = Some(Actions(
                  items = if (request.readOnly()) {
                    Seq(
                      viewOnlyLinkForEvent(eventSummary.eventType).map { link =>
                        ActionItem(
                          content = Text(Message("site.view")),
                          href = link
                        )
                      }
                    ).flatten
                  } else {
                    Seq(
                      changeLinkForEvent(eventSummary.eventType, eventSummary.lockedBy.isDefined).map { link =>
                        ActionItem(
                          content = Text(Message("site.change")),
                          href = link
                        )
                      },
                      removeLinkForEvent(eventSummary.eventType, eventSummary.lockedBy.isDefined).map { link =>
                        ActionItem(
                          content = Text(Message("site.remove")),
                          href = link
                        )
                      }
                    ).flatten
                  }
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
    val version = request.userAnswers.get(VersionInfoPage).map(_.version)
    summaryListRows.map { rows =>
      val schemeName = request.schemeName
      val selectedTaxYear = getSelectedTaxYearAsString(request.userAnswers)
      Ok(view(form, waypoints, rows, selectedTaxYear, schemeName, version))
    }
  }

  def onPageLoadForEvent18ViewOnlyLink(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async { implicit request =>
    val version = request.userAnswers.get(VersionInfoPage).map(_.version)
    val schemeName = request.schemeName
    val selectedTaxYear = getSelectedTaxYearAsString(request.userAnswers)
    val t = Seq(SummaryListRow(
      key = Key(content = Text(Message("eventSummary.event18"))),
      actions = Some(Actions(
        items = Seq(
          removeLinkForEvent(Event18, false).map { link =>
            ActionItem(
              content = Text(Message("site.remove")),
              href = link
            )
          }
        ).flatten
      ))
    ))
    Future.successful(Ok(view(form, waypoints, t, selectedTaxYear, schemeName, version)))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      val version = request.userAnswers.get(VersionInfoPage).map(_.version)
      val selectedTaxYear = getSelectedTaxYearAsString(request.userAnswers)
      val schemeName = request.schemeName
      form.bindFromRequest().fold(
        formWithErrors => summaryListRows.map(rows => BadRequest(view(formWithErrors, waypoints, rows, selectedTaxYear, schemeName, version))),
        value => {
          val originalUserAnswers = UserAnswers()
          val updatedUserAnswers = originalUserAnswers.setOrException(EventSummaryPage, value)
          Future.successful(Redirect(EventSummaryPage.navigate(waypoints, originalUserAnswers, updatedUserAnswers).route))
        }
      )
  }

  private def changeLinkForEvent(eventType: EventType, locked: Boolean): Option[String] = {
    if(locked) return None
    eventType match {
      case EventType.Event1 => Some(controllers.event1.routes.UnauthPaymentSummaryController.onPageLoad(EmptyWaypoints).url)
      case EventType.Event2 | EventType.Event3 |
           EventType.Event4 | EventType.Event5 |
           EventType.Event6 | EventType.Event8 |
           EventType.Event8A | EventType.Event22 |
           EventType.Event23 | EventType.Event24 =>
        Some(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(eventType)).url)
      case EventType.Event7 => Some(controllers.event7.routes.Event7MembersSummaryController.onPageLoad(EmptyWaypoints).url)
      case EventType.Event10 => Some(controllers.event10.routes.Event10CheckYourAnswersController.onPageLoad.url)
      case EventType.Event11 => Some(controllers.event11.routes.Event11CheckYourAnswersController.onPageLoad.url)
      case EventType.Event12 => Some(controllers.event12.routes.Event12CheckYourAnswersController.onPageLoad.url)
      case EventType.Event13 => Some(controllers.event13.routes.Event13CheckYourAnswersController.onPageLoad.url)
      case EventType.Event14 => None
      case EventType.Event18 => None
      case EventType.Event19 => Some(controllers.event19.routes.Event19CheckYourAnswersController.onPageLoad.url)
      case EventType.Event20 => Some(controllers.event20.routes.Event20CheckYourAnswersController.onPageLoad.url)
      case EventType.WindUp => None
      case _ =>
        logger.error(s"Missing event type $eventType")
        None
    }
  }

  private def removeLinkForEvent(eventType: EventType, locked: Boolean): Option[String] = {
    if(locked) return None
    eventType match {
      case EventType.Event1 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event2 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event3 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event4 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event5 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event6 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event7 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event8 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event8A => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event10 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event11 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event12 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event13 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event14 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event18 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event19 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event20 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event20A => None
      case EventType.Event22 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event23 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.Event24 => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case EventType.WindUp => Some(controllers.common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, eventType).url)
      case _ =>
        logger.info(s"Missing event type $eventType")
        None
    }
  }

  private def viewOnlyLinkForEvent(eventType: EventType): Option[String] = {
    eventType match {
      case EventType.Event1 => Some(controllers.event1.routes.UnauthPaymentSummaryController.onPageLoad(EmptyWaypoints).url)
      case EventType.Event2 | EventType.Event3 |
           EventType.Event4 | EventType.Event5 |
           EventType.Event6 | EventType.Event8 |
           EventType.Event8A | EventType.Event22 |
           EventType.Event23 | EventType.Event24 =>
        Some(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(eventType)).url)
      case EventType.Event7 => Some(controllers.event7.routes.Event7MembersSummaryController.onPageLoad(EmptyWaypoints).url)
      case EventType.Event10 => Some(controllers.event10.routes.Event10CheckYourAnswersController.onPageLoad.url)
      case EventType.Event11 => Some(controllers.event11.routes.Event11CheckYourAnswersController.onPageLoad.url)
      case EventType.Event12 => Some(controllers.event12.routes.Event12CheckYourAnswersController.onPageLoad.url)
      case EventType.Event13 => Some(controllers.event13.routes.Event13CheckYourAnswersController.onPageLoad.url)
      case EventType.Event14 => Some(controllers.event14.routes.Event14CheckYourAnswersController.onPageLoad().url)
      case EventType.Event18 => Some(controllers.routes.EventSummaryController.onPageLoadForEvent18ViewOnlyLink(EmptyWaypoints).url)
      case EventType.Event19 => Some(controllers.event19.routes.Event19CheckYourAnswersController.onPageLoad.url)
      case EventType.Event20 => Some(controllers.event20.routes.Event20CheckYourAnswersController.onPageLoad.url)
      case EventType.WindUp => Some(controllers.eventWindUp.routes.EventWindUpCheckYourAnswersController.onPageLoad().url)
      case _ =>
        logger.error(s"Missing event type $eventType")
        None
    }
  }
}

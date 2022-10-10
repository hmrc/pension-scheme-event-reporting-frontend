/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import controllers.actions._
import forms.EventSummaryFormProvider
import models.enumeration.EventType
import pages.{EventSummaryPage, Waypoints}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Key, SummaryListRow, Text, Value}
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
                                        userAnswersCacheConnector: UserAnswersCacheConnector,
                                        formProvider: EventSummaryFormProvider,
                                        view: EventSummaryView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  private val eventType = EventType.Event1

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = identify.async { implicit request =>
    connector.getEventReportSummary(request.pstr).map{ seqOfEventTypes =>
      val mappedEvents = seqOfEventTypes.map{ event =>
        SummaryListRow(
          key = Key(
            content = Text("Event"),
            classes = "govuk-visually-hidden"
          ),
          value = Value(
            content = Text(Message(s"eventSummary.event${event.toString}")),
            classes = "govuk-!-font-weight-bold govuk-!-width-full"
          ),
          actions = Some(Actions(
            items = Seq(
              ActionItem(
                href = "",
                content = Text("Change"),
                visuallyHiddenText = Some(Message(s"eventSummary.event${event.toString}"))
              )
            )
          ))
        )
      }
    Ok(view(form, waypoints, mappedEvents))
    }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, waypoints, Nil))),
        value => {
          val originalUserAnswers = request.userAnswers
          val updatedAnswers = originalUserAnswers.setOrException(EventSummaryPage, value)
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
          Redirect(EventSummaryPage.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
        }
      }
    )
  }
}

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

import audit.{AuditService, StartNewERAuditEvent}
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.EventSelectionFormProvider
import models.EventSelection.{Event2, Event6, Event7, Event8, Event8A}
import models.enumeration.EventType
import models.{EventSelection, TaxYear, UserAnswers}
import pages.{EventSelectionPage, Waypoints}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.EventSelectionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EventSelectionController @Inject()(val controllerComponents: MessagesControllerComponents,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: EventSelectionFormProvider,
                                         view: EventSelectionView,
                                         userAnswersCacheConnector: UserAnswersCacheConnector,
                                         eventReportingConnector: EventReportingConnector,
                                         auditService: AuditService
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async { implicit request =>
    eventReportingConnector.getFeatureToggle("lta-events-show-hide").flatMap { toggleData =>
      val displayEventList: Seq[RadioItem] = getFilteredOptions(toggleData.isEnabled, request.userAnswers)
      Future.successful(Ok(view(form, displayEventList, waypoints)))
    }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          eventReportingConnector.getFeatureToggle("lta-events-show-hide").flatMap { toggleData =>
            val displayEventList: Seq[RadioItem] = getFilteredOptions(toggleData.isEnabled, request.userAnswers)
            Future.successful(BadRequest(view(formWithErrors, displayEventList, waypoints)))
          }
        ,
        value => {
          EventType.fromEventSelection(value) match {
            case Some(eventType) =>
              val futureUA = userAnswersCacheConnector.get(request.pstr, eventType).map {
                case Some(ua) => ua
                case None => UserAnswers()
              }

              futureUA.map { ua =>

                val taxYear = TaxYear.getSelectedTaxYear(ua)
                val answers = ua.setOrException(EventSelectionPage, value)
                auditService.sendEvent(
                  StartNewERAuditEvent(
                    request.loggedInUser.psaIdOrPspId,
                    request.pstr,
                    taxYear = taxYear,
                    eventType: EventType,
                    reportVersion = answers.eventDataIdentifier(eventType).version))
                Redirect(EventSelectionPage.navigate(waypoints, answers, answers).route)
              }
            case _ =>
              eventReportingConnector.getFeatureToggle("lta-events-show-hide").flatMap { toggleData =>
                val displayEventList: Seq[RadioItem] = getFilteredOptions(toggleData.isEnabled, request.userAnswers)
                Future.successful(Ok(view(form, displayEventList, waypoints)))
              }
          }
        }
      )
  }

  private def getFilteredOptions(isEnabled: Boolean, ua: UserAnswers)(implicit messages: Messages): Seq[RadioItem] = {
    val eventsToRemove: Seq[EventSelection] = TaxYear.getSelectedTaxYear(ua).startYear >= "2024" match {
      case true => Seq(Event2, Event6, Event7, Event8, Event8A)
      case false => Seq() // TODO: Include new events once developed Seq(Event25, Event26)
    }
    if (isEnabled) {
      EventSelection.options(EventSelection.values.diff(eventsToRemove))
    } else {
      EventSelection.options(EventSelection.values)
    }
  }
}

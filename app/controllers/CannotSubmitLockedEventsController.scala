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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.{TaxYearPage, VersionInfoPage, Waypoints}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CannotSubmitLockedEventsView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CannotSubmitLockedEventsController @Inject()(
                                                    val controllerComponents: MessagesControllerComponents,
                                                    identify: IdentifierAction,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    eventReportingConnector: EventReportingConnector,
                                                    eventLockView: CannotSubmitLockedEventsView
                                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async { implicit request =>
    val selectedTaxYear = request.userAnswers.get(TaxYearPage)
    val selectedVersionInfo = request.userAnswers.get(VersionInfoPage)
    (selectedTaxYear, selectedVersionInfo) match {
      case (Some(taxYear), Some(versionInfo)) =>
        val startYear = s"${taxYear.startYear}-04-06"
        eventReportingConnector.getEventReportSummary(request.pstr, startYear, versionInfo.version)
          .map { seqOfEventTypes =>
            val eventSummary = seqOfEventTypes.filter(_.lockedBy.nonEmpty)
            if (eventSummary.size > 1) {
              val eventSelectionUrl = controllers.routes.EventSelectionController.onPageLoad().url
              Ok(eventLockView(eventSelectionUrl, eventSummary, true))
            } else if (eventSummary.size == 1) {
              val eventSelectionUrl = controllers.routes.EventSelectionController.onPageLoad().url
              Ok(eventLockView(eventSelectionUrl, eventSummary, false))
            } else {
              Redirect(controllers.routes.WantToSubmitController.onPageLoad(waypoints))
            }
          }
      case _ => throw new RuntimeException("Either tax year or version info is missing.")
    }
  }
}
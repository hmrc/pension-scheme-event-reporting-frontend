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

package controllers.eventWindUp

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.enumeration.EventType.WindUp
import models.requests.DataRequest
import pages.eventWindUp.EventWindUpCheckYourAnswersPage
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.SchemeWindUpDateSummary
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

class EventWindUpCheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData(WindUp) andThen requireData) { implicit request =>

      val thisPage = EventWindUpCheckYourAnswersPage
      val waypoints = EmptyWaypoints

      Ok(view(SummaryListViewModel(rows = buildEventWindUpCYARows(waypoints, thisPage))))
    }

    private def buildEventWindUpCYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage)(implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] =
      SchemeWindUpDateSummary.row(request.userAnswers, waypoints, sourcePage).toSeq
}
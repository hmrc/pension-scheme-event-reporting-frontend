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

package controllers.event20

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import helpers.ReadOnlyCYA
import models.UserAnswers
import models.enumeration.EventType.Event20
import models.event20.WhatChange.{BecameOccupationalScheme, CeasedOccupationalScheme}
import models.requests.DataRequest
import pages.event20._
import pages.{CheckAnswersPage, EmptyWaypoints, VersionInfoPage, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswersValidation
import viewmodels.event20.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class Event20CheckYourAnswersController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: CheckYourAnswersView,
                                                   userAnswersValidation: UserAnswersValidation
                                                 )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData(Event20) andThen requireData) { implicit request =>
      val thisPage = Event20CheckYourAnswersPage()
      val waypoints = EmptyWaypoints
      val continueUrl = controllers.event20.routes.Event20CheckYourAnswersController.onClick.url
      val version = request.userAnswers.get(VersionInfoPage).map(_.version)
      val readOnlyHeading = ReadOnlyCYA.readOnlyHeading(Event20, version, request.readOnly())
      Ok(view(SummaryListViewModel(rows = buildEvent20CYARows(waypoints, thisPage, request.userAnswers)), continueUrl, readOnlyHeading))
    }

  def onClick: Action[AnyContent] =
    (identify andThen getData(Event20) andThen requireData).async { implicit request =>
          userAnswersValidation.validate(Event20)
    }

  private def buildEvent20CYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage, answers: UserAnswers)
                                 (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {

    val dateRow = answers.get(WhatChangePage) match {
      case Some(BecameOccupationalScheme) => BecameDateSummary.row(request.userAnswers, waypoints, sourcePage, request.readOnly())
      case Some(CeasedOccupationalScheme) => CeasedDateSummary.row(request.userAnswers, waypoints, sourcePage, request.readOnly())
      case _ => Nil
    }

    Seq(WhatChangeSummary.row(request.userAnswers, waypoints, sourcePage, request.readOnly()) ++ dateRow).flatten
  }
}

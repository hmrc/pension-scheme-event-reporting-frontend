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

package controllers.event20A

import com.google.inject.Inject
import connectors.EventReportingConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.UserAnswers
import models.enumeration.AdministratorOrPractitioner.{Administrator, Practitioner}
import models.enumeration.EventType.Event20A
import models.event20A.WhatChange.{BecameMasterTrust, CeasedMasterTrust}
import models.requests.DataRequest
import pages.event20A._
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.event20A.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class Event20ACheckYourAnswersController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   connector: EventReportingConnector,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: CheckYourAnswersView
                                                 )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData(Event20A) andThen requireData) { implicit request =>
      val thisPage = Event20ACheckYourAnswersPage()
      val waypoints = EmptyWaypoints
      val continueUrl = request.loggedInUser.administratorOrPractitioner match {
        case Administrator => controllers.event20A.routes.Event20APsaDeclarationController.onPageLoad(waypoints).url
        case Practitioner => controllers.event20A.routes.Event20APspDeclarationController.onPageLoad(waypoints).url
      }
      Ok(view(SummaryListViewModel(rows = buildEvent20ACYARows(waypoints, thisPage, request.userAnswers)), continueUrl))
    }

  private def buildEvent20ACYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage, answers: UserAnswers)
                                 (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {

    val dateRow = answers.get(WhatChangePage) match {
      case Some(BecameMasterTrust) => BecameDateSummary.row(request.userAnswers, waypoints, sourcePage)
      case Some(CeasedMasterTrust) => CeasedDateSummary.row(request.userAnswers, waypoints, sourcePage)
      case _ => Nil
    }

    Seq(WhatChangeSummary.row(request.userAnswers, waypoints, sourcePage) ++ dateRow).flatten
  }
}

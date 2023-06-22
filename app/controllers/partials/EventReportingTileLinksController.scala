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

package controllers.partials

import com.google.inject.Inject
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.EROverview
import pages.{EmptyWaypoints, EventReportingOverviewPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class EventReportingTileLinksController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   connector: EventReportingConnector,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: CheckYourAnswersView
                                                 )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onClickCompiled: Action[AnyContent] =
    (identify andThen getData() andThen requireData) { implicit request =>
      // TODO: implement redirect to the compiled event summary in the specified year.
      request.userAnswers.get(EventReportingOverviewPage) match {
        case Some(Seq(erOverview)) => Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)
        case Some(s:Seq[EROverview]) => Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)
        case _ => Redirect(controllers.routes.IndexController.onPageLoad.url)
      }

      /* TODO: implement below in PODS-8491.
      If 1 compile is in progress (i.e. with compiledVersionAvailable as Yes) then

      a) display the link "View event report in progress"
      (target is summary page for that year hence prior to rendering the page the year will need to be
      first saved in the user answers Mongo with event type of None) and

      b) display content in subheading of tile "Event report 2022 to 2023: in progress"
       */

    }

  def onClickSubmitted: Action[AnyContent] =
    (identify andThen getData() andThen requireData) { implicit request =>
      // TODO: change redirect to go to new "select which year you want to see event report" or similar.
      request.userAnswers.get(EventReportingOverviewPage) match {
        case Some(Seq(erOverview)) => Redirect(controllers.routes.TaxYearController.onPageLoad(EmptyWaypoints).url)
        case Some(s: Seq[EROverview]) => Redirect(controllers.routes.TaxYearController.onPageLoad(EmptyWaypoints).url)
        case _ => Redirect(controllers.routes.TaxYearController.onPageLoad(EmptyWaypoints).url)
      }
    }
}

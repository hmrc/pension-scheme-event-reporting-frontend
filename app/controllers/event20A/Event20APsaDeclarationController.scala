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

import connectors.{EventReportingConnector, MinimalConnector}
import controllers.actions._
import models.TaxYear.getTaxYearFromOption
import models.enumeration.EventType
import models.event20A.WhatChange.{BecameMasterTrust, CeasedMasterTrust}
import models.requests.DataRequest
import models.{LoggedInUser, TaxYear, UserAnswers}
import pages.Waypoints
import pages.event20A.{BecameDatePage, CeasedDatePage, WhatChangePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event20A.Event20APsaDeclarationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Event20APsaDeclarationController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: Event20APsaDeclarationView,
                                           eventReportingConnector: EventReportingConnector,
                                           minimalConnector: MinimalConnector)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val eventType = EventType.Event20A

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType)).async { implicit request =>
    minimalConnector.getMinimalDetails(request.loggedInUser.idName, request.loggedInUser.psaIdOrPspId).map{
        minimalDetails =>
      Ok(view(request.schemeName, request.pstr, getTaxYearFromOption(request.userAnswers).toString, minimalDetails.name,
        controllers.event20A.routes.Event20APsaDeclarationController.onClick(waypoints).url))
      }
  }

  def onClick(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData).async {
    implicit request =>
      declarationDataEvent20A(request.pstr, TaxYear.getSelectedTaxYear(request.userAnswers), request.loggedInUser, request) match {
        case Some(data) =>
          eventReportingConnector.submitReportEvent20A(request.pstr, UserAnswers(data)).map { _ =>
            Redirect(controllers.routes.EventSummaryController.onPageLoad(waypoints).url)
          }
        case _ => Future.successful(Redirect(controllers.routes.IndexController.onPageLoad.url))
      }
  }

  def declarationDataEvent20A(pstr: String, taxYear: TaxYear, loggedInUser: LoggedInUser, request: DataRequest[AnyContent]): Option[JsObject] = {
    val optCeaseDateOrStartDateNode =
      (request.userAnswers.get(WhatChangePage), request.userAnswers.get(BecameDatePage), request.userAnswers.get(CeasedDatePage)) match {
      case (Some(BecameMasterTrust), Some(becameDate), _) => Some(Json.obj("schemeMasterTrustStartDate" -> becameDate))
      case (Some(CeasedMasterTrust), _, Some(ceasedDate)) => Some(Json.obj("schemeMasterTrustCeaseDate" -> ceasedDate))
      case _ => None
    }

    optCeaseDateOrStartDateNode.map{ ceaseDateOrStartDateNode =>

      val event20AUserAnswers: JsObject = Json.obj(
          "pstr" -> pstr,
          "reportStartDate" -> s"${taxYear.startYear}-04-06",
          "reportEndDate" -> s"${taxYear.endYear}-04-05",
          "submittedBy" -> "PSA",
          "submittedID" -> loggedInUser.psaIdOrPspId,
        //Both of those are always selected. Users can't access the page otherwise.
        "psaDeclaration1" -> "Selected",
          "psaDeclaration2" -> "Selected"
      )
      event20AUserAnswers ++ ceaseDateOrStartDateNode
    }
  }

}

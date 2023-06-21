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
import helpers.DateHelper.getTaxYear
import models.{LoggedInUser, TaxYear, UserAnswers}
import models.enumeration.{AdministratorOrPractitioner, EventType}
import pages.Waypoints
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event20A.Event20APsaDeclarationView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

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
      Ok(view(request.schemeName, request.pstr, getTaxYear(request.userAnswers).toString, minimalDetails.name,
        controllers.event20A.routes.Event20APsaDeclarationController.onClick(waypoints).url))
      }
  }

//  def onClick(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
//    implicit request =>
//      val data: UserAnswers = UserAnswers(declarationDataEvent20A(request.pstr, getTaxYear(request.userAnswers)))
//      eventReportingConnector.submitReportEvent20A(request.pstr, ).map { _ =>
//        Redirect(controllers.routes.ReturnSubmittedController.onPageLoad(waypoints).url)
//
//      }
//  }

  def declarationDataEvent20A(pstr: String, taxYear: TaxYear, loggedInUser: LoggedInUser): JsObject = {
    val common: JsObject = Json.obj(
      "erDetails" -> Json.obj(
        "pSTR" -> pstr,
        "reportStartDate" -> s"${taxYear.startYear}-04-06",
        "reportEndDate" -> s"${taxYear.endYear}-04-05"
      ),
      "erDeclarationDetails" -> Json.obj(
        //PSA or PSP access
        "submittedBy" -> "PSA",
        //PSA or PSP ID
        "submittedID" -> loggedInUser.psaIdOrPspId
      )
    )

    val declarationDetails = common + ("psaDeclaration" -> Json.obj(
       //Both of those are always selected. Users can't access the page otherwise.
       "psaDeclaration1" -> "Selected",
       "psaDeclaration2" -> "Selected"
        ))

    Json.obj(
      "declarationDetails" -> declarationDetails
    )
  }

}

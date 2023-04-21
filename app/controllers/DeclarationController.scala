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

import connectors.EventReportingConnector
import controllers.actions._
import pages.Waypoints
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DeclarationView

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import models.UserAnswers
import DeclarationController.testDataPsa

class DeclarationController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       erConnector: EventReportingConnector,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: DeclarationView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = identify {
    implicit request =>
      Ok(view(continueUrl = controllers.routes.DeclarationController.onClick(waypoints).url))
  }

  def onClick(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData()).async {
    implicit request =>
      val testUserAnswers: UserAnswers = UserAnswers(testDataPsa(request.pstr))

      request.userAnswers match {
        //TODO: Replace test user answers above with ua when FE captures sufficient data
        case Some(_) => erConnector.submitReport(request.pstr, testUserAnswers).map {
          _ => Redirect(controllers.routes.ReturnSubmittedController.onPageLoad(waypoints).url)
        }
        case None => throw new RuntimeException("No user answers found in DeclarationController - required for report submit")
      }
  }
}

object DeclarationController {

  /**
   * The frontend and backend changes for report submission are complete, however the correct data is not captured in the frontend yet
   * Please see the To Do comments below for more info
   **/

  private def testDataPsa(pstr: String): JsObject = {
    Json.obj(
      "declarationDetails" -> Json.obj(
        "erDetails" -> Json.obj(
          "pSTR" -> pstr,
          //TODO: Report start date = tax year start date
          "reportStartDate" -> "2020-04-06",
          //TODO: Report end date = tax year end date
          "reportEndDate" -> "2021-04-05"
        ),
        "erDeclarationDetails" -> Json.obj(
          //TODO: Get PSA ID or PSP ID here from user answers
          "submittedBy" -> "PSA",
          //TODO: Get PSA or PSP ID from user answers
          "submittedID" -> "A2345678"
        ),
        "psaDeclaration" -> Json.obj(
          //TODO: Relates to wantToSubmit and Declaration
          "psaDeclaration1" -> "Selected",
          "psaDeclaration2" -> "Selected"
        )
      )
    )
  }
}
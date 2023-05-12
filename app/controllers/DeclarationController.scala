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
import models.{LoggedInUser, TaxYear, UserAnswers}
import DeclarationController.testDataPsa
import models.enumeration.AdministratorOrPractitioner

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
      println("---------------------")
      Json.prettyPrint(request.userAnswers.get.data)

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

  private def declarationData(pstr: String, taxYear: TaxYear, loggedInUser: LoggedInUser): JsObject = {
    val psaOrPsp = loggedInUser.administratorOrPractitioner match {
      case AdministratorOrPractitioner.Administrator => "PSA"
      case AdministratorOrPractitioner.Practitioner => "PSP"
      case _ => throw new RuntimeException("Unknown user type")
    }
    val common = Json.obj(
      "declarationDetails" -> Json.obj(
        "erDetails" -> Json.obj(
          "pSTR" -> pstr,
          //Report start date = tax year start date
          "reportStartDate" -> s"${taxYear.startYear}-04-06",
          //Report end date = tax year end date
          "reportEndDate" -> s"${taxYear.endYear}-04-05"
        ),
        "erDeclarationDetails" -> Json.obj(
          //PSA or PSP access
          "submittedBy" -> psaOrPsp,
          //PSA or PSP ID
          "submittedID" -> loggedInUser.psaIdOrPspId
        ),

      )
    )

    loggedInUser.administratorOrPractitioner match {
      case AdministratorOrPractitioner.Administrator =>
        common + ("psaDeclaration" -> Json.obj(
          //TODO: Relates to wantToSubmit and Declaration
          "psaDeclaration1" -> "Selected",
          "psaDeclaration2" -> "Selected"
        ))
      case AdministratorOrPractitioner.Practitioner => ??? //TODO: Implement declaration submission by PSA
    }
  }
}
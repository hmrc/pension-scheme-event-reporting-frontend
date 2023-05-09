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

package controllers.fileUpload

import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.fileUpload.FileUploadResultFormProvider
import models.FileUploadOutcomeStatus.IN_PROGRESS
import models.FileUploadOutcomeStatus.SUCCESS
import models.FileUploadOutcomeStatus.FAILURE
import models.FileUploadOutcomeResponse
import models.UserAnswers
import models.enumeration.EventType
import models.enumeration.EventType.getEventTypeByName
import models.fileUpload.FileUploadResult
import models.requests.OptionalDataRequest
import pages.Waypoints
import pages.fileUpload.FileUploadResultPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.fileUpload.FileUploadResultView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FileUploadResultController @Inject()(val controllerComponents: MessagesControllerComponents,
                                          identify: IdentifierAction,
                                          getData: DataRetrievalAction,
                                          userAnswersCacheConnector: UserAnswersCacheConnector,
                                          eventReportingConnector: EventReportingConnector,
                                          formProvider: FileUploadResultFormProvider,
                                          view: FileUploadResultView
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  private def renderView(waypoints: Waypoints, eventType: EventType, preparedForm: Form[FileUploadResult], status: Status)
                        (implicit request: OptionalDataRequest[AnyContent])= {
    request.request.queryString.get("key").flatMap(_.headOption) match {
      case Some(uploadIdReference) =>
    val submitUrl = Call("POST", routes.FileUploadResultController.onSubmit(waypoints).url + s"?key=$uploadIdReference")
        eventReportingConnector.getFileUploadOutcome(uploadIdReference).map {
          case FileUploadOutcomeResponse(_, IN_PROGRESS) =>
            status(view(preparedForm, waypoints, getEventTypeByName(eventType), None, submitUrl))
          case FileUploadOutcomeResponse(fileName@Some(_), SUCCESS) =>
            status(view(preparedForm, waypoints, getEventTypeByName(eventType), fileName, submitUrl))
          case FileUploadOutcomeResponse(_, FAILURE) =>
            Redirect(controllers.fileUpload.routes.FileRejectedController.onPageLoad(waypoints).url)
          case _ => throw new RuntimeException("UploadId reference does not exist")
        }
      case _ => Future.successful(BadRequest("Missing Key"))
    }
  }
  def onPageLoad(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType)) async { implicit request =>
    val preparedForm = request.userAnswers.flatMap(_.get(FileUploadResultPage(eventType))).fold(form)(form.fill)
    renderView(waypoints, eventType, preparedForm, Ok)
  }

  def onSubmit(waypoints: Waypoints, eventType: EventType): Action[AnyContent] = (identify andThen getData(eventType)).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          renderView(waypoints, eventType, formWithErrors, BadRequest),
        value => {
          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
          val updatedAnswers = originalUserAnswers.setOrException(FileUploadResultPage(eventType), value)
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
            Redirect(FileUploadResultPage(eventType).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
          }
        }
      )
  }

}
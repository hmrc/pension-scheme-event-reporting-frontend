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

package controllers.common

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.common.MembersDetailsFormProvider
import models.Index.{indexToInt, intToIndex}
import models.enumeration.EventType
import models.enumeration.EventType.Event1
import models.requests.OptionalDataRequest
import models.{Index, UserAnswers}
import pages.Waypoints
import pages.common.{MembersDetailsPage, MembersOrEmployersPage, MembersPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsPath, Reads}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.common.MembersDetailsView

import javax.inject.Inject
import scala.collection.immutable.HashSet
import scala.concurrent.{ExecutionContext, Future}

class MembersDetailsController @Inject()(val controllerComponents: MessagesControllerComponents,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         userAnswersCacheConnector: UserAnswersCacheConnector,
                                         formProvider: MembersDetailsFormProvider,
                                         view: MembersDetailsView
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {
  private def memberNinos(eventType: EventType, index: Int)(implicit request: OptionalDataRequest[_]): HashSet[String] = {
    val readsNINumber: Reads[Option[String]] = (JsPath \ "membersDetails" \ "nino").readNullable[String]
    val pg = eventType match {
      case Event1 => MembersOrEmployersPage(eventType)
      case _ => MembersPage(eventType)
    }
    val s = request.userAnswers.map(_.getAll(pg.path)(readsNINumber.map(_.toSeq))).toSeq.flatten.flatten
      .zipWithIndex.filter(_._2 != index).map(_._1)
    HashSet(s: _*)
  }

  def onPageLoad(waypoints: Waypoints, eventType: EventType, index: Index, memberPageNo: Int): Action[AnyContent] =
    (identify andThen getData(eventType)) { implicit request =>
      val form = formProvider(eventType, memberNinos(eventType, indexToInt(index)), memberPageNo)
      val preparedForm = request.userAnswers.flatMap(_.get(MembersDetailsPage(eventType, indexToInt(index), memberPageNo))).fold(form)(form.fill)
      Ok(view(preparedForm, waypoints, eventType, memberPageNo, controllers.common.routes.MembersDetailsController.onSubmit(waypoints, eventType, index, memberPageNo)))
    }

  def onSubmit(waypoints: Waypoints, eventType: EventType, index: Index, memberPageNo: Int): Action[AnyContent] = (identify andThen getData(eventType)).async {
    implicit request =>
      doOnSubmit(
        waypoints,
        eventType,
        MembersDetailsPage(eventType, index, memberPageNo),
        controllers.common.routes.MembersDetailsController.onSubmit(waypoints, eventType, index, memberPageNo),
        memberPageNo,
        index
      )
  }

  private def doOnSubmit(waypoints: Waypoints,
                         eventType: EventType,
                         page: MembersDetailsPage,
                         postCall: => Call,
                         memberPageNo: Int,
                         index: Int
                        )(implicit request: OptionalDataRequest[_]): Future[Result] = {
    val form = formProvider(eventType, memberNinos(eventType, indexToInt(index)), memberPageNo)
    form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors, waypoints, eventType, memberPageNo, postCall))),
      value => {
        val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
        val updatedAnswers = originalUserAnswers.setOrException(page, value)
        userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
          Redirect(page.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
        }
      }
    )
  }
}

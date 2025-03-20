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

package controllers.address

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.event1.IsUkFormProvider
import models.Index
import models.enumeration.AddressJourneyType
import models.requests.DataRequest
import pages.Waypoints
import pages.address.IsUkPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AddressHelper.retrieveNameManual
import views.html.event1.IsUkVIew

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsUkController @Inject()(val controllerComponents: MessagesControllerComponents,
                               identify: IdentifierAction,
                               getData: DataRetrievalAction,
                               requireData: DataRequiredAction,
                               userAnswersCacheConnector: UserAnswersCacheConnector,
                               formProvider: IsUkFormProvider,
                               view: IsUkVIew
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints, addressJourneyType: AddressJourneyType, index: Index): Action[AnyContent] =
    (identify andThen getData(addressJourneyType.eventType) andThen requireData) { implicit request =>
      val name = getName(addressJourneyType, index, request)
      Ok(
        view(
          request.userAnswers.get(IsUkPage(addressJourneyType, index)).fold(formProvider(name))(formProvider(name).fill),
          addressJourneyType,
          waypoints,
          index,
          name
        )
      )
    }

  private def getName(addressJourneyType: AddressJourneyType, index: Index, request: DataRequest[AnyContent]) =
    if(addressJourneyType.nodeName == "employerResidentialAddress" || addressJourneyType.nodeName == "memberResidentialAddress") {
      messagesApi.preferred(request)("entityType.theResidentialProperty")
    } else {
      retrieveNameManual(request, index)
    }

  def onSubmit(waypoints: Waypoints, addressJourneyType: AddressJourneyType, index: Index): Action[AnyContent] =
    (identify andThen getData(addressJourneyType.eventType) andThen requireData).async {
      implicit request =>

        val page = IsUkPage(addressJourneyType, index)


        val name = getName(addressJourneyType, index, request)

        formProvider(name).bindFromRequest().fold(
          formWithErrors => Future.successful(
            BadRequest(
              view(
                formWithErrors, addressJourneyType, waypoints, index, name
              )
            )
          ),
          answer => {
            val originalUserAnswers = request.userAnswers
            val updatedAnswers = originalUserAnswers.setOrException(page, answer)
            userAnswersCacheConnector.save(request.pstr, addressJourneyType.eventType, updatedAnswers).map { _ =>
              Redirect(page.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
            }
          }
        )
    }

}

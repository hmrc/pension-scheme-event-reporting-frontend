/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.address.ChooseAddressFormProvider
import models.enumeration.AddressJourneyType
import pages.Waypoints
import pages.address.ChooseAddressPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.address.ChooseAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChooseAddressController @Inject()(val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        userAnswersCacheConnector: UserAnswersCacheConnector,
                                        formProvider: ChooseAddressFormProvider,
                                        view: ChooseAddressView
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val whichAddressPage = "chooseAddress"

  private val form = formProvider()

  def onPageLoad(waypoints: Waypoints, addressJourneyType: AddressJourneyType): Action[AnyContent] =
    (identify andThen getData(addressJourneyType.eventType) andThen requireData) { implicit request =>
      val preparedForm = request.userAnswers.get(ChooseAddressPage(addressJourneyType)).fold(form)(form.fill)
      Ok(view(preparedForm, waypoints, addressJourneyType,
        addressJourneyType.title(whichAddressPage), addressJourneyType.heading(whichAddressPage)))
    }

  def onSubmit(waypoints: Waypoints, addressJourneyType: AddressJourneyType): Action[AnyContent] =
    (identify andThen getData(addressJourneyType.eventType) andThen requireData).async {
      implicit request =>
        form.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(BadRequest(view(formWithErrors, waypoints, addressJourneyType,
              addressJourneyType.title(whichAddressPage), addressJourneyType.heading(whichAddressPage))))
          },
          value => {
            val originalUserAnswers = request.userAnswers
            val updatedAnswers = originalUserAnswers.setOrException(ChooseAddressPage(addressJourneyType), value)
            userAnswersCacheConnector.save(request.pstr, addressJourneyType.eventType, updatedAnswers).map { _ =>
              Redirect(ChooseAddressPage(addressJourneyType).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
            }
          }
        )
    }

}

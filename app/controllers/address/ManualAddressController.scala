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
import forms.address.ManualAddressFormProvider
import models.address.Address
import models.enumeration.AddressJourneyType
import pages.Waypoints
import pages.address.ManualAddressPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CountryOptions
import views.html.address.ManualAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ManualAddressController @Inject()(val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        userAnswersCacheConnector: UserAnswersCacheConnector,
                                        formProvider: ManualAddressFormProvider,
                                        view: ManualAddressView,
                                        val countryOptions: CountryOptions
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[Address] = formProvider()

  def onPageLoad(waypoints: Waypoints, addressJourneyType: AddressJourneyType): Action[AnyContent] =
    (identify andThen getData(addressJourneyType.eventType) andThen requireData) { implicit request =>
      val page = ManualAddressPage(addressJourneyType)
      val preparedForm = request.userAnswers.get(page).fold(form)(form.fill)
      Ok(
        view(
          preparedForm,
          waypoints,
          addressJourneyType,
          addressJourneyType.title(page),
          addressJourneyType.heading(page),
          countryOptions.options)
      )
    }

  def onSubmit(waypoints: Waypoints, addressJourneyType: AddressJourneyType): Action[AnyContent] =
    (identify andThen getData(addressJourneyType.eventType) andThen requireData).async {
      implicit request =>
        val page = ManualAddressPage(addressJourneyType)
        form.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(
              BadRequest(
                view(
                  addArgsToErrors(formWithErrors, addressJourneyType.entityTypeInstanceName(request.userAnswers)),
                  waypoints,
                  addressJourneyType,
                  addressJourneyType.title(page),
                  addressJourneyType.heading(page), countryOptions.options)
              )
            )
          },
          value => {
            val originalUserAnswers = request.userAnswers
            val updatedAnswers = originalUserAnswers.setOrException(page, value)
            userAnswersCacheConnector.save(request.pstr, addressJourneyType.eventType, updatedAnswers).map { _ =>
              Redirect(page.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
            }
          }
        )
    }

  private def addArgsToErrors(form: Form[Address], args: String*): Form[Address] =
    form copy (errors = form.errors.map(_ copy (args = args)))

}

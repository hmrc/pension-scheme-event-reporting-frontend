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
import forms.address.ManualAddressFormProvider
import models.Index
import models.address.Address
import models.enumeration.AddressJourneyType
import pages.Waypoints
import pages.address.{EnterPostcodePage, ManualAddressPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AddressHelper.retrieveNameManual
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

  def onPageLoad(waypoints: Waypoints, addressJourneyType: AddressJourneyType, index: Index): Action[AnyContent] =
    (identify andThen getData(addressJourneyType.eventType) andThen requireData) { implicit request =>
      val form: Form[Address] = formProvider(retrieveNameManual(request, index))
      val page = ManualAddressPage(addressJourneyType, index)
      val preparedForm = request.userAnswers.get(page) match {
        case None => request.userAnswers.get(EnterPostcodePage(addressJourneyType, index)) match {
          case Some(value) => form.fill(value.head.toPrepopAddress)
          case None => form
        }
        case Some(value) => form.fill(value)
      }
      Ok(
        view(
          preparedForm,
          waypoints,
          addressJourneyType,
          addressJourneyType.title(page),
          addressJourneyType.heading(page, index),
          countryOptions.options,
          index)
      )
    }

  def onSubmit(waypoints: Waypoints, addressJourneyType: AddressJourneyType, index: Index): Action[AnyContent] =
    (identify andThen getData(addressJourneyType.eventType) andThen requireData).async {
      implicit request =>
        val form: Form[Address] = formProvider(retrieveNameManual(request, index))
        val page = ManualAddressPage(addressJourneyType, index)
        form.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(
              BadRequest(
                view(
                  addArgsToErrors(formWithErrors, addressJourneyType.entityName(request.userAnswers, index)),
                  waypoints,
                  addressJourneyType,
                  addressJourneyType.title(page),
                  addressJourneyType.heading(page, index), countryOptions.options,
                  index
                )
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

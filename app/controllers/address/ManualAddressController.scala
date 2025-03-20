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
import models.address.{Address, TolerantAddress}
import models.enumeration.AddressJourneyType
import models.requests.DataRequest
import pages.Waypoints
import pages.address.{EnterPostcodePage, ManualAddressPage, UserEnteredAddressPage}
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

  private def getName(addressJourneyType: AddressJourneyType, index: Index, request: DataRequest[AnyContent]) =
    if(addressJourneyType.nodeName == "employerResidentialAddress" || addressJourneyType.nodeName == "memberResidentialAddress") {
      messagesApi.preferred(request)("entityType.theResidentialProperty")
    } else {
      retrieveNameManual(request, index)
    }

  def onPageLoad(waypoints: Waypoints, addressJourneyType: AddressJourneyType, index: Index, isUk: Boolean): Action[AnyContent] =
    (identify andThen getData(addressJourneyType.eventType) andThen requireData) { implicit request =>
      val form: Form[Address] = formProvider(getName(addressJourneyType, index, request))
      val manualPage = ManualAddressPage(addressJourneyType, index, isUk)
      val postCode = request.userAnswers.get(EnterPostcodePage(addressJourneyType, index))
      val address = request.userAnswers.get(UserEnteredAddressPage(addressJourneyType, index))

      val dataToFill = address.map { address =>
        val a = address.copy(postcode = postCode)
        if(isUk) a.copy(country = "GB") else a
      }.getOrElse(TolerantAddress(None, None, None, None, postCode, if(isUk) Some("GB") else None).toPrepopAddress)

      val preparedForm = form.fill(dataToFill)

      Ok(
        view(
          preparedForm,
          waypoints,
          addressJourneyType,
          addressJourneyType.title(manualPage),
          addressJourneyType.heading(manualPage, index),
          countryOptions.options,
          index,
          isUk)
      )
    }

  def onSubmit(waypoints: Waypoints, addressJourneyType: AddressJourneyType, index: Index, isUk: Boolean): Action[AnyContent] =
    (identify andThen getData(addressJourneyType.eventType) andThen requireData).async {
      implicit request =>
        val form: Form[Address] = formProvider(getName(addressJourneyType, index, request))
        val page = ManualAddressPage(addressJourneyType, index, isUk)
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
                  index,
                  isUk
                )
              )
            )
          },
          value => {
            val originalUserAnswers = request.userAnswers
            val updatedAnswers = originalUserAnswers.setOrException(page, value)
              .setOrException(UserEnteredAddressPage(addressJourneyType, index), value)
            val uaWithPostcode = value.postcode.map(postCode => updatedAnswers.setOrException(EnterPostcodePage(addressJourneyType, index), postCode))
              .getOrElse(updatedAnswers)
            userAnswersCacheConnector.save(request.pstr, addressJourneyType.eventType, uaWithPostcode).map { _ =>
              Redirect(page.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
            }
          }
        )
    }

  private def addArgsToErrors(form: Form[Address], args: String*): Form[Address] =
    form copy (errors = form.errors.map(_ copy (args = args)))

}

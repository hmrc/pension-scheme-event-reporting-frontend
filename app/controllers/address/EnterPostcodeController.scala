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

import connectors.{AddressLookupConnector, UserAnswersCacheConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.address.EnterPostcodeFormProvider
import models.Index
import models.enumeration.AddressJourneyType
import models.requests.DataRequest
import pages.Waypoints
import pages.address.{EnterPostcodePage, EnterPostcodeRetrievedPage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AddressHelper.retrieveNameManual
import viewmodels.Message
import views.html.address.EnterPostcodeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EnterPostcodeController @Inject()(val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        userAnswersCacheConnector: UserAnswersCacheConnector,
                                        formProvider: EnterPostcodeFormProvider,
                                        view: EnterPostcodeView,
                                        addressLookupConnector: AddressLookupConnector
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints, addressJourneyType: AddressJourneyType, index: Index): Action[AnyContent] =
    (identify andThen getData(addressJourneyType.eventType) andThen requireData) { implicit request =>

      val page = EnterPostcodePage(addressJourneyType, index)
      val form = request.userAnswers.get(page).fold(formProvider(""))( postCode => formProvider(postCode).fill(postCode))
      Ok(
        view(
          form,
          waypoints,
          addressJourneyType,
          addressJourneyType.title(page),
          addressJourneyType.heading(page, index),
          index
        )
      )
    }

  def onSubmit(waypoints: Waypoints, addressJourneyType: AddressJourneyType, index: Index): Action[AnyContent] =
    (identify andThen getData(addressJourneyType.eventType) andThen requireData).async {
      implicit request =>

        val page = EnterPostcodePage(addressJourneyType, index)

        def renderView(formForRender: Form[String]): Future[Result] = {
          Future.successful(
            BadRequest(
              view(
                formForRender,
                waypoints,
                addressJourneyType,
                addressJourneyType.title(page),
                addressJourneyType.heading(page, index),
                index
              )
            )
          )
        }

        def invalidResponse(postCode: String, error: String) = {
          val updatedAnswers = request.userAnswers.setOrException(page, postCode)
          userAnswersCacheConnector.save(
            request.pstr,
            addressJourneyType.eventType,
            updatedAnswers
          ).flatMap { _ =>
            renderView(formWithError(Message(error, postCode), retrieveNameManual(request, index)).fill(postCode))
          }
        }

        formProvider(retrieveNameManual(request, index)).bindFromRequest().fold(
          formWithErrors => renderView(formWithErrors),
          postCode => {
            addressLookupConnector.addressLookupByPostCode(postCode).flatMap {
              case Nil =>
                invalidResponse(postCode, "enterPostcode.error.notFound")
              case addresses =>
                val originalUserAnswers = request.userAnswers
                val updatedAnswers = originalUserAnswers
                  .setOrException(EnterPostcodeRetrievedPage(addressJourneyType, index), addresses)
                  .setOrException(EnterPostcodePage(addressJourneyType, index), postCode)
                userAnswersCacheConnector.save(request.pstr, addressJourneyType.eventType, updatedAnswers).map { _ =>
                  Redirect(page.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
                }

            } recoverWith {
              case _ =>
                invalidResponse(postCode, "enterPostcode.error.invalid")
            }
          }
        )
    }

  private def formWithError(message: Message, name: String)(implicit request: DataRequest[AnyContent]): Form[String] = {
    formProvider(name).withError("value", message)
  }
}

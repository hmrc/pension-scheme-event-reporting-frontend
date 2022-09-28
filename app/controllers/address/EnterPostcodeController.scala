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

import connectors.{AddressLookupConnector, UserAnswersCacheConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.address.EnterPostcodeFormProvider
import models.enumeration.AddressJourneyType
import models.requests.DataRequest
import pages.Waypoints
import pages.address.EnterPostcodePage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
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

  private val form = formProvider()

  def onPageLoad(waypoints: Waypoints, addressJourneyType: AddressJourneyType): Action[AnyContent] =
    (identify andThen getData(addressJourneyType.eventType) andThen requireData) { implicit request =>
      val page = EnterPostcodePage(addressJourneyType)
      Ok(
        view(
          form,
          waypoints,
          addressJourneyType,
          addressJourneyType.title(page),
          addressJourneyType.heading(page)
        )
      )
    }

  def onSubmit(waypoints: Waypoints, addressJourneyType: AddressJourneyType): Action[AnyContent] =
    (identify andThen getData(addressJourneyType.eventType) andThen requireData).async {
      implicit request =>
        val page = EnterPostcodePage(addressJourneyType)
        def renderView(formForRender: Form[String]): Future[Result] = {
          Future.successful(
            BadRequest(
              view(
                formForRender,
                waypoints,
                addressJourneyType,
                addressJourneyType.title(page),
                addressJourneyType.heading(page)
              )
            )
          )
        }

        form.bindFromRequest().fold(
          formWithErrors => renderView(formWithErrors),
          postCode => {
            val noResults: Message = Message("enterPostcode.error.noResults", postCode)
            val invalidPostcode: Message = Message("enterPostcode.error.noResults", postCode)
            addressLookupConnector.addressLookupByPostCode(postCode).flatMap {
              case Nil =>
                renderView(formWithError(noResults))
              case addresses =>
                val originalUserAnswers = request.userAnswers
                val updatedAnswers = originalUserAnswers.setOrException(page, addresses)
                userAnswersCacheConnector.save(request.pstr, addressJourneyType.eventType, updatedAnswers).map { _ =>
                  Redirect(page.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
                }

            } recoverWith {
              case _ =>
                renderView(formWithError(invalidPostcode))
            }
          }
        )
    }

  private def formWithError(message: Message)(implicit request: DataRequest[AnyContent]): Form[String] = {
    form.withError("postcode", message)
  }

}

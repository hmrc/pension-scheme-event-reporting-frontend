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
import forms.address.ChooseAddressFormProvider
import models.Index
import models.address.TolerantAddress
import models.enumeration.AddressJourneyType
import pages.Waypoints
import pages.address.{ChooseAddressPage, EnterPostcodeRetrievedPage, ManualAddressPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CountryOptions
import views.html.address.ChooseAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class ChooseAddressController @Inject()(val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        userAnswersCacheConnector: UserAnswersCacheConnector,
                                        formProvider: ChooseAddressFormProvider,
                                        view: ChooseAddressView,
                                        countryOptions: CountryOptions
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  private def getSortedAddresses(addresses:Seq[TolerantAddress]) = {
    addresses.distinct.sortWith { case (a1, a2) =>
      def number(address: TolerantAddress) = address.addressLine1.flatMap { a =>
        Try(a.split(" ").head.toInt).toOption
      }

      number(a1) -> number(a2) match {
        case (Some(a1n), Some(a2n)) => a1n < a2n
        case (None, Some(a2n)) => false
        case (Some(a1n), None) => true
        case (None, None) => a1.toPrepopAddress.lines(countryOptions).mkString(" ") < a2.toPrepopAddress.lines(countryOptions).mkString(" ")
      }
    }
  }


  def onPageLoad(waypoints: Waypoints, addressJourneyType: AddressJourneyType, index: Index): Action[AnyContent] =
    (identify andThen getData(addressJourneyType.eventType) andThen requireData) { implicit request =>
      request.userAnswers.get(EnterPostcodeRetrievedPage(addressJourneyType, index)) match {
        case Some(addresses) =>
          val chosenAddress = request.userAnswers.get(ManualAddressPage(addressJourneyType, index, false))
          val sortedAddresses = getSortedAddresses(addresses)
          val form = formProvider(sortedAddresses)
          val page = ChooseAddressPage(addressJourneyType, index)
          Ok(view(form, waypoints, addressJourneyType,
            addressJourneyType.title(page),
            addressJourneyType.heading(page, index),
            sortedAddresses,
            index,
            chosenAddress.map(_.toTolerantAddress)
          ))
        case _ => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad(None).url)
      }
    }

  def onSubmit(waypoints: Waypoints, addressJourneyType: AddressJourneyType, index: Index): Action[AnyContent] =
    (identify andThen getData(addressJourneyType.eventType) andThen requireData).async {
      implicit request =>
        val page = ChooseAddressPage(addressJourneyType, index)
        request.userAnswers.get(EnterPostcodeRetrievedPage(addressJourneyType, index)) match {
          case Some(addresses) =>
            val chosenAddress = request.userAnswers.get(ManualAddressPage(addressJourneyType, index, false))
            val sortedAddresses = getSortedAddresses(addresses)
            val form = formProvider(sortedAddresses)
            form.bindFromRequest().fold(
              formWithErrors => {
                Future.successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      waypoints,
                      addressJourneyType,
                      addressJourneyType.title(page),
                      addressJourneyType.heading(page, index),
                      sortedAddresses,
                      index,
                      chosenAddress.map(_.toTolerantAddress)
                    )
                  )
                )
              },
              value => {
                val originalUserAnswers = request.userAnswers
                getSortedAddresses(addresses)(value).toAddress match {
                  case Some(address) =>
                    val updatedAnswers = originalUserAnswers.setOrException(ManualAddressPage(addressJourneyType, index, true), address)
                    userAnswersCacheConnector.save(request.pstr, addressJourneyType.eventType, updatedAnswers).map { _ =>
                      Redirect(page.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
                    }
                  case _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad(None).url))
                }
              }
            )
          case _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad(None).url))
        }
    }

}

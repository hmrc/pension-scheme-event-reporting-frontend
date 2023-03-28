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

package controllers.event2

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.event2.datePaidFormProvider
import models.common.MembersDetails
import models.enumeration.EventType
import models.{Index, UserAnswers}
import pages.common.MembersDetailsPage
import pages.event2.DatePaidPage
import pages.{TaxYearPage, Waypoints}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Event2MemberPageNumbers
import viewmodels.Message
import views.html.event2.datePaidView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class datePaidController @Inject()(val controllerComponents: MessagesControllerComponents,
                                    identify: IdentifierAction,
                                    getData: DataRetrievalAction,
                                    userAnswersCacheConnector: UserAnswersCacheConnector,
                                    formProvider: datePaidFormProvider,
                                    view: datePaidView
                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val eventType = EventType.Event2

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType)) { implicit request =>
    //TODO: what to return in default case
    val taxYear = request.userAnswers.flatMap(_.get(TaxYearPage)) match {
      case Some(year) => year.getYearAsString
      case _ => ""
    }

    val form = formProvider(taxYear)

    //TODO: remove duplicate code, find more effective method
    val beneficiaryFullNameOpt = request.userAnswers.flatMap {
      _.get(MembersDetailsPage(eventType, index, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY)).map {
        g => MembersDetails(g.firstName, g.lastName, g.nino).fullName
      }
    }
    val datePaidHeadingMessage = beneficiaryFullNameOpt match {
      case Some(beneficiaryName) => Message("datePaid.event2.heading", beneficiaryName)
      //TODO: what to do when no name is pulled back here
      case _ => Message("datePaid.event2.heading", "")
    }
    val preparedForm = request.userAnswers.flatMap(_.get(DatePaidPage(index))).fold(form)(form.fill)

    Ok(view(preparedForm, waypoints, datePaidHeadingMessage, index))
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType)).async {
    implicit request =>
      //TODO: what to return in default case
      val taxYear = request.userAnswers.flatMap(_.get(TaxYearPage)) match {
        case Some(year) => year.getYearAsString
        case _ => ""
      }
      val form = formProvider(taxYear)

      //TODO: remove duplicate code, find more effective method
      val beneficiaryFullNameOpt = request.userAnswers.flatMap {
        _.get(MembersDetailsPage(eventType, index, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY)).map {
          g => MembersDetails(g.firstName, g.lastName, g.nino).fullName
        }
      }
      val datePaidHeadingMessage = beneficiaryFullNameOpt match {
        case Some(beneficiaryName) => Message("datePaid.event2.heading", beneficiaryName)
        //TODO: what to do when no name is pulled back here
        case _ => Message("datePaid.event2.heading", "")
      }
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, waypoints, datePaidHeadingMessage, index))),
        value => {
          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
          val updatedAnswers = originalUserAnswers.setOrException(DatePaidPage(index), value)
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
            Redirect(DatePaidPage(index).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
          }
        }
      )
  }

}

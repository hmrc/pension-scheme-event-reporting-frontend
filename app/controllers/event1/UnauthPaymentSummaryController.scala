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

package controllers.event1

import connectors.{EventReportingConnector, UnauthPaymentConnector, UserAnswersCacheConnector}
import controllers.actions._
import forms.event1.UnauthPaymentSummaryFormProvider
import models.UserAnswers
import models.enumeration.EventType
import models.event1.employer.CompanyDetails
import pages.Waypoints
import pages.common.MembersDetailsPage
import pages.event1.UnauthPaymentSummaryPage
import pages.event1.employer.CompanyDetailsPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.Message
import views.html.event1.UnauthPaymentSummaryView

import javax.inject.Inject
import scala.concurrent.ExecutionContext
// TODO bring in correct information for table no tests have been added yet
class UnauthPaymentSummaryController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        connector: EventReportingConnector,
                                        userAnswersCacheConnector: UserAnswersCacheConnector,
                                        formProvider: UnauthPaymentSummaryFormProvider,
                                        view: UnauthPaymentSummaryView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  private val eventType = EventType.Event1

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType)).async { implicit request =>

    //    val ua = request.userAnswers.get
    //    val memberName = ua.get(MembersDetailsPage(eventType)).map {
    //      member =>
    //        (member.firstName, member.lastName).toString()
    //    }
    //    val companyName = ua.get(CompanyDetailsPage).map {
    //      company =>
    //        company.companyName
    //    }

    connector.getEventReportSummary(request.pstr).map { seqOfEventTypes =>
      val mappedEvents = seqOfEventTypes.map { event =>
        SummaryListRow(
          key = Key(
            content = Text(Message(s"unauthPaymentSummary.name${event.toString}"))
          ),
          actions = Some(Actions(
            items = Seq(
              ActionItem(
                content = Text(Message("site.change")),
                href = "#"
              ),
              ActionItem(
                content = Text(Message("site.remove")),
                href = "#"
              )
            )
          ))
        )
      }
      Ok(view(form, waypoints, mappedEvents))
    }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = identify {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => BadRequest(view(formWithErrors, waypoints, Nil)),
        value => {
          val userAnswerUpdated = UserAnswers().setOrException(UnauthPaymentSummaryPage, value)
          Redirect(UnauthPaymentSummaryPage.navigate(waypoints, userAnswerUpdated, userAnswerUpdated).route)}
    )
  }
}

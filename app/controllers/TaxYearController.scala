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

package controllers

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.TaxYearFormProvider
import models.enumeration.JourneyStartType.PastEventTypes
import models.requests.OptionalDataRequest
import models.{TaxYear, UserAnswers}
import pages.{EventReportingOverviewPage, EventReportingTileLinksPage, TaxYearPage, Waypoints}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TaxYearView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxYearController @Inject()(val controllerComponents: MessagesControllerComponents,
                                  identify: IdentifierAction,
                                  getData: DataRetrievalAction,
                                  userAnswersCacheConnector: UserAnswersCacheConnector,
                                  formProvider: TaxYearFormProvider,
                                  view: TaxYearView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  private def renderPage(form: Form[TaxYear], waypoints: Waypoints, status: Status)(implicit request: OptionalDataRequest[AnyContent]): Result = {
    val radioOptions = request.userAnswers match {
      case Some(ua) =>
        ua.get(EventReportingTileLinksPage) match {
          case Some(PastEventTypes) =>
            ua.get(EventReportingOverviewPage) match {
              case Some(s) =>
                println("\nHERE:" + s)
                val applicableYears: Seq[String] = s.flatMap { erOverview =>
                  if (erOverview.versionDetails.exists(_.submittedVersionAvailable)) {
                    Seq(erOverview.periodStartDate.getYear.toString)
                  } else {
                    Nil
                  }
                }
                println("\nappli years: " + applicableYears)
                TaxYear.optionsFiltered(option => applicableYears.contains(option.startYear))
            }
          case _ => TaxYear.options
        }
      case _ => TaxYear.options
    }

    status(view(form, waypoints, radioOptions))
  }

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData()) { implicit request =>
    val preparedForm = request.userAnswers.flatMap(_.get(TaxYearPage)).fold(form)(form.fill)
    renderPage(preparedForm, waypoints, Ok)
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData()).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(renderPage(formWithErrors, waypoints, BadRequest)),
        value => {
          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
          val updatedAnswers = originalUserAnswers.setOrException(TaxYearPage, value, nonEventTypeData = true)
          userAnswersCacheConnector.save(request.pstr, updatedAnswers).map { _ =>
            Redirect(TaxYearPage.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
          }
        }
      )
  }

}

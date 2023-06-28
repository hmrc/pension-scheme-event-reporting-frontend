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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.TaxYearFormProvider
import models.enumeration.JourneyStartType.{InProgress, PastEventTypes}
import models.requests.DataRequest
import models.{EROverview, TaxYear}
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
                                  requireData: DataRequiredAction,
                                  userAnswersCacheConnector: UserAnswersCacheConnector,
                                  formProvider: TaxYearFormProvider,
                                  view: TaxYearView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  private val yearsWhereSubmittedVersionAvailable: EROverview => Seq[String] = erOverview =>
    if (erOverview.versionDetails.exists(_.submittedVersionAvailable)) {
      Seq(erOverview.taxYear.startYear)
    } else {
      Nil
    }

  private val yearsWhereCompiledVersionAvailable: EROverview => Seq[String] = erOverview =>
    if (erOverview.versionDetails.exists(_.compiledVersionAvailable)) {
      Seq(erOverview.periodStartDate.getYear.toString)
    } else {
      Nil
    }

  private def renderPage(form: Form[TaxYear], waypoints: Waypoints, status: Status)(implicit request: DataRequest[AnyContent]): Result = {
    val ua = request.userAnswers
    val radioOptions =
      (ua.get(EventReportingTileLinksPage), ua.get(EventReportingOverviewPage)) match {
        case (Some(PastEventTypes), Some(seqEROverview)) =>
          val applicableYears: Seq[String] = seqEROverview.flatMap(yearsWhereSubmittedVersionAvailable)
          TaxYear.optionsFiltered(taxYear => applicableYears.contains(taxYear.startYear))
        case (Some(InProgress), Some(seqEROverview)) =>
          val applicableYears: Seq[String] = seqEROverview.flatMap(yearsWhereCompiledVersionAvailable)
          TaxYear.optionsFiltered(taxYear => applicableYears.contains(taxYear.startYear))
        case _ => TaxYear.options
      }
    status(view(form, waypoints, radioOptions))
  }

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(TaxYearPage).fold(form)(form.fill)
    renderPage(preparedForm, waypoints, Ok)
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(renderPage(formWithErrors, waypoints, BadRequest)),
        value => {
          val originalUserAnswers = request.userAnswers

//          val vv = originalUserAnswers.get(EventReportingOverviewPage).toSeq.flatten.filter(
//            _.periodStartDate
//          )

          val updatedAnswers = originalUserAnswers.setOrException(TaxYearPage, value, nonEventTypeData = true)
          userAnswersCacheConnector.save(request.pstr, updatedAnswers).map { _ =>
            Redirect(TaxYearPage.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
          }
        }
      )
  }

}

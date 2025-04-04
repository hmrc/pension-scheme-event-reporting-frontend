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

package controllers

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.TaxYearFormProvider
import models.enumeration.JourneyStartType.{InProgress, PastEventTypes}
import models.enumeration.VersionStatus.{Compiled, NotStarted, Submitted}
import models.requests.DataRequest
import models.{EROverview, TaxYear, VersionInfo}
import pages.{EventReportingOverviewPage, EventReportingTileLinksPage, TaxYearPage, VersionInfoPage, Waypoints}
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
                                  config: FrontendAppConfig,
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
          TaxYear.optionsFiltered(taxYear => applicableYears.contains(taxYear.startYear), ua.get(TaxYearPage))
        case (Some(InProgress), Some(seqEROverview)) =>
          val applicableYears: Seq[String] = seqEROverview.flatMap(yearsWhereCompiledVersionAvailable)
          TaxYear.optionsFiltered(taxYear => applicableYears.contains(taxYear.startYear), ua.get(TaxYearPage))
        case (Some(_), Some(seqEROverview)) =>
          val yearsInProgressOrSubmitted: Seq[Int] = seqEROverview.collect{
            case x if x.versionDetails.exists(x => x.compiledVersionAvailable || x.submittedVersionAvailable) => x.taxYear.startYear.toInt
          }
          TaxYear.optionsFiltered( taxYear => taxYear.startYear.toInt >= config.eventReportingStartTaxYear &&
                                              !yearsInProgressOrSubmitted.contains(taxYear.startYear.toInt), ua.get(TaxYearPage))
        case _ =>
          TaxYear.optionsFiltered( taxYear =>  taxYear.startYear.toInt >= config.eventReportingStartTaxYear, ua.get(TaxYearPage))
      }

    status(view(form, waypoints, radioOptions, routes.EventReportingOverviewController.onPageLoad(request.pstr).url))
  }

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async { implicit request =>

    Future.successful(renderPage(form, waypoints, Ok))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(renderPage(formWithErrors, waypoints, BadRequest)),
        value => {
          val originalUserAnswers = request.userAnswers
          val vd = originalUserAnswers
            .get(EventReportingOverviewPage).toSeq.flatten.find(_.taxYear == value).flatMap(_.versionDetails)
          val versionInfo =
            (vd.map(_.compiledVersionAvailable), vd.map(_.submittedVersionAvailable), vd.map(_.numberOfVersions)) match {
              case (Some(true), _, Some(versions)) => VersionInfo(versions, Compiled)
              case (_, Some(true), Some(versions)) => VersionInfo(versions, Submitted)
              case _ => VersionInfo(1, NotStarted)
            }

          val futureAfterClearDown = request.userAnswers.get(TaxYearPage) match {
            case Some(v) if v != value => userAnswersCacheConnector.removeAll(request.pstr)
            case _ => Future.successful((): Unit)
          }

          val updatedAnswers = originalUserAnswers
            .setOrException(TaxYearPage, value, nonEventTypeData = true)
            .setOrException(VersionInfoPage, versionInfo, nonEventTypeData = true)
          futureAfterClearDown.flatMap { _ =>
            userAnswersCacheConnector.save(request.pstr, updatedAnswers).map { _ =>
              Redirect(TaxYearPage.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
            }
          }
        }
      )
  }
}

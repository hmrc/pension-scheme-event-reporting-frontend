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
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import controllers.EventReportingOverviewController.OverviewViewModel
import controllers.actions._
import controllers.routes
import forms.{EventSummaryFormProvider, TaxYearFormProvider}
import models.TaxYear.getSelectedTaxYearAsString
import models.{EventSummary, MemberSummaryPath, TaxYear, UserAnswers, VersionInfo}
import models.enumeration.EventType
import models.enumeration.EventType.{Event18, Event20A, Event8A, WindUp}
import models.enumeration.VersionStatus.{Compiled, NotStarted, Submitted}
import models.requests.DataRequest
import pages.{EmptyWaypoints, EventReportingOverviewPage, EventReportingTileLinksPage, EventSummaryPage, TaxYearPage, VersionInfoPage, Waypoints}
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.Message
import views.html.{EventReportingOverviewView, EventSummaryView, TaxYearView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
class EventReportingOverviewController @Inject()(
                                         val controllerComponents: MessagesControllerComponents,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         connector: EventReportingConnector,
                                         userAnswersCacheConnector: UserAnswersCacheConnector,
                                         config: FrontendAppConfig,
                                         view: EventReportingOverviewView
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData() andThen requireData).async { implicit request =>

    val ua = request.userAnswers
    println(s">>>>>>>>>>>>>>> ${ua.get(EventReportingOverviewPage)}")
    println(s">>>>>>>>>>>>>>> ${ua.get(EventReportingTileLinksPage)}")

    val pastYears = ua.get(EventReportingOverviewPage) match {
      case Some(ll) => ll.map( y => (s"${y.taxYear.startYear} to ${y.taxYear.endYear}", routes.EventReportingOverviewController.onSubmit(y.taxYear.startYear).url))
      case _ => Nil
    }

    val ovm = OverviewViewModel(pastYearsAndQuarters = pastYears)
      Future.successful(Ok(view(ovm)))
  }

  def onSubmit(taxYear: String, waypoints: Waypoints = EmptyWaypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>

      println(s"TaxYearController: onSubmit ************ ${request.userAnswers.get(TaxYearPage)}")
          val originalUserAnswers = request.userAnswers
          val vd = originalUserAnswers
            .get(EventReportingOverviewPage).toSeq.flatten.find(_.taxYear.startYear == taxYear).flatMap(_.versionDetails)
          val versionInfo =
            (vd.map(_.compiledVersionAvailable), vd.map(_.submittedVersionAvailable), vd.map(_.numberOfVersions)) match {
              case (Some(true), _, Some(versions)) => VersionInfo(versions, Compiled)
              case (_, Some(true), Some(versions)) => VersionInfo(versions, Submitted)
              case _ => VersionInfo(1, NotStarted)
            }

          val futureAfterClearDown = request.userAnswers.get(TaxYearPage) match {
            case Some(v) if v.startYear != taxYear => userAnswersCacheConnector.removeAll(request.pstr)
            case _ => Future.successful((): Unit)
          }

          val updatedAnswers = originalUserAnswers
            .setOrException(TaxYearPage, TaxYear(taxYear), nonEventTypeData = true)
            .setOrException(VersionInfoPage, versionInfo, nonEventTypeData = true)
          futureAfterClearDown.flatMap { _ =>
            userAnswersCacheConnector.save(request.pstr, updatedAnswers).map { _ =>
              Redirect(TaxYearPage.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
            }
          }
        }


}

object EventReportingOverviewController {
  case class OverviewViewModel(
                                returnUrl: String = "returnUrl",
                                newAftUrl: String = "newAftUrl",
                                paymentsAndChargesUrl: String = "paymentsAndChargesUrl",
                                schemeName: String =  "schemeName",
                                outstandingAmount: String = "outstandingAmount",
                                quartersInProgress: Seq[(String, String)] = Seq(("quartersInProgress1", "quartersInProgress2")),
                                pastYearsAndQuarters: Seq[(String, String)],
                                viewAllPastAftsUrl: String = "viewAllPastAftsUrl"
                              )
}

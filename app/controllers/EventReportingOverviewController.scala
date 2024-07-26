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
import connectors.{AFTFrontendConnector, EventReportingConnector, UserAnswersCacheConnector}
import controllers.actions._
import controllers.partials.EventReportingTileController.{maxEndDateAsString, minStartDateAsString}
import models.enumeration.JourneyStartType.{InProgress, PastEventTypes, StartNew}
import models.enumeration.VersionStatus.{Compiled, NotStarted, Submitted}
import models.{TaxYear, VersionInfo}
import pages.{EmptyWaypoints, EventReportingOverviewPage, EventReportingTileLinksPage, TaxYearPage, VersionInfoPage, Waypoints}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.EventReportingOverviewService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.OverviewViewModel
import views.html.EventReportingOverviewView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EventReportingOverviewController @Inject()(
                                                  val controllerComponents: MessagesControllerComponents,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  connector: EventReportingConnector,
                                                  aftConnector: AFTFrontendConnector,
                                                  appConfig: FrontendAppConfig,
                                                  service: EventReportingOverviewService,
                                                  userAnswersCacheConnector: UserAnswersCacheConnector,
                                                  view: EventReportingOverviewView
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(srn : String): Action[AnyContent] = (identify andThen getData() andThen requireData).async { implicit request =>

    val ua = request.userAnswers



    val ovm = for {
      pastYears <- service.getPastYearsAndUrl(ua, request.pstr)
      inProgressYears <- service.getInProgressYearAndUrl(ua, request.pstr)
      seqEROverview <- connector.getOverview(request.pstr, "ER", minStartDateAsString, maxEndDateAsString)
      outstandingAmount <- aftConnector.getErOutstandingPaymentAmount(srn)
      isAnySubmittedReports = seqEROverview.exists(_.versionDetails.exists(_.submittedVersionAvailable))
      isAnyCompiledReports = seqEROverview.exists(_.versionDetails.exists(_.compiledVersionAvailable))
    } yield OverviewViewModel(pastYears = pastYears, yearsInProgress = inProgressYears, schemeName = request.schemeName,
      outstandingAmount = outstandingAmount.toString(), paymentsAndChargesUrl = service.linkForOutstandingAmount(srn, outstandingAmount.toString()),
      isAnyCompiledReports = isAnyCompiledReports, isAnySubmittedReports = isAnySubmittedReports,
      newEventReportingUrl = appConfig.erStartNewUrl)


      ovm.map( y => Ok(view(y)))
  }

  def onSubmit(taxYear: String, journeyType: String,  waypoints: Waypoints = EmptyWaypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>

      val jt = journeyType match {
        case "InProgress" => InProgress
        case "PastEventTypes" => PastEventTypes
        case "StartNew" => StartNew
      }
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

      jt match {
        case InProgress | PastEventTypes =>
          val updatedAnswers = originalUserAnswers
            .setOrException(TaxYearPage, TaxYear(taxYear), nonEventTypeData = true)
            .setOrException(EventReportingTileLinksPage, jt, nonEventTypeData = true)
            .setOrException(VersionInfoPage, versionInfo, nonEventTypeData = true)
          futureAfterClearDown.flatMap {
            _ =>
              userAnswersCacheConnector.save(request.pstr, updatedAnswers).map {
                _ =>
                  Redirect(TaxYearPage.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
              }
          }
        case StartNew =>
          val updatedAnswers = originalUserAnswers.setOrException(EventReportingTileLinksPage, StartNew, nonEventTypeData = true)
          userAnswersCacheConnector.save(request.pstr, updatedAnswers).map { _ =>
            Redirect(controllers.routes.TaxYearController.onPageLoad(EmptyWaypoints))
          }
      }
  }
}


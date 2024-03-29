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

package controllers.partials

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.enumeration.JourneyStartType._
import models.enumeration.VersionStatus.Compiled
import models.{EROverview, VersionInfo}
import pages.{EmptyWaypoints, EventReportingOverviewPage, EventReportingTileLinksPage, TaxYearPage, VersionInfoPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class EventReportingTileLinksController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   userAnswersCacheConnector: UserAnswersCacheConnector
                                                 )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onClickCompiled: Action[AnyContent] =
    (identify andThen getData() andThen requireData).async { implicit request =>
      request.userAnswers.get(EventReportingOverviewPage) match {
        case Some(s) =>
          val compiledVersionsOnly = s.filter(_.versionDetails.exists(_.compiledVersionAvailable))
          compiledVersionsOnly match {
            case Seq(erOverview) =>

              val version = erOverview.versionDetails.map(_.numberOfVersions).getOrElse(1)
              val versionInfo = VersionInfo(version, Compiled)
              val ua = request.userAnswers
                .setOrException(TaxYearPage, erOverview.taxYear, nonEventTypeData = true)
                .setOrException(EventReportingTileLinksPage, InProgress, nonEventTypeData = true)
                .setOrException(VersionInfoPage, versionInfo, nonEventTypeData = true)

              userAnswersCacheConnector.save(request.pstr, ua).map { _ =>
                Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)
              }
            case _ =>
              val ua = request.userAnswers.setOrException(EventReportingTileLinksPage, InProgress, nonEventTypeData = true)
              userAnswersCacheConnector.save(request.pstr, ua).map { _ =>
                Redirect(controllers.routes.TaxYearController.onPageLoad(EmptyWaypoints).url)
              }
          }
        case _ =>
          val ua = request.userAnswers.setOrException(EventReportingTileLinksPage, InProgress, nonEventTypeData = true)
          userAnswersCacheConnector.save(request.pstr, ua).map { _ =>
            Redirect(controllers.routes.TaxYearController.onPageLoad(EmptyWaypoints).url)
          }
      }
    }

  def onClickNew: Action[AnyContent] =
    (identify andThen getData() andThen requireData).async { implicit request =>
      val ua = request.userAnswers.setOrException(EventReportingTileLinksPage, StartNew, nonEventTypeData = true)
      userAnswersCacheConnector.save(request.pstr, ua).map { _ =>
        Redirect(controllers.routes.TaxYearController.onPageLoad(EmptyWaypoints))
      }
    }

  def onClickSubmitted: Action[AnyContent] =
    (identify andThen getData() andThen requireData).async { implicit request =>
      request.userAnswers.get(EventReportingOverviewPage) match {
        case Some(s: Seq[EROverview]) =>
          val ua = request.userAnswers
            .setOrException(EventReportingTileLinksPage, PastEventTypes, nonEventTypeData = true)
          userAnswersCacheConnector.save(request.pstr, ua).map { _ =>
            Redirect(controllers.routes.TaxYearController.onPageLoad(EmptyWaypoints).url)
          }

        case _ => Future.successful(Redirect(controllers.routes.TaxYearController.onPageLoad(EmptyWaypoints).url))
      }
    }
}

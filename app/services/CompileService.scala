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

package services

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import models.enumeration.EventType
import models.enumeration.VersionStatus.{Compiled, NotStarted, Submitted}
import models.requests.RequiredSchemeDataRequest
import models.{EROverview, EROverviewVersion, EventDataIdentifier, TaxYear, UserAnswers, VersionInfo}
import org.apache.pekko.actor.ActorSystem
import pages.{EventReportingOverviewPage, TaxYearPage, VersionInfoPage}
import play.api.Logging
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future, Promise}

class CompileService @Inject()(
                                eventReportingConnector: EventReportingConnector,
                                userAnswersCacheConnector: UserAnswersCacheConnector,
                                appConfig: FrontendAppConfig,
                                actorSystem: ActorSystem
                              ) (implicit ec: ExecutionContext) extends Logging{

  private def doCompile(currentVersionInfo: VersionInfo,
                        newVersionInfo: VersionInfo,
                        pstr: String,
                        userAnswers: UserAnswers,
                        delete: Boolean,
                        eventOrDelete: Either[EventType, (String, EventDataIdentifier, Int, String)])
                       (implicit headerCarrier: HeaderCarrier, req: RequiredSchemeDataRequest[AnyContent]): Future[Unit] = {

    val futureOptChangedOverviewSeq = if (newVersionInfo.version > currentVersionInfo.version) {
      updateUAVersionAndOverview(pstr, userAnswers, currentVersionInfo.version, newVersionInfo.version)
    } else {
      Future.successful(None)
    }

    futureOptChangedOverviewSeq.flatMap { optChangedOverviewSeq =>
      val uaNonEventTypeVersionUpdated = userAnswers.setOrException(VersionInfoPage, newVersionInfo, nonEventTypeData = true)
      val updatedUA = optChangedOverviewSeq match {
        case Some(seqErOverview) => uaNonEventTypeVersionUpdated
          .setOrException(EventReportingOverviewPage, seqErOverview, nonEventTypeData = true)
        case _ => uaNonEventTypeVersionUpdated
      }

      def delay = appConfig.compileDelayInSeconds match {
        case v if v > 0 =>
          val p = Promise[Unit]()
          actorSystem.scheduler.scheduleOnce(v.seconds)(p.success(()))
          p.future
        case _ => Future.unit
      }

      userAnswersCacheConnector.save(pstr, updatedUA).map { _ =>
        eventOrDelete match {
          case Left(eventTypeVal) =>
            eventReportingConnector.compileEvent(pstr, updatedUA.eventDataIdentifier(eventTypeVal, Some(newVersionInfo)), currentVersionInfo.version, delete)
              .map { _ => delay }
          case Right((pstrVal, edi, currentVersionVal, memberIdToDelete)) =>
            eventReportingConnector.deleteMember(pstrVal, edi, currentVersionVal, memberIdToDelete)
              .map { _ => delay }
        }
      }
    }
  }

  private def updateOverviewSeq(overviewSeq: Seq[EROverview], taxYear: TaxYear, newVersion: Int): Seq[EROverview] = {
    overviewSeq.map { erOverview =>
      if (erOverview.taxYear == taxYear) {
        val newVersionDetails = erOverview.versionDetails.map { erOverviewVersion =>
          EROverviewVersion(
            numberOfVersions = newVersion,
            submittedVersionAvailable = erOverviewVersion.submittedVersionAvailable,
            compiledVersionAvailable = true
          )
        }
        erOverview.copy(versionDetails = newVersionDetails)
      } else {
        erOverview
      }
    }
  }

  private def updateUAVersionAndOverview(pstr: String,
                                         userAnswers: UserAnswers,
                                         version: Int,
                                         newVersion: Int)(implicit headerCarrier: HeaderCarrier, req: RequiredSchemeDataRequest[AnyContent]): Future[Option[Seq[EROverview]]] = {
    userAnswersCacheConnector.changeVersion(pstr, version.toString, newVersion.toString).map { _ =>
      (userAnswers.get(EventReportingOverviewPage), userAnswers.get(TaxYearPage)) match {
        case (Some(overviewSeq), Some(taxYear)) => Some(updateOverviewSeq(overviewSeq, taxYear, newVersion))
        case _ => None
      }
    }
  }

  private def changeVersionInfo(versionInfo: VersionInfo) = {
    versionInfo match {
      case VersionInfo(version, NotStarted) => VersionInfo(version, Compiled)
      case vi@VersionInfo(_, Compiled) => vi
      case VersionInfo(version, Submitted) => VersionInfo(version + 1, Compiled)
    }
  }

  def deleteMember(pstr: String, edi: EventDataIdentifier, currentVersion: Int, memberIdToDelete: String,
                   userAnswers: UserAnswers)(implicit headerCarrier: HeaderCarrier, req: RequiredSchemeDataRequest[AnyContent]): Future[Unit] = {
    userAnswers.get(VersionInfoPage) match {

      case Some(vi)  =>
          val newVersionInfo = changeVersionInfo(vi)
          doCompile(
            vi,
            newVersionInfo,
            pstr,
            userAnswers,
            delete = true,
            eventOrDelete = Right((pstr, edi, currentVersion, memberIdToDelete))
          )

      case _ => throw new RuntimeException(s"No version available")
    }
  }

  def compileEvent(eventType: EventType, pstr: String, userAnswers: UserAnswers, delete: Boolean = false)
                  (implicit headerCarrier: HeaderCarrier, req: RequiredSchemeDataRequest[AnyContent]): Future[Unit] = {

    def compileWithNewVersionInfo(vi: VersionInfo): Future[Unit] = {
      val newVersionInfo = changeVersionInfo(vi)
      doCompile(vi, newVersionInfo, pstr, userAnswers, delete, eventOrDelete = Left(eventType))
    }

    userAnswers.get(VersionInfoPage) match {
      case Some(vi) if vi.status == NotStarted || vi.status == Compiled => compileWithNewVersionInfo(vi)
      case Some(vi) if delete => compileWithNewVersionInfo(vi)
      case Some(vi) => userAnswersCacheConnector.isDataModified(pstr, eventType).flatMap {
        case Some(true) => compileWithNewVersionInfo(vi)
        case Some(false) =>
          logger.warn(s"Data not modified for pstr: $pstr, event: ${eventType}, version: $vi")
          Future.successful(())
        case _ => throw new RuntimeException(s"Data Changed Checks failed for $pstr and $eventType")
      }

      case None => Future.failed(new RuntimeException("No version available"))
    }
  }
}

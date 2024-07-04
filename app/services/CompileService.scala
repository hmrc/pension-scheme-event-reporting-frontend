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
import models.{EROverview, EROverviewVersion, EventDataIdentifier, TaxYear, UserAnswers, VersionInfo}
import org.apache.pekko.actor.ActorSystem
import pages.{EventReportingOverviewPage, TaxYearPage, VersionInfoPage}
import play.api.Logging
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Results.NotFound
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class CompileService @Inject()(
                                eventReportingConnector: EventReportingConnector,
                                userAnswersCacheConnector: UserAnswersCacheConnector,
                                appConfig: FrontendAppConfig,
                                actorSystem: ActorSystem
                              ) (implicit ec: ExecutionContext) extends Logging  {

  private def doCompile(currentVersionInfo: VersionInfo,
                        newVersionInfo: VersionInfo,
                        pstr: String,
                        userAnswers: UserAnswers,
                        delete: Boolean,
                        eventOrDelete: Either[EventType, (String, EventDataIdentifier, Int, String)])(implicit headerCarrier: HeaderCarrier): Future[Unit] = {

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

      val futureAction = eventOrDelete match {
        case Left(eventTypeVal) =>
          eventReportingConnector.compileEvent(pstr, updatedUA.eventDataIdentifier(eventTypeVal, Some(newVersionInfo)), currentVersionInfo.version, delete)
        case Right((pstrVal, edi, currentVersionVal, memberIdToDelete)) =>
          eventReportingConnector.deleteMember(pstrVal, edi, currentVersionVal, memberIdToDelete)
      }
      userAnswersCacheConnector.save(pstr, updatedUA).flatMap{_ => futureAction}
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
        erOverview copy (versionDetails = newVersionDetails)
      } else {
        erOverview
      }
    }
  }

  private def updateUAVersionAndOverview(pstr: String,
                                         userAnswers: UserAnswers,
                                         version: Int,
                                         newVersion: Int)(implicit headerCarrier: HeaderCarrier): Future[Option[Seq[EROverview]]] = {
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



  def deleteMember(pstr: String, edi: EventDataIdentifier, currentVersion: Int, memberIdToDelete: String, userAnswers: UserAnswers)(implicit headerCarrier: HeaderCarrier): Future[Unit] = {
    userAnswers.get(VersionInfoPage) match {
      case Some(vi) =>
        val oldNewFuture = for {
          oldUserAnswers <- userAnswersCacheConnector.get(pstr, edi.eventType)
          newUserAnswers <- userAnswersCacheConnector.get(pstr + "_original_cache", edi.eventType)
        } yield (oldUserAnswers, newUserAnswers)

        oldNewFuture.flatMap {
          case x if isEventDataNotModified(x._1.map(_.data), x._2.map(_.data)) =>
            Future.successful(())
            val newVersionInfo = changeVersionInfo(vi)
            doCompile(
              vi,
              newVersionInfo,
              pstr,
              userAnswers,
              delete = false,
              eventOrDelete = Right((pstr, edi, currentVersion, memberIdToDelete))
            )
        }

      case _ => throw new RuntimeException(s"No version available")
    }
  }
  def compileEvent(eventType: EventType, pstr: String, userAnswers: UserAnswers, delete: Boolean = false)
                  (implicit headerCarrier: HeaderCarrier): Future[Unit] = {

    userAnswers.get(VersionInfoPage) match {
      case Some(vi) =>
        val oldNewFuture = for {
          oldUserAnswers <- userAnswersCacheConnector.get(pstr, eventType)
          newUserAnswers <- userAnswersCacheConnector.get(pstr + "_original_cache", eventType)
        } yield (oldUserAnswers, newUserAnswers)

        oldNewFuture.flatMap {
          case x if isEventDataNotModified(x._1.map(_.data), x._2.map(_.data)) =>
            Future.successful(())

          case _ =>
            val newVersionInfo = changeVersionInfo(vi)
            doCompile(
              vi,
              newVersionInfo,
              pstr,
              userAnswers,
              delete,
              eventOrDelete = Left(eventType)
            )
        }

      case None => Future.failed(new RuntimeException("No version available"))
    }
  }


  private def isEventDataNotModified(oldUserAnswers: Option[JsObject], newUserAnswers: Option[JsObject]) = {
    println(s"*************** $oldUserAnswers")
    println(s"*************** $newUserAnswers")
    oldUserAnswers.getOrElse(Json.obj()) == newUserAnswers.getOrElse(Json.obj())
  }


}

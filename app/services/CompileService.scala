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

package services

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import models.enumeration.EventType
import models.enumeration.VersionStatus.{Compiled, NotStarted, Submitted}
import models.{EROverview, EROverviewVersion, EventDataIdentifier, TaxYear, UserAnswers, VersionInfo}
import pages.{EventReportingOverviewPage, TaxYearPage, VersionInfoPage}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class CompileService @Inject()(
                                eventReportingConnector: EventReportingConnector,
                                userAnswersCacheConnector: UserAnswersCacheConnector,
                                appConfig: FrontendAppConfig
                              ) (implicit ec: ExecutionContext) {


  private def doCompile(currentVersionInfo: VersionInfo,
                newVersionInfo: VersionInfo,
                pstr: String,
                userAnswers: UserAnswers,
                delete :Boolean,
                compileResponse: Future[Unit])(implicit headerCarrier: HeaderCarrier): Future[Unit] = {

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
      userAnswersCacheConnector.save(pstr, updatedUA).flatMap { _ =>
        val response = compileResponse
        response.map { _ =>
          appConfig.compileDelayInSeconds match {
            case v if v > 0 => Thread.sleep(appConfig.compileDelayInSeconds * 1000)
            case _ => (): Unit
          }
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
        val newVersionInfo = changeVersionInfo(vi)
        doCompile(
          vi,
          newVersionInfo,
          pstr,
          userAnswers,
          delete = false,
          eventReportingConnector.deleteMember(pstr, edi, currentVersion, memberIdToDelete)
        )
      case _ => throw new RuntimeException(s"No version available")
    }
  }

  def compileEvent(eventType: EventType, pstr: String, userAnswers: UserAnswers, delete: Boolean = false)(implicit headerCarrier: HeaderCarrier): Future[Unit] = {
    userAnswers.get(VersionInfoPage) match {
      case Some(vi) =>
        val newVersionInfo = changeVersionInfo(vi)
        doCompile(
          vi,
          newVersionInfo,
          pstr,
          userAnswers,
          delete,
          eventReportingConnector.compileEvent(pstr, userAnswers.eventDataIdentifier(eventType, Some(newVersionInfo)), vi.version, delete)
        )
      case _ => throw new RuntimeException(s"No version available")
    }
  }
}

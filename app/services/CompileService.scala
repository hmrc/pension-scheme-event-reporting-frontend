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
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import models.enumeration.EventType
import models.enumeration.VersionStatus.{Compiled, NotStarted, Submitted}
import models.{EROverview, EROverviewVersion, UserAnswers, VersionInfo}
import pages.{EventReportingOverviewPage, TaxYearPage, VersionInfoPage}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class CompileService @Inject()(
                                eventReportingConnector: EventReportingConnector,
                                userAnswersCacheConnector: UserAnswersCacheConnector
                              ) {

  private def updateUAVersionAndOverview(pstr: String,
                                   eventType: EventType,
                                   userAnswers: UserAnswers,
                                   newVersionInfo: VersionInfo)(implicit ec: ExecutionContext,
                                                                headerCarrier: HeaderCarrier): Future[Option[Seq[EROverview]]] = {
    userAnswersCacheConnector.changeVersion(pstr, userAnswers.eventDataIdentifier(eventType), newVersionInfo.version.toString).map { _ =>
      (userAnswers.get(EventReportingOverviewPage), userAnswers.get(TaxYearPage)) match {
        case (Some(overviewSeq), Some(taxYear)) =>
          val newOverviewSeq = overviewSeq.map { erOverview =>
            if (erOverview.taxYear == taxYear) {
              val newVersionDetails = erOverview.versionDetails.map { yy =>
                EROverviewVersion(
                  numberOfVersions = newVersionInfo.version,
                  submittedVersionAvailable = yy.submittedVersionAvailable,
                  compiledVersionAvailable = true
                )
              }
              erOverview copy (versionDetails = newVersionDetails)
            } else {
              erOverview
            }
          }
          Some(newOverviewSeq)
        case _ => None
      }
    }
  }

  def compileEvent(eventType: EventType, pstr: String, userAnswers: UserAnswers)(implicit ec: ExecutionContext,
                                                                                 headerCarrier: HeaderCarrier): Future[Unit] = {
    eventReportingConnector.compileEvent(pstr, userAnswers.eventDataIdentifier(eventType)).flatMap { _ =>
      userAnswers.get(VersionInfoPage) match {
        case Some(vi) =>
          val newVersionInfo = vi match {
            case VersionInfo(version, NotStarted) => VersionInfo(version, Compiled)
            case vi@VersionInfo(_, Compiled) => vi
            case VersionInfo(version, Submitted) => VersionInfo(version + 1, Compiled)
          }
          val futureOptChangedOverviewSeq = if (newVersionInfo.version > vi.version) {
            updateUAVersionAndOverview(pstr, eventType, userAnswers, newVersionInfo)
          } else {
            Future.successful(None)
          }

          futureOptChangedOverviewSeq.flatMap { optChangedOverviewSeq =>
            def uaNonEventTypeVersionUpdated: UserAnswers =
              userAnswers.setOrException(VersionInfoPage, newVersionInfo, nonEventTypeData = true)
            val updatedUA = optChangedOverviewSeq match {
              case Some(seqErOverview) => uaNonEventTypeVersionUpdated
                .setOrException(EventReportingOverviewPage, seqErOverview, nonEventTypeData = true)
              case _ => uaNonEventTypeVersionUpdated
            }
            userAnswersCacheConnector.save(pstr, updatedUA)
          }
        case _ => throw new RuntimeException(s"No version available")
      }
    }
  }
}

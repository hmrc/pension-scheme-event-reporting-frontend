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
import models.{UserAnswers, VersionInfo}
import pages.VersionInfoPage
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class CompileService @Inject()(
                              eventReportingConnector: EventReportingConnector,
                              userAnswersCacheConnector: UserAnswersCacheConnector
                              ){

  def compileEvent(eventType: EventType, pstr: String, userAnswers: UserAnswers)(implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Unit] = {
    eventReportingConnector.compileEvent(pstr, userAnswers.eventDataIdentifier(eventType)).flatMap{ _ =>
      val newVersionInfo = userAnswers.get(VersionInfoPage) match {
        case Some(vi) =>
          vi match {
            case VersionInfo(version, NotStarted) => VersionInfo(version, Compiled)
            case vi@VersionInfo(_, Compiled) => vi
            case VersionInfo(version, Submitted) => VersionInfo(version + 1, Compiled)
          }
        case _ => throw new RuntimeException(s"No version available")
      }
      val updatedUA = userAnswers.setOrException(VersionInfoPage, newVersionInfo, nonEventTypeData = true)
      userAnswersCacheConnector.save(pstr, updatedUA)
    }
  }
}

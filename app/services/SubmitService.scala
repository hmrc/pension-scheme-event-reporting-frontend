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
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import models.enumeration.VersionStatus.{Compiled, Submitted}
import models.requests.{DataRequest, RequiredSchemeDataRequest}
import models.{UserAnswers, VersionInfo}
import pages.VersionInfoPage
import play.api.Logger
import play.api.http.Status.NO_CONTENT
import play.api.mvc.{AnyContent, Result}
import play.api.mvc.Results.{BadRequest, NotFound, Ok}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class SubmitService @Inject()(
                               eventReportingConnector: EventReportingConnector,
                               userAnswersCacheConnector: UserAnswersCacheConnector
                             ) {

  private val logger = Logger(classOf[SubmitService])

  def submitReport(pstr: String, ua: UserAnswers)
                  (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier, req: RequiredSchemeDataRequest[AnyContent]): Future[Result] = {
    ua.get(VersionInfoPage) match {
      case Some(VersionInfo(version, Compiled)) =>
        eventReportingConnector.submitReport(pstr, ua, version.toString).flatMap { response =>
          if (response.header.status == NO_CONTENT) {
            val updatedUA = ua.setOrException(VersionInfoPage, VersionInfo(version, Submitted), nonEventTypeData = true)
            userAnswersCacheConnector.save(pstr, updatedUA).map { _ => Ok }
          } else {
            logger.warn(s"The event report has already been submitted with status: ${Compiled.toString}")
            Future.successful(BadRequest("The event report has already been submitted"))
          }
        }
      case Some(VersionInfo(_, Submitted)) =>
        logger.warn(s"The event report has already been submitted with status: ${Submitted.toString}")
        Future.successful(BadRequest("The event report has already been submitted"))
      case Some(vi) =>
        logger.warn(s"No compiled version to submit! Version info is $vi")
        Future.successful(NotFound(s"No compiled version to submit! Version info is $vi"))
      case _ =>
        Future.successful(NotFound("No version info found"))
    }
  }

}

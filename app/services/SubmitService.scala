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
import models.requests.RequiredSchemeDataRequest
import models.{UserAnswers, VersionInfo}
import pages.VersionInfoPage
import play.api.Logger
import play.api.http.Status.{NO_CONTENT, SERVICE_UNAVAILABLE}
import play.api.mvc.{AnyContent, Result}
import play.api.mvc.Results.{BadRequest, NotFound, Ok, ServiceUnavailable}
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
          response.header.status match {
            case NO_CONTENT => val updatedUA = ua.setOrException(VersionInfoPage, VersionInfo(version, Submitted), nonEventTypeData = true)
              userAnswersCacheConnector.save(pstr, updatedUA).map { _ => Ok }
            case SERVICE_UNAVAILABLE             => Future.successful(ServiceUnavailable("IFS Service returned service unavailable"))
            case _ => logger.warn(s"The event report has already been submitted with status: ${Compiled.toString}, ${response.header.status}")
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

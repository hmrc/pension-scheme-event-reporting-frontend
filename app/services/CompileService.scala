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
import connectors.EventReportingConnector
import models.UserAnswers
import models.enumeration.EventType
import models.requests.DataRequest
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class CompileService @Inject()(
                              eventReportingConnector: EventReportingConnector
                              ){
  def compileEvent(eventType: EventType, pstr: String, userAnswers: UserAnswers)(implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Unit] = {
    val edi = userAnswers.eventDataIdentifier(eventType) // Version is so can retrieve in BE from user answers
    // Need to get version from user answers not hard code in above
    eventReportingConnector.compileEvent(pstr, edi)
    // Then after successful compile do point c) on 8520 ticket
  }
}

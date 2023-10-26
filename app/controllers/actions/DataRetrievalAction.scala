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

package controllers.actions

import com.google.inject.ImplementedBy
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import models.JourneyDataEntry
import models.enumeration.EventType
import models.requests.{IdentifierRequest, OptionalDataRequest}
import play.api.mvc.ActionTransformer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataRetrievalService @Inject() (eventReportingConnector: EventReportingConnector) {
  def getEventReportingData[A](implicit request: IdentifierRequest[A], hc:HeaderCarrier): Future[JourneyDataEntry] = {
    println(request.uri)
    println(request.rawQueryString)
    val journeyId = request.queryString
      .getOrElse(
        "journeyId",
        throw new RuntimeException("journeyId query parameter not available")
      ).head
    eventReportingConnector.getJourneyData(journeyId)
  }
}

class DataRetrievalImpl(eventType: EventType,
                        userAnswersCacheConnector: UserAnswersCacheConnector,
                        dataRetrievalService: DataRetrievalService
                       )(implicit val executionContext: ExecutionContext)
  extends DataRetrieval {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    for {
      journeyData <- dataRetrievalService.getEventReportingData(request, hc)
      data <- userAnswersCacheConnector.get(journeyData.journeyId, journeyData.pstr, eventType)
    } yield {
      println(journeyData)
      OptionalDataRequest[A](journeyData.pstr, journeyData.schemeName, journeyData.returnUrl, request, request.loggedInUser, data, journeyData.journeyId)
    }
  }
}

class DataRetrievalNoEventTypeImpl(userAnswersCacheConnector: UserAnswersCacheConnector,
                                   dataRetrievalService: DataRetrievalService
                                  )(implicit val executionContext: ExecutionContext)
  extends DataRetrieval {



  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    for {
      journeyData <- dataRetrievalService.getEventReportingData(request, hc)
      data <- userAnswersCacheConnector.get(journeyData.journeyId, journeyData.pstr)
    } yield {
      OptionalDataRequest[A](journeyData.pstr, journeyData.schemeName, journeyData.returnUrl, request, request.loggedInUser, data, journeyData.journeyId)
    }
  }
}

class DataRetrievalActionImpl @Inject()(
                                         userAnswersCacheConnector: UserAnswersCacheConnector,
                                         dataRetrievalService: DataRetrievalService
                                       )
                                       (implicit val executionContext: ExecutionContext)
  extends DataRetrievalAction {
  override def apply(eventType: EventType): DataRetrieval =
    new DataRetrievalImpl(eventType, userAnswersCacheConnector, dataRetrievalService)

  override def apply(): DataRetrieval =
    new DataRetrievalNoEventTypeImpl(userAnswersCacheConnector, dataRetrievalService)
}

@ImplementedBy(classOf[DataRetrievalImpl])
trait DataRetrieval extends ActionTransformer[IdentifierRequest, OptionalDataRequest]

@ImplementedBy(classOf[DataRetrievalActionImpl])
trait DataRetrievalAction {
  def apply(eventType: EventType): DataRetrieval

  def apply(): DataRetrieval
}

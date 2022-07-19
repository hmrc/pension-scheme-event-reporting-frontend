/*
 * Copyright 2022 HM Revenue & Customs
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
import connectors.UserAnswersCacheConnector
import models.enumeration.EventType
import models.requests.{IdentifierRequest, OptionalDataRequest}
import play.api.Logger
import play.api.mvc.ActionTransformer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class DataRetrievalImpl(
                         pstr: String,
                         eventType: EventType,
                         userAnswersCacheConnector: UserAnswersCacheConnector
                       )(implicit val executionContext: ExecutionContext)
  extends DataRetrieval {
  private val logger = Logger(classOf[DataRetrievalImpl])

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val result = for {
      data <- userAnswersCacheConnector.get(pstr, eventType)
    } yield {
      // TODO: What should userId be? For now make it internal ID but should probably be PSA ID/ PSP ID
      OptionalDataRequest[A](request, request.userId, data)
    }
    result andThen {
      case Success(v) => logger.info("Successful response to data retrieval:" + v)
      case Failure(t: Throwable) => logger.warn("Unable to complete dataretrieval", t)
    }
  }
}

class DataRetrievalActionImpl @Inject()(userAnswersCacheConnector: UserAnswersCacheConnector)
                                       (implicit val executionContext: ExecutionContext)
  extends DataRetrievalAction {
  override def apply(pstr: String, eventType: EventType): DataRetrieval =
    new DataRetrievalImpl(pstr, eventType, userAnswersCacheConnector)
}

@ImplementedBy(classOf[DataRetrievalImpl])
trait DataRetrieval extends ActionTransformer[IdentifierRequest, OptionalDataRequest]

@ImplementedBy(classOf[DataRetrievalActionImpl])
trait DataRetrievalAction {
  def apply(pstr: String, eventType: EventType): DataRetrieval
}

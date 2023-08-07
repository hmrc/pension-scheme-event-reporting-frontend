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
import connectors.UserAnswersCacheConnector
import models.enumeration.EventType
import models.requests.{IdentifierRequest, OptionalDataRequest}
import pages.VersionInfoPage
import play.api.Logger
import play.api.mvc.ActionTransformer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalImpl(eventType: EventType,
                        userAnswersCacheConnector: UserAnswersCacheConnector
                       )(implicit val executionContext: ExecutionContext)
  extends DataRetrieval {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    for {
      data <- userAnswersCacheConnector.get(request.pstr, eventType)
    } yield {
      data.map{ uA => uA.get(VersionInfoPage) match {
        case Some(value) => value.version
      }

      }
      OptionalDataRequest[A](request.pstr, request.schemeName, request.returnUrl, request, request.loggedInUser, data)
    }
  }
}

class DataRetrievalNoEventTypeImpl(userAnswersCacheConnector: UserAnswersCacheConnector
                                  )(implicit val executionContext: ExecutionContext)
  extends DataRetrieval {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    for {
      data <- userAnswersCacheConnector.get(request.pstr)
    } yield {
      OptionalDataRequest[A](request.pstr, request.schemeName, request.returnUrl, request, request.loggedInUser, data)
    }
  }
}

class DataRetrievalActionImpl @Inject()(
                                         userAnswersCacheConnector: UserAnswersCacheConnector
                                       )
                                       (implicit val executionContext: ExecutionContext)
  extends DataRetrievalAction {
  override def apply(eventType: EventType): DataRetrieval =
    new DataRetrievalImpl(eventType, userAnswersCacheConnector)

  override def apply(): DataRetrieval =
    new DataRetrievalNoEventTypeImpl(userAnswersCacheConnector)
}

@ImplementedBy(classOf[DataRetrievalImpl])
trait DataRetrieval extends ActionTransformer[IdentifierRequest, OptionalDataRequest]

@ImplementedBy(classOf[DataRetrievalActionImpl])
trait DataRetrievalAction {
  def apply(eventType: EventType): DataRetrieval

  def apply(): DataRetrieval
}

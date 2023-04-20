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

import models.enumeration.AdministratorOrPractitioner.{Administrator, Practitioner}
import models.enumeration.EventType
import models.requests.{IdentifierRequest, OptionalDataRequest}
import models.{LoggedInUser, UserAnswers}

import scala.concurrent.{ExecutionContext, Future}

class FakeDataRetrievalAction(json: Option[UserAnswers]) extends DataRetrievalAction {
  override def apply(eventType: EventType): DataRetrieval = new FakeDataRetrieval(json)
  override def apply(): DataRetrieval = new FakeDataRetrieval(json)
}

class FakeDataRetrievalActionForPSP(json: Option[UserAnswers]) extends DataRetrievalAction {
  override def apply(eventType: EventType): DataRetrieval = new FakeDataRetrievalForPSP(json)
  override def apply(): DataRetrieval = new FakeDataRetrievalForPSP(json)
}

class FakeDataRetrieval(dataToReturn: Option[UserAnswers]) extends DataRetrieval {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] =
    Future(OptionalDataRequest("87219363YN", "schemeName", "returnUrl", request.request, LoggedInUser("user-id", Administrator, "psaId"), dataToReturn))

  override protected implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}

class FakeDataRetrievalForPSP(dataToReturn: Option[UserAnswers]) extends DataRetrieval {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] =
    Future(OptionalDataRequest("87219363YN", "schemeName", "returnUrl", request.request, LoggedInUser("user-id", Practitioner, "pspId"), dataToReturn))

  override protected implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}


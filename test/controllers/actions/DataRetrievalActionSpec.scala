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

import base.SpecBase
import connectors.UserAnswersCacheConnector
import models.enumeration.AdministratorOrPractitioner.Administrator
import models.{LoggedInUser, UserAnswers}
import models.enumeration.EventType
import models.requests.{IdentifierRequest, OptionalDataRequest}
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import play.api.mvc.AnyContent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class DataRetrievalActionSpec extends SpecBase with MockitoSugar with ScalaFutures {

  val userAnswersCacheConnector: UserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private val loggedInUser = LoggedInUser("user", Administrator, "psaId")
  private val request: IdentifierRequest[AnyContent] = IdentifierRequest(fakeRequest, loggedInUser)
  private val pstr = "123"
  private val eventType = EventType.Event1

  private val json = Json.obj("test" -> "test")
  private val userAnswers = UserAnswers(json)

  class Harness extends DataRetrievalImpl(pstr, eventType, userAnswersCacheConnector) {
    def callTransform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
  }


  "Data Retrieval Action when there is no data in the cache" - {
    "must set userAnswers to 'None' in the request" in {
      when(userAnswersCacheConnector.get(eqTo(pstr), eqTo(eventType))(any(), any())) thenReturn Future(None)
      val action = new Harness

      val expectedResult = OptionalDataRequest(request, loggedInUser, None)
      val futureResult = action.callTransform(request)

      whenReady(futureResult) { result =>
        result.userAnswers.isEmpty mustBe true
        result mustBe expectedResult
      }
    }
  }

  "Data Retrieval Action when there is data in the cache" - {
    "must build a userAnswers object and add it to the request" in {
      when(userAnswersCacheConnector.get(eqTo(pstr), eqTo(eventType))(any(), any())) thenReturn Future(Some(userAnswers))
      val action = new Harness

      val expectedResult = OptionalDataRequest(request, loggedInUser, Some(userAnswers))
      val futureResult = action.callTransform(request)

      whenReady(futureResult) { result =>
        result.userAnswers.isDefined mustBe true
        result mustBe expectedResult
      }
    }
  }
}

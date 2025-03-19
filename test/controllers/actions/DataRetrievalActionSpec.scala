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

package controllers.actions

import base.SpecBase
import connectors.UserAnswersCacheConnector
import models.LoggedInUser
import models.enumeration.AdministratorOrPractitioner.Administrator
import models.enumeration.EventType
import models.requests.{IdentifierRequest, OptionalDataRequest}
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.AnyContent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class DataRetrievalActionSpec extends SpecBase with MockitoSugar with ScalaFutures with BeforeAndAfterEach {

  val userAnswersCacheConnector: UserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private val loggedInUser = LoggedInUser("user", Administrator, "psaId")
  private val pstr = "pstr"
  private val srn = "S2400000041"
  private val request: IdentifierRequest[AnyContent] = IdentifierRequest(fakeRequest, loggedInUser, pstr, "schemeName", "returnUrl", srn)
  private val eventType = EventType.Event1

  class Harness extends DataRetrievalImpl(eventType, userAnswersCacheConnector) {
    def callTransform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(userAnswersCacheConnector)
  }

  "Data Retrieval Action when there is no data in the cache" - {
    "must set userAnswers to 'None' in the request" in {
      when(userAnswersCacheConnector.getBySrn(eqTo(pstr), eqTo(eventType), eqTo(srn))(any(), any())) thenReturn Future(None)
      when(userAnswersCacheConnector.getBySrn(eqTo(pstr), eqTo(srn))(any(), any())) thenReturn Future(None)
      val action = new Harness

      val expectedResult = OptionalDataRequest(pstr, "schemeName", "returnUrl", request, loggedInUser, None, srn)
      val futureResult = action.callTransform(request)

      whenReady(futureResult) { result =>
        result.userAnswers.isEmpty mustBe true
        result mustBe expectedResult
      }
    }
  }
}

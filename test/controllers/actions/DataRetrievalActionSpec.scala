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
import org.scalatestplus.mockito.MockitoSugar

class DataRetrievalActionSpec extends SpecBase with MockitoSugar {

  "Data Retrieval Action" - {

    // TODO: Commented for now - will need to decide how to do data retrieval

//    "when there is no data in the cache" - {
//
//      "must set userAnswers to 'None' in the request" in {
//
//        val sessionRepository = mock[SessionRepository]
//        when(sessionRepository.get("id")) thenReturn Future(None)
//        val action = new Harness(sessionRepository)
//
//        val result = action.callTransform(IdentifierRequest(FakeRequest(), "id")).futureValue
//
//        result.userAnswers must not be defined
//      }
//    }
//
//    "when there is data in the cache" - {
//
//      "must build a userAnswers object and add it to the request" in {
//
//        val sessionRepository = mock[SessionRepository]
//        when(sessionRepository.get("id")) thenReturn Future(Some(UserAnswers("id")))
//        val action = new Harness(sessionRepository)
//
//        val result = action.callTransform(new IdentifierRequest(FakeRequest(), "id")).futureValue
//
//        result.userAnswers mustBe defined
//      }
//    }
  }
}

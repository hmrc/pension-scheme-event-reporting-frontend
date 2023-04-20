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

package controllers

import base.SpecBase
import connectors.UserAnswersCacheConnector
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.DeclarationView
import org.mockito.Mockito._
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future

class DeclarationControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
  }

  "Declaration Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.DeclarationController.onPageLoad(waypoints).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclarationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(routes.DeclarationController.onClick(waypoints).url)(request, messages(application)).toString
      }
    }

    //TODO - update tests and json data for submitting event report/ connector once sufficient data is captured in the FE (separate ticket being raised)

    "must redirect to the correct page for method onClick" in {

      val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockUserAnswersCacheConnector.save(any(), any(), uaCaptor.capture())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.DeclarationController.onClick(waypoints).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ReturnSubmittedController.onPageLoad(waypoints).url
      }
    }
  }
}
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

package controllers.event18

import base.SpecBase
import connectors.UserAnswersCacheConnector
import models.enumeration.EventType.Event18
import models.enumeration.VersionStatus.Compiled
import models.{TaxYear, UserAnswers, VersionInfo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.event18.Event18ConfirmationPage
import pages.{EmptyWaypoints, TaxYearPage, VersionInfoPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.CompileService
import views.html.event18.Event18ConfirmationView

import scala.concurrent.Future

class Event18ConfirmationControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockCompileService = mock[CompileService]

  private def getRoute: String = routes.Event18ConfirmationController.onPageLoad(waypoints).url

  private def getRouteOnClick: String = routes.Event18ConfirmationController.onClick(waypoints).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector),
    bind[CompileService].toInstance(mockCompileService)
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
    reset(mockCompileService)
  }

  private val validAnswer: Boolean = true

  "Event18Confirmation Controller onPageLoad" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[Event18ConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(getRouteOnClick, waypoints)(request, messages(application)).toString
      }
    }
  }

  "Event18Confirmation Controller onClick" - {
    "must always save the answer 'true' and redirect to the next page" in {
      val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockUserAnswersCacheConnector.save(any(), any(), uaCaptor.capture())(any(), any()))
        .thenReturn(Future.successful(()))
      when(mockCompileService.compileEvent(ArgumentMatchers.eq(Event18), ArgumentMatchers.eq("87219363YN"), any(), any())(any()))
        .thenReturn(Future.successful(()))

      val ua = emptyUserAnswers
        .setOrException(TaxYearPage, TaxYear("2020"), nonEventTypeData = true)
        .setOrException(VersionInfoPage, VersionInfo(1, Compiled))

      val application =
        applicationBuilder(userAnswers = Some(ua), extraModules)
          .build()
      running(application) {
        val request = FakeRequest(GET, getRouteOnClick)

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(Event18ConfirmationPage, validAnswer).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual Event18ConfirmationPage.navigate(waypoints, emptyUserAnswers, updatedAnswers).url
        uaCaptor.getValue.get(Event18ConfirmationPage) mustBe Some(true)
      }
    }
  }
}

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

package controllers.common

import base.SpecBase
import connectors.UserAnswersCacheConnector
import forms.common.ManualOrUploadFormProvider
import models.{TaxYear, VersionInfo}
import models.common.ManualOrUpload
import models.enumeration.{EventType, VersionStatus}
import models.enumeration.EventType.{Event1, Event22, Event23, Event24, Event6}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, TaxYearPage, VersionInfoPage}
import pages.common.ManualOrUploadPage
import play.api.data.Form
import play.api.inject
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.common.ManualOrUploadView

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class ManualOrUploadControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val seqOfEvents = Seq(Event1, Event6, Event22, Event23, Event24)
  private val waypoints = EmptyWaypoints
  private val manualOrUploadFormProvider = new ManualOrUploadFormProvider()
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    inject.bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )
  private def form(eventType: EventType): Form[ManualOrUpload] = manualOrUploadFormProvider(eventType)

  private def getRoute(eventType: EventType): String = routes.ManualOrUploadController.onPageLoad(waypoints, eventType, 0).url

  private def postRoute(eventType: EventType): String = routes.ManualOrUploadController.onSubmit(waypoints, eventType, 0).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
  }

  "ManualOrUploadController" - {
    for (event <- seqOfEvents) {
      testReturnOkAndCorrectView(event)
      testSaveAnswerAndRedirectWhenValid(event)
      testBadRequestForInvalidDataSubmission(event)
    }
  }

  private def testReturnOkAndCorrectView(eventType: EventType): Unit = {
    s"must return OK and the correct view for a GET for Event $eventType" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute(eventType))

        val result = route(application, request).value

        val view = application.injector.instanceOf[ManualOrUploadView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view.render(form(eventType), waypoints, eventType, index = 0, request, messages(application)).toString
      }

      Await.result(application.stop(), 10.seconds)
    }
  }

  private def testSaveAnswerAndRedirectWhenValid(eventType: EventType): Unit = {
    s"must save the answer and redirect to the next page when valid data is submitted for Event $eventType" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))

      val ua = emptyUserAnswersWithTaxYear.setOrException(TaxYearPage, TaxYear("2022"))
        .setOrException(VersionInfoPage, VersionInfo(1, VersionStatus.Submitted))

      val application = applicationBuilder(userAnswers = Some(ua), extraModules).build()

      running(application) {
        val request = FakeRequest(POST, postRoute(eventType)).withFormUrlEncodedBody(("value", ManualOrUpload.values.head.toString))
        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(ManualOrUploadPage(eventType, 0), ManualOrUpload.values.head).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual ManualOrUploadPage(eventType, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
      }

      Await.result(application.stop(), 10.seconds)
    }
  }

  private def testBadRequestForInvalidDataSubmission(eventType: EventType): Unit = {
    s"must return bad request when invalid data is submitted for Event $eventType" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, postRoute(eventType)).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[ManualOrUploadView]
        val boundForm = form(eventType).bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result).removeAllNonces()mustEqual view.render(boundForm, waypoints, eventType, index = 0, request, messages(application)).toString
      }

      Await.result(application.stop(), 10.seconds)
    }
  }

}

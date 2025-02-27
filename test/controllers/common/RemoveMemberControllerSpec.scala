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
import connectors.EventReportingConnector
import data.SampleData.{memberDetails, sampleMemberJourneyDataEvent1, sampleMemberJourneyDataEvent3and4and5}
import forms.common.RemoveMemberFormProvider
import models.{EventDataIdentifier, UserAnswers, VersionInfo}
import models.enumeration.EventType.{Event1, Event5}
import models.enumeration.JourneyStartType.InProgress
import models.enumeration.VersionStatus.{Compiled, NotStarted}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.{EmptyWaypoints, VersionInfoPage}
import pages.common.RemoveMemberPage
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.CompileService
import views.html.common.RemoveMemberView

import scala.concurrent.Future

class RemoveMemberControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val waypoints = EmptyWaypoints
  private val formProvider = new RemoveMemberFormProvider()
  private val formEvent1 = formProvider("unauthorised payment")
  private val event1TypeMessage = messages(s"eventDescription.event1")
  private val event5TypeMessage = messages(s"eventDescription.event5")
  private val memberName = memberDetails.fullName
  private val event1Title = messages("removeMember.title", event1TypeMessage, memberName)
  private val event1Heading = messages("removeMember.heading", event1TypeMessage, memberName)
  private val event5Title = messages("removeMember.title", event5TypeMessage, memberName)
  private val event5Heading = messages("removeMember.heading", event5TypeMessage, memberName)
  private val formEvent5 = formProvider("cessation of ill-health pension")
  private val mockEventReportingConnector = mock[EventReportingConnector]
  private val mockCompileService = mock[CompileService]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[EventReportingConnector].toInstance(mockEventReportingConnector),
    bind[CompileService].toInstance(mockCompileService)
  )

  private def getRouteForEvent1: String = routes.RemoveMemberController.onPageLoad(waypoints, Event1, 0).url

  private def getRoute: String = routes.RemoveMemberController.onPageLoad(waypoints, Event5, 0).url

  private def postRouteForEvent1: String = routes.RemoveMemberController.onSubmit(waypoints, Event1, 0).url

  private def postRoute: String = routes.RemoveMemberController.onSubmit(waypoints, Event5, 0).url

  override def beforeEach(): Unit = {
    super.beforeEach
    reset(mockEventReportingConnector)
    reset(mockCompileService)
  }

  "RemoveMember Controller" - {
    "must return OK and the correct view for a GET in event 1" in {
      val application = applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent1)).build()

      running(application) {
        val request = FakeRequest(GET, getRouteForEvent1)
        val result = route(application, request).value
        val view = application.injector.instanceOf[RemoveMemberView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view.render(
          formEvent1,
          waypoints,
          Event1,
          event1Title,
          event1Heading,
          index = 0,
          request,
          messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET in an event that isn't event 1" in {
      val application = applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent3and4and5(Event5))).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[RemoveMemberView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view.render(
          formEvent5,
          waypoints,
          Event5,
          event5Title,
          event5Heading,
          index = 0,
          request = request,
          messages = messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered for event 1" in {
      val userAnswers = sampleMemberJourneyDataEvent1.set(RemoveMemberPage(Event1, 0), true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, getRouteForEvent1)
        val view = application.injector.instanceOf[RemoveMemberView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view.render(
          formEvent1.fill(true),
          waypoints,
          Event1,
          event1Title,
          event1Heading,
          index = 0,
          request = request,
          messages = messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered in an event that isn't event 1" in {
      val userAnswers = sampleMemberJourneyDataEvent3and4and5(Event5).set(RemoveMemberPage(Event5, 0), true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      running(application) {
        val request = FakeRequest(GET, getRoute)
        val view = application.injector.instanceOf[RemoveMemberView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view.render(
          formEvent5.fill(true),
          waypoints,
          Event5,
          event5Title,
          event5Heading,
          index = 0,
          request = request,
          messages = messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted  for event 1" in {
      when(mockEventReportingConnector.deleteMember(any(), any(), any(), any())(any(), any())).thenReturn(Future.successful())

      when(mockCompileService.deleteMember(any(), any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful())

      val application = applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent1), extraModules).build()
      running(application) {
        val request = FakeRequest(POST, postRouteForEvent1).withFormUrlEncodedBody(("value", "true"))
        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(RemoveMemberPage(Event1, 0), true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual RemoveMemberPage(Event1, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted in an event that isn't event 1" in {
      when(mockEventReportingConnector.deleteMember(any(), any(), any(), any())(any(), any())).thenReturn(Future.successful())

      when(mockCompileService.deleteMember(any(), any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful())

      val application = applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent3and4and5(Event5)), extraModules).build()
      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "true"))
        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(RemoveMemberPage(Event5, 0), true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual RemoveMemberPage(Event5, 0).navigate(waypoints, emptyUserAnswers, updatedAnswers).url
      }
    }

    "must return bad request when invalid data is submitted for event 1" in {
      val application = applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent1), extraModules).build()

      running(application) {
        val request = FakeRequest(POST, postRouteForEvent1).withFormUrlEncodedBody(("value", "invalid"))
        val view = application.injector.instanceOf[RemoveMemberView]
        val boundForm = formEvent1.bind(Map("value" -> "invalid"))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result).removeAllNonces()mustEqual view.render(
          boundForm,
          waypoints,
          Event1,
          event1Title,
          event1Heading,
          index = 0,
          request = request,
          messages = messages(application)).toString
      }
    }

    "must return bad request when invalid data is submitted in an event that isn't event 1" in {
      val application = applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent3and4and5(Event5)), extraModules).build()

      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))
        val view = application.injector.instanceOf[RemoveMemberView]
        val boundForm = formEvent5.bind(Map("value" -> "invalid"))
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result).removeAllNonces()mustEqual view.render(
          boundForm,
          waypoints,
          Event5,
          event5Title,
          event5Heading,
          index = 0,
          request = request,
          messages = messages(application)).toString
      }
    }
  }
}

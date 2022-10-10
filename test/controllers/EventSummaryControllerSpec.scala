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

package controllers

import base.SpecBase
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import forms.EventSummaryFormProvider
import models.UserAnswers
import models.enumeration.EventType
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.{mock, reset}
import org.scalatest.BeforeAndAfterEach
import pages.{EmptyWaypoints, EventSummaryPage, TestYesNoPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Key, SummaryListRow, Text, Value}
import viewmodels.Message
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.Future
import views.html.EventSummaryView

class EventSummaryControllerSpec extends SpecBase with SummaryListFluency with BeforeAndAfterEach{
  private val mockEventReportSummaryConnector = mock[EventReportingConnector]
  private val waypoints = EmptyWaypoints
  private def postRoute: String = routes.EventSummaryController.onSubmit(waypoints).url

  override protected def beforeEach(): Unit = {
    reset(mockEventReportSummaryConnector)
  }

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[EventReportingConnector].toInstance(mockEventReportSummaryConnector)
  )

  "Event Summary Controller" - {

    "must return OK and the correct view for a GET" in {

      val ua = emptyUserAnswers.setOrException(TestYesNoPage, true)

      val seqOfEvents = Seq(EventType.Event1, EventType.Event2)

      when(mockEventReportSummaryConnector.getEventReportSummary(any())(any(), any())).thenReturn(
        Future.successful(seqOfEvents)
      )

      val application = applicationBuilder(userAnswers = Some(ua), extraModules).build()

      val view = application.injector.instanceOf[EventSummaryView]

      running(application) {
        val request = FakeRequest(GET, routes.EventSummaryController.onPageLoad().url)

        val result = route(application, request).value

        val formProvider = new EventSummaryFormProvider()
        val form = formProvider()

        val mappedEvents = seqOfEvents.map{ event =>
          SummaryListRow(
            key = Key(
              content = Text("Event"),
              classes = "govuk-visually-hidden"
            ),
            value = Value(
              content = Text(Message(s"eventSummary.event${event.toString}")),
              classes = "govuk-!-font-weight-bold govuk-!-width-full"
            ),
            actions = Some(Actions(
              items = Seq(
                ActionItem(
                  href = "",
                  content = Text("Change"),
                  visuallyHiddenText = Some(Message(s"eventSummary.event${event.toString}"))
                )
              )
            ))
          )
        }

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, mappedEvents)(request, messages(application)).toString
      }
    }

    "must redirect to next page on submit (when selecting YES)" in {

      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val userAnswerUpdated = UserAnswers().setOrException(EventSummaryPage, true)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual EventSummaryPage.navigate(waypoints, userAnswerUpdated, userAnswerUpdated).url
      }
    }

    "must redirect to next page on submit (when selecting NO)" in {

      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value
        val userAnswerUpdated = UserAnswers().setOrException(EventSummaryPage, false)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual EventSummaryPage.navigate(waypoints, userAnswerUpdated, userAnswerUpdated).url
      }
    }
  }
}

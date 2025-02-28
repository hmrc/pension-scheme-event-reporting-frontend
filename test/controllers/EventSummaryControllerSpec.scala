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

package controllers

import base.SpecBase
import connectors.EventReportingConnector
import forms.EventSummaryFormProvider
import models.enumeration.EventType
import models.enumeration.EventType._
import models.enumeration.VersionStatus.{Compiled, Submitted}
import models.{EROverview, EROverviewVersion, EventSummary, TaxYear, UserAnswers, VersionInfo}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, EventReportingOverviewPage, EventSummaryPage, TaxYearPage, VersionInfoPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases._
import viewmodels.Message
import viewmodels.govuk.SummaryListFluency
import views.html.EventSummaryView

import java.time.LocalDate
import scala.concurrent.Future

class EventSummaryControllerSpec extends SpecBase with SummaryListFluency with BeforeAndAfterEach with MockitoSugar {
  private val mockEventReportSummaryConnector = mock[EventReportingConnector]
  private val waypoints = EmptyWaypoints

  val erOverviewSeq = Seq(EROverview(
    LocalDate.of(2022, 4, 6),
    LocalDate.of(2023, 4, 5),
    TaxYear("2022"),
    tpssReportPresent = true,
    Some(EROverviewVersion(
      1,
      submittedVersionAvailable = true,
      compiledVersionAvailable = false
    ))
  ))


  private def postRoute: String = routes.EventSummaryController.onSubmit(waypoints).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[EventReportingConnector].toInstance(mockEventReportSummaryConnector)
  )

  override protected def beforeEach(): Unit = {
    reset(mockEventReportSummaryConnector)
  }

  "Event Summary Controller" - {

    "must return OK and the correct view for a GET" in {

      val ua = emptyUserAnswers
        .setOrException(EventSummaryPage, true)
        .setOrException(TaxYearPage, TaxYear("2022"))
        .setOrException(VersionInfoPage, VersionInfo(1, Compiled))
        .setOrException(EventReportingOverviewPage, erOverviewSeq)

      val seqOfEvents = Seq(
        EventSummary(EventType.Event1, 1, None),
        EventSummary(EventType.Event2, 1, None),
        EventSummary(EventType.Event3, 1, None),
        EventSummary(EventType.Event4, 1, None),
        EventSummary(EventType.Event5, 1, None),
        EventSummary(EventType.Event6, 1, None),
        EventSummary(EventType.Event7, 1, None),
        EventSummary(EventType.Event8, 1, Some("TestUser")),
        EventSummary(EventType.Event8A, 1, None),
        EventSummary(EventType.Event10, 1, None),
        EventSummary(EventType.Event11, 1, None),
        EventSummary(EventType.Event12, 1, None),
        EventSummary(EventType.Event13, 1, None),
        EventSummary(EventType.Event18, 1, None),
        EventSummary(EventType.Event20A, 1, None),
        EventSummary(EventType.Event22, 1, None),
        EventSummary(EventType.Event23, 1, None),
        EventSummary(EventType.Event24, 1, None))

      when(mockEventReportSummaryConnector.getEventReportSummary(any(), ArgumentMatchers.eq("2022-04-06"), ArgumentMatchers.eq(1))(any(), any())).thenReturn(
        Future.successful(seqOfEvents)
      )

      val application = applicationBuilder(userAnswers = Some(ua), extraModules).build()

      val view = application.injector.instanceOf[EventSummaryView]

      running(application) {
        val request = FakeRequest(GET, routes.EventSummaryController.onPageLoad().url)
        val schemeName = request.schemeName
        val result = route(application, request).value

        val formProvider = new EventSummaryFormProvider()
        val form = formProvider()

        val events1To6 = Seq(Event1, Event2, Event3, Event4, Event5, Event6)
        val events10To13 = Seq(Event10, Event11, Event12, Event13)
        val events22To24 = Seq(Event22, Event23, Event24)

        val rowsEvents1To6 = getStandardCompiledEventRows(events1To6)

        val event8Row = Seq({
          val eventMessageKey = Message(s"eventSummary.event8")
          SummaryListRow(
            key = Key(
              content = HtmlContent(Text(eventMessageKey).asHtml.toString() + s"""<span style="font-weight:400;"><br/>${Message("eventSummary.lockedBy").resolve} TestUser</span>"""),
              classes = "govuk-!-width-full"
            ),
            actions = Some(Actions(
              items = Seq()
            ))
          )
        })

        val event7Row = getStandardCompiledEventRows(Seq(Event7))

        val event8ARow = getStandardCompiledEventRows(Seq(Event8A))

        val events10to13Rows = getStandardCompiledEventRows(events10To13, isMemberEvent = false)

        val event18Row = Seq({
          val eventMessageKey = Message(s"eventSummary.event18")
          SummaryListRow(
            key = Key(
              content = HtmlContent(Text(eventMessageKey).asHtml.toString() +"""<span style="font-weight:400;"></span>"""),
              classes = "govuk-!-width-full"
            ),
            actions = Some(Actions(
              items = Seq(
                ActionItem(
                  content = Text(Message("site.remove")),
                  href = common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, Event18).url
                )
              )
            ))
          )
        })

        val event20ARow = Seq({
          val eventMessageKey = Message(s"eventSummary.event20A")
          SummaryListRow(
            key = Key(
              content = HtmlContent(Text(eventMessageKey).asHtml.toString() +"""<span style="font-weight:400;"></span>"""),
              classes = "govuk-!-width-full"
            ),
            actions = Some(Actions(
              items = Seq(
                ActionItem(
                  content = Text(Message("site.view")),
                  href = "/manage-pension-scheme-event-report/report/event-20A-check-your-answers?fromViewOnlyLink=true"
                )
              )
            ))
          )
        })

        val rowsEvents22To24 = getStandardCompiledEventRows(events22To24)

        val mappedEvents = rowsEvents1To6 ++ event7Row ++ event8Row ++ event8ARow ++ events10to13Rows ++ event18Row ++ event20ARow ++ rowsEvents22To24

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(form, waypoints, mappedEvents, "2023", schemeName, Some(1), false)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET if has been submitted" in {

      val ua = emptyUserAnswers
        .setOrException(EventSummaryPage, true)
        .setOrException(TaxYearPage, TaxYear("2022"))
        .setOrException(VersionInfoPage, VersionInfo(1, Submitted))
        .setOrException(EventReportingOverviewPage, erOverviewSeq)

      val seqOfEvents = Seq(
        EventSummary(EventType.Event1, 1, None),
        EventSummary(EventType.Event2, 1, None),
        EventSummary(EventType.Event3, 1, None),
        EventSummary(EventType.Event4, 1, None),
        EventSummary(EventType.Event5, 1, None),
        EventSummary(EventType.Event6, 1, None),
        EventSummary(EventType.Event10, 1, None),
        EventSummary(EventType.Event11, 1, None),
        EventSummary(EventType.Event12, 1, None),
        EventSummary(EventType.Event13, 1, None),
        EventSummary(EventType.Event20A, 1, None)
      )

      when(mockEventReportSummaryConnector.getEventReportSummary(any(), ArgumentMatchers.eq("2022-04-06"), ArgumentMatchers.eq(1))(any(), any())).thenReturn(
        Future.successful(seqOfEvents)
      )

      val application = applicationBuilder(userAnswers = Some(ua), extraModules).build()

      val view = application.injector.instanceOf[EventSummaryView]

      running(application) {
        val request = FakeRequest(GET, routes.EventSummaryController.onPageLoad().url)
        val schemeName = request.schemeName
        val result = route(application, request).value

        val formProvider = new EventSummaryFormProvider()
        val form = formProvider()

        val events1To6 = Seq(Event1, Event2, Event3, Event4, Event5, Event6)
        val events10To13 = Seq(Event10, Event11, Event12, Event13)

        val rowsEvents1To6 = getStandardSubmittedEventRows(events1To6)

        val events10to13Rows = getStandardSubmittedEventRows(events10To13, isMemberEvent = false)

        val event20ARow = Seq({
          val eventMessageKey = Message(s"eventSummary.event20A")
          SummaryListRow(
            key = Key(
              content = HtmlContent(Text(eventMessageKey).asHtml.toString() +"""<span style="font-weight:400;"></span>"""),
              classes = "govuk-!-width-full"
            ),
            actions = Some(Actions(
              items = Seq(
                ActionItem(
                  content = Text(Message("site.view")),
                  href = "/manage-pension-scheme-event-report/report/event-20A-check-your-answers?fromViewOnlyLink=true"
                )
              )
            ))
          )
        })

        val mappedEvents = rowsEvents1To6 ++ events10to13Rows ++ event20ARow

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(form, waypoints, mappedEvents, "2023", schemeName, Some(1), true)(request, messages(application)).toString
      }
    }

    "must redirect to next page on submit (when selecting YES)" in {
      val ua = emptyUserAnswersWithTaxYear
      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val userAnswerUpdated = UserAnswers().setOrException(EventSummaryPage, true)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual EventSummaryPage.navigate(waypoints, userAnswerUpdated, userAnswerUpdated).url
      }
    }

    "must redirect to next page on submit (when selecting NO)" in {
      val ua = emptyUserAnswersWithTaxYear
      val application = applicationBuilder(userAnswers = Some(ua)).build()
      running(application) {
        val request = FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value
        val userAnswerUpdated = UserAnswers().setOrException(EventSummaryPage, false)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual EventSummaryPage.navigate(waypoints, userAnswerUpdated, userAnswerUpdated).url
      }
    }
  }

  private def getStandardCompiledEventRows(events: Seq[EventType], isMemberEvent: Boolean = true) = {
    events.map { event =>
      val eventMessageKey = Message(s"eventSummary.event${event.toString}")
      val changeLink = if (isMemberEvent) {
        s"/manage-pension-scheme-event-report/report/event-${event.toString}-summary"
      } else {
        s"/manage-pension-scheme-event-report/report/event-${event.toString}-check-your-answers"
      }
      SummaryListRow(
        key = Key(
          content = HtmlContent(Text(eventMessageKey).asHtml.toString() +"""<span style="font-weight:400;"></span>"""),
          classes = "govuk-!-width-full"
        ),
        actions = Some(Actions(
          items = Seq(
            ActionItem(
              content = Text(Message("site.change")),
              href = changeLink
            ),
            ActionItem(
              content = Text(Message("site.remove")),
              href = common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, event).url
            )
          )
        ))
      )
    }
  }

  private def getStandardSubmittedEventRows(events: Seq[EventType], isMemberEvent: Boolean = true) = {
    events.map { event =>
      val eventMessageKey = Message(s"eventSummary.event${event.toString}")
      val changeLink = if (isMemberEvent) {
        s"/manage-pension-scheme-event-report/report/event-${event.toString}-summary"
      } else {
        s"/manage-pension-scheme-event-report/report/event-${event.toString}-check-your-answers"
      }
      SummaryListRow(
        key = Key(
          content = HtmlContent(Text(eventMessageKey).asHtml.toString() +"""<span style="font-weight:400;"></span>"""),
          classes = "govuk-!-width-full"
        ),
        actions = Some(Actions(
          items = Seq(
            ActionItem(
              content = Text(Message("site.changeOrView")),
              href = changeLink
            )
          )
        ))
      )
    }
  }


}

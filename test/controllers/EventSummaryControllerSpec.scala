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
import models.enumeration.EventType.{Event1, Event18}
import models.enumeration.VersionStatus.Compiled
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

      val seqOfEvents = Seq(EventSummary(EventType.Event1, 1, None), EventSummary(EventType.Event8, 1, Some("TestUser")), EventSummary(EventType.Event18, 1, None))

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

        val mappedEvents = Seq({
          val eventMessageKey = Message(s"eventSummary.event1")
          SummaryListRow(
            key = Key(
              content = HtmlContent(Text(eventMessageKey).asHtml.toString() +"""<span style="font-weight:400;"></span>"""),
              classes = "govuk-!-width-full"
            ),
            actions = Some(Actions(
              items = Seq(
                ActionItem(
                  content = Text(Message("site.change")),
                  href = event1.routes.UnauthPaymentSummaryController.onPageLoad(EmptyWaypoints).url
                ),
                ActionItem(
                  content = Text(Message("site.remove")),
                  href = common.routes.RemoveEventController.onPageLoad(EmptyWaypoints, Event1).url
                )
              )
            ))
          )
        }, {
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
        }, {
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

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(form, waypoints, mappedEvents, "2023", schemeName, Some(1), false)(request, messages(application)).toString
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
}

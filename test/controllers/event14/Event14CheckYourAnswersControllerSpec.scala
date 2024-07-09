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

package controllers.event14

import base.SpecBase
import models.enumeration.EventType.Event14
import models.enumeration.VersionStatus.{Compiled, Submitted}
import models.event14.HowManySchemeMembers
import models.{EROverview, EROverviewVersion, TaxYear, VersionInfo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.event14.HowManySchemeMembersPage
import pages.{EmptyWaypoints, EventReportingOverviewPage, VersionInfoPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.CompileService
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.Future

class Event14CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  private val mockCompileService = mock[CompileService]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[CompileService].toInstance(mockCompileService)
  )

  "Check Your Answers Controller" - {

    val erOverviewSeq = Seq(EROverview(
      LocalDate.of(2022, 4, 6),
      LocalDate.of(2023, 4, 5),
      TaxYear("2022"),
      tpssReportPresent = true,
      Some(EROverviewVersion(
        3,
        submittedVersionAvailable = true,
        compiledVersionAvailable = false
      ))
    ),
      EROverview(
        LocalDate.of(2023, 4, 6),
        LocalDate.of(2024, 4, 5),
        TaxYear("2023"),
        tpssReportPresent = true,
        Some(EROverviewVersion(
          2,
          submittedVersionAvailable = true,
          compiledVersionAvailable = false
        ))
      ))

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear
        .setOrException(VersionInfoPage, VersionInfo(3, Submitted))
        .setOrException(EventReportingOverviewPage, erOverviewSeq))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event14.routes.Event14CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view(list,
          "/manage-pension-scheme-event-report/report/event-14-click")(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET (View Only (different heading))" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear
        .setOrException(VersionInfoPage, VersionInfo(1, Submitted))
        .setOrException(EventReportingOverviewPage, erOverviewSeq))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event14.routes.Event14CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view(list,
          "/manage-pension-scheme-event-report/report/event-14-click", Tuple2(Some(1), Some(Event14)))(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event14.routes.Event14CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
    "must redirect to the correct page onClick when all answers are present" in {
      when(mockCompileService.compileEvent(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful())

      val event14Answers = emptyUserAnswers.set(HowManySchemeMembersPage, HowManySchemeMembers.OptionOne).get

      val userAnswersWithVersionInfo = event14Answers.setOrException(VersionInfoPage, VersionInfo(1, Compiled))
      val application = applicationBuilder(userAnswers = Some(userAnswersWithVersionInfo), extraModules).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event14.routes.Event14CheckYourAnswersController.onClick.url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url
        verify(mockCompileService, times(1)).compileEvent(any(), any(), any(), any())(any())
      }
    }

    "must redirect to the correct page onClick when an answer is missing" in {
      when(mockCompileService.compileEvent(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful())

      val userAnswersWithVersionInfo = emptyUserAnswers.setOrException(VersionInfoPage, VersionInfo(1, Compiled))
      val application = applicationBuilder(userAnswers = Some(userAnswersWithVersionInfo), extraModules).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event14.routes.Event14CheckYourAnswersController.onClick.url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual s"${
          controllers.event14.routes.HowManySchemeMembersController.onPageLoad(EmptyWaypoints).url
        }?waypoints=event-14-check-answers"
      }
    }
  }
}

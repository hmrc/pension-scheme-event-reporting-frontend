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

package controllers.event24

import base.SpecBase
import models.common.MembersDetails
import models.enumeration.EventType.Event24
import models.enumeration.VersionStatus.{Compiled, Submitted}
import models.event24.{BCETypeSelection, CrystallisedDate, TypeOfProtectionSelection}
import models.{EROverview, EROverviewVersion, MemberSummaryPath, TaxYear, VersionInfo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.common.MembersDetailsPage
import pages.event24._
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

class Event24CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with BeforeAndAfterEach {

  private val mockCompileService = mock[CompileService]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[CompileService].toInstance(mockCompileService)
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCompileService)
  }

  "Check Your Answers Controller for Event 24" - {

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

      val application = applicationBuilder(userAnswers =
        Some(emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(3, Submitted))
          .setOrException(EventReportingOverviewPage, erOverviewSeq))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event24.routes.Event24CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view.render(
          list,
          continueUrl = "/manage-pension-scheme-event-report/report/1/event-24-click",
          Tuple2(None, None),
          request,
          messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event24.routes.Event24CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the correct page onClick if all answers are present" in {
      when(mockCompileService.compileEvent(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful())

      val event24Answers = emptyUserAnswers
        .set(MembersDetailsPage(Event24, 0), MembersDetails("Jane", "Doe", "AB123456A")).get
        .set(CrystallisedDatePage(0), CrystallisedDate(LocalDate.of(2024, 4, 4))).get
        .set(BCETypeSelectionPage(0), BCETypeSelection.Small).get
        .set(TotalAmountBenefitCrystallisationPage(0), BigDecimal(123)).get
        .set(ValidProtectionPage(0), true).get
        .set(TypeOfProtectionPage(0), TypeOfProtectionSelection.FixedProtection).get
        .set(TypeOfProtectionReferencePage(0), "abcdef123").get
        .set(OverAllowancePage(0), false).get
        .set(OverAllowanceAndDeathBenefitPage(0), true).get
        .set(MarginalRatePage(0), true).get
        .set(EmployerPayeReferencePage(0), "123/ABCDE").get

      val userAnswersWithVersionInfo = event24Answers.setOrException(VersionInfoPage, VersionInfo(1, Compiled))
      val application = applicationBuilder(userAnswers = Some(userAnswersWithVersionInfo), extraModules).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event24.routes.Event24CheckYourAnswersController.onClick(0).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event24)).url
        verify(mockCompileService, times(1)).compileEvent(any(), any(), any(), any())(any())
      }
    }
    "must redirect to the correct page onClick if an answers is missing" in {
      when(mockCompileService.compileEvent(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful())

      val event24Answers = emptyUserAnswers
        .set(MembersDetailsPage(Event24, 0), MembersDetails("Jane", "Doe", "AB123456A")).get
        .set(BCETypeSelectionPage(0), BCETypeSelection.Small).get
        .set(TotalAmountBenefitCrystallisationPage(0), BigDecimal(123)).get
        .set(ValidProtectionPage(0), true).get
        .set(TypeOfProtectionPage(0), TypeOfProtectionSelection.FixedProtection).get
        .set(TypeOfProtectionReferencePage(0), "abcdef123").get
        .set(OverAllowancePage(0), false).get
        .set(OverAllowanceAndDeathBenefitPage(0), true).get
        .set(MarginalRatePage(0), true).get
        .set(EmployerPayeReferencePage(0), "123/ABCDE").get

      val userAnswersWithVersionInfo = event24Answers.setOrException(VersionInfoPage, VersionInfo(1, Compiled))
      val application = applicationBuilder(userAnswers = Some(userAnswersWithVersionInfo), extraModules).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event24.routes.Event24CheckYourAnswersController.onClick(0).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual s"${
          controllers.event24.routes.CrystallisedDateController.onPageLoad(EmptyWaypoints, 0).url
        }?waypoints=event-24-check-answers-1"
      }
    }
  }
}
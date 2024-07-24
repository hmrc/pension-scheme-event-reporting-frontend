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

package controllers.event11

import base.SpecBase
import data.SampleData.{sampleJourneyData11SchemeChangedBothRules, sampleJourneyData11SchemeChangedRulesForAssetsOnly, sampleJourneyData11SchemeNotChangedInAssets}
import models.enumeration.EventType.Event11
import models.enumeration.VersionStatus.{Compiled, Submitted}
import models.event11.Event11Date
import models.{EROverview, EROverviewVersion, TaxYear, VersionInfo}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.event11.{HasSchemeChangedRulesInvestmentsInAssetsPage, HasSchemeChangedRulesPage, InvestmentsInAssetsRuleChangeDatePage}
import pages.{EmptyWaypoints, EventReportingOverviewPage, TaxYearPage, VersionInfoPage}
import play.api.i18n.Messages
import play.api.inject
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.CompileService
import uk.gov.hmrc.govukfrontend.views.Aliases
import uk.gov.hmrc.govukfrontend.views.Aliases._
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.Future

class Event11CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with BeforeAndAfterEach {

  private val mockCompileService = mock[CompileService]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[CompileService].toInstance(mockCompileService)
  )

  import Event11CheckYourAnswersControllerSpec._

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCompileService)
  }

  "Check Your Answers Controller for Event 11" - {

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
        val request = FakeRequest(GET, controllers.event11.routes.Event11CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view.render(
          list,
          continueUrl = "/manage-pension-scheme-event-report/report/event-11-click", Tuple2(None, None),
          request,
          messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET (View Only (different heading))" in {

      val application = applicationBuilder(userAnswers =
        Some(emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(1, Submitted))
        .setOrException(EventReportingOverviewPage, erOverviewSeq))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event11.routes.Event11CheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view(
          list, "/manage-pension-scheme-event-report/report/event-11-click", Tuple2(Some(1), Some(Event11)))(request, messages(application)).toString
      }
    }

    "must return OK and the correct summary list row items for a GET (Scheme has changed rules and changed rules in assets) (change links present)" in {
      val mockView = mock[CheckYourAnswersView]
      val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
        inject.bind[CheckYourAnswersView].toInstance(mockView)
      )

      val application = applicationBuilder(
        userAnswers = Some(sampleJourneyData11SchemeChangedBothRules
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(3, Submitted))),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any(), any())(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event11.routes.Event11CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRowsEvent11BothRuleChange

        actual.size mustBe expected.size

        actual.zipWithIndex.map { case (a, i) =>
          a mustBe expected(i)
        }
      }
    }

    "must return OK and the correct summary list row items for a GET (Scheme has not changed rules in assets) (change links present)" in {
      val mockView = mock[CheckYourAnswersView]
      val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
        inject.bind[CheckYourAnswersView].toInstance(mockView)
      )

      val application = applicationBuilder(
        userAnswers = Some(sampleJourneyData11SchemeNotChangedInAssets
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(3, Submitted))),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any(), any())(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event11.routes.Event11CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRowsEvent11NoChangeForAssets

        actual.size mustBe expected.size

        actual.zipWithIndex.map { case (a, i) =>
          a mustBe expected(i)
        }
      }
    }


    "must return OK and the correct summary list row items for a GET (Changed rules in assets only) (change links present)" in {
      val mockView = mock[CheckYourAnswersView]
      val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
        inject.bind[CheckYourAnswersView].toInstance(mockView)
      )

      val application = applicationBuilder(
        userAnswers = Some(sampleJourneyData11SchemeChangedRulesForAssetsOnly
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(3, Submitted))),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any(), any())(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event11.routes.Event11CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRowsEvent11ChangedRulesForAssetsOnly

        actual.size mustBe expected.size

        actual.zipWithIndex.map { case (a, i) =>
          a mustBe expected(i)
        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event11.routes.Event11CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the correct page onClick when all answers are present" in {
      when(mockCompileService.compileEvent(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful())

      val userAnswersWithVersionInfo = sampleJourneyData11SchemeChangedBothRules.setOrException(VersionInfoPage, VersionInfo(1, Compiled))
      val application = applicationBuilder(userAnswers = Some(userAnswersWithVersionInfo), extraModules).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event11.routes.Event11CheckYourAnswersController.onClick.url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url
        verify(mockCompileService, times(1)).compileEvent(any(), any(), any(), any())(any())
      }
    }

    "must redirect to the correct page onClick when an answer is missing" in {
      when(mockCompileService.compileEvent(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful())

      val event11Answers = emptyUserAnswers.set(HasSchemeChangedRulesPage, true).get
        .set(HasSchemeChangedRulesInvestmentsInAssetsPage, true).get
        .set(InvestmentsInAssetsRuleChangeDatePage, Event11Date(LocalDate.of(2024, 4, 4))).get

      val userAnswersWithVersionInfo = event11Answers.setOrException(VersionInfoPage, VersionInfo(1, Compiled))
      val application = applicationBuilder(userAnswers = Some(userAnswersWithVersionInfo), extraModules).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event11.routes.Event11CheckYourAnswersController.onClick.url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual s"${
          controllers.event11.routes.UnAuthPaymentsRuleChangeDateController.onPageLoad(EmptyWaypoints).url
        }?waypoints=event-11-check-answers"
      }
    }
  }
}

object Event11CheckYourAnswersControllerSpec {
  private def fakeSummaryListRowWithHtmlContentWithHiddenContent(messageKey: String, htmlContent: String, changeLink: String, hiddenContentChangeLink: String)
                                                                (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKey)
        ), ""),
      Value(HtmlContent(htmlContent), ""), "",
      Some(Actions("", List(ActionItem(changeLink, Text("Change"), Some(messages(hiddenContentChangeLink)), "", Map()))))
    )

  private def fakeSummaryListRowWithTextWithHiddenContent(messageKey: String, text: String, changeLink: String, hiddenContentChangeLink: String)
                                                         (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKey)
        ), ""),
      Value(Text(text), ""), "",
      Some(Actions("", List(ActionItem(changeLink, Text("Change"), Some(messages(hiddenContentChangeLink)), "", Map()))))
    )

  private def expectedMemberSummaryListRowsEvent11BothRuleChange(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "hasSchemeChangedRules.event11.checkYourAnswersLabel",
      "Yes",
      "/manage-pension-scheme-event-report/report/event-11-rule-change-to-allow-unauthorised-payments?waypoints=event-11-check-answers",
      "hasSchemeChangedRules.change.hidden"
    ),
    fakeSummaryListRowWithTextWithHiddenContent(
      "unAuthPaymentsRuleChangeDate.checkYourAnswersLabel",
      "04 April 2024",
      "/manage-pension-scheme-event-report/report/event-11-when-unauthorised-payments-rule-change-took-effect?waypoints=event-11-check-answers",
      "unAuthPaymentsRuleChangeDate.change.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "hasSchemeChangedRulesInvestmentsInAssets.checkYourAnswersLabel",
      "Yes",
      "/manage-pension-scheme-event-report/report/event-11-rule-change-to-allow-investments-in-assets?waypoints=event-11-check-answers",
      "hasSchemeChangedRulesInvestmentsInAssets.change.hidden"
    ),
      fakeSummaryListRowWithTextWithHiddenContent(
      "investmentsInAssetsRuleChangeDate.checkYourAnswersLabel",
        "04 April 2024",
      "/manage-pension-scheme-event-report/report/event-11-when-investment-in-assets-rule-change-took-effect?waypoints=event-11-check-answers",
      "investmentsInAssetsRuleChangeDate.change.hidden"
    )
  )

  private def expectedMemberSummaryListRowsEvent11NoChangeForAssets(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "hasSchemeChangedRules.event11.checkYourAnswersLabel",
      "Yes",
      "/manage-pension-scheme-event-report/report/event-11-rule-change-to-allow-unauthorised-payments?waypoints=event-11-check-answers",
      "hasSchemeChangedRules.change.hidden"
    ),
    fakeSummaryListRowWithTextWithHiddenContent(
      "unAuthPaymentsRuleChangeDate.checkYourAnswersLabel",
      "04 April 2024",
      "/manage-pension-scheme-event-report/report/event-11-when-unauthorised-payments-rule-change-took-effect?waypoints=event-11-check-answers",
      "unAuthPaymentsRuleChangeDate.change.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "hasSchemeChangedRulesInvestmentsInAssets.checkYourAnswersLabel",
      "No",
      "/manage-pension-scheme-event-report/report/event-11-rule-change-to-allow-investments-in-assets?waypoints=event-11-check-answers",
      "hasSchemeChangedRulesInvestmentsInAssets.change.hidden"
    )
  )

  private def expectedMemberSummaryListRowsEvent11ChangedRulesForAssetsOnly(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "hasSchemeChangedRules.event11.checkYourAnswersLabel",
      "No",
      "/manage-pension-scheme-event-report/report/event-11-rule-change-to-allow-unauthorised-payments?waypoints=event-11-check-answers",
      "hasSchemeChangedRules.change.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "hasSchemeChangedRulesInvestmentsInAssets.checkYourAnswersLabel",
      "Yes",
      "/manage-pension-scheme-event-report/report/event-11-rule-change-to-allow-investments-in-assets?waypoints=event-11-check-answers",
      "hasSchemeChangedRulesInvestmentsInAssets.change.hidden"
    ),
    fakeSummaryListRowWithTextWithHiddenContent(
      "investmentsInAssetsRuleChangeDate.checkYourAnswersLabel",
      "04 April 2024",
      "/manage-pension-scheme-event-report/report/event-11-when-investment-in-assets-rule-change-took-effect?waypoints=event-11-check-answers",
      "investmentsInAssetsRuleChangeDate.change.hidden"
    )
  )
}
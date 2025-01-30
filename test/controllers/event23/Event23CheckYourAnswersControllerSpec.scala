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

package controllers.event23

import base.SpecBase
import data.SampleData.{erOverviewSeq, sampleMemberJourneyDataEvent22and23, sampleMemberJourneyDataEvent22and23WithMissingAmount}
import models.enumeration.EventType.Event23
import models.enumeration.VersionStatus.Submitted
import models.{MemberSummaryPath, TaxYear, VersionInfo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
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

import scala.concurrent.Future

class Event23CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with BeforeAndAfterEach {

  private val mockCompileService = mock[CompileService]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[CompileService].toInstance(mockCompileService)
  )

  import Event23CheckYourAnswersControllerSpec._

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCompileService)
  }

  "Check Your Answers Controller for Event 23" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear
        .setOrException(VersionInfoPage, VersionInfo(3, Submitted))
        .setOrException(EventReportingOverviewPage, erOverviewSeq))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event23.routes.Event23CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view.render(list,
          continueUrl = "/manage-pension-scheme-event-report/report/1/event-23-click",
          Tuple2(None, None),
          request = request,
          messages = messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET (View Only (different heading))" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear
        .setOrException(VersionInfoPage, VersionInfo(1, Submitted))
        .setOrException(EventReportingOverviewPage, erOverviewSeq))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event23.routes.Event23CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view.render(
          list,
          continueUrl = "/manage-pension-scheme-event-report/report/1/event-23-click",
          Tuple2(Some(1), Some(Event23)),
          request,
          messages(application)).toString
      }
    }

    "must return OK and the correct summary list row items for a GET (member)" in {
      val mockView = mock[CheckYourAnswersView]
      val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
        inject.bind[CheckYourAnswersView].toInstance(mockView)
      )

      val application = applicationBuilder(
        userAnswers = Some(sampleMemberJourneyDataEvent22and23(Event23)
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(3, Submitted))),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any(), any())(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event23.routes.Event23CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRowsEvent23

        actual.size mustBe expected.size

        actual.zipWithIndex.map { case (a, i) =>
          a mustBe expected(i)
        }
      }
    }

    "must return OK and the correct summary list row items for a GET (member) (NO change links present)" in {
      val mockView = mock[CheckYourAnswersView]
      val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
        inject.bind[CheckYourAnswersView].toInstance(mockView)
      )

      val application = applicationBuilder(
        userAnswers = Some(sampleMemberJourneyDataEvent22and23(Event23)
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(1, Submitted))),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any(), ArgumentMatchers.eq((Some(1), Some(Event23))))(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event23.routes.Event23CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRowsEvent23ViewOnly

        actual.size mustBe expected.size

        actual.zipWithIndex.map { case (a, i) =>
          a mustBe expected(i)
        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event23.routes.Event23CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the correct page onClick if all answers are present" in {
      when(mockCompileService.compileEvent(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful())

      val application = applicationBuilder(
        userAnswers = Some(sampleMemberJourneyDataEvent22and23(Event23)
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(1, Submitted))),
        extraModules = extraModules
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event23.routes.Event23CheckYourAnswersController.onClick(0).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event23)).url
        verify(mockCompileService, times(1)).compileEvent(any(), any(), any(), any())(any(), any())
      }
    }

    "must redirect to the correct page onClick if an answer is missing" in {
      when(mockCompileService.compileEvent(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful())

      val application = applicationBuilder(
        userAnswers = Some(sampleMemberJourneyDataEvent22and23WithMissingAmount(Event23)
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(1, Submitted))),
        extraModules = extraModules
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event23.routes.Event23CheckYourAnswersController.onClick(0).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual s"${
          controllers.common.routes.TotalPensionAmountsController.onPageLoad(EmptyWaypoints, Event23, 0).url
        }?waypoints=event-23-check-answers-1"
      }
    }
  }
}

object Event23CheckYourAnswersControllerSpec {

  private def fakeSummaryListRowWithHtmlContentWithHiddenContentWithoutChange(messageKey: String, htmlContent: String,
                                                                              changeLink: String, hiddenContentChangeLink: String)
                                                                             (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKey)
        ), ""),
      Value(HtmlContent(htmlContent), ""), "",
      Some(Actions("", List(ActionItem(changeLink, Text("Change"), Some(messages(hiddenContentChangeLink)), "", Map()))))
    )

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

  private def fakeSummaryListRowWithHtmlContentWithHiddenContentWithoutChangeViewOnly(messageKey: String, htmlContent: String)
                                                                                     (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKey)
        ), ""),
      Value(HtmlContent(htmlContent), ""), "")

  private def fakeSummaryListRowWithHtmlContentWithHiddenContentViewOnly(messageKey: String, htmlContent: String)
                                                                        (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKey)
        ), ""),
      Value(HtmlContent(htmlContent), ""), "")

  private def expectedMemberSummaryListRowsEvent23(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "membersDetails.checkYourAnswersLabel",
      "Joe Bloggs",
      "/manage-pension-scheme-event-report/report/1/event-23-member-details?waypoints=event-23-check-answers-1",
      "membersDetails.change.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "membersDetails.checkYourAnswersLabel.nino",
      "AA234567D",
      "/manage-pension-scheme-event-report/report/1/event-23-member-details?waypoints=event-23-check-answers-1",
      "membersDetails.change.nino.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithoutChange(
      "chooseTaxYear.event23.checkYourAnswersLabel",
      "2015 to 2016",
      "/manage-pension-scheme-event-report/report/1/event-23-tax-year?waypoints=event-23-check-answers-1",
      "chooseTaxYear.event23.change.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithoutChange(
      "totalPensionAmounts.event23.checkYourAnswersLabel",
      "£10.00",
      "/manage-pension-scheme-event-report/report/1/event-23-total-input-amount?waypoints=event-23-check-answers-1",
      "totalPensionAmounts.event23.change.hidden"
    )
  )

  private def expectedMemberSummaryListRowsEvent23ViewOnly(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentWithHiddenContentViewOnly(
      "membersDetails.checkYourAnswersLabel",
      "Joe Bloggs"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentViewOnly(
      "membersDetails.checkYourAnswersLabel.nino",
      "AA234567D"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithoutChangeViewOnly(
      "chooseTaxYear.event23.checkYourAnswersLabel",
      "2015 to 2016"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithoutChangeViewOnly(
      "totalPensionAmounts.event23.checkYourAnswersLabel",
      "£10.00"
    )
  )
}
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

package controllers.event22

import base.SpecBase
import connectors.UserAnswersCacheConnector
import data.SampleData.{erOverviewSeq, sampleMemberJourneyDataEvent22and23, sampleMemberJourneyDataEvent22and23WithMissingAmount}
import models.enumeration.EventType.Event22
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

class Event22CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with BeforeAndAfterEach {

  private val mockCompileService = mock[CompileService]

  import Event22CheckYourAnswersControllerSpec._

  private val mockUserCacheConnector = mock[UserAnswersCacheConnector]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[CompileService].toInstance(mockCompileService),
    bind[UserAnswersCacheConnector].toInstance(mockUserCacheConnector)
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(mockUserCacheConnector.save(any(),any(), any())(any(),any(), any())).thenReturn(Future.successful(()))
    reset(mockCompileService)
  }

  "Check Your Answers Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear
        .setOrException(VersionInfoPage, VersionInfo(3, Submitted))
        .setOrException(EventReportingOverviewPage, erOverviewSeq))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event22.routes.Event22CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view.render(list,
          continueUrl = "/manage-pension-scheme-event-report/report/1/event-22-click",
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
        val request = FakeRequest(GET, controllers.event22.routes.Event22CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view.render(
          list,
          continueUrl = "/manage-pension-scheme-event-report/report/1/event-22-click",
          Tuple2(Some(1), Some(Event22)),
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
        userAnswers = Some(sampleMemberJourneyDataEvent22and23(Event22)
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(3, Submitted))),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any(), any())(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event22.routes.Event22CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRowsEvent22

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
        userAnswers = Some(sampleMemberJourneyDataEvent22and23(Event22)
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(1, Submitted))),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any(), ArgumentMatchers.eq((Some(1), Some(Event22))))(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event22.routes.Event22CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRowsEvent22ViewOnly

        actual.size mustBe expected.size

        actual.zipWithIndex.map { case (a, i) =>
          a mustBe expected(i)
        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event22.routes.Event22CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the correct page onClick if all answers are present" in {
      when(mockCompileService.compileEvent(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application = applicationBuilder(
        userAnswers = Some(sampleMemberJourneyDataEvent22and23(Event22)
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(1, Submitted))),
        extraModules = extraModules
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event22.routes.Event22CheckYourAnswersController.onClick(0).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event22)).url
        verify(mockCompileService, times(1)).compileEvent(any(), any(), any(), any())(any(), any())
      }
    }

    "must redirect to the correct page onClick if an answer is missing" in {
      when(mockCompileService.compileEvent(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application = applicationBuilder(
        userAnswers = Some(sampleMemberJourneyDataEvent22and23WithMissingAmount(Event22)
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(1, Submitted))),
        extraModules = extraModules
      ).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event22.routes.Event22CheckYourAnswersController.onClick(0).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual s"${
          controllers.common.routes.TotalPensionAmountsController.onPageLoad(EmptyWaypoints, Event22, 0).url
        }?waypoints=event-22-check-answers-1"
      }
    }
  }
}

object Event22CheckYourAnswersControllerSpec {

  private def fakeSummaryListRowWithHtmlContentWithHiddenContentWithChange(messageKey: String, htmlContent: String, changeLink: String,
                                                                           hiddenContentChangeLink: String)
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

  private def fakeSummaryListRowWithHtmlContentWithHiddenContentWithChangeViewOnly(messageKey: String, htmlContent: String)
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

  private val memberDetailsContent = s"""<p class="govuk-body">Joe Bloggs</p>
                                        |<p class="govuk-body">AA234567D</p>""".stripMargin

  private def expectedMemberSummaryListRowsEvent22(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithChange(
      "membersDetails.checkYourAnswersLabel",
      memberDetailsContent,
      "/manage-pension-scheme-event-report/report/1/event-22-member-details?waypoints=event-22-check-answers-1",
      "membersDetails.change.hidden"

    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "chooseTaxYear.event22.checkYourAnswersLabel",
      "2015 to 2016",
      "/manage-pension-scheme-event-report/report/1/event-22-tax-year?waypoints=event-22-check-answers-1",
      "chooseTaxYear.event22.change.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "totalPensionAmounts.event22.checkYourAnswersLabel",
      "£10.00",
      "/manage-pension-scheme-event-report/report/1/event-22-total-input-amount?waypoints=event-22-check-answers-1",
      "totalPensionAmounts.event22.change.hidden"
    )
  )

  private def expectedMemberSummaryListRowsEvent22ViewOnly(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithChangeViewOnly(
      "membersDetails.checkYourAnswersLabel",
      memberDetailsContent
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentViewOnly(
      "chooseTaxYear.event22.checkYourAnswersLabel",
      "2015 to 2016"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentViewOnly(
      "totalPensionAmounts.event22.checkYourAnswersLabel",
      "£10.00"
    )
  )
}
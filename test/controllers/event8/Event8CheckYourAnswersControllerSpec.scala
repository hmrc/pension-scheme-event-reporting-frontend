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

package controllers.event8

import base.SpecBase
import data.SampleData.{erOverviewSeq, sampleMemberJourneyDataEvent8}
import models.common.MembersDetails
import models.enumeration.EventType.Event8
import models.enumeration.VersionStatus.{Compiled, Submitted}
import models.event8.{LumpSumDetails, TypeOfProtection}
import models.{MemberSummaryPath, TaxYear, VersionInfo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.common.MembersDetailsPage
import pages.event8.{LumpSumAmountAndDatePage, TypeOfProtectionPage, TypeOfProtectionReferencePage}
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

class Event8CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with BeforeAndAfterEach {

  private val mockCompileService = mock[CompileService]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[CompileService].toInstance(mockCompileService)
  )

  import Event8CheckYourAnswersControllerSpec._

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCompileService)
  }

  "Check Your Answers Controller for Event 8" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers =
        Some(emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(3, Submitted))
          .setOrException(EventReportingOverviewPage, erOverviewSeq))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event8.routes.Event8CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view.render(list,
          continueUrl = "/manage-pension-scheme-event-report/report/1/event-8-click",
          Tuple2(None, None),
          request,
          messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET (View Only (different heading))" in {
      val application = applicationBuilder(userAnswers =
        Some(emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(1, Submitted))
          .setOrException(EventReportingOverviewPage, erOverviewSeq))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event8.routes.Event8CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view.render(
          list,
          continueUrl = "/manage-pension-scheme-event-report/report/1/event-8-click",
          Tuple2(Some(1), Some(Event8)),
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
        userAnswers = Some(sampleMemberJourneyDataEvent8.setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(3, Submitted))),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any(), any())(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event8.routes.Event8CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRowsEvent8

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
        userAnswers = Some(sampleMemberJourneyDataEvent8
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(1, Submitted))),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any(), ArgumentMatchers.eq(Some(1), Some(Event8)))(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event8.routes.Event8CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRowsEvent8ViewOnly

        actual.size mustBe expected.size

        actual.zipWithIndex.map { case (a, i) =>
          a mustBe expected(i)
        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event8.routes.Event8CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the correct page onClick if all answers are present" in {
      when(mockCompileService.compileEvent(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful())

      val event8Answers = emptyUserAnswers.set(MembersDetailsPage(Event8, 0), MembersDetails("Jane", "Doe", "AB123456B")).get
        .set(TypeOfProtectionPage(Event8, 0), TypeOfProtection.PrimaryProtection).get
        .set(TypeOfProtectionReferencePage(Event8, 0), "abcdefg123").get
        .set(LumpSumAmountAndDatePage(Event8, 0), LumpSumDetails(BigDecimal(123), LocalDate.of(2024, 4, 4))).get

      val userAnswersWithVersionInfo = event8Answers.setOrException(VersionInfoPage, VersionInfo(1, Compiled))
      val application = applicationBuilder(userAnswers = Some(userAnswersWithVersionInfo), extraModules).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event8.routes.Event8CheckYourAnswersController.onClick(0).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event8)).url
        verify(mockCompileService, times(1)).compileEvent(any(), any(), any(), any())(any())
      }
    }

    "must redirect to the correct page onClick if an answer is missing" in {
      when(mockCompileService.compileEvent(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful())

      val event8Answers = emptyUserAnswers.set(MembersDetailsPage(Event8, 0), MembersDetails("Jane", "Doe", "AB123456B")).get
        .set(TypeOfProtectionReferencePage(Event8, 0), "abcdefg123").get
        .set(LumpSumAmountAndDatePage(Event8, 0), LumpSumDetails(BigDecimal(123), LocalDate.of(2024, 4, 4))).get

      val userAnswersWithVersionInfo = event8Answers.setOrException(VersionInfoPage, VersionInfo(1, Compiled))
      val application = applicationBuilder(userAnswers = Some(userAnswersWithVersionInfo), extraModules).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event8.routes.Event8CheckYourAnswersController.onClick(0).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual s"${
          controllers.event8.routes.TypeOfProtectionController.onPageLoad(EmptyWaypoints, Event8, 0).url
        }?waypoints=event-8-check-answers-1"
      }
    }
  }
}

object Event8CheckYourAnswersControllerSpec {

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

  private def fakeSummaryListRowWithHtmlContentWithHiddenContentWithTwoMsgKeys(messageKeyOne: String, messageKeyTwo: String, htmlContent: String,
                                                                               changeLink: String, hiddenContentChangeLink: String)
                                                                              (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKeyOne, messages(messageKeyTwo).toLowerCase)
        ), ""),
      Value(Text(htmlContent), ""), "",
      Some(Actions("", List(ActionItem(changeLink, Text("Change"),
        Some(messages(hiddenContentChangeLink, messages(messageKeyTwo).toLowerCase)), "", Map()))))
    )

  private def fakeSummaryListRowWithHtmlContentWithHiddenContentViewOnly(messageKey: String, htmlContent: String)
                                                                        (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKey)
        ), ""),
      Value(HtmlContent(htmlContent), ""), "")

  private def fakeSummaryListRowWithTextWithHiddenContentViewOnly(messageKey: String, text: String)
                                                                 (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKey)
        ), ""),
      Value(Text(text), ""), "")

  private def fakeSummaryListRowWithHtmlContentWithHiddenContentWithTwoMsgKeysViewOnly(messageKeyOne: String, messageKeyTwo: String, htmlContent: String)
                                                                                      (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKeyOne, messages(messageKeyTwo).toLowerCase)
        ), ""),
      Value(Text(htmlContent), ""), "")

  private def expectedMemberSummaryListRowsEvent8(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "membersDetails.checkYourAnswersLabel",
      "Joe Bloggs",
      "/manage-pension-scheme-event-report/report/1/event-8-member-details?waypoints=event-8-check-answers-1",
      "membersDetails.change.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "membersDetails.checkYourAnswersLabel.nino",
      "AA234567D",
      "/manage-pension-scheme-event-report/report/1/event-8-member-details?waypoints=event-8-check-answers-1",
      "membersDetails.change.nino.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "event8.typeOfProtection.checkYourAnswersLabel",
      "Primary protection",
      "/manage-pension-scheme-event-report/report/1/event-8-type-of-protection?waypoints=event-8-check-answers-1",
      "event8.typeOfProtection.change.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithTwoMsgKeys(
      "typeOfProtectionReference.checkYourAnswersLabel",
      "event8.typeOfProtection.primaryProtection",
      "1234567A",
      "/manage-pension-scheme-event-report/report/1/event-8-protection-reference?waypoints=event-8-check-answers-1",
      "typeOfProtectionReference.change.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "lumpSumAmountAndDate.value.checkYourAnswersLabel",
      "£10.00",
      "/manage-pension-scheme-event-report/report/1/event-8-lump-sum-details?waypoints=event-8-check-answers-1",
      "lumpSumAmountAndDate.value.change.hidden"
    ),
    fakeSummaryListRowWithTextWithHiddenContent(
      "lumpSumAmountAndDate.date.checkYourAnswersLabel",
      "22 March 2022",
      "/manage-pension-scheme-event-report/report/1/event-8-lump-sum-details?waypoints=event-8-check-answers-1",
      "lumpSumAmountAndDate.date.change.hidden"
    )
  )

  private def expectedMemberSummaryListRowsEvent8ViewOnly(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentWithHiddenContentViewOnly(
      "membersDetails.checkYourAnswersLabel",
      "Joe Bloggs"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentViewOnly(
      "membersDetails.checkYourAnswersLabel.nino",
      "AA234567D"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentViewOnly(
      "event8.typeOfProtection.checkYourAnswersLabel",
      "Primary protection"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithTwoMsgKeysViewOnly(
      "typeOfProtectionReference.checkYourAnswersLabel",
      "event8.typeOfProtection.primaryProtection",
      "1234567A"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentViewOnly(
      "lumpSumAmountAndDate.value.checkYourAnswersLabel",
      "£10.00"
    ),
    fakeSummaryListRowWithTextWithHiddenContentViewOnly(
      "lumpSumAmountAndDate.date.checkYourAnswersLabel",
      "22 March 2022"
    )
  )
}
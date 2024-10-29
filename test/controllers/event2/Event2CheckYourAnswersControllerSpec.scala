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

package controllers.event2

import base.SpecBase
import data.SampleData.{erOverviewSeq, sampleMemberJourneyDataEvent2}
import models.common.MembersDetails
import models.enumeration.EventType.Event2
import models.enumeration.VersionStatus.{Compiled, Submitted}
import models.{MemberSummaryPath, TaxYear, VersionInfo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.common.MembersDetailsPage
import pages.event2.{AmountPaidPage, DatePaidPage}
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
import utils.Event2MemberPageNumbers
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.Future


class Event2CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with BeforeAndAfterEach {

  private val mockCompileService = mock[CompileService]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[CompileService].toInstance(mockCompileService)
  )

  import Event2CheckYourAnswersControllerSpec._

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCompileService)
  }

  "Check Your Answers Controller for Event 2" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear
        .setOrException(VersionInfoPage, VersionInfo(3, Submitted))
        .setOrException(EventReportingOverviewPage, erOverviewSeq))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event2.routes.Event2CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view.render(list,
          "/manage-pension-scheme-event-report/report/1/event-2-click",
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
        val request = FakeRequest(GET, controllers.event2.routes.Event2CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view.render(
          list,
          continueUrl = "/manage-pension-scheme-event-report/report/1/event-2-click",
          Tuple2(Some(1), Some(Event2)),
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
        userAnswers = Some(sampleMemberJourneyDataEvent2
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(3, Submitted))),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any(), any())(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event2.routes.Event2CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRowsEvent2

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
        userAnswers = Some(sampleMemberJourneyDataEvent2
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(1, Submitted))),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any(), ArgumentMatchers.eq((Some(1), Some(Event2))))(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event2.routes.Event2CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRowsEvent2ViewOnly

        actual.size mustBe expected.size

        actual.zipWithIndex.map { case (a, i) =>
          a mustBe expected(i)
        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event2.routes.Event2CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the correct page onClick if all answers are present" in {
      when(mockCompileService.compileEvent(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful())

      val event2Answers = emptyUserAnswers
        .set(MembersDetailsPage(Event2, 0, Event2MemberPageNumbers.FIRST_PAGE_DECEASED), MembersDetails("Jane", "Doe", "AB123456A")).get
        .set(MembersDetailsPage(Event2, 0, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY), MembersDetails("John", "Doe", "AB123456D")).get
        .set(AmountPaidPage(0, Event2), BigDecimal(123)).get
        .set(DatePaidPage(0, Event2), LocalDate.of(2024,1,24)).get

      val userAnswersWithVersionInfo = event2Answers.setOrException(VersionInfoPage, VersionInfo(1, Compiled))
      val application = applicationBuilder(userAnswers = Some(userAnswersWithVersionInfo), extraModules).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event2.routes.Event2CheckYourAnswersController.onClick(0).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, MemberSummaryPath(Event2)).url
        verify(mockCompileService, times(1)).compileEvent(any(), any(), any(), any())(any())
      }
    }
    "must redirect to the correct page onClick if an answer is missing" in {
      when(mockCompileService.compileEvent(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful())

      val event2Answers = emptyUserAnswers
        .set(MembersDetailsPage(Event2, 0, Event2MemberPageNumbers.FIRST_PAGE_DECEASED), MembersDetails("Jane", "Doe", "AB123456A")).get
        .set(MembersDetailsPage(Event2, 0, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY), MembersDetails("John", "Doe", "AB123456D")).get
        .set(DatePaidPage(0, Event2), LocalDate.of(2024, 1, 24)).get

      val userAnswersWithVersionInfo = event2Answers.setOrException(VersionInfoPage, VersionInfo(1, Compiled))
      val application = applicationBuilder(userAnswers = Some(userAnswersWithVersionInfo), extraModules).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event2.routes.Event2CheckYourAnswersController.onClick(0).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual s"${
          controllers.event2.routes.AmountPaidController.onPageLoad(EmptyWaypoints, 0).url
        }?waypoints=event-2-check-answers-1"
      }
    }
  }
}

object Event2CheckYourAnswersControllerSpec {

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

  private def fakeSummaryListRowWithHtmlContentWithHiddenContent(messageKey: String, messageInterpolation: String, htmlContent: String, changeLink: String,
                                                                 hiddenContentChangeLink: String, hiddenContentInterpolation: String = "")
                                                                (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKey, messageInterpolation)
        ), ""),
      Value(Text(htmlContent), ""), "",
      Some(Actions("", List(ActionItem(changeLink, Text("Change"), Some(messages(hiddenContentChangeLink, hiddenContentInterpolation)), "", Map()))))
    )

  private def fakeSummaryListRowWithHtmlContentWithHiddenContentWithChangeViewOnly(messageKey: String, htmlContent: String)
                                                                          (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKey)
        ), ""),
      Value(HtmlContent(htmlContent), ""), "")

  private def fakeSummaryListRowWithHtmlContentWithHiddenContentViewOnly(messageKey: String, messageInterpolation: String, htmlContent: String)
                                                                (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKey, messageInterpolation)
        ), ""),
      Value(Text(htmlContent), ""), "")

  private def expectedMemberSummaryListRowsEvent2(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithChange(
      "deceasedMembersDetails.checkYourAnswersLabel",
      "Joe Bloggs",
      "/manage-pension-scheme-event-report/report/1/event-2-deceased-member-details?waypoints=event-2-check-answers-1",
      "deceasedMembersDetails.change.hidden"

    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithChange(
      "deceasedMembersDetails.checkYourAnswersLabel.nino",
      "AA234567D",
      "/manage-pension-scheme-event-report/report/1/event-2-deceased-member-details?waypoints=event-2-check-answers-1",
      "deceasedMembersDetails.change.nino.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithChange(
      "beneficiaryDetails.checkYourAnswersLabel",
      "Joe Bloggs",
      "/manage-pension-scheme-event-report/report/1/event-2-person-who-was-paid?waypoints=event-2-check-answers-1",
      "beneficiaryDetails.change.hidden"

    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithChange(
      "beneficiaryDetails.checkYourAnswersLabel.nino",
      "AA234567D",
      "/manage-pension-scheme-event-report/report/1/event-2-person-who-was-paid?waypoints=event-2-check-answers-1",
      "beneficiaryDetails.change.nino.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "amountPaid.event2.checkYourAnswersLabel",
      "Joe Bloggs",
      "£999.11",
      "/manage-pension-scheme-event-report/report/1/event-2-how-much-was-paid?waypoints=event-2-check-answers-1",
      "amountPaid.event2.change.hidden",
      "Joe Bloggs"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "datePaid.event2.checkYourAnswersLabel",
      messageInterpolation = "Joe Bloggs",
      htmlContent = "22 March 2022",
      changeLink = "/manage-pension-scheme-event-report/report/1/event-2-when-was-payment-made?waypoints=event-2-check-answers-1",
      hiddenContentChangeLink = "datePaid.event2.change.hidden"
    )
  )

  private def expectedMemberSummaryListRowsEvent2ViewOnly(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithChangeViewOnly(
      "deceasedMembersDetails.checkYourAnswersLabel",
      "Joe Bloggs"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithChangeViewOnly(
      "deceasedMembersDetails.checkYourAnswersLabel.nino",
      "AA234567D"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithChangeViewOnly(
      "beneficiaryDetails.checkYourAnswersLabel",
      "Joe Bloggs"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithChangeViewOnly(
      "beneficiaryDetails.checkYourAnswersLabel.nino",
      "AA234567D"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentViewOnly(
      "amountPaid.event2.checkYourAnswersLabel",
      "Joe Bloggs",
      "£999.11"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentViewOnly(
      "datePaid.event2.checkYourAnswersLabel",
      messageInterpolation = "Joe Bloggs",
      htmlContent = "22 March 2022"
    )
  )
}


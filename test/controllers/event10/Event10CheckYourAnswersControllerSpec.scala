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

package controllers.event10

import base.SpecBase
import data.SampleData.{sampleJourneyData10BecameAScheme, sampleJourneyData10CeasedToBecomeAScheme}
import models.enumeration.VersionStatus.Submitted
import models.{EROverview, EROverviewVersion, TaxYear, VersionInfo}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.{EventReportingOverviewPage, TaxYearPage, VersionInfoPage}
import play.api.i18n.Messages
import play.api.inject
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases
import uk.gov.hmrc.govukfrontend.views.Aliases._
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView
import java.time.LocalDate

class Event10CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  import Event10CheckYourAnswersControllerSpec._

  "Check Your Answers Controller for Event 10" - {

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
        Some(emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(1, Submitted))
        .setOrException(EventReportingOverviewPage, erOverviewSeq))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event10.routes.Event10CheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, "/manage-pension-scheme-event-report/new-report/event-10-click")(request, messages(application)).toString
      }
    }

    "must return OK and the correct summary list row items for a GET (Became a scheme) (change links present)" in {
      val mockView = mock[CheckYourAnswersView]
      val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
        inject.bind[CheckYourAnswersView].toInstance(mockView)
      )

      val application = applicationBuilder(
        userAnswers = Some(sampleJourneyData10BecameAScheme
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(3, Submitted))),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any())(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event10.routes.Event10CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRowsEvent10BecomeAScheme

        actual.size mustBe expected.size

        actual.zipWithIndex.map { case (a, i) =>
          a mustBe expected(i)
        }
      }
    }

    "must return OK and the correct summary list row items for a GET (Ceased to become a scheme) (change links present)" in {
      val mockView = mock[CheckYourAnswersView]
      val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
        inject.bind[CheckYourAnswersView].toInstance(mockView)
      )

      val application = applicationBuilder(
        userAnswers = Some(sampleJourneyData10CeasedToBecomeAScheme
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(3, Submitted))),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any())(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event10.routes.Event10CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRowsEvent10CeasedToBecomeAScheme

        actual.size mustBe expected.size

        actual.zipWithIndex.map { case (a, i) =>
          a mustBe expected(i)
        }
      }
    }

    "must return OK and the correct summary list row items for a GET (Ceased to become a scheme) (NO change links present)" in {
      val mockView = mock[CheckYourAnswersView]
      val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
        inject.bind[CheckYourAnswersView].toInstance(mockView)
      )

      val application = applicationBuilder(
        userAnswers = Some(sampleJourneyData10CeasedToBecomeAScheme
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(1, Submitted))),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any())(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event10.routes.Event10CheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRowsEvent10CeasedToBecomeASchemeViewOnly

        actual.size mustBe expected.size

        actual.zipWithIndex.map { case (a, i) =>
          a mustBe expected(i)
        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event10.routes.Event10CheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

object Event10CheckYourAnswersControllerSpec {
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

  private def fakeSummaryListRowWithHtmlContentWithHiddenContentViewOnly(messageKey: String, htmlContent: String)
                                                                (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKey)
        ), ""),
      Value(HtmlContent(htmlContent), ""), "",
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

  private def fakeSummaryListRowWithTextViewOnly(messageKey: String, text: String)
                                                (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKey)
        ), ""),
      Value(Text(text), ""), "")

  private def expectedMemberSummaryListRowsEvent10BecomeAScheme(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "becomeOrCeaseScheme.checkYourAnswersLabel",
      "It became an investment regulated pension scheme",
      "/manage-pension-scheme-event-report/new-report/event-10-become-or-ceased?waypoints=event-10-check-answers",
      "becomeOrCeaseScheme.change.hidden"
    ),
    fakeSummaryListRowWithTextWithHiddenContent(
      "became.schemeChangeDate.checkYourAnswersLabel",
      "22 March 2022",
      "/manage-pension-scheme-event-report/new-report/event-10-when-scheme-changed?waypoints=event-10-check-answers",
      "became.schemeChangeDate.change.hidden"
    ),
    fakeSummaryListRowWithTextWithHiddenContent(
      "contractsOrPolicies.checkYourAnswersLabel",
      "Yes",
      "/manage-pension-scheme-event-report/new-report/event-10-are-investments-contracts-or-policies-of-insurance?waypoints=event-10-check-answers",
      "contractsOrPolicies.change.hidden"
    )
  )

  private def expectedMemberSummaryListRowsEvent10CeasedToBecomeAScheme(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "becomeOrCeaseScheme.checkYourAnswersLabel",
      "It has ceased to be an investment regulated pension scheme",
      "/manage-pension-scheme-event-report/new-report/event-10-become-or-ceased?waypoints=event-10-check-answers",
      "becomeOrCeaseScheme.change.hidden"
    ),
    fakeSummaryListRowWithTextWithHiddenContent(
      "ceased.schemeChangeDate.checkYourAnswersLabel",
      "22 March 2022",
      "/manage-pension-scheme-event-report/new-report/event-10-when-scheme-changed?waypoints=event-10-check-answers",
      "ceased.schemeChangeDate.change.hidden"
    )
  )

  private def expectedMemberSummaryListRowsEvent10CeasedToBecomeASchemeViewOnly(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentWithHiddenContentViewOnly(
      "becomeOrCeaseScheme.checkYourAnswersLabel",
      "It has ceased to be an investment regulated pension scheme"
    ),
    fakeSummaryListRowWithTextViewOnly(
      "ceased.schemeChangeDate.checkYourAnswersLabel",
      "22 March 2022"
    )
  )
}
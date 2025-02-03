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

package controllers.event1

import base.SpecBase
import data.SampleData.{erOverviewSeq, sampleEmployerJourneyDataEvent1, sampleMemberJourneyDataEvent1}
import models.common.MembersDetails
import models.enumeration.EventType.Event1
import models.enumeration.VersionStatus.{Compiled, Submitted}
import models.event1.PaymentNature.MemberOther
import models.event1.{PaymentDetails, WhoReceivedUnauthPayment}
import models.{TaxYear, VersionInfo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.common.MembersDetailsPage
import pages.event1.{DoYouHoldSignedMandatePage, PaymentValueAndDatePage, ValueOfUnauthorisedPaymentPage, WhoReceivedUnauthPaymentPage}
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

class Event1CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with BeforeAndAfterEach {

  private val mockCompileService = mock[CompileService]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[CompileService].toInstance(mockCompileService)
  )

  import Event1CheckYourAnswersControllerSpec._

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockCompileService)
  }

  "Check Your Answers Controller for Event 1" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear
        .setOrException(VersionInfoPage, VersionInfo(3, Submitted))
        .setOrException(EventReportingOverviewPage, erOverviewSeq))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event1.routes.Event1CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view.render(list,
          continueUrl = "/manage-pension-scheme-event-report/report/1/event-1-click",
          Tuple2(None, None),
          request,
          messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET (View Only (different heading))" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear
        .setOrException(VersionInfoPage, VersionInfo(1, Submitted))
        .setOrException(EventReportingOverviewPage, erOverviewSeq))).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event1.routes.Event1CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view.render(
          list,
          continueUrl = "/manage-pension-scheme-event-report/report/1/event-1-click",
          Tuple2(Some(1), Some(Event1)),
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
        userAnswers = Some(sampleMemberJourneyDataEvent1
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(3, Submitted))),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any(), any())(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event1.routes.Event1CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRows

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
        userAnswers = Some(sampleMemberJourneyDataEvent1
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(1, Submitted))),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any(), ArgumentMatchers.eq((Some(1), Some(Event1))))
        (any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event1.routes.Event1CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRowsViewOnly

        actual.size mustBe expected.size

        actual.zipWithIndex.map { case (a, i) =>
          a mustBe expected(i)
        }
      }
    }

    "must return OK and the correct summary list row items for a GET (employer)" in {
      val mockView = mock[CheckYourAnswersView]
      val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
        inject.bind[CheckYourAnswersView].toInstance(mockView)
      )

      val application = applicationBuilder(
        userAnswers = Some(sampleEmployerJourneyDataEvent1
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(3, Submitted))),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any(), any())(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event1.routes.Event1CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedEmployerSummaryListRows

        actual.size mustBe expected.size

        actual.zipWithIndex.map { case (a, i) =>
          a mustBe expected(i)
        }
      }
    }

    "must return OK and the correct summary list row items for a GET (employer) (NO change links present)" in {
      val mockView = mock[CheckYourAnswersView]
      val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
        inject.bind[CheckYourAnswersView].toInstance(mockView)
      )

      val application = applicationBuilder(
        userAnswers = Some(sampleEmployerJourneyDataEvent1
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(1, Submitted))),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any(), ArgumentMatchers.eq((Some(1),
          Some(Event1))))(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event1.routes.Event1CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedEmployerSummaryListRowsViewOnly

        actual.size mustBe expected.size

        actual.zipWithIndex.map { case (a, i) =>
          a mustBe expected(i)
        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event1.routes.Event1CheckYourAnswersController.onPageLoad(0).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the correct page onClick if all expected answers are present" in {
      when(mockCompileService.compileEvent(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful())

      val event1Answers = emptyUserAnswers.set(WhoReceivedUnauthPaymentPage(0), WhoReceivedUnauthPayment.Member).get
        .set(MembersDetailsPage(Event1, 0), MembersDetails("Jane", "Doe", "AB 123456 A")).get
        .set(DoYouHoldSignedMandatePage(0), true).get
        .set(ValueOfUnauthorisedPaymentPage(0), false).get
        .set(pages.event1.member.PaymentNaturePage(0), MemberOther).get
        .set(PaymentValueAndDatePage(0), PaymentDetails(BigDecimal(123), LocalDate.of(2024, 2, 2))).get

      val userAnswersWithVersionInfo = event1Answers.setOrException(VersionInfoPage, VersionInfo(1, Compiled))
      val application = applicationBuilder(userAnswers = Some(userAnswersWithVersionInfo), extraModules).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event1.routes.Event1CheckYourAnswersController.onClick(0).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.event1.routes.UnauthPaymentSummaryController.onPageLoad(EmptyWaypoints).url
        verify(mockCompileService, times(1)).compileEvent(any(), any(), any(), any())(any())
      }
    }
    "must redirect to the correct page onClick if an answer is missing" in {
      when(mockCompileService.compileEvent(any(), any(), any(), any())(any()))
        .thenReturn(Future.successful())

      val userAnswersWithVersionInfo = emptyUserAnswers.setOrException(VersionInfoPage, VersionInfo(1, Compiled))
      val application = applicationBuilder(userAnswers = Some(userAnswersWithVersionInfo), extraModules).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event1.routes.Event1CheckYourAnswersController.onClick(0).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual s"${
          controllers.event1.routes.WhoReceivedUnauthPaymentController.onPageLoad(EmptyWaypoints, 0).url
        }?waypoints=event-1-check-answers-1"
      }
    }
  }
}

object Event1CheckYourAnswersControllerSpec {

  private def fakeSummaryListRowWithText(messageKey: String, text: String, changeLink: String)
                                        (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKey)
        ), ""),
      Value(Text(text), ""), "",
      Some(Actions("", List(ActionItem(changeLink, Text("Change"), Some(messages(messageKey)), "", Map()))))
    )

  private def fakeSummaryListRowWithHtmlContent(messageKey: String, htmlContent: String, changeLink: String)
                                               (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKey)
        ), ""),
      Value(HtmlContent(htmlContent), ""), "",
      Some(Actions("", List(ActionItem(changeLink, Text("Change"), Some(messages(messageKey)), "", Map()))))
    )

  private def fakeSummaryListRowWithHtmlContentWithHiddenContentWithChange(messageKey: String, htmlContent: String,
                                                                           changeLink: String, hiddenKey: String)
                                                                          (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKey)
        ), ""),
      Value(HtmlContent(htmlContent), ""), "",
      Some(Actions("", List(ActionItem(changeLink, Text("Change"), Some(messages(hiddenKey)), "", Map()))))
    )

  private def fakeSummaryListRowWithHtmlContentWithHiddenContent(messageKey: String,
                                                                 htmlContent: String,
                                                                 changeLink: String,
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

  private def fakeSummaryListRowWithTextWithHiddenContent(messageKey: String, text: String,
                                                          changeLink: String,
                                                          hiddenContentChangeLink: String)
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

  private def fakeSummaryListRowWithHtmlContentViewOnly(messageKey: String, htmlContent: String)
                                               (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKey)
        ), ""),
      Value(HtmlContent(htmlContent), ""), "")
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

  private def fakeSummaryListRowWithTextWithHiddenContentViewOnly(messageKey: String, text: String)
                                                         (implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      Key(
        Text(
          messages(messageKey)
        ), ""),
      Value(Text(text), ""), "")

  private val membersDetailsContent = s"""<p class="govuk-body">Joe Bloggs</p>
                                         |<p class="govuk-body">AA234567D</p>""".stripMargin

  private val paymentDetails = s"""<p class="govuk-body">£1,000.00</p>
                                  |<p class="govuk-body">08 November 2022</p>""".stripMargin

  private def expectedMemberSummaryListRows(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithChange(
      "membersDetails.checkYourAnswersLabel",
      membersDetailsContent,
      "/manage-pension-scheme-event-report/report/1/event-1-member-details?waypoints=event-1-check-answers-1",
      "membersDetails.change.hidden"
    ),
    fakeSummaryListRowWithText(
      "doYouHoldSignedMandate.checkYourAnswersLabel",
      "No",
      "/manage-pension-scheme-event-report/report/1/event-1-mandate?waypoints=event-1-check-answers-1"
    ),
    fakeSummaryListRowWithText(
      "valueOfUnauthorisedPayment.checkYourAnswersLabel",
      "No",
      "/manage-pension-scheme-event-report/report/1/event-1-payment-value?waypoints=event-1-check-answers-1"
    ),
    fakeSummaryListRowWithHtmlContent(
      "paymentNature.checkYourAnswersLabel",
      "Benefit in kind",
      "/manage-pension-scheme-event-report/report/1/event-1-member-payment-nature?waypoints=event-1-check-answers-1"
    ),
    fakeSummaryListRowWithText(
      "benefitInKindBriefDescription.checkYourAnswersLabel",
      "Test description",
      "/manage-pension-scheme-event-report/report/1/event-1-benefit-in-kind?waypoints=event-1-check-answers-1"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "paymentValueAndDate.value.checkYourAnswersLabel",
      paymentDetails,
      "/manage-pension-scheme-event-report/report/1/event-1-payment-details?waypoints=event-1-check-answers-1",
      "paymentValueAndDate.value.change.hidden"
    )
  )

  private def expectedMemberSummaryListRowsViewOnly(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithChangeViewOnly(
      "membersDetails.checkYourAnswersLabel",
      membersDetailsContent
    ),
    fakeSummaryListRowWithTextViewOnly(
      "doYouHoldSignedMandate.checkYourAnswersLabel",
      "No"
    ),
    fakeSummaryListRowWithTextViewOnly(
      "valueOfUnauthorisedPayment.checkYourAnswersLabel",
      "No"
    ),
    fakeSummaryListRowWithHtmlContentViewOnly(
      "paymentNature.checkYourAnswersLabel",
      "Benefit in kind"
    ),
    fakeSummaryListRowWithTextViewOnly(
      "benefitInKindBriefDescription.checkYourAnswersLabel",
      "Test description"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentViewOnly(
      "paymentValueAndDate.value.checkYourAnswersLabel",
      paymentDetails
    )
  )

  private val companyDetailsContent = s"""<p class="govuk-body">Company Name</p>
                                         |<p class="govuk-body">12345678</p>""".stripMargin

  private def expectedEmployerSummaryListRows(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContent(
      "companyDetails.title",
      companyDetailsContent,
      "/manage-pension-scheme-event-report/report/1/event-1-company-details?waypoints=event-1-check-answers-1"
    ),
    fakeSummaryListRowWithHtmlContent(
      "companyDetails.CYA.companyAddress",
      """<span class="govuk-!-display-block">addr11</span><span class="govuk-!-display-block">addr12</span><span class="govuk-!-display-block">addr13</span><span class="govuk-!-display-block">addr14</span><span class="govuk-!-display-block">zz11zz</span><span class="govuk-!-display-block">United Kingdom</span>""".stripMargin,
      "/manage-pension-scheme-event-report/report/1/event-1-company-postcode?waypoints=event-1-check-answers-1"
    ),
    fakeSummaryListRowWithHtmlContent(
      "paymentNature.checkYourAnswersLabel",
      "Tangible moveable property held directly or indirectly by an investment-regulated pension scheme",
      "/manage-pension-scheme-event-report/report/1/event-1-employer-payment-nature?waypoints=event-1-check-answers-1"
    ),
    fakeSummaryListRowWithText(
      "employerTangibleMoveableProperty.checkYourAnswersLabel",
      "Another test description",
      "/manage-pension-scheme-event-report/report/1/event-1-employer-tangible-moveable-property?waypoints=event-1-check-answers-1"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "paymentValueAndDate.value.checkYourAnswersLabel",
      paymentDetails,
      "/manage-pension-scheme-event-report/report/1/event-1-payment-details?waypoints=event-1-check-answers-1",
      "paymentValueAndDate.value.change.hidden"
    )
  )

  private def expectedEmployerSummaryListRowsViewOnly(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentViewOnly(
      "companyDetails.title",
      companyDetailsContent
    ),
    fakeSummaryListRowWithHtmlContentViewOnly(
      "companyDetails.CYA.companyAddress",
      """<span class="govuk-!-display-block">addr11</span><span class="govuk-!-display-block">addr12</span><span class="govuk-!-display-block">addr13</span><span class="govuk-!-display-block">addr14</span><span class="govuk-!-display-block">zz11zz</span><span class="govuk-!-display-block">United Kingdom</span>""".stripMargin
    ),
    fakeSummaryListRowWithHtmlContentViewOnly(
      "paymentNature.checkYourAnswersLabel",
      "Tangible moveable property held directly or indirectly by an investment-regulated pension scheme"
    ),
    fakeSummaryListRowWithTextViewOnly(
      "employerTangibleMoveableProperty.checkYourAnswersLabel",
      "Another test description"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentViewOnly(
      "paymentValueAndDate.value.checkYourAnswersLabel",
      paymentDetails
    )
  )
}

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

package controllers.common

import base.SpecBase
import connectors.UserAnswersCacheConnector
import controllers.common.MembersSummaryControllerSpec.{cYAHref, fakeChangeUrl, fakeRemoveUrl, paginationStats26Members}
import controllers.common.routes._
import data.SampleData
import data.SampleData._
import forms.common.MembersSummaryFormProvider
import helpers.DateHelper
import models.enumeration.EventType
import models.enumeration.EventType._
import models.enumeration.VersionStatus.Submitted
import models.{Index, MemberSummaryPath, TaxYear, UserAnswers, VersionInfo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.common.MembersSummaryPage
import pages.{EmptyWaypoints, EventReportingOverviewPage, TaxYearPage, VersionInfoPage}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.EventPaginationService
import services.EventPaginationService.PaginationStats
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions}
import viewmodels.{Message, SummaryListRowWithTwoValues}
import views.html.common.MembersSummaryView

import java.time.LocalDate
import scala.concurrent.Future

class MembersSummaryControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints
  private val formProvider = new MembersSummaryFormProvider()
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockEventPaginationService = mock[EventPaginationService]
  private val mockTaxYear = mock[DateHelper]
  private val dateYear = 2022
  private val dateMonth = 5
  private val dateDay = 12
  private val totalAmount: String = "10.00"
  private val validAnswer = LocalDate.of(dateYear, dateMonth, dateDay)

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector),
    bind[EventPaginationService].toInstance(mockEventPaginationService),
    bind[DateHelper].toInstance(mockTaxYear)
  )

  private val searchValue = "xjshaiak"

  private def pageTitle(eventType: EventType, selectedTaxYear: String, searching: Boolean = false) = if (searching) {
    Messages(s"membersSummary.event${eventType.toString}.title.search", searchValue, selectedTaxYear)
  } else {
    Messages(s"membersSummary.event${eventType.toString}.title", selectedTaxYear)
  }

  private def getRoute(eventType: EventType): String = routes.MembersSummaryController.onPageLoad(waypoints, MemberSummaryPath(eventType)).url
  private def getRouteWithPagination(eventType: EventType): String = routes.MembersSummaryController.onPageLoadPaginated(waypoints, MemberSummaryPath(eventType), 0).url

  private def postRoute(eventType: EventType): String = routes.MembersSummaryController.onSubmit(waypoints, MemberSummaryPath(eventType)).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
    when(mockTaxYear.now).thenReturn(validAnswer)
  }

  "MembersSummary Controller" - {
    behave like testSuite(
      formProvider(Event2),
      Event2,
      sampleMemberJourneyDataEvent2,
      SampleData.amountPaid.toString(),
      fakeChangeUrl(Event2), "999.11")

    behave like testSuite(
      formProvider(Event3),
      Event3,
      sampleMemberJourneyDataEvent3and4and5(Event3),
      SampleData.paymentDetailsCommon.amountPaid.setScale(2).toString,
      fakeChangeUrl(Event3),
      totalAmount)

    behave like testSuite(
      formProvider(Event4),
      Event4,
      sampleMemberJourneyDataEvent3and4and5(Event4),
      SampleData.paymentDetailsCommon.amountPaid.setScale(2).toString(),
      fakeChangeUrl(Event4),
      totalAmount)

    behave like testSuite(
      formProvider(Event5),
      Event5,
      sampleMemberJourneyDataEvent3and4and5(Event5),
      SampleData.paymentDetailsCommon.amountPaid.setScale(2).toString(),
      fakeChangeUrl(Event5),
      totalAmount)

    behave like testSuite(
      formProvider(Event6),
      Event6,
      sampleMemberJourneyDataEvent6,
      SampleData.crystallisedDetails.amountCrystallised.setScale(2).toString(),
      fakeChangeUrl(Event6),
      totalAmount)

    behave like testSuite(
      formProvider(Event8),
      Event8,
      sampleMemberJourneyDataEvent8,
      SampleData.lumpSumDetails.lumpSumAmount.setScale(2).toString(),
      fakeChangeUrl(Event8),
      totalAmount)

    behave like testSuite(
      formProvider(Event8A),
      Event8A,
      sampleMemberJourneyDataEvent8A,
      SampleData.lumpSumDetails.lumpSumAmount.setScale(2).toString(),
      fakeChangeUrl(Event8A),
      totalAmount)

    behave like testSuite(
      formProvider(Event22),
      Event22,
      sampleMemberJourneyDataEvent22and23(Event22),
      SampleData.totalPaymentAmountEvent22and23.setScale(2).toString(),
      fakeChangeUrl(Event22),
      totalAmount)

    behave like testSuite(
      formProvider(Event23),
      Event23,
      sampleMemberJourneyDataEvent22and23(Event23),
      SampleData.totalPaymentAmountEvent22and23.setScale(2).toString(),
      fakeChangeUrl(Event23),
      totalAmount)

    behave like testSuite(
      formProvider(Event24),
      Event24,
      sampleMemberJourneyDataEvent24(Event24),
      SampleData.crystallisedAmountEvent24.setScale(2).toString(),
      fakeChangeUrl(Event24),
      totalAmount)


    behave like testReturnOkAndCorrectViewWithPagination(
      formProvider(Event2), Event2, "260.00", SampleData.sampleMemberJourneyDataWithPaginationEvent2)

    behave like testReturnOkAndCorrectViewWithPagination(
      formProvider(Event3), Event3, "260.00", SampleData.event345UADataWithPagnination(Event3))

    behave like testReturnOkAndCorrectViewWithPagination(
      formProvider(Event4), Event4, "260.00", SampleData.event345UADataWithPagnination(Event4))

    behave like testReturnOkAndCorrectViewWithPagination(
      formProvider(Event5), Event5, "260.00", SampleData.event345UADataWithPagnination(Event5))

    behave like testReturnOkAndCorrectViewWithPagination(
      formProvider(Event6), Event6, "260.00", SampleData.event6UADataWithPagination)

    behave like testReturnOkAndCorrectViewWithPagination(
      formProvider(Event8), Event8, "260.00", SampleData.event8UADataWithPagination)

    behave like testReturnOkAndCorrectViewWithPagination(
      formProvider(Event8A), Event8A, "260.00", SampleData.event8aUADataWithPagination)

    behave like testReturnOkAndCorrectViewWithPagination(
      formProvider(Event22), Event22, "260.00", SampleData.event22and23UADataWithPagination(Event22))

    behave like testReturnOkAndCorrectViewWithPagination(
      formProvider(Event23), Event23, "260.00", SampleData.event22and23UADataWithPagination(Event23))

    behave like testReturnOkAndCorrectViewWithPagination(
      formProvider(Event24), Event24, "260.00", SampleData.event24UADataWithPagination(Event24))

  }

  private def testSuite(form: Form[Boolean], eventType: EventType, sampleData: UserAnswers, secondValue: String, href: String, totalAmount: String): Unit = {
    testReturnOkAndCorrectView(eventType, form, sampleData, secondValue, href, totalAmount)
    testSaveAnswerAndRedirectWhenValid(eventType)
    testBadRequestForInvalidDataSubmission(eventType, form)
  }

  private def testReturnOkAndCorrectView(eventType: EventType,
                                         form: Form[Boolean],
                                         sampleData: UserAnswers,
                                         secondValue: String,
                                         href: String,
                                         totalAmount: String): Unit = {
    s"must return OK and the correct view for a GET for Event $eventType" in {
      val application = applicationBuilder(userAnswers = Some(sampleData
        .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
        .setOrException(EventReportingOverviewPage, erOverviewSeq)
        .setOrException(VersionInfoPage, VersionInfo(3, Submitted)))).build()

      running(application) {
        val request = FakeRequest(GET, getRoute(eventType))
        val result = route(application, request).value
        val eventPaginationService = application.injector.instanceOf[EventPaginationService]
        val view = application.injector.instanceOf[MembersSummaryView]

        val expectedSeq =
          Seq(
            SummaryListRowWithTwoValues(
              key = SampleData.memberDetails.fullName,
              firstValue = SampleData.memberDetails.nino,
              secondValue = secondValue,
              actions = Some(Actions(
                items = Seq(
                  ActionItem(
                    content = Text(Message("site.view")),
                    href = href
                  ),
                  ActionItem(
                    content = Text(Message("site.remove")),
                    href = fakeRemoveUrl(eventType, 0)
                  )
                )
              ))
            ))

        val title = pageTitle(eventType, "2023")

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view.render(form, title, waypoints, eventType, expectedSeq, totalAmount,
          selectedTaxYear = "2023",
          request = request,
          messages = messages(application),
          paginationStats = eventPaginationService.paginateMappedMembers(expectedSeq, 1),
          pageNumber = Index(0),
          searchValue = None,
          searchHref = s"/manage-pension-scheme-event-report/report/event-$eventType-summary").toString
      }
    }
  }

  private def testReturnOkAndCorrectViewOnSearch(eventType: EventType,
                                         form: Form[Boolean],
                                         sampleData: UserAnswers,
                                         secondValue: String,
                                         href: String,
                                         totalAmount: String): Unit = {
    s"must return OK and the correct view for a GET for Event $eventType" in {
      val application = applicationBuilder(userAnswers = Some(sampleData
        .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
        .setOrException(EventReportingOverviewPage, erOverviewSeq)
        .setOrException(VersionInfoPage, VersionInfo(3, Submitted)))).build()

      running(application) {
        val request = FakeRequest(GET, getRoute(eventType))
        val result = route(application, request).value
        val eventPaginationService = application.injector.instanceOf[EventPaginationService]
        val view = application.injector.instanceOf[MembersSummaryView]

        val expectedSeq =
          Seq(
            SummaryListRowWithTwoValues(
              key = SampleData.memberDetails.fullName,
              firstValue = SampleData.memberDetails.nino,
              secondValue = secondValue,
              actions = Some(Actions(
                items = Seq(
                  ActionItem(
                    content = Text(Message("site.view")),
                    href = href
                  ),
                  ActionItem(
                    content = Text(Message("site.remove")),
                    href = fakeRemoveUrl(eventType, 0)
                  )
                )
              ))
            ))

        val title = pageTitle(eventType, "2023", true)

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces()mustEqual view.render(form, title, waypoints, eventType, expectedSeq, totalAmount,
          selectedTaxYear = "2023",
          request = request,
          messages = messages(application),
          paginationStats = eventPaginationService.paginateMappedMembers(expectedSeq, 1),
          pageNumber = Index(0),
          searchValue = Some(searchValue),
          searchHref = s"/manage-pension-scheme-event-report/report/event-$eventType-summary").toString
      }
    }
  }

  private def testReturnOkAndCorrectViewWithPagination(form: Form[Boolean],
                                                       eventType: EventType,
                                                       totalAmount: String,
                                                       sampleData: UserAnswers): Unit = {
    s"must return OK and the correct view with pagination for a GET for Event $eventType" in {

      val fakeMembers = (1 to 25).map(i =>
        SummaryListRowWithTwoValues(memberDetails.fullName, memberDetails.nino, "10.00",
          actions = Some(Actions(
            items = Seq(
              ActionItem(
                content = Text(Message("site.view")),
                href = cYAHref(eventType, i - 1)
              ),
              ActionItem(
                content = Text(Message("site.remove")),
                href = fakeRemoveUrl(eventType, i - 1)
              )
            )
          ))
        ))

      when(mockEventPaginationService.paginateMappedMembers[SummaryListRowWithTwoValues](any(), any())).thenReturn(paginationStats26Members(fakeMembers))

      val application = applicationBuilder(userAnswers = Some(sampleData
        .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
        .setOrException(EventReportingOverviewPage, erOverviewSeq)
        .setOrException(VersionInfoPage, VersionInfo(3, Submitted)))).build()
      val totalNumberOfMembers = 26

      running(application) {
        val request = FakeRequest(GET, getRouteWithPagination(eventType))
        val result = route(application, request).value
        val view = application.injector.instanceOf[MembersSummaryView]

        val expectedPaginationStats = PaginationStats(
          slicedMembers = fakeMembers,
          totalNumberOfMembers,
          totalNumberOfPages = 2,
          pageStartAndEnd = (1, 25),
          pagerSeq = Seq(1, 2))

        val title = pageTitle(eventType, "2023")

        status(result) mustEqual OK
        val expectedView = view.render(
          form,
          title,
          waypoints,
          eventType,
          fakeMembers,
          totalAmount,
          selectedTaxYear = "2023",
          expectedPaginationStats,
          pageNumber = 0,
          None,
          s"/manage-pension-scheme-event-report/report/event-$eventType-summary",
          request,
          messages(application)).toString

        contentAsString(result).removeAllNonces()mustEqual expectedView
      }
    }
  }

  private def testSaveAnswerAndRedirectWhenValid(eventType: EventType): Unit = {
    s"must save the answer and redirect to the next page when valid data is submitted for Event $eventType" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(3, Submitted))),
          extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute(eventType)).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswersWithTaxYear.set(MembersSummaryPage(eventType, 1), true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual MembersSummaryPage(eventType, 1).navigate(waypoints, emptyUserAnswersWithTaxYear, updatedAnswers).url
      }
    }
  }

  private def testBadRequestForInvalidDataSubmission(eventType: EventType, form: Form[Boolean]): Unit = {
    s"must return bad request when invalid data is submitted for Event $eventType" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))
      val emptyPageStats = PaginationStats(Seq(), 0, 1, (0,1),Seq())
      when(mockEventPaginationService.paginateMappedMembers(any(), any())).thenReturn(emptyPageStats)
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear
        .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
        .setOrException(EventReportingOverviewPage, erOverviewSeq)
        .setOrException(VersionInfoPage, VersionInfo(3, Submitted))),
        extraModules).build()

      running(application) {
        val request = FakeRequest(POST, postRoute(eventType)).withFormUrlEncodedBody(("value", "invalid"))
        val view = application.injector.instanceOf[MembersSummaryView]
        val boundForm = form.bind(Map("value" -> "invalid"))
        val result = route(application, request).value

        val title = pageTitle(eventType, "2023")

        status(result) mustEqual BAD_REQUEST
        contentAsString(result).removeAllNonces()mustEqual view.render(
          boundForm,
          title,
          waypoints,
          eventType,
          Nil,
          total = "0.00",
          selectedTaxYear = "2023",
          request = request,
          messages = messages(application), paginationStats = emptyPageStats, pageNumber = Index(0), searchValue = None, searchHref = s"/manage-pension-scheme-event-report/report/event-$eventType-summary").toString
        verify(mockUserAnswersCacheConnector, never).save(any(), any(), any())(any(), any(), any())
      }
    }
  }
}

object MembersSummaryControllerSpec {

  private val lowerBound = 0
  private val upperBound = 24
  private val totalMembers = 26

  private def fakeChangeUrl(eventType: EventType): String = {
    eventType match {
      case Event2 => controllers.event2.routes.Event2CheckYourAnswersController.onPageLoad(0).url
      case Event3 => controllers.event3.routes.Event3CheckYourAnswersController.onPageLoad(0).url
      case Event4 => controllers.event4.routes.Event4CheckYourAnswersController.onPageLoad(0).url
      case Event5 => controllers.event5.routes.Event5CheckYourAnswersController.onPageLoad(0).url
      case Event6 => controllers.event6.routes.Event6CheckYourAnswersController.onPageLoad(0).url
      case Event8 => controllers.event8.routes.Event8CheckYourAnswersController.onPageLoad(0).url
      case Event8A => controllers.event8a.routes.Event8ACheckYourAnswersController.onPageLoad(0).url
      case Event22 => controllers.event22.routes.Event22CheckYourAnswersController.onPageLoad(0).url
      case Event23 => controllers.event23.routes.Event23CheckYourAnswersController.onPageLoad(0).url
      case Event24 => controllers.event24.routes.Event24CheckYourAnswersController.onPageLoad(0).url
      case _ => "Not a member event used on this summary page"
    }
  }

  private def fakeRemoveUrl(eventType: EventType, index: Index): String = {
    eventType match {
      case Event2 | Event3 |
           Event4 | Event5 |
           Event6 | Event8 |
           Event8A | Event22 |
           Event23 | Event24 => RemoveMemberController.onPageLoad(EmptyWaypoints, eventType, index).url
      case _ => "Not a member event used on this summary page"
    }
  }

  private def cYAHref(eventType: EventType, index: Int): String = {
    eventType match {
      case Event2 => controllers.event2.routes.Event2CheckYourAnswersController.onPageLoad(index).url
      case Event3 => controllers.event3.routes.Event3CheckYourAnswersController.onPageLoad(index).url
      case Event4 => controllers.event4.routes.Event4CheckYourAnswersController.onPageLoad(index).url
      case Event5 => controllers.event5.routes.Event5CheckYourAnswersController.onPageLoad(index).url
      case Event6 => controllers.event6.routes.Event6CheckYourAnswersController.onPageLoad(index).url
      case Event8 => controllers.event8.routes.Event8CheckYourAnswersController.onPageLoad(index).url
      case Event8A => controllers.event8a.routes.Event8ACheckYourAnswersController.onPageLoad(index).url
      case Event22 => controllers.event22.routes.Event22CheckYourAnswersController.onPageLoad(index).url
      case Event23 => controllers.event23.routes.Event23CheckYourAnswersController.onPageLoad(index).url
      case Event24 => controllers.event24.routes.Event24CheckYourAnswersController.onPageLoad(index).url
      case _ => "Not a member event"
    }
  }

  private def paginationStats26Members(fakeMembers: Seq[SummaryListRowWithTwoValues]) = PaginationStats(
    fakeMembers.slice(lowerBound, upperBound),
    totalMembers,
    totalNumberOfPages = 2,
    pageStartAndEnd = (1, 25),
    pagerSeq = 1 to 2
  )
}

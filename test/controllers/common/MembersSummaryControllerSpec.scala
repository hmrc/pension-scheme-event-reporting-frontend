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

package controllers.common

import base.SpecBase
import connectors.UserAnswersCacheConnector
import controllers.common.MembersSummaryControllerSpec.{fake26MappedMembers, fakeChangeUrl, fakeRemoveUrl, paginationStats26Members}
import controllers.common.routes._
import data.SampleData
import data.SampleData._
import forms.common.MembersSummaryFormProvider
import helpers.DateHelper
import models.UserAnswers
import models.enumeration.EventType
import models.enumeration.EventType._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.common.MembersSummaryPage
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.EventPaginationService
import services.EventPaginationService.PaginationStats
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import viewmodels.{Message, SummaryListRowWithTwoValues}
import views.html.common.{MembersSummaryView, MembersSummaryViewWithPagination}

import java.time.LocalDate
import scala.concurrent.Future

class MembersSummaryControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private val formProvider = new MembersSummaryFormProvider()
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockEventPaginationService = mock[EventPaginationService]

  private val mockTaxYear = mock[DateHelper]
  private val validAnswer = LocalDate.of(2022, 5, 12)

  private def getRoute(eventType: EventType): String = routes.MembersSummaryController.onPageLoad(waypoints, eventType).url

  private def getRouteWithPagination(eventType: EventType): String = routes.MembersSummaryController.onPageLoadWithPageNumber(waypoints, eventType, 0).url

  private def postRoute(eventType: EventType): String = routes.MembersSummaryController.onSubmit(waypoints, eventType).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector),
    bind[EventPaginationService].toInstance(mockEventPaginationService),
    bind[DateHelper].toInstance(mockTaxYear)
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
    when(mockTaxYear.now).thenReturn(validAnswer)
  }

  /*
  fakeChangeLink(eventType) -> String = s"manual expected URL"
  fakeRemoveLink(eventType) -> String = s"manual expected URL"
   */

  "MembersSummary Controller" - {
    behave like testSuite(formProvider(Event2), Event2, sampleMemberJourneyDataEvent2, SampleData.amountPaid.toString(),
      fakeChangeUrl(Event2), "999.11")

    behave like testSuite(formProvider(Event3), Event3, sampleMemberJourneyDataEvent3and4and5(Event3), SampleData.paymentDetailsCommon.amountPaid.setScale(2).toString,
      fakeChangeUrl(Event3), "10.00")

    behave like testSuite(formProvider(Event4), Event4, sampleMemberJourneyDataEvent3and4and5(Event4), SampleData.paymentDetailsCommon.amountPaid.setScale(2).toString(),
      fakeChangeUrl(Event4), "10.00")

    behave like testSuite(formProvider(Event5), Event5, sampleMemberJourneyDataEvent3and4and5(Event5), SampleData.paymentDetailsCommon.amountPaid.setScale(2).toString(),
      fakeChangeUrl(Event5), "10.00")

    behave like testSuite(formProvider(Event6), Event6, sampleMemberJourneyDataEvent6, SampleData.crystallisedDetails.amountCrystallised.setScale(2).toString(),
      fakeChangeUrl(Event6), "10.00")

    behave like testSuite(formProvider(Event8), Event8, sampleMemberJourneyDataEvent8, SampleData.lumpSumDetails.lumpSumAmount.setScale(2).toString(),
      fakeChangeUrl(Event8), "10.00")

    behave like testSuite(formProvider(Event8A), Event8A, sampleMemberJourneyDataEvent8A, SampleData.lumpSumDetails.lumpSumAmount.setScale(2).toString(),
      fakeChangeUrl(Event8A), "10.00")

    behave like testSuite(formProvider(Event22), Event22, sampleMemberJourneyDataEvent22and23(Event22), SampleData.totalPaymentAmountEvent22and23.setScale(2).toString(),
      fakeChangeUrl(Event22), "10.00")

    behave like testSuite(formProvider(Event23), Event23, sampleMemberJourneyDataEvent22and23(Event23), SampleData.totalPaymentAmountEvent22and23.setScale(2).toString(),
      fakeChangeUrl(Event23), "10.00")

    /* TODO: Temporarily disabled pagination test due to performance issues. -Pavel Vjalicin
    behave like testSuiteWithPagination(
      formProvider(Event2), Event2, cYAHref(Event2, 0), "260.00", SampleData.sampleMemberJourneyDataWithPaginationEvent2)

    behave like testSuiteWithPagination(
      formProvider(Event3), Event3, cYAHref(Event3, 0), "260.00", SampleData.event345UADataWithPagnination(Event3))

    behave like testSuiteWithPagination(
      formProvider(Event4), Event4, cYAHref(Event4, 0), "260.00", SampleData.event345UADataWithPagnination(Event4))

    behave like testSuiteWithPagination(
      formProvider(Event5), Event5, cYAHref(Event5, 0), "260.00", SampleData.event345UADataWithPagnination(Event5))

    behave like testSuiteWithPagination(
      formProvider(Event6), Event6, cYAHref(Event6, 0), "260.00", SampleData.event6UADataWithPagination)

    behave like testSuiteWithPagination(
      formProvider(Event8), Event8, cYAHref(Event8, 0), "260.00", SampleData.event8UADataWithPagination)

    behave like testSuiteWithPagination(
      formProvider(Event8A), Event8A, cYAHref(Event8A, 0), "260.00", SampleData.event8aUADataWithPagination)

    behave like testSuiteWithPagination(
      formProvider(Event22), Event22, cYAHref(Event22, 0), "260.00", SampleData.event22and23UADataWithPagination(Event22))

    behave like testSuiteWithPagination(
      formProvider(Event23), Event23, cYAHref(Event23, 0), "260.00", SampleData.event22and23UADataWithPagination(Event23))
    */
  }


  private def testSuite(form: Form[Boolean], eventType: EventType, sampleData: UserAnswers, secondValue: String, href: String, totalAmount: String): Unit = {
    testReturnOkAndCorrectView(eventType, form, sampleData, secondValue, href, totalAmount)
    testSaveAnswerAndRedirectWhenValid(eventType)
    testBadRequestForInvalidDataSubmission(eventType, form)
  }

  private def testSuiteWithPagination(form: Form[Boolean], eventType: EventType, href: String, totalAmount: String, sampleData: UserAnswers): Unit = {
    testReturnOkAndCorrectViewWithPagination(eventType, form, href, totalAmount, sampleData)
  }

  private def testReturnOkAndCorrectView(eventType: EventType, form: Form[Boolean], sampleData: UserAnswers, secondValue: String, href: String, totalAmount: String): Unit = {
    s"must return OK and the correct view for a GET for Event $eventType" in {

      val application = applicationBuilder(userAnswers = Some(sampleData)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute(eventType))

        val result = route(application, request).value

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
                    href = fakeRemoveUrl(eventType)
                  )
                )
              ))
            ))
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, eventType, expectedSeq, totalAmount, "2023")(request, messages(application)).toString
      }
    }
  }

  private def testReturnOkAndCorrectViewWithPagination(eventType: EventType, form: Form[Boolean], href: String, totalAmount: String, sampleData: UserAnswers): Unit = {
    s"must return OK and the correct view with pagination for a GET for Event $eventType" in {

      when(mockEventPaginationService.paginateMappedMembers(any(), any())).thenReturn(paginationStats26Members(href, eventType))

      val application = applicationBuilder(userAnswers = Some(sampleData)).build()

      running(application) {
        val request = FakeRequest(GET, getRouteWithPagination(eventType))

        val result = route(application, request).value

        val view = application.injector.instanceOf[MembersSummaryViewWithPagination]

        val expectedPaginationStats = PaginationStats(
          slicedMembers = fake26MappedMembers(href, eventType),
          totalNumberOfMembers = 26,
          totalNumberOfPages = 2,
          pageStartAndEnd = (1, 25),
          pagerSeq = Seq(1, 2))

        status(result) mustEqual OK

        val expectedView = view(form, waypoints, eventType, fake26MappedMembers(href, eventType), totalAmount, "2023", expectedPaginationStats, 0)(request, messages(application)).toString

        contentAsString(result) mustEqual expectedView
      }
    }
  }

  private def testSaveAnswerAndRedirectWhenValid(eventType: EventType): Unit = {
    s"must save the answer and redirect to the next page when valid data is submitted for Event $eventType" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
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
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute(eventType)).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[MembersSummaryView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, eventType, Nil, "0.00", "2023")(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never).save(any(), any(), any())(any(), any())
      }
    }
  }
}

object MembersSummaryControllerSpec extends SpecBase {

  private def fakeChangeUrl(eventType: EventType): String = {
    val directory = "/manage-pension-scheme-event-report"
    eventType match {
      case Event2 => directory + controllers.event2.routes.Event2CheckYourAnswersController.onPageLoad(0).url
      case Event3 => directory + controllers.event3.routes.Event3CheckYourAnswersController.onPageLoad(0).url
      case Event4 => directory + controllers.event4.routes.Event4CheckYourAnswersController.onPageLoad(0).url
      case Event5 => directory + controllers.event5.routes.Event5CheckYourAnswersController.onPageLoad(0).url
      case Event6 => directory + controllers.event6.routes.Event6CheckYourAnswersController.onPageLoad(0).url
      case Event8 => directory + controllers.event8.routes.Event8CheckYourAnswersController.onPageLoad(0).url
      case Event8A => directory + controllers.event8a.routes.Event8ACheckYourAnswersController.onPageLoad(0).url
      case Event22 => directory + controllers.event22.routes.Event22CheckYourAnswersController.onPageLoad(0).url
      case Event23 => directory + controllers.event23.routes.Event23CheckYourAnswersController.onPageLoad(0).url
      case _ => "Not a member event"
    }
  }

  private def fakeRemoveUrl(eventType: EventType): String = {
    val directory = "/manage-pension-scheme-event-report"
    eventType match {
      case Event2 | Event3 |
        Event4 | Event5 |
        Event6 | Event8 |
        Event8A | Event22 |
        Event23 => directory + RemoveMemberController.onPageLoad(EmptyWaypoints, eventType, 0).url
      case _ => "Not a member event used on this summary page"
    }
  }

  private def fake26MappedMembers(href: String, eventType: EventType): Seq[SummaryListRowWithTwoValues] = fakeXMappedMembers(25, href, eventType)

  private def cYAHref(eventType: EventType, index: Int) = {
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
        case _ => "Not a member event"
      }
  }

  private def fakeXMappedMembers(x: Int, href: String, eventType: EventType): Seq[SummaryListRowWithTwoValues] = for {
    i <- 1 to x
  } yield {
    SummaryListRowWithTwoValues(s"${memberDetails.fullName}", s"${memberDetails.nino}", "10.00",
      actions = Some(Actions(
        items = Seq(
          ActionItem(
            content = Text(Message("site.view")),
            href = cYAHref(eventType, i-1)
          ),
          ActionItem(
            content = Text(Message("site.remove")),
            href = fakeRemoveUrl(eventType)
          )
        )
      )))
  }

  private def fakePaginationStats(
                                   slicedMems: Seq[SummaryListRowWithTwoValues],
                                   totMems: Int,
                                   totPages: Int,
                                   pagStartEnd: (Int, Int),
                                   pagSeq: Seq[Int]
                                 ): PaginationStats = PaginationStats(
    slicedMembers = slicedMems,
    totalNumberOfMembers = totMems,
    totalNumberOfPages = totPages,
    pageStartAndEnd = pagStartEnd,
    pagerSeq = pagSeq
  )

  private def paginationStats26Members(href: String, eventType: EventType) = fakePaginationStats(
    fake26MappedMembers(href, eventType).slice(0, 24),
    26,
    2,
    (1, 25),
    1 to 2
  )
}

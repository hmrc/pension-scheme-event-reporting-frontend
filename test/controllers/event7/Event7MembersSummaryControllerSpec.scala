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

package controllers.event7

import base.SpecBase
import connectors.UserAnswersCacheConnector
import data.SampleData
import data.SampleData.{erOverviewSeq, memberDetails, sampleMemberJourneyDataEvent7}
import forms.common.MembersSummaryFormProvider
import forms.mappings.Formatters
import helpers.DateHelper
import models.enumeration.EventType.Event7
import models.enumeration.VersionStatus.Submitted
import models.{Index, VersionInfo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.common.MembersSummaryPage
import pages.{EmptyWaypoints, EventReportingOverviewPage, VersionInfoPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.EventPaginationService
import services.EventPaginationService.PaginationStatsEvent7
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import viewmodels.{Message, SummaryListRowWithThreeValues}
import views.html.event7.Event7MembersSummaryView

import scala.concurrent.Future

class Event7MembersSummaryControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar with Formatters {

  private val waypoints = EmptyWaypoints

  private val formProvider = new MembersSummaryFormProvider()
  private val formEvent7 = formProvider(Event7)

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val mockEventPaginationService = mock[EventPaginationService]
  private val mockTaxYear = mock[DateHelper]

  private def getRouteEvent7: String = routes.Event7MembersSummaryController.onPageLoad(waypoints).url

  private def postRouteEvent7: String = routes.Event7MembersSummaryController.onSubmit(waypoints).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[EventPaginationService].toInstance(mockEventPaginationService),
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector),
    bind[DateHelper].toInstance(mockTaxYear)
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
  }

  "Event7MembersSummary Controller" - {

    "Event 7" - {
      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent7
          .setOrException(VersionInfoPage, VersionInfo(3, Submitted))
          .setOrException(EventReportingOverviewPage, erOverviewSeq))).build()

        val eventPaginationService = application.injector.instanceOf[EventPaginationService]

        running(application) {
          val request = FakeRequest(GET, getRouteEvent7)

          val result = route(application, request).value

          val view = application.injector.instanceOf[Event7MembersSummaryView]

          val expectedSeq =
            Seq(
              SummaryListRowWithThreeValues(
                key = SampleData.memberDetails.fullName,
                firstValue = SampleData.memberDetails.nino,
                secondValue = decimalFormat.format(SampleData.lumpSumAmountEvent7),
                thirdValue = decimalFormat.format(SampleData.crystallisedAmountEvent7),
                actions = Some(Actions(
                  items = Seq(
                    ActionItem(
                      content = Text(Message("site.view")),
                      href = controllers.event7.routes.Event7CheckYourAnswersController.onPageLoad(0).url
                    ),
                    ActionItem(
                      content = Text(Message("site.remove")),
                      href = controllers.common.routes.RemoveMemberController.onPageLoad(waypoints, Event7, 0).url
                    )
                  )
                ))
              ))


          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formEvent7, waypoints, Event7, expectedSeq, "150.00", "2023", eventPaginationService.paginateMappedMembersThreeValues(expectedSeq, 1), Index(0), searchValue = None,
            searchHref = "/manage-pension-scheme-event-report/report/event-7-summary")(request, messages(application)).toString
        }
      }
      /* TODO: Temporarily disabled pagination test due to performance issues. -Pavel Vjalicin
      "must return OK and the correct view for a GET with pagination" in {

        when(mockEventPaginationService.paginateMappedMembersThreeValues(any(), any())).thenReturn(paginationStats26Members)

        val application = applicationBuilder(userAnswers = Some(event7UADataWithPagination)).build()

        running(application) {
          val request = FakeRequest(GET, getRouteEvent7WithPagination)

          val result = route(application, request).value

          val view = application.injector.instanceOf[Event7MembersSummaryViewWithPagination]

          val expectedPaginationStats = PaginationStatsEvent7(
            slicedMembers = fake26MappedMembers,
            totalNumberOfMembers = 26,
            totalNumberOfPages = 2,
            pageStartAndEnd = (1, 25),
            pagerSeq = Seq(1, 2))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formEvent7, waypoints, Event7, fake26MappedMembers, "3,900.00", "2023", expectedPaginationStats, 0)(request, messages(application)).toString
        }
      } */

      "must save the answer and redirect to the next page when valid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear
            .setOrException(VersionInfoPage, VersionInfo(3, Submitted))
            .setOrException(EventReportingOverviewPage, erOverviewSeq)), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent7).withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value
          val updatedAnswers = emptyUserAnswers.set(MembersSummaryPage(Event7, 1), true).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual MembersSummaryPage(Event7, 1).navigate(waypoints, emptyUserAnswersWithTaxYear, updatedAnswers).url
        }
      }

      "must return bad request when invalid data is submitted" in {
        when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(()))
        val emptyPageStats = PaginationStatsEvent7(Seq(), 0, 1, (0, 1), Seq())
        when(mockEventPaginationService.paginateMappedMembersThreeValues(any(), any())).thenReturn(emptyPageStats)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear
            .setOrException(VersionInfoPage, VersionInfo(3, Submitted))
            .setOrException(EventReportingOverviewPage, erOverviewSeq)), extraModules)
            .build()

        running(application) {
          val request =
            FakeRequest(POST, postRouteEvent7).withFormUrlEncodedBody(("value", "invalid"))

          val view = application.injector.instanceOf[Event7MembersSummaryView]
          val boundForm = formEvent7.bind(Map("value" -> "invalid"))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, waypoints, Event7, Nil, "0.00", "2023", emptyPageStats, Index(0), None, "/manage-pension-scheme-event-report/report/event-7-summary")(request, messages(application)).toString
          verify(mockUserAnswersCacheConnector, never).save(any(), any(), any())(any(), any())
        }
      }
    }
  }
}

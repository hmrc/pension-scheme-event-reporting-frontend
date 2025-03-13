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
import connectors.UserAnswersCacheConnector
import data.SampleData.{erOverviewSeq, userAnswersWithOneMemberAndEmployerEvent1}
import forms.event1.UnauthPaymentSummaryFormProvider
import models.enumeration.VersionStatus.Submitted
import models.{Index, TaxYear, VersionInfo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.event1.UnauthPaymentSummaryPage
import pages.{EmptyWaypoints, EventReportingOverviewPage, TaxYearPage, VersionInfoPage}
import play.api.Application
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.{JsPath, JsString}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.EventPaginationService
import uk.gov.hmrc.govukfrontend.views.Aliases._
import views.html.event1.UnauthPaymentSummaryView

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class UnauthPaymentSummaryControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints
  private val formProvider = new UnauthPaymentSummaryFormProvider()
  private val form = formProvider()
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val taxYear = "2023"

  private def paginationStats(application: Application, members: Seq[SummaryListRow]) = {
    val paginationService = application.injector.instanceOf[EventPaginationService]
    paginationService.paginateMappedMembers(members, 0)
  }

  private def getRoute: String = routes.UnauthPaymentSummaryController.onPageLoad(waypoints).url

  private def postRoute: String = routes.UnauthPaymentSummaryController.onSubmit(waypoints).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  private val searchValue = "xjshaiak"

  private def pageTitle(searching: Boolean = false) = if(searching) {
    Messages("unauthPaymentSummary.title.search", searchValue, taxYear)
  } else {
    Messages("unauthPaymentSummary.title", taxYear)
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
  }

  "UnauthPaymentSummary Controller" - {


    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithOneMemberAndEmployerEvent1
        .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
        .setOrException(EventReportingOverviewPage, erOverviewSeq)
        .setOrException(VersionInfoPage, VersionInfo(3, Submitted)))).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnauthPaymentSummaryView]

        val expectedSeq = Seq(SummaryListRow(
          key = Key(Text(value = "Joe Bloggs")),
          value = Value(Text(value = "857.00")),
          actions = Some(Actions(
            items = List(
              ActionItem(
                href = "/manage-pension-scheme-event-report/report/1/event-1-check-your-answers",
                content = Text(Messages("site.view")),
                visuallyHiddenText = None,
                attributes = Map()
              ),
              ActionItem(
                href = "/manage-pension-scheme-event-report/report/1/remove-event-1",
                content = Text(Messages("site.remove")),
                visuallyHiddenText = None,
                attributes = Map()
              )
            )
          ))
        ),
          SummaryListRow(
            key = Key(Text(value = "Company Name")),
            value = Value(Text(value = "7,687.00")),
            actions = Some(Actions(
              items = List(
                ActionItem(
                  href = "/manage-pension-scheme-event-report/report/2/event-1-check-your-answers",
                  content = Text(Messages("site.view")),
                  visuallyHiddenText = None,
                  attributes = Map()
                ),
                ActionItem(href = "/manage-pension-scheme-event-report/report/2/remove-event-1", content = Text(Messages("site.remove")),
                  visuallyHiddenText = None,
                  attributes = Map()
                )
              )
            ))
          ))


        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(
          form, pageTitle(), waypoints, expectedSeq, paginationStats(application, expectedSeq), Index(0), "8,544.00", taxYear,
          None, "/manage-pension-scheme-event-report/report/event-1-summary/1")(request, messages(application)).toString
      }

      Await.result(application.stop(), 10.seconds)
    }

    "must return OK and the correct view for a GET on search" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithOneMemberAndEmployerEvent1
        .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
        .setOrException(EventReportingOverviewPage, erOverviewSeq)
        .setOrException(VersionInfoPage, VersionInfo(3, Submitted)))).build()

      running(application) {
        def getRouteSearching: String = routes.UnauthPaymentSummaryController.onPageLoad(waypoints, Some(searchValue)).url

        val request = FakeRequest(GET, getRouteSearching)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnauthPaymentSummaryView]

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(
          form, pageTitle(true), waypoints, Seq(), paginationStats(application, Seq()), Index(0), "8,544.00", taxYear,
          Some(searchValue), "/manage-pension-scheme-event-report/report/event-1-summary/1")(request, messages(application)).toString
      }

      Await.result(application.stop(), 10.seconds)
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(UnauthPaymentSummaryPage, true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual UnauthPaymentSummaryPage.navigate(waypoints, emptyUserAnswers, updatedAnswers).url
      }

      Await.result(application.stop(), 10.seconds)
    }

    "must return bad request when invalid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear
          .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
          .setOrException(EventReportingOverviewPage, erOverviewSeq)
          .setOrException(VersionInfoPage, VersionInfo(1, Submitted))), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[UnauthPaymentSummaryView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result).removeAllNonces() mustEqual view(
          boundForm, pageTitle(), waypoints, Nil, paginationStats(application, Seq[SummaryListRow]()), Index(0), "0.00", taxYear,
          None, "/manage-pension-scheme-event-report/report/event-1-summary/1")(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never).save(any(), any(), any())(any(), any(), any())
      }

      Await.result(application.stop(), 10.seconds)
    }

    "must return OK and the correct view for a GET and ignore deleted members" in {

      val userAnswers = userAnswersWithOneMemberAndEmployerEvent1
        .setOrException(JsPath \ "event1" \ "membersOrEmployers" \ 0 \ "memberStatus", JsString("Deleted"))
        .setOrException(TaxYearPage, TaxYear("2022"), nonEventTypeData = true)
        .setOrException(EventReportingOverviewPage, erOverviewSeq)
        .setOrException(VersionInfoPage, VersionInfo(3, Submitted))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnauthPaymentSummaryView]

        val expectedSeq = Seq(
          SummaryListRow(
            key = Key(Text(value = "Company Name")),
            value = Value(Text(value = "7,687.00")),
            actions = Some(Actions(
              items = List(
                ActionItem(
                  href = "/manage-pension-scheme-event-report/report/2/event-1-check-your-answers",
                  content = Text(Messages("site.view")),
                  visuallyHiddenText = None,
                  attributes = Map()
                ),
                ActionItem(
                  href = "/manage-pension-scheme-event-report/report/2/remove-event-1",
                  content = Text(Messages("site.remove")),
                  visuallyHiddenText = None,
                  attributes = Map()
                )
              )
            ))
          )
        )

        status(result) mustEqual OK
        contentAsString(result).removeAllNonces() mustEqual view(
          form, pageTitle(), waypoints, expectedSeq, paginationStats(application, expectedSeq), Index(0), "7,687.00", taxYear,
          None, "/manage-pension-scheme-event-report/report/event-1-summary/1"
        )(request, messages(application)).toString
      }

      Await.result(application.stop(), 10.seconds)
    }

  }
}

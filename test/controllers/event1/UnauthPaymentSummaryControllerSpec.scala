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

package controllers.event1

import base.SpecBase
import connectors.UserAnswersCacheConnector
import data.SampleData.userAnswersWithOneMemberAndEmployerEvent1
import forms.event1.UnauthPaymentSummaryFormProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.event1.UnauthPaymentSummaryPage
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Key, SummaryListRow, Text, Value}
import views.html.event1.UnauthPaymentSummaryView

import scala.concurrent.Future

class UnauthPaymentSummaryControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private val formProvider = new UnauthPaymentSummaryFormProvider()
  private val form = formProvider()

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def getRoute: String = routes.UnauthPaymentSummaryController.onPageLoad(waypoints).url

  private def postRoute: String = routes.UnauthPaymentSummaryController.onSubmit(waypoints).url

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[UserAnswersCacheConnector].toInstance(mockUserAnswersCacheConnector)
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
  }

  "UnauthPaymentSummary Controller" - {


    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithOneMemberAndEmployerEvent1)).build()

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
                href = "/manage-pension-scheme-event-report/new-report/1/event-1-check-your-answers",
                content = Text(Messages("site.view")),
                visuallyHiddenText = None,
                attributes = Map()
              ),
              ActionItem(
                href = "/manage-pension-scheme-event-report/removeMember/1?eventType=1",
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
                  href = "/manage-pension-scheme-event-report/new-report/2/event-1-check-your-answers",
                  content = Text(Messages("site.view")),
                  visuallyHiddenText = None,
                  attributes = Map()
                ),
                ActionItem(href = "/manage-pension-scheme-event-report/removeMember/2?eventType=1", content = Text(Messages("site.remove")),
                  visuallyHiddenText = None,
                  attributes = Map()
                )
              )
            ))
          ))


        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, expectedSeq, "8,544.00")(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val updatedAnswers = emptyUserAnswers.set(UnauthPaymentSummaryPage, true).success.value

          status (result) mustEqual SEE_OTHER
          redirectLocation (result).value mustEqual UnauthPaymentSummaryPage.navigate(waypoints, emptyUserAnswers, updatedAnswers).url
      }
    }

    "must return bad request when invalid data is submitted" in {
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers), extraModules)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(("value", "invalid"))

        val view = application.injector.instanceOf[UnauthPaymentSummaryView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, Nil, "0.00")(request, messages(application)).toString
        verify(mockUserAnswersCacheConnector, never).save(any(), any(), any())(any(), any())
      }
    }
  }
}

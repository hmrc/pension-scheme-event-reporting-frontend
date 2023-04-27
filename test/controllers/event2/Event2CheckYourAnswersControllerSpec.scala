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

package controllers.event2

import base.SpecBase
import data.SampleData.sampleMemberJourneyDataEvent2
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.Messages
import play.api.inject
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases
import uk.gov.hmrc.govukfrontend.views.Aliases._
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView


class Event2CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  import Event2CheckYourAnswersControllerSpec._


  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event2.routes.Event2CheckYourAnswersController.onPageLoad(0).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, "/manage-pension-scheme-event-report/new-report/event-2-click")(request, messages(application)).toString
      }
    }

    "must return OK and the correct summary list row items for a GET (member)" in {
      val mockView = mock[CheckYourAnswersView]
      val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
        inject.bind[CheckYourAnswersView].toInstance(mockView)
      )

      val application = applicationBuilder(
        userAnswers = Some(sampleMemberJourneyDataEvent2),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any())(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event2.routes.Event2CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRowsevent2

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
      Some(Actions("", List(ActionItem(changeLink, Text("Change"),
        Some(messages(hiddenContentChangeLink)), "", Map()))))
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

  private def expectedMemberSummaryListRowsevent2(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithChange(
      "deceasedMembersDetails.checkYourAnswersLabel",
      "Joe Bloggs",
      "/manage-pension-scheme-event-report/new-report/1/event-2-deceased-member-details?waypoints=event-2-check-answers-1",
      "deceasedMembersDetails.change.hidden"

    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithChange(
      "deceasedMembersDetails.checkYourAnswersLabel.nino",
      "AA234567V",
      "/manage-pension-scheme-event-report/new-report/1/event-2-deceased-member-details?waypoints=event-2-check-answers-1",
      "deceasedMembersDetails.change.nino.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithChange(
      "beneficiaryDetails.checkYourAnswersLabel",
      "Joe Bloggs",
      "/manage-pension-scheme-event-report/new-report/1/event-2-person-who-was-paid?waypoints=event-2-check-answers-1",
      "beneficiaryDetails.change.hidden"

    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithChange(
      "beneficiaryDetails.checkYourAnswersLabel.nino",
      "AA234567V",
      "/manage-pension-scheme-event-report/new-report/1/event-2-person-who-was-paid?waypoints=event-2-check-answers-1",
      "beneficiaryDetails.change.nino.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "amountPaid.event2.checkYourAnswersLabel",
      "Joe Bloggs",
      "999.11",
      "/manage-pension-scheme-event-report/new-report/1/event-2-how-much-was-paid?waypoints=event-2-check-answers-1",
      "amountPaid.event2.change.hidden",
      "Joe Bloggs"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "datePaid.event2.checkYourAnswersLabel",
      messageInterpolation = "Joe Bloggs", htmlContent = "22 March 2022",
      changeLink = "/manage-pension-scheme-event-report/new-report/1/event-2-when-was-payment-made?waypoints=event-2-check-answers-1",
      hiddenContentChangeLink = "datePaid.event2.change.hidden"
    )
  )
}


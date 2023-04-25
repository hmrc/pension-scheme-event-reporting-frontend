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

package controllers.event8a

import base.SpecBase
import data.SampleData.sampleMemberJourneyDataEvent8A
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

class Event8ACheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  import Event8ACheckYourAnswersControllerSpec._

  "Check Your Answers Controller for Event 8A" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(sampleMemberJourneyDataEvent8A)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event8a.routes.Event8ACheckYourAnswersController.onPageLoad(0).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(expectedMemberSummaryListRowsEvent8A)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, "/manage-pension-scheme-event-report/new-report/event-8a-click")(request, messages(application)).toString
      }
    }

    "must return OK and the correct summary list row items for a GET (member)" in {
      val mockView = mock[CheckYourAnswersView]
      val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
        inject.bind[CheckYourAnswersView].toInstance(mockView)
      )

      val application = applicationBuilder(
        userAnswers = Some(sampleMemberJourneyDataEvent8A),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any())(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event8a.routes.Event8ACheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRowsEvent8A

        actual.size mustBe expected.size

        actual.zipWithIndex.map { case (a, i) =>
          a mustBe expected(i)
        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event8a.routes.Event8ACheckYourAnswersController.onPageLoad(0).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

object Event8ACheckYourAnswersControllerSpec {

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

  private def expectedMemberSummaryListRowsEvent8A(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "membersDetails.checkYourAnswersLabel",
      "Joe Bloggs",
      "/manage-pension-scheme-event-report/new-report/1/event-8a-member-details?waypoints=event-8A-check-answers-1",
      "membersDetails.change.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "membersDetails.checkYourAnswersLabel.nino",
      "AA234567V",
      "/manage-pension-scheme-event-report/new-report/1/event-8a-member-details?waypoints=event-8A-check-answers-1",
      "membersDetails.change.nino.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "paymentType.checkYourAnswersLabel",
      "Payment of a stand-alone lump sum",
      "/manage-pension-scheme-event-report/new-report/1/event-8a-lump-sum-payment-type?waypoints=event-8A-check-answers-1",
      "paymentType.change.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "event8.typeOfProtection.checkYourAnswersLabel",
      "Primary protection",
      "/manage-pension-scheme-event-report/new-report/1/event-8a-type-of-protection?waypoints=event-8A-check-answers-1",
      "event8.typeOfProtection.change.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContentWithTwoMsgKeys(
      "typeOfProtectionReference.checkYourAnswersLabel",
      "event8.typeOfProtection.primaryProtection",
      "1234567A",
      "/manage-pension-scheme-event-report/new-report/1/event-8a-protection-reference?waypoints=event-8A-check-answers-1",
      "typeOfProtectionReference.change.hidden"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "lumpSumAmountAndDate.value.checkYourAnswersLabel",
      "£223.11",
      "/manage-pension-scheme-event-report/new-report/1/event-8a-payment-details?waypoints=event-8A-check-answers-1",
      "lumpSumAmountAndDate.value.change.hidden"
    ),
    fakeSummaryListRowWithTextWithHiddenContent(
      "lumpSumAmountAndDate.date.checkYourAnswersLabel",
      "22 March 2022",
      "/manage-pension-scheme-event-report/new-report/1/event-8a-payment-details?waypoints=event-8A-check-answers-1",
      "lumpSumAmountAndDate.date.change.hidden"
    )
  )
}
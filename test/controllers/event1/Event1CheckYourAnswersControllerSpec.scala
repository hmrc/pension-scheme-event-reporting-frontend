/*
 * Copyright 2022 HM Revenue & Customs
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

/*
 * Copyright 2022 HM Revenue & Customs
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
import data.SampleData.sampleMemberJourneyData
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

class Event1CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  import Event1CheckYourAnswersControllerSpec._

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event1.routes.Event1CheckYourAnswersController.onPageLoad(0).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }

    "must return OK and the correct summary list row items for a GET" in {
      val mockView = mock[CheckYourAnswersView]
      val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
        inject.bind[CheckYourAnswersView].toInstance(mockView)
      )

      val application = applicationBuilder(
        userAnswers = Some(sampleMemberJourneyData),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture())(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event1.routes.Event1CheckYourAnswersController.onPageLoad(0).url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedMemberSummaryListRows.toSeq

        actual.size mustBe expected.size

        actual.zipWithIndex.map{ case (a, i) =>
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

  private def expectedMemberSummaryListRows(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlContent(
      "membersDetails.checkYourAnswersLabel",
      "Joe Bloggs",
      "/manage-pension-scheme-event-report/new-report/1/event-1-member-details?waypoints=event-1-check-answers-1"
    ),
    fakeSummaryListRowWithHtmlContent(
      "membersDetails.checkYourAnswersLabel.nino",
      "AA234567V",
      "/manage-pension-scheme-event-report/new-report/1/event-1-member-details?waypoints=event-1-check-answers-1"
    ),
    fakeSummaryListRowWithText(
      "doYouHoldSignedMandate.checkYourAnswersLabel",
      "No",
      "/manage-pension-scheme-event-report/new-report/1/event-1-mandate?waypoints=event-1-check-answers-1"
    ),
    fakeSummaryListRowWithText(
      "valueOfUnauthorisedPayment.checkYourAnswersLabel",
      "No",
      "/manage-pension-scheme-event-report/new-report/1/event-1-payment-value?waypoints=event-1-check-answers-1"
    ),
    fakeSummaryListRowWithHtmlContent(
      "paymentNature.checkYourAnswersLabel",
      "Benefit in kind",
      "/manage-pension-scheme-event-report/new-report/1/event-1-member-payment-nature?waypoints=event-1-check-answers-1"
    ),
    fakeSummaryListRowWithText(
      "benefitInKindBriefDescription.checkYourAnswersLabel",
      "Test description",
      "/manage-pension-scheme-event-report/new-report/1/event-1-benefit-in-kind?waypoints=event-1-check-answers-1"
    ),
    fakeSummaryListRowWithHtmlContentWithHiddenContent(
      "paymentValueAndDate.value.checkYourAnswersLabel",
      "£1,000.00",
      "/manage-pension-scheme-event-report/new-report/1/event-1-payment-details?waypoints=event-1-check-answers-1",
      "paymentValueAndDate.value.change.hidden"
    ),
    fakeSummaryListRowWithTextWithHiddenContent(
      "paymentValueAndDate.date.checkYourAnswersLabel",
      "08/11/2022",
      "/manage-pension-scheme-event-report/new-report/1/event-1-payment-details?waypoints=event-1-check-answers-1",
      "paymentValueAndDate.date.change.hidden"
    )
  )


}

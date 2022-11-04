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
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, _}
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

    "must return OK and the correct view for a GET with appropriate CYA rows" in {

      val application = applicationBuilder(userAnswers = Some(sampleMemberJourneyData)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event1.routes.Event1CheckYourAnswersController.onPageLoad(0).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(expectedSummaryListRows)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
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
  private val xx =  "/manage-pension-scheme-event-report/new-report/1/event-1-member-details?waypoints=event-1-check-answers-1"
  def expectedSummaryListRows(implicit messages: Messages): Seq[SummaryListRow] = List(
    SummaryListRow(
      Key(Text(messages("membersDetails.title")), ""), Value(HtmlContent("Joe Bloggs"), ""), "", Some(Actions("", List(ActionItem(xx, Text("Change"), Some("Member’s details"), "", Map()))))
    ),
    SummaryListRow(
      Key(Text("Member’s National Insurance number"), ""), Value(HtmlContent("AA234567V"), ""), "", Some(Actions("", List(ActionItem("/manage-pension-scheme-event-report/new-report/1/event-1-member-details?waypoints=event-1-check-answers-1", Text("Change"), Some("Member’s details"), "", Map()))))
    ),
    SummaryListRow(
      Key(Text(messages("doYouHoldSignedMandate.checkYourAnswersLabel")), ""), Value(Text("No"), ""), "", Some(Actions("", List(ActionItem("/manage-pension-scheme-event-report/new-report/1/event-1-mandate?waypoints=event-1-check-answers-1", Text("Change"), Some("Do you hold a signed mandate from the member to deduct tax from their unauthorised payment?"), "", Map()))))
    ),
    SummaryListRow(
      Key(Text("Is the value of the unauthorised payment more than 25% of the pension fund for the individual?"), ""), Value(Text("No"), ""), "", Some(Actions("", List(ActionItem("/manage-pension-scheme-event-report/new-report/1/event-1-payment-value?waypoints=event-1-check-answers-1", Text("Change"), Some("Is the value of the unauthorised payment more than 25% of the pension fund for the individual?"), "", Map()))))
    ),
    SummaryListRow(
      Key(Text("Nature of the payment or deemed payment"), ""), Value(HtmlContent("Benefit in kind"), ""), "", Some(Actions("", List(ActionItem("/manage-pension-scheme-event-report/new-report/1/event-1-member-payment-nature?waypoints=event-1-check-answers-1", Text("Change"), Some("Nature of the payment or deemed payment"), "", Map()))))
    ),
    SummaryListRow(
      Key(Text("Give a brief description of the benefit in kind"), ""), Value(Text("Test description"), ""), "", Some(Actions("", List(ActionItem("/manage-pension-scheme-event-report/new-report/1/event-1-benefit-in-kind?waypoints=event-1-check-answers-1", Text("Change"), Some("Give a brief description of the benefit in kind"), "", Map()))))
    ),
    SummaryListRow(
      Key(Text("Payment value"), ""), Value(HtmlContent("£1,000.00"), ""), "", Some(Actions("", List(ActionItem("/manage-pension-scheme-event-report/new-report/1/event-1-payment-details?waypoints=event-1-check-answers-1", Text("Change"), Some("Enter the payment value"), "", Map()))))
    ),
    SummaryListRow(
      Key(Text("Payment date"), ""), Value(Text("04/11/2022"), ""), "", Some(Actions("", List(ActionItem("/manage-pension-scheme-event-report/new-report/1/event-1-payment-details?waypoints=event-1-check-answers-1", Text("Change"), Some("Enter the date of payment or when benefit made available"), "", Map()))))
    )
  )
}

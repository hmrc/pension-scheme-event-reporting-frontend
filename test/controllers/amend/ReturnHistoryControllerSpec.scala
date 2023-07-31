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

package controllers.amend

import base.SpecBase
import controllers.amend.ReturnHistoryControllerSpec.h
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import viewmodels.ReturnHistorySummary
import views.html.amend.ReturnHistoryView

class ReturnHistoryControllerSpec extends SpecBase {

  "ReturnHistory Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, routes.ReturnHistoryController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ReturnHistoryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(h, "2021-04-06", "2022-04-05")(request, messages(application)).toString
      }
    }
  }
}

object ReturnHistoryControllerSpec {

  val h = Seq(ReturnHistorySummary(
    key = "1",
    firstValue = "SubmittedAndInProgress",
    secondValue = "submitterName",
    actions = Some(Actions(
      items = Seq(
        ActionItem(
          content = Text("View or change"),
          href = "#"
        )
      )
    ))
  ))
}

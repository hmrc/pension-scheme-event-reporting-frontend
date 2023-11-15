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

package controllers.event20A

import base.SpecBase
import data.SampleData.{sampleEvent20ABecameJourneyData, sampleEvent20ACeasedJourneyData}
import models.VersionInfo
import models.enumeration.AdministratorOrPractitioner.{Administrator, Practitioner}
import models.enumeration.VersionStatus.Compiled
import models.event20A.WhatChange
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.{EmptyWaypoints, VersionInfoPage}
import pages.event20A.{BecameDatePage, WhatChangePage}
import play.api.i18n.Messages
import play.api.inject
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.CompileService
import uk.gov.hmrc.govukfrontend.views.Aliases
import uk.gov.hmrc.govukfrontend.views.Aliases._
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView
import play.api.inject.bind

import java.time.LocalDate
import scala.concurrent.Future

class Event20ACheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {
  private val mockCompileService = mock[CompileService]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[CompileService].toInstance(mockCompileService)
  )


  import Event20ACheckYourAnswersControllerSpec._

  "Check Your Answers Controller for Event 20A" - {

    "must return OK and the correct view for a GET" in {
      val event20AUserAnswers = emptyUserAnswersWithTaxYear.set(WhatChangePage, WhatChange.BecameMasterTrust).get
        .set(BecameDatePage, LocalDate.of(2023, 1, 12)).get

      val application = applicationBuilder(userAnswers = Some(event20AUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event20A.routes.Event20ACheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(expectedSummaryListRowsEvent20ABecame)

        status(result) mustEqual OK
        request.loggedInUser.administratorOrPractitioner match {
          case Administrator =>
            contentAsString(result) mustEqual view(
              list, "/manage-pension-scheme-event-report/report/event-20a-declaration-PSA")(request, messages(application)).toString
          case Practitioner =>
            contentAsString(result) mustEqual view(
              list, "/manage-pension-scheme-event-report/report/event-20a-declaration-PSP")(request, messages(application)).toString
        }
      }
    }

    "must return OK and the correct summary list row items for a GET (Became Master Trust)" in {

      val mockView = mock[CheckYourAnswersView]
      val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
        inject.bind[CheckYourAnswersView].toInstance(mockView)
      )

      val application = applicationBuilder(
        userAnswers = Some(sampleEvent20ABecameJourneyData),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any(), any())(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event20A.routes.Event20ACheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows
        val expected: Seq[Aliases.SummaryListRow] = expectedSummaryListRowsEvent20ABecame

        actual.size mustBe expected.size

        actual.zipWithIndex.map { case (a, i) =>
          a mustBe expected(i)
        }
      }
    }

    "must return OK and the correct summary list row items for a GET (Ceased to become Master Trust)" in {

      val mockView = mock[CheckYourAnswersView]
      val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
        inject.bind[CheckYourAnswersView].toInstance(mockView)
      )

      val application = applicationBuilder(
        userAnswers = Some(sampleEvent20ACeasedJourneyData),
        extraModules = extraModules
      ).build()

      val captor: ArgumentCaptor[SummaryList] =
        ArgumentCaptor.forClass(classOf[SummaryList])

      running(application) {
        when(mockView.apply(captor.capture(), any(), any())(any(), any())).thenReturn(play.twirl.api.Html(""))
        val request = FakeRequest(GET, controllers.event20A.routes.Event20ACheckYourAnswersController.onPageLoad.url)
        val result = route(application, request).value
        status(result) mustEqual OK

        val actual: Seq[SummaryListRow] = captor.getValue.rows

        val expected: Seq[Aliases.SummaryListRow] = expectedSummaryListRowsEvent20ACeased

        actual.size mustBe expected.size

        actual.zipWithIndex.map { case (a, i) =>
          a mustBe expected(i)
        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllers.event20A.routes.Event20ACheckYourAnswersController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}

object Event20ACheckYourAnswersControllerSpec {
  private def fakeSummaryListRowWithTextWithHiddenContent(messageKey: String, text: String, changeLink: String, hiddenContentChangeLink: String)
                                                         (implicit messages: Messages): SummaryListRow = {
    SummaryListRow(
      Key(
        Text(
          messages(messageKey)
        ), ""),
      Value(Text(text), ""), "",
      Some(Actions("", List(ActionItem(changeLink, Text("Change"), Some(messages(hiddenContentChangeLink)), "", Map()))))
    )
  }

  private def fakeSummaryListRowWithHtmlWithHiddenContent(messageKey: String, text: String, changeLink: String, hiddenContentChangeLink: String)
                                                         (implicit messages: Messages): SummaryListRow = {
    SummaryListRow(
      Key(
        Text(
          messages(messageKey)
        ), ""),
      Value(HtmlContent(text), ""), "",
      Some(Actions("", List(ActionItem(changeLink, Text("Change"), Some(messages(hiddenContentChangeLink)), "", Map()))))
    )
  }

  private def expectedSummaryListRowsEvent20ABecame(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlWithHiddenContent(
      "whatChange.checkYourAnswersLabel",
      "It became a Master Trust",
      "/manage-pension-scheme-event-report/report/event-20A-Master-Trust?waypoints=event-20A-check-answers",
      "whatChange.change.hidden"
    ),
    fakeSummaryListRowWithTextWithHiddenContent(
      "becameDateMasterTrust.checkYourAnswersLabel",
      "12 January 2023",
      "/manage-pension-scheme-event-report/report/event-20A-when-scheme-became-Master-Trust?waypoints=event-20A-check-answers",
      "becameDateMasterTrust.change.hidden"
    )
  )

  private def expectedSummaryListRowsEvent20ACeased(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    fakeSummaryListRowWithHtmlWithHiddenContent(
      "whatChange.checkYourAnswersLabel",
      "It has ceased to be a Master Trust",
      "/manage-pension-scheme-event-report/report/event-20A-Master-Trust?waypoints=event-20A-check-answers",
      "whatChange.change.hidden"
    ),
    fakeSummaryListRowWithTextWithHiddenContent(
      "ceasedDateMasterTrust.checkYourAnswersLabel",
      "12 January 2023",
      "/manage-pension-scheme-event-report/report/event-20A-when-scheme-ceased-to-be-Master-Trust?waypoints=event-20A-check-answers",
      "ceasedDateMasterTrust.change.hidden"
    )
  )
}


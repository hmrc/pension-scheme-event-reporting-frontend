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
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import controllers.amend.ReturnHistoryControllerSpec.{seqOfReturnHistorySummary, versionsWithSubmitter}
import models.VersionInfo
import models.amend.VersionsWithSubmitter
import models.enumeration.VersionStatus.Submitted
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.EmptyWaypoints
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import viewmodels.ReturnHistorySummary
import views.html.amend.ReturnHistoryView

import java.time.{LocalDate, Month}
import scala.concurrent.Future

class ReturnHistoryControllerSpec extends SpecBase with BeforeAndAfterEach {

  val mockErConnector: EventReportingConnector = mock[EventReportingConnector]
  val mockUACacheConnector: UserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private val extraModules: Seq[GuiceableModule] = Seq[GuiceableModule](
    bind[EventReportingConnector].toInstance(mockErConnector),
    bind[UserAnswersCacheConnector].toInstance(mockUACacheConnector)
  )

  override def beforeEach(): Unit = {
    reset(mockErConnector)
    reset(mockUACacheConnector)
  }

  "ReturnHistory Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()

      when(mockErConnector.getListOfVersions(ArgumentMatchers.eq("87219363YN"), ArgumentMatchers.eq("2022-04-06"))(any()))
        .thenReturn(Future.successful(Seq(versionsWithSubmitter)))

      running(application) {
        val request = FakeRequest(GET, routes.ReturnHistoryController.onPageLoad().url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[ReturnHistoryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view.render(seqOfReturnHistorySummary,
          taxYearStart = "2022",
          taxYearEnd = "2023",
          schemeName = "schemeName",
          request = request,
          messages = messages(application)).toString
      }
    }

    "must redirect to the correct page and save correct version in mongodb onClick " in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersWithTaxYear), extraModules).build()

      when(mockUACacheConnector.save(any(), any())(any(), any())).thenReturn(Future.successful())

      running(application) {
        val request = FakeRequest(GET, routes.ReturnHistoryController.onClick(EmptyWaypoints, "1").url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url
        verify(mockUACacheConnector, times(1)).save(any(), any())(any(), any())
      }
    }
  }
}

object ReturnHistoryControllerSpec {

  private val dayOfMonth: Int = 9
  private val dateYear: Int = 2022
  private val versionNo: Int = 1

  val versionsWithSubmitter: VersionsWithSubmitter =
    VersionsWithSubmitter(VersionInfo(versionNo, Submitted), Some("John Smith"), LocalDate.of(dateYear, Month.JUNE, dayOfMonth))

  val seqOfReturnHistorySummary: Seq[ReturnHistorySummary] = Seq(ReturnHistorySummary(
    key = "1",
    firstValue = "Submitted on 09 June 2022",
    secondValue = "John Smith",
    actions = Some(Actions(
      items = Seq(
        ActionItem(
          content = Text("View or change"),
          href = controllers.amend.routes.ReturnHistoryController.onClick(EmptyWaypoints, "1").url
        )
      )
    ))
  ))
}

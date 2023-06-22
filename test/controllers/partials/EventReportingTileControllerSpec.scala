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

package controllers.partials

import base.SpecBase
import config.FrontendAppConfig
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import models.{EROverview, ToggleDetails}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.partials.{CardSubHeading, CardSubHeadingParam, CardViewModel, Link}
import views.html.partials.EventReportingTileView

import java.time.LocalDate
import scala.concurrent.Future

class EventReportingTileControllerSpec extends SpecBase with BeforeAndAfterEach with MockitoSugar {

  private val mockConnector = mock[EventReportingConnector]
  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  def appNoTaxYear: Application = applicationBuilder()
    .bindings(
      bind[EventReportingConnector].to(mockConnector),
      bind[UserAnswersCacheConnector].to(mockUserAnswersCacheConnector)
    )
    .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAnswersCacheConnector)
  }

  "Event Reporting Tile Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockConnector.getFeatureToggle(any())(any(), any())).thenReturn(
        Future.successful(ToggleDetails("event-reporting", None, isEnabled = true))
      )

      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      when(mockConnector.getOverview(any(), any(), any(), any())(any(), any())).thenReturn(
        Future.successful(Seq(EROverview(
          LocalDate.now(),
          LocalDate.now().plusYears(1),
          tpssReportPresent = false,
          None
        )))
      )


      val application = appNoTaxYear

      val view = application.injector.instanceOf[EventReportingTileView]
      val appConfig = application.injector.instanceOf[FrontendAppConfig]

      running(application) {
        val request = FakeRequest(GET, controllers.partials.routes.EventReportingTileController.eventReportPartial().url)

        val result = route(application, request).value

        val card = Seq(
          CardViewModel(
            id = "new",
            heading = Messages("eventReportingTile.heading"),
            subHeadings = Seq(CardSubHeading(subHeading = "", subHeadingClasses = "")),
            links = Seq(Link("erLoginLink", appConfig.erLoginUrl, Text(Messages("eventReportingTile.link.new"))))
          )
        )
        status(result) mustEqual OK
        
        contentAsString(result).filterNot(_.isWhitespace) mustEqual view(card)(request, messages(application)).toString.filterNot(_.isWhitespace)
      }
    }

    "must return empty html if feature toggle is disabled" in {
      when(mockConnector.getFeatureToggle(any())(any(), any())).thenReturn(
        Future.successful(ToggleDetails("event-reporting", None, isEnabled = false))
      )

      when(mockUserAnswersCacheConnector.save(any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      when(mockConnector.getOverview(any(), any(), any(), any())(any(), any())).thenReturn(
        Future.successful(Seq(EROverview(
          LocalDate.now(),
          LocalDate.now().plusYears(1),
          tpssReportPresent = false,
          None
        )))
      )

      val application = appNoTaxYear

      running(application) {
        val request = FakeRequest(GET, controllers.partials.routes.EventReportingTileController.eventReportPartial().url)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual ""
      }
    }
  }
}


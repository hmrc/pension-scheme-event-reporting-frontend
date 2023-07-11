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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import models.FileUploadOutcomeStatus.{FAILURE, IN_PROGRESS, SUCCESS}
import models.enumeration.{Enumerable, EventType}
import models.{EROverview, EROverviewVersion, EventDataIdentifier, FileUploadOutcomeResponse, TaxYear, UserAnswers}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.api.libs.json.{JsArray, Json}
import uk.gov.hmrc.http._
import utils.WireMockHelper

import java.time.LocalDate

class EventReportingConnectorSpec
  extends AsyncWordSpec
    with Matchers
    with WireMockHelper
    with Enumerable.Implicits {

  private val pstr = "87219363YN"
  private val eventType: EventType = EventType.Event1
  private val eventType2: EventType = EventType.Event2
  private val referenceStub: String = "123"
  private val reportVersion: String = "reportVersion"
  private val userAnswers = UserAnswers()

  private val validResponse = Seq(
    eventType, eventType2
  )

  import EventType.enumerable

  private val validResponseJson = Json.arr(
    eventType, eventType2
  )

  private implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  override protected def portConfigKey: String = "microservice.services.pension-scheme-event-reporting.port"

  private lazy val connector: EventReportingConnector = injector.instanceOf[EventReportingConnector]
  private val eventReportSummaryCacheUrl = "/pension-scheme-event-reporting/event-summary"
  private val eventReportCompileUrl = "/pension-scheme-event-reporting/compile"
  private val eventReportSubmitUrl = "/pension-scheme-event-reporting/submit-event-declaration-report"

  private def event20AReportSubmitUrl = "/pension-scheme-event-reporting/submit-event20a-declaration-report"

  private val getFileUploadResponseUrl = "/pension-scheme-event-reporting/file-upload-response/get"
  private val getOverviewUrl = "/pension-scheme-event-reporting/overview"

  private val failureOutcome = FileUploadOutcomeResponse(fileName = None, FAILURE, None, referenceStub, None)
  private val failureOutcomeJson = Json.obj("fileStatus" -> "ERROR")
  private val successOutcome = FileUploadOutcomeResponse(fileName = Some("test"), SUCCESS, Some("downloadUrl"), referenceStub, Some(100L))
  private val successOutcomeJson = Json.obj(
    "fileStatus" -> "READY",
    "downloadUrl" -> "downloadUrl",
    "uploadDetails" -> Json.obj(
      "fileName" -> "test",
      "size" -> 100L
    )
  )
  private val inProgressOutcome = FileUploadOutcomeResponse(fileName = None, IN_PROGRESS, None, referenceStub, None)

  "getEventReportSummary" must {
    "return successfully when the backend has returned OK and a correct response" in {
      server.stubFor(
        get(urlEqualTo(eventReportSummaryCacheUrl))
          .willReturn(
            ok
              .withHeader("Content-Type", "application/json")
              .withBody(validResponseJson.toString())
          )
      )

      connector.getEventReportSummary(pstr, "21/01/22") map { response =>
        response mustBe validResponse
      }
    }

    "return successfully when the backend has returned NOT FOUND and a correct response" in {
      server.stubFor(
        get(urlEqualTo(eventReportSummaryCacheUrl))
          .willReturn(
            notFound
              .withHeader("Content-Type", "application/json")
          )
      )

      connector.getEventReportSummary(pstr, "21/01/22") map {
        _ mustBe Nil
      }
    }

    "return BadRequestException when the backend has returned bad request response" in {
      server.stubFor(
        get(urlEqualTo(eventReportSummaryCacheUrl))
          .willReturn(
            badRequest
              .withHeader("Content-Type", "application/json")
          )
      )

      recoverToSucceededIf[HttpException] {
        connector.getEventReportSummary(pstr, "21/01/22")
      }
    }
  }

  "compileEvent" must {
    "return unit for successful post" in {
      server.stubFor(
        post(urlEqualTo(eventReportCompileUrl))
          .willReturn(
            noContent
          )
      )
      connector.compileEvent(pstr, EventDataIdentifier(eventType, "2020", "1")).map {
        _ mustBe()
      }
    }

    "return BadRequestException when the backend has returned bad request response" in {
      server.stubFor(
        post(urlEqualTo(eventReportCompileUrl))
          .willReturn(
            badRequest
              .withHeader("Content-Type", "application/json")
          )
      )

      recoverToSucceededIf[HttpException] {
        connector.compileEvent(pstr, EventDataIdentifier(eventType, "2020", "1"))
      }
    }
  }

  "submitReport" must {
    "return unit for successful post" in {
      server.stubFor(
        post(urlEqualTo(eventReportSubmitUrl))
          .willReturn(
            noContent
          )
      )
      connector.submitReport(pstr, userAnswers, reportVersion).map {
        _ mustBe()
      }
    }

    "return BadRequestException when the backend has returned bad request response" in {
      server.stubFor(
        post(urlEqualTo(eventReportSubmitUrl))
          .willReturn(
            badRequest
              .withHeader("Content-Type", "application/json")
          )
      )

      recoverToSucceededIf[HttpException] {
        connector.submitReport(pstr, userAnswers, reportVersion)
      }
    }
  }

  "submitReportEvent20A" must {
    "return unit for successful post" in {
      server.stubFor(
        post(urlEqualTo(event20AReportSubmitUrl))
          .willReturn(
            noContent
          )
      )
      connector.submitReportEvent20A(pstr, userAnswers, reportVersion).map {
        _ mustBe()
      }
    }

    "return BadRequestException when the backend has returned bad request response" in {
      server.stubFor(
        post(urlEqualTo(event20AReportSubmitUrl))
          .willReturn(
            badRequest
              .withHeader("Content-Type", "application/json")
          )
      )

      recoverToSucceededIf[HttpException] {
        connector.submitReportEvent20A(pstr, userAnswers, reportVersion)
      }
    }
  }

  "getFileUploadOutcome" must {

    "return data if data is present in the collection" in {
      server.stubFor(
        get(urlEqualTo(getFileUploadResponseUrl))
          .willReturn(
            ok
              .withBody(successOutcomeJson.toString)
              .withHeader("reference", referenceStub)
          )
      )

      connector.getFileUploadOutcome(referenceStub) map {
        result =>
          result mustEqual successOutcome
      }
    }

    "return no data and a failure if data is not found in collection" in {
      server.stubFor(
        get(urlEqualTo(getFileUploadResponseUrl))
          .willReturn(
            ok
              .withBody(failureOutcomeJson.toString)
              .withHeader("reference", referenceStub)
          )
      )

      connector.getFileUploadOutcome(referenceStub) map {
        result =>
          result mustEqual failureOutcome
      }
    }

    "return return not found if data hasn't been returned" in {
      server.stubFor(
        get(urlEqualTo(getFileUploadResponseUrl))
          .willReturn(
            notFound()
          )
      )

      connector.getFileUploadOutcome(referenceStub) map {
        result =>
          result mustEqual inProgressOutcome
      }
    }
  }

  "getOverview" must {
    "return the seq of overviewDetails returned from BE" in {
      val erOverviewResponseJson: JsArray = Json.arr(
        Json.obj(
          "periodStartDate" -> "2022-04-06",
          "periodEndDate" -> "2023-04-05",
          "versionDetails" -> Json.obj(
            "numberOfVersions" -> 3,
            "submittedVersionAvailable" -> false,
            "compiledVersionAvailable" -> true
          )
        ),
        Json.obj(
          "periodStartDate" -> "2022-04-06",
          "periodEndDate" -> "2023-04-05",
          "versionDetails" -> Json.obj(
            "numberOfVersions" -> 2,
            "submittedVersionAvailable" -> true,
            "compiledVersionAvailable" -> true
          )
        )
      )

      val overview1 = EROverview(
        LocalDate.of(2022, 4, 6),
        LocalDate.of(2023, 4, 5),
        TaxYear("2022"),
        tpssReportPresent = false,
        Some(EROverviewVersion(
          3,
          submittedVersionAvailable = false,
          compiledVersionAvailable = true)))

      val overview2 = EROverview(
        LocalDate.of(2022, 4, 6),
        LocalDate.of(2023, 4, 5),
        TaxYear("2022"),
        tpssReportPresent = false,
        Some(EROverviewVersion(
          2,
          submittedVersionAvailable = true,
          compiledVersionAvailable = true)))

      val erOverview = Seq(overview1, overview2)


      server.stubFor(
        get(urlEqualTo(getOverviewUrl))
          .willReturn(
            ok
              .withHeader("Content-Type", "application/json")
              .withBody(erOverviewResponseJson.toString())
          )
      )
      connector.getOverview(pstr, "ER", "2022-04-06", "2023-04-05").map { response =>
        response mustBe erOverview
      }
    }
  }
}

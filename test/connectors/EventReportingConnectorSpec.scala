/*
 * Copyright 2024 HM Revenue & Customs
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
import models.amend.VersionsWithSubmitter
import models.enumeration.AdministratorOrPractitioner.Administrator
import models.enumeration.EventType.{Event1, Event2}
import models.enumeration.VersionStatus.Submitted
import models.enumeration.{Enumerable, EventType}
import models.requests.DataRequest
import models.{EROverview, EROverviewVersion, EventDataIdentifier, EventSummary, FileUploadOutcomeResponse, LoggedInUser, TaxYear, UserAnswers, VersionInfo}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.api.libs.json.{JsArray, JsResultException, Json}
import play.api.mvc.AnyContent
import play.api.mvc.Results.{BadRequest, NoContent}
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
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
  private val referenceStub: String = "123"
  private val reportVersion: String = "reportVersion"
  private val userAnswers = UserAnswers()

  private implicit val dataRequest: DataRequest[AnyContent] =
    DataRequest("Pstr123", "SchemeABC", "returnUrl", FakeRequest(GET, "/"), LoggedInUser("user", Administrator, "psaId"), UserAnswers(), "S2400000041")

  private val validResponse = Seq(
    EventSummary(Event1, 2, None),
    EventSummary(Event2, 1, None)
  )

  private val validResponseJson = Json.arr(
    Json.obj("eventType" -> "1", "recordVersion" -> 2),
    Json.obj("eventType" -> "2", "recordVersion" -> 1)
  )
  private val srn = "S2400000041"
  private implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  override protected def portConfigKey: String = "microservice.services.pension-scheme-event-reporting.port"

  private lazy val connector: EventReportingConnector = injector.instanceOf[EventReportingConnector]
  private val eventReportSummaryCacheUrl = s"/pension-scheme-event-reporting/event-summary/$srn"
  private val eventReportCompileUrl = s"/pension-scheme-event-reporting/compile/$srn"
  private val eventReportSubmitUrl = s"/pension-scheme-event-reporting/submit-event-declaration-report/$srn"
  private val deleteMemberUrl = s"/pension-scheme-event-reporting/delete-member/$srn"

  private def event20AReportSubmitUrl = s"/pension-scheme-event-reporting/submit-event20a-declaration-report/$srn"

  private val getFileUploadResponseUrl = s"/pension-scheme-event-reporting/file-upload-response/get/$srn"
  private val getOverviewUrl = s"/pension-scheme-event-reporting/overview/$srn"
  private val getVersionUrl = s"/pension-scheme-event-reporting/versions/$srn"

  private val failureOutcome = FileUploadOutcomeResponse(fileName = None, FAILURE, None, referenceStub, None)
  private val failureOutcomeJson = Json.obj("fileStatus" -> "ERROR")
  private val successOutcome = FileUploadOutcomeResponse(
    fileName = Some("test"), SUCCESS, Some("downloadUrl"), referenceStub, Some(100L))
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

      connector.getEventReportSummary(pstr, "21/01/22", 1) map { response =>
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

      connector.getEventReportSummary(pstr, "21/01/22", 1) map {
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
        connector.getEventReportSummary(pstr, "21/01/22", 1)
      }
    }

      "return an empty list if the backend returns an empty JSON array" in {
        server.stubFor(
          get(urlEqualTo(eventReportSummaryCacheUrl))
            .willReturn(
              ok
                .withHeader("Content-Type", "application/json")
                .withBody(Json.arr().toString())
            )
        )

        connector.getEventReportSummary(pstr, "21/01/22", 1) map { response =>
          response mustBe Nil
        }
      }

      "throw a RuntimeException for unexpected response structure" in {
        server.stubFor(
          get(urlEqualTo(eventReportSummaryCacheUrl))
            .willReturn(
              ok
                .withHeader("Content-Type", "application/json")
                .withBody("""{"unexpectedKey": "unexpectedValue"}""")
            )
        )

        recoverToSucceededIf[RuntimeException] {
          connector.getEventReportSummary(pstr, "21/01/22", 1)
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
      connector.compileEvent(pstr, EventDataIdentifier(eventType, "2020", "1"), 1).map {
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
        connector.compileEvent(pstr, EventDataIdentifier(eventType, "2020", "1"), 1)
      }
    }
  }

  "deleteMember" must {
    "return unit for successful post" in {
      server.stubFor(
        post(urlEqualTo(deleteMemberUrl))
          .willReturn(
            noContent
          )
      )
      connector.deleteMember(pstr, EventDataIdentifier(eventType, "2020", "1"), 0, "0").map {
        _ mustBe()
      }
    }

    "return BadRequestException when the backend has returned bad request response" in {
      server.stubFor(
        post(urlEqualTo(deleteMemberUrl))
          .willReturn(
            badRequest
              .withHeader("Content-Type", "application/json")
          )
      )

      recoverToSucceededIf[HttpException] {
        connector.deleteMember(pstr, EventDataIdentifier(eventType, "2020", "1"), 0, "0")
      }
    }

    "return HttpException for a 500 internal server error response" in {
      server.stubFor(
        post(urlEqualTo(eventReportCompileUrl))
          .willReturn(
            serverError()
          )
      )

      recoverToSucceededIf[HttpException] {
        connector.compileEvent(pstr, EventDataIdentifier(eventType, "2020", "1"), 1)
      }
    }

    "return HttpException when the backend responds with a 404 not found" in {
      server.stubFor(
        post(urlEqualTo(deleteMemberUrl))
          .willReturn(
            notFound()
          )
      )

      recoverToSucceededIf[HttpException] {
        connector.deleteMember(pstr, EventDataIdentifier(eventType, "2020", "1"), 0, "0")
      }
    }
  }

  "submitReport" must {
    "return NoContent for successful post" in {
      server.stubFor(
        post(urlEqualTo(eventReportSubmitUrl))
          .willReturn(
            noContent
          )
      )
      connector.submitReport(pstr, userAnswers, reportVersion).map {
        _ mustBe NoContent
      }
    }

    "return BadRequest when the backend has returned bad request response" in {
      server.stubFor(
        post(urlEqualTo(eventReportSubmitUrl))
          .willReturn(
            badRequest
              .withHeader("Content-Type", "application/json")
          )
      )

      connector.submitReport(pstr, userAnswers, reportVersion).map {
        _ mustBe BadRequest
      }
    }

    "return HttpException when the backend has returned server error" in {
      server.stubFor(
        post(urlEqualTo(eventReportSubmitUrl))
          .willReturn(
            serverError
              .withHeader("Content-Type", "application/json")
          )
      )

      recoverToSucceededIf[HttpException] {
        connector.submitReport(pstr, userAnswers, reportVersion)
      }
    }
  }

  "submitReportEvent20A" must {
    "return NoContent for successful post" in {
      server.stubFor(
        post(urlEqualTo(event20AReportSubmitUrl))
          .willReturn(
            noContent
          )
      )
      connector.submitReportEvent20A(pstr, userAnswers, reportVersion).map {
        _ mustBe NoContent
      }
    }

    "return BadRequest when the backend has returned bad request response" in {
      server.stubFor(
        post(urlEqualTo(event20AReportSubmitUrl))
          .willReturn(
            badRequest
              .withHeader("Content-Type", "application/json")
          )
      )

      connector.submitReportEvent20A(pstr, userAnswers, reportVersion).map {
        _ mustBe BadRequest
      }
    }

    "return HttpException when the backend has returned server error" in {
      server.stubFor(
        post(urlEqualTo(event20AReportSubmitUrl))
          .willReturn(
            serverError
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

    "return BadRequestException when the backend has returned bad request response" in {
      server.stubFor(
        get(urlEqualTo(getFileUploadResponseUrl))
          .willReturn(
            badRequest
              .withHeader("Content-Type", "application/json")
          )
      )

      recoverToSucceededIf[HttpException] {
        connector.getFileUploadOutcome(referenceStub) map {
          result =>
            result mustEqual successOutcome
        }
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

    "return JsResultException when the backend has returned errors" in {
      val erOverviewResponseJson: JsArray = Json.arr(
        Json.obj(
          "periodStartDate" -> "2022-04-06",
          "versionDetails" -> Json.obj(
            "numberOfVersions" -> 3,
            "submittedVersionAvailable" -> false,
            "compiledVersionAvailable" -> true
          )
        )
      )

      server.stubFor(
        get(urlEqualTo(getOverviewUrl))
          .willReturn(
            ok
              .withHeader("Content-Type", "application/json")
              .withBody(erOverviewResponseJson.toString())
          )
      )

      recoverToSucceededIf[JsResultException] {
        connector.getOverview(pstr, "ER", "2022-04-06", "2023-04-05")
      }
    }

    "return BadRequestException when the backend has returned bad request response" in {
      server.stubFor(
        get(urlEqualTo(getOverviewUrl))
          .willReturn(
            badRequest
              .withHeader("Content-Type", "application/json")
          )
      )

      recoverToSucceededIf[HttpException] {
        connector.getOverview(pstr, "ER", "2022-04-06", "2023-04-05")
      }
    }
  }

  "getListOfVersions" must {
    "return the seq of VersionsWithSubmitter returned from BE" in {

      val versionWithSubmitterResponse =
        Seq(VersionsWithSubmitter(VersionInfo(1, Submitted), Some("John Smith"), LocalDate.of(2022, 6, 9)))

      val versionWithSubmitterResponseJson = {
        Json.arr(
          Json.obj("versionDetails" -> Json.obj(
            "version" -> 1,
            "status" -> "submitted"),
            "submitterName" -> "John Smith",
            "submittedDate" -> "2022-06-09")
        )
      }

      server.stubFor(
        get(urlEqualTo(getVersionUrl))
          .willReturn(
            ok
              .withHeader("Content-Type", "application/json")
              .withBody(versionWithSubmitterResponseJson.toString())
          )
      )
      connector.getListOfVersions(pstr, "2022-04-06").map { response =>
        response mustBe versionWithSubmitterResponse
      }

    }

    "return not found if data hasn't been returned" in {
      server.stubFor(
        get(urlEqualTo(getVersionUrl))
          .willReturn(
            notFound
          )
      )

      connector.getListOfVersions(pstr, "2022-04-06") map {
        result =>
          result mustEqual Nil
      }
    }

    "return an empty sequence if the backend responds with an empty JSON array" in {
      server.stubFor(
        get(urlEqualTo(getOverviewUrl))
          .willReturn(
            ok
              .withHeader("Content-Type", "application/json")
              .withBody(Json.arr().toString())
          )
      )

      connector.getOverview(pstr, "ER", "2022-04-06", "2023-04-05") map { response =>
        response mustBe Seq.empty
      }
    }

    "throw an HttpException for a 401 unauthorized response" in {
      server.stubFor(
        get(urlEqualTo(getOverviewUrl))
          .willReturn(
            unauthorized()
          )
      )

      recoverToSucceededIf[HttpException] {
        connector.getOverview(pstr, "ER", "2022-04-06", "2023-04-05")
      }
    }
  }
}

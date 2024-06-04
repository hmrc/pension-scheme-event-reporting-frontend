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

///*
// * Copyright 2024 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package services
//
//import org.scalatest.concurrent.ScalaFutures
//import org.scalatest.flatspec.AsyncFlatSpec
//import org.scalatest.matchers.should.Matchers
//import org.scalatestplus.mockito.MockitoSugar
//import org.mockito.ArgumentMatchers.{any, eq => meq}
//import org.mockito.Mockito._
//import org.scalatest.BeforeAndAfterEach
//import org.scalatest.OptionValues
//import uk.gov.hmrc.http.HeaderCarrier
//import app.RoutesPrefix
//import base.SpecBase
//
//import scala.concurrent.{ExecutionContext, Future}
//import config.FrontendAppConfig
//import connectors.UserAnswersCacheConnector
//import models._
//import models.enumeration.JourneyStartType.{InProgress, PastEventTypes}
//import models.enumeration.VersionStatus.Compiled
//import pages.{EventReportingOverviewPage, EventReportingTileLinksPage, TaxYearPage, VersionInfoPage}
//import controllers._
//
//import java.time.LocalDate
//
//class EventReportingOverviewServiceSpec extends AsyncFlatSpec with Matchers with MockitoSugar with ScalaFutures with BeforeAndAfterEach with OptionValues {
//
//  implicit val hc: HeaderCarrier = HeaderCarrier()
//  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
//
//  val mockUserAnswersCacheConnector: UserAnswersCacheConnector = mock[UserAnswersCacheConnector]
//  val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
//
//  var service: EventReportingOverviewService = _
//
//  override def beforeEach(): Unit = {
//    reset(mockUserAnswersCacheConnector, mockConfig)
//    service = new EventReportingOverviewService(mockUserAnswersCacheConnector, mockConfig)
//  }
//
//  "getInProgressYearAndUrl" should "return the correct year and URL when there is one compiled version" in {
//    val userAnswers = mock[UserAnswers]
//    val pstr = "testPstr"
//    val versionDetails = EROverviewVersion(numberOfVersions = 1, compiledVersionAvailable = true, submittedVersionAvailable = false)
//    val erOverview = EROverview(
//      LocalDate.of(2021, 4, 6),
//      LocalDate.of(2022, 4, 5),
//      TaxYear(2021.toString), false, Some(versionDetails))
//    val eventReportingOverviewPageData = Seq(erOverview)
//
//    when(mockUserAnswersCacheConnector.get(meq(pstr))(any(), any()))
//      .thenReturn(Future.successful(Some(userAnswers)))
//    when(userAnswers.get(EventReportingOverviewPage))
//      .thenReturn(Some(eventReportingOverviewPageData))
//    when(userAnswers.setOrException(any(), any(), any())(any())).thenReturn(userAnswers)
//    when(mockUserAnswersCacheConnector.save(any(), any())(any(), any())).thenReturn(Future.successful(userAnswers))
//
//    val result = service.getInProgressYearAndUrl(userAnswers, pstr)
//
//    result.futureValue shouldBe Seq(("2022 to 2023", controllers.routes.EventReportingOverviewController.onSubmit(2022, "InProgress").url))
//  }
//
//  "getInProgressYearAndUrl" should "return the correct years and URLs when there are multiple compiled versions" in {
//    val userAnswers = mock[UserAnswers]
//    val pstr = "testPstr"
//    val versionDetails = EROverviewVersion(numberOfVersions = 1, compiledVersionAvailable = true, submittedVersionAvailable = false)
//    val erOverview1 = EROverview(TaxYear(2022), Some(versionDetails))
//    val erOverview2 = EROverview(
//      LocalDate.of(2021, 4, 6),
//      LocalDate.of(2022, 4, 5),
//      TaxYear(2023.toString), false, Some(versionDetails))
//    val eventReportingOverviewPageData = Seq(erOverview1, erOverview2)
//
//    when(mockUserAnswersCacheConnector.get(meq(pstr))(any(), any()))
//      .thenReturn(Future.successful(Some(userAnswers)))
//    when(userAnswers.get(EventReportingOverviewPage))
//      .thenReturn(Some(eventReportingOverviewPageData))
//    when(userAnswers.setOrException(any(), any(), any())(any())).thenReturn(userAnswers)
//    when(mockUserAnswersCacheConnector.save(any(), any())(any(), any())).thenReturn(Future.successful(userAnswers))
//
//    val result = service.getInProgressYearAndUrl(userAnswers, pstr)
//
//    result.futureValue shouldBe Seq(
//      ("2022 to 2023", controllers.routes.EventReportingOverviewController.onSubmit(2022, "InProgress").url),
//      ("2023 to 2024", controllers.routes.EventReportingOverviewController.onSubmit(2023, "InProgress").url)
//    )
//  }
//
//  "getPastYearsAndUrl" should "return the correct past years and URLs" in {
//    val userAnswers = mock[UserAnswers]
//    val pstr = "testPstr"
//    val versionDetails = EROverviewVersion(numberOfVersions = 1, compiledVersionAvailable = true, submittedVersionAvailable = true)
//    val erOverview = EROverview(
//      LocalDate.of(2021, 4, 6),
//      LocalDate.of(2022, 4, 5),
//      TaxYear(2022.toString), true,
//      Some(versionDetails))
//    val eventReportingOverviewPageData = Seq(erOverview)
//
//    when(mockUserAnswersCacheConnector.get(meq(pstr))(any(), any()))
//      .thenReturn(Future.successful(Some(userAnswers)))
//    when(userAnswers.get(EventReportingOverviewPage))
//      .thenReturn(Some(eventReportingOverviewPageData))
//    when(userAnswers.setOrException(any(), any(), any())(any())).thenReturn(userAnswers)
//    when(mockUserAnswersCacheConnector.save(any(), any())(any(), any())).thenReturn(Future.successful(userAnswers))
//
//    val result = service.getPastYearsAndUrl(userAnswers, pstr)
//
//    result.futureValue shouldBe Seq(("2022 to 2023", controllers.routes.EventReportingOverviewController.onSubmit(2022, "PastEventTypes").url))
//  }
//
//  "getPastYearsAndUrl" should "return an empty list if no past years are available" in {
//    val userAnswers = mock[UserAnswers]
//    val pstr = "testPstr"
//
//    when(mockUserAnswersCacheConnector.get(meq(pstr))(any()))
//      .thenReturn(Future.successful(Some(userAnswers)))
//    when(userAnswers.get(EventReportingOverviewPage))
//      .thenReturn(None)
//
//    val result = service.getPastYearsAndUrl(userAnswers, pstr)
//
//    result.futureValue shouldBe Seq.empty
//  }
//
//  "getTaxYears" should "return tax years with submitted versions available" in {
//    val userAnswers = mock[UserAnswers]
//    val versionDetails = EROverviewVersion(numberOfVersions = 1, compiledVersionAvailable = true, submittedVersionAvailable = true)
//    val erOverview = EROverview(
//      LocalDate.of(2021, 4, 6),
//      LocalDate.of(2022, 4, 5),
//      TaxYear(2022.toString), true,
//      Some(versionDetails))
//    val eventReportingOverviewPageData = Seq(erOverview)
//
//    when(userAnswers.get(EventReportingTileLinksPage))
//      .thenReturn(Some(PastEventTypes))
//    when(userAnswers.get(EventReportingOverviewPage))
//      .thenReturn(Some(eventReportingOverviewPageData))
//
//    val result = service.getTaxYears(userAnswers)
//
//    result shouldBe Seq(TaxYear(2022.toString))
//  }
//
//  "getTaxYears" should "return tax years with compiled versions available" in {
//    val userAnswers = mock[UserAnswers]
//    val versionDetails = EROverviewVersion(numberOfVersions = 1, compiledVersionAvailable = true, submittedVersionAvailable = true)
//    val erOverview = EROverview(
//      LocalDate.of(2021, 4, 6),
//      LocalDate.of(2022, 4, 5),
//      TaxYear(2022.toString), true,
//      Some(versionDetails))
//    val eventReportingOverviewPageData = Seq(erOverview)
//
//    when(userAnswers.get(EventReportingTileLinksPage))
//      .thenReturn(Some(InProgress))
//    when(userAnswers.get(EventReportingOverviewPage))
//      .thenReturn(Some(eventReportingOverviewPageData))
//
//    val result = service.getTaxYears(userAnswers)
//
//    result shouldBe Seq(TaxYear(2022.toString))
//  }
//}
//

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

package controllers.actions

import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.SessionDataCacheConnector
import models.LoggedInUser
import models.enumeration.AdministratorOrPractitioner
import models.enumeration.AdministratorOrPractitioner.{Administrator, Practitioner}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{Action, AnyContent, BodyParsers, Call}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class IdentifierActionSpec
  extends SpecBase with BeforeAndAfterEach with GuiceOneAppPerSuite with MockitoSugar {

  private class FakeFailingAuthConnector @Inject()(exceptionToReturn: Throwable) extends AuthConnector {
    val serviceUrl: String = ""

    override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
      Future.failed(exceptionToReturn)

  }

  private val mockSessionDataCacheConnector = mock[SessionDataCacheConnector]
  val dummyCall: Call = Call("GET", "/foo")

  private val mockFrontendAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction {
      implicit request =>
        Ok(Json.obj(
          "loggedInUser" -> Json.toJson(request.loggedInUser),
          "pstr" -> request.pstr,
          "srn" -> request.srn
        ))
    }
  }

  private val authConnector: AuthConnector = mock[AuthConnector]

  private val bodyParsers: BodyParsers.Default = app.injector.instanceOf[BodyParsers.Default]

  private val authAction = new AuthenticatedIdentifierAction(
    authConnector,
    mockFrontendAppConfig, bodyParsers, mockSessionDataCacheConnector)

  private val psaId = "A0000000"
  private val pspId = "20000000"
  private val externalId = "id"
  private val psaEnrolmentKey = "HMRC-PODS-ORG"
  private val pspEnrolmentKey = "HMRC-PODSPP-ORG"

  private def jsonAOP(aop: AdministratorOrPractitioner) =
    Json.obj("administratorOrPractitioner" -> aop.toString)

  override def beforeEach(): Unit = {
    Mockito.reset(authConnector)
    Mockito.reset(mockSessionDataCacheConnector)
    Mockito.reset(mockFrontendAppConfig)
    when(mockFrontendAppConfig.loginUrl).thenReturn(dummyCall.url)
    when(mockFrontendAppConfig.loginContinueUrl).thenReturn(dummyCall.url)
    when(mockSessionDataCacheConnector.fetch(ArgumentMatchers.eq(externalId))(any(), any()))
      .thenReturn(Future.successful(None))
    when(mockSessionDataCacheConnector.fetch(ArgumentMatchers.eq(SessionKeys.sessionId))(any(), any()))
      .thenReturn(Future.successful(None))
  }

  "Identifier Action" - {
    "when the user has logged in with HMRC-PODS-ORG enrolment must have the PSAID and retrieve PSTR from session DB" in {
      val controller = new Harness(authAction)
      val enrolments = Enrolments(Set(
        Enrolment(psaEnrolmentKey, Seq(EnrolmentIdentifier("PSAID", psaId)), "Activated", None)
      ))

      when(authConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(new ~(Some(externalId), enrolments)))
      val pstrInDB = "456"
      val pstrJson = Json.obj(
        "eventReporting" -> Json.obj(
          "pstr" -> pstrInDB,
          "schemeName" -> "schemeName",
          "returnUrl" -> "returnUrl",
          "srn" -> "srn"
        )
      )

      when(mockSessionDataCacheConnector.fetch(ArgumentMatchers.eq(SessionKeys.sessionId))(any(), any()))
        .thenReturn(Future.successful(Some(pstrJson)))

      val result = controller.onPageLoad()(fakeRequest)
      status(result) mustBe OK
      val actualJsonContent = contentAsJson(result)
      (actualJsonContent \ "loggedInUser").asOpt[LoggedInUser].value mustEqual
        LoggedInUser(externalId = externalId, administratorOrPractitioner = AdministratorOrPractitioner.Administrator, psaIdOrPspId = psaId)
      (actualJsonContent \ "pstr").asOpt[String] mustBe Some(pstrInDB)
    }

    "the user has logged in with HMRC-PODS-ORG and HMRC_PODSPP_ORG enrolments and has not chosen a role " +
      "must redirect to administrator or practitioner page" in {
      val controller = new Harness(authAction)
      val enrolments = Enrolments(Set(
        Enrolment(psaEnrolmentKey, Seq(EnrolmentIdentifier("PSAID", psaId)), "Activated", None),
        Enrolment(pspEnrolmentKey, Seq(EnrolmentIdentifier("PSPID", pspId)), "Activated", None)
      ))

      val pstrInDB = "456"
      val pstrJson = Json.obj(
        "eventReporting" -> Json.obj(
          "pstr" -> pstrInDB,
          "schemeName" -> "schemeName",
          "returnUrl" -> "returnUrl",
          "srn" -> "srn"
        )
      )

      when(mockSessionDataCacheConnector.fetch(ArgumentMatchers.eq(SessionKeys.sessionId))(any(), any()))
        .thenReturn(Future.successful(Some(pstrJson)))

      val adminOrPractitionerUrl = "/dummy-url"

      when(authConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(new ~(Some("id"), enrolments)))
      when(mockFrontendAppConfig.administratorOrPractitionerUrl).thenReturn(adminOrPractitionerUrl)

      val result = controller.onPageLoad()(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result).get must startWith(adminOrPractitionerUrl)
    }

    "the user has logged in with HMRC-PODS-ORG and HMRC_PODSPP_ORG enrolments and has chosen the role of administrator " +
      "must have the PSAID" in {
      when(mockSessionDataCacheConnector.fetch(ArgumentMatchers.eq(externalId))(any(), any()))
        .thenReturn(Future.successful(Some(jsonAOP(Administrator))))
      val controller = new Harness(authAction)
      val enrolments = Enrolments(Set(
        Enrolment(psaEnrolmentKey, Seq(EnrolmentIdentifier("PSAID", psaId)), "Activated", None),
        Enrolment(pspEnrolmentKey, Seq(EnrolmentIdentifier("PSPID", pspId)), "Activated", None)
      ))

      val pstrInDB = "456"
      val pstrJson = Json.obj(
        "eventReporting" -> Json.obj(
          "pstr" -> pstrInDB,
          "schemeName" -> "schemeName",
          "returnUrl" -> "returnUrl",
          "srn" -> "srn"
        )
      )

      when(mockSessionDataCacheConnector.fetch(ArgumentMatchers.eq(SessionKeys.sessionId))(any(), any()))
        .thenReturn(Future.successful(Some(pstrJson)))

      when(authConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(new ~(Some("id"), enrolments)))

      val result = controller.onPageLoad()(fakeRequest)
      status(result) mustBe OK
      (contentAsJson(result) \ "loggedInUser").asOpt[LoggedInUser].value mustEqual
        LoggedInUser(externalId = externalId, administratorOrPractitioner = AdministratorOrPractitioner.Administrator, psaIdOrPspId = psaId)
    }

    "the user has logged in with HMRC-PODS-ORG and HMRC_PODSPP_ORG enrolments and has chosen the role of practitioner " +
      "must have the PSPID and no PSAID" in {
      when(mockSessionDataCacheConnector.fetch(ArgumentMatchers.eq(externalId))(any(), any()))
        .thenReturn(Future.successful(Some(jsonAOP(Practitioner))))
      val controller = new Harness(authAction)
      val enrolments = Enrolments(Set(
        Enrolment(psaEnrolmentKey, Seq(EnrolmentIdentifier("PSAID", psaId)), "Activated", None),
        Enrolment(pspEnrolmentKey, Seq(EnrolmentIdentifier("PSPID", pspId)), "Activated", None)
      ))

      val pstrInDB = "456"
      val pstrJson = Json.obj(
        "eventReporting" -> Json.obj(
          "pstr" -> pstrInDB,
          "schemeName" -> "schemeName",
          "returnUrl" -> "returnUrl",
          "srn" -> "srn"
        )
      )

      when(mockSessionDataCacheConnector.fetch(ArgumentMatchers.eq(SessionKeys.sessionId))(any(), any()))
        .thenReturn(Future.successful(Some(pstrJson)))

      when(authConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(new ~(Some("id"), enrolments)))

      val result = controller.onPageLoad()(fakeRequest)
      status(result) mustBe OK
      (contentAsJson(result) \ "loggedInUser").asOpt[LoggedInUser].value mustEqual
        LoggedInUser(externalId = externalId, administratorOrPractitioner = AdministratorOrPractitioner.Practitioner, psaIdOrPspId = pspId)
    }
  }

  "the user hasn't logged in must redirect the user to log in " in {

    val authAction = new AuthenticatedIdentifierAction(
      new FakeFailingAuthConnector(new MissingBearerToken),
      mockFrontendAppConfig, bodyParsers, mockSessionDataCacheConnector
    )
    val controller = new Harness(authAction)
    val result = controller.onPageLoad()(fakeRequest)

    status(result) mustBe SEE_OTHER

    redirectLocation(result).get must startWith(dummyCall.url)
  }


  "the user doesn't have sufficient enrolments " +
    "must redirect the user to the unauthorised page" in {

    val authAction = new AuthenticatedIdentifierAction(
      new FakeFailingAuthConnector(new InsufficientEnrolments),
      mockFrontendAppConfig, bodyParsers, mockSessionDataCacheConnector
    )

    val testUrl = "/test"

    when(mockFrontendAppConfig.youNeedToRegisterUrl).thenReturn(testUrl)

    val controller = new Harness(authAction)
    val result = controller.onPageLoad()(fakeRequest)

    status(result) mustBe SEE_OTHER

    redirectLocation(result) mustBe Some(testUrl)
  }
  "No pstr, schemeName, returnUrl in sessionCache must return runtimeException" in {
    val authAction = new AuthenticatedIdentifierAction(
      new FakeFailingAuthConnector(new InsufficientEnrolments),
      mockFrontendAppConfig, bodyParsers, mockSessionDataCacheConnector
    )
    val controller = new Harness(authAction)
    a[RuntimeException] mustBe thrownBy {
      status(controller.onPageLoad()(fakeRequest))
    }
  }
}



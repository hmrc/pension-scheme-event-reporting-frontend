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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.SessionDataCacheConnector
import models.LoggedInUser
import models.common.EventReporting
import models.enumeration.AdministratorOrPractitioner
import models.enumeration.AdministratorOrPractitioner._
import models.requests.IdentifierRequest
import play.api.Logger
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.domain.{PsaId, PspId}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject()(
                                               override val authConnector: AuthConnector,
                                               config: FrontendAppConfig,
                                               val parser: BodyParsers.Default,
                                               sessionDataCacheConnector: SessionDataCacheConnector
                                             )
                                             (implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions {

  private val logger = Logger(classOf[AuthenticatedIdentifierAction])

  private val enrolmentPSA = "HMRC-PODS-ORG"
  private val enrolmentPSP = "HMRC-PODSPP-ORG"

  private def getEventReportingData[A](request: Request[A])(implicit hc: HeaderCarrier): Future[Option[EventReporting]] = {
    request.session.get(SessionKeys.sessionId) match {
      case None => Future.successful(None)
      case Some(sessionId) =>
        sessionDataCacheConnector.fetch(sessionId).map { optionJsValue =>
          optionJsValue.flatMap { json =>
            (json \ "eventReporting").validate[EventReporting].asOpt
          }
        }
    }
  }

  private def withAuthInfo[A](
                               block: (Option[String], Enrolments, Option[EventReporting]) => Future[Result]
                             )(implicit request: Request[A], hd: HeaderCarrier): Future[Result] = {
    authorised(Enrolment(enrolmentPSA) or Enrolment(enrolmentPSP)).retrieve(
      Retrievals.externalId and Retrievals.allEnrolments
    ) {
      case optionExternalId ~ enrolments =>
        getEventReportingData(request).flatMap(block.apply(optionExternalId, enrolments, _))
    }
  }

  private def recoverFromError: PartialFunction[Throwable, Future[Result]] = {
    case _: NoActiveSession =>
      Future.successful(Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl))))
    case e: AuthorisationException =>
      logger.warn(s"Authorization Failed with error $e")
      Future.successful(Redirect(config.youNeedToRegisterUrl))
  }

  override def invokeBlock[A](
                               request: Request[A],
                               block: IdentifierRequest[A] => Future[Result]
                             ): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    implicit val req: Request[A] = request

    withAuthInfo {
      case (Some(_), _, None) => Future.successful(Redirect(config.yourPensionSchemesUrl))
      case (Some(externalId), enrolments, Some(er)) if bothPsaAndPspEnrolmentsPresent(enrolments) =>
        actionForBothEnrolments(er, externalId, enrolments, request, block)
      case (Some(externalId), enrolments, Some(er)) if enrolments.getEnrolment(enrolmentPSA).isDefined =>
        actionForOneEnrolment(Administrator, er, externalId, enrolments, request, block)
      case (Some(externalId), enrolments, Some(er)) if enrolments.getEnrolment(enrolmentPSP).isDefined =>
        actionForOneEnrolment(Practitioner, er, externalId, enrolments, request, block)
      case _ => futureUnauthorisedPage
    } recoverWith recoverFromError
  }

  private def getPsaId(enrolments: Enrolments): Option[String] =
    enrolments
      .getEnrolment(key = enrolmentPSA)
      .flatMap(_.getIdentifier("PSAID"))
      .map(enrolmentIdentifier => PsaId(enrolmentIdentifier.value).id)

  private def getPspId(enrolments: Enrolments): Option[String] =
    enrolments
      .getEnrolment(key = enrolmentPSP)
      .flatMap(_.getIdentifier("PSPID"))
      .map(enrolmentIdentifier => PspId(enrolmentIdentifier.value).id)

  private def administratorOrPractitioner(id: String)(implicit hc: HeaderCarrier): Future[Option[AdministratorOrPractitioner]] = {
    sessionDataCacheConnector.fetch(id).map { optionJsValue =>
      optionJsValue.flatMap { json =>
        (json \ "administratorOrPractitioner").toOption.flatMap(_.validate[AdministratorOrPractitioner].asOpt)
      }
    }
  }

  private def bothPsaAndPspEnrolmentsPresent(enrolments: Enrolments): Boolean =
    enrolments.getEnrolment(enrolmentPSA).isDefined && enrolments.getEnrolment(enrolmentPSP).isDefined

  private def getLoggedInUser(externalId: String, role: AdministratorOrPractitioner, enrolments: Enrolments): Option[LoggedInUser] = role match {
    case Administrator => getPsaId(enrolments).map(LoggedInUser(externalId, Administrator, _))
    case Practitioner => getPspId(enrolments).map(LoggedInUser(externalId, Practitioner, _))
  }

  private def futureUnauthorisedPage: Future[Result] = Future.successful(Redirect(config.youNeedToRegisterUrl))

  private def actionForBothEnrolments[A](eventReporting: EventReporting, externalId: String, enrolments: Enrolments, request: Request[A],
                                         block: IdentifierRequest[A] => Future[Result])(implicit headerCarrier: HeaderCarrier): Future[Result] = {
    administratorOrPractitioner(externalId).flatMap {
      case None => Future.successful(Redirect(Call("GET", config.administratorOrPractitionerUrl)))
      case Some(role) =>
        getLoggedInUser(externalId, role, enrolments) match {
          case Some(loggedInUser) => block(IdentifierRequest(request, loggedInUser, eventReporting.pstr, eventReporting.schemeName, eventReporting.returnUrl, eventReporting.srn))
          case _ => futureUnauthorisedPage
        }
    }
  }

  def actionForOneEnrolment[A](administratorOrPractitioner: AdministratorOrPractitioner,
                               eventReporting: EventReporting,
                               externalId: String, enrolments: Enrolments, request: Request[A],
                               block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
    getLoggedInUser(externalId, administratorOrPractitioner, enrolments) match {
      case Some(loggedInUser) => block(IdentifierRequest(request, loggedInUser, eventReporting.pstr, eventReporting.schemeName, eventReporting.returnUrl, eventReporting.srn))
      case _ => futureUnauthorisedPage
    }
  }
}

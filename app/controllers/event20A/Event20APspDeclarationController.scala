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

import connectors.{EventReportingConnector, MinimalConnector, SchemeConnector, UserAnswersCacheConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.event20A.Event20APspDeclarationFormProvider
import models.TaxYear.{getTaxYear, getTaxYearFromOption}
import models.enumeration.EventType
import models.event20A.WhatChange.{BecameMasterTrust, CeasedMasterTrust}
import models.requests.DataRequest
import models.{LoggedInUser, TaxYear, UserAnswers}
import pages.event20A.{BecameDatePage, CeasedDatePage, Event20APspDeclarationPage, WhatChangePage}
import pages.{VersionInfoPage, Waypoints}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event20A.Event20APspDeclarationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Event20APspDeclarationController @Inject()(val controllerComponents: MessagesControllerComponents,
                                                 identify: IdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 userAnswersCacheConnector: UserAnswersCacheConnector,
                                                 formProvider: Event20APspDeclarationFormProvider,
                                                 minimalConnector: MinimalConnector,
                                                 requireData: DataRequiredAction,
                                                 eventReportingConnector: EventReportingConnector,
                                                 schemeDetailsConnector: SchemeConnector,
                                                 view: Event20APspDeclarationView
                                                )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def form(authorisingPsaId: Option[String]): Form[String] = formProvider(authorisingPSAID = authorisingPsaId)

  private val eventType = EventType.Event20A

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType)).async { implicit request =>
    val preparedForm = request.userAnswers.flatMap(_.get(Event20APspDeclarationPage)) match {
      case Some(value) => form(authorisingPsaId = None).fill(value)
      case None => form(authorisingPsaId = None)
    }
    minimalConnector.getMinimalDetails(request.loggedInUser.idName, request.loggedInUser.psaIdOrPspId).map {
      minimalDetails =>
        Ok(view(request.schemeName, request.pstr, getTaxYearFromOption(request.userAnswers).toString, minimalDetails.name, preparedForm, waypoints))
    }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData(eventType) andThen requireData).async {
    implicit request =>
      minimalConnector.getMinimalDetails(request.loggedInUser.idName, request.loggedInUser.psaIdOrPspId).flatMap { minimalDetails =>
        schemeDetailsConnector.getPspSchemeDetails(request.loggedInUser.psaIdOrPspId, request.pstr).map(_.authorisingPSAID).flatMap { authorisingPsaId =>
          form(authorisingPsaId = authorisingPsaId)
            .bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(request.schemeName, request.pstr, getTaxYear(request.userAnswers).toString, minimalDetails.name, formWithErrors, waypoints))),
            value => {
              val originalUserAnswers = request.userAnswers
              val updatedAnswers = originalUserAnswers.setOrException(Event20APspDeclarationPage, value)
              userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).flatMap { _ =>
                declarationDataEvent20A(request.pstr, TaxYear.getSelectedTaxYear(request.userAnswers), request.loggedInUser, authorisingPsaId, request) match {
                  case Some(data) =>
                    val reportVersion = request.userAnswers.get(VersionInfoPage).get.version.toString
                    eventReportingConnector.submitReportEvent20A(request.pstr, UserAnswers(data), reportVersion).map { _ =>
                      Redirect(Event20APspDeclarationPage.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
                    }
                  case _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad(None).url))
                }
              }
            }
          )
        }
      }
  }

  def declarationDataEvent20A(pstr: String, taxYear: TaxYear, loggedInUser: LoggedInUser, optAuthorisingPsaId: Option[String], request: DataRequest[AnyContent]): Option[JsObject] = {
    val optCeaseDateOrStartDateNode = (request.userAnswers.get(WhatChangePage), request.userAnswers.get(BecameDatePage), request.userAnswers.get(CeasedDatePage)) match {
      case (Some(BecameMasterTrust), Some(becameDate), _) => Some(Json.obj("schemeMasterTrustStartDate" -> becameDate))
      case (Some(CeasedMasterTrust), _, Some(ceasedDate)) => Some(Json.obj("schemeMasterTrustCeaseDate" -> ceasedDate))
      case _ => None
    }
    optCeaseDateOrStartDateNode.flatMap { ceaseDateOrStartDateNode =>
      optAuthorisingPsaId.map { authorisingPsaId =>
        val event20AUserAnswers: JsObject = Json.obj(
          "pstr" -> pstr,
          "reportStartDate" -> s"${taxYear.startYear}-04-06",
          "reportEndDate" -> s"${taxYear.endYear}-04-05",
          "submittedBy" -> "PSP",
          "submittedID" -> loggedInUser.psaIdOrPspId,
          "authorisedPSAID" -> authorisingPsaId,
          //Both of those are always selected. Users can't access the page otherwise.
          "pspDeclaration1" -> "Selected",
          "pspDeclaration2" -> "Selected"
        )
        event20AUserAnswers ++ ceaseDateOrStartDateNode
      }
    }
  }
}

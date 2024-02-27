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

package controllers.event24

import connectors.UserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.event24.TypeOfProtectionGroup1ReferenceFormProvider
import models.enumeration.EventType
import models.event24.{ProtectionReferenceData, TypeOfProtectionGroup1}
import models.event24.TypeOfProtectionGroup1.SchemeSpecific
import models.{Index, UserAnswers}
import pages.Waypoints
import pages.event24.{TypeOfProtectionGroup1Page, TypeOfProtectionGroup1ReferencePage}
import play.api.data.FormError
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.event24.TypeOfProtectionGroup1ReferenceView

import javax.inject.Inject
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

class TypeOfProtectionGroup1ReferenceController @Inject()(val controllerComponents: MessagesControllerComponents,
                                                          identify: IdentifierAction,
                                                          getData: DataRetrievalAction,
                                                          userAnswersCacheConnector: UserAnswersCacheConnector,
                                                          formProvider: TypeOfProtectionGroup1ReferenceFormProvider,
                                                          view: TypeOfProtectionGroup1ReferenceView
                                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  private val eventType = EventType.Event24

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType)) { implicit request =>
    val requiredReferenceTypes = getRequiredReferenceTypes(request.userAnswers, index)
    val preparedForm = request.userAnswers.flatMap(_.get(TypeOfProtectionGroup1ReferencePage(index))).fold(form)(form.fill)
    Ok(view(preparedForm, waypoints, index, requiredReferenceTypes))
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData(eventType)).async {
    implicit request =>
      val requiredReferenceTypes = getRequiredReferenceTypes(request.userAnswers, index)
      form.bindFromRequest().fold(
        formWithErrors => {
          val refTypesForm = form.bindFromRequest()

          val validErrors = getValidErrors(refTypesForm.errors, Seq.empty, requiredReferenceTypes)

          if (validErrors.nonEmpty) {
            val updatedFormWithErrors = formWithErrors.copy(errors = validErrors)
            Future.successful(BadRequest(view(updatedFormWithErrors, waypoints, index, requiredReferenceTypes)))
          } else {
            val value = getUserAnswers(refTypesForm.data)

            val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
            val updatedAnswers = originalUserAnswers.setOrException(TypeOfProtectionGroup1ReferencePage(index), value)
            userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
              Redirect(TypeOfProtectionGroup1ReferencePage(index).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
            }
          }
        },
        value => {
          val originalUserAnswers = request.userAnswers.fold(UserAnswers())(identity)
          val updatedAnswers = originalUserAnswers.setOrException(TypeOfProtectionGroup1ReferencePage(index), value)
          userAnswersCacheConnector.save(request.pstr, eventType, updatedAnswers).map { _ =>
            Redirect(TypeOfProtectionGroup1ReferencePage(index).navigate(waypoints, originalUserAnswers, updatedAnswers).route)
          }
        }
      )
  }

  private def getRequiredReferenceTypes(userAnswers: Option[UserAnswers], index: Index): Seq[TypeOfProtectionGroup1] = {
    userAnswers.flatMap(_.get(TypeOfProtectionGroup1Page(index))) match {
      case Some(typesOfProtection) => typesOfProtection.filter(_ != SchemeSpecific).toSeq
      case None => throw new NotFoundException("typeOfProtectionGroup1 value was missing from user answers")
    }
  }

  @tailrec
  private def getValidErrors(allErrors: Seq[FormError], validErrors: Seq[FormError], requiredReferenceTypes: Seq[TypeOfProtectionGroup1]): Seq[FormError] = {
    if (allErrors.isEmpty) {
      validErrors
    } else if (requiredReferenceTypes.map(_.toString).contains(allErrors.head.key)) {
      val errors = validErrors ++ Seq(allErrors.head)
      getValidErrors(allErrors.tail, errors, requiredReferenceTypes)
    } else {
      getValidErrors(allErrors.tail, validErrors, requiredReferenceTypes)
    }
  }

  private def getUserAnswers(formData: Map[String, String]): ProtectionReferenceData = {
    val nonResidenceEnhancement = formData.getOrElse("nonResidenceEnhancement", "")
    val pensionCreditsPreCRE = formData.getOrElse("pensionCreditsPreCRE", "")
    val preCommencement = formData.getOrElse("preCommencement", "")
    val recognisedOverseasPSTE = formData.getOrElse("recognisedOverseasPSTE", "")
    ProtectionReferenceData(nonResidenceEnhancement, pensionCreditsPreCRE, preCommencement, recognisedOverseasPSTE)
  }
}
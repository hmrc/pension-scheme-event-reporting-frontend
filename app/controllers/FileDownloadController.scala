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

package controllers

import controllers.actions.IdentifierAction
import models.enumeration.EventType
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FileProviderService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FileDownloadController @Inject()(override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       fileProviderService: FileProviderService,
                                       val controllerComponents: MessagesControllerComponents
                                      )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {

  def templateFile(eventType: EventType): Action[AnyContent] = {
    identify.async { _ =>
      Future.successful(
        Ok.sendFile(
          content = fileProviderService.getTemplateFile(eventType),
          inline = false
        ))
    }
  }

  def instructionsFile(eventType: EventType): Action[AnyContent] = {
    identify.async { _ =>
      Future.successful(
        Ok.sendFile(
          content = fileProviderService.getInstructionsFile(eventType),
          inline = false
        ))
    }
  }
}

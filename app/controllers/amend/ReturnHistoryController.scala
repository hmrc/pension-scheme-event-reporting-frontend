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

import connectors.EventReportingConnector
import controllers.actions._
import models.TaxYear.getTaxYearFromOption
import pages.Waypoints
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.amend.ReturnHistoryView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ReturnHistoryController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           erConnector: EventReportingConnector,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: ReturnHistoryView
                                         )(implicit ec: ExecutionContext)  extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData()) async {
    implicit request =>
      val g = getTaxYearFromOption(request.userAnswers).toString
      erConnector.getListOfVersions(request.pstr, g + "-04-06").map { g =>
        val taxYearRange = (getTaxYearFromOption(request.userAnswers).toString, (getTaxYearFromOption(request.userAnswers) + 1).toString)
        Ok(view(g, taxYearRange._1, taxYearRange._2))
      }
  }
}

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

import config.FrontendAppConfig
import controllers.actions.IdentifierAction
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.partials._
import views.html.partials.EventReportingTileView

import javax.inject.Inject
import scala.concurrent.Future

class EventReportingTileController @Inject()(
                                              identify: IdentifierAction,
                                              view: EventReportingTileView,
                                              val controllerComponents: MessagesControllerComponents,
                                              appConfig: FrontendAppConfig
                                            )
  extends FrontendBaseController
    with I18nSupport {

  def eventReportPartial(): Action[AnyContent] = {
    identify.async { implicit request =>

       val card = Seq(CardViewModel(
          id = "aft-overview",
          heading = Messages("eventReportingTile.heading"),
          subHeadings = Seq(CardSubHeading(subHeading = Messages("eventReportingTile.subHeading", "2022", "2023"),
            subHeadingClasses = "card-sub-heading",
            subHeadingParams = Seq(CardSubHeadingParam(
              subHeadingParam = Messages("eventReportingTile.subHeading.param"),
              subHeadingParamClasses = "font-small bold")
            ))),
          links = Seq(Link("erLoginLink", appConfig.erLoginUrl, Text(Messages("eventReportingTile.link.item2"))))
        ))

        Future.successful(Ok(view(card)))
    }
  }
}
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
import connectors.EventReportingConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.{TaxYear, ToggleDetails}
import pages.TaxYearPage
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.partials._
import views.html.partials.EventReportingTileView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class EventReportingTileController @Inject()(
                                              identify: IdentifierAction,
                                              view: EventReportingTileView,
                                              getData: DataRetrievalAction,
                                              val controllerComponents: MessagesControllerComponents,
                                              appConfig: FrontendAppConfig,
                                              eventReportingConnector: EventReportingConnector
                                            )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {

  def eventReportPartial(): Action[AnyContent] = {
    (identify andThen getData()).async { implicit request =>
      val taxYearRangeAsSeqString = request.userAnswers.flatMap(_.get(TaxYearPage)) match {
        case Some(taxYear: TaxYear) => taxYear.rangeAsSeqString
        case _ => Seq.empty
      }

      val maybeCardSubHeading: Seq[String] => Seq[CardSubHeading] = (seqOfString: Seq[String]) => {
        if (seqOfString.length == 2) {
          Seq(
            CardSubHeading(
              subHeading = Messages("eventReportingTile.subHeading", seqOfString.head, seqOfString.tail.head),
              subHeadingClasses = "card-sub-heading",
              subHeadingParams =
                Seq(
                  CardSubHeadingParam(
                    subHeadingParam = Messages("eventReportingTile.subHeading.param"),
                    subHeadingParamClasses = "font-small bold"
                  )
                )
            )
          )
        } else {
          Seq(CardSubHeading(subHeading = "", subHeadingClasses = ""))
        }
      }

      eventReportingConnector.getFeatureToggle("event-reporting").map {
        case ToggleDetails(_, _, true) =>
          val card =
            Seq(
              CardViewModel(
                id = "aft-overview",
                heading = Messages("eventReportingTile.heading"),
                subHeadings = maybeCardSubHeading(taxYearRangeAsSeqString),
                links = Seq(Link("erLoginLink", appConfig.erLoginUrl, Text(Messages("eventReportingTile.link.item2"))))
              )
            )
          Ok(view(card))
        case _ => Ok("")
      }
    }
  }
}

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
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import helpers.DateHelper.dateFormatter
import models.{EROverview, TileCard, TileLink, UserAnswers}
import pages.EventReportingOverviewPage
import play.api.i18n.{I18nSupport, Lang, Messages}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.partials._

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.ExecutionContext

//noinspection ScalaStyle
class EventReportingTileController @Inject()(
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              val controllerComponents: MessagesControllerComponents,
                                              appConfig: FrontendAppConfig,
                                              eventReportingConnector: EventReportingConnector,
                                              userAnswersCacheConnector: UserAnswersCacheConnector
                                            )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {

  import EventReportingTileController._

  def eventReportPartial: Action[AnyContent] = {
    Action.async { implicit request =>

      implicit val lang: Lang = request.lang
      val json = request.body.asJson.getOrElse(throw new RuntimeException("Request body is not json"))
      val pstr = (json \ "pstr").validate[String].getOrElse("PSTR not in json body")
      eventReportingConnector.getOverview(pstr, "ER", minStartDateAsString, maxEndDateAsString).flatMap { seqEROverview =>
        val ua = UserAnswers().setOrException(EventReportingOverviewPage, seqEROverview, nonEventTypeData = true)
        userAnswersCacheConnector.removeAll(pstr).flatMap { _ =>
          userAnswersCacheConnector.save(pstr, ua).map { _ =>
            val isAnySubmittedReports = seqEROverview.exists(_.versionDetails.exists(_.submittedVersionAvailable))
            val isAnyCompiledReports = seqEROverview.exists(_.versionDetails.exists(_.compiledVersionAvailable))
            val compiledLinks = if (isAnyCompiledReports) {
              Seq(TileLink("compiled", messagesApi("eventReportingTile.link.compiled"), appConfig.erCompiledUrl))
            } else Nil

            val submittedLinks = if (isAnySubmittedReports) {
              Seq(TileLink("submitted", messagesApi("eventReportingTile.link.submitted"), appConfig.erSubmittedUrl))
            } else Nil
            val loginLink = Seq(TileLink("new", messagesApi("eventReportingTile.link.new"), appConfig.erStartNewUrl))

            val subHeading = cardSubheadings(isAnyCompiledReports, seqEROverview)

            Ok(Json.toJson(TileCard(
              "event-reporting",
              messagesApi("eventReportingTile.heading"),
              s"""
              |<p class="card-sub-heading bold govuk-body-m">
              |<span class="font-xsmall">${subHeading.subHeading}</span>
              | ${subHeading.subHeadingParams.map { subParam => {
                s"""<span class="govuk-!-font-weight-bold govuk-!-display-inline-block ${subParam.subHeadingParamClasses}">
                  ${subParam.subHeadingParam}
                </span>"""
              }}.mkString}
              |</p>""".stripMargin,
              compiledLinks ++ submittedLinks ++ loginLink
            )))
          }
        }
      }
    }
  }

  private def cardSubheadings(isAnyCompiledReports: Boolean, seqEROverview: Seq[EROverview])(implicit request: Request[AnyContent]): CardSubHeading = {
    if (isAnyCompiledReports) {
      val overviewsInProgress = seqEROverview.filter(_.versionDetails.exists(_.compiledVersionAvailable))

      val (subHeadingMessage, subHeadingParamMessage) = overviewsInProgress.size match {
        case 1 =>
          (Messages("eventReportingTile.subHeading", dateFormatter.format(overviewsInProgress.head.periodStartDate), dateFormatter.format(overviewsInProgress.head.periodEndDate)),
            Messages("eventReportingTile.subHeading.param"))
        case _ =>
          (Messages("eventReportingTile.subHeading.multiple"),
            Messages("eventReportingTile.subHeading.param.multiple", overviewsInProgress.size))
      }

        CardSubHeading(
          subHeading = subHeadingMessage,
          subHeadingClasses = "card-sub-heading",
          subHeadingParams =
            Seq(
              CardSubHeadingParam(
                subHeadingParam = subHeadingParamMessage,
                subHeadingParamClasses = "font-small bold"
              )
            )
        )
    } else {
      CardSubHeading(subHeading = "", subHeadingClasses = "")
    }
  }
}

object EventReportingTileController {
  val minStartDateAsString = "2000-04-06"
  val maxEndDateAsString = s"${LocalDate.now().getYear + 1}-04-05"
}

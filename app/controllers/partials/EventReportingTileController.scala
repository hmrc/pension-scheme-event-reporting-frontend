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
import models.enumeration.EventType.Event10
import models.requests.OptionalDataRequest
import models.{EROverviewVersion, ToggleDetails, UserAnswers}
import pages.EventReportingOverviewPage
import play.api.i18n.{I18nSupport, Messages}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.partials._
import views.html.partials.EventReportingTileView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

//noinspection ScalaStyle
class EventReportingTileController @Inject()(
                                              identify: IdentifierAction,
                                              view: EventReportingTileView,
                                              getData: DataRetrievalAction,
                                              val controllerComponents: MessagesControllerComponents,
                                              appConfig: FrontendAppConfig,
                                              eventReportingConnector: EventReportingConnector,
                                              userAnswersCacheConnector: UserAnswersCacheConnector
                                            )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {
  import EventReportingTileController._

  def eventReportPartial(): Action[AnyContent] = {
    (identify andThen getData()).async { implicit request =>
      eventReportingConnector.getOverview(request.pstr, "ER", minStartDateAsString, maxEndDateAsString).flatMap { result =>
        val ua = request.userAnswers.getOrElse(UserAnswers()).setOrException(EventReportingOverviewPage, result, nonEventTypeData = true)
        userAnswersCacheConnector.save(request.pstr, ua).flatMap { _ =>
          val anySubmittedReports = result.map { value =>
            value.versionDetails match {
              case Some(EROverviewVersion(_, submittedVersionAvailable, _)) => submittedVersionAvailable
              case None => false
            }
          }
          val anyCompiledReports = result.map { value =>
            value.versionDetails match {
              case Some(EROverviewVersion(_, _, compiledVersionAvailable)) => compiledVersionAvailable
              case None => false
            }
          }

          // TODO: if this seq > 1, you want to display s"${seq.length} in progress" rather than the year range
          val maybeCompiledVersions = anyCompiledReports.collectFirst(x => x).getOrElse(false)
          val maybeCompiledLink: Seq[Link] = if (maybeCompiledVersions) {
            Seq(Link("erCompiledLink", appConfig.erCompiledUrl, Text("eventReportingTile.link.compiled")))
          } else Nil

          val maybeSubmittedVersions = anySubmittedReports.collectFirst(x => x).getOrElse(false)
          val maybeSubmittedLink: Seq[Link] = if (maybeSubmittedVersions) {
            Seq(Link("erSubmittedLink", appConfig.erSubmittedUrl, Text("eventReportingTile.link.submitted")))
          } else Nil

          // TODO: implement below link in future - in PODS-8491.
          // View event reports in progress
          // Link(...)

          val maybeCardSubHeading: Seq[CardSubHeading] = {
            if (maybeCompiledVersions) {
              val anyStartAndEndDates = result.map { value => (value.periodStartDate, value.periodEndDate) }
              Seq(
                CardSubHeading(
                  // TODO: if multiple compile in progress, display s"${seq.length} in progress", PODS-8491
                  subHeading = Messages("eventReportingTile.subHeading", anyStartAndEndDates.head._1, anyStartAndEndDates.head._2),
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
          val loginLink: Seq[Link] = Seq(Link("erLoginLink", appConfig.erLoginUrl, Text(Messages("eventReportingTile.link.new"))))
          renderTile(maybeCompiledLink, maybeSubmittedLink, maybeCardSubHeading, loginLink)
        }
      }
    }
  }

  private def renderTile(maybeCompiledLink: Seq[Link],
                  maybeSubmittedLink: Seq[Link],
                  maybeCardSubHeading: Seq[CardSubHeading],
                  loginLink: Seq[Link])(implicit request: OptionalDataRequest[AnyContent]): Future[Result] = {
    eventReportingConnector.getFeatureToggle("event-reporting").map {
      case ToggleDetails(_, _, true) =>
        val card =
          Seq(
            CardViewModel(
              id = "new",
              heading = Messages("eventReportingTile.heading"),
              subHeadings = maybeCardSubHeading,
              links = loginLink
            ),
            CardViewModel(
              id = "compiled",
              heading = "",
              links = maybeCompiledLink
            ),
            CardViewModel(
              id = "submitted",
              heading = "",
              links = maybeSubmittedLink
            )
          )
        Ok(view(card))
      case _ => Ok("")
    }
  }
}

object EventReportingTileController {
  val minStartDateAsString = "2000-04-06"                               // TODO: confirm min start date
  val maxEndDateAsString = s"${LocalDate.now().getYear + 1}-04-05"      // TODO: confirm max end date
}

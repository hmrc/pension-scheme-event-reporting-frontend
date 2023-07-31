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

import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import controllers.actions._
import models.TaxYear.getTaxYearFromOption
import models.VersionInfo
import models.enumeration.VersionStatus.{Compiled, Submitted}
import pages.{EventSummaryPage, VersionInfoPage, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Actions, Text}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateHelper.formatDateDMY
import viewmodels.{Message, ReturnHistorySummary}
import views.html.amend.ReturnHistoryView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ReturnHistoryController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           erConnector: EventReportingConnector,
                                           requireData: DataRequiredAction,
                                           val controllerComponents: MessagesControllerComponents,
                                           userAnswersCacheConnector: UserAnswersCacheConnector,
                                           view: ReturnHistoryView
                                         )(implicit ec: ExecutionContext)  extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData()) async {
    implicit request =>
      val g = getTaxYearFromOption(request.userAnswers).toString
      erConnector.getListOfVersions(request.pstr, g + "-04-06").map { h =>
        val t = h.filter(x => x.versionDetails.status == Submitted)
        val m = t.map { kk =>

          val (compileStatus, version, submitterName) = kk.versionDetails.status match {
            case Compiled => ("In progress", "Draft", "")
            case _ => (kk.versionDetails.status.toString.capitalize + " on " + formatDateDMY(kk.submittedDate), kk.versionDetails.version.toString, kk.submitterName.getOrElse(""))
          }

          def changeOrViewLink = {
            val finalVersion = h.length
            if (kk.versionDetails.version == finalVersion) "site.viewOrChange" else "site.view"
          }

          val viewOrChangeLink = kk.versionDetails.status match {
            case Compiled => "site.change"
            case _ => changeOrViewLink
          }

          //view (for all other cases), view or change (when its the most recent version), change (when in compile)

          ReturnHistorySummary(
            key = version,
            firstValue = compileStatus,
            secondValue = submitterName,
            actions = Some(Actions(
              items = Seq(
                ActionItem(
                  content = Text(Message(viewOrChangeLink)),
                  href = controllers.amend.routes.ReturnHistoryController.onClick(waypoints, version).url
                )
              )
            ))
          )
        }
        val taxYearRange = (getTaxYearFromOption(request.userAnswers).toString, (getTaxYearFromOption(request.userAnswers) + 1).toString)
        Ok(view(m, taxYearRange._1, taxYearRange._2))
      }
  }

      def onClick(waypoints: Waypoints, version: String): Action[AnyContent] = (identify andThen getData() andThen requireData) async {
        implicit request =>
          val versionObj = VersionInfo(version.toInt, Submitted)
            val updateUA = request.userAnswers.setOrException(VersionInfoPage, versionObj, nonEventTypeData = true)
          userAnswersCacheConnector.save(request.pstr, updateUA).map {
            _ => Redirect(controllers.routes.EventSummaryController.onPageLoad(waypoints))
          }
      }

  }

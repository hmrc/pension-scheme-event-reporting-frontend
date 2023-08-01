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
import pages.{VersionInfoPage, Waypoints}
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
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData()) async {
    implicit request =>
      val currentTaxYear = getTaxYearFromOption(request.userAnswers)
      erConnector.getListOfVersions(request.pstr, currentTaxYear.toString + "-04-06").map { seqVersionsWithSubmitter =>
        val seqVersionsSubmitted = seqVersionsWithSubmitter.filter(versions => versions.versionDetails.status == Submitted)
        val seqRetHistorySummary = seqVersionsSubmitted.map { versionWithSubmitter =>

          def changeOrViewLink = {
            val recentSubmittedVersion = seqVersionsSubmitted.length
            if (versionWithSubmitter.versionDetails.version == recentSubmittedVersion) "site.viewOrChange" else "site.view"
          }

          val version = versionWithSubmitter.versionDetails.version.toString

          ReturnHistorySummary(
            key = version,
            firstValue = versionWithSubmitter.versionDetails.status.toString.capitalize + " on " + formatDateDMY(versionWithSubmitter.submittedDate),
            secondValue = versionWithSubmitter.submitterName.getOrElse(""),
            actions = Some(Actions(
              items = Seq(
                ActionItem(
                  content = Text(Message(changeOrViewLink)),
                  href = controllers.amend.routes.ReturnHistoryController.onClick(waypoints, version).url
                )
              )
            ))
          )
        }
        val taxYearRange = (currentTaxYear.toString, (currentTaxYear + 1).toString)
        Ok(view(seqRetHistorySummary, taxYearRange._1, taxYearRange._2))
      }
  }

  def onClick(waypoints: Waypoints, version: String): Action[AnyContent] = (identify andThen getData() andThen requireData) async {
    implicit request =>
      val versionInfo = VersionInfo(version.toInt, Submitted)
      val updateUA = request.userAnswers.setOrException(VersionInfoPage, versionInfo, nonEventTypeData = true)
      userAnswersCacheConnector.save(request.pstr, updateUA).map {
        _ => Redirect(controllers.routes.EventSummaryController.onPageLoad(waypoints))
      }
  }

}

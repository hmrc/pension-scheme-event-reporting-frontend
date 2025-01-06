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

package controllers.amend

import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import controllers.actions._
import models.TaxYear.getTaxYearFromOption
import models.VersionInfo
import models.amend.VersionsWithSubmitter
import models.enumeration.VersionStatus
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
        val compileVersion: Int = seqVersionsWithSubmitter.map(_.versionDetails.version) match {
          case Nil => 1 // Default to 1 if the collection is empty
          case versions => versions.max + 1
        }
        val latestSubmittedReturn = seqVersionsWithSubmitter.map(_.versionDetails.version).maxOption.getOrElse(1)
        val isDraftExist = seqVersionsWithSubmitter.exists(_.versionDetails.status == Compiled)
        val seqRetHistorySummary = seqVersionsWithSubmitter.sortBy(sortExpr).map { versionWithSubmitter =>
          val isSubmitted = versionWithSubmitter.versionDetails.status == Submitted
          val version = versionWithSubmitter.versionDetails.version
          val versionToDisplay = if (isSubmitted) version.toString else "Draft"
          val versionLabel = if (isSubmitted) version.toString else compileVersion.toString
          val submitterBy = if (isSubmitted) versionWithSubmitter.submitterName.getOrElse("") else ""
          val viewOrChange = (isSubmitted, isDraftExist, latestSubmittedReturn == version) match {
            case (true, false, true) => "site.viewOrChange"
            case (false, _, _) => "site.viewOrChange"
            case _ => "site.view"
          }

          ReturnHistorySummary(
            key = versionToDisplay,
            firstValue = getFirstValue(versionWithSubmitter),
            secondValue = submitterBy,
            actions = Some(Actions(
              items = Seq(
                ActionItem(
                  content = Text(Message(viewOrChange)),
                  href = controllers.amend.routes.ReturnHistoryController.onClick(waypoints, versionLabel).url
                )
              )
            ))
          )
        }
        val schemeName = request.schemeName
        val taxYearEnd = (currentTaxYear + 1).toString
        Ok(view(seqRetHistorySummary, taxYearEnd, schemeName))
      }
  }

  private val sortExpr: VersionsWithSubmitter => (Int, Int) = versionsWithSubmitter => {
    versionsWithSubmitter.versionDetails.status match {
      case Compiled => (0, Int.MaxValue) // Compiled (Draft) comes first
      case _        => (1, -versionsWithSubmitter.versionDetails.version) // Other versions in descending order
    }
  }

  def getFirstValue(versionWithSubmitter: VersionsWithSubmitter) = if (versionWithSubmitter.versionDetails.status == Submitted) {
    versionWithSubmitter.versionDetails.status.toString.capitalize + " on " + formatDateDMY(versionWithSubmitter.submittedDate)
  } else {
    "In progress"
  }

  def onClick(waypoints: Waypoints, version: String): Action[AnyContent] = (identify andThen getData() andThen requireData) async {
    implicit request =>
      val currentTaxYear = getTaxYearFromOption(Some(request.userAnswers))
      erConnector.getListOfVersions(request.pstr, currentTaxYear.toString + "-04-06").flatMap { seqVersionsWithSubmitter =>
       val statusFromDb: Option[VersionStatus] = seqVersionsWithSubmitter.find(_.versionDetails.version == version.toInt).map(_.versionDetails.status)
        val status = statusFromDb.getOrElse(VersionStatus.Compiled)
        val versionInfo = VersionInfo(version.toInt, status)
        val updateUA = request.userAnswers.setOrException(VersionInfoPage, versionInfo, nonEventTypeData = true)
        userAnswersCacheConnector.save(request.pstr, updateUA).map {
          _ => Redirect(controllers.routes.EventSummaryController.onPageLoad(waypoints))
        }
      }

  }

}

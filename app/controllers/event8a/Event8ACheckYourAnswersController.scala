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

package controllers.event8a

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.enumeration.EventType.Event8A
import models.event8a.PaymentType
import models.requests.DataRequest
import models.{Index, UserAnswers}
import pages.common.MembersDetailsPage
import pages.event8a.Event8ACheckYourAnswersPage
import pages.{CheckAnswersPage, EmptyWaypoints, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CompileService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.MembersDetailsSummary
import viewmodels.event8.checkAnswers.{LumpSumAmountAndDateSummary, TypeOfProtectionReferenceSummary, TypeOfProtectionSummary}
import viewmodels.event8a.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.ExecutionContext

class Event8ACheckYourAnswersController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   compileService: CompileService,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: CheckYourAnswersView
                                                 )(implicit val ec: ExecutionContext) extends FrontendBaseController with I18nSupport { //scalastyle:off method.length

  private def missingDataResult(userAnswers: UserAnswers, index: Index, waypoints: Waypoints) = {
    def missingDataStandAloneLumpSumPage = {
      val typeOfProtection = pages.event8.TypeOfProtectionPage(Event8A, index)
      val typeOfProtectionRef = pages.event8.TypeOfProtectionReferencePage(Event8A, index)
      val lumpSumDetails = pages.event8.LumpSumAmountAndDatePage(Event8A, index)

      val requiredData = Seq(
        typeOfProtection -> userAnswers.get(typeOfProtection),
        typeOfProtectionRef -> userAnswers.get(typeOfProtectionRef),
        lumpSumDetails -> userAnswers.get(lumpSumDetails)
      )

      requiredData.collectFirst {
        case (page, None) => page
      }
    }

    def missingDataSchemeSpecificLumpSumPage = {
      val page = pages.event8.LumpSumAmountAndDatePage(Event8A, index)
      userAnswers.get(page) match {
        case None => Some(page)
        case _ => None
      }
    }

    val details = MembersDetailsPage(Event8A, index)
    val paymentType = pages.event8a.PaymentTypePage(Event8A, index)
    lazy val paymentTypeUserAnswers = userAnswers.get(paymentType)

    val requiredData = Seq(
      details -> userAnswers.get(details),
      paymentType -> paymentTypeUserAnswers
    )

    val page = requiredData.collectFirst {
      case (page, None) => page
    }

    val finalPage = page match {
      case None => paymentTypeUserAnswers.flatMap({ paymentType =>
        if (paymentType == PaymentType.PaymentOfAStandAloneLumpSum) {
          missingDataStandAloneLumpSumPage
        } else {
          missingDataSchemeSpecificLumpSumPage
        }
      })
      case _ => page
    }

    finalPage.map { page =>
      Redirect(page.changeLink(waypoints, Event8ACheckYourAnswersPage(index)).route)
    }
  }


  def onPageLoad(index: Index): Action[AnyContent] =
    (identify andThen getData(Event8A) andThen requireData) { implicit request =>
      val thisPage = Event8ACheckYourAnswersPage(index)
      val waypoints = EmptyWaypoints
      val continueUrl = controllers.event8a.routes.Event8ACheckYourAnswersController.onClick.url

      missingDataResult(request.userAnswers, index, waypoints).getOrElse(
        Ok(view(SummaryListViewModel(rows = buildEvent8aCYARows(waypoints, thisPage, index)), continueUrl))
      )
    }

  def onClick: Action[AnyContent] =
    (identify andThen getData(Event8A) andThen requireData).async { implicit request =>
      compileService.compileEvent(Event8A, request.pstr, request.userAnswers).map {
        _ =>
          Redirect(controllers.common.routes.MembersSummaryController.onPageLoad(EmptyWaypoints, Event8A).url)
      }
    }

  private def buildEvent8aCYARows(waypoints: Waypoints, sourcePage: CheckAnswersPage, index: Index)
                                 (implicit request: DataRequest[AnyContent]): Seq[SummaryListRow] = {
    MembersDetailsSummary.rowFullName(request.userAnswers, waypoints, index, sourcePage, request.readOnly(), Event8A).toSeq ++
      MembersDetailsSummary.rowNino(request.userAnswers, waypoints, index, sourcePage, Event8A).toSeq ++
      PaymentTypeSummary.row(request.userAnswers, waypoints, index, sourcePage, Event8A).toSeq ++
      TypeOfProtectionSummary.row(request.userAnswers, waypoints, index, sourcePage, Event8A).toSeq ++
      TypeOfProtectionReferenceSummary.row(request.userAnswers, waypoints, sourcePage, Event8A, index).toSeq ++
      LumpSumAmountAndDateSummary.rowLumpSumValue(request.userAnswers, waypoints, sourcePage, Event8A, index).toSeq ++
      LumpSumAmountAndDateSummary.rowLumpSumDate(request.userAnswers, waypoints, sourcePage, Event8A, index).toSeq
  }
}

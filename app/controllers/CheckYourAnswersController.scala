/*
 * Copyright 2022 HM Revenue & Customs
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

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.enumeration.EventType
import pages.{CheckYourAnswersPage, EmptyWaypoints, TestYesNoPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.govuk.summarylist._
import viewmodels.implicits._
import views.html.CheckYourAnswersView

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView
                                          ) extends FrontendBaseController with I18nSupport {

  private val pstr = "123"

  def onPageLoad: Action[AnyContent] = (identify andThen getData(pstr, EventType.Event1) andThen requireData) { implicit request =>

    val thisPage  = CheckYourAnswersPage
    val waypoints = EmptyWaypoints

    request.userAnswers.get(TestYesNoPage) match {
      case Some(answer) =>
        val summaryListRows = SummaryListRowViewModel(
          key = "test.checkYourAnswersLabel",
          value = ValueViewModel(answer.toString),
          actions = Seq(
            ActionItemViewModel("site.change", TestYesNoPage.changeLink(waypoints, thisPage).url)
          )
        )
        val list = SummaryListViewModel(
          rows = Seq(summaryListRows)
        )
        Ok(view(list))
      case _ => Redirect(routes.JourneyRecoveryController.onPageLoad())
    }

  }
}

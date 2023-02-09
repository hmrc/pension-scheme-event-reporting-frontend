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

import connectors.EventReportingConnector
import controllers.actions.IdentifierAction
import models.event1.member.SchemeDetails
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.partials.{CardSubHeading, CardSubHeadingParam, CardViewModel}
import views.html.partials.EventReportingTileView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EventReportingTileController @Inject()(
                                              identify: IdentifierAction,
                                              view: EventReportingTileView,
                                              //                                            override val messagesApi: MessagesApi,
                                              val controllerComponents: MessagesControllerComponents,
                                              erConnector: EventReportingConnector,
                                              //                                            renderer: Renderer,
                                              //                                            aftPartialService: AFTPartialService
                                            )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {

  def eventReportPartial(): Action[AnyContent] = {
    identify.async { implicit request =>

       val card = for {
          overview <- erConnector.getEventReportSummary(request.pstr)
//          startLink <- getStartReturnLink(overview, srn, schemeDetails.pstr)
//          (subHeadings, inProgressLink) <- getInProgressReturnsModel(overview, srn, schemeDetails.pstr)
        } yield Seq(CardViewModel(
          id = "aft-overview",
          heading = Messages("eventReportingTile.heading"),
          subHeadings = Seq(CardSubHeading(subHeading = "Event report 2022 to 2023:",
            subHeadingClasses = "card-sub-heading",
            subHeadingParams = Seq(CardSubHeadingParam(subHeadingParam = "in progress", subHeadingParamClasses = "font-small bold")))),
          links = Nil
        ))

      card.flatMap{ d =>
        Future.successful(Ok(view(d)))
      }
      //    fsConnector.getPsaFS(request.psaIdOrException.id).flatMap { psaFS =>
      //      val result = if (psaFS.seqPsaFSDetail.isEmpty) {
      //        Future.successful(Html(""))
      //      } else {
      //        val viewModel = aftPartialService.penaltiesAndCharges(psaFS.seqPsaFSDetail)
      //        renderer.render(
      //          template = "partials/psaSchemeDashboardPartial.njk",
      //          ctx = Json.obj("cards" -> Json.toJson(viewModel))
      //        )
      //      }
      //      result.map(Ok(_))
      //    }
      //  }
    }
  }
}
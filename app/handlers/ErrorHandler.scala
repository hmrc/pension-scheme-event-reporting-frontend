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

package handlers

import config.FrontendAppConfig
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc.{Request, RequestHeader, Result}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import views.html.{ErrorTemplate, NoDataEnteredErrorView, PageNotFoundErrorView}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ErrorHandler @Inject()(
                              val messagesApi: MessagesApi,
                              errorTemplateView: ErrorTemplate,
                              noDataEnteredView: NoDataEnteredErrorView,
                              pageNotFoundView: PageNotFoundErrorView,
                              config: FrontendAppConfig
                            ) (implicit val ec: ExecutionContext)
  extends FrontendErrorHandler with I18nSupport {

  def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: RequestHeader): Future[Html] = {
    implicit def requestImplicit: Request[?] = Request(request, "")

    Future.successful(errorTemplateView(pageTitle, heading, message))
  }

  override def notFoundTemplate(implicit request: RequestHeader): Future[Html] = {
    implicit def requestImplicit: Request[?] = Request(request, "")

    Future.successful(pageNotFoundView(config.contactHmrcURL))
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    exception match {
      case _ : TaxYearNotAvailableException =>
        Future.successful(Redirect(config.yourPensionSchemesUrl))
      case _: NothingToSubmitException =>
        Future.successful(Ok(noDataEnteredView(config.manageOverviewDashboardUrl)(Request(request, ""), request2Messages(request))))
      case _ => super.onServerError(request, exception)
    }
  }

}

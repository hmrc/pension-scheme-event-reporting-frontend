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

package utils.event19

import models.enumeration.EventType.Event19
import models.requests.DataRequest
import pages.EmptyWaypoints
import pages.event19.{CountryOrTerritoryPage, DateChangeMadePage, Event19CheckYourAnswersPage}
import play.api.libs.json.JsBoolean
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import services.CompileService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Event19UserAnswerValidation @Inject()(compileService: CompileService) {
  def validateAnswers()(implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    val countryOrTerritoryAnswer = request.userAnswers.get(CountryOrTerritoryPage)
    val dateAnswer = request.userAnswers.get(DateChangeMadePage)

    val originalUserAnswers = request.userAnswers
    val isCountryUkEuOrEEA = originalUserAnswers.get(CountryOrTerritoryPage.booleanPath)
    val countryCodeForNonUkEuOrEEACountries = "ZZ"

    val maybeUpdatedAnswers = isCountryUkEuOrEEA.collect {
      case JsBoolean(false) =>
        originalUserAnswers.setOrException(CountryOrTerritoryPage, countryCodeForNonUkEuOrEEACountries)
    }.getOrElse(originalUserAnswers)

    (countryOrTerritoryAnswer, dateAnswer) match {
      case (Some(_), Some(_)) => compileService.compileEvent(Event19, request.pstr, maybeUpdatedAnswers).map { _ =>
        Redirect(controllers.routes.EventSummaryController.onPageLoad(EmptyWaypoints).url)
      }
      case (Some(_), None) => Future.successful(
        Redirect(DateChangeMadePage.changeLink(EmptyWaypoints, Event19CheckYourAnswersPage).url)
      )
      case _ => Future.successful(
        Redirect(CountryOrTerritoryPage.changeLink(EmptyWaypoints, Event19CheckYourAnswersPage).url)
      )
    }
  }
}

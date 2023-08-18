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

package viewmodels.event19.checkAnswers

import models.UserAnswers
import pages.event19.CountryOrTerritoryPage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, SummaryListRow}
import utils.CountryOptions
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import javax.inject.{Inject, Singleton}

@Singleton
class CountryOrTerritorySummary @Inject()(val countryOptions: CountryOptions) {

  val fullNameOfChosenCountry: String => String = (countryCode: String) => {
    countryOptions.options.collectFirst {
      case country if country.value == countryCode => country.label
    }.getOrElse(countryCode)
  }

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage, isReadOnly: Boolean)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(CountryOrTerritoryPage).map {
      answer =>
        SummaryListRow(
          key     = "event19.countryOrTerritory.change.checkYourAnswersLabel",
          value   = ValueViewModel(fullNameOfChosenCountry(answer)),
          actions = if (isReadOnly) None else {
            Some(Actions(items = Seq(
              ActionItemViewModel("site.change", CountryOrTerritoryPage.changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("event19.countryOrTerritory.change.hidden"))
            )))
          }
        )
    }
}

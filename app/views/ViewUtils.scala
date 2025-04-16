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

package views

import org.apache.commons.lang3.StringUtils
import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.errormessage.ErrorMessage

object ViewUtils {

  def title(form: Form[?], title: String, section: Option[String] = None)(implicit messages: Messages): String =
    titleNoForm(
      title   = s"${errorPrefix(form)} ${messages(title)}",
      section = section
    )

  def titleWithErrors(errorMessage: Option[ErrorMessage], title: String, section: Option[String] = None)(implicit messages: Messages): String = {

    def errorPrefix: String = {
      if (errorMessage.isDefined) messages("error.browser.title.prefix") else StringUtils.EMPTY
    }

    titleNoForm(
      title = s"$errorPrefix ${messages(title)}",
      section = section
    )
  }

  def titleNoForm(title: String, section: Option[String] = None)(implicit messages: Messages): String =
    s"${messages(title)} - ${section.fold(StringUtils.EMPTY)(messages(_) + " - ")}${messages("service.name")} - ${messages("site.govuk")}"

  def errorPrefix(form: Form[?])(implicit messages: Messages): String = {
    if (form.hasErrors || form.hasGlobalErrors) messages("error.browser.title.prefix") else StringUtils.EMPTY
  }
}

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

package models.requests

import models.TaxYear.getSelectedTaxYear
import models.enumeration.VersionStatus.Submitted
import models.{LoggedInUser, UserAnswers}
import pages.{EventReportingOverviewPage, VersionInfoPage}
import play.api.mvc.{Request, WrappedRequest}

abstract class RequiredSchemeDataRequest[A](request: Request[A]) extends WrappedRequest[A](request) {
  def pstr: String

  def schemeName: String

  def returnUrl: String

  def loggedInUser: LoggedInUser
}

case class OptionalDataRequest[A](
                                   pstr: String,
                                   schemeName: String,
                                   returnUrl: String,
                                   request: Request[A],
                                   loggedInUser: LoggedInUser,
                                   userAnswers: Option[UserAnswers]
                                 ) extends RequiredSchemeDataRequest[A](request) {
  def isReportSubmitted: Boolean = {
    userAnswers match {
      case None => false
      case Some(ua) => ua.get(VersionInfoPage).exists(_.status == Submitted)
    }
  }
}

case class DataRequest[A](pstr: String,
                          schemeName: String,
                          returnUrl: String,
                          request: Request[A],
                          loggedInUser: LoggedInUser,
                          userAnswers: UserAnswers
                         ) extends RequiredSchemeDataRequest[A](request) {

  // read only is true if the version selected is less than the current report version:
  def readOnly(): Boolean = {
    val versionSelected: Int = userAnswers.get(VersionInfoPage).map(g => g.version) match {
      case Some(value) => value
      case _ => throw new RuntimeException("No version selected")
    }

    val taxYear = getSelectedTaxYear(userAnswers)
    userAnswers.get(EventReportingOverviewPage) match {
      case Some(value) => value.find(_.taxYear.equals(taxYear)) match {
        case Some(erOverview) =>
          val isMostRecentVersionInCompileState = erOverview.versionDetails.map(_.compiledVersionAvailable)
          val reportVersion: Int = isMostRecentVersionInCompileState match {
            case Some(true) => erOverview.versionDetails.map(_.numberOfVersions).getOrElse(1)
            case Some(false) => erOverview.versionDetails.map(_.numberOfVersions).getOrElse(1)
            case _ => throw new RuntimeException("Missing event report compile information")
          }
          versionSelected < reportVersion
        case None => false
      }
      case _ => throw new RuntimeException("No Event Report Overview information")
    }
  }

  def isReportSubmitted: Boolean = {
    userAnswers.get(VersionInfoPage).exists(_.status == Submitted)
  }
}
